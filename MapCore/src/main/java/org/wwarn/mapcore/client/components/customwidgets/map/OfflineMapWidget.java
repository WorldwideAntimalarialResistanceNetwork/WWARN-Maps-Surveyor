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
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

import java.util.List;

/**
 * Open layers Open street map implementation with offline support
 * limited to one layer of tiles
 */
public class OfflineMapWidget extends GenericMapWidget {
    private final MapBuilder builder;
    private static int GUID = 0;
    private JavaScriptObject markerContainer;

    HTMLPanel htmlPanel = new HTMLPanel("");
    private String currentId;
    private Runnable loadCompleted;
    private boolean mapDrawCalled = false;
    private List<GenericMarker> markers;

    public OfflineMapWidget(MapBuilder mapBuilder) {
        this.builder = mapBuilder;
        currentId = generateUID();
        htmlPanel.getElement().setId(currentId);
        initWidget(htmlPanel);
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
        var map, markerContainer, boolHasRendered, iconFeature, iconStyle;
        boolHasRendered = false;
        //markerContainer
        markerContainer = new $wnd.ol.source.Vector({
            //create empty vector
        });
        this.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapWidget::markerContainer = markerContainer;

//        //create a bunch of icons and add to source vector
//        for (var i=0;i<50;i++){
//
//            iconFeature = new $wnd.ol.Feature({
//              geometry: new
//                  $wnd.ol.geom.Point($wnd.ol.proj.transform([Math.random()*360-180, Math.random()*180-90], 'EPSG:4326',   'EPSG:3857')),
//            name: 'Null Island ' + i,
//            population: 4000,
//            rainfall: 500
//            });
//            markerContainer.addFeature(iconFeature);
//        }

        //create the style
        iconStyle = new $wnd.ol.style.Style({
          image: new $wnd.ol.style.Icon( ({
            anchor: [0.5, 46],
            anchorXUnits: 'fraction',
                    anchorYUnits: 'pixels',
                    opacity: 0.75,
                    src: 'http://ol3js.org/en/master/examples/data/icon.png'
            }))
        });

        //add the feature vector to the layer vector, and apply a style to whole layer
        var vectorLayer = new $wnd.ol.layer.Vector({
        source: markerContainer,
        style: iconStyle
        });


        map = new $wnd.ol.Map({
            target: mapContainerId,
            layers: [
                new $wnd.ol.layer.Tile({
                    source: new $wnd.ol.source.OSM()
                }),
                vectorLayer
            ],
            view: new $wnd.ol.View({
                center: $wnd.ol.proj.transform([37.41, 8.82], 'EPSG:4326', 'EPSG:3857'),
                zoom: 4
            })
        });

        map.on("postrender", function(){
            if(!boolHasRendered){
                boolHasRendered = true;
                offlineMapWidget.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapWidget::onLoadCompleted()();
            }
        }, false);


    }-*/;

    void onLoadCompleted(){
        if(loadCompleted!=null) {
            loadCompleted.run();

        }
    }

    private String generateUID() {
        return "highMapsContainer" + GUID++;
    }


    @Override
    public void indicateLoading() {

    }

    @Override
    public void removeLoadingIndicator() {

    }

    @Override
    public int getZoomLevel() {
        return 0;
    }

    @Override
    public void setZoomLevel(int zoomLevel) {

    }

    @Override
    public CoordinatesLatLon getCenter() {
        return null;
    }

    @Override
    public void setCenter(CoordinatesLatLon center) {

    }

    @Override
    public void addMarkers(List<GenericMarker> m) {
        this.markers = m;
        for (GenericMarker marker : m) {
            //addMarker();

        }

    }

    @Override
    public void clearMarkers() {

    }

    @Override
    public HandlerRegistration onLoadComplete(Runnable onLoadComplete) {
        loadCompleted = onLoadComplete;
        return new HandlerRegistration() {
            @Override
            public void removeHandler() {}
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

    }

    @Override
    public void resizeMapWidget() {

    }

    @Override
    public void setMapLegend(Widget legendImage, int legendPixelsFromBottom) {

    }

    @Override
    public void setMapFiltersDisplay(Widget filtersWidget) {

    }
}
