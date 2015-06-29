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

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import org.wwarn.mapcore.client.utils.StringUtils;

import java.util.List;

/**
* Created by nigelthomas on 05/07/2014.
*/
public class RecordListBuilder {

    private final CompressionMode compressionMode;
    private final RecordList recordList;

    public void add(RecordList.Record currentRecord) {
        recordList.add(currentRecord);
    }

    public enum CompressionMode{
        NONE, CANONICAL, CANONICAL_WITH_INVERTED_INDEX
    }

    public RecordListBuilder(DataSchema dataSchema) {
        this(CompressionMode.NONE, dataSchema);
    }

    public RecordListBuilder(CompressionMode compressionMode, DataSchema schema) {
        this.compressionMode = compressionMode;
        switch (compressionMode){
            case NONE:
                this.recordList = new RecordList(schema);
                break;
            default:
            case CANONICAL:
                this.recordList = new RecordListCompressedImpl(schema);
                break;
            case CANONICAL_WITH_INVERTED_INDEX:
                this.recordList = new RecordListCompressedWithInvertedIndexImpl(schema);
                break;
        }
    }

    public RecordListBuilder addRecord(String... fields){
        recordList.addRecord(fields);
        return this;
    }

    public RecordListBuilder addAllRecords(List<RecordList.Record> records){
        recordList.addAll(records);
        return this;
    }

    public RecordList createRecordList(String dataSourceHash) {
        switch (compressionMode) {
            case NONE:
                Log.error("Using RecordList uncompressed");
                break;
            case CANONICAL:
            case CANONICAL_WITH_INVERTED_INDEX:
            default:
                ((RecordListCompressedImpl) this.recordList).initialise();
                break;
        }
        dataSourceHash = StringUtils.ifEmpty(dataSourceHash, "");
        recordList.setDataSourceHash(dataSourceHash);
        return recordList;
    }
}
