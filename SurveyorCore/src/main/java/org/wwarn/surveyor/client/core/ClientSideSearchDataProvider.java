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
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.i18n.shared.DateTimeFormat;
import org.jetbrains.annotations.NotNull;
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.surveyor.client.event.DataUpdatedEvent;
import org.wwarn.surveyor.client.model.DataSourceProvider;
import org.wwarn.surveyor.client.util.AsyncCallbackWithTimeout;
import org.wwarn.surveyor.client.util.OfflineStorageUtil;
import com.google.gwt.user.client.Timer;

import java.util.*;

/**
 * A client search implementation using BitSets and offline data storage
 * When I wrote this, only God and I understood what I was doing. Now, God only knows. - Karl Weierstrass
 */
public class ClientSideSearchDataProvider extends ServerSideSearchDataProvider implements DataProvider{
    private boolean isTest = false;
    private List<Map<String, BitSet>> fieldInvertedIndex;
    private static final String ISO8601_PATTERN = DataType.ISO_DATE_FORMAT;
    private RecordListCompressedWithInvertedIndexImpl recordListCompressedWithInvertedIndex;
    private FacetList facetList = new FacetList();
    OfflineStorageUtil<QueryResult> offlineDataStore = null;
    OfflineStorageUtil<String> offlineStorageCurrentKeyStore;
    OfflineStorageUtil<String> offlineStoragePreviousKeyStore;

    public ClientSideSearchDataProvider(GenericDataSource dataSource, DataSchema dataSchema, String[] fieldList) {
        super(dataSource, dataSchema, fieldList);

        if(dataSource.getDataSourceProvider()!= DataSourceProvider.ClientSideSearchDataProvider){
            throw new IllegalArgumentException("Expected data source provider client side data provider");
        }
    }

    protected ClientSideSearchDataProvider(GenericDataSource dataSource, DataSchema dataSchema, String[] fieldList, boolean isTest) {
        this(dataSource, dataSchema, fieldList);

        if(dataSource.getDataSourceProvider()!= DataSourceProvider.ClientSideSearchDataProvider){
            throw new IllegalArgumentException("Expected data source provider client side data provider");
        }
        this.isTest = isTest;
    }

    private String createOfflineStorageUniqueKey(String keySuffix){
        if(StringUtils.isEmpty(keySuffix)){
            throw new IllegalArgumentException("dataSourceHash cannot be empty");
        }
        return getOfflineStoreUniqueKey() + keySuffix;
    }

    @NotNull
    private String getOfflineStoreUniqueKey() {
        return schema.getUniqueId() + "_uniqueKey";
    }

    @NotNull
    private String getOfflineStorePreviousUniqueKey() {
        return schema.getUniqueId() + "_previousUniqueKey";
    }

    @Override
    public void onLoad(final Runnable callOnLoad) {
        if(isTest){
            fetchAllDataFromServers(callOnLoad);return;
        }
        //todo move this into a better datasync abstraction or tidy up
        offlineStorageCurrentKeyStore = new OfflineStorageUtil(String.class, getOfflineStoreUniqueKey());
        offlineStorageCurrentKeyStore.fetch(new OfflineStorageUtil.AsyncCommand<String>() {
            @Override
            public void success(final String key) {
                Objects.requireNonNull(key);
                if (Log.isDebugEnabled()) {
                    Log.debug("Key found \"" + key + "\", now using this to find data store");
                }

                //if null then first load, no keys stored yet defer offline datastore creation
                offlineDataStore = new OfflineStorageUtil(QueryResult.class, createOfflineStorageUniqueKey(key));
                // try to load query from data store without server calls
                offlineDataStore.fetch(new OfflineStorageUtil.AsyncCommand<QueryResult>() {
                    @Override
                    public void success(QueryResult queryResult) {
                        if (Log.isDebugEnabled()) {
                            Log.debug("Query result found for key\"" + key + "\", intialising app with offline data");
                        }
                        initialisedDataProvider(queryResult, callOnLoad);
                        // setup future calls to check for fresh data
                        scheduleCheckForDataUpdates();
                    }

                    @Override
                    public void failure() {
                        if (Log.isDebugEnabled()) {
                            Log.debug("failed to fetch data from store, queryresult not found");
                        }
                        // failed to find query result in index, then fetch from server
                        fetchAllDataFromServers(callOnLoad);
                    }
                });
            }

            @Override
            public void failure() {
                // if key doesn't exist then fetch from server
                fetchAllDataFromServers(callOnLoad);
            }
        });


    }

