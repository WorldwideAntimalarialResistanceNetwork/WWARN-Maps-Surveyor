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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.wwarn.mapcore.client.resources.OpenLayersV3Resources;

import java.util.List;

/**
 * Open layers Open street map implementation with offline support
 * limited to one layer of tiles
 */
public class OfflineMapWidget extends GenericMapWidget {
    private final MapBuilder builder;
    private static int GUID = 0;
    private JavaScriptObject map;
    private JavaScriptObject markerContainer;
    HTMLPanel htmlPanel = new HTMLPanel("");
    private String currentId;
    private Runnable loadCompleted;
    private boolean mapDrawCalled = false;
    private List<GenericMarker> markers;

    static {
        if (!isLoaded()) {
            load();
        }
    }

    private int minZoomLevel;
    private double mapCenterLat;
    private double mapCentreLon;
    private int zoomLevel;
    private CoordinatesLatLon centerCoordinatesLatLon;

    /**
     * Loads the offline library.
     */
    public static void load() {
        if (!isLoaded()) {
//            ScriptInjector.fromString(OpenLayersV3Resources.INSTANCE.proj4js().getText()).setWindow(ScriptInjector.TOP_WINDOW).inject();
            ScriptInjector.fromString(OpenLayersV3Resources.INSTANCE.js().getText()).setWindow(ScriptInjector.TOP_WINDOW).inject();
            StyleInjector.injectStylesheetAtStart(OpenLayersV3Resources.INSTANCE.css().getText());
        }
    }
    public static native boolean isLoaded()/*-{
        if (typeof $wnd.ol === "undefined" || $wnd.ol === null) {
            return false;
        }
        return true;
    }-*/;

    public OfflineMapWidget(MapBuilder mapBuilder) {
        this.builder = mapBuilder;
        mapBuilderToLocalAttributes(mapBuilder);
        currentId = generateUID();
        htmlPanel.getElement().setId(currentId);
        htmlPanel.setHeight(mapBuilder.mapHeight + "px");
        initializePopup(htmlPanel, currentId);
        initWidget(htmlPanel);
    }

    void initializePopup(HTMLPanel htmlPanel, String currentId) {
        final HTMLPanel popup = new HTMLPanel("");
        final String popupID = currentId+"Popup";
        popup.getElement().setId(popupID);
        htmlPanel.add(popup);
    }

    private void mapBuilderToLocalAttributes(MapBuilder mapBuilder) {
        minZoomLevel = mapBuilder.minZoomLevel;
        zoomLevel = mapBuilder.zoomLevel;
        centerCoordinatesLatLon = mapBuilder.centerCoordinatesLatLon;
        mapCenterLat = centerCoordinatesLatLon.getMapCenterLat();
        mapCentreLon = centerCoordinatesLatLon.getMapCentreLon();

//        Window.alert(String.valueOf(mapBuilder.mapHeight));
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        drawMap();
    }

    private void drawMap() {
        if(!mapDrawCalled) {
            drawBasicMap(this, currentId);
            mapDrawCalled = true;
        }
    }

