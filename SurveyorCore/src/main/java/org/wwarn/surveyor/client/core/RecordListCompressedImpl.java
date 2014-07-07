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
import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.*;

/**
 * Created by nigelthomas on 02/07/2014.
 */
public class RecordListCompressedImpl extends RecordList {
    // List each unique value in a single list
    // eg. 1.342, 1.212, Africa, Some Journal Title
    //String[] heap;

    private transient Map<String, Integer> inverseHeap = new LinkedHashMap<>() /*Only used in initialisation phase, kep null afterwards*/;
    private List<String> heap = new ArrayList<>();
    private int heapIndex = -1;

    public RecordListCompressedImpl(DataSchema schema) {
        super(schema);
    }

    public RecordListCompressedImpl() {
        super();
    }

    @Override
    public List<Record> getRecords() {
        return super.getRecords();
    }

    @Override
    public RecordList getUniqueRecordsBy(String fieldName) {
        Set<String> uniqueKeys = new HashSet<String>();
        final RecordListBuilder uniqueRecordListBuilder = new RecordListBuilder(RecordListBuilder.CompressionMode.CANONICAL, this.schema);
        for (int rowIndex = 0; rowIndex < this.size(); rowIndex++) {
            final Record currentRecord = this.records.get(rowIndex);
            String uniqueKeyField = currentRecord.getValueByFieldName(fieldName);
            if(!uniqueKeys.contains(uniqueKeyField)){
                uniqueRecordListBuilder.add(currentRecord);
            }
            uniqueKeys.add(uniqueKeyField);
        }
        return uniqueRecordListBuilder.createRecordList();
    }

    @Override
    public void add(Record record) {
        this.addRecord(record.getFields());
    }

    @Override
    public void addRecord(String... fields) {
        Record record = new RecordCompressedImpl(fields, schema, this);
        super.add(record);
    }

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    private int getIndexOf(String field){
        final Integer index = inverseHeap.get(field);
        if(index == null){
            return -1;
        }
        return index;
    }

    private int getAndSetIndex(String field){
        final int indexOf = getIndexOf(field);
        if(indexOf < 0){
            inverseHeap.put(field, ++heapIndex);
            return heapIndex;
        }else {
            return indexOf;
        }
    }

    private String getFieldValueFromHeap(int index){
        return heap.get(index);
    }

    public void initialise() {
        // initialise heap from inverse heap, null inverse heap
        heap  = new ArrayList<>(inverseHeap.keySet());
        inverseHeap = null;
    }

    public static class RecordCompressedImpl extends Record {
        private RecordListCompressedImpl recordListCompressed;
        //array of fields
        // map of column names to fields
        DataSchema schema = new DataSchema();
        int[] fieldIndex;

        public RecordCompressedImpl() {
        }

        public RecordCompressedImpl(String[] fieldValues, DataSchema schema, RecordListCompressedImpl recordListCompressed) {
            this.schema = schema;
            if(fieldValues.length != schema.size()){
                throw new IllegalArgumentException("Schema length should be same as field length");
            }
            if(recordListCompressed == null){
                throw new IllegalArgumentException("RecordListCompressed cannot be null");
            }
            this.recordListCompressed = recordListCompressed;
            this.fieldIndex = new int[schema.size()];
            normaliseFields(fieldValues, schema);
            for (int i = 0; i < fieldValues.length; i++) {
                String field = fieldValues[i];
                final int index = recordListCompressed.getAndSetIndex(field);
                fieldIndex[i] = index;
            };
        }

        @Override
        public String getValueByFieldIndex(int index) {
            validateIndex(index, "Index out of bounds");
            final int indexOfFieldValueInHeap = fieldIndex[index];
            return recordListCompressed.getFieldValueFromHeap(indexOfFieldValueInHeap);
        }

        @Override
        public String getValueByFieldName(String fieldName) {
            int index = schema.getColumnIndex(fieldName);
            validateIndex(index, "Index out of bounds for fieldName : " + fieldName);
            return getValueByFieldIndex(index);
        }

        @Override
        protected void validateIndex(int index, String message) {
            if(index > fieldIndex.length-1 || index < 0){
                IndexOutOfBoundsException e = new IndexOutOfBoundsException(message);
                GWT.log("", e);
                throw e;
            }
        }

        @Override
        public String toString() {
            return "Record{" +
                    "fields=" + Arrays.toString(getFields()) +
                    '}';
        }

        public String[] getFields() {
            final ArrayList<String> fields = new ArrayList<String>();
            for (int index : fieldIndex) {
                fields.add(recordListCompressed.getFieldValueFromHeap(index));
            }
            return fields.toArray(new String[fields.size()]);
        }
    }
}
