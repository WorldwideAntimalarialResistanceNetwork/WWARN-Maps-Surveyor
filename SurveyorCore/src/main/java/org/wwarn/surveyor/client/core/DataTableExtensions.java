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

import com.google.gwt.core.client.*;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.DataView;
import com.google.gwt.visualization.client.Properties;
import org.wwarn.mapcore.client.utils.StringUtils;

import java.util.*;

/**
 * This class holds extension to support some the missing GWT DataTable/DataView methods, ie getFilteredRows and getDistinctValues <br/>
 * All native JS calls encapsulated in this class, however holds reference to
 * See gwt docs:
 * http://gwt-google-apis.googlecode.com/svn/javadoc/visualization/1.1/index.html?com/google/gwt/visualization/client/package-summary.html
 * See javascript api docs for method description:
 * https://developers.google.com/chart/interactive/docs/reference#DataTable_getDistinctValues
 * https://developers.google.com/chart/interactive/docs/reference#DataTable_getFilteredRows
 */
public class DataTableExtensions {

    static class FilteredResults{
        RecordList recordList;
        AbstractDataTable dataTable;

        FilteredResults(RecordList recordList, AbstractDataTable dataTable) {
            this.recordList = recordList;
            this.dataTable = dataTable;
        }

        public RecordList getRecordList() {
            return recordList;
        }

        public void setRecordList(RecordList recordList) {
            this.recordList = recordList;
        }

        public AbstractDataTable getDataTable() {
            return dataTable;
        }

        public void setDataTable(AbstractDataTable dataTable) {
            this.dataTable = dataTable;
        }
    }

    private final DataTableConversionUtility dataTableConversionUtility = new DataTableConversionUtility();

    public FilteredResults filter(DataTable table, FilterQuery filterQuery, DataSchema schema) {
        DataView dataView = DataView.create(table);
        if (!(filterQuery instanceof MatchAllQuery) && filterQuery.getFilterQueries().size() > 0) {
            Set<Integer> rowToFilter = new HashSet<Integer>();
            rowToFilter.addAll(calculateRowsToFilter(dataView, filterQuery, schema));
            dataView.setRows(toPrimitiveArray(rowToFilter));
        }

        // covert a gwtDatatable to an internal RecordList
        return new FilteredResults(convertDataTable(dataView, schema), dataView);
    }

    public Set<String> getDistinctColumnValues(AbstractDataTable table, int columnIndex){
        //noinspection deprecation
        JsArrayMixed rawDistinctValues = DataTableExtensions.getDistinctValues(table, columnIndex);

        List<String> parsedDistinctValues = new ArrayList<>();
        for (int i = 0; i < rawDistinctValues.length(); i++) {
            final String s = rawDistinctValues.getString(i);
            if(StringUtils.isEmpty(s)) continue;
            parsedDistinctValues.add(s);
        }
        return addAll(new TreeSet<String>(), parsedDistinctValues.toArray(new String[parsedDistinctValues.size()]));
    }

    private Set<String> addAll(Set<String> distinctColumnValues, String[] parsedDistinctValues) {
        final ArrayList<String> collectionOfDistinctValues = new ArrayList<String>();
        Collections.addAll(collectionOfDistinctValues, parsedDistinctValues);
        distinctColumnValues.addAll(collectionOfDistinctValues);
        return distinctColumnValues;
    }

    /**
     * covert a gwtDatatable to an internal RecordList
     * @param table
     * @param schema
     * @return
     */
    private RecordList convertDataTable(AbstractDataTable table, DataSchema schema) {
        return dataTableConversionUtility.convertDataTableToRecordList(schema, table);
    }
    /**
     * Calculate the row indexes for rows that match all of the given filters.
     * Parses the filterQuery
     *
     * @param dataView
     * @param filterQuery
     * @param schema
     * @return a list of rows to filter
     */
    private List<Integer> calculateRowsToFilter(DataView dataView, FilterQuery filterQuery, DataSchema schema) {
        JsArray<JavaScriptObject> propertiesJsArray = convertToColumnIndexAndValueArray(filterQuery, schema);
        JsArrayInteger jsArrayInteger = getFilteredRows(dataView, propertiesJsArray);
        return toTypedObjectArray(jsArrayInteger);
    }

