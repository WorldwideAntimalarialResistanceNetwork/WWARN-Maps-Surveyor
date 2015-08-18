package org.wwarn.mapcore.client.components.customwidgets.map;

/*
 * #%L
 * MapCore
 * %%
 * Copyright (C) 2013 - 2015 University of Oxford
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

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

/**
 * Offline marker implementation
 */
public class OfflineMapMarker<T> extends GenericMarker<T> {
    public static final String MARKER_ID_PREFIX = "ol3MapsMarkerID";
    private final double markerLat;
    private final double markerLon;
    private static int GUID = 0;


    private String markerID;
    private JavaScriptObject markerContainer;
    private JavaScriptObject markerFeature;

    private OfflineMapWidget offlineMapWidget;
    private String markerTitle;
    private Point markerAnchorPoint;
    private Integer zIndex;
    private String markerIconPath;
    private OfflineMapMarker<T> referenceToOfflineMapMarker;
    private MarkerCallBackEventHandler<GenericMarker> mouseOverCallback;
    private MarkerCallBackEventHandler<GenericMarker> mouseOutCallback;
    private List<MarkerCallBackEventHandler> clickCallbackHandlers = new ArrayList<>();
    private double xPositionForClick;
    private double yPositionForClick;


    public OfflineMapMarker(MapMarkerBuilder mapMarkerBuilder, T markerContext) {
        super(mapMarkerBuilder.map, markerContext);
        if (!(mapMarkerBuilder.map instanceof OfflineMapWidget)) {
            throw new IllegalArgumentException("Expected an offline map widget");
        }
        this.markerID = generateUID();
        this.referenceToOfflineMapMarker = this;
        this.markerTitle = mapMarkerBuilder.title;
        this.offlineMapWidget = (OfflineMapWidget) mapMarkerBuilder.map;
//        this.offlineMapWidget.addMarkers();
        this.markerContainer = offlineMapWidget.getMarkerContainer();
        this.markerIconPathBuilder = mapMarkerBuilder.markerIconPathBuilder;
        this.markerLat = mapMarkerBuilder.markerLat;
        this.markerLon = mapMarkerBuilder.markerLon;
        if (markerIconPathBuilder == null) { /*no default marker icon set, so use default */
            this.setIcon(new GenericMarker.DefaultMarkerIconPathBuilder(), null, null);
        } else {
            this.setIcon(markerIconPathBuilder, mapMarkerBuilder.markerAnchor, mapMarkerBuilder.zIndex);
        }
        if (mapMarkerBuilder.preventMarkerRepeatingHorizontally) {
         //todo find equivalent for open layers
        }
    }

    public String getMarkerID() {
        return markerID;
    }

