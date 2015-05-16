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

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.visualization.client.visualizations.Table;
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.surveyor.client.util.AsyncCallbackWithTimeout;

import java.util.*;

/**
 * TODO This test case uses AQData as sample data source, is there be a better test data set?
 * User: nigel
 * Date: 19/07/13
 * Time: 15:24
 */
public class GwtTestDefaultLocalJSONDataProvider extends VisualizationTest {

    public static final int QUERY_SIZE = 28;
    public static final int FINISH_TEST_DELAY_TIMEOUT_MILLIS = 1500;
    private DataProviderTestUtility.DataProviderSource dataProviderSource;
    private final DataProviderTestUtility dataProviderTestUtility = new DataProviderTestUtility();
    protected JSONArray jsonArray;
    protected DataSchema schema;
    DataProvider dataProvider;


    public GwtTestDefaultLocalJSONDataProvider(DataProviderTestUtility.DataProviderSource source) {
        this.dataProviderSource = source;
    }

    public GwtTestDefaultLocalJSONDataProvider() {
        this.dataProviderSource = new DataProviderTestUtility.DataProviderSource() {
            @Override
            public DataProvider getDataProvider() {
                final GenericDataSource dataSource = new GenericDataSource(null, Constants.JSON_DATA_SOURCE, GenericDataSource.DataSourceType.JSONPropertyList);
                DefaultLocalJSONDataProvider providerSource = new DefaultLocalJSONDataProvider(dataSource, schema, dataProviderTestUtility.getSelectorList());
                return providerSource;
            }
        };
    }

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        final DataProviderTestUtility testUtility = new DataProviderTestUtility();
        jsonArray = testUtility.getJSONArray();
        schema = testUtility.fetchSampleDataSchema();

    }

    @Override
    protected String[] getVisualizationPackage() {
        return new String[]{Table.PACKAGE};    //Must Override
    }


    void runTestWithDefaultDataSetup(final Runnable runnable){
        final boolean callFinishTest = false; // important as if this is called other calls to finishTest fails
        delayTestFinish(FINISH_TEST_DELAY_TIMEOUT_MILLIS);
        loadApi(new Runnable() {
            @Override
            public void run() {
                dataProvider = dataProviderSource.getDataProvider();
                dataProvider.onLoad(new Runnable() {
                    @Override
                    public void run() {
                        runnable.run();
                    }
                });
            }
        }, callFinishTest);
    }

    public void testMatchAllQuery() throws Exception {
        runTestWithDefaultDataSetup(new Runnable() {
            @Override
            public void run() {
                try {
                    dataProvider.query(new MatchAllQuery(), new AsyncCallbackWithTimeout<QueryResult>() {
                        @Override
                        public void onTimeOutOrOtherFailure(Throwable caught) {
                            this.onFailure(caught);
                        }

                        @Override
                        public void onNonTimedOutSuccess(QueryResult result) {
                            this.onSuccess(result);
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            throw new IllegalStateException(throwable);
                        }

                        @Override
                        public void onSuccess(QueryResult queryResult) {
                            RecordList recordList = queryResult.getRecordList();
                            assertEquals(QUERY_SIZE, recordList.size());

                            // validate all records are present in the query

                            final DataProviderTestUtility testUtility = new DataProviderTestUtility();
                            jsonArray = testUtility.getJSONArray();
                            schema = testUtility.fetchSampleDataSchema();
                            assertNotNull(jsonArray);
                            assertNotNull(schema);
                            List<RecordList.Record> records = recordList.getRecords();
                            for (int rowIdx = 0; rowIdx < jsonArray.size(); rowIdx++) {
                                JSONObject jsonObject = jsonArray.get(rowIdx).isObject();
                                Set<String> keys = jsonObject.keySet();
                                for (String key : keys) {
                                    JSONValue value = jsonObject.get(key);
                                    if (schema.hasColumn(key)) {
                                        RecordList.Record record = records.get(rowIdx);
                                        String field = record.getValueByFieldIndex(schema.getColumnIndex(key));
                                        switch (schema.getType(key)){
                                            case Date:
                                                String dateField = field;
                                                isValidDate("Failed on jsonObject:" + jsonObject.toString() + ",\n record:" + record, dateField);
                                                assertTrue("Failed on jsonObject:" + jsonObject.toString() + ",\n record:" + record, dateField.startsWith(parseDateYearOnly(dateField)));
                                                break;
                                            case DateYear:
                                                dateField = field;
                                                isValidDate("Failed on jsonObject:" + jsonObject.toString() + ",\n record:" + record, dateField);
                                                assertEquals("Failed on jsonObject:" + jsonObject.toString() + ",\n record:" + record, value.toString(), parseDateYearOnly(dateField));
                                                break;
                                            case String:
                                            case CoordinateLat:
                                            case CoordinateLon:
                                            case Integer:
                                            case Boolean:
                                                String valueString = (value.isString() == null) ? value.toString() : value.isString().stringValue();
                                                assertEquals("Failed on jsonObject:" + jsonObject.toString() + ",\n record:" + record, valueString, field);
                                                break;
                                        }
                                    }
                                }
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

    public void testFilterQueryByString() throws Exception {
        runTestWithDefaultDataSetup(new Runnable() {
            @Override
            public void run() {
            FilterQuery filterQuery = new FilterQuery();
            filterQuery.addFilter("PUB", "Nigerian Tribune");
                try {
                    dataProvider.query(filterQuery, new AsyncCallbackWithTimeout<QueryResult>() {
                        @Override
                        public void onTimeOutOrOtherFailure(Throwable caught) {
                            this.onFailure(caught);
                        }

                        @Override
                        public void onNonTimedOutSuccess(QueryResult result) {
                            this.onSuccess(result);
                        }
                        @Override
                        public void onFailure(Throwable throwable) {
                            throw new IllegalStateException(throwable);
                        }

                        @Override
                        public void onSuccess(QueryResult queryResult) {
                            assertEquals(5, queryResult.getRecordList().size());
                            finishTest();
                        }
                    });
                } catch (SearchException e) {
                    throw new IllegalStateException(e);
                }

                filterQuery = new FilterQuery();
            filterQuery.addFilter("QI", "Substandard");
                try {
                    dataProvider.query(filterQuery, new AsyncCallbackWithTimeout<QueryResult>() {
                        @Override
                        public void onTimeOutOrOtherFailure(Throwable caught) {
                            this.onFailure(caught);
                        }

                        @Override
                        public void onNonTimedOutSuccess(QueryResult result) {
                            this.onSuccess(result);
                        }
                        @Override
                        public void onFailure(Throwable throwable) {
                            throw new IllegalStateException(throwable);
                        }

                        @Override
                        public void onSuccess(QueryResult queryResult) {
                            assertEquals(1, queryResult.getRecordList().size());
                            finishTest();
                        }
                    });
                } catch (SearchException e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    public void testFilterQueryByMultipleFields() throws Exception {
        runTestWithDefaultDataSetup(new Runnable() {
            @Override
            public void run() {
                FilterQuery filterQuery = new FilterQuery();
                filterQuery.addFilter("PUB", "Nigerian Tribune");
                filterQuery.addFilter("PID", "187");
                // should fetch record:
                //"{\"PID\":187,\"DOI\":\"\",\"TTL\":\"NAFDAC´s unfinished business in Kaduna\",\"FA\":\"Nigerian Tribune\",\"PY\":2011,\"PUB\":\"Nigerian Tribune\",\"PTI\":4,\"PTN\":\"Lay press\",\"URL\":\"\",\"DID\":29,\"DSN\":\"AL\",\"DN\":\"Artemether-Lumefantrine\",\"CID\":30,\"CN\":\"Nigeria\",\"CLAT\":9.17583,\"CLON\":7.167,\"LID\":100,\"LN\":\"Kaduna\",\"LLAT\":10.51667,\"LLON\":7.433333,\"SDI\":516,\"OTI\":2,\"OTN\":\"Private pharmacy\",\"STI\":6,\"STN\":\"MRA seizure\",\"DQI\":0,\"QI\":\"Falsified\",\"FR\":100,\"SD\":\"15/02/2010\",\"ED\":\"15/02/2010\",\"ICR\":\"-1\",\"DCN\":\"ACT\",\"NST\":-1}," +

                try {
                    dataProvider.query(filterQuery, new AsyncCallbackWithTimeout<QueryResult>() {
                        @Override
                        public void onTimeOutOrOtherFailure(Throwable caught) {
                            this.onFailure(caught);
                        }

                        @Override
                        public void onNonTimedOutSuccess(QueryResult result) {
                            this.onSuccess(result);
                        }
                        @Override
                        public void onFailure(Throwable throwable) {
                            throw new IllegalStateException(throwable);
                        }

                        @Override
                        public void onSuccess(QueryResult query) {
                            assertEquals(1, query.getRecordList().size());
                            RecordList.Record record = query.getRecordList().getRecords().get(0);

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

    void isValidDate(String message, String publishedYear) {
        assertFalse(message, StringUtils.isEmpty(publishedYear.trim()));
    }

    public void testFilterQueryByLatLon() throws Exception {
        runTestWithDefaultDataSetup(new Runnable() {
            @Override
            public void run() {
                FilterQuery filterQuery = new FilterQuery();
                filterQuery.addFilter("CLAT", "9.17583");
                filterQuery.addFilter("CLON", "7.167");
                try {
                    dataProvider.query(filterQuery, new AsyncCallbackWithTimeout<QueryResult>() {
                        @Override
                        public void onTimeOutOrOtherFailure(Throwable caught) {
                            this.onFailure(caught);
                        }

                        @Override
                        public void onNonTimedOutSuccess(QueryResult result) {
                            this.onSuccess(result);
                        }
                        @Override
                        public void onFailure(Throwable throwable) {
                            System.out.println();
                        }

                        @Override
                        public void onSuccess(QueryResult query) {
                            assertEquals(11, query.getRecordList().size());
                            finishTest();
                        }
                    });
                } catch (SearchException e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    public void testFilterQueryByInteger() throws Exception {
        runTestWithDefaultDataSetup(new Runnable() {
            @Override
            public void run() {
                FilterQuery filterQuery = new FilterQuery();
                filterQuery.addFilter("PID", "128");
                filterQuery.addFilter("PY", "2010");
                try {
                    dataProvider.query(filterQuery, new AsyncCallbackWithTimeout<QueryResult>() {
                        @Override
                        public void onTimeOutOrOtherFailure(Throwable caught) {
                            this.onFailure(caught);
                        }

                        @Override
                        public void onNonTimedOutSuccess(QueryResult result) {
                            this.onSuccess(result);
                        }
                        @Override
                        public void onFailure(Throwable throwable) {
                            throw new IllegalStateException(throwable);
                        }

                        @Override
                        public void onSuccess(QueryResult query) {
                            assertEquals(4, query.getRecordList().size());
                            final FacetList facetFields = query.getFacetFields();
                            for (FacetList.FacetField facetField : facetFields) {
                                assertTrue(facetField.getDistinctFacetValues().size() > 0);
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



    public void testQueryWithSelectors() throws Exception {
        runTestWithDefaultDataSetup(new Runnable() {
            @Override
            public void run() {
                try {
                    dataProvider.query(new MatchAllQuery(), new AsyncCallbackWithTimeout<QueryResult>() {
                        @Override
                        public void onTimeOutOrOtherFailure(Throwable caught) {
                            this.onFailure(caught);
                        }

                        @Override
                        public void onNonTimedOutSuccess(QueryResult result) {
                            this.onSuccess(result);
                        }
                        @Override
                        public void onFailure(Throwable throwable) {
                            throw new IllegalStateException(throwable);
                        }

                        @Override
                        public void onSuccess(QueryResult query) {
                            assertNotNull(query);
                            assertNotNull(query.getRecordList());
                            assertNotNull(query.getFacetFields());
                            assertEquals("Expected at least 3 facets", 3, query.getFacetFields().size());
                            for (FacetList.FacetField facetField : query.getFacetFields()) {
                                assertNotNull(facetField);
                                assertNotNull(facetField.getDistinctFacetValues());
                                assertNotNull(facetField.getFacetField());
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

    public void testQueryWithRangeDateYearOnly() throws Exception {

        runTestWithDefaultDataSetup(new Runnable() {
            @Override
            public void run() {
            FilterQuery filterQuery = new FilterQuery();
            filterQuery.addRangeFilter("PY", "2001", "2002");
                try {
                    dataProvider.query(filterQuery, new AsyncCallbackWithTimeout<QueryResult>() {
                        @Override
                        public void onTimeOutOrOtherFailure(Throwable caught) {
                            this.onFailure(caught);
                        }

                        @Override
                        public void onNonTimedOutSuccess(QueryResult result) {
                            this.onSuccess(result);
                        }
                        @Override
                        public void onFailure(Throwable throwable) {
                            throw new IllegalStateException(throwable);
                        }

                        @Override
                        public void onSuccess(QueryResult query) {
                            assertNotNull(query);
                            assertNotNull(query.getRecordList());
                            assertNotNull(query.getFacetFields());
                            assertEquals("Expected only 3 results", 3, query.getRecordList().size());
                            assertEquals("Expected at least 3 facets", 3, query.getFacetFields().size());
                            for (FacetList.FacetField facetField : query.getFacetFields()) {
                                assertNotNull(facetField);
                                assertNotNull(facetField.getDistinctFacetValues());
                                assertNotNull(facetField.getFacetField());
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

    public void testQueryWithRangeDate() throws Exception {
        runTestWithDefaultDataSetup(new Runnable() {
            @Override
            public void run() {
                FilterQuery filterQuery = new FilterQuery();
                filterQuery.addRangeFilter("SD", parseYearOnly("2001"), parseYearOnly("2002"));
                try {
                    dataProvider.query(filterQuery, new AsyncCallbackWithTimeout<QueryResult>() {
                        @Override
                        public void onTimeOutOrOtherFailure(Throwable caught) {
                            this.onFailure(caught);
                        }

                        @Override
                        public void onNonTimedOutSuccess(QueryResult result) {
                            this.onSuccess(result);
                        }
                        @Override
                        public void onFailure(Throwable throwable) {
                            throw new IllegalStateException(throwable);
                        }

                        @Override
                        public void onSuccess(QueryResult query) {
                            assertNotNull(query);
                            assertNotNull(query.getRecordList());
                            assertNotNull(query.getFacetFields());
                            assertEquals(1, query.getRecordList().size());
                            for (FacetList.FacetField facetField : query.getFacetFields()) {
                                assertNotNull(facetField);
                                assertNotNull(facetField.getDistinctFacetValues());
                                assertNotNull(facetField.getFacetField());
                            }
                            assertEquals("Expected at least 3 facets", 3 , query.getFacetFields().size());
                            finishTest();
                        }
                    });
                } catch (SearchException e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    public void testQueryWithGreaterThan() throws Exception {
        runTestWithDefaultDataSetup(new Runnable() {
            @Override
            public void run() {
                FilterQuery filterQuery = new FilterQuery();
                filterQuery.addFilterGreater("PID", 200);
                try {
                    dataProvider.query(filterQuery, new AsyncCallbackWithTimeout<QueryResult>() {
                        @Override
                        public void onTimeOutOrOtherFailure(Throwable caught) {
                            this.onFailure(caught);
                        }

                        @Override
                        public void onNonTimedOutSuccess(QueryResult result) {
                            this.onSuccess(result);
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            throw new IllegalStateException(throwable);
                        }

                        @Override
                        public void onSuccess(QueryResult query) {
                            assertNotNull(query);
                            assertNotNull(query.getRecordList());
                            assertNotNull(query.getFacetFields());
                            assertEquals(4, query.getRecordList().size());
                            for (FacetList.FacetField facetField : query.getFacetFields()) {
                                assertNotNull(facetField);
                                assertNotNull(facetField.getDistinctFacetValues());
                                assertNotNull(facetField.getFacetField());
                            }
                            assertEquals("Expected at least 3 facets", 3, query.getFacetFields().size());
                            finishTest();
                        }
                    });
                } catch (SearchException e) {
                    throw new IllegalStateException(e);
                }
            }
        });


    }

    private Date parseYearOnly(String rawDate){
        return  DataType.ParseUtil.parseDateYearOnly(rawDate);
    }

    public String getModuleName() {
        return "org.wwarn.surveyor.surveyorJUnit";
    }

    String parseDateYearOnly(String value) {
        if(value == null || !value.contains("-")){
            throw new IllegalArgumentException("Illegal date value:"+value);
        }
        int parsedYear = Integer.parseInt(value.split("-")[0].trim());
        return String.valueOf(parsedYear);
    }
}
