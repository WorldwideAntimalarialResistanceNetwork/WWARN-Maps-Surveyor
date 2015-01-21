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
import org.wwarn.mapcore.client.utils.StringUtils;

import java.util.*;

/**
 * A filter query is a set of of fields with values to restrict the results by <br/>
 * FilterQuery := None | (Field, FieldValueToFilter) <br/>
 * User: nigel
 * Date: 19/07/13
 */
public class FilterQuery implements IsSerializable{
    Map<String, FilterQueryElement> filterQueries;

    boolean fetchAllDistinctFieldValues = true;

    public boolean buildInvertedIndex() {
        return buildInvertedIndex;
    }

    public void setBuildInvertedIndex(boolean buildInvertedIndex) {
        this.buildInvertedIndex = buildInvertedIndex;
    }

    boolean buildInvertedIndex = false;

    Set<String> fields;

    public FilterQuery() {
        this.filterQueries = new HashMap<String, FilterQueryElement>();
    }

    /**
     * Should only be called by DataProvider to filter records..
     * @return
     */
    public Map<String, FilterQueryElement> getFilterQueries() {
        return filterQueries;
    }

    /**
     * Sets if all distinct filter field values should be returned, default is true,
     * determines the type of facet filter behaviour, if set to false, then one could use
     * this to create linked facets where changing one value, reduces options in another
     * @param fetchAllDistinctFieldValues
     */
    public void setFetchAllDistinctFieldValues(boolean fetchAllDistinctFieldValues) {
        this.fetchAllDistinctFieldValues = fetchAllDistinctFieldValues;
    }

    /**
     * add a filter to the to build FilterQuery instance
     * @param field
     * @param valueToFilter
     */
    public void addFilter(String field, String valueToFilter){
        if(StringUtils.isEmpty(field) || StringUtils.isEmpty(valueToFilter)){
            throw new IllegalArgumentException("Expected all attributes to be non empty");
        }
        Set<String> valuesToFilter = new HashSet<String>();
        valuesToFilter.add(valueToFilter);
        filterQueries.put(field, new FilterFieldValue(field, valuesToFilter));
    }

    /**
     * add a filter with multiple values to build FilterQuery instance
     * @param field
     * @param valueToFilter
     */
    public void addMultipleValuesFilter(String field, Set<String> valueToFilter){
        if(!valueToFilter.isEmpty()){
            filterQueries.put(field, new FilterFieldValue(field, valueToFilter));
        }

    }

    /**
     * Alternative to filter, which accepts a range of items to filter.
     * For instance, the field might be a year, and minValue 2000 and maxValue 2009
     * this filter will return records between the supplied ranges (inclusive)
     * @param field
     * @param minValue
     * @param maxValue
     */
    public <T> void addRangeFilter(String field, T minValue, T maxValue){
        if(minValue instanceof String){
            if(StringUtils.isEmpty(field) || StringUtils.isEmpty((String) minValue) || StringUtils.isEmpty((String) maxValue)){
                throw new IllegalArgumentException("Expected all attributes to be non empty");
            }
            filterQueries.put(field, new FilterFieldRange(field, (String)minValue, (String)maxValue));
        }else if (minValue instanceof Date){
            filterQueries.put(field, new FilterFieldRangeDate(field, (Date)minValue, (Date)maxValue));
        }
    }

    public void addFilterGreater(String field, int valueToFilter){
        if(StringUtils.isEmpty(field) ){
            throw new IllegalArgumentException("Expected all attributes to be non empty");
        }
        filterQueries.put(field, new FilterFieldGreaterThanInteger(field, valueToFilter));
    }

    public static interface FilterQueryElement extends IsSerializable{
        public String getFilterField();
    }

    public Set<String> getFields() {
        return fields;
    }

    public void setFields(Set<String> fields) {
        this.fields = fields;
    }

    public static class FilterFieldValue implements FilterQueryElement, IsSerializable{
        private String field;
        Set<String> fieldValues;

        FilterFieldValue() {
        }

        FilterFieldValue(String field, Set<String> fieldValues) {
            this.field = field;
            this.fieldValues = fieldValues;
        }

        public Set<String>  getFieldsValue() {
            return fieldValues;
        }

