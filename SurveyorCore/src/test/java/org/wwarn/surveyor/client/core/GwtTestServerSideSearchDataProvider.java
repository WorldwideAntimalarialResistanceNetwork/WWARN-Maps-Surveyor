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

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.junit.Test;
import org.wwarn.surveyor.client.event.FilterChangedEvent;

import java.util.*;

/**
 * Created by nigelthomas on 29/05/2014.
 */
public class GwtTestServerSideSearchDataProvider extends GwtTestDefaultLocalJSONDataProvider{
    private final DataProviderTestUtility dataProviderTestUtility = new DataProviderTestUtility();

    public GwtTestServerSideSearchDataProvider() {
        super(new DataProviderTestUtility.DataProviderSource() {
            @Override
            public DataProvider getDataProvider() {
//                jsonArray = testUtility.getJSONArray();
//                schema = testUtility.fetchSampleDataSchema();
//                String[] selectorList = testUtility.getSelectorList();

                GenericDataSource dataSource = new GenericDataSource(null, Constants.JSON_DATA_SOURCE, GenericDataSource.DataSourceType.JSONPropertyList);
                String[] selectorList = testUtility.getSelectorList();
                return new ServerSideSearchDataProvider(dataSource, testUtility.fetchSampleDataSchema(), selectorList);
            }
        });
    }

    public GwtTestServerSideSearchDataProvider(DataProviderTestUtility.DataProviderSource dataProviderSource) {
        super(dataProviderSource);
    }

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        DataSchema schema = dataProviderTestUtility.fetchSampleDataSchema();
        String[] facetFieldList = dataProviderTestUtility.getSelectorList();
        GenericDataSource dataSource = new GenericDataSource(null, Constants.JSON_DATA_SOURCE, GenericDataSource.DataSourceType.JSONPropertyList);
        dataProvider = new ServerSideSearchDataProvider(dataSource, schema, facetFieldList);
    }

    @Test
    public void testOnLoad() throws Exception {
        delayTestFinish(10*1000);
        dataProvider.onLoad(new Runnable() {
            @Override
            public void run() {
                finishTest();
            }
        });

    }

    @Test
    public void testQuery() throws Exception {

        delayTestFinish(10*1000);
        final AsyncCallback<QueryResult> callback = new AsyncCallback<QueryResult>() {
            @Override
            public void onFailure(Throwable throwable) {
                fail();
            }

            @Override
            public void onSuccess(QueryResult queryResult) {
                assertNotNull(queryResult);
                assertNotNull(queryResult.getFacetFields());
                final RecordList recordList = queryResult.getRecordList();
                assertNotNull(recordList);
                assertTrue(recordList.size()>1);
                assertEquals(3, recordList.size());
                for (RecordList.Record record : recordList.getRecords()) {
                    final String publicationYear = record.getValueByFieldName(DataProviderTestUtility.FIELD_PUBLICATION_YEAR);
                    assertDateFormatIsISO(publicationYear, DataType.ISO_DATE_FORMAT);
                }
                System.out.println("testQuery complete");
                finishTest();
            }
        };



        runTestWithDefaultDataSetup(new Runnable() {
            @Override
            public void run() {
                final FilterQuery filterQuery = new FilterQuery();
                Set<String> values = new HashSet<String>(Arrays.asList("122", "135"));
                filterQuery.addMultipleValuesFilter("PID", values);

                try {
                    dataProvider.query(filterQuery, new AsyncCallback<QueryResult>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                            fail();
                        }

                        @Override
                        public void onSuccess(QueryResult queryresult) {

                            try {
                                dataProvider.query(filterQuery, callback);
                            } catch (SearchException e) {
                                throw new IllegalStateException(e);
                            }
                            finishTest();
                        }
                    });
                } catch (SearchException e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    /**
     * Multiple values for the same Field will only work in the server side implementation.
     * In the client side,the data provider will only filter by the last value inserted.
     */
    public void testFilterQuerySingleFieldMultipleOptionsSelectedAllString() throws Exception {
        runTestWithDefaultDataSetup(new Runnable() {
            @Override
            public void run() {
                FilterQuery filterQuery = new FilterQuery();
                Set<String> values = new HashSet<String>(Arrays.asList("122", "135"));
                filterQuery.addMultipleValuesFilter("PID", values);

                try {
                    dataProvider.query(filterQuery, new AsyncCallback<QueryResult>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                            fail();
                        }

                        @Override
                        public void onSuccess(QueryResult queryresult) {
                            assertEquals(3, queryresult.getRecordList().size());
                            assertEquals("122", queryresult.getRecordList().getRecords().get(0).getValueByFieldName("PID"));
                            assertEquals("122", queryresult.getRecordList().getRecords().get(2).getValueByFieldName("PID"));
                            RecordList.Record record = queryresult.getRecordList().getRecords().get(1);

                            String pid = record.getValueByFieldName("PID");
                            assertEquals("135", pid);

                            String publishedYear = record.getValueByFieldName("PY");
                            isValidDate("Failed on record:" + ",\n record:" + record, publishedYear);
                            int year = Integer.parseInt(parseDateYearOnly(publishedYear));
                            assertEquals(2011, year);

                            String clat = record.getValueByFieldName("CLAT");
                            assertEquals("9.17583", clat);

                            String clon = record.getValueByFieldName("CLON");
                            assertEquals("7.167", clon);

                            String pub = record.getValueByFieldName("PUB");
                            assertEquals("Nigerian Tribune", pub);
                            finishTest();
                        }
                    });
                } catch (SearchException e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }



    public void testFilterQueryWithMultipleFilterValue() throws Exception {
        runTestWithDefaultDataSetup(new Runnable() {
            @Override
            public void run() {
                //Create a Filter Changed Event
                Set<String> values = new HashSet<String>(Arrays.asList("122", "135"));
                FilterChangedEvent filterChangedEvent = new FilterChangedEvent("PID", values);

                //Get the MultipleFilterValue and compare with the initial values
                List<FilterChangedEvent.FilterElement> filterElements = filterChangedEvent.getSelectedListItems();
                FilterChangedEvent.MultipleFilterValue listValues = (FilterChangedEvent.MultipleFilterValue) filterElements.iterator().next();
                assertEquals(values, listValues.getFacetFieldValues());

                //Create a query using the values from the event
                FilterQuery filterQuery = new FilterQuery();
                filterQuery.addMultipleValuesFilter("PID", values);

                try {
                    dataProvider.query(filterQuery, new AsyncCallback<QueryResult>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                            System.out.println("error");
                            fail();
                        }

                        @Override
                        public void onSuccess(QueryResult queryresult) {
                            assertEquals(3, queryresult.getRecordList().size());
                            assertEquals("122", queryresult.getRecordList().getRecords().get(0).getValueByFieldName("PID"));
                            assertEquals("122", queryresult.getRecordList().getRecords().get(2).getValueByFieldName("PID"));
                            RecordList.Record record = queryresult.getRecordList().getRecords().get(1);

                            String pid = record.getValueByFieldName("PID");
                            assertEquals("135", pid);
                           finishTest();
                        }
                    });
                } catch (SearchException e) {
                    fail(e.getMessage());
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    private void assertDateFormatIsISO(String publicationYear, String format) {
        DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(format);
        final Date date = dateTimeFormat.parse(publicationYear);
        assertNotNull("publication year ", date);
    }

    public String getModuleName() {
        return "org.wwarn.surveyor.surveyorJUnit";
    }
}
