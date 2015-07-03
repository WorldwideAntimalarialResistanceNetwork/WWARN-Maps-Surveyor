package org.wwarn.surveyor.client.model;

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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: nigel
 * Date: 31/07/13
 */
public class DatasourceConfig implements Config {
    private final DataSourceProvider dataSourceProvider;
    private final String filename;
    private final SchemaConfig config = new SchemaConfig();
    private final String uniqueId;

    public DataSourceProvider getDataSourceProvider() {
        return dataSourceProvider;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public static class SchemaConfig {
        Map<String, FieldConfig> fields = new LinkedHashMap<String, FieldConfig>();
        public class FieldConfig {
            String fieldName;
            String fileType;

            public String getFieldName() {
                return fieldName;
            }

            public String getFileType() {
                return fileType;
            }

            FieldConfig(String fieldName, String fileType) {
                this.fieldName = fieldName;
                this.fileType = fileType;
            }
        }

        public void add(String fieldName, String fieldType)
        {
           fields.put(fieldName, new FieldConfig(fieldName, fieldType));
        }

        public Map<String, FieldConfig> getFields() {
            return this.fields;
        }
    }

    public DatasourceConfig(String uniqueId, String filename, String dataSourceType) {
        this.uniqueId = uniqueId;
        this.filename = filename;
        this.dataSourceProvider = DataSourceProvider.valueOf(dataSourceType);
    }

    public String getFilename() {
        return filename;
    }

    public SchemaConfig getConfig() {
        return config;
    }

}
