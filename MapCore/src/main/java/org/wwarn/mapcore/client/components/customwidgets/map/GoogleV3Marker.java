package org.wwarn.mapcore.client.components.customwidgets.map;

/*
 * #%L
 * MapCore
 * %%
 * Copyright (C) 2013 - 2014 University of Oxford
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the University of Oxford nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.dragend.DragEndMapEvent;
import com.google.gwt.maps.client.events.dragend.DragEndMapHandler;
import com.google.gwt.maps.client.events.dragstart.DragStartMapEvent;
import com.google.gwt.maps.client.events.dragstart.DragStartMapHandler;
import com.google.gwt.maps.client.events.mouseout.MouseOutMapEvent;
import com.google.gwt.maps.client.events.mouseout.MouseOutMapHandler;
import com.google.gwt.maps.client.events.mouseover.MouseOverMapEvent;
import com.google.gwt.maps.client.events.mouseover.MouseOverMapHandler;
import com.google.gwt.maps.client.maptypes.Projection;
import com.google.gwt.maps.client.overlays.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import org.wwarn.mapcore.client.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * A class intended to wrap marker implementation...
 * expose mouse over and mouse out event, as well as click handler
 * main function of a marker:
 * * display specific marker icons based on some criteria, this is specified through MarkerIconPathBuilder interface
 * * display a label on hover, handled via MarkerHoverLabelBuilder interface
 * * display a map pop up panel on click, handled via MarkerClickInfoWindowBuilder interface
 * For convenience marker allows the client to simply register a popup or hover label
 * User: nigel
 * Date: 07/08/13
 * Time: 16:06
 * @see MarkerIconPathBuilder
 * @see MarkerHoverLabelBuilder
 * @see MarkerClickInfoWindowBuilder
 */
public class GoogleV3Marker<T> extends GenericMarker<T> {
    private final Marker marker;
    private final GoogleV3Marker<T> referenceToThisInstanceOfGenericMarker = this;

    public GoogleV3Marker(MapMarkerBuilder mapMarkerBuilder, T markerContext) {
        super(mapMarkerBuilder.map, markerContext);
        if (!(mapMarkerBuilder.map instanceof GoogleV3MapWidget)) {
            throw new IllegalArgumentException("Expected an instance of GoogleV3MapWidget");
        }
        this.markerIconPathBuilder = mapMarkerBuilder.markerIconPathBuilder;
        LatLng latLng = LatLng.newInstance(mapMarkerBuilder.markerLat, mapMarkerBuilder.markerLon);
        GoogleV3MapWidget v3MapWidget = (GoogleV3MapWidget) mapMarkerBuilder.map;
        MarkerOptions options = MarkerOptions.newInstance();
        options.setPosition(latLng);

        if (mapMarkerBuilder.preventMarkerRepeatingHorizontally) {
            options.setDraggable(true);
        }


        options.setMap(v3MapWidget.getInternalGoogleMapWidget());
        if (!StringUtils.isEmpty(mapMarkerBuilder.title)) {
            options.setTitle(mapMarkerBuilder.title);
        }

        marker = Marker.newInstance(options);
        GoogleV3Marker<T> genericMarker = new GoogleV3Marker<T>(marker, markerContext, v3MapWidget);
        if (markerIconPathBuilder == null) { /*no default marker icon set, so use default */
            genericMarker.setIcon(new GenericMarker.DefaultMarkerIconPathBuilder(), null, null);
        } else {
            genericMarker.setIcon(markerIconPathBuilder, castToGooglPoint(mapMarkerBuilder.markerAnchor), mapMarkerBuilder.zIndex);
        }

        if (mapMarkerBuilder.useDispersion) {
            setupMarkerDispersion(v3MapWidget, latLng, genericMarker);
        }

        if (mapMarkerBuilder.preventMarkerRepeatingHorizontally) {
            setupMarkerFixedNoRepeatHack(genericMarker);
        }

    }

    private Point castToGooglPoint(org.wwarn.mapcore.client.components.customwidgets.map.Point p) {
        if(p == null){
            return null;
        }
        return Point.newInstance(p.getX(), p.getY());
    }

    private GoogleV3Marker(Marker marker, T context, GenericMapWidget mapWidget) {
        super(mapWidget, context);
        this.marker = marker;
    }

