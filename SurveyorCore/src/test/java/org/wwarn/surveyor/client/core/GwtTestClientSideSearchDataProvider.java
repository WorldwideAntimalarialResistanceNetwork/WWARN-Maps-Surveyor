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
import org.wwarn.surveyor.client.util.AsyncCallbackWithTimeout;
import org.wwarn.surveyor.client.util.OfflineStorageUtil;
import org.wwarn.surveyor.client.util.SerializationUtil;

public class GwtTestClientSideSearchDataProvider extends GwtTestServerSideSearchDataProvider{
    private static DataProvider dataProvider;
    private final DataProviderTestUtility dataProviderTestUtility = new DataProviderTestUtility();

    public GwtTestClientSideSearchDataProvider() {
        super(new DataProviderTestUtility.DataProviderSource() {
            @Override
            public DataProvider getDataProvider() {
                GenericDataSource dataSource = GwtTestClientSideSearchDataProvider.getGenericDataSource();
                String[] selectorList = testUtility.getSelectorList();
                boolean isTest = true;
                return new ClientSideSearchDataProvider(dataSource, testUtility.fetchSampleDataSchema(), selectorList, isTest);
            }
        });
    }

    @Override
    protected void gwtSetUp() throws Exception {
        DataSchema schema = dataProviderTestUtility.fetchSampleDataSchema();
        String[] facetFieldList = dataProviderTestUtility.getSelectorList();
        GenericDataSource dataSource = getGenericDataSource();
        final boolean isTest = true;
        dataProvider = new ClientSideSearchDataProvider(dataSource, schema, facetFieldList, isTest);
    }

    static class TestSerializationUtil extends SerializationUtil{
        @Override
        public <T> String serialize(Class<T> aClass, T object1) {
            return super.serialize(aClass, object1);
        }

        @Override
        public <T> T deserialize(Class<T> clazz, String stringContainingSerialisedInput) {
            return super.deserialize(clazz, stringContainingSerialisedInput);
        }
    }

    private static GenericDataSource getGenericDataSource() {
        return new GenericDataSource(LOCATION_DEFAULT_PUBLICATIONS_JSON, Constants.JSON_DATA_SOURCE, GenericDataSource.DataSourceType.JSONPropertyList, DataSourceProvider.ClientSideSearchDataProvider);
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

    @Test
    public void testRecordListView() throws Exception {
        delayTestFinish(10*1000);
        dataProvider.onLoad(new Runnable() {
            @Override
            public void run() {
                final QueryResult lastQueryResult = SimpleClientFactory.getInstance().getLastQueryResult();
                assertNotNull(lastQueryResult);
                final RecordList recordList = lastQueryResult.getRecordList();
                assertTrue(recordList instanceof RecordListCompressedWithInvertedIndexImpl);
                final RecordList uniqueRecordsByPublicationYear = recordList.getUniqueRecordsBy(DataProviderTestUtility.FIELD_PUBLICATION_YEAR);
                assertNotNull(uniqueRecordsByPublicationYear);
                assertTrue(uniqueRecordsByPublicationYear instanceof RecordList);
                assertEquals(8, uniqueRecordsByPublicationYear.size());
                try {
                    dataProvider.query(new MatchAllQuery(), new AsyncCallbackWithTimeout<QueryResult>() {
                        @Override
                        public void onTimeOutOrOtherFailure(Throwable caught) {
                            fail();
                        }

                        @Override
                        public void onNonTimedOutSuccess(QueryResult result) {
                            final RecordList resultRecordList = result.getRecordList();
                            assertNotNull(resultRecordList);
                            final RecordList uniqueRecordsBy = resultRecordList.getUniqueRecordsBy(DataProviderTestUtility.FIELD_PUBLICATION_YEAR);
                            assertTrue(uniqueRecordsBy instanceof RecordList);
                            assertEquals(8, uniqueRecordsBy.size());
                            finishTest();
                        }
                    });
                } catch (SearchException e) {
                    throw new IllegalStateException(e);
                }
            }
        });

    }
}