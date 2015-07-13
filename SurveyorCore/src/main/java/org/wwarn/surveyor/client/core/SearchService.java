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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.core.client.GWT;
import org.wwarn.surveyor.client.model.TableViewConfig;
import org.wwarn.surveyor.client.mvp.DataSource;

import java.util.List;

/**
 * Created by nigelthomas on 28/05/2014.
 */
@RemoteServiceRelativePath("SearchService")
public interface SearchService extends RemoteService {

    /**
     * Utility/Convenience class.
     * Use SearchService.App.getInstance() to access static instance of SearchServiceAsync
     */
    public static class App {

        private static final SearchServiceAsync ourInstance = (SearchServiceAsync) GWT.create(SearchService.class);
        public static SearchServiceAsync getInstance() {
            return ourInstance;
        }

    }
    /**
     * Call first to initialise the index
     * @param schema
     * @param dataSource
     * @param facetList
     * @return
     */
    public QueryResult preFetchData(DataSchema schema, GenericDataSource dataSource, String[] facetList, FilterQuery filterQuery) throws SearchException;

    public String fetchDataVersion(DataSchema schema, GenericDataSource dataSource) throws SearchException;

    public QueryResult query(FilterQuery filterQuery, String[] facetFields) throws SearchException;

    public List<RecordList.Record> queryTable(FilterQuery filterQuery, String[] facetFields ,int start, int length, TableViewConfig tableViewConfig) throws SearchException;

    public QueryResult queryUniqueRecords(FilterQuery filterQuery, String[] facetFields) throws SearchException;
}