    private Marker getMarker() {
        return this.marker;
    }

    public void clear() {
        getMarker().clear();
    }

    public void displayPopup(Widget infoWindowWidget) {
        MarkerClickInfoWindowSingleton markerClickInfoWindowSingleton = MarkerClickInfoWindowSingleton.getInstance();
        infoWindowWidget.setVisible(false);
        RootPanel.get().add(infoWindowWidget); // this hack fixes various event related bugs for gwt components embedded in info window
        infoWindowWidget.setVisible(true);
        markerClickInfoWindowSingleton.display(infoWindowWidget, getMap(), getMarker());
    }

    public void setupMarkerHoverLabel(final MarkerHoverLabelBuilder markerHoverLabelBuilder){
        if(markerHoverLabelBuilder == null){
            throw new IllegalArgumentException("markerHoverLabelBuilder is required");
        }
        final MarkerHoverLabel markerHoverLabel = new MarkerHoverLabel(markerHoverLabelBuilder);

        addMouseOverHandler(new MarkerCallBackEventHandler<GenericMarker>() {
            @Override
            public void run(final GenericMarker sourceElement) {
                markerHoverLabel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
                    /**
                     * Provides the opportunity to set the position of the PopupPanel right before the PopupPanel is shown.
                     * The offsetWidth and offsetHeight values of the PopupPanel are made available to allow for positioning based on its size.
                     * http://www.gwtproject.org/javadoc/latest/com/google/gwt/user/client/ui/PopupPanel.html
                     * @param offsetWidth The offsetWidth and offsetHeight values of the PopupPanel are made available to allow for positioning based on its size.
                     * @param offsetHeight The offsetWidth and offsetHeight values of the PopupPanel are made available to allow for positioning based on its size.
                     */
                    @Override
                    public void setPosition(int offsetWidth, int offsetHeight) {
                        LatLng position = referenceToThisInstanceOfGenericMarker.getMarker().getPosition();
                        final GoogleV3Marker.ProjectionUtility projectionUtility = new GoogleV3Marker.ProjectionUtility();
                        final GoogleV3MapWidget googleV3MapWidget = (GoogleV3MapWidget) (GoogleV3MapWidget)genericMapWidget;
                        Point markerPosition = projectionUtility.calculatePixelPositionCentreFromMapCanvasPosition(position, googleV3MapWidget);
                        int popupX = (int) (markerPosition.getX() - Math.round(offsetWidth));
                        int popupY = (int) ((markerPosition.getY()) - (Math.round(offsetHeight + 25)));
                        markerHoverLabel.setPopupPosition(popupX, popupY);
                    }
                });

            }
        });

        addMouseOutMoveHandler(new MarkerCallBackEventHandler<GenericMarker>() {
            @Override
            public void run(GenericMarker sourceElement) {
                markerHoverLabel.hide();
            }
        });
    }

    private class MarkerHoverLabel extends PopupPanel {

        private MarkerHoverLabel(MarkerHoverLabelBuilder markerHoverLabelBuilder) {
            //auto hide
            super(true);
            setWidget(markerHoverLabelBuilder.build(getContext()));
        }

    }

    public void setMap(GenericMapWidget mapWidget) {
        if((mapWidget instanceof GoogleV3MapWidget)){
            final MapWidget internalGoogleMapWidget = ((GoogleV3MapWidget) mapWidget).getInternalGoogleMapWidget();
            getMarker().setMap(internalGoogleMapWidget);
        }
    }

    public final void addMouseOutMoveHandler(final MarkerCallBackEventHandler<GenericMarker> mouseOutCallback){
        getMarker().addMouseOutMoveHandler(new MouseOutMapHandler() {
            @Override
            public void onEvent(MouseOutMapEvent mouseOutMapEvent) {
                mouseOutCallback.run(referenceToThisInstanceOfGenericMarker);
            }
        });
    }

    public final void addMouseOverHandler(final MarkerCallBackEventHandler<GenericMarker> mouseOverCallback){
        getMarker().addMouseOverHandler(new MouseOverMapHandler() {
            @Override
            public void onEvent(MouseOverMapEvent mouseOverMapEvent) {
                mouseOverCallback.run(referenceToThisInstanceOfGenericMarker);
            }
        });
    }

    public final void addClickHandler(final MarkerCallBackEventHandler<GenericMarker> clickCallback){
        getMarker().addClickHandler(new ClickMapHandler() {
            @Override
            public void onEvent(ClickMapEvent clickMapEvent) {
                clickCallback.run(referenceToThisInstanceOfGenericMarker);
            }
        });
    }

    protected final void setMap(GoogleV3MapWidget map) {
        if(map == null || map.getInternalGoogleMapWidget() == null){
            throw new IllegalArgumentException("map instance cannot be null");
        }
        getMarker().setMap(map.getInternalGoogleMapWidget());
    }

    private void setIcon(MarkerIconPathBuilder markerIconPath, final Point markerAnchor, final Integer zIndex) {
        if(markerIconPath instanceof MarkerIconPathBuilderAsync){
            // do async...
            ((MarkerIconPathBuilderAsync) markerIconPath).getMarkerIconPath(context, new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable throwable) {
                    throw new IllegalStateException(throwable);
                }

                @Override
                public void onSuccess(String markerIconPath) {
                    setupIcon(markerAnchor, zIndex, markerIconPath);
                }
            });
        }else if(markerIconPath instanceof MarkerIconPathBuilder){
            setupIcon(markerAnchor, zIndex, markerIconPath.getMarkerIconPath(context));
        }
    }

    private void setupIcon(Point markerAnchor, Integer zIndex, String iconPath) {
        MarkerImage markerImage = MarkerImage.newInstance(iconPath);
        getMarker().setIcon(markerImage);
        if(markerAnchor!=null) {
            markerImage.setAnchor(markerAnchor);
        }
        if(zIndex!=null) {
            getMarker().setZindex(zIndex);
        }
    }

    private MapWidget getMap() {
        return getMarker().getMap();
    }



    static class ProjectionUtility{


        /**
         * Calculation based on discussion found <a href="http://krasimirtsonev.com/blog/article/google-maps-api-v3-convert-latlng-object-to-actual-pixels-point-object">here</a>
         * <pre>
         function fromLatLngToPoint(latLng, map) {
         var topRight = map.getProjection().fromLatLngToPoint(map.getBounds().getNorthEast());
         var bottomLeft = map.getProjection().fromLatLngToPoint(map.getBounds().getSouthWest());
         var scale = Math.pow(2, map.getZoom());
         var worldPoint = map.getProjection().fromLatLngToPoint(latLng);
         return new google.maps.Point((worldPoint.x - bottomLeft.x) * scale, (worldPoint.y - topRight.y) * scale);
         }
         * </pre>
         * an alternative implementation is available from google which explicitly implements the <a href="">Mercator projection algorithm</a>
         * @deprecated Fails to calculate x coordinate correctly after map panned/zoomed
         */
        private Point calculatePixelPositionFrom(LatLng position, GoogleV3MapWidget mapWidget) {
            Point defaultPoint = null;
            MapWidget map = mapWidget.getInternalGoogleMapWidget();
            Projection projection = map.getProjection();
            Point topRight = projection.fromLatLngToPoint(map.getBounds().getNorthEast(), defaultPoint);
            Point bottomLeft = projection.fromLatLngToPoint(map.getBounds().getSouthWest(), defaultPoint);
            double scale = Math.pow(2, map.getZoom());
            Point worldPoint = projection.fromLatLngToPoint(position, defaultPoint);
            double xCoordinate = Math.floor((worldPoint.getX() - bottomLeft.getX()) * scale);
            double yCoordinate = Math.floor((worldPoint.getY() - topRight.getY()) * scale);
            return Point.newInstance(xCoordinate, yCoordinate);
        }


        /**
         * An alternative calculation based on the center point
         * @deprecated Fails to calculate x coordinate correctly after map panned/zoomed
         * @param position
         * @param mapWidget
         * @return
         */
        private Point calculatePixelPositionCentreFrom(LatLng position, GoogleV3MapWidget mapWidget) {
            Point defaultPoint = null;
            MapWidget map = mapWidget.getInternalGoogleMapWidget();
            Projection projection = map.getProjection();

            Point topRight = projection.fromLatLngToPoint(map.getBounds().getNorthEast(), defaultPoint);
            Point center = projection.fromLatLngToPoint(map.getBounds().getCenter(), defaultPoint);
            double scale = Math.pow(2, map.getZoom());
            Point markerPoint = projection.fromLatLngToPoint(position, defaultPoint);
            final int mapClientWidth = map.getDiv().getClientWidth();
            double xCoordinate = Math.floor((markerPoint.getX() - center.getX()) * scale) + mapClientWidth/2;
            double yCoordinate = Math.floor((markerPoint.getY() - topRight.getY()) * scale);
            return Point.newInstance(xCoordinate, yCoordinate);
        }

        /**
         * The recommended alternative for calculating pixel position from map canvas
         * @param position
         * @param mapWidget
         * @return
         */
        private Point calculatePixelPositionCentreFromMapCanvasPosition(LatLng position, GoogleV3MapWidget mapWidget) {
                GoogleV3MapWidget map = mapWidget;
                Point defaultPoint = null;
                final MapCanvasProjection mapCanvasProjection = map.getMapCanvasProjection();
                Point point = mapCanvasProjection.fromLatLngToContainerPixel(position);
                double x = point.getX() + map.getAbsoluteLeft();
                double y = point.getY() + map.getAbsoluteTop();
                return Point.newInstance(x, y);
        }

    }

    /**
     *  A utility for dispersing overlapping map points, resulting in higher density of markers being observed
     * around same point.
     */
    static class DispersionUtility{
        private final HashMap<Integer, DispersionVector> dispersionLookup;

        private final double[] zoomlevels = {2.4,1.9,1.4,0.9,0.6,0.3,0.2,0.1,0.06,0.03,0.015,0.01,0.005,0.003};

        private Map<String, Integer> locationPosition = new HashMap<String, Integer>();

        private static DispersionUtility ourInstance = new DispersionUtility();
        private boolean hasRegisteredZoomEndHandler = false;

        public static DispersionUtility getInstance(GenericMapWidget genericMapWidget) {
            return ourInstance.registerZoomEnd(genericMapWidget);
        }

        /**
         * Registers zoom end handler, enforces a single registration
         * reset locationPosition after map zoom has changed.
         * @param genericMapWidget
         */
        private DispersionUtility registerZoomEnd(GenericMapWidget genericMapWidget){
            if(!hasRegisteredZoomEndHandler){
                genericMapWidget.addMapZoomEndHandler(new Runnable() {
                    @Override
                    public void run() {
                        locationPosition = new HashMap<String, Integer>();
                    }
                });
            }
            hasRegisteredZoomEndHandler = true;
            return this;
        }

        /**
         * Logic to reposition the markers after zoom, this is needed to adjust the spread, otherwise the
         * markers will be appear in the wrong position after zooming in.
         * @return
         * @param markerPosition
         * @param genericMapWidget
         * @param markerToAdjust
         */
        private Runnable adjustDispersionOnMapZoom(final LatLng markerPosition, final GenericMapWidget genericMapWidget, final GoogleV3Marker markerToAdjust){
            return new Runnable() {
                @Override
                public void run() {
                    final DispersionUtility.DispersionVector dispersionVector = DispersionUtility.getInstance(genericMapWidget).getDispersion(markerPosition, genericMapWidget.getZoomLevel());
                    LatLng adjustedPostionAfterZoom = LatLng.newInstance(markerPosition.getLatitude() + dispersionVector.getLat(), markerPosition.getLongitude() + dispersionVector.getLon());
                    markerToAdjust.setPosition(adjustedPostionAfterZoom);
                }
            };
        }

        private DispersionUtility() {
            dispersionLookup = createDispersion();
        }

        private Integer getMarkerPositionFrom(LatLng markerCoordinates){
            final String markerCoordinatesKey = markerCoordinates.getToString();
            return locationPosition.get(markerCoordinatesKey);
        }

        private Integer storeMarkerAndPosition(LatLng markerCoordinates, int position){
            final String markerCoordinatesKey = markerCoordinates.getToString();
            return locationPosition.put(markerCoordinatesKey, position);
        }

        /**
         * Call once per marker to register marker position.
         * @param markerCoordinates
         * @return
         */
        public int registerMarkerPosition(LatLng markerCoordinates) {
            Integer positionCount = getMarkerPositionFrom(markerCoordinates);
            if(positionCount == null){
                positionCount = 0;
            }
            storeMarkerAndPosition(markerCoordinates, ++positionCount);
            return positionCount;
        }

        /**
         * A getter for the dispersion class if the value exceeds the index of the dispersion table
         * a coordinate of 0.0, 0.0 is returned
         * @param markerCoordinates
         * @param zoomLevel
         * @return
         */
        public DispersionVector getDispersion (LatLng markerCoordinates, int zoomLevel)
        {
            int position;
            position = this.registerMarkerPosition(markerCoordinates);

//            position = getMarkerPositionFrom(markerCoordinates);

            if (dispersionLookup.get(new Integer(position)) == null)
                return new DispersionVector(0.0, 0.0);
            else {
                DispersionVector myDispersionVector = dispersionLookup.get(new Integer(position));
                double lat = 0.0,lon=0.0;
                if(myDispersionVector.getLat()!=0.0)
                    lat = myDispersionVector.getLat() * zoomlevels[zoomLevel];
                if(myDispersionVector.getLon()!=0.0)
                    lon = myDispersionVector.getLon() * zoomlevels[zoomLevel];

                return new DispersionVector(lat,lon);
            }
        }

        private class DispersionVector
        {
            final double latDispersion;
            final double lonDispersion;

            DispersionVector(double latDispersion,
                             double lonDispersion)
            {
                this.latDispersion = latDispersion;
                this.lonDispersion = lonDispersion;
            }

            public double getLat()
            {
                return latDispersion;
            }

            public double getLon()
            {
                return lonDispersion;
            }
        }

        private HashMap<Integer, DispersionVector> createDispersion ()
        {
            double INITIAL_RING_COUNT = 5;
            double PIN_SIZE = 0.3;
            double radius = PIN_SIZE * 1.3;
            double ringCount = INITIAL_RING_COUNT;
            double angle;
            double length;
            int j = 0;

            HashMap<Integer, DispersionVector> hm = new HashMap<Integer, DispersionVector>();
            DispersionVector d = new DispersionVector(0.0, 0.0);
            hm.put(j, d);
            j++;

            for (int r = 0; r < 5; r++)     // Do five concentric rings
            {
                for (int i = 0; i < ringCount; i++)
                {
                    // Calculate polar coordinates
                    angle =  (Math.PI * 2.0 / (ringCount + 0.0001) * i);    //  watch out for division by zero here
                    length = radius;
                    double x = Math.cos(angle) * length;
                    double y = Math.sin(angle) * length;

                    d = new DispersionVector(x, y);

                    hm.put(j, d);

                    j++;
                }
                double newRadius = radius + PIN_SIZE * 1.3;
                ringCount = (int)(ringCount * newRadius / radius);
                radius = newRadius;
            }

            return hm;
        }


    }

    public void setPosition(LatLng latLng) {
        getMarker().setPosition(latLng);
    }




    /**
     * Prevent marker from repeating horizontally by setting marker drag and adjusting drag behaviour to reset
     *
     * @param genericMarker
     * @param <T>
     */
    private <T> void setupMarkerFixedNoRepeatHack(GoogleV3Marker<T> genericMarker) {
        final Marker marker1 = genericMarker.getMarker();
        final LatLng[] initialPosition = new LatLng[1];
        marker1.addDragStartHandler(new DragStartMapHandler() {
            @Override
            public void onEvent(DragStartMapEvent dragStartMapEvent) {
                initialPosition[0] = marker1.getPosition();
            }
        });

        marker1.addDragEndHandler(new DragEndMapHandler() {
            @Override
            public void onEvent(DragEndMapEvent dragEndMapEvent) {
                marker1.setPosition(initialPosition[0]);
            }
        });
    }

    private void setupMarkerDispersion(GenericMapWidget mapWidget, LatLng latLng, GoogleV3Marker genericMarker) {
        final GoogleV3Marker.DispersionUtility dispersionUtil = GoogleV3Marker.DispersionUtility.getInstance(mapWidget);
        final Runnable zoomHandler = dispersionUtil.adjustDispersionOnMapZoom(latLng, mapWidget, genericMarker);
        mapWidget.addMapZoomEndHandler(zoomHandler);
        zoomHandler.run();
    }

}
