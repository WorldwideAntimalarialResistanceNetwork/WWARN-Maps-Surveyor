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

import com.google.gwt.user.client.rpc.IsSerializable;
import org.wwarn.surveyor.client.model.DatasourceConfig;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Holds a mapping between fields (aka column) and data types
 * and convenience functions to return the type for a field
 * effectively a tabular data structure, each field has a column index
 * User: nigel
 * Date: 19/07/13
 * Time: 11:42
 */
public class DataSchema implements IsSerializable, Serializable{
    // insertion order is preserved
    LinkedHashMap<String, DataSchemaRecord> fieldNameAndAssociatedType = new LinkedHashMap<String, DataSchemaRecord>();
    private int ordinal = 0;

    public DataSchema(DatasourceConfig datasourceConfig){
        Map<String,DatasourceConfig.SchemaConfig.FieldConfig> fields = datasourceConfig.getConfig().getFields();
        for (String key : fields.keySet()) {
            DatasourceConfig.SchemaConfig.FieldConfig fieldConfig = fields.get(key);
            this.addField(fieldConfig.getFieldName(), DataType.valueOf(fieldConfig.getFileType()));
        }
    }

    public DataSchema() {
    }

    public void addField(String fieldName, DataType fieldType){
        if(fieldNameAndAssociatedType.keySet().contains(fieldName)){throw new IllegalArgumentException("This key is already present :" + fieldName);}
        fieldNameAndAssociatedType.put(fieldName, new DataSchemaRecord(fieldType, ordinal++));
    }

    public DataType getType(String fieldName){
        final DataSchemaRecord dataSchemaRecord = fieldNameAndAssociatedType.get(fieldName);
        if(dataSchemaRecord ==null){return  null;}
        return dataSchemaRecord.getDataType();
    }

    public DataType getType(int columnIndex){
        final Set<String> keySet = fieldNameAndAssociatedType.keySet();
        final String[] fieldNames = keySet.toArray(new String[keySet.size()]);
        if(fieldNames[columnIndex] == null){return  null;}
        String fieldName = fieldNames[columnIndex];
        return fieldNameAndAssociatedType.get(fieldName).getDataType();
    }

    public int getColumnIndex(String fieldName){
        final DataSchemaRecord dataSchemaRecord = fieldNameAndAssociatedType.get(fieldName);
        if(dataSchemaRecord == null){
            return -1;
        }
        return dataSchemaRecord.getOrdinal();
    }

    public int size() {
        return this.fieldNameAndAssociatedType.size();
    }

    public boolean hasColumn(String key) {
        return this.fieldNameAndAssociatedType.containsKey(key);
    }

    public Set<String> getColumns() {
        return fieldNameAndAssociatedType.keySet();
    }

    static class DataSchemaRecord implements IsSerializable, Serializable {
        private DataType dataType;
        private int ordinal;

        public DataSchemaRecord(DataType dataType, int ordinal) {
            this.dataType = dataType;
            this.ordinal = ordinal;
        }

        public DataSchemaRecord() {
        }

        public DataType getDataType() {

            return dataType;
        }

        public int getOrdinal() {
            return ordinal;
        }
    }
}
