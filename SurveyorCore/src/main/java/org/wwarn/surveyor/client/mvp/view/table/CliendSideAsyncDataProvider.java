package org.wwarn.surveyor.client.mvp.view.table;

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


import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.surveyor.client.core.*;
import org.wwarn.surveyor.client.event.InterfaceLoadCompleteEvent;
import org.wwarn.surveyor.client.event.ResultChangedEvent;
import org.wwarn.surveyor.client.model.TableViewConfig;
import org.wwarn.surveyor.client.mvp.ClientFactory;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;
import org.wwarn.surveyor.client.util.AsyncCallbackWithTimeout;

import java.util.*;


public class CliendSideAsyncDataProvider extends AsyncDataProvider<RecordList.Record> {


    // result change handler
    interface CliendSideAsyncDataProviderEventBinder extends EventBinder<CliendSideAsyncDataProvider> {};


    Range range;

    ClientFactory clientFactory = SimpleClientFactory.getInstance();
    private DataSchema dataSchema = clientFactory.getSchema();

    TableViewConfig tableViewConfig;

    /**
     * Create a new AllDataAsyncDataProvider instance and set up the
     * RPC framework that it will use.
     */
    public CliendSideAsyncDataProvider(TableViewConfig tableViewConfig) {
        this.tableViewConfig = tableViewConfig;
        CliendSideAsyncDataProviderEventBinder eventBinder = GWT.create(CliendSideAsyncDataProviderEventBinder.class);
        eventBinder.bindEventHandlers(this, clientFactory.getEventBus());
    }

    @EventHandler
    public void onResultChanged(ResultChangedEvent resultChangedEvent){
        updateResults(0, tableViewConfig.getPageSize());
    }

    /**
     * {@link #onRangeChanged(HasData)} is called when the table requests a new
     * range of data. You can push data back to the displays using
     * {@link #updateRowData(int, List)}.
     */
    @Override
    protected void onRangeChanged(HasData<RecordList.Record> display) {
        // Get the new range required.
        range = display.getVisibleRange();
        updateResults(range.getStart(), range.getLength());
    }


    private void queryTable(ClientSideSearchDataProvider clientSideSearchDataProvider, final FilterQuery filterQuery, String[] facetFields, final int start, final int length, final TableViewConfig tableViewConfig, final AsyncCallback<List<RecordList.Record>> asyncCallback) throws SearchException {
        try{
            clientSideSearchDataProvider.query(filterQuery, facetFields, new AsyncCallbackWithTimeout<QueryResult>() {
                @Override
                public void onTimeOutOrOtherFailure(Throwable caught) {
                    asyncCallback.onFailure(caught);
                }

                @Override
                public void onNonTimedOutSuccess(QueryResult queryResult) {
                    RecordList recordList = queryResult.getRecordList();
                    List<RecordList.Record>  searchedRecords = recordList.getRecords();
                    List<RecordList.Record> orderRecords = orderRecords(searchedRecords, tableViewConfig);
                    List<RecordList.Record> uniqueRecords = subsetUniqueRecords(filterQuery, orderRecords);
                    final List<RecordList.Record> pageRecords = getPageRecords(uniqueRecords, start, length);
                    asyncCallback.onSuccess(pageRecords);
                }
            });
        }catch(Exception e){
            final String message = "Unable to query the table";
            Log.error(message, e);
            throw new SearchException(message,e);
        }

    }
    List<RecordList.Record> orderRecords(List<RecordList.Record> records, final TableViewConfig tableViewConfig){

        if(tableViewConfig == null || tableViewConfig.getSortColumn().isEmpty()){
            return records;
        }

        List<RecordList.Record> orderList = new ArrayList<>(records.size());
        orderList.addAll(records);

        Collections.sort(orderList,new Comparator<RecordList.Record>() {
            @Override
            public int compare(RecordList.Record o1, RecordList.Record o2) {
                if (o1 == o2) {
                    return 0;
                }

                String sortColumn = tableViewConfig.getSortColumn();
                int diff = -1;
                if (o1 != null) {
                    diff = (o2 != null) ? o1.getValueByFieldName(sortColumn).compareTo(o2.getValueByFieldName(sortColumn)) : 1;
                }

                return tableViewConfig.isDescendentOrder() ? -diff : diff;
            }
        });
        return orderList;
    }
    private List<RecordList.Record>  getPageRecords(List<RecordList.Record> uniqueRecords, int start, int length){
        if(start > uniqueRecords.size()){
            return Collections.emptyList();
        }

        List<RecordList.Record> pageRecords = new ArrayList<>(length);
        for(int i = start; i < start + length; i++){
            if(uniqueRecords.size() <= i){
                break;
            }
            pageRecords.add(uniqueRecords.get(i));
        }
        return pageRecords;
    }

