package org.wwarn.surveyor.server.core;

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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wwarn.surveyor.client.core.*;
import org.wwarn.surveyor.client.model.TableViewConfig;

import java.lang.reflect.Array;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by suay on 8/1/14.
 */
public class LuceneSearchServiceImplTest {

    final LuceneSearchServiceImpl luceneSearchService = LuceneSearchServiceImpl.getInstance();
    final DataProviderTestUtility testUtility = new DataProviderTestUtility();

    @Before
    public void setup() throws Exception{

        final DataSchema schema = testUtility.fetchSampleDataSchema();
        final GenericDataSource dataSource = new GenericDataSource(null, Constants.JSON_DATA_SOURCE_EXTENDED, GenericDataSource.DataSourceType.JSONPropertyList);

        luceneSearchService.init(schema, dataSource);
        assertNotNull(luceneSearchService);
    }

    @Test
    public void queryAllFields() throws Exception{
        FilterQuery filterQuery = new FilterQuery();
        filterQuery.addFilter("PID", "128");
        filterQuery.addFilter("PY", "2010");
        QueryResult queryResult = luceneSearchService.query(filterQuery, testUtility.getSelectorList());
        assertNotNull(queryResult);

        RecordList recordList = queryResult.getRecordList();
        assertEquals(recordList.size(), 4);
        for (RecordList.Record record : recordList.getRecords()){
            assertEquals(record.getValueByFieldName("PID"), "128");
        }
    }

    @Test
    public void queryAllFieldsWithEmptySet() throws Exception{
        FilterQuery filterQuery = new FilterQuery();
        filterQuery.addFilter("PID", "128");
        filterQuery.addFilter("PY", "2010");
        filterQuery.setFields(Collections.EMPTY_SET);
        QueryResult queryResult = luceneSearchService.query(filterQuery, testUtility.getSelectorList());
        assertNotNull(queryResult);

        RecordList recordList = queryResult.getRecordList();
        assertEquals(recordList.size(), 4);
        for (RecordList.Record record : recordList.getRecords()){
            assertEquals(record.getValueByFieldName("PID"), "128");
        }
    }

    @Test
    public void querySelectedFields() throws Exception{
        FilterQuery filterQuery = new FilterQuery();
        //Add table fields
        Set<String> selectedFields = new HashSet<String>(Arrays.asList("PID", "PY", "PTN"));
        filterQuery.setFields(selectedFields);

        //Add filter
        filterQuery.addFilter("PID", "128");
        filterQuery.addFilter("PY", "2010");

        QueryResult queryResult = luceneSearchService.query(filterQuery, testUtility.getSelectorList());
        assertNotNull(queryResult);

        RecordList recordList = queryResult.getRecordList();
        assertEquals(recordList.size(), 4);
        for (RecordList.Record record : recordList.getRecords()){
            assertEquals(record.getValueByFieldName("PID"), "128");
        }
    }

    @Test
    public void queryTable() throws Exception{
        FilterQuery filterQuery = new FilterQuery();

        //Add table fields
        Set<String> selectedFields = new HashSet<String>(Arrays.asList("PID", "PY", "PTN"));
        filterQuery.setFields(selectedFields);

        //Add filter
        filterQuery.addFilter("PID", "128");
        filterQuery.addFilter("PY", "2010");

        List<RecordList.Record> records = luceneSearchService.queryTable(filterQuery, testUtility.getSelectorList(), 0, 10, null);
        assertNotNull(records);

        assertEquals(records.size(), 1);
        for (RecordList.Record record : records){
            assertEquals(record.getValueByFieldName("PID"), "128");
        }
    }

    @Test
    public void queryPageSizeTable() throws Exception{
        FilterQuery filterQuery = new FilterQuery();

//        //Add table fields
        Set<String> selectedFields = new HashSet<String>(Arrays.asList("PID", "PUB"));
        filterQuery.setFields(selectedFields);

        //Add filter
        filterQuery.addFilter("PTN", "Lay press");

        List<RecordList.Record> records = luceneSearchService.queryTable(filterQuery, testUtility.getSelectorList(), 0, 50, null);
        assertEquals(18,records.size());

        records = luceneSearchService.queryTable(filterQuery, testUtility.getSelectorList(), 0, 10, null);
        assertEquals(10,records.size());

        records = luceneSearchService.queryTable(filterQuery, testUtility.getSelectorList(), 10, 10, null);
        assertEquals(8, records.size());

    }

    @Test(expected = SearchException.class)
    public void queryTableWithoutTableFields() throws Exception{
        // A Filterquery without table fields should throw a SearchException
        FilterQuery filterQuery = new FilterQuery();
        //Add filter
        filterQuery.addFilter("PID", "128");
        filterQuery.addFilter("PY", "2010");

        List<RecordList.Record> records = luceneSearchService.queryTable(filterQuery, testUtility.getSelectorList(), 0, 10, null);
    }

    @Test
    public void testSortedTable() throws Exception{

        FilterQuery filterQuery = new FilterQuery();
        Set<String> selectedFields = new HashSet<String>(Arrays.asList("PID", "PUB", "PY"));
        filterQuery.setFields(selectedFields);
        filterQuery.addFilter("PID", "166");

        TableViewConfig tableViewConfig = new TableViewConfig("tableView", "tableLabel");
        tableViewConfig.setSortColumn("PY");
        tableViewConfig.setSortOrder("desc");

        List<RecordList.Record> records = luceneSearchService.queryTable(filterQuery, testUtility.getSelectorList(), 0, 10, tableViewConfig);

        assertEquals(3, records.size());

        assertEquals("2012", records.get(0).getValueByFieldName("PY").substring(0,4));
        assertEquals("2011", records.get(1).getValueByFieldName("PY").substring(0,4));
        assertEquals("2010", records.get(2).getValueByFieldName("PY").substring(0,4));
    }


}
