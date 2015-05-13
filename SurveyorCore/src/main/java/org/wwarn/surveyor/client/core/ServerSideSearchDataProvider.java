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

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.wwarn.surveyor.client.mvp.ClientFactory;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;

import java.util.List;

/**
 * Created by nigelthomas on 27/05/2014.
 */
public class ServerSideSearchDataProvider implements DataProvider {
    protected final GenericDataSource dataSource;
    protected SearchServiceAsync searchServiceAsync = (SearchServiceAsync) GWT.create(SearchService.class);
    protected ClientFactory clientFactory = SimpleClientFactory.getInstance();
    protected final DataSchema schema;
    public String[] facetFieldList;

    public ServerSideSearchDataProvider(GenericDataSource dataSource, DataSchema dataSchema, String[] fieldList) {
        this.dataSource = dataSource;
        this.schema = dataSchema;
        this.facetFieldList = fieldList;
    }

    @Override
    public void onLoad(final Runnable callOnLoad) {
        try {
            InitialFilterQuery initialFilterQuery = getInitialFilterQuery();
            clientFactory.setLastFilterQuery(initialFilterQuery.getInitialFilterQuery());

            searchServiceAsync.preFetchData(schema, this.dataSource, this.facetFieldList, initialFilterQuery.getInitialFilterQuery(), new AsyncCallback<QueryResult>() {
                @Override
                public void onFailure(Throwable throwable) {
                    throw new IllegalStateException(throwable);
                }

                @Override
                public void onSuccess(QueryResult queryResult) {
                    clientFactory.setLastQueryResult(queryResult);
                    callOnLoad.run();
                }
            });
        } catch (SearchException e) {
            Log.error("Unable to initalise the index", e);
            throw new IllegalStateException(e);
        }
    }

    static InitialFilterQuery initialFilterQuery;
    static class DefaultInitialFilterQuery implements InitialFilterQuery{

        @Override
        public FilterQuery getInitialFilterQuery() {
            return new MatchAllQuery();
        }
    }
    InitialFilterQuery getInitialFilterQuery() {
        if(initialFilterQuery != null){ return initialFilterQuery; }
        try {
            initialFilterQuery = GWT.create(InitialFilterQuery.class);
        }catch(RuntimeException e){
            if(!e.getMessage().startsWith("Deferred binding")) throw e;
            //by pass deferred binding error and use default value
            initialFilterQuery = new DefaultInitialFilterQuery();
        }
        return initialFilterQuery;
    }

    @Override
    public void query(FilterQuery filterQuery, String[] facetFields, AsyncCallback<QueryResult> queryResultCallBack) throws SearchException {
        try {
            searchServiceAsync.query(filterQuery, facetFields,  queryResultCallBack);
        } catch (SearchException e) {
            Log.error("Query failed with error", e);
            throw e;
        }
    }

    @Override
    public void query(FilterQuery filterQuery, AsyncCallback<QueryResult> queryResultCallBack) throws SearchException {
        try {
            searchServiceAsync.query(filterQuery, this.facetFieldList, queryResultCallBack);
        } catch (SearchException e) {
            Log.error("Error completing query", e);
            throw (e);
        }
    }

    public void queryUniqueRecords(FilterQuery filterQuery, AsyncCallback<QueryResult> queryResultCallBack) throws SearchException {
        try {
            searchServiceAsync.queryUniqueRecords(filterQuery, this.facetFieldList, queryResultCallBack);
        } catch (Exception e) {
            Log.error("query unique record failed", e);
            throw new SearchException("query unique record failed", e);
        }
    }
}
