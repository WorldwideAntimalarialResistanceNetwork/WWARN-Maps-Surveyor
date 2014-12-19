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

import org.junit.Test;
import org.wwarn.surveyor.client.model.DataSourceProvider;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;

public class GwtTestClientSideSearchDataProvider extends GwtTestServerSideSearchDataProvider{
    private static DataProvider dataProvider;
    private final DataProviderTestUtility dataProviderTestUtility = new DataProviderTestUtility();

    public GwtTestClientSideSearchDataProvider() {
        super(new DataProviderTestUtility.DataProviderSource() {
            @Override
            public DataProvider getDataProvider() {
                GenericDataSource dataSource = GwtTestClientSideSearchDataProvider.getGenericDataSource();
                String[] selectorList = testUtility.getSelectorList();
                return new ClientSideSearchDataProvider(dataSource, testUtility.fetchSampleDataSchema(), selectorList);
            }
        });
    }

    @Override
    protected void gwtSetUp() throws Exception {
        DataSchema schema = dataProviderTestUtility.fetchSampleDataSchema();
        String[] facetFieldList = dataProviderTestUtility.getSelectorList();
        GenericDataSource dataSource = getGenericDataSource();
        dataProvider = new ClientSideSearchDataProvider(dataSource, schema, facetFieldList);
    }

    private static GenericDataSource getGenericDataSource() {
        return new GenericDataSource(null, Constants.JSON_DATA_SOURCE, GenericDataSource.DataSourceType.JSONPropertyList, DataSourceProvider.ClientSideSearchDataProvider);
    }

    @Test
    public void testOnLoad() throws Exception {
        delayTestFinish(10*1000);
        dataProvider.onLoad(new Runnable() {
            @Override
            public void run() {
                final QueryResult lastQueryResult = SimpleClientFactory.getInstance().getLastQueryResult();
                assertNotNull(lastQueryResult);
                final RecordList recordList = lastQueryResult.getRecordList();
                assertTrue(recordList instanceof RecordListCompressedWithInvertedIndexImpl);
                RecordListCompressedWithInvertedIndexImpl recordListCompressedWithInvertedIndex = (RecordListCompressedWithInvertedIndexImpl) recordList;
                final FieldInvertedIndex index = recordListCompressedWithInvertedIndex.index;
                assertNotNull(index);
                System.out.println(index);
                finishTest();
            }
        });

    }

    @Override
    public void testQueryWithGreaterThan() throws Exception {
        //not yet implemented
    }

    @Override
    public void testQueryWithSelectors() throws Exception {
        //not yet implemented
    }

    @Override
    public void testQueryWithRangeDateYearOnly() throws Exception {
        //not yet implemented
    }

    @Override
    public void testQueryWithRangeDate() throws Exception {
        //not yet implemented
    }


}