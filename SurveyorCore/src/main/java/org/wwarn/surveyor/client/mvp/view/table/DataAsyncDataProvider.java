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


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.surveyor.client.core.*;
import org.wwarn.surveyor.client.event.ResultChangedEvent;
import org.wwarn.surveyor.client.model.TableViewConfig;
import org.wwarn.surveyor.client.mvp.ClientFactory;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;

import java.util.*;


public class DataAsyncDataProvider extends AsyncDataProvider<RecordList.Record> {

    // result change handler
    interface DataAsyncDataProviderEventBinder extends EventBinder<DataAsyncDataProvider> {};

    private SearchServiceAsync rpcService;

    Range range;

    Range previousRange;

    ClientFactory clientFactory = SimpleClientFactory.getInstance();

    TableViewConfig tableViewConfig;

    /**
     * Create a new AllDataAsyncDataProvider instance and set up the
     * RPC framework that it will use.
     */
    public DataAsyncDataProvider(TableViewConfig tableViewConfig) {
        this.tableViewConfig = tableViewConfig;
        rpcService = GWT.create(SearchService.class);
        DataAsyncDataProviderEventBinder eventBinder = GWT.create(DataAsyncDataProviderEventBinder.class);
        eventBinder.bindEventHandlers(this, clientFactory.getEventBus());
    }

    @EventHandler
    public void onResultChanged(ResultChangedEvent resultChangedEvent){
//        if (range == null){

            updateResults(0, tableViewConfig.getPageSize());

//        }else{
//            updateResults(range.getStart(), range.getLength());
//        }
    }

    /**
     * {@link #onRangeChanged(com.google.gwt.view.client.HasData)} is called when the table requests a new
     * range of data. You can push data back to the displays using
     * {@link #updateRowData(int, java.util.List)}.
     */
    @Override
    protected void onRangeChanged(HasData<RecordList.Record> display) {

        // Get the new range required.
        range = display.getVisibleRange();
        //if it is the last page don't load more results
        if(display.getRowCount()%range.getLength()!= 0 && display.getRowCount() > 0 && range.getStart() > previousRange.getStart()){
            display.setVisibleRange(previousRange.getStart(), previousRange.getLength());
        }

        updateResults(range.getStart(), range.getLength());
        previousRange = range;
    }


    private void updateResults(final int rangeStart, final int rangeLength){
        FilterQuery filterQuery = clientFactory.getLastFilterQuery();
        filterQuery.setFields(getFields());
        try {
            ServerSideSearchDataProvider serverSideSearchDataProvider = (ServerSideSearchDataProvider)clientFactory.getDataProvider();

            rpcService.queryTable(filterQuery, serverSideSearchDataProvider.facetFieldList, rangeStart, rangeLength, tableViewConfig, new AsyncCallback<List<RecordList.Record>>() {
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
        } catch (SearchException e) {
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

    /**
     *Get parameters from functions
     *functions are usually as func(CONCAT_DATE(sf,sTo)) or func(ARITH(pre,tes,%))
     * @param function
     * @return
     */
    public static String[] getParameters(String function){
        if(function==null){return new String[]{};}
        function = function.substring(function.indexOf("(",5)+1, function.indexOf(")",1));
        return function.split(",");
    }
}
