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

import org.wwarn.surveyor.client.core.DataSchema;
import org.wwarn.surveyor.client.core.RecordList;
import org.wwarn.surveyor.client.core.RecordListCompressed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by suay on 6/5/14.
 */
public class CompressRecordListService {

    DataSchema schema;

    // List each unique value in a single list
    // eg. 1.342, 1.212, Africa, Some Journal Title
    //String[] headers;
    List<String> headers;

    //List of records, each record will contain a "pointer" to the header
    List<int[]> records;

    private final static int NUM_CORES = Runtime.getRuntime().availableProcessors();


    private static CompressRecordListService ourInstance = new CompressRecordListService();

    public static CompressRecordListService getOurInstance() {
        return ourInstance;
    }

    public RecordListCompressed compressRecordList(DataSchema schema, RecordList recordList){
        this.schema = schema;
        calculateHeaders(recordList);
        calculateRowsMultiThreading(recordList);
        return new RecordListCompressed(schema, headers, records);
    }


    private void calculateHeaders(RecordList recordList){
         headers = new ArrayList<>();
        Set<String> tmpHeaders = new HashSet<String>();
        for(RecordList.Record record : recordList.getRecords()){
            for(String recordValue : record.getFields())
                tmpHeaders.add(recordValue);
        }
        headers.addAll(tmpHeaders);
    }

    private void calculateRowsMultiThreading(RecordList recordList){
        records = new CopyOnWriteArrayList<int[]>();
        try {
            int numThreads = NUM_CORES + 1;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            List<RecordList.Record> records = recordList.getRecords();

            int numRecords = records.size();
            int step = numRecords / numThreads;
            int end = step;
            for (int i = 0; i < numRecords-step ; i = i + step){
                end = i + step;
                Runnable task = new CalculateRowsThread(records.subList(i, end));
                executor.execute(task);
            }
            Runnable task = new CalculateRowsThread(records.subList(end, numRecords));
            executor.execute(task);

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void calculateRows(List<RecordList.Record> recordList){

        int numColumns = schema.getColumns().size();
        for(RecordList.Record record : recordList){
            int[] row = new int[numColumns];
            for (int i = 0; i < record.getFields().length; i++){
                String fields[] = record.getFields();
                row[i] = getIndexInHeader(fields[i]);
            }
            addRecord(row);
        }
    }

    public void addRecord(int[] record){
        records.add(record);
    }

    private int getIndexInHeader(String value){
        for (int i = 0 ; i < headers.size(); i++){
            String headerValue = headers.get(i);
            if(value.equals(headerValue)){
                return i;
            }
        }
        return -1;
    }


    public class CalculateRowsThread implements Runnable {
        List<RecordList.Record> recordSubList;

        CalculateRowsThread(List<RecordList.Record> recordSubList) {
            this.recordSubList = recordSubList;
        }

        @Override
        public void run() {
            calculateRows(recordSubList);
        }
    }


}