    private JsArray<JavaScriptObject> convertToColumnIndexAndValueArray(FilterQuery filterQuery, DataSchema schema) {
        JsArray<JavaScriptObject> jsArray = JsArray.createArray().cast();

        Map<String, FilterQuery.FilterQueryElement> filterQueries = filterQuery.getFilterQueries();
        for (String key : filterQueries.keySet()){
            DataType type = schema.getType(key);
            JavaScriptObject javaScriptObject;
            int columnIndex = schema.getColumnIndex(key);

            FilterQuery.FilterQueryElement filterQueryElement = filterQueries.get(key);

            switch (type){
                case Date:
                    if(filterQueryElement instanceof FilterQuery.FilterFieldRangeDate){
                        Date minValue = ((FilterQuery.FilterFieldRangeDate) filterQueryElement).getMinValue();
                        Date maxValue = ((FilterQuery.FilterFieldRangeDate) filterQueryElement).getMaxValue();
                        javaScriptObject = JSFilterRangeObject.create(columnIndex, parseDoubleFromDate(minValue), parseDoubleFromDate(maxValue));
                    } else{
                        javaScriptObject = JSFilterObject.create(columnIndex, parseDoubleFromDate(DataType.ParseUtil.parseDateYearOnly(((FilterQuery.FilterFieldValue) filterQueryElement).getFieldValue())));
                    }
                    break;
                case Integer:
                    // if range query
                    if(filterQueryElement instanceof FilterQuery.FilterFieldRange){
                        String minValue = ((FilterQuery.FilterFieldRange) filterQueryElement).getMinValue();
                        String maxValue = ((FilterQuery.FilterFieldRange) filterQueryElement).getMaxValue();
                        javaScriptObject = JSFilterRangeObject.create(columnIndex, Integer.parseInt(minValue), Integer.parseInt(maxValue));
                    }else if(filterQueryElement instanceof FilterQuery.FilterFieldGreaterThanInteger){
                        int minValue = ((FilterQuery.FilterFieldGreaterThanInteger) filterQueryElement).getFieldValue();
                        javaScriptObject = JSFilterRangeObject.create(columnIndex, minValue);
                    }else{
                        javaScriptObject = JSFilterObject.create(columnIndex, Integer.parseInt(((FilterQuery.FilterFieldValue) filterQueryElement).getFieldValue()));
                    }
                    break;
                case DateYear:
                    // if range query
                    if(filterQueryElement instanceof FilterQuery.FilterFieldRange){
                        String minValue = ((FilterQuery.FilterFieldRange) filterQueryElement).getMinValue();
                        String maxValue = ((FilterQuery.FilterFieldRange) filterQueryElement).getMaxValue();
                        javaScriptObject = JSFilterRangeObject.create(columnIndex, parseDoubleFromDate(DataType.ParseUtil.parseDateYearOnly(minValue)), parseDoubleFromDate(DataType.ParseUtil.parseDateYearOnly(maxValue)));

                    }else{
                        javaScriptObject = JSFilterObject.create(columnIndex, parseDoubleFromDate(DataType.ParseUtil.parseDateYearOnly(((FilterQuery.FilterFieldValue) filterQueryElement).getFieldValue())));
                    }
                    break;
                default:
                    // if range query
                    if(filterQueryElement instanceof FilterQuery.FilterFieldRange){
                        String minValue = ((FilterQuery.FilterFieldRange) filterQueryElement).getMinValue();
                        String maxValue = ((FilterQuery.FilterFieldRange) filterQueryElement).getMaxValue();
                        javaScriptObject = JSFilterRangeObject.create(columnIndex, minValue, maxValue);
                    }else{
                        javaScriptObject = JSFilterObject.create(columnIndex, ((FilterQuery.FilterFieldValue) filterQueryElement).getFieldValue());
                    }
                    break;
            }
            if(javaScriptObject!= null)
                jsArray.push(javaScriptObject);
        }

        return jsArray;
    }

