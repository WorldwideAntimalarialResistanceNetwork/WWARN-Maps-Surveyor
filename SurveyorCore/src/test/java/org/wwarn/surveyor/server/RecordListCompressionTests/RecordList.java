package org.wwarn.surveyor.server.RecordListCompressionTests;

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



import org.wwarn.surveyor.client.core.DefaultLocalJSONDataProvider;

import java.io.Serializable;
import java.util.*;

/**
 * Record list holds a list of records, which in turn hold a list of fields
 * Effectively a table data structure
 * Constructor takes a gwt datatable
 * User: nigel
 * Date: 19/07/13
 * Time: 11:33
 */
public class RecordList implements Serializable{
    private DataSchema schema;
    private List<Record> records = new ArrayList<Record>();

    public RecordList(DataSchema schema) {
        this.schema = schema;
    }

    public RecordList() {
    }

    public List<Record> getRecords() {
        return Collections.unmodifiableList(records);
    }

    /**
     * Picks the first available record by given fieldName
     * @param fieldName the unique field value
     * @return
     */
    public RecordList getUniqueRecordsBy(String fieldName) {
        Set<String> uniqueKeys = new HashSet<String>();
        final RecordList uniqueRecordList = new RecordList(this.schema);
        for (int rowIndex = 0; rowIndex < this.size(); rowIndex++) {
            final Record currentRecord = this.records.get(rowIndex);
            String uniqueKeyField = currentRecord.getValueByFieldName(fieldName);
            if(!uniqueKeys.contains(uniqueKeyField)){
                uniqueRecordList.add(currentRecord);
            }
            uniqueKeys.add(uniqueKeyField);
        }
        return uniqueRecordList;
    }

    public void add(Record record) {
        this.records.add(record);
    }

    public void addRecord(String... fields){
        Record record = new Record(fields, schema);
        this.add(record);
    }

    public int size() {
        return records.size();
    }


    @Override
    public String toString() {
        return "RecordList{" +
                "records=" + records +
                '}';
    }

    public static class Record extends org.wwarn.surveyor.client.core.RecordList.Record implements Serializable{
        //array of fields
        // map of column names to fields
        String[] fields;
        DataSchema schema = new DataSchema();

        public Record() {
            super();
            this.fields = new String[schema.size()];
        }
        public Record(String[] fields, DataSchema schema) {
            this.schema = schema;
            if(fields.length != schema.size()){
                throw new IllegalArgumentException("Schema length should be same as field length");
            }
            this.fields = fields;
        }

        /**
         * Returns a record by field name
         * @param fieldName
         * @return
         */
        public String getValueByFieldName(String fieldName){
            int index = schema.getColumnIndex(fieldName);
            if(index > fields.length-1 || index < 0){
                IndexOutOfBoundsException e = new IndexOutOfBoundsException("Index out of bounds for fieldName : "+ fieldName);
                throw e;
            }
            return fields[index];
        }

        @Override
        public String toString() {
            return "Record{" +
                    "fields=" + Arrays.toString(fields) +
                    '}';
        }
    }
}
