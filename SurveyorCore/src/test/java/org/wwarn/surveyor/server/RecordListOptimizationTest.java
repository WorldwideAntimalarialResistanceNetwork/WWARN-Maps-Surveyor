package org.wwarn.surveyor.server;

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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Test;
import org.wwarn.surveyor.client.core.*;
import org.wwarn.surveyor.client.model.DatasourceConfig;
import org.wwarn.surveyor.server.core.CompressRecordListService;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertTrue;

/**
 * Created by suay on 6/4/14.
 */
public class RecordListOptimizationTest {

    DataSchema schema;

    RecordList recordList;

    @Test
    public void testTime() throws IOException {
        setup();

        long startTime = System.nanoTime();
        CompressRecordListService compressRecordListService = CompressRecordListService.getOurInstance();
        compressRecordListService.compressRecordList(schema, recordList);
        long stopTime = System.nanoTime();
        long elapsedTime = stopTime - startTime;
        int miliseconds = (int) elapsedTime / 1000;

        System.out.println("Compression took " + elapsedTime + " nano seconds. Around " + miliseconds + " ");

    }


    private void setup(){
        String dataSourceType = DatasourceConfig.DataSourceType.LocalClientSideDataProvider.name();
        schema = new DataSchema(new DatasourceConfig("", dataSourceType));

        List<String> jsonFields = Arrays.asList("MGroup", "Present", "Author", "StudyID", "SiteName",
                "Lat", "RN", "PubMedURL", "Country", "Title",
                "Notes", "MarkerType", "Tested", "Drug", "PubMedID",
                "PubYear", "StudyTo", "rIdx", "Lon", "StudyFrom");

        for (String jsonField : jsonFields){
            schema.addField(jsonField, DataType.String);
        }

        RecordListBuilder recordListBuilder = new RecordListBuilder(RecordListBuilder.CompressionMode.NONE, schema);

        InputStream is =  getClass().getResourceAsStream("/org/wwarn/surveyor/public/data/molecular.publications.json");
        JSONArray arrayObjects= parseJSONfile(is);

        String[] fields;
        for(JSONObject obj : (List<JSONObject>) arrayObjects){
            fields = new String[jsonFields.size()];
            for (int i = 0; i < jsonFields.size(); i++){

                String type = obj.get(jsonFields.get(i)).getClass().getName();
                switch (type) {
                    case "java.lang.String":
                        fields[i] = (String)obj.get(jsonFields.get(i));
                        break;
                    case "java.lang.Long":
                        fields[i] = Long.toString((long)obj.get(jsonFields.get(i)));
                        break;
                    case "java.lang.Double":
                        fields[i] = Double.toString((double)obj.get(jsonFields.get(i)));
                        break;
                    default:
                        fields[i] = "x";
                        break;
                }

            }
            recordListBuilder.addRecord(fields);
        }
        this.recordList = recordListBuilder.createRecordList();
    }


    public static JSONArray parseJSONfile(InputStream is){

        Reader reader = new InputStreamReader(is);
        Object fileObjects= JSONValue.parse(reader);
        JSONArray arrayObjects=(JSONArray)fileObjects;
        return arrayObjects;

    }
}
