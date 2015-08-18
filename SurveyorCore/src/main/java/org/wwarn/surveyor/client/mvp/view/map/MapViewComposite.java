package org.wwarn.surveyor.client.mvp.view.map;

/*
 * #%L
 * SurveyorCore
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.utility.markerclustererplus.client.ClusterIconStyle;
import com.google.gwt.maps.utility.markerclustererplus.client.MarkerClusterer;
import com.google.gwt.maps.utility.markerclustererplus.client.MarkerClustererOptions;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import org.wwarn.mapcore.client.components.customwidgets.map.*;
import org.wwarn.mapcore.client.components.customwidgets.LegendButton;
import org.wwarn.surveyor.client.core.DataSchema;
import org.wwarn.surveyor.client.core.QueryResult;
import org.wwarn.surveyor.client.core.RecordList;
import org.wwarn.surveyor.client.event.InterfaceLoadCompleteEvent;
import org.wwarn.surveyor.client.event.ToggleLayerEvent;
import org.wwarn.surveyor.client.model.MapViewConfig;
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.surveyor.client.mvp.ClientFactory;
import org.wwarn.surveyor.client.event.MarkerClickEvent;
import org.wwarn.surveyor.client.event.ResultChangedEvent;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * MapViewComposite combines view and presenter logic
 *  - load map markers from datasource
 *  - listen for result update event and reload map markers
 * User: nigelthomas
 * Date: 10/09/2013
 * Time: 15:12
 */
public class MapViewComposite extends Composite {

    private final FlowPanel panel;
    private static MapViewConfig viewConfig = null;
    private GenericMapWidget mapWidget;
    private ClientFactory clientFactory = SimpleClientFactory.getInstance();
    private GoogleV3Marker.MarkerIconPathBuilder markerIconPathBuilder;
    private MarkerDisplayFilter markerDisplayFilter;
    private MarkerCoordinateSource markerCoordinateSource;
    private Scheduler scheduler = Scheduler.get();
    private GoogleV3Marker.MarkerHoverLabelBuilder markerHoverLabelBuilder;
    private GoogleV3Marker.MarkerClickInfoWindowBuilder markerClickInfoWindow;
    private MarkerLegendLoader markerLegendLoader;
    private MarkerClusterer markerClusterer;

    // UI Binder boiler plate
    interface MapViewUIUiBinder extends UiBinder<FlowPanel, MapViewComposite> {}
    private static MapViewUIUiBinder ourUiBinder = GWT.create(MapViewUIUiBinder.class);

    // Event Bus bindings
    interface SomeEventBinder extends EventBinder<MapViewComposite> {};

    public MapViewComposite(final MapViewConfig viewConfig) {
        if(viewConfig == null){
            throw new IllegalArgumentException("Expected MapView config");
        }
        this.viewConfig = viewConfig;
        panel = ourUiBinder.createAndBindUi(this);
        initWidget(panel);
        SomeEventBinder eventBinder = GWT.create(SomeEventBinder.class);
        eventBinder.bindEventHandlers(this, clientFactory.getEventBus());
        setupDisplay();
    }

    private void onLoadComplete() {
        mapWidget.onLoadComplete(new Runnable() {
            @Override
            public void run() {
                clientFactory.getEventBus().fireEvent(new InterfaceLoadCompleteEvent());
            }
        });
    }

    private void setupDisplay(){
        // ensure that display is called just once
        if(mapWidget!=null){
            return;
        }

        scheduler.scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                MapBuilder builder = new MapBuilder();
                //todo move type of map into config
                final MapBuilder mapBuilder = builder.configureMapDimension(400, 500).setCenter(viewConfig.getInitialLat(), viewConfig.getInitialLon()).setZoomLevel(viewConfig.getInitialZoomLevel()).setMapTypeId(viewConfig.getMapType());
                mapWidget = mapBuilder.createMapWidget(viewConfig.getMapImplementation());
                panel.add(mapWidget);
                mapWidget.justResizeMapWidget();
                mapWidget.resizeMapWidget();
                loadMapLegend();
                onLoadComplete();
                setMarkers(clientFactory.getLastQueryResult());
            }
        });

    }

    private void loadMapLegend() {
        GenericMapWidget.LegendOptions legendOptions = getMarkerLegendBuilder().createLegend();
        if (legendOptions!=null) {
            mapWidget.setMapLegend(legendOptions);
        }
    }

    /**
     * Contain logic for getting records, building marker from results
     * Called on start and when filters are changed
     * @param queryResult records from query
     */
    public void setMarkers(QueryResult queryResult){
        mapWidget.clearMarkers();
        RecordList recordList = queryResult.getRecordList();
        List<RecordList.Record> records = recordList.getRecords();
        List<GenericMarker> markers = new ArrayList<GenericMarker>();
        final MarkerCoordinateSource markerCoordinateSource1 = getMarkerCoordinateSource();
        final MarkerDisplayFilter markerFilter = getMarkerDisplayFilter();
        markerFilter.init();
        for (final RecordList.Record record : records) {
            MapMarkerBuilder markerBuilder = new MapMarkerBuilder();
            final MarkerCoordinateSource.LatitudeLongitude latitudeLongitude = markerCoordinateSource1.process(record);
            double lat = latitudeLongitude.getLatitude();
            double lon = latitudeLongitude.getLongitude();

            if(markerFilter.filter(record)){
                GenericMarker<RecordList.Record> marker = markerBuilder.setMarkerLat(lat).setMarkerLon(lon).setMarkerIconPathBuilder(getMarkerIconPathBuilder()).createMarker(record, mapWidget);
                marker.setupMarkerHoverLabel(getMarkerHoverLabelBuilder());
                marker.setupMarkerClickInfoWindow(getMarkerClickInfoWindow());
                marker.addClickHandler(new GenericMarker.MarkerCallBackEventHandler<GenericMarker>() {
                    @Override
                    public void run(GenericMarker sourceElement) {
                    clientFactory.getEventBus().fireEvent(new MarkerClickEvent(record));
                    }
                });
                markers.add(marker);
            }
        }


        if(viewConfig.isDoCluster()){
            buildMarkerClusterer(markers);
        }else{
            mapWidget.addMarkers(markers);
        }

    }



    private void buildMarkerClusterer(List<GenericMarker> markers){
        if(markerClusterer != null)
            markerClusterer.clearMarkers();

        List<Marker> googleMarkers = convertToGoogleMarker(markers);
        final MarkerClustererOptions clusterOptions = MarkerClustererOptions.newInstance();
        clusterOptions.setAverageCenter( true );
        //clusterOptions.setGridSize(5);
        clusterOptions.setMaxZoom(10);
        markerClusterer = MarkerClusterer.newInstance(mapWidget.getInternalGoogleMapWidget(), googleMarkers, clusterOptions);
    }

    private List<Marker> convertToGoogleMarker(List<GenericMarker> markers){
        List<Marker> googleMarkers = new ArrayList<>();
        for(GenericMarker marker : markers){
            if(marker instanceof GoogleV3Marker){
                googleMarkers.add(((GoogleV3Marker) marker).getMarker());
            }
        }
        return googleMarkers;
    }

    private MarkerCoordinateSource getMarkerCoordinateSource() {
        if(markerCoordinateSource!=null) return markerCoordinateSource;
        try {
            markerCoordinateSource = GWT.create(MarkerCoordinateSource.class);
        }catch (RuntimeException e){
            if(!e.getMessage().startsWith("Deferred binding")) throw e;
            //by pass deferred binding error and use default value
            markerCoordinateSource = new DefaultMarkerCoordinateSource();
        }

        return markerCoordinateSource;
    }

    private MarkerDisplayFilter getMarkerDisplayFilter() {
        if(markerDisplayFilter != null ){return markerDisplayFilter;}
        try{
            markerDisplayFilter = GWT.create(MarkerDisplayFilter.class);
        }catch (RuntimeException e){
            if(!e.getMessage().startsWith("Deferred binding")) throw e;
            //by pass deferred binding error and use default value
            markerDisplayFilter = new DefaultMarkerDisplayFilter();
        }
        return markerDisplayFilter;
    }

    private GoogleV3Marker.MarkerIconPathBuilder getMarkerIconPathBuilder() {
        if(markerIconPathBuilder !=null){ return markerIconPathBuilder; }
        try{
            markerIconPathBuilder = GWT.create(GenericMarker.MarkerIconPathBuilder.class);
        }catch (RuntimeException e){
            if(!e.getMessage().startsWith("Deferred binding")) throw e;
            //by pass deferred binding error and use default value
            markerIconPathBuilder = new GenericMarker.DefaultMarkerIconPathBuilder();
        }
        return markerIconPathBuilder;

    }

    private GoogleV3Marker.MarkerClickInfoWindowBuilder getMarkerClickInfoWindow() {
        if(markerClickInfoWindow !=null){ return markerClickInfoWindow; }
        try{
            markerClickInfoWindow = GWT.create(GenericMarker.MarkerClickInfoWindowBuilder.class);
        }catch (RuntimeException e){
            if(!e.getMessage().startsWith("Deferred binding")) throw e;
            //by pass deferred binding error and use default value
            markerClickInfoWindow = new DefaultMarkerClickInfoWindowBuilder();
        }
        return markerClickInfoWindow;
    }

    private GoogleV3Marker.MarkerHoverLabelBuilder getMarkerHoverLabelBuilder() {
        if(markerHoverLabelBuilder!=null){ return markerHoverLabelBuilder; }
        try {
            markerHoverLabelBuilder = GWT.create(GoogleV3Marker.MarkerHoverLabelBuilder.class);
        }catch (RuntimeException e){
            if(!e.getMessage().startsWith("Deferred binding")) throw e;
            //by pass deferred binding error and use default value
            markerHoverLabelBuilder = new DefaultMarkerHoverLabelBuilder();
        }
        return markerHoverLabelBuilder;
    }

    public static class DefaultMarkerHoverLabelBuilder implements GoogleV3Marker.MarkerHoverLabelBuilder<RecordList.Record>{
        SimpleClientFactory simpleClientFactory = SimpleClientFactory.getInstance();
        DataSchema schema = simpleClientFactory.getSchema();
        @Override
        public Widget build(RecordList.Record markerContext) {
            final Set<String> fieldNames = schema.getColumns();
            SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
            safeHtmlBuilder.appendHtmlConstant("<span>");
            int indexOfFields = 0;
            for (String fieldName : fieldNames) {
                if(++indexOfFields > 4){ break;} /* output first three columns only */
                safeHtmlBuilder.appendEscapedLines(fieldName+" : "+markerContext.getValueByFieldName(fieldName));
                safeHtmlBuilder.appendHtmlConstant("<br/>");
            }
            safeHtmlBuilder.appendHtmlConstant("</span>");

            return new HTMLPanel(safeHtmlBuilder.toSafeHtml());
        }
    }

    @EventHandler
    public void onResultChanged(ResultChangedEvent resultChangedEvent){
        setMarkers(resultChangedEvent.getQueryResult());
    }

    @EventHandler
    public void onToggleLayer(ToggleLayerEvent toggleLayerEvent){
        if(mapWidget instanceof OfflineMapWidget){
            final OfflineMapWidget offlineMapWidget = (OfflineMapWidget) ((OfflineMapWidget) mapWidget);
            offlineMapWidget.toggleLayer(toggleLayerEvent.getLayerName());
        }
    }

    private MarkerLegendLoader getMarkerLegendBuilder() {
        if(markerLegendLoader!=null){ return markerLegendLoader; }
        try {
            markerLegendLoader = GWT.create(MarkerLegendLoader.class);
        }catch (RuntimeException e){
            if(!e.getMessage().startsWith("Deferred binding")) throw e;
            //by pass deferred binding error and use default value
            markerLegendLoader = new DefaultLegendBuilder();
        }
        return markerLegendLoader;
    }


    /**
     *
     */
    public interface MarkerLegendLoader{
        public GenericMapWidget.LegendOptions createLegend();
    }

    public static class DefaultLegendBuilder implements MarkerLegendLoader{

        @Override
        public GenericMapWidget.LegendOptions createLegend() {
            final String relativeImagePath = viewConfig.getMapImageRelativePath();
            GenericMapWidget.LegendOptions legendOptions = null;
            if (!StringUtils.isEmpty(relativeImagePath)) {
                Integer imageLegendPositionFromTopInPixels = viewConfig.getImageLegendPositionFromTopInPixels();
                final int legendPixelsFromTop = (imageLegendPositionFromTopInPixels == null) ? 250 : imageLegendPositionFromTopInPixels;
                final String imageLegendPosition = StringUtils.ifEmpty(viewConfig.getImageLegendPosition(), "BOTTOM_LEFT");
                GenericMapWidget.LegendPosition position = GenericMapWidget.LegendPosition.valueOf(imageLegendPosition);
                final LegendButton legendWidget = new LegendButton(relativeImagePath);
                legendOptions = GenericMapWidget.LegendOptions.createLegendOptions(legendWidget, legendPixelsFromTop, position, true);
            }
            return  legendOptions;
        }
    }


    /**
     * Logic to decide if a marker should be displayed is encapsulated here,
     * Often the decision on which record should be filtered depends on previously filtered items
     * @param <T>
     */
    public interface MarkerDisplayFilter<T>{
        /**
         * Called once before the first filter call is made,
         * this allows impl to reset any state, and get a fresh instance
         */
        public void init();

        /**
         * returns if the current record should be filtered, if false the current record is filtered from display.
         * @param record
         * @return
         */
        public boolean filter(T record);
    }

    public static class DefaultMarkerDisplayFilter implements MarkerDisplayFilter<RecordList.Record>{
        @Override
        public void init() {

        }

        @Override
        public boolean filter(RecordList.Record record) {
            return true;
        }
    }

    public static class DefaultMarkerCoordinateSource implements MarkerCoordinateSource<RecordList.Record> {
        @Override
        public LatitudeLongitude process(RecordList.Record record) {
            double lat = getDefaultMarkerLatitude(record);
            final String markerLongitudeField = viewConfig.getMarkerLongitudeField();
            double lon = Double.parseDouble(getCoordinateWithDefault(record,markerLongitudeField));
            return new LatitudeLongitude(lat, lon);
        }
    }

    private static double getDefaultMarkerLatitude(RecordList.Record record) {
        final String markerLatitudeField = viewConfig.getMarkerLatitudeField();
        return Double.parseDouble(getCoordinateWithDefault(record, markerLatitudeField));
    }

    private static String getCoordinateWithDefault(RecordList.Record record, String markerLatitudeField) {
        return StringUtils.ifEmpty(record.getValueByFieldName(markerLatitudeField), "0");
    }

    public interface MarkerCoordinateSource<T>{
        public LatitudeLongitude process(T record);

        class LatitudeLongitude {
            private final double latitude, longitude;

            public LatitudeLongitude(double latitude, double longitude) {
                this.latitude = latitude;
                this.longitude = longitude;
            }

            public double getLatitude() {
                return latitude;
            }

            public double getLongitude() {
                return longitude;
            }
        }
    }

}
