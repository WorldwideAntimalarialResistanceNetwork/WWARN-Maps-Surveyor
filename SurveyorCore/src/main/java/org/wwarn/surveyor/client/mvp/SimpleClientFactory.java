package org.wwarn.surveyor.client.mvp;

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
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.wwarn.surveyor.client.core.*;
import org.wwarn.surveyor.client.model.*;
import org.wwarn.mapcore.client.utils.XMLUtils;
import org.wwarn.surveyor.client.util.AsyncCallbackWithTimeout;

import java.util.ArrayList;
import java.util.List;

/**
 * Client factory implementation
 * all get methods use lazy initialization
 * User: nigel
 * Date: 30/07/13
 * Time: 11:05
 */
public class SimpleClientFactory implements ClientFactory {
    private final ConfigLoader configLoader = GWT.create(ConfigLoader.class);
    private EventBus eventBus;
    private ApplicationContext applicationContext;
    private DataProvider dataProvider;
    private QueryResult lastQueryResult;
    private FilterQuery lastFilterQuery = new FilterQuery();

    //TODO replace this singleton with GIN and @Singleton
    private static SimpleClientFactory ourInstance = new SimpleClientFactory();

    public static SimpleClientFactory getInstance() {
        return ourInstance;
    }
    private SimpleClientFactory() {
    }

    public EventBus getEventBus() {
        if (eventBus == null) eventBus = new SimpleEventBus();
        return eventBus;
    }

    public ApplicationContext getApplicationContext() {
        if(applicationContext == null)
        {
            applicationContext = loadApplicationContext();
        }
        return applicationContext;
    }

    private ApplicationContext loadApplicationContext() {
        String xmlConfig = configLoader.getXMLConfig();
        try {
            applicationContext =  new XMLApplicationLoader(xmlConfig);
        } catch (XMLUtils.ParseException e) {
            throw new IllegalStateException(e);
        }
        return applicationContext;
    }

    /**
     * Lazy initialise Dataprovider
     * @return DataProvider
     */
    public DataProvider getDataProvider(){
        if(this.dataProvider == null){
//            EventLogger.logEvent("org.wwarn.surveyor.client.mvp.SimpleClientFactory", "getDataProvider", "begin");

//            EventLogger.logEvent("org.wwarn.surveyor.client.mvp.SimpleClientFactory", "getSchema", "begin");
            DataSchema schema = getSchema();
//            EventLogger.logEvent("org.wwarn.surveyor.client.mvp.SimpleClientFactory", "getSchema", "end");
//            EventLogger.logEvent("org.wwarn.surveyor.client.mvp.SimpleClientFactory", "getFacetFieldList", "begin");
            String[] facetFieldList = getFacetFieldList();
//            EventLogger.logEvent("org.wwarn.surveyor.client.mvp.SimpleClientFactory", "getFacetFieldList", "end");
//            EventLogger.logEvent("org.wwarn.surveyor.client.mvp.SimpleClientFactory", "DefaultLocalJSONDataProvider", "begin");
            final ApplicationContext applicationContext1 = getApplicationContext();
            final DatasourceConfig config = applicationContext1.getConfig(DatasourceConfig.class);
            GenericDataSource dataSource = new GenericDataSource(config.getFilename(), null, GenericDataSource.DataSourceType.ServletRelativeDataSource);

            switch (config.getDataSourceProvider()){
                case ClientSideSearchDataProvider:
                    dataSource.setDataSourceProvider(DataSourceProvider.ClientSideSearchDataProvider);
                    this.dataProvider = new ClientSideSearchDataProvider(dataSource, schema, facetFieldList);
                break;
                case ServerSideLuceneDataProvider:
                    dataSource.setDataSourceProvider(DataSourceProvider.ServerSideLuceneDataProvider);
                    this.dataProvider = new ServerSideSearchDataProvider(dataSource, schema, facetFieldList);
                    break;
                case GoogleAppEngineLuceneDataSource:
                    dataSource.setDataSourceProvider(DataSourceProvider.GoogleAppEngineLuceneDataSource);
                    this.dataProvider = new ServerSideSearchDataProvider(dataSource, schema, facetFieldList);
                    break;
                case LocalClientSideDataProvider:
                default:
                    dataSource.setDataSourceProvider(DataSourceProvider.LocalClientSideDataProvider);
                    this.dataProvider =  new DefaultLocalJSONDataProvider(dataSource, schema, facetFieldList);
                    break;
            }
//            EventLogger.logEvent("org.wwarn.surveyor.client.mvp.SimpleClientFactory", "DefaultLocalJSONDataProvider", "end");
//            EventLogger.logEvent("org.wwarn.surveyor.client.mvp.SimpleClientFactory", "getDataProvider", "end");

        }
        return this.dataProvider;

    }

    public String[] getFacetFieldList() {
        FilterConfig config = (FilterConfig) applicationContext.getConfig(FilterConfig.class);
        List<String> facetFields = new ArrayList<String>();
        int fieldCount = 0;
        for (int i = 0; i < config.getFilterCount(); i++) {
            final FilterSetting filterSetting = config.getFilters().get(i);
            // handle case for multiple filter fields
            if(filterSetting instanceof FilterConfig.FilterMultipleFields){
                final String[] filterColumns = ((FilterConfig.FilterMultipleFields) filterSetting).getFilterColumns();
                for (String filterColumn : filterColumns) {
                    facetFields.add(filterColumn);
                    fieldCount++;
                }
            }else{
                facetFields.add(filterSetting.filterFieldName);
                fieldCount++;
            }
        }
        return facetFields.toArray(new String[fieldCount]);
    }

    public DataSchema getSchema() {
        Config datasource = this.getApplicationContext().getConfig(DatasourceConfig.class);
        return new DataSchema((DatasourceConfig) datasource);
    }

    public QueryResult getLastQueryResult() {
        if(lastQueryResult == null){
            // run default search and store result as LastQueryResult
            try {
                this.getDataProvider().query(new MatchAllQuery(), new AsyncCallbackWithTimeout<QueryResult>() {
                    @Override
                    public void onTimeOutOrOtherFailure(Throwable caught) {
                        throw new IllegalStateException(caught);
                    }

                    @Override
                    public void onNonTimedOutSuccess(QueryResult result) {
                        lastQueryResult = result;
                    }
                });
            } catch (SearchException e) {
                throw new IllegalStateException(e);
            }
        }
        return lastQueryResult;
    }

    public void setLastQueryResult(QueryResult queryResult) {
        this.lastQueryResult = queryResult;
    }

    @Override
    public FilterQuery getLastFilterQuery() {
        return lastFilterQuery;
    }

    @Override
    public void setLastFilterQuery(FilterQuery filterQuery) {
        this.lastFilterQuery = filterQuery;
    }

    /**
     * This method configures dynamically any config in the xml.
     * Right now, only overrides the coordinates of the map but it could be use to override any configuration
     * @param viewConfig
     */
    @Override
    public void setConfigDynamically(Config viewConfig) {

        if(viewConfig instanceof MapViewConfig) {
            MapViewConfig newMapViewConfig = (MapViewConfig) viewConfig;
            updateMapViewConfig(newMapViewConfig);
        }
    }

    private void updateMapViewConfig(MapViewConfig newMapViewConfig){
        MapViewConfig currentMapViewConfig = applicationContext.getConfig(MapViewConfig.class);
        currentMapViewConfig.setInitialLat(newMapViewConfig.getInitialLat());
        currentMapViewConfig.setInitialLon(newMapViewConfig.getInitialLon());
        currentMapViewConfig.setInitialZoomLevel(newMapViewConfig.getInitialZoomLevel());
    }


}
