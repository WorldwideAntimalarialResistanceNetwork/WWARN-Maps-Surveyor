package org.wwarn.mapcore.client.components.customwidgets;

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

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.controls.MapTypeControlOptions;
import com.google.gwt.maps.client.events.dragend.DragEndMapEvent;
import com.google.gwt.maps.client.events.dragend.DragEndMapHandler;
import com.google.gwt.maps.client.events.idle.IdleMapEvent;
import com.google.gwt.maps.client.events.idle.IdleMapHandler;
import com.google.gwt.maps.client.events.zoom.ZoomChangeMapEvent;
import com.google.gwt.maps.client.events.zoom.ZoomChangeMapHandler;
import com.google.gwt.maps.client.overlays.MapCanvasProjection;
import com.google.gwt.maps.client.overlays.OverlayView;
import com.google.gwt.maps.client.overlays.overlayhandlers.OverlayViewMethods;
import com.google.gwt.maps.client.overlays.overlayhandlers.OverlayViewOnAddHandler;
import com.google.gwt.maps.client.overlays.overlayhandlers.OverlayViewOnDrawHandler;
import com.google.gwt.maps.client.overlays.overlayhandlers.OverlayViewOnRemoveHandler;
import com.google.gwt.user.client.ui.*;

import java.util.List;

/**
* Encapsulates most of map functionality
* References:
* - GenericMarker
* Goals:
* Ensure that all com.google.gwt.maps.* is hidden from the client.
* Provide extension points for handling markers and map popups.
* Use MAP v3 api...
* User: nigel
*/
public class GenericMapWidget extends Composite {
    private final Builder builder;

    AbsolutePanel absoluteMapContentOverlayPanel = new AbsolutePanel();

    private final String mapWidgetStyleName = "mapWidget";
    private final MapWidget mapWidget;
    public static final int LEGEND_X_INDENT = 42; //just to the right of the map zoom controls
    public static final int FILTERS_PANEL_Y_POS = 45;
    private int legendPixelsFromBottom;
    final private SimplePanel legendWidgetPlaceHolder = new SimplePanel();
    final private SimplePanel filtersDisplayWidgetPlaceHolder = new SimplePanel();
    private List<GenericMarker> markers;
    private MapCanvasProjection mapCanvasProjection;
    final private PopupPanel loadingPanelPopup = new PopupPanel();

    private GenericMapWidget() {
        this.builder = null;
        this.mapWidget = new MapWidget(null);
        absoluteMapContentOverlayPanel.add(filtersDisplayWidgetPlaceHolder, calcFiltersPanelXPos(), FILTERS_PANEL_Y_POS);
    }

    public GenericMapWidget(Builder builder) {
        this.builder = builder;
        initWidget(this.absoluteMapContentOverlayPanel);
        MapOptions mapOptions = setupDisplay(builder.options);
        mapWidget = new MapWidget(mapOptions);
        mapWidget.setSize(Integer.toBinaryString(builder.mapWidth), Integer.toString(builder.mapHeight) + "px");

        final OverlayView overlayView = OverlayView.newInstance(mapWidget,
                new OverlayViewOnDrawHandler() {
                    @Override
                    public void onDraw(OverlayViewMethods overlayViewMethods) {
                        mapCanvasProjection = overlayViewMethods.getProjection();
                    }
            },
                new OverlayViewOnAddHandler() {
                    @Override
                    public void onAdd(OverlayViewMethods overlayViewMethods) {

                    }
            },
                new OverlayViewOnRemoveHandler() {
                    @Override
                    public void onRemove(OverlayViewMethods overlayViewMethods) {

                    }
        });

        absoluteMapContentOverlayPanel.add(mapWidget);
        absoluteMapContentOverlayPanel.add(filtersDisplayWidgetPlaceHolder, calcFiltersPanelXPos(), FILTERS_PANEL_Y_POS);
    }

    public void indicateLoading() {
        loadingPanelPopup.setWidget(new HTML("Loading data... please wait"));
        loadingPanelPopup.show();
        loadingPanelPopup.center();
        loadingPanelPopup.setPopupPosition(loadingPanelPopup.getAbsoluteLeft(), mapWidget.getAbsoluteTop());
    }

    public void removeLoadingIndicator() {
        loadingPanelPopup.hide();
    }

    public MapCanvasProjection getMapCanvasProjection() {
        return mapCanvasProjection;
    }

    private MapOptions setupDisplay(MapOptions options) {
        absoluteMapContentOverlayPanel.setWidth("100%");
        absoluteMapContentOverlayPanel.setStyleName(mapWidgetStyleName);

        //absolute panel needs an explicitly set height
        absoluteMapContentOverlayPanel.setHeight(Integer.toString(builder.mapHeight));

        //setup map types
        MapTypeControlOptions mapTypeControlOptions = MapTypeControlOptions.newInstance();
        mapTypeControlOptions.setMapTypeIds(new MapTypeId[]{MapTypeId.TERRAIN, MapTypeId.SATELLITE, MapTypeId.HYBRID, MapTypeId.ROADMAP});
        options.setMapTypeControl(true);
        options.setMapTypeControlOptions(mapTypeControlOptions);
        options.setMapTypeId(MapTypeId.TERRAIN);


        options.setStreetViewControl(false);
        options.setScaleControl(true);
        options.setScrollWheel(false);

        options.setMinZoom(builder.minZoomLevel);
        options.setZoom(builder.zoomLevel);
        options.setCenter(builder.latLng);

        return options;
    }

    /**
     * Returns map zoom level
     * @return
     */
    public int getZoomLevel(){
        return mapWidget.getZoom();
    }
    public void setZoomLevel(int zoomLevel) {
        mapWidget.setZoom(zoomLevel);
    }

