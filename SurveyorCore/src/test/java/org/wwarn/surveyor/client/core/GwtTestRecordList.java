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

import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.Table;
import org.junit.Test;
import org.wwarn.surveyor.client.model.DataSourceProvider;
import org.wwarn.surveyor.client.model.DatasourceConfig;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * User: nigelthomas
 * Date: 22/10/2013
 * Time: 14:14
 */
public class GwtTestRecordList extends VisualizationTest {

    public static final String UNIQUE_FIELD = "uniqueField";
    public static final String NON_UNIQUE_FIELD = "nonUniqueField";
    private DataTable table;
    private DataSchema schema;
    private int rowIndex = 0;
    private final DataProviderTestUtility dataProviderTestUtility = new DataProviderTestUtility();

    @Override
    protected String[] getVisualizationPackage() {
        return new String[]{Table.PACKAGE};    //Must Override
    }

    public String getModuleName() {
        return "org.wwarn.surveyor.surveyorJUnit";
    }
    private void runTestWithDefaultDataSetup(final Runnable runnable){
        loadApi(new Runnable() {
            @Override
            public void run() {
                setupDefaultData();
                runnable.run();
            }
        });
    }

    public void setupDefaultData(){
        String dataSourceType = DataSourceProvider.LocalClientSideDataProvider.name();
        schema = new DataSchema(new DatasourceConfig("UniqueId","", dataSourceType));
        schema.addField(UNIQUE_FIELD, DataType.Integer);
        schema.addField(NON_UNIQUE_FIELD, DataType.String);
        table = DataTable.create();
        table.addColumn(AbstractDataTable.ColumnType.NUMBER, UNIQUE_FIELD);
        table.addColumn(AbstractDataTable.ColumnType.STRING, NON_UNIQUE_FIELD);
        table.addRows(4);
        addRow(1, "a1");
        addRow(2, "b1");
        addRow(2, "b2");
        addRow(2, "b3");
    }

    private void addRow(int column0, String column1) {
        table.setValue(rowIndex, 0, column0);
        table.setValue(rowIndex,1, column1);
        rowIndex++;
    }

    @Test
    public void testGetUniqueRecordsBy() throws Exception {
        runTestWithDefaultDataSetup(new Runnable() {
            @Override
            public void run() {
                assertEquals(2, table.getNumberOfColumns());
                assertEquals(4, table.getNumberOfRows());

                final DataTableConversionUtility dataTableConversionUtility = new DataTableConversionUtility();
                RecordList recordList  = dataTableConversionUtility.convertDataTableToRecordList(schema, table);

                assertNotNull(recordList);
                assertEquals(4, recordList.size());
                // test that only the 2-> b1 is returned after uniqueRecords is called, as current impl assumes first available
                // unique records is returned.
                final RecordList uniqueRecords = recordList.getUniqueRecordsBy(UNIQUE_FIELD);
                final List<RecordList.Record> records = uniqueRecords.getRecords();
                assertNotNull(uniqueRecords);
                assertNotNull(records);
                assertEquals(2, records.size());
                assertEquals("Record{fields=[1, a1]}", records.get(0).toString());
                assertEquals("Record{fields=[2, b1]}", records.get(1).toString());
                assertEquals(2, uniqueRecords.size());
            }
        });
    }

    @Test
    public void testRecordListCompressedBuilder() throws Exception {
        // test base case

        final RecordListBuilder.CompressionMode[] values = RecordListBuilder.CompressionMode.values();
        assertTrue(values.length > 0);
        for (RecordListBuilder.CompressionMode compressionMode : values) {
            System.out.println(compressionMode);

            RecordListBuilder recordListBuilder = new RecordListBuilder(compressionMode, dataProviderTestUtility.fetchSampleDataSchema());
            String dataSourceHash = "";
            RecordList recordList = recordListBuilder.createRecordList(dataSourceHash);
            assertNotNull(recordList);
            switch (compressionMode) {
                case NONE:
                    assertTrue(recordList instanceof RecordList);
                    break;
                case CANONICAL:
                    assertTrue(recordList instanceof RecordListCompressedImpl);
                    break;
                case CANONICAL_WITH_INVERTED_INDEX:
                    assertTrue(recordList instanceof RecordListCompressedWithInvertedIndexImpl);
                    break;
            }
            assertNotNull(recordList.toString());

            // test adding records
            recordListBuilder = new RecordListBuilder(RecordListBuilder.CompressionMode.CANONICAL_WITH_INVERTED_INDEX, dataProviderTestUtility.fetchSampleDataSchema());
            for (int i = 0; i < 7; i++) {
                recordListBuilder.addRecord("200"+i,"2b","3c","4d","5e","6f","7g", "180"+i, "8h");
            }
            recordList = recordListBuilder.createRecordList(dataSourceHash);
            assertNotNull(recordList);
            assertTrue(recordList instanceof RecordListCompressedWithInvertedIndexImpl);
            assertNotNull(recordList.toString());
        }

    }
}