    private double parseDoubleFromDate(Date minValue) {
        return Double.parseDouble(String.valueOf(minValue.getTime()));
    }


    protected List<Integer> toTypedObjectArray(JsArrayInteger integers){
        List<Integer> integerList = new ArrayList<Integer>();
        for (int i = 0; i < integers.length(); i++) {
            integerList.add(integers.get(i));
        }
        return integerList;
    }

    protected int[] toPrimitiveArray(Set<Integer> integers){
        int[] a = new int[integers.size()];
        int indx = 0;
        for (Integer integer : integers) {
            a[indx++] = integer;
        }
        return a;
    }

    /**
     * A overlay class to support calling filter rows
     * @param <T>
     */
    private static class JSFilterObject<T> extends JavaScriptObject {
        protected JSFilterObject() {
        }

        public final native int Column()/*-{
            return this.column;
        }-*/;

        public final native T Value()/*-{
            return this.value;
        }-*/;

        public static native JSFilterObject<String> create(int columnIndex, String value) /*-{
            return {column: columnIndex, value: value}
        }-*/;

        public static native JSFilterObject<Integer> create(int columnIndex, int value) /*-{
            return {column: columnIndex, value: value}
        }-*/;

        public static native JSFilterObject<Date> create(int columnIndex, double value) /*-{
            return {column: columnIndex, value: new $wnd.Date(value)}
        }-*/;
    }
    /**
     * A overlay class to support calling filter rows
     * @param <T>
     */
    private static class JSFilterRangeObject<T> extends JavaScriptObject {
        protected JSFilterRangeObject() {
        }

        public final native int Column()/*-{
            return this.column;
        }-*/;

        public final native T MinValue()/*-{
            return this.minValue;
        }-*/;

        public final native T MaxValue()/*-{
            return this.maxValue;
        }-*/;

        public static native JSFilterObject<String> create(int columnIndex, String minValue, String maxValue) /*-{
            return {column: columnIndex, minValue: minValue, maxValue: maxValue}
        }-*/;

        public static native JSFilterObject<Integer> create(int columnIndex, int minValue, int maxValue) /*-{
            return {column: columnIndex,  minValue: minValue, maxValue: maxValue}
        }-*/;

        /**
         * Filter only minimum values
         */
        public static native JSFilterObject<Integer> create(int columnIndex, int minValue) /*-{
            return {column: columnIndex,  minValue: minValue}
        }-*/;

        /**
         * Note that we cannot use java.util.Date as an input parameter, as this isn't supported, <br/>
         * instead a double is used and the value is parsed to a native javascript date, based on this <a href="https://code.google.com/p/gwt-chronoscope/source/browse/trunk/gviz/gviz-api/src/main/java/org/timepedia/chronoscope/gviz/api/client/DataTable.java">example</a>:<br/>
         * (in place of long, since gwt can't work covert long)
         * @param columnIndex
         * @param minValue
         * @param maxValue
         * @return
         */
        public static native JSFilterObject<Date> create(int columnIndex, double minValue, double maxValue) /*-{
            return {column: columnIndex,  minValue: new $wnd.Date(minValue), maxValue: new $wnd.Date(maxValue)}
        }-*/;
    }



    /**
     * Native call to implement filtered row, which is absent from the current GWTDataTable implementation
     * https://developers.google.com/chart/interactive/docs/reference#DataTable_getFilteredRows
     * @param data Takes a DataTable or DataView
     * @param properties Use overlay classes in this package to create this type
     * @return an integer values to filter a native JS type, returns the row indexes for rows that match all of the given filters.
     */
    private static native JsArrayInteger getFilteredRows(AbstractDataTable data,
                                                        JsArray<JavaScriptObject> properties) /*-{
        return data.getFilteredRows(properties);
    }-*/;


    /**
     * A wrapper to call getDistinctValues
     * https://developers.google.com/chart/interactive/docs/reference#DataTable_getDistinctValues
     * @param data
     * @param columnIndex
     * @return
     */
    private static native JsArrayMixed getDistinctValues(AbstractDataTable data, int columnIndex) /*-{
        return data.getDistinctValues(columnIndex);
    }-*/;

}