    private List<RecordList.Record> subsetUniqueRecords(FilterQuery filterQuery, List<RecordList.Record> searchedRecords) throws IllegalArgumentException {

        if(filterQuery.getFields() == null){
            throw new IllegalArgumentException("Table fields cannot be null. Please see them into the FilterQuery");
        }

        List<RecordList.Record> pageRecords = new ArrayList<>();
        int schemaSize = dataSchema.size();

        for (RecordList.Record searchedRecord : searchedRecords){
            String[] fields = new String[schemaSize];
            for(String field : filterQuery.getFields()){
                String columnValue = searchedRecord.getValueByFieldName(field);
                int columnIndex = dataSchema.getColumnIndex(field);
                fields[columnIndex] = columnValue;
            }

            RecordList.Record record = new RecordList.Record(fields, dataSchema);
            if(!pageRecords.contains(record)){
//                if(uniqueRecordsCount >= start){
                pageRecords.add(record);
//                }
//                uniqueRecordsCount++;
            }

//            if(pageRecords.size() > (length+start)){
//                break;
//            }
        }

        return pageRecords;
    }
    private void updateResults(final int rangeStart, final int rangeLength){
        FilterQuery filterQuery = clientFactory.getLastFilterQuery();
        filterQuery.setFields(getFields());
        try {
            ClientSideSearchDataProvider clientSideSearchDataProvider = (ClientSideSearchDataProvider)clientFactory.getDataProvider();
            this.queryTable(clientSideSearchDataProvider, filterQuery, clientSideSearchDataProvider.facetFieldList, rangeStart, rangeLength, tableViewConfig, new AsyncCallback<List<RecordList.Record>>() {
                @Override
                public void onFailure(Throwable throwable) {
                    throw new IllegalStateException(throwable);
                }

                @Override
                public void onSuccess(List<RecordList.Record> records) {
                    updateRowCount(records.size(), false);
                    updateRowData(rangeStart, records);
                }

            });
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    private Set<String> getFields(){
        Set<String> fields = new HashSet<String>();
        DataSchema dataSchema = clientFactory.getSchema();

        for(TableViewConfig.TableColumn column : tableViewConfig.getColumns()){

            if(!StringUtils.isEmpty(column.getHyperLinkField())){
                fields.add(column.getHyperLinkField());
            }

            if(isFunction(column.getFieldName())){
                for(String param : getParameters(column.getFieldName())){
                    int columnIndex = dataSchema.getColumnIndex(param);
                    if (columnIndex > -1){
                        fields.add(param);
                    }
                }
            }else{
                fields.add(column.getFieldName());
            }

        }

        return fields;
    }

    public static boolean isFunction(String fieldName){
        return fieldName.startsWith("func");
    }

    //Get parameters from functions
    //functions are usually as func(CONCAT_DATE(sf,sTo)) or func(ARITH(pre,tes,%))
    public static String[] getParameters(String function){
        function = function.substring(function.indexOf("(",5)+1, function.indexOf(")",1));
        return function.split(",");
    }
}
