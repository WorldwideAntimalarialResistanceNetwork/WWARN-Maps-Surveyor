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

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.visualization.client.AbstractDataTable;

import java.util.Date;

/**
 * Created by nigelthomas on 29/05/2014.
 */
public class DataTableConversionUtility {
    public RecordList convertDataTableToRecordList(DataSchema schema, AbstractDataTable dataTable){
        return this.loadData(dataTable, schema);
    }

    /**
     * Given a schema, attempts to load table into internal structure
     * @param table the existing DataTable/DataView containing rows to load into RecordList
     */
    private RecordList loadData(AbstractDataTable table, DataSchema schema) {
        RecordListBuilder recordListBuilder = new RecordListBuilder(RecordListBuilder.CompressionMode.CANONICAL, schema);
        // simple check to ensure schema and table definitions are aligned
        if(schema.size()!= table.getNumberOfColumns()){
            final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("schema row definition do not match table supplied");
            GWT.log("Warning:", illegalArgumentException);
        }
        int columnLength = table.getNumberOfColumns();

        // for each row
        for (int rowIndex = 0; rowIndex < table.getNumberOfRows(); rowIndex++) {
            //for each column
            String[] fields = new String[columnLength];
            for (int columnIndex = 0; columnIndex < columnLength; columnIndex++) {
                if(table.getColumnType(columnIndex) == AbstractDataTable.ColumnType.DATE){
                    Date valueDate = table.getValueDate(rowIndex, columnIndex);
                    if(valueDate == null){ continue; }
                    String dateFormatted = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.ISO_8601).format(valueDate);
                    fields[columnIndex] = dateFormatted;
                }else{
                    fields[columnIndex] = table.getFormattedValue(rowIndex, columnIndex);
                }
            }
            recordListBuilder.addRecord((fields));
        }
        return recordListBuilder.createRecordList();
    }
}
