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

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.wwarn.surveyor.client.model.DataSourceProvider;
import org.wwarn.surveyor.client.util.AsyncCallbackWithTimeout;

import java.util.*;

/**
 * A client search implementation using BitSets
 * When I wrote this, only God and I understood what I was doing. Now, God only knows. - Karl Weierstrass
 */
public class ClientSideSearchDataProvider extends ServerSideSearchDataProvider implements DataProvider{
    private List<Map<String, BitSet>> fieldInvertedIndex;
    private static final String ISO8601_PATTERN = DataType.ISO_DATE_FORMAT;
    private RecordListCompressedWithInvertedIndexImpl recordListCompressedWithInvertedIndex;

    public ClientSideSearchDataProvider(GenericDataSource dataSource, DataSchema dataSchema, String[] fieldList) {
        super(dataSource, dataSchema, fieldList);
        if(dataSource.getDataSourceProvider()!= DataSourceProvider.ClientSideSearchDataProvider){
            throw new IllegalArgumentException("Expected data source provider client side data provider");
        }
    }

    @Override
    public void onLoad(final Runnable callOnLoad) {
        try {
            //todo something with initialFilterQuery
            InitialFilterQuery initialFilterQuery = getInitialFilterQuery();
            final FilterQuery filterQuery = new MatchAllQuery(); // fetch everything
            clientFactory.setLastFilterQuery(filterQuery);


            searchServiceAsync.preFetchData(schema, this.dataSource, this.facetFieldList, filterQuery, new AsyncCallbackWithTimeout<QueryResult>() {
                @Override
                public void onTimeOutOrOtherFailure(Throwable throwable) {
                    throw new IllegalStateException(throwable);
                }

                @Override
                public void onNonTimedOutSuccess(QueryResult queryResult) {
                    clientFactory.setLastQueryResult(queryResult);
                    final RecordList recordList = queryResult.getRecordList();
                    if(!(recordList instanceof RecordListCompressedWithInvertedIndexImpl)){ throw new IllegalArgumentException("Expected compressed index with inverted list");}
                    recordListCompressedWithInvertedIndex = (RecordListCompressedWithInvertedIndexImpl) recordList;

                    fieldInvertedIndex = setupIndex(recordListCompressedWithInvertedIndex);
                    callOnLoad.run();
                }
            });
        } catch (SearchException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<Map<String, BitSet>> setupIndex(RecordListCompressedWithInvertedIndexImpl recordListCompressedWithInvertedIndex) {
        final FieldInvertedIndex index = recordListCompressedWithInvertedIndex.getIndex();
        //todo, as this is expensive, instead of creating a new data structure, must reuse existing, replace Set<Integer> with BitSet in place
        // setup initial BitSet
        final List<Map<String, Set<Integer>>> fields = index.getFields();
        final List<Map<String, BitSet>> fieldsBitSet = new ArrayList<>();
        int fieldIndex = 0;
        //for each field
        for (Map<String, Set<Integer>> field : fields) {
            final DataType type = schema.getType(fieldIndex);
            //for each field value
            Map<String, BitSet> fieldValueBitSetMap = new TreeMap<>();
            for (String fieldValue : field.keySet()) {
                //get postings list
                BitSet bitSet = fieldValueBitSetMap.get(fieldValue);
                if (bitSet == null) {
                    bitSet = new BitSet();
                }
                final Set<Integer> positions = field.get(fieldValue);
                for (Integer pos : positions) {
                    bitSet.set(pos);
                }
                fieldValue = typeBasedStringFormatting(type, fieldValue);
                fieldValueBitSetMap.put(fieldValue, bitSet);
            }
            fieldsBitSet.add(fieldValueBitSetMap);
            fieldIndex++;
        }
        return fieldsBitSet;
    }

    private String typeBasedStringFormatting(DataType type, String fieldValue) {
        if(type == DataType.Integer || type == DataType.DateYear){
            /* added to handle natural numbers properly*/
            fieldValue = leftPaddedInteger(Integer.parseInt(fieldValue));
        }
        return fieldValue;
    }

    @Override
    public void query(FilterQuery filterQuery, String[] facetFields, AsyncCallbackWithTimeout<QueryResult> queryResultCallBack) throws SearchException {
        Objects.requireNonNull(fieldInvertedIndex, "Search not ready : onLoad method must be called first");
        queryIndex(filterQuery, facetFields, queryResultCallBack);
    }

    @Override
    public void query(FilterQuery filterQuery, AsyncCallbackWithTimeout<QueryResult> queryResultCallBack) throws SearchException {
        query(filterQuery, new String[]{}, queryResultCallBack);
    }

    private void queryIndex(FilterQuery filterQuery, String[] facetFields, AsyncCallbackWithTimeout<QueryResult> queryResultCallBack) {
        if(filterQuery instanceof MatchAllQuery){
            queryResultCallBack.onNonTimedOutSuccess(new QueryResult(recordListCompressedWithInvertedIndex, new FacetList()));
            return;
        }
        final BitSet bitSet = parseQuery(filterQuery, schema);
        RecordList recordList = restrictRecordList(bitSet);
        queryResultCallBack.onNonTimedOutSuccess(new QueryResult(recordList, new FacetList()));
    }

    private RecordList restrictRecordList(BitSet bitSet) {
        // filter recordListCompressedWith
        return new RecordListView(recordListCompressedWithInvertedIndex, bitSet);
    }

    private BitSet parseQuery(FilterQuery filterQuery, DataSchema schema) {
        BitSet queryBitSet = new BitSet();

        if (!(filterQuery instanceof MatchAllQuery) && filterQuery.getFilterQueries().size() > 0) {
            //facet drill down happens here
            for (String filterField : filterQuery.getFilterQueries().keySet()) {

                final int columnIndex = schema.getColumnIndex(filterField);
                final Map<String, BitSet> map = fieldInvertedIndex.get(columnIndex);

                final FilterQuery.FilterQueryElement filterQueryElement = filterQuery.getFilterQueries().get(filterField);
                DataType type = schema.getType(columnIndex);
                switch (type){
                    case CoordinateLat:
                    case CoordinateLon:
                    case String:
                    case Boolean:
                    case Integer:
                        // if range query
                        if(filterQueryElement instanceof FilterQuery.FilterFieldRange){

                            queryBitSet = processRangeQueryDefaultTypes(queryBitSet, map, filterQueryElement, type);

                        }else if(filterQueryElement instanceof FilterQuery.FilterFieldGreaterThanInteger){
                            queryBitSet = processIntegerGreaterThanDefaultTypes(queryBitSet, map, filterQueryElement, type);
                        }else{
                            // does most of the commons multi value types
                            queryBitSet = processMultiValueQueryDefaultTypes(queryBitSet, map, filterQueryElement, type);
                        }
                        break;
                    case Date:

                        if(filterQueryElement instanceof FilterQuery.FilterFieldRangeDate){
                            Date minValue = ((FilterQuery.FilterFieldRangeDate) filterQueryElement).getMinValue();
                            Date maxValue = ((FilterQuery.FilterFieldRangeDate) filterQueryElement).getMaxValue();
                            //todo query.add(filterField, NumericRangeQuery.newLongRange(filterField, minValue.getTime(), maxValue.getTime(), true, true));
                        } else{
                            for(String fieldValue : getFieldValues(filterQueryElement)){
                                final Date date = parseDateFrom(fieldValue, ISO8601_PATTERN);
                                //todo query.add(filterField, String.valueOf(date.getTime()));
                            }
                        }

                        break;
                    case DateYear:
                        // if range query
                        if(filterQueryElement instanceof FilterQuery.FilterFieldRange){
                            queryBitSet = processRangeQueryDateYearType(queryBitSet, map, filterQueryElement, type);
                        }else{
                            queryBitSet = processRangeQueryMultiValueQueryDateYear(queryBitSet, map, filterQueryElement, type);
                        }
                        break;
                }
            }
        }

        return queryBitSet;
    }


    private BitSet processRangeQueryDefaultTypes(BitSet queryBitSet, Map<String, BitSet> map, FilterQuery.FilterQueryElement filterQueryElement, DataType type) {
        String minValue = ((FilterQuery.FilterFieldRange) filterQueryElement).getMinValue();
        String maxValue = ((FilterQuery.FilterFieldRange) filterQueryElement).getMaxValue();
        //sorted set assumes all keys are of same type and sorted..
        final TreeMap treeMap = (TreeMap) map;
        final NavigableMap navigableMap = treeMap.subMap(minValue, true, maxValue, true);
        return processMultiValueQueryAllTypes(queryBitSet, map, navigableMap.keySet(), type);
    }

    private BitSet processRangeQueryMultiValueQueryDateYear(BitSet queryBitSet, Map<String, BitSet> map, FilterQuery.FilterQueryElement filterQueryElement, DataType type) {
        Set<String> dateYearSet = new HashSet<>(getFieldValues(filterQueryElement));
        return processMultiValueQueryAllTypes(queryBitSet, map, dateYearSet, type);
    }

    private BitSet processRangeQueryDateYearType(BitSet queryBitSet, Map<String, BitSet> map, FilterQuery.FilterQueryElement filterQueryElement, DataType type) {
        return processRangeQueryDefaultTypes(queryBitSet, map, filterQueryElement, type);
    }

    private BitSet processIntegerGreaterThanDefaultTypes(BitSet queryBitSet, Map<String, BitSet> map, FilterQuery.FilterQueryElement filterQueryElement, DataType type) {
        int minValue = ((FilterQuery.FilterFieldGreaterThanInteger) filterQueryElement).getFieldValue();
        final TreeMap treeMap = (TreeMap) map;

        final NavigableMap navigableMap = treeMap.tailMap(typeBasedStringFormatting(type, String.valueOf(minValue)), false);

        return processMultiValueQueryAllTypes(queryBitSet, map, navigableMap.keySet(), type);
    }

    private BitSet processMultiValueQueryDefaultTypes(BitSet queryBitSet, Map<String, BitSet> map, FilterQuery.FilterQueryElement filterQueryElement, DataType type) {
        final Set<String> fieldValues = getFieldValues(filterQueryElement);
        return processMultiValueQueryAllTypes(queryBitSet, map, fieldValues, type);
    }

    private BitSet processMultiValueQueryAllTypes(BitSet queryBitSet, Map<String, BitSet> map, Set<String> fieldValues, DataType type) {
        BitSet multiValueSet = new BitSet();
        for(String fieldValue : fieldValues){
            final BitSet bitSet = map.get(typeBasedStringFormatting(type,fieldValue));
            multiValueSet.or(bitSet);
        }
        if(multiValueSet.length()>0) {
            if(queryBitSet.length() > 1){
                queryBitSet.and(multiValueSet);
            }else { // if query bitset empty, just assign current multiset values to it
                queryBitSet = multiValueSet;
            }
        }
        return queryBitSet;
    }

    private Set<String> getFieldValues(FilterQuery.FilterQueryElement filterQueryElement) {
        return ((FilterQuery.FilterFieldValue) filterQueryElement).getFieldsValue();
    }

    private Date parseDateFrom(String fieldValue, final String pattern) {
        final DateTimeFormat isoFormat = getDateFormatFrom(pattern);
        return isoFormat.parse(fieldValue);
    }

    private DateTimeFormat getDateFormatFrom(final String pattern) {
        return DataType.ParseUtil.getDateFormatFrom(pattern);
    }

    static String leftPaddedInteger(int i) {
        if(i < 0){
            throw new IllegalArgumentException("Natural numbers only, does not support negative integers");
        }
        //max int length is  (2^31)-1
        final int maxSize = Integer.toString(Integer.MAX_VALUE).length()+1;
        final int length = maxSize - Integer.toString(i).length();
        char[] padArray = new char[length];
        Arrays.fill(padArray, '0');
        final String paddedInteger = (new String(padArray)) + Integer.toString(i);
        return paddedInteger;
    }

    static class QueryResponse {

    }
}
