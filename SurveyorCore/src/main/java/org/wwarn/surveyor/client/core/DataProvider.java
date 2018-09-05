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


import com.google.gwt.user.client.rpc.AsyncCallback;
import org.wwarn.surveyor.client.util.AsyncCallbackWithTimeout;
import org.wwarn.surveyor.server.core.SearchServiceLayer;

/**
 * A basic facade over a data source exposing retrieval methods which support simple queries and faceted search.
 * Faceted search, also called faceted navigation or faceted browsing, is a technique for accessing information
 * organized according to a faceted classification system, allowing users to explore a collection of information by
 * applying multiple filters, <a href="http://en.wikipedia.org/wiki/Faceted_search">see wiki definition</a>
 * User: nigel
 * Date: 19/07/13
 * Time: 10:59
 */
public interface DataProvider {
    /**
     * Call first to check data has loaded before continuing with query
     * @param callOnLoad
     */
    void onLoad (Runnable callOnLoad);

    /**
     * A basic façade for a data source, supports filter queries and returns a query result containing dataset
     * and adjusted map of facet fields x unique(facet values)
     *
     * @param filterQuery allows specifying a query composed of a field and the value to filter upon.
     * @param facetFields a facetField is a field defined in the schema for which distinct values should be returned
     * @param queryResultCallBack containing the data set and adjusted map of facet fields x unique(facet field values)
     * @see FilterQuery
     * @see QueryResult
     */
    void query(FilterQuery filterQuery, String[] facetFields, AsyncCallbackWithTimeout<QueryResult> queryResultCallBack) throws SearchException;

    /**
     * A basic façade for a data source
     * @param filterQuery a filter query is a set of of fields with values to restrict the results by
     * @param queryResultCallBack  query result containing the data set and adjusted map of facet fields x unique(facet field values)
     * @see FilterQuery
     * @see QueryResult
     */
    void query(FilterQuery filterQuery, AsyncCallbackWithTimeout<QueryResult> queryResultCallBack) throws SearchException;
}
