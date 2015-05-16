package org.wwarn.surveyor.client.core;

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
import com.google.gwt.http.client.*;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import org.wwarn.mapcore.client.utils.EventLogger;
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.surveyor.client.mvp.ClientFactory;
import org.wwarn.surveyor.client.mvp.DataSource;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;
import org.wwarn.surveyor.client.resources.Resources;
import org.wwarn.surveyor.client.util.AsyncCallbackWithTimeout;

import java.util.Date;
import java.util.Set;


/**
 * JSON Data provider, this contains the logic to query a JSON data source, supports filter queries, and faceted
 * navigation. Multiple filters can be applied, including date range, single field : single value filter across
 * many fields. Multiple values selection on a single field is not yet supported.
 * The internal structure datatable gwt.visualization.client.DataTable and gwt.visualization.client.DataView,
 * User: nigel
 * Date: 19/07/13
 * Time: 15:40
 */
public class DefaultLocalJSONDataProvider implements DataProvider {

    private final DataTable dataTable = DataTable.create();
    private final DataSchema schema;
    private final DataSource dataSource;
    private JSONArray jsonArray;
    private String[] facetFieldList;
    private DataTableExtensions dataTableExtensions = new DataTableExtensions();
    private final DataTypeUtility dataTypeUtility = new DataTypeUtility();
    private boolean isStillLoading = true;
    // A keeper of the timer instance in case we need to cancel it
    private Timer timeoutTimer = null;

    // An indicator when the computation should quit
    private boolean abortFlag = false;

    static final int TIMEOUT = 30; // 30 second timeout

    public DefaultLocalJSONDataProvider(DataSource dataSource, DataSchema schema, final String[] facetFields) {
        if(dataSource == null || schema == null) {
            throw new IllegalArgumentException("datasource or schema missing");
        }
        switch (dataSource.getDataSourceType()) {
            case FusionTable:
            case GoogleSpreadSheet:
                throw new UnsupportedOperationException("Currently on servelet relative data sources is supported");
            case ServletRelativeDataSource:
                break;
            case JSONPropertyList:
                break;
        }
        this.schema = schema;
        this.dataSource = dataSource;
        this.facetFieldList = facetFields;
//        EventLogger.logEvent("DefaultLocalJSONDataProvider", "loadData", "begin");
//        loadData(jsonArray, schema);
//        EventLogger.logEvent("DefaultLocalJSONDataProvider", "loadData", "end");

//        EventLogger.logEvent("DefaultLocalJSONDataProvider", "validateFacetFields", "begin");
//        validateFacetFields(facetFields);
//        EventLogger.logEvent("DefaultLocalJSONDataProvider", "validateFacetFields", "end");
    }

    public void onLoad(final Runnable onLoadComplete){
        if(dataSource.getDataSourceType()== GenericDataSource.DataSourceType.ServletRelativeDataSource) {
            final AsyncDataSourceLoader asyncDataSourceLoader = new AsyncDataSourceLoader();
            asyncDataSourceLoader.preFetchData(dataSource.getLocation(), new Runnable() {
                @Override
                public void run() {
                    loadData(jsonArray, schema, onLoadComplete);
                }
            });
        }
        if(dataSource.getDataSourceType()== GenericDataSource.DataSourceType.JSONPropertyList){
            final JSONArray jsonArrayData = convertToJSONArray(dataSource.getResource());
            loadData(jsonArrayData, schema, onLoadComplete);
        }
    }

    /**
     * Check each facet field is present in schema.
     * @param facetFieldList an array of facet fields
     */
    private void validateFacetFields(String[] facetFieldList) {
        if(facetFieldList == null || facetFieldList.length == 0){
            return;
        }
        for (String facetfield : facetFieldList){
            if(schema.getColumnIndex(facetfield) < 0){
                throw new IllegalArgumentException("Facet field:"+facetfield+", not found in schema");
            }
        }

    }