    public LatLng getCenter() {
        return mapWidget.getCenter();
    }
    public void setCenter(LatLng center) {
        mapWidget.setCenter(center);
    }

    /**
     * Set markers for the map
     * @param m
     */
    public void addMarkers(List<GenericMarker> m){
        this.markers = m;
        for (GenericMarker marker : m) {
            marker.setMap(mapWidget);
        }
    }

    public void clearMarkers(){
        if(markers == null){
            return;
        }
        for (GenericMarker marker : markers) {
            marker.clear();
        }
    }
    public HandlerRegistration onLoadComplete(final Runnable onLoadComplete){
        mapWidget.triggerResize(); // Added to prevent only single tile showing : http://stackoverflow.com/a/16348551/192040
        return mapWidget.addIdleHandler(new IdleMapHandler() {
            @Override
            public void onEvent(IdleMapEvent idleMapEvent) {
                onLoadComplete.run();
            }
        });
    }


    /**
     * Add zoom handler, event is not exposed at present
     * @param zoomhandler
     */
    public HandlerRegistration addMapZoomEndHandler(final Runnable zoomhandler){
        return mapWidget.addZoomChangeHandler(new ZoomChangeMapHandler() {
            @Override
            public void onEvent(ZoomChangeMapEvent zoomChangeMapEvent) {
                zoomhandler.run();
            }
        });
    }

    public HandlerRegistration addDragEndHandler(final Runnable draghandler) {
        return mapWidget.addDragEndHandler(new DragEndMapHandler() {
            @Override
            public void onEvent(DragEndMapEvent event) {
                draghandler.run();
            }
        });
    }

    public void justResizeMapWidget() {
        mapWidget.setWidth("100%");
    }

    public void resizeMapWidget() {
        justResizeMapWidget();

        absoluteMapContentOverlayPanel.setWidgetPosition(filtersDisplayWidgetPlaceHolder, calcFiltersPanelXPos(), FILTERS_PANEL_Y_POS);
    }

    public void setMapLegend(Widget legendImage, int legendPixelsFromBottom) {
        //setup map legend position
        absoluteMapContentOverlayPanel.add(legendWidgetPlaceHolder, LEGEND_X_INDENT, calcLegendPanelYPos(legendPixelsFromBottom));
        legendWidgetPlaceHolder.clear();
        absoluteMapContentOverlayPanel.setWidgetPosition(legendWidgetPlaceHolder, LEGEND_X_INDENT, calcLegendPanelYPos(legendPixelsFromBottom));
        legendWidgetPlaceHolder.setWidget(legendImage);
    }

    public void setMapFiltersDisplay(Widget filtersWidget) {
        //setup map filters display position
        absoluteMapContentOverlayPanel.add(filtersDisplayWidgetPlaceHolder, calcFiltersPanelXPos(), FILTERS_PANEL_Y_POS);
        filtersDisplayWidgetPlaceHolder.clear();
        absoluteMapContentOverlayPanel.setWidgetPosition(filtersDisplayWidgetPlaceHolder, calcFiltersPanelXPos(), FILTERS_PANEL_Y_POS);
        filtersDisplayWidgetPlaceHolder.setWidget(filtersWidget);
    }

    private int calcLegendPanelYPos(int legendPixelsFromBottom) {
        return mapWidget.getOffsetHeight() - legendPixelsFromBottom;
    }

    private int calcFiltersPanelXPos() {
        return mapWidget.getAbsoluteLeft() + mapWidget.getOffsetWidth() - 413;
    }

    protected MapWidget getInternalGoogleMapWidget() {
        return mapWidget;
    }

    public static class Builder {
        public static final String STOCK_NO_STUDIES_MSG = "No studies found matching the filters chosen.<br/>" +
                "Please choose less stringent criteria.";
        private int minZoomLevel = 0;
        private String noStudiesFoundMsg = STOCK_NO_STUDIES_MSG;
        private static final int DEFAULT_ZOOM_LEVEL = 2;
        private MapOptions options = MapOptions.newInstance();
        int mapHeight = 0;
        int mapWidth = 0;
        private int zoomLevel = DEFAULT_ZOOM_LEVEL;
        private LatLng latLng;

        /**
         * set max zoom out level
         * @param minZoomLevel
         * @return
         */
        public Builder setMinZoomLevel(int minZoomLevel) {
            this.minZoomLevel = minZoomLevel;
            return this;
        }

        /**
         * Set map zoom level
         * @param i
         * @return
         */
        public Builder setZoomLevel(int i) {
            this.zoomLevel = i;
            return this;
        }

        public Builder setCenter(double mapCentreLat, double mapCentreLon){
            latLng = LatLng.newInstance(mapCentreLat, mapCentreLon);
            return this;
        }

        /**
         * sent value to show, when no studies found
         * @param noStudiesFoundMsg
         * @return
         */
        public Builder setNoStudiesFoundMsg(String noStudiesFoundMsg) {
            this.noStudiesFoundMsg = noStudiesFoundMsg;
            return this;
        }

        /**
         * Setup map display properties
         * @param width value in px
         * @param height value in px
         */
        public Builder configureMapDimension(Integer width, Integer height) {
            this.mapHeight = height;
            this.mapWidth = width;
            return this;
        }

        /**
         * Call to return a map widget
         * @return
         */
        public GenericMapWidget createMapWidget() {
            validate();
            return new GenericMapWidget(this);
        }

        private void validate() {
            if(this.mapHeight == 0 || this.mapWidth == 0){
                throw new IllegalArgumentException("Expected map width and height to be set");
            }

            if(this.latLng == null){
                throw new IllegalArgumentException("Expected centre coordinates to be set");
            }
        }
    }
}
