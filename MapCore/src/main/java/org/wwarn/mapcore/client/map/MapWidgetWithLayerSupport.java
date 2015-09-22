//package org.wwarn.mapcore.client.map;

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

//
//import org.wwarn.mapcore.client.map.data.CountryHighlightDetails;
//import org.wwarn.mapcore.client.map.data.CountryOutline;
//import org.wwarn.mapcore.client.map.data.CountryPartOutline;
//import com.google.gwt.core.client.GWT;
//import com.google.gwt.maps.client.MapType;
//import com.google.gwt.maps.client.MapWidget;
//import com.google.gwt.maps.client.control.HierarchicalMapTypeControl;
//import com.google.gwt.maps.client.control.LargeMapControl3D;
//import com.google.gwt.maps.client.control.ScaleControl;
//import com.google.gwt.maps.client.plot.LatLng;
//import com.google.gwt.maps.client.plot.Point;
//import com.google.gwt.maps.client.overlay.EncodedPolyline;
//import com.google.gwt.maps.client.overlay.Marker;
//import com.google.gwt.maps.client.overlay.Polygon;
//import com.google.gwt.user.client.ui.*;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
///**
//* User: richardc
//* Date: 25-Jun-2009
//* Time: 12:28:48
//*/
//public class MapWidgetWithLayerSupport {
//
//    final private MapWidget mapWidget;
//
//    public static final int DEFAULT_ZOOM_LEVEL = 2;
//    public static final int LEGEND_X_INDENT = 14;
//    public static final int FILTERS_PANEL_Y_POS = 45;
//
//    final AbsolutePanel absoluteMapContentOverlayPanel = new AbsolutePanel();
//
//    HashMap<String, CountryHighlightDetails> currentHighlightDetailsList = new HashMap<String, CountryHighlightDetails>();
//
//    HashMap<String, CountryOutline> countryEPolylineHashMap = new HashMap<String, CountryOutline>();
//
//    private HashMap<String, Polygon> countryCodePolygonHashMap = new HashMap<String, Polygon>();
//    final private SimplePanel filtersDisplayWidgetPlaceHolder = new SimplePanel();
//    final private SimplePanel legendWidgetPlaceHolder = new SimplePanel();
//    final private PopupPanel loadingPanelPopup = new PopupPanel();
//    final private PopupPanel mapMessagePopup = new PopupPanel(false);
//    private static final String stockNoStudiesMsg = "No studies found matching the filters chosen.<br/>" +
//            "Please choose less stringent criteria.";
//    private int legendPixelsFromBottom;
//    private String noStudiesFoundMsg;
//
//    public MapWidgetWithLayerSupport(MapWidget mapWidget) {
//        this(mapWidget, 0);
//    }
//
//    public MapWidgetWithLayerSupport(MapWidget mapWidget, int maxZoomOutLevel, String noStudiesFoundMsg) {
//        this.mapWidget = mapWidget;
//        this.noStudiesFoundMsg = noStudiesFoundMsg;
//
//        //limits the maximum zoom out level
//        limitMaxZoomOut(maxZoomOutLevel);
//
//        absoluteMapContentOverlayPanel.add(mapWidget, 0, 0);
//        absoluteMapContentOverlayPanel.add(filtersDisplayWidgetPlaceHolder, calcFiltersPanelXPos(), FILTERS_PANEL_Y_POS);
//        absoluteMapContentOverlayPanel.add(legendWidgetPlaceHolder, LEGEND_X_INDENT, calcLegendPanelYPos());
//
//        loadingPanelPopup.setWidget(new HTML("Loading data... please wait"));
//    }
//
//    public MapWidgetWithLayerSupport(MapWidget mapWidget, int maxZoomOutLevel) {
//        this(mapWidget, maxZoomOutLevel, stockNoStudiesMsg);
//    }
//
//    private native void limitMaxZoomOut(int maxZoomOutLevel) /*-{
//
//        $wnd.G_PHYSICAL_MAP.getMinimumResolution = function () {
//            return maxZoomOutLevel
//        };
//        $wnd.G_NORMAL_MAP.getMinimumResolution = function () {
//            return maxZoomOutLevel
//        };
//        $wnd.G_SATELLITE_MAP.getMinimumResolution = function () {
//            return maxZoomOutLevel
//        };
//        $wnd.G_HYBRID_MAP.getMinimumResolution = function () {
//            return maxZoomOutLevel
//        };
//
//    }-*/;
//
//    public void setCountryEPolylineHashMap(HashMap<String, CountryOutline> countryEPolylineHashMap) {
//        this.countryEPolylineHashMap = countryEPolylineHashMap;
//    }
//
//    public MapWidget getMapWidget() {
//        return mapWidget;
//    }
//
//    //TODO add this back in, no equivalent yet found
//    public void clearOverlays() {
//        mapWidget.clearOverlays();
//    }
//
//    public void setMarkers(List<Marker> markers) {
//        mapMessagePopup.hide();
//        if (markers.size() > 0) {
//
//            GWT.log("Adding markers to map");
//            for (Marker marker : markers) {
//                mapWidget.addOverlay(marker);
//            }
//            GWT.log(" - markers added to map");
//
//        } else {
//
//            GWT.log("No markers available for selection");
//            indicateNoMarkersForSelectedFilters();
//
//        }
//    }
//
//    public void displayMessage(String message) {
//        mapMessagePopup.setWidget(new HTML(message));
//        mapMessagePopup.center();
//        mapMessagePopup.show();
//    }
//
//
//    private void indicateNoMarkersForSelectedFilters() {
//        displayMessage(noStudiesFoundMsg);
//    }
//
//    public void clearTheseMarkers(List<Marker> markers) {
//        for (Marker marker : markers) {
//            mapWidget.removeOverlay(marker);
//        }
//    }
//
//    /**
//     * @see org.wwarn.mapcore.client.components.customwidgets.GenericMapWidget#justResizeMapWidget()
//     */
//    public void justResizeMapWidget() {
//        mapWidget.setWidth("100%");
//    }
//
//    /**
//     * @see org.wwarn.mapcore.client.components.customwidgets.GenericMapWidget#resizeMapWidget()
//     */
//    public void resizeMapWidget() {
//
//        justResizeMapWidget();
//
//        absoluteMapContentOverlayPanel.setWidgetPosition(filtersDisplayWidgetPlaceHolder, calcFiltersPanelXPos(), FILTERS_PANEL_Y_POS);
////        absoluteMapContentOverlayPanel.setWidgetPosition(legendWidgetPlaceHolder, LEGEND_X_INDENT, calcLegendPanelYPos());
//    }
//
//    /**
//     * @see org.wwarn.mapcore.client.components.customwidgets.GenericMapWidget.setupDisplay()
//     * @param width
//     * @param height
//     */
//    public void configureMapDimension(Integer width, Integer height) {
//
//        absoluteMapContentOverlayPanel.setWidth("100%");
//
//        /*FIXME mapWidget shows as an error because css files aren't linked correctly.
//        * mapWidget css should be defined in Map.css. Currently it's defined in datamap.css*/
//        absoluteMapContentOverlayPanel.setStyleName("mapWidget");
//
//        //absolute panel needs an explicitly set height
//        absoluteMapContentOverlayPanel.setHeight(height.toString());
//
//        //mapwidget initially needs an explicit width to prevent an initial incomplete loading bug.
//        //width is set to 100% in resizeMapWidget
//        mapWidget.setSize(width.toString(), "100%");
//
//        mapWidget.addControl(new LargeMapControl3D());
//        mapWidget.addControl(new HierarchicalMapTypeControl());
//        mapWidget.addMapType(MapType.getPhysicalMap());
////        mapWidget.addMapType(MapType.getEarthMap());   // TODO: Fix problem with Google Map API key
////        mapWidget.addControl(new SmallZoomControl3D());
////        mapWigetMolecular.addControl(new OverviewMapControl());
//        mapWidget.addControl(new ScaleControl());
//        mapWidget.setScrollWheelZoomEnabled(false);
//        mapWidget.setCurrentMapType(MapType.getPhysicalMap());
//
//    }
//
//    public void drawCountryPolygonsFromCountryHighlightDetails(List<CountryHighlightDetails> highlightDetailsList) {
//        List<String> countryCodes = new ArrayList<String>();
//        for (CountryHighlightDetails countryHighlightDetails : highlightDetailsList) {
//            countryCodes.add(countryHighlightDetails.getCountryCode());
//        }
//
//        List<CountryHighlightDetails> countriesToChange = new ArrayList<CountryHighlightDetails>();
//        List<CountryHighlightDetails> countriesToAdd = new ArrayList<CountryHighlightDetails>();
//
//        HashMap<String, CountryHighlightDetails> newHighlightDetailsList = (HashMap<String, CountryHighlightDetails>) currentHighlightDetailsList.clone();
//
//        for (CountryHighlightDetails countryHighlightDetails : highlightDetailsList) {
//            if (countryEPolylineHashMap.containsKey(countryHighlightDetails.getCountryCode())) {
//
//                // Find out if the polygon with the same specification is already on the map.
//                if (currentHighlightDetailsList.containsKey(countryHighlightDetails.getCountryCode())) {
//
//                    if (!currentHighlightDetailsList.get(countryHighlightDetails.getCountryCode()).equals(countryHighlightDetails)) {
//                        countriesToChange.add(countryHighlightDetails);
//                        newHighlightDetailsList.remove(countryHighlightDetails.getCountryCode());
//                        newHighlightDetailsList.put(countryHighlightDetails.getCountryCode(), countryHighlightDetails);
//                    } else {
//                        // It's already on the map.
//                    }
//                } else {
//                    countriesToAdd.add(countryHighlightDetails);
//                    newHighlightDetailsList.put(countryHighlightDetails.getCountryCode(), countryHighlightDetails);
//                }
//            }
//        }
//
//        // Remove any polygon which isn't part of the new country list
//        for (String code : currentHighlightDetailsList.keySet()) {
//            if (!countryCodes.contains(code)) {
//                newHighlightDetailsList.remove(code);
//                removeCountryPolygon(code);
//            }
//        }
//
//        // Add any new polygons
//        for (CountryHighlightDetails countryHighlightDetails : countriesToAdd) {
//            addCountryPolygonWithOpacity(countryHighlightDetails);
//        }
//
//        // Update any polygons (country polygons which have different properties now)
//        for (CountryHighlightDetails countryHighlightDetails : countriesToChange) {
//            updateCountryPolygonWithOpacity(countryHighlightDetails);
//        }
//
//        currentHighlightDetailsList = newHighlightDetailsList;
//    }
//
//    private void updateCountryPolygonWithOpacity(CountryHighlightDetails countryHighlightDetails) {
//        Polygon polygon = generatePolygonForCountry(countryHighlightDetails);
//        mapWidget.removeOverlay(countryCodePolygonHashMap.get(countryHighlightDetails.getCountryCode()));
//        countryCodePolygonHashMap.remove(countryHighlightDetails.getCountryCode());
//        countryCodePolygonHashMap.put(countryHighlightDetails.getCountryCode(), polygon);
//        mapWidget.addOverlay(polygon);
//
//    }
//
//    private void removeCountryPolygon(String countryCode) {
//        mapWidget.removeOverlay(countryCodePolygonHashMap.get(countryCode));
//        countryCodePolygonHashMap.remove(countryCode);
//    }
//
//    private void addCountryPolygonWithOpacity(CountryHighlightDetails countryHighlightDetails) {
//
//        Polygon polygon = generatePolygonForCountry(countryHighlightDetails);
//        mapWidget.addOverlay(polygon);
//        countryCodePolygonHashMap.put(countryHighlightDetails.getCountryCode(), polygon);
//    }
//
//    private Polygon generatePolygonForCountry(CountryHighlightDetails countryHighlightDetails) {
//        CountryOutline countryOutline = countryEPolylineHashMap.get(countryHighlightDetails.getCountryCode());
//
//        EncodedPolyline[] encodedPolylines = new EncodedPolyline[countryOutline.getCountryPartOutlines().size()];
//
//        for (int i = 0; i < countryOutline.getCountryPartOutlines().size(); i++) {
//            CountryPartOutline partOutline = countryOutline.getCountryPartOutlines().get(i);
//
//            Integer lineThickness = 1;
//
//            if (countryHighlightDetails.getOpacity() > 0.25) {
//                lineThickness = 3;
//            }
//
//            EncodedPolyline encodedPolyline = EncodedPolyline.newInstance(
//                    partOutline.getEncodedPoints(),                 //points
//                    4,                                              //zoomFactor
//                    partOutline.getLevels(),                        //levels
//                    4,                                              //numLevels
//                    countryHighlightDetails.getColourStringRGB(),   //colour
//                    lineThickness,                                  //weight
//                    1.0);                                           //opacity
//            encodedPolylines[i] = encodedPolyline;
//        }
//
//        return Polygon.fromEncoded(encodedPolylines,
//                true,
//                countryHighlightDetails.getColourStringRGB(),
//                countryHighlightDetails.getOpacity(), true);
//    }
//
//    public AbsolutePanel createMapPanel() {
//        return absoluteMapContentOverlayPanel;
//    }
//
//    /**
//     * @see org.wwarn.mapcore.client.components.customwidgets.GenericMapWidget#setMapLegend(com.google.gwt.user.client.ui.Widget, int)
//     * @param legendImage
//     * @param legendPixelsFromTop
//     */
//    public void setMapLegend(Widget legendImage, int legendPixelsFromTop) {
//        this.legendPixelsFromBottom = legendPixelsFromTop;
//        absoluteMapContentOverlayPanel.setWidgetPosition(legendWidgetPlaceHolder, LEGEND_X_INDENT, calcLegendPanelYPos());
//        legendWidgetPlaceHolder.setWidget(legendImage);
//    }
//
//    public SimplePanel getLegendWidgetPlaceHolder ()
//    {
//        return legendWidgetPlaceHolder;
//    }
//
//    private int calcLegendPanelYPos() {
//        return mapWidget.getOffsetHeight() - legendPixelsFromBottom;
//    }
//
//    public void setMapFiltersDisplay(Widget filtersDisplayWidget) {
//        filtersDisplayWidgetPlaceHolder.setWidget(filtersDisplayWidget);
//    }
//
//    private int calcFiltersPanelXPos() {
//        return mapWidget.getAbsoluteLeft() + mapWidget.getOffsetWidth() - 413;
//    }
//
//    /**
//     * Migrated to
//     * org.wwarn.mapcore.client.components.customwidgets.GenericMapWidget.Builder#setZoomLevel(int)
//     * @param i
//     */
//    public void setZoomLevel(int i) {
//        mapWidget.setZoomLevel(i);
//    }
//
//    public void setCenter(LatLng latLng) {
//        mapWidget.setCenter(latLng);
//    }
//
//    public Point convertLatLngToContainerPixel(LatLng latlng) {
//        return mapWidget.convertLatLngToContainerPixel(latlng);
//    }
//
//    public int getAbsoluteLeft() {
//        return mapWidget.getAbsoluteLeft();
//    }
//
//    public int getAbsoluteTop() {
//        return mapWidget.getAbsoluteTop();
//    }
//
//    public void indicateLoading() {
//        loadingPanelPopup.show();
//        loadingPanelPopup.center();
//        loadingPanelPopup.setPopupPosition(loadingPanelPopup.getAbsoluteLeft(), mapWidget.getAbsoluteTop());
//    }
//
//    public void removeLoadingIndicator() {
//        loadingPanelPopup.hide();
//    }
//}package org.wwarn.mapcore.client.map;
//
//import org.wwarn.mapcore.client.map.data.CountryHighlightDetails;
//import org.wwarn.mapcore.client.map.data.CountryOutline;
//import org.wwarn.mapcore.client.map.data.CountryPartOutline;
//import com.google.gwt.core.client.GWT;
//import com.google.gwt.maps.client.MapType;
//import com.google.gwt.maps.client.MapWidget;
//import com.google.gwt.maps.client.control.HierarchicalMapTypeControl;
//import com.google.gwt.maps.client.control.LargeMapControl3D;
//import com.google.gwt.maps.client.control.ScaleControl;
//import com.google.gwt.maps.client.plot.LatLng;
//import com.google.gwt.maps.client.plot.Point;
//import com.google.gwt.maps.client.overlay.EncodedPolyline;
//import com.google.gwt.maps.client.overlay.Marker;
//import com.google.gwt.maps.client.overlay.Polygon;
//import com.google.gwt.user.client.ui.*;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
///**
//* User: richardc
//* Date: 25-Jun-2009
//* Time: 12:28:48
//*/
//public class MapWidgetWithLayerSupport {
//
//    final private MapWidget mapWidget;
//
//    public static final int DEFAULT_ZOOM_LEVEL = 2;
//    public static final int LEGEND_X_INDENT = 14;
//    public static final int FILTERS_PANEL_Y_POS = 45;
//
//    final AbsolutePanel absoluteMapContentOverlayPanel = new AbsolutePanel();
//
//    HashMap<String, CountryHighlightDetails> currentHighlightDetailsList = new HashMap<String, CountryHighlightDetails>();
//
//    HashMap<String, CountryOutline> countryEPolylineHashMap = new HashMap<String, CountryOutline>();
//
//    private HashMap<String, Polygon> countryCodePolygonHashMap = new HashMap<String, Polygon>();
//    final private SimplePanel filtersDisplayWidgetPlaceHolder = new SimplePanel();
//    final private SimplePanel legendWidgetPlaceHolder = new SimplePanel();
//    final private PopupPanel loadingPanelPopup = new PopupPanel();
//    final private PopupPanel mapMessagePopup = new PopupPanel(false);
//    private static final String stockNoStudiesMsg = "No studies found matching the filters chosen.<br/>" +
//            "Please choose less stringent criteria.";
//    private int legendPixelsFromBottom;
//    private String noStudiesFoundMsg;
//
//    public MapWidgetWithLayerSupport(MapWidget mapWidget) {
//        this(mapWidget, 0);
//    }
//
//    public MapWidgetWithLayerSupport(MapWidget mapWidget, int maxZoomOutLevel, String noStudiesFoundMsg) {
//        this.mapWidget = mapWidget;
//        this.noStudiesFoundMsg = noStudiesFoundMsg;
//
//        //limits the maximum zoom out level
//        limitMaxZoomOut(maxZoomOutLevel);
//
//        absoluteMapContentOverlayPanel.add(mapWidget, 0, 0);
//        absoluteMapContentOverlayPanel.add(filtersDisplayWidgetPlaceHolder, calcFiltersPanelXPos(), FILTERS_PANEL_Y_POS);
//        absoluteMapContentOverlayPanel.add(legendWidgetPlaceHolder, LEGEND_X_INDENT, calcLegendPanelYPos());
//
//        loadingPanelPopup.setWidget(new HTML("Loading data... please wait"));
//    }
//
//    public MapWidgetWithLayerSupport(MapWidget mapWidget, int maxZoomOutLevel) {
//        this(mapWidget, maxZoomOutLevel, stockNoStudiesMsg);
//    }
//
//    private native void limitMaxZoomOut(int maxZoomOutLevel) /*-{
//
//        $wnd.G_PHYSICAL_MAP.getMinimumResolution = function () {
//            return maxZoomOutLevel
//        };
//        $wnd.G_NORMAL_MAP.getMinimumResolution = function () {
//            return maxZoomOutLevel
//        };
//        $wnd.G_SATELLITE_MAP.getMinimumResolution = function () {
//            return maxZoomOutLevel
//        };
//        $wnd.G_HYBRID_MAP.getMinimumResolution = function () {
//            return maxZoomOutLevel
//        };
//
//    }-*/;
//
//    public void setCountryEPolylineHashMap(HashMap<String, CountryOutline> countryEPolylineHashMap) {
//        this.countryEPolylineHashMap = countryEPolylineHashMap;
//    }
//
//    public MapWidget getMapWidget() {
//        return mapWidget;
//    }
//
//    //TODO add this back in, no equivalent yet found
//    public void clearOverlays() {
//        mapWidget.clearOverlays();
//    }
//
//    public void setMarkers(List<Marker> markers) {
//        mapMessagePopup.hide();
//        if (markers.size() > 0) {
//
//            GWT.log("Adding markers to map");
//            for (Marker marker : markers) {
//                mapWidget.addOverlay(marker);
//            }
//            GWT.log(" - markers added to map");
//
//        } else {
//
//            GWT.log("No markers available for selection");
//            indicateNoMarkersForSelectedFilters();
//
//        }
//    }
//
//    public void displayMessage(String message) {
//        mapMessagePopup.setWidget(new HTML(message));
//        mapMessagePopup.center();
//        mapMessagePopup.show();
//    }
//
//
//    private void indicateNoMarkersForSelectedFilters() {
//        displayMessage(noStudiesFoundMsg);
//    }
//
//    public void clearTheseMarkers(List<Marker> markers) {
//        for (Marker marker : markers) {
//            mapWidget.removeOverlay(marker);
//        }
//    }
//
//    /**
//     * @see org.wwarn.mapcore.client.components.customwidgets.GenericMapWidget#justResizeMapWidget()
//     */
//    public void justResizeMapWidget() {
//        mapWidget.setWidth("100%");
//    }
//
//    /**
//     * @see org.wwarn.mapcore.client.components.customwidgets.GenericMapWidget#resizeMapWidget()
//     */
//    public void resizeMapWidget() {
//
//        justResizeMapWidget();
//
//        absoluteMapContentOverlayPanel.setWidgetPosition(filtersDisplayWidgetPlaceHolder, calcFiltersPanelXPos(), FILTERS_PANEL_Y_POS);
////        absoluteMapContentOverlayPanel.setWidgetPosition(legendWidgetPlaceHolder, LEGEND_X_INDENT, calcLegendPanelYPos());
//    }
//
//    /**
//     * @see org.wwarn.mapcore.client.components.customwidgets.GenericMapWidget.setupDisplay()
//     * @param width
//     * @param height
//     */
//    public void configureMapDimension(Integer width, Integer height) {
//
//        absoluteMapContentOverlayPanel.setWidth("100%");
//
//        /*FIXME mapWidget shows as an error because css files aren't linked correctly.
//        * mapWidget css should be defined in Map.css. Currently it's defined in datamap.css*/
//        absoluteMapContentOverlayPanel.setStyleName("mapWidget");
//
//        //absolute panel needs an explicitly set height
//        absoluteMapContentOverlayPanel.setHeight(height.toString());
//
//        //mapwidget initially needs an explicit width to prevent an initial incomplete loading bug.
//        //width is set to 100% in resizeMapWidget
//        mapWidget.setSize(width.toString(), "100%");
//
//        mapWidget.addControl(new LargeMapControl3D());
//        mapWidget.addControl(new HierarchicalMapTypeControl());
//        mapWidget.addMapType(MapType.getPhysicalMap());
////        mapWidget.addMapType(MapType.getEarthMap());   // TODO: Fix problem with Google Map API key
////        mapWidget.addControl(new SmallZoomControl3D());
////        mapWigetMolecular.addControl(new OverviewMapControl());
//        mapWidget.addControl(new ScaleControl());
//        mapWidget.setScrollWheelZoomEnabled(false);
//        mapWidget.setCurrentMapType(MapType.getPhysicalMap());
//
//    }
//
//    public void drawCountryPolygonsFromCountryHighlightDetails(List<CountryHighlightDetails> highlightDetailsList) {
//        List<String> countryCodes = new ArrayList<String>();
//        for (CountryHighlightDetails countryHighlightDetails : highlightDetailsList) {
//            countryCodes.add(countryHighlightDetails.getCountryCode());
//        }
//
//        List<CountryHighlightDetails> countriesToChange = new ArrayList<CountryHighlightDetails>();
//        List<CountryHighlightDetails> countriesToAdd = new ArrayList<CountryHighlightDetails>();
//
//        HashMap<String, CountryHighlightDetails> newHighlightDetailsList = (HashMap<String, CountryHighlightDetails>) currentHighlightDetailsList.clone();
//
//        for (CountryHighlightDetails countryHighlightDetails : highlightDetailsList) {
//            if (countryEPolylineHashMap.containsKey(countryHighlightDetails.getCountryCode())) {
//
//                // Find out if the polygon with the same specification is already on the map.
//                if (currentHighlightDetailsList.containsKey(countryHighlightDetails.getCountryCode())) {
//
//                    if (!currentHighlightDetailsList.get(countryHighlightDetails.getCountryCode()).equals(countryHighlightDetails)) {
//                        countriesToChange.add(countryHighlightDetails);
//                        newHighlightDetailsList.remove(countryHighlightDetails.getCountryCode());
//                        newHighlightDetailsList.put(countryHighlightDetails.getCountryCode(), countryHighlightDetails);
//                    } else {
//                        // It's already on the map.
//                    }
//                } else {
//                    countriesToAdd.add(countryHighlightDetails);
//                    newHighlightDetailsList.put(countryHighlightDetails.getCountryCode(), countryHighlightDetails);
//                }
//            }
//        }
//
//        // Remove any polygon which isn't part of the new country list
//        for (String code : currentHighlightDetailsList.keySet()) {
//            if (!countryCodes.contains(code)) {
//                newHighlightDetailsList.remove(code);
//                removeCountryPolygon(code);
//            }
//        }
//
//        // Add any new polygons
//        for (CountryHighlightDetails countryHighlightDetails : countriesToAdd) {
//            addCountryPolygonWithOpacity(countryHighlightDetails);
//        }
//
//        // Update any polygons (country polygons which have different properties now)
//        for (CountryHighlightDetails countryHighlightDetails : countriesToChange) {
//            updateCountryPolygonWithOpacity(countryHighlightDetails);
//        }
//
//        currentHighlightDetailsList = newHighlightDetailsList;
//    }
//
//    private void updateCountryPolygonWithOpacity(CountryHighlightDetails countryHighlightDetails) {
//        Polygon polygon = generatePolygonForCountry(countryHighlightDetails);
//        mapWidget.removeOverlay(countryCodePolygonHashMap.get(countryHighlightDetails.getCountryCode()));
//        countryCodePolygonHashMap.remove(countryHighlightDetails.getCountryCode());
//        countryCodePolygonHashMap.put(countryHighlightDetails.getCountryCode(), polygon);
//        mapWidget.addOverlay(polygon);
//
//    }
//
//    private void removeCountryPolygon(String countryCode) {
//        mapWidget.removeOverlay(countryCodePolygonHashMap.get(countryCode));
//        countryCodePolygonHashMap.remove(countryCode);
//    }
//
//    private void addCountryPolygonWithOpacity(CountryHighlightDetails countryHighlightDetails) {
//
//        Polygon polygon = generatePolygonForCountry(countryHighlightDetails);
//        mapWidget.addOverlay(polygon);
//        countryCodePolygonHashMap.put(countryHighlightDetails.getCountryCode(), polygon);
//    }
//
//    private Polygon generatePolygonForCountry(CountryHighlightDetails countryHighlightDetails) {
//        CountryOutline countryOutline = countryEPolylineHashMap.get(countryHighlightDetails.getCountryCode());
//
//        EncodedPolyline[] encodedPolylines = new EncodedPolyline[countryOutline.getCountryPartOutlines().size()];
//
//        for (int i = 0; i < countryOutline.getCountryPartOutlines().size(); i++) {
//            CountryPartOutline partOutline = countryOutline.getCountryPartOutlines().get(i);
//
//            Integer lineThickness = 1;
//
//            if (countryHighlightDetails.getOpacity() > 0.25) {
//                lineThickness = 3;
//            }
//
//            EncodedPolyline encodedPolyline = EncodedPolyline.newInstance(
//                    partOutline.getEncodedPoints(),                 //points
//                    4,                                              //zoomFactor
//                    partOutline.getLevels(),                        //levels
//                    4,                                              //numLevels
//                    countryHighlightDetails.getColourStringRGB(),   //colour
//                    lineThickness,                                  //weight
//                    1.0);                                           //opacity
//            encodedPolylines[i] = encodedPolyline;
//        }
//
//        return Polygon.fromEncoded(encodedPolylines,
//                true,
//                countryHighlightDetails.getColourStringRGB(),
//                countryHighlightDetails.getOpacity(), true);
//    }
//
//    public AbsolutePanel createMapPanel() {
//        return absoluteMapContentOverlayPanel;
//    }
//
//    /**
//     * @see
//     * @param legendImage
//     * @param legendPixelsFromTop
//     */
//    public void setMapLegend(Widget legendImage, int legendPixelsFromTop) {
//        this.legendPixelsFromBottom = legendPixelsFromTop;
//        absoluteMapContentOverlayPanel.setWidgetPosition(legendWidgetPlaceHolder, LEGEND_X_INDENT, calcLegendPanelYPos());
//        legendWidgetPlaceHolder.setWidget(legendImage);
//    }
//
//    public SimplePanel getLegendWidgetPlaceHolder ()
//    {
//        return legendWidgetPlaceHolder;
//    }
//
//    private int calcLegendPanelYPos() {
//        return mapWidget.getOffsetHeight() - legendPixelsFromBottom;
//    }
//
//    public void setMapFiltersDisplay(Widget filtersDisplayWidget) {
//        filtersDisplayWidgetPlaceHolder.setWidget(filtersDisplayWidget);
//    }
//
//    private int calcFiltersPanelXPos() {
//        return mapWidget.getAbsoluteLeft() + mapWidget.getOffsetWidth() - 413;
//    }
//
//    /**
//     * Migrated to
//     * org.wwarn.mapcore.client.components.customwidgets.GenericMapWidget.Builder#setZoomLevel(int)
//     * @param i
//     */
//    public void setZoomLevel(int i) {
//        mapWidget.setZoomLevel(i);
//    }
//
//    public void setCenter(LatLng latLng) {
//        mapWidget.setCenter(latLng);
//    }
//
//    public Point convertLatLngToContainerPixel(LatLng latlng) {
//        return mapWidget.convertLatLngToContainerPixel(latlng);
//    }
//
//    public int getAbsoluteLeft() {
//        return mapWidget.getAbsoluteLeft();
//    }
//
//    public int getAbsoluteTop() {
//        return mapWidget.getAbsoluteTop();
//    }
//
//    public void indicateLoading() {
//        loadingPanelPopup.show();
//        loadingPanelPopup.center();
//        loadingPanelPopup.setPopupPosition(loadingPanelPopup.getAbsoluteLeft(), mapWidget.getAbsoluteTop());
//    }
//
//    public void removeLoadingIndicator() {
//        loadingPanelPopup.hide();
//    }
//}