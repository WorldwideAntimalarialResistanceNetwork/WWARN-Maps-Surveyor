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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import org.wwarn.mapcore.client.components.customwidgets.GenericMapWidget;
import org.wwarn.mapcore.client.components.customwidgets.GenericMarker;
import org.wwarn.mapcore.client.components.customwidgets.LegendButton;
import org.wwarn.surveyor.client.core.QueryResult;
import org.wwarn.surveyor.client.core.RecordList;
import org.wwarn.surveyor.client.model.MapViewConfig;
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.surveyor.client.mvp.ClientFactory;
import org.wwarn.surveyor.client.event.MapLoadCompleteEvent;
import org.wwarn.surveyor.client.event.MarkerClickEvent;
import org.wwarn.surveyor.client.event.ResultChangedEvent;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;

import java.util.ArrayList;
import java.util.List;

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
    private GenericMarker.MarkerHoverLabelBuilder markerHoverLabelBuilder = GWT.create(GenericMarker.MarkerHoverLabelBuilder.class);
    private GenericMarker.MarkerClickInfoWindowBuilder markerClickInfoWindow = GWT.create(GenericMarker.MarkerClickInfoWindowBuilder.class);
    private GenericMarker.MarkerIconPathBuilder markerIconPathBuilder = GWT.create(GenericMarker.MarkerIconPathBuilder.class);
    private MarkerDisplayFilter markerDisplayFilter = GWT.create(MarkerDisplayFilter.class);
    private MarkerCoordinateSource markerCoordinateSource = GWT.create(MarkerCoordinateSource.class);
    private Scheduler scheduler = Scheduler.get();

    // UI Binder boiler plate
    interface MapViewUIUiBinder extends UiBinder<FlowPanel, MapViewComposite> {}
    private static MapViewUIUiBinder ourUiBinder = GWT.create(MapViewUIUiBinder.class);

    // Event Bus bindings
    interface ResultChangedEventBinder extends EventBinder<MapViewComposite> {};
    private ResultChangedEventBinder eventBinder = GWT.create(ResultChangedEventBinder.class);

    public MapViewComposite(final MapViewConfig viewConfig) {
        if(!(viewConfig != null)){
            throw new IllegalArgumentException("Expected MapView config");
        }
        this.viewConfig = viewConfig;
        panel = ourUiBinder.createAndBindUi(this);
        initWidget(panel);
        eventBinder.bindEventHandlers(this, clientFactory.getEventBus());
        setupDisplay();
    }

    private void onLoadComplete() {
        mapWidget.onLoadComplete(new Runnable() {
            @Override
            public void run() {
                clientFactory.getEventBus().fireEvent(new MapLoadCompleteEvent());
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
                GenericMapWidget.Builder builder = new GenericMapWidget.Builder();
                mapWidget = builder.configureMapDimension(400, 500).setCenter(viewConfig.getInitialLat(), viewConfig.getInitialLon()).setZoomLevel(viewConfig.getInitialZoomLevel()).createMapWidget();
                final String relativeImagePath = viewConfig.getMapImageRelativePath();
                if (!StringUtils.isEmpty(relativeImagePath)) {
                    Integer imageLegendPositionFromTopInPixels = viewConfig.getImageLegendPositionFromTopInPixels();
                    final int legendPixelsFromTop = (imageLegendPositionFromTopInPixels == null) ? 250 : imageLegendPositionFromTopInPixels;
                    LegendButton legendButton = new LegendButton(relativeImagePath);
                    mapWidget.setMapLegend(legendButton, legendPixelsFromTop);
                }
                setMarkers(clientFactory.getLastQueryResult());
                panel.add(mapWidget);
                mapWidget.justResizeMapWidget();
                mapWidget.resizeMapWidget();
                onLoadComplete();
            }
        });

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
        for (final RecordList.Record record : records) {
            GenericMarker.Builder markerBuilder = new GenericMarker.Builder();
            final MarkerCoordinateSource.LatitudeLongitude latitudeLongitude = markerCoordinateSource.process(record);
            double lat = latitudeLongitude.getLatitude();
            double lon = latitudeLongitude.getLongitude();

            if(markerDisplayFilter.filter(record)){
                GenericMarker<RecordList.Record> marker = markerBuilder.setMarkerLat(lat).setMarkerLon(lon).setMarkerIconPath(markerIconPathBuilder).createMarker(record, mapWidget);
                marker.setupMarkerHoverLabel(markerHoverLabelBuilder);
                marker.setupMarkerClickInfoWindow(markerClickInfoWindow);
                marker.addClickHandler(new GenericMarker.MarkerCallBackEventHandler<GenericMarker>() {
                    @Override
                    public void run(GenericMarker sourceElement) {
                        clientFactory.getEventBus().fireEvent(new MarkerClickEvent(record));
                    }
                });
                markers.add(marker);
            }
        }

        mapWidget.addMarkers(markers);
    }

    @EventHandler
    public void onResultChanged(ResultChangedEvent resultChangedEvent){
        setMarkers(resultChangedEvent.getQueryResult());
    }

    public interface MarkerDisplayFilter<T>{
        /**
         * returns if the current record should be filtered, if false the current record is filtered from display.
         * @param record
         * @return
         */
        public boolean filter(T record);
    }

    public static class DefaultMarkerDisplayFilter implements MarkerDisplayFilter<RecordList.Record>{
        @Override
        public boolean filter(RecordList.Record record) {
            double lat = getDefaultMarkerLatitude(record);
            double lon = Double.parseDouble(record.getValueByFieldName(viewConfig.getMarkerLongitudeField()));
            return (lat != 0.0 && lon != 0.0);
        }
    }

    public static class DefaultMarkerCoordinateSource implements MarkerCoordinateSource<RecordList.Record> {
        @Override
        public LatitudeLongitude process(RecordList.Record record) {
            double lat = getDefaultMarkerLatitude(record);
            double lon = Double.parseDouble(record.getValueByFieldName(viewConfig.getMarkerLongitudeField()));
            return new LatitudeLongitude(lat, lon);
        }
    }

    private static double getDefaultMarkerLatitude(RecordList.Record record) {
        return Double.parseDouble(record.getValueByFieldName(viewConfig.getMarkerLatitudeField()));
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