    private void fetchAllDataFromServers(final Runnable callOnLoad) {
        try {
            final FilterQuery filterQuery = new MatchAllQuery(); // fetch everything
            clientFactory.setLastFilterQuery(filterQuery);
            searchServiceAsync.preFetchData(this.schema, this.dataSource, this.facetFieldList, filterQuery, new AsyncCallbackWithTimeout<QueryResult>() {
                @Override
                public void onTimeOutOrOtherFailure(Throwable throwable) {
                    throw new IllegalStateException(throwable);
                }

                @Override
                public void onNonTimedOutSuccess(final QueryResult queryResult) {
                    initialisedDataProvider(queryResult, callOnLoad);
                    scheduleStoreToOfflineDataStore(queryResult);

                }

            });
        } catch (SearchException e) {
            throw new IllegalStateException(e);
        }
    }

    private void scheduleStoreToOfflineDataStore(final QueryResult queryResult) {
        // attempt to store in 7 seconds after fetching from server, should help reduce initial load time.
        listOfScheduledJobs.add(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        storeToOfflineDataStore(queryResult);
                    }
                });
                return true;
            }
        });
    }

    private void scheduleCheckForDataUpdates() {
//        //checks server for data updates
        final DataSchema schema = this.schema;
        final GenericDataSource dataSource = this.dataSource;
        listOfScheduledJobs.add(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                searchServiceAsync.fetchDataVersion(schema, dataSource, new AsyncCallbackWithTimeout<String>() {
                    @Override
                    public void onTimeOutOrOtherFailure(Throwable caught) {
                        Log.warn("Unable to fetch latest data version", caught);
                    }

                    @Override
                    public void onNonTimedOutSuccess(final String currentDataHash) {
                        if (recordListCompressedWithInvertedIndex == null) throw new IllegalStateException("recordList was null");
                        if (recordListCompressedWithInvertedIndex.getDataSourceHash().equals(currentDataHash)) { cleanupPreviousData(currentDataHash); return; }
                        final String previousDataSourceHash = recordListCompressedWithInvertedIndex.getDataSourceHash();
                        if(Log.isDebugEnabled()) Log.debug("New data found, fetch records from server");
                        fetchAllDataFromServers(new Runnable() {
                            @Override
                            public void run() {
                                if (Log.isDebugEnabled()) Log.debug("New data fetch complete");
                                storePreviousDataSourceHash(previousDataSourceHash);
                            }
                        });

                    }
                });

                return true;
            }
        });
    }

    private void cleanupPreviousData(final String currentDataHash) {
        if(Log.isDebugEnabled()) Log.debug("Attempting to remove old key, if present");
        final OfflineStorageUtil<String> offlineStoragePreviousKeyStore = getOfflineStoragePreviousKeyStore();

        offlineStoragePreviousKeyStore.fetch(new OfflineStorageUtil.AsyncCommand<String>() {
            @Override
            public void success(@NotNull String previousKeyToRemove) {
                if(StringUtils.isEmpty(previousKeyToRemove)) return;
                if(previousKeyToRemove.equals(currentDataHash)){
                    offlineStoragePreviousKeyStore.removeItem(getOfflineStorePreviousUniqueKey(), new Runnable() {
                        @Override
                        public void run() {
                            if(Log.isDebugEnabled()){
                                Log.debug("Old key not removed, as current and previous keys are equal, remove previous key reference instead");
                            }
                        }
                    });
                }else {
                    final String keyForOldHash = createOfflineStorageUniqueKey(previousKeyToRemove);
                    offlineStoragePreviousKeyStore.removeItem(keyForOldHash, new Runnable() {
                        @Override
                        public void run() {
                            if (Log.isDebugEnabled()) {
                                Log.debug(" Old key removed : " + keyForOldHash);
                            }
                            //remove previous key reference, now old key referenced data deleted
                            offlineStoragePreviousKeyStore.removeItem(getOfflineStorePreviousUniqueKey(), new Runnable() {
                                @Override
                                public void run() {
                                    if(Log.isDebugEnabled()){
                                        Log.debug("removing previous key, now old key referenced data deleted");
                                    }
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void failure() {
                Log.warn("Offline storage: Unable to remove old key, probably because key not found or already deleted");
            }
        });
    }

    private void storePreviousDataSourceHash(String previousDataSourceHash) {
        if(Log.isDebugEnabled()) Log.debug("Attempting to store previous datasourcehash");
        OfflineStorageUtil<String> offlineStoragePreviousKeyStore = getOfflineStoragePreviousKeyStore();
        offlineStoragePreviousKeyStore.store(previousDataSourceHash, new OfflineStorageUtil.AsyncCommand<String>() {
            @Override
            public void success(@NotNull String objectToStore) {
                if (Log.isDebugEnabled()) Log.debug("Stored previous datasourcehash - successfully");
                // send an event to inform users to refresh browser as data has been updated.
                clientFactory.getEventBus().fireEvent(new DataUpdatedEvent());
//  Remove surplus call to fetch data
//                fetchAllDataFromServers(new Runnable() {
//                    @Override
//                    public void run() {
//                    }
//                });
            }

            @Override
            public void failure() {
                Log.warn("failed to store previous key");
            }
        });
    }

    private OfflineStorageUtil<String> getOfflineStoragePreviousKeyStore() {
        if(offlineStoragePreviousKeyStore == null){
            offlineStoragePreviousKeyStore = new OfflineStorageUtil(String.class, getOfflineStorePreviousUniqueKey());
        }
        return offlineStoragePreviousKeyStore;
    }

    private void storeToOfflineDataStore(final QueryResult queryResult) {
        if(isTest) return;
        // store current datasourceHash
        final String dataSourceHash = queryResult.getRecordList().getDataSourceHash();
        offlineStorageCurrentKeyStore.store((dataSourceHash), new OfflineStorageUtil.AsyncCommand<String>() {
            @Override
            public void success(String objectToStore) {
                // on storing current datasourcehash, initialise offline data store
                offlineDataStore = new OfflineStorageUtil(QueryResult.class, createOfflineStorageUniqueKey(dataSourceHash));
                // store current query result against DataSourceHash and SchemaUniqueID
                offlineDataStore.store(queryResult, new OfflineStorageUtil.AsyncCommand<QueryResult>() {
                    @Override
                    public void success(QueryResult queryResult) {if(Log.isDebugEnabled()){Log.debug("stored current query result");}}

                    @Override
                    public void failure() {
                        final String message = "Unable to store result in offline store";
                        Log.warn(message);
                        throw new IllegalStateException(message);
                    }
                });
            }

            @Override
            public void failure() {
                final String message = "Unable to store key in offlinestore";
                Log.warn(message);
                throw new IllegalStateException(message);
            }
        });
;
    }

    private void initialisedDataProvider(QueryResult queryResult, Runnable callOnLoad) {
        clientFactory.setLastQueryResult(queryResult);
        final RecordList recordList = queryResult.getRecordList();
        if(!(recordList instanceof RecordListCompressedWithInvertedIndexImpl)){ throw new IllegalArgumentException("Expected compressed index with inverted list");}
        recordListCompressedWithInvertedIndex = (RecordListCompressedWithInvertedIndexImpl) recordList;
        fieldInvertedIndex = setupIndex(recordListCompressedWithInvertedIndex);
        onLoadComplete(callOnLoad);
    }

    private void onLoadComplete(Runnable callOnLoad) {
        callOnLoad.run();
        executeScheduleTasks();
    }

    private List<Scheduler.RepeatingCommand> listOfScheduledJobs = new ArrayList<>();

    private void executeScheduleTasks() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {

                final Iterator<Scheduler.RepeatingCommand> iterator = listOfScheduledJobs.iterator();
                Timer scheduleTaskExector = new Timer() {
                    @Override
                    public void run() {
                        Scheduler.get().scheduleIncremental(new Scheduler.RepeatingCommand() {
                            @Override
                            public boolean execute() {
                                Scheduler.RepeatingCommand next = iterator.next();
                                try {
                                    Log.debug("ClientSideSearchDataProvider::executeScheduleTasks", "attempting to execute task");
                                    boolean isOK = next != null && next.execute();
                                    if (!isOK) {
                                        //handle case where execution failed
                                        Log.warn("ClientSideSearchDataProvider::executeScheduleTasks", "execution returned false");
                                    } else
                                        Log.debug("ClientSideSearchDataProvider::executeScheduleTasks", "task execution complete");

                                    return isOK && iterator.hasNext();
                                } catch (Exception e) {
                                    Log.error("ClientSideSearchDataProvider::executeScheduleTasks", "failed to execute task", e);
                                    return false;
                                } finally {
                                    if (next != null)
                                        iterator.remove();
                                    if (!iterator.hasNext()) {
                                        // end of list clean up
                                        listOfScheduledJobs.clear();
                                    }
                                }
                            }
                        });
                    }
                };
                scheduleTaskExector.schedule(20 * 1000); // start 20 seconds after page load
            }
        });
    }

    private List<Map<String, BitSet>> setupIndex(RecordListCompressedWithInvertedIndexImpl recordListCompressedWithInvertedIndex) {
        final FieldInvertedIndex index = recordListCompressedWithInvertedIndex.getIndex();
        //todo, as this is expensive, instead of creating a new data structure, must reuse existing, replace Set<Integer> with BitSet in place
        //Ordered list of fields in schema order, each field hold a mapping of fields values to terms to document positions (implicitly ordered - TreeSet)
        final List<Map<String, Set<Integer>>> fields = index.getFields();
        // setup initial BitSet
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
        query(filterQuery, this.facetFieldList, queryResultCallBack);
    }

    private void queryIndex(final FilterQuery filterQuery, final String[] facetFields, final AsyncCallbackWithTimeout<QueryResult> queryResultCallBack) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                final BitSet bitSet = parseQuery(filterQuery, schema);
                RecordList recordList = restrictRecordList(bitSet);
                final FacetList calculateFacetFieldsAndDistinctValues = calculateFacetFieldsAndDistinctValues(schema, facetFields, fieldInvertedIndex);
                queryResultCallBack.onSuccess(new QueryResult(recordList, calculateFacetFieldsAndDistinctValues));
            }
        });
    }



    private RecordList restrictRecordList(BitSet bitSet) {
        // filter recordListCompressedWith
        return new RecordListView(recordListCompressedWithInvertedIndex, bitSet);
    }

    private BitSet parseQuery(FilterQuery filterQuery, DataSchema schema) {
        BitSet queryBitSet = new BitSet();
        if(filterQuery instanceof MatchAllQuery || filterQuery.getFilterQueries().size() < 1) return queryBitSet;

        //facet drill down happens here
        for (String filterField : filterQuery.getFilterQueries().keySet()) {

            final int columnIndex = schema.getColumnIndex(filterField);
            final Map<String, BitSet> map = fieldInvertedIndex.get(columnIndex);

            final FilterQuery.FilterQueryElement filterQueryElement = filterQuery.getFilterQueries().get(filterField);
            DataType type = schema.getType(columnIndex);
            switch (type) {
                case CoordinateLat:
                case CoordinateLon:
                case String:
                case Boolean:
                case Integer:
                    // if range query
                    if (filterQueryElement instanceof FilterQuery.FilterFieldRange) {
                        queryBitSet = processRangeQueryDefaultTypes(queryBitSet, map, filterQueryElement, type);
                    } else if (filterQueryElement instanceof FilterQuery.FilterFieldGreaterThanInteger) {
                        queryBitSet = processIntegerGreaterThanDefaultTypes(queryBitSet, map, filterQueryElement, type);
                    } else {
                        // does most of the commons multi value types
                        queryBitSet = processMultiValueQueryDefaultTypes(queryBitSet, map, filterQueryElement, type);
                    }
                    break;
                case Date:

                    if (filterQueryElement instanceof FilterQuery.FilterFieldRangeDate) {
                        Date minValue = ((FilterQuery.FilterFieldRangeDate) filterQueryElement).getMinValue();
                        Date maxValue = ((FilterQuery.FilterFieldRangeDate) filterQueryElement).getMaxValue();
                        //todo query.add(filterField, NumericRangeQuery.newLongRange(filterField, minValue.getTime(), maxValue.getTime(), true, true));
                    } else {
                        for (String fieldValue : getFieldValues(filterQueryElement)) {
                            final Date date = parseDateFrom(fieldValue, ISO8601_PATTERN);
                            //todo query.add(filterField, String.valueOf(date.getTime()));
                        }
                    }

                    break;
                case DateYear:
                    // if range query
                    if (filterQueryElement instanceof FilterQuery.FilterFieldRange) {
                        queryBitSet = processRangeQueryDateYearType(queryBitSet, map, filterQueryElement, type);
                    } else {
                        queryBitSet = processRangeQueryMultiValueQueryDateYear(queryBitSet, map, filterQueryElement, type);
                    }
                    break;
            }

        }
        return queryBitSet;
    }

    private FacetList calculateFacetFieldsAndDistinctValues(DataSchema schema, String[] facetFields, List<Map<String, BitSet>> fieldInvertedIndex) {
        for (String facetField : facetFields) {
            final int columnIndex = schema.getColumnIndex(facetField);
            final Map<String, BitSet> map = fieldInvertedIndex.get(columnIndex);
            String facetName = facetField;
            Set<String> uniqueFacetValues = new TreeSet<>();
            for (String fieldValue : map.keySet()) {
                final BitSet bitSet = map.get(fieldValue);
                if(bitSet.cardinality() > 0){
                    uniqueFacetValues.add(fieldValue);
                }
            }
            facetList.addFacetField(facetName, uniqueFacetValues);
        }
        return facetList;
    }


    private BitSet processRangeQueryDefaultTypes(BitSet queryBitSet, Map<String, BitSet> map, FilterQuery.FilterQueryElement filterQueryElement, DataType type) {
        String minValue = leftPaddedInteger(Integer.parseInt(((FilterQuery.FilterFieldRange) filterQueryElement).getMinValue()));
        String maxValue = leftPaddedInteger(Integer.parseInt(((FilterQuery.FilterFieldRange) filterQueryElement).getMaxValue()));
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
}
