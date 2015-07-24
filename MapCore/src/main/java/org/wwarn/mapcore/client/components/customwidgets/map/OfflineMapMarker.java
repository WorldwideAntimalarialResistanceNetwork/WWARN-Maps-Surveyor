package org.wwarn.mapcore.client.components.customwidgets.map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * Offline marker implementation
 */
public class OfflineMapMarker<T> extends GenericMarker<T> {
    private final double markerLat;
    private final double markerLon;
    private JavaScriptObject markerContainer;
    private OfflineMapWidget offlineMapWidget;
    private String markerTitle;
    private Point markerAnchorPoint;
    private Integer zIndex;
    private String markerIconPath;
    private OfflineMapMarker<T> referenceToOfflineMapMarker;


    public OfflineMapMarker(MapMarkerBuilder mapMarkerBuilder, T markerContext) {
        super(mapMarkerBuilder.map, markerContext);
        if (!(mapMarkerBuilder.map instanceof OfflineMapWidget)) {
            throw new IllegalArgumentException("Expected an offline map widget");
        }
        this.referenceToOfflineMapMarker = this;
        this.markerTitle = mapMarkerBuilder.title;
        this.offlineMapWidget = (OfflineMapWidget) mapMarkerBuilder.map;
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
         //todo fine equvalent for open layers
        }


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

    private static native void setupMarker(OfflineMapMarker offlineMapMarker, JavaScriptObject markerContainer)/*-{
        var ol = $wnd.ol, markerTitle, markerIconPath, zIndex, styleProperties, markerAnchorPoint, markerGeom, markerLat, markerLon;
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
            // Web Mercator coordinate system EPSG: 3857, is a projected coordinate from sphere to flat surface, this is the same as EPSG:900913/'EPSG:3785'/'GOOGLE'/'EPSG:102113' and used by google maps, openstreetmap and bing
            // geo coords if they were got from map, they might be  EPSG:27700 instead of EPSG:3857
            markerGeom = new ol.geom.Point(ol.proj.transform([markerLon, markerLat], 'EPSG:4326',   'EPSG:3857'));
        }

        iconFeature = new ol.Feature({
            geometry: markerGeom,
            name: ''+markerTitle,
        });

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

    }

    @Override
    void addMouseOverHandler(MarkerCallBackEventHandler<GenericMarker> mouseOverCallback) {

    }

    @Override
    void addMouseOutMoveHandler(MarkerCallBackEventHandler<GenericMarker> mouseOutCallback) {

    }



    @Override
    public void setMap(GenericMapWidget mapWidget) {

    }

    @Override
    void clear() {

    }

    @Override
    void displayPopup(Widget infoWindowWidget) {

    }

    @Override
    public void setupMarkerHoverLabel(MarkerHoverLabelBuilder markerHoverLabelBuilder) {

    }
}