        //Get first element; Multiple Selection is not yet supported in the view side Implementation
        public String  getFieldValue() {
            if(!fieldValues.isEmpty()){
                return fieldValues.iterator().next();
            }
            return null;
        }

        @Override
        public String getFilterField() {
            return field;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FilterFieldValue)) return false;

            FilterFieldValue that = (FilterFieldValue) o;

            if (field != null ? !field.equals(that.field) : that.field != null) return false;
            if (fieldValues != null ? !fieldValues.equals(that.fieldValues) : that.fieldValues != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = field != null ? field.hashCode() : 0;
            result = 31 * result + (fieldValues != null ? fieldValues.hashCode() : 0);
            return result;
        }
    }

    public static class FilterFieldRange implements FilterQueryElement, IsSerializable{
        FilterFieldRange() {
        }

        private String field, minValue, maxValue;

        FilterFieldRange(String field, String minValue, String maxValue) {
            if(StringUtils.isEmpty(field, minValue, maxValue)) {
                throw new IllegalArgumentException("All parameters must be set.");
            }
            this.field = field;
            this.minValue = minValue;
            this.maxValue = maxValue;

        }

        public String getMinValue() {
            return minValue;
        }

        public String getMaxValue() {
            return maxValue;
        }

        @Override
        public String getFilterField() {
            return field;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FilterFieldRange)) return false;

            FilterFieldRange that = (FilterFieldRange) o;

            if (field != null ? !field.equals(that.field) : that.field != null) return false;
            if (maxValue != null ? !maxValue.equals(that.maxValue) : that.maxValue != null) return false;
            if (minValue != null ? !minValue.equals(that.minValue) : that.minValue != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = field != null ? field.hashCode() : 0;
            result = 31 * result + (minValue != null ? minValue.hashCode() : 0);
            result = 31 * result + (maxValue != null ? maxValue.hashCode() : 0);
            return result;
        }
    }

    public static class FilterFieldRangeDate implements FilterQueryElement, IsSerializable{
        FilterFieldRangeDate() {
        }

        private String field;
        private Date minValue, maxValue;

        FilterFieldRangeDate(String field, Date minValue, Date maxValue) {
            if(StringUtils.isEmpty(field)) {
                throw new IllegalArgumentException("All parameters must be set.");
            }
            this.field = field;
            this.minValue = minValue;
            this.maxValue = maxValue;

        }

        public Date getMinValue() {
            return minValue;
        }

        public Date getMaxValue() {
            return maxValue;
        }

        @Override
        public String getFilterField() {
            return field;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FilterFieldRangeDate)) return false;

            FilterFieldRangeDate that = (FilterFieldRangeDate) o;

            if (field != null ? !field.equals(that.field) : that.field != null) return false;
            if (maxValue != null ? !maxValue.equals(that.maxValue) : that.maxValue != null) return false;
            if (minValue != null ? !minValue.equals(that.minValue) : that.minValue != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = field != null ? field.hashCode() : 0;
            result = 31 * result + (minValue != null ? minValue.hashCode() : 0);
            result = 31 * result + (maxValue != null ? maxValue.hashCode() : 0);
            return result;
        }
    }

    public static class FilterFieldGreaterThanInteger implements FilterQueryElement, IsSerializable{
        private String field;
        private int fieldValue;

        FilterFieldGreaterThanInteger() {
        }

        FilterFieldGreaterThanInteger(String field, int fieldValue) {
            this.field = field;
            this.fieldValue = fieldValue;
        }

        public int getFieldValue() {
            return fieldValue;
        }

        @Override
        public String getFilterField() {
            return field;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FilterFieldGreaterThanInteger)) return false;

            FilterFieldGreaterThanInteger that = (FilterFieldGreaterThanInteger) o;

            if (fieldValue != that.fieldValue) return false;
            if (field != null ? !field.equals(that.field) : that.field != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = field != null ? field.hashCode() : 0;
            result = 31 * result + fieldValue;
            return result;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilterQuery)) return false;

        FilterQuery that = (FilterQuery) o;

        if (filterQueries != null ? !filterQueries.equals(that.filterQueries) : that.filterQueries != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return filterQueries != null ? filterQueries.hashCode() : 0;
    }
}