    private static native void drawBasicMap(OfflineMapWidget offlineMapWidget, String mapContainerId)/*-{
        var ol = $wnd.ol;
        var $ = $wnd.$;
        var map, mapSource, markerContainer, boolHasRendered, iconFeature, iconStyle, mapCentreLon, mapCentreLat, zoomLevel;

        boolHasRendered = false;
        //markerContainer
        markerContainer = new ol.source.Vector({
            //create empty vector
        });

        mapCentreLon = offlineMapWidget.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapWidget::mapCentreLon;
        mapCentreLat = offlineMapWidget.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapWidget::mapCenterLat;
        zoomLevel = offlineMapWidget.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapWidget::zoomLevel;

        offlineMapWidget.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapWidget::markerContainer = markerContainer;


        var vectorLayer = new ol.layer.Vector({
            source: markerContainer
        });

        mapSource = new ol.source.MapQuest({layer: 'osm'});
        map = new ol.Map({
            target: mapContainerId,
            layers: [
                new ol.layer.Tile({
                    source: mapSource
                }),
                vectorLayer
            ],
            view: new ol.View({
                projection: 'EPSG:3857',
                center: ol.proj.transform([mapCentreLon, mapCentreLat], 'EPSG:4326', 'EPSG:3857'),
                zoom: zoomLevel
            }),
            interactions: ol.interaction.defaults({mouseWheelZoom: false})
        });
        offlineMapWidget.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapWidget::map = map;


        map.on("postrender", function () {
            if (!boolHasRendered) {
                boolHasRendered = true;
                offlineMapWidget.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapWidget::onLoadCompleted()();
            }
        }, false);

        mapSource.once('tileloadend', function (e) {
            map.updateSize();
        });


        //setup popup on click stuff
        var element = $wnd.document.getElementById(mapContainerId + 'Popup');
        var popup = new ol.Overlay({
            element: element,
            positioning: 'bottom-center',
            stopEvent: false
        });
        map.addOverlay(popup);

        // display popup on click
        map.on('click', function (evt) {
            var feature = map.forEachFeatureAtPixel(evt.pixel,
                function (feature, layer) {
                    return feature;
                });
            if (feature) {
                var geometry = feature.getGeometry();
                var coord = geometry.getCoordinates();
                popup.setPosition(coord);
                $(element).popover({
                    'placement': 'top',
                    'html': true,
                    'content': feature.get('name')
                });
                $(element).popover('show');
            } else {
                $(element).popover('destroy');
            }
        });


        // change mouse cursor when over marker
        map.on('pointermove', function (e) {
            if (e.dragging) {
                $(element).popover('destroy');
                return;
            }
            var pixel = map.getEventPixel(e.originalEvent);
            var hit = map.hasFeatureAtPixel(pixel);
            console.log(map.getTarget())
            console.log(map.getTarget().style)
            console.log(map.getTarget().cursor)
            //map.getTarget().style.cursor = hit ? 'pointer' : '';
        });


    }-*/;

    private static native void updateMap(OfflineMapWidget offlineMapWidget)/*-{
        var map = offlineMapWidget.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapWidget::map;
        map.updateSize();
    }-*/;

    void onLoadCompleted(){
        if(loadCompleted!=null) {
            loadCompleted.run();
        }
    }

    private String generateUID() {
        return "ol3MapsContainer" + GUID++;
    }


    @Override
    public void indicateLoading() {

    }

    @Override
    public void removeLoadingIndicator() {

    }

    @Override
    public int getZoomLevel() {
        return zoomLevel;
    }

    @Override
    public void setZoomLevel(int zoomLevel) {
        zoomLevel = zoomLevel;
    }

    @Override
    public CoordinatesLatLon getCenter() {
        return centerCoordinatesLatLon;
    }

    @Override
    public void setCenter(CoordinatesLatLon center) {
        centerCoordinatesLatLon = center;
    }

    @Override
    public HandlerRegistration onLoadComplete(Runnable onLoadComplete) {
        loadCompleted = onLoadComplete;
        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                loadCompleted = null;
            }
        };
    }

    @Override
    public HandlerRegistration addMapZoomEndHandler(Runnable zoomhandler) {
        return null;
    }

    @Override
    public HandlerRegistration addDragEndHandler(Runnable draghandler) {
        return null;
    }

    @Override
    public void justResizeMapWidget() {
        updateMap(this);
    }

    @Override
    public void resizeMapWidget() {
        updateMap(this);
    }

    @Override
    public void setMapLegend(LegendOptions legendOptions) {
//        throw new UnsupportedOperationException("Not yet implemented..");
    }


    @Override
    public void setMapFiltersDisplay(Widget filtersWidget) {

    }

    public JavaScriptObject getMarkerContainer() {
        return markerContainer;
    }
}