    private String generateUID() {
        return MARKER_ID_PREFIX + GUID++;
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

    private void setupIcon(Point markerAnchorPoint, Integer zIndex, String iconPath) {
        this.markerAnchorPoint = markerAnchorPoint;
        this.zIndex = zIndex;
        this.markerIconPath = iconPath;
        setupMarker(this, markerContainer);
    }

    public void fireClickEvent(double x, double y){
        xPositionForClick = x;
        yPositionForClick = y;

        for (MarkerCallBackEventHandler clickCallbackHandler : clickCallbackHandlers) {
            clickCallbackHandler.run(this);
        }
    }

    private static native void setupMarker(OfflineMapMarker offlineMapMarker, JavaScriptObject markerContainer)/*-{
        var ol = $wnd.ol, iconFeature, markerID, markerTitle, markerIconPath, zIndex, styleProperties, markerAnchorPoint, markerGeom, markerLat, markerLon;

        // assignments from java attributes to local variables
        markerID = offlineMapMarker.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapMarker::markerID;
        markerTitle = offlineMapMarker.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapMarker::markerTitle;
        markerIconPath = offlineMapMarker.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapMarker::markerIconPath;
        zIndex = offlineMapMarker.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapMarker::zIndex;
        markerAnchorPoint = offlineMapMarker.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapMarker::markerAnchorPoint;
        markerLat = offlineMapMarker.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapMarker::markerLat;
        markerLon  = offlineMapMarker.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapMarker::markerLon;
        //$wnd.proj4.defs("EPSG:27700", '+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717' +
        //    ' +x_0=400000 +y_0=-100000 +ellps=airy +datum=OSGB36 +units=m +no_defs');
        if(!(typeof markerAnchorPoint === "undefined" || markerAnchorPoint === null)){
            markerGeom = ol.Projection.transformWithCodes(new ol.geom.Point(new ol.Coordinate(markerAnchorPoint.getX(), markerAnchorPoint.getY())), 'EPSG:4326',   'EPSG:3857');
        }else{
            // WGS84/EPSG:4326 used by Google Earth, and  Open Street Map database is stored in a gcs with units decimal degrees & datum, it also the Geographic coordinate http://en.wikipedia.org/wiki/Google_Earth#Imagery_and_coordination
            // Web Mercator coordinate system EPSG: 3857, is a projected coordinate from sphere to flat surface, this is the same as EPSG:900913/'EPSG:3785'/'GOOGLE'/'EPSG:102113' and used by ol.Source.OSM, google maps, openstreetmap and bing
            // geo coords if they were got from map, they might be  EPSG:27700 instead of EPSG:3857
            markerGeom = new ol.geom.Point(ol.proj.transform([markerLon, markerLat], 'EPSG:4326',   'EPSG:3857'));
        }

        iconFeature = new ol.Feature({
            geometry: markerGeom,
            name: '' + markerTitle,
            markerID: '' + markerID
        });

        // assignments from local variables to java class attributes
        offlineMapMarker.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapMarker::markerFeature = iconFeature;

        styleProperties = {
            opacity: 0.75,
            src: markerIconPath
        }

        if(typeof zIndex === "undefined" || zIndex === null){
            styleProperties.zIndex = zIndex;
        }

        iconFeature.setStyle(new ol.style.Style({
                image: new ol.style.Icon( (styleProperties))
            })
        )

        markerContainer.addFeature(iconFeature);
    }-*/;

    @Override
    public void addClickHandler(MarkerCallBackEventHandler<GenericMarker> clickCallback) {
        this.clickCallbackHandlers.add(clickCallback);
    }

    @Override
    void addMouseOverHandler(MarkerCallBackEventHandler<GenericMarker> mouseOverCallback) {
        this.mouseOverCallback = mouseOverCallback;
    }

    @Override
    void addMouseOutMoveHandler(MarkerCallBackEventHandler<GenericMarker> mouseOutCallback) {
        this.mouseOutCallback = mouseOutCallback;
    }



    @Override
    public void setMap(GenericMapWidget mapWidget) {
        if (!(mapWidget instanceof OfflineMapWidget)) {
            throw new IllegalArgumentException("Expected offline map widget");
        }
        this.offlineMapWidget = (OfflineMapWidget) mapWidget;
    }

    @Override
    void clear() {
//        offlineMapWidget.removeMarker(this);
        if(markerContainer!=null) {
            removeMarker(this, markerContainer);
        }
    }

    private static native void removeMarker(OfflineMapMarker offlineMapMarker, JavaScriptObject markerContainer)/*-{
        var feature;
        feature = (offlineMapMarker.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapMarker::markerFeature);
        if(((typeof feature !== "undefined") && feature !== null)){
            markerContainer.removeFeature(feature);
        }
    }-*/;


    @Override
    void displayPopup(Widget infoWindowWidget) {
        infoWindowWidget.setVisible(false);
        RootPanel.get().add(infoWindowWidget); // this hack fixes various event related bugs for gwt components embedded in info window
        final JavaScriptObject mapPopupOverlay = this.offlineMapWidget.getMapPopupOverlay();
        final JavaScriptObject mapPopupContainerElement = this.offlineMapWidget.getMapPopupContainerElement();
        infoWindowWidget.setVisible(true);
        final HTMLPanel panel = this.offlineMapWidget.getPopupElement();
        final Element popupElement = panel.getElement();
        final int padding = 50;
        final int offsetWidth = infoWindowWidget.getOffsetWidth() + padding;
        final int offsetHeight = infoWindowWidget.getOffsetHeight() + padding;
        setDataAttribute(popupElement, "<div style='width:"+offsetWidth+"px;height:"+offsetHeight+"px;' id='" + markerID + "'></div>");
        displayPopup(this, mapPopupOverlay, mapPopupContainerElement, this.markerFeature, popupElement);
        /*
        * Works around the horrors of HTMLPanel.wrap(DOM.getElementById("moo")),
        * which fails with "A widget that has an existing parent widget may not be added to the detach list",
        * if the parent is already a widget, you cannont use HTMLPanel wrap to create a widget from a dom element
        * instead use the addAndReplaceElement to find the element and replace with widget...
        * */
        panel.addAndReplaceElement(infoWindowWidget, markerID);
    }

    protected void setDataAttribute(Element e , String value) {
        e.setAttribute("data-content", value);
    }

    private static native void displayPopup(OfflineMapMarker offlineMapMarker, JavaScriptObject mapPopupOverlay, JavaScriptObject mapPopupContainerElement, JavaScriptObject markerFeature, Element element)/*-{
        var ol = $wnd.ol;
        var $ = $wnd.$;
        //setup popup on click stuff
        //var element = mapPopupContainerElement;
        var feature = markerFeature;
        var popup = mapPopupOverlay;
        var xPositionForClick = (offlineMapMarker.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapMarker::xPositionForClick);
        var yPositionForClick = (offlineMapMarker.@org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapMarker::yPositionForClick);

        if (feature) {
            var geometry = feature.getGeometry();
            var coord = geometry.getCoordinates();
            //coord = ol.proj.transform(coord, 'EPSG:3857',   'EPSG:4326');
            //popup.setPosition([yPositionForClick,xPositionForClick])
            popup.setPosition(coord);

            var popOverSettings = {
                'placement': 'top',
                'html': true
                //,'content': popupHTMLContent // this gets overwritten later for dynamic popups content
            };
            var popover = $(element).popover(popOverSettings);

            $(element).popover('show');
            $(".popover").css("max-width", "none");
        }
    }-*/;

    @Override
    public void setupMarkerHoverLabel(MarkerHoverLabelBuilder markerHoverLabelBuilder) {
        //todo hover logic
    }
}