    private void loadData(final JSONArray jsonArray, final DataSchema schema, final Runnable onLoadComplete) {
        // Check to make sure the timer isn't already running.
        if (timeoutTimer != null) {
            Window.alert("Command is already running!");
            return;
        }

        // Create a timer to abort if the loadData call if it takes too long
        timeoutTimer = new Timer() {
            public void run() {
                Window.alert("Timeout expired. Failed to load data.");
                timeoutTimer = null;
                abortFlag = true;
            }
        };
        // (re)Initialize the abort flag and start the timer.
        abortFlag = false;
        timeoutTimer.schedule(TIMEOUT * 1000); // timeout is in milliseconds

        // expects an array of objects
        // object should be key-value pairs
        // only grab keys that are present in the schema
        dataTypeUtility.setupDataTableColumns(schema, dataTable);

//        EventLogger.logEvent("DefaultLocalJSONDataProvider", "setupDataTableRowLength", "begin");
        setupDataTableRowLength(jsonArray.size());
//        EventLogger.logEvent("DefaultLocalJSONDataProvider", "setupDataTableRowLength", "end");

//        EventLogger.logEvent("DefaultLocalJSONDataProvider", "loadDataLoop", "begin");
        // for each row in array
        final int size = jsonArray.size();
        final int[] rowIndex = {0};
        final Scheduler scheduler = Scheduler.get();
        scheduler.scheduleIncremental(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {


//        for (int rowIndex = 0; rowIndex < size; rowIndex++) {
//            EventLogger.logEvent("DefaultLocalJSONDataProvider", "forRowIdxHeader"+rowIndex, "begin");

                JSONValue jsonValue = jsonArray.get(rowIndex[0]);
                JSONObject jsonObject = jsonValue.isObject();

                // for each field in row, map each field to internal structure
                Set<String> keys = jsonObject.keySet();

//            EventLogger.logEvent("DefaultLocalJSONDataProvider", "forRowIdxHeader"+rowIndex[0], "end");

//            EventLogger.logEvent("DefaultLocalJSONDataProvider", "forRowIdxBody"+rowIndex[0], "begin");

                // setup rows
                for (String key : keys) {
//                EventLogger.logEvent("DefaultLocalJSONDataProvider", "rowLoopBody"+key, "begin");
//
//                EventLogger.logEvent("DefaultLocalJSONDataProvider", "jsonObject.get"+key, "begin");
                    JSONValue value = jsonObject.get(key);
//                EventLogger.logEvent("DefaultLocalJSONDataProvider", "jsonObject.get"+key, "end");
//
//                EventLogger.logEvent("DefaultLocalJSONDataProvider", "schema.hasColumn"+key, "begin");
                    // track column index
                    int columnIndex = schema.getColumnIndex(key);

                    // if schema contains column
                    if (columnIndex >= 0) {
                        // map type to data table type
                        final DataType dataType = schema.getType(key);
                        switch (dataType) {
                            case Date:
                            case DateYear:
                                Date parsedDateYear = DataType.ParseUtil.tryParseDate(value.toString(), "01/01/1970");
                                dataTable.setValue(rowIndex[0], columnIndex, parsedDateYear);
                                break;
                            case Integer:
                                double parsedDouble = parseDouble(value);
                                dataTable.setValue(rowIndex[0], columnIndex, parsedDouble);
                                break;
                            case CoordinateLat:
                            case CoordinateLon:
                                double coords = parseDouble(value);
                                dataTable.setValue(rowIndex[0], columnIndex, StringUtils.ifEmpty(Double.toString(coords), ""));
                                break;
                            default:
                                String stringValue = (value == null || value.isString() == null) ? "" : value.isString().stringValue();
                                dataTable.setValue(rowIndex[0], columnIndex, StringUtils.ifEmpty(stringValue, ""));
                                break;
                        }
                    }
//                EventLogger.logEvent("DefaultLocalJSONDataProvider", "schema.hasColumn"+key, "end");
//                EventLogger.logEvent("DefaultLocalJSONDataProvider", "rowLoopBody"+key, "end");
//                }
//            EventLogger.logEvent("DefaultLocalJSONDataProvider", "forRowIdxBody"+rowIndex[0], "end");

        }
                ++rowIndex[0];
                final boolean status = rowIndex[0] < size;
                if(abortFlag){
                    cancelTimer();
                    return false;
                }
                if(!status){
                    isStillLoading = false;
                    cancelTimer();
                    onLoadComplete.run();
                }
                return status;
            }
        });

//        EventLogger.logEvent("DefaultLocalJSONDataProvider", "loadDataLoop", "end");
    }

    // Stop the timeout timer if it is running
    private void cancelTimer() {
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
            timeoutTimer = null;
        }
    }

    private int setupDataTableRowLength(int size) {
        return dataTable.addRows(size);
    }

    private double parseDouble(JSONValue value) {
        return (value == null || value.isNumber() == null)? Integer.MIN_VALUE:value.isNumber().doubleValue();
    }


    @Override
    public void query(FilterQuery filterQuery, String[] facetFields, AsyncCallbackWithTimeout<QueryResult> queryResultCallBack) {
//        EventLogger.logEvent("org.wwarn.surveyor.client.core.DefaultLocalJSONDataProvider", "query(org.wwarn.surveyor.client.core.FilterQuery, java.lang.String[])", "begin");

        validateFacetFields(facetFields);
        // apply filter query to data dataTable
        DataTableExtensions.FilteredResults filteredResults = dataTableExtensions.filter(dataTable, filterQuery, schema);

        final AbstractDataTable table = (filterQuery.fetchAllDistinctFieldValues)?dataTable:filteredResults.getDataTable();
        FacetList facetList = calculateFacetList(table, facetFields);

//        EventLogger.logEvent("org.wwarn.surveyor.client.core.DefaultLocalJSONDataProvider", "query(org.wwarn.surveyor.client.core.FilterQuery, java.lang.String[])", "end");
        final QueryResult queryResult = new QueryResult(filteredResults.getRecordList(), facetList);
        queryResultCallBack.onNonTimedOutSuccess(queryResult);
    }


    @Override
    public void query(FilterQuery filterQuery, AsyncCallbackWithTimeout<QueryResult> queryResultCallBack) {
        query(filterQuery, this.facetFieldList, queryResultCallBack);
    }

    /**
     * facet field -> list of facet field values
     * @param table gwt datatable as internal table data source
     * @param facetFields list of field to calculate facet values for
     */
    private FacetList calculateFacetList(AbstractDataTable table, String[] facetFields) {
        if(facetFields == null || facetFields.length == 0){
             throw new NullPointerException("Facet field list empty");
        }
        FacetList facetList = new FacetList();
        for (String facetField : facetFields) {
            Set<String> distinctColumnValues = dataTableExtensions.getDistinctColumnValues(table, schema.getColumnIndex(facetField));
            facetList.addFacetField(facetField, distinctColumnValues);
        }
        return facetList;
    }

    public JSONArray convertToJSONArray(String jsonData) {

//        EventLogger.logEvent("AsyncDataSourceLoader", "parseJSON", "begin");
        JSONValue jsonValue = parseJSONFromString(jsonData);
//        EventLogger.logEvent("AsyncDataSourceLoader", "parseJSON", "end");

        return jsonValue.isArray();
    }



    private JSONValue parseJSONFromString(String jsonString) {
        return JSONParser.parseStrict(jsonString);
    }

    /**
    * Created by nigelthomas on 13/06/2014.
    */
    public static class DataTypeUtility {

        /**
         * Method with side effects, takes an instance of dataTable and schema and setups DataTable with columns
         * based on schema type
         * @param schema data schema
         * @param dataTable this objects state is changed during the course of this function
         */
        public void setupDataTableColumns(DataSchema schema, DataTable dataTable) {
            // map each datatype to gwt visualisation column type
    //        EventLogger.logEvent("DataTypeUtility", "setupDataTableColumns", "begin");

            for( String column : schema.getColumns()){
                DataType dataType = schema.getType(column);
                switch (dataType){
                    case Integer:
                        dataTable.addColumn(AbstractDataTable.ColumnType.NUMBER, column);
                        break;
                    case Date:
                    case DateYear:
                        dataTable.addColumn(AbstractDataTable.ColumnType.DATE, column);
                        break;
                    case CoordinateLat:
                    case CoordinateLon:
                    default:
                        dataTable.addColumn(AbstractDataTable.ColumnType.STRING, column);
                        break;
                }
            }
    //        EventLogger.logEvent("DataTypeUtility", "setupDataTableColumns", "end");

        }
    }

    /**
     * Created by nigelthomas on 05/03/2014.
     */
    private class AsyncDataSourceLoader {
        // TODO repeated all over the place!!
        private ClientFactory clientFactory = SimpleClientFactory.getInstance();

        final Resources res = Resources.IMPL;
        final String jsonString = "[]";
        private AsyncDataSourceLoader() {
        }

        private void preFetchData(String dataToFetch, final Runnable onComplete) {

            RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, GWT.getModuleBaseURL().concat(dataToFetch));
            requestBuilder.setCallback(new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if(200 == response.getStatusCode()){
                        String data = response.getText();
                        jsonArray = convertToJSONArray(data);
                        onComplete.run();
                    }else{
                        final String message = "Failed to retrieve external resources ; ERROR CODE:" + response.getStatusCode();
                        GWT.log(message);
                        throw new IllegalStateException(message);
                    }
                }

                @Override
                public void onError(Request request, Throwable throwable) {
                    final String message = "Failed to retrieve external resources; ERROR CODE:" + throwable.getMessage();
                    GWT.log(message, throwable);
                    throw new IllegalStateException(message, throwable);
            }
            });
            try{
                requestBuilder.send();
            }catch(RequestException e){
                GWT.log("Failed to retrieve external resources ", e);
            }
        }

    }

}
