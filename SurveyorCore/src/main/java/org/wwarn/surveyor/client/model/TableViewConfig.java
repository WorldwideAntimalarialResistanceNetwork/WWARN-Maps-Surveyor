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

import com.google.gwt.user.client.rpc.IsSerializable;
import org.wwarn.surveyor.client.model.ViewConfig;
import org.wwarn.mapcore.client.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds table model
 * User: nigel
 * Date: 15/08/13
 * Time: 15:29
 */
public class TableViewConfig implements ViewConfig, IsSerializable {

    public final static String DESCENDENT_ORDER = "desc";
    public final static String ASCENDENT_ORDER = "asc";

    String viewName = "";
    private List<TableColumn> columns = new ArrayList<TableColumn>();
    private String sortColumn;
    private String sortOrder;
    private String label;
    private int pageSize;
    private String filterBy;



    private TableType type;

    private TableViewConfig(){};

    public TableViewConfig(String viewName, String viewLabel) {
        this.viewName = viewName;
        this.label = viewLabel;
    }

    @Override
    public String getViewName() {
        return viewName;
    }

    @Override
    public String getViewLabel() {
        return label;
    }

    public List<TableColumn> getColumns() {
        return (columns);
    }

    public void add(TableColumn tableColumn){
        columns.add(tableColumn);
    }

    public boolean containsColumnByFieldName(String fieldName){
        return this.columns.contains(new TableColumn(fieldName, "" ,"", ""));
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getFilterBy() {
        return filterBy;
    }

    public void setFilterBy(String filterBy) {
        this.filterBy = filterBy;
    }

    public TableType getType() {
        return type;
    }

    public void setType(TableType type) {
        this.type = type;
    }

    public static class TableColumn implements IsSerializable {
        private String fieldName, fieldTitle, hyperLinkField, dateFormat;

        public TableColumn(String fieldName, String fieldTitle, String hyperLinkField, String dateFormat) {
            if(StringUtils.isEmpty(fieldName)){
                throw new IllegalArgumentException("filterTitle may not be null");
            }
            this.fieldName = fieldName;
            this.fieldTitle = fieldTitle;
            this.hyperLinkField = hyperLinkField;
            this.dateFormat = dateFormat;
        }

        public TableColumn() {
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getFieldTitle() {
            return fieldTitle;
        }

        public String getHyperLinkField() {
            return hyperLinkField;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TableColumn that = (TableColumn) o;

            return fieldName.equals(that.fieldName);

        }

        @Override
        public int hashCode() {
            return fieldName.hashCode();
        }

        public String getDateFormat() {
            return dateFormat;
        }

        @Override
        public String toString() {
            return "TableColumn{" +
                    "fieldName='" + fieldName + '\'' +
                    ", fieldTitle='" + fieldTitle + '\'' +
                    ", hyperLinkField='" + hyperLinkField + '\'' +
                    ", dateFormat='" + dateFormat + '\'' +
                    '}';
        }
    }

    public enum TableType implements IsSerializable{
        //it will create a TableViewComposite
        CLIENT_TABLE,

        //It will create a CellTableServer
        SERVER_TABLE
    }

    public boolean isDescendentOrder(){
        if (DESCENDENT_ORDER.equals(sortOrder)){
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "TableViewConfig{" +
                "viewName='" + viewName + '\'' +
                ", columns=" + columns +
                ", sortColumn='" + sortColumn + '\'' +
                ", sortOrder='" + sortOrder + '\'' +
                ", label='" + label + '\'' +
                ", pageSize=" + pageSize +
                ", filterBy='" + filterBy + '\'' +
                ", type=" + type +
                '}';
    }
}
