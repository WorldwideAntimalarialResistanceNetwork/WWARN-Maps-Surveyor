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
public class GoogleV3MapWidget extends GenericMapWidget {
    private final MapBuilder builder;

    AbsolutePanel absoluteMapContentOverlayPanel = new AbsolutePanel();

    private final String mapWidgetStyleName = "mapWidget";
    private final MapWidget mapWidget;
    private int legendPixelsFromBottom;
    final private SimplePanel legendWidgetPlaceHolder = new SimplePanel();
    final private SimplePanel filtersDisplayWidgetPlaceHolder = new SimplePanel();
    private List<GenericMarker> markers;
    private MapCanvasProjection mapCanvasProjection;
    final private PopupPanel loadingPanelPopup = new PopupPanel();

    GoogleV3MapWidget() {
        this.builder = null;
        this.mapWidget = new MapWidget(null);
        absoluteMapContentOverlayPanel.add(filtersDisplayWidgetPlaceHolder, calcFiltersPanelXPos(), FILTERS_PANEL_Y_POS);
    }

    public GoogleV3MapWidget(MapBuilder builder) {
        this.builder = builder;
        initWidget(this.absoluteMapContentOverlayPanel);
        MapOptions options = MapOptions.newInstance();
        MapOptions mapOptions = setupDisplay(options);
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

    @Override
    public void indicateLoading() {
        loadingPanelPopup.setWidget(new HTML("Loading data... please wait"));
        loadingPanelPopup.show();
        loadingPanelPopup.center();
        loadingPanelPopup.setPopupPosition(loadingPanelPopup.getAbsoluteLeft(), mapWidget.getAbsoluteTop());
    }

    @Override
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
        options.setCenter(getLatLng(builder.coordinatesLatLon));

        return options;
    }

    private LatLng getLatLng(CoordinatesLatLon coordinatesLatLon) {
        return LatLng.newInstance(coordinatesLatLon.getMapCenterLat(), coordinatesLatLon.getMapCentreLon());
    }

    /**
     * Returns map zoom level
     * @return
     */
    @Override
    public int getZoomLevel(){
        return mapWidget.getZoom();
    }
    @Override
    public void setZoomLevel(int zoomLevel) {
        mapWidget.setZoom(zoomLevel);
    }

    @Override
    public CoordinatesLatLon getCenter() {
        final LatLng center = mapWidget.getCenter();
        return CoordinatesLatLon.newInstance(center.getLatitude(), center.getLongitude());
    }

    @Override
    public void setCenter(CoordinatesLatLon center) {
        mapWidget.setCenter(getLatLng(center));
    }

    /**
     * Set markers for the map
     * @param m
     */
    @Override
    public void addMarkers(List<GenericMarker> m){
        this.markers = m;
        for (GenericMarker marker : m) {
            marker.setMap(mapWidget);
        }
    }

    @Override
    public void clearMarkers(){
        if(markers == null){
            return;
        }
        for (GenericMarker marker : markers) {
            marker.clear();
        }
    }
    @Override
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
    @Override
    public HandlerRegistration addMapZoomEndHandler(final Runnable zoomhandler){
        return mapWidget.addZoomChangeHandler(new ZoomChangeMapHandler() {
            @Override
            public void onEvent(ZoomChangeMapEvent zoomChangeMapEvent) {
                zoomhandler.run();
            }
        });
    }

    @Override
    public HandlerRegistration addDragEndHandler(final Runnable draghandler) {
        return mapWidget.addDragEndHandler(new DragEndMapHandler() {
            @Override
            public void onEvent(DragEndMapEvent event) {
                draghandler.run();
            }
        });
    }

    @Override
    public void justResizeMapWidget() {
        mapWidget.setWidth("100%");
    }

    @Override
    public void resizeMapWidget() {
        justResizeMapWidget();

        absoluteMapContentOverlayPanel.setWidgetPosition(filtersDisplayWidgetPlaceHolder, calcFiltersPanelXPos(), FILTERS_PANEL_Y_POS);
    }

    @Override
    public void setMapLegend(Widget legendImage, int legendPixelsFromBottom) {
        //setup map legend position
        absoluteMapContentOverlayPanel.add(legendWidgetPlaceHolder, LEGEND_X_INDENT, calcLegendPanelYPos(legendPixelsFromBottom));
        legendWidgetPlaceHolder.clear();
        absoluteMapContentOverlayPanel.setWidgetPosition(legendWidgetPlaceHolder, LEGEND_X_INDENT, calcLegendPanelYPos(legendPixelsFromBottom));
        legendWidgetPlaceHolder.setWidget(legendImage);
    }

    @Override
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

    public MapWidget getInternalGoogleMapWidget() {
        return mapWidget;
    }

}
