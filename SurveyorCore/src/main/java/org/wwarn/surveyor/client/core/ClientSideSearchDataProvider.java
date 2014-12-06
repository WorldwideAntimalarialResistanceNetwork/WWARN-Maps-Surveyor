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
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.surveyor.client.model.DataSourceProvider;

import java.util.*;

/**
 *
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
            InitialFilterQuery initialFilterQuery = getInitialFilterQuery();
            final FilterQuery filterQuery = new MatchAllQuery(); // fetch everything
            clientFactory.setLastFilterQuery(filterQuery);


            searchServiceAsync.preFetchData(schema, this.dataSource, this.facetFieldList, filterQuery, new AsyncCallback<QueryResult>() {
                @Override
                public void onFailure(Throwable throwable) {
                    throw new IllegalStateException(throwable);
                }

                @Override
                public void onSuccess(QueryResult queryResult) {
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
        //for each field
        for (Map<String, Set<Integer>> field : fields) {
            //for each field value
            Map<String, BitSet> fieldValueBitSetMap = new HashMap<>();
            for (String fieldValue : field.keySet()) {
                //get postings list
                BitSet bitSet = fieldValueBitSetMap.get(fieldValue);
                if(bitSet == null){bitSet = new BitSet();}
                final Set<Integer> positions = field.get(fieldValue);
                for (Integer pos : positions) {
                    bitSet.set(pos);
                }
                fieldValueBitSetMap.put(fieldValue, bitSet);
            }
            fieldsBitSet.add(fieldValueBitSetMap);
        }
        return fieldsBitSet;
    }

    @Override
    public void query(FilterQuery filterQuery, String[] facetFields, AsyncCallback<QueryResult> queryResultCallBack) throws SearchException {
        Objects.requireNonNull(fieldInvertedIndex, "Search not ready : onLoad method must be called first");
        queryIndex(filterQuery, facetFields, queryResultCallBack);
    }

    @Override
    public void query(FilterQuery filterQuery, AsyncCallback<QueryResult> queryResultCallBack) throws SearchException {
        query(filterQuery, new String[]{}, queryResultCallBack);
    }

    private void queryIndex(FilterQuery filterQuery, String[] facetFields, AsyncCallback<QueryResult> queryResultCallBack) {
        if(filterQuery instanceof MatchAllQuery){
            queryResultCallBack.onSuccess(new QueryResult(recordListCompressedWithInvertedIndex, new FacetList()));
            return;
        }
        final BitSet bitSet = parseQuery(filterQuery, schema);
        RecordList recordList = restrictRecordList(bitSet);
        queryResultCallBack.onSuccess(new QueryResult(recordList, new FacetList()));
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
                            String minValue = ((FilterQuery.FilterFieldRange) filterQueryElement).getMinValue();
                            String maxValue = ((FilterQuery.FilterFieldRange) filterQueryElement).getMaxValue();

                            //todo query.add(filterField, TermRangeQuery.newStringRange(filterField, minValue, maxValue, true, true));
                        }else if(filterQueryElement instanceof FilterQuery.FilterFieldGreaterThanInteger){
                            int minValue = ((FilterQuery.FilterFieldGreaterThanInteger) filterQueryElement).getFieldValue();
                            //todo query.add(filterField, NumericRangeFilter.newIntRange(filterField, minValue, Integer.MAX_VALUE, true, true));

                        }else{
                            final Set<String> fieldValues = getFieldValues(filterQueryElement);
                            BitSet multiValueSet = new BitSet();
                            for(String fieldValue : fieldValues){
                                final BitSet bitSet = map.get(fieldValue);
                                multiValueSet.or(bitSet);
                            }
                            if(multiValueSet.length()>0) {
//                                System.out.println(queryBitSet.toBinaryString());
//                                System.out.println(multiValueSet.toBinaryString());
                                if(queryBitSet.length() > 1){
                                    queryBitSet.and(multiValueSet);
                                }else { // if query bitset empty, just assign current multiset values to it
                                    queryBitSet = multiValueSet;
                                }
                            }
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
                            String minValue = ((FilterQuery.FilterFieldRange) filterQueryElement).getMinValue();
                            String maxValue = ((FilterQuery.FilterFieldRange) filterQueryElement).getMaxValue();
                            //todo query.add(filterField, NumericRangeQuery.newIntRange(filterField, Integer.parseInt(minValue), Integer.parseInt(maxValue), true, true));
                        }else{
                            for(String fieldValue : getFieldValues(filterQueryElement)){
                                //todo query.add(filterField, String.valueOf(Integer.parseInt(fieldValue)));
                            }
                        }
                        break;
                }
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
}
