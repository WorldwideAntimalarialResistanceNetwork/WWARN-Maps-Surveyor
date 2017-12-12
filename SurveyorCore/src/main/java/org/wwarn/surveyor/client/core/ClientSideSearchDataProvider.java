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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.Window;
import org.jetbrains.annotations.NotNull;
import org.wwarn.mapcore.client.offline.OfflineStatusObserver;
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.surveyor.client.event.DataUpdatedEvent;
import org.wwarn.surveyor.client.model.DataSourceProvider;
import org.wwarn.surveyor.client.util.AsyncCallbackWithTimeout;
import org.wwarn.surveyor.client.util.OfflineStorageUtil;
import com.google.gwt.user.client.Timer;

import java.util.*;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;


/**
 * A client search implementation using BitSets and offline data storage
 * When I wrote this, only God and I understood what I was doing. Now, God only knows. - Karl Weierstrass
 */
public class ClientSideSearchDataProvider extends ServerSideSearchDataProvider implements DataProvider{
    private static Logger logger = Logger.getLogger("SurveyorCore.ClientSideSearchDataProvider");

    private boolean isTest = false;
    private OfflineStatusObserver offlineStatusObserver = new OfflineStatusObserver();
    {
        try {
            final String check = offlineStatusObserver.check();
        }catch (Exception e){
            // do nothing but log exception
            if (logger.isLoggable(FINE)) {
                logger.log(FINE,"Failed in online/offline check using offlineStatusObserver");
            }

        }
    }

    /**
     * Ordered list (implicitly ordered - TreeSet) of fields in schema order, each field hold a mapping of fields values (terms) to document positions
     */
    private List<Map<String, BitSet>> fieldInvertedIndex;
    private RecordListCompressedWithInvertedIndexImpl recordListCompressedWithInvertedIndex;
    private FacetList facetList = new FacetList();
    OfflineStorageUtil<QueryResult> offlineDataStore = null;
    OfflineStorageUtil<String> offlineStorageCurrentKeyStore;
    OfflineStorageUtil<String> offlineStoragePreviousKeyStore;
    private DateTimeFormat dateTimeFormat = DataType.ParseUtil.getDateFormatFrom(DataType.ISO_DATE_FORMAT);

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
        //todo move this into a better data sync abstraction or tidy up
        offlineStorageCurrentKeyStore = new OfflineStorageUtil(String.class, getOfflineStoreUniqueKey());
        offlineStorageCurrentKeyStore.fetch(new OfflineStorageUtil.AsyncCommand<String>() {
            @Override
            public void success(final String key) {
                Objects.requireNonNull(key);
                if (logger.isLoggable(FINE)) {
                    logger.log(FINE,"Key found \"" + key + "\", now using this to find data store");
                }

                //if null then first load, no keys stored yet defer offline datastore creation
                offlineDataStore = new OfflineStorageUtil(QueryResult.class, createOfflineStorageUniqueKey(key));
                // try to load query from data store without server calls
                offlineDataStore.fetch(new OfflineStorageUtil.AsyncCommand<QueryResult>() {
                    @Override
                    public void success(QueryResult queryResult) {
                        if (logger.isLoggable(FINE)) {
                            logger.log(FINE,"Query result found for key\"" + key + "\", initialising app with offline data");
                        }
                        initialisedDataProvider(queryResult, callOnLoad);
                        // setup future calls to check for fresh data
                        scheduleCheckForDataUpdates();
                    }

                    @Override
                    public void failure() {
                        if (logger.isLoggable(FINE)) {
                            logger.log(FINE,"failed to fetch data from store, query result not found");
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

    boolean isDevelopmentMode() {
        return !GWT.isProdMode() && GWT.isClient();
    }

    private void fetchAllDataFromServers(final Runnable callOnLoad) {

        if(isOffline()){ // if offline, skip this step if this fails in debug mode
            return;
        }
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

    private boolean isOffline() {
        return !offlineStatusObserver.isOnline();
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
        if(isOffline()){
            return;
        }
//        //checks server for data updates
        final DataSchema schema = this.schema;
        final GenericDataSource dataSource = this.dataSource;
        listOfScheduledJobs.add(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                searchServiceAsync.fetchDataVersion(schema, dataSource, new AsyncCallbackWithTimeout<String>() {
                    @Override
                    public void onTimeOutOrOtherFailure(Throwable caught) {
                        logger.log(SEVERE,"Unable to fetch latest data version", caught);
                    }

                    @Override
                    public void onNonTimedOutSuccess(final String currentDataHash) {
                        if (recordListCompressedWithInvertedIndex == null)
                            throw new IllegalStateException("recordList was null");
                        if (recordListCompressedWithInvertedIndex.getDataSourceHash().equals(currentDataHash)) {
                            cleanupPreviousData(currentDataHash);
                            return;
                        }
                        final String previousDataSourceHash = recordListCompressedWithInvertedIndex.getDataSourceHash();
                        if (logger.isLoggable(FINE)) logger.log(FINE,"New data found, fetch records from server");
                        fetchAllDataFromServers(new Runnable() {
                            @Override
                            public void run() {
                                if (logger.isLoggable(FINE)) logger.log(FINE,"New data fetch complete");
                                storePreviousDataSourceHash(previousDataSourceHash);
                                promptUserToReload();
                            }
                        });

                    }
                });

                return true;
            }
        });
    }

    private boolean hasAlertRanOnceBeforeCheck = false;

    private void promptUserToReload() {
        if(hasAlertRanOnceBeforeCheck){return;}
        if(isReloadSupport()){
            if(Window.confirm("A new update has been received, would you like to refresh your page to receive this update?")){
                hasAlertRanOnceBeforeCheck = true;
                reloadPage();
            }
        }else {
            hasAlertRanOnceBeforeCheck = true;
            Window.alert("A new update has been received, please refresh your page.");
        }
    }
    public static native void reloadPage()/*-{
        $wnd.location.reload();
    }-*/;

    public static native boolean isReloadSupport()/*-{
        // We know that serialisation is slow in ie10 or below, too slow to be usable, so we disable this
        // We use GetRandomValues http://caniuse.com/#feat=getrandomvalues to determine if browser is recent,
        // as it is only support in IE 11 and above
        if (typeof $wnd.location === "undefined" || $wnd.location === null || typeof $wnd.location.reload === "undefined" || $wnd.location.reload === null) {
            return false;
        }
        return true;
    }-*/;
    private void cleanupPreviousData(final String currentDataHash) {
        if(logger.isLoggable(FINE)) logger.log(FINE,"Attempting to remove old key, if present");
        final OfflineStorageUtil<String> offlineStoragePreviousKeyStore = getOfflineStoragePreviousKeyStore();

        offlineStoragePreviousKeyStore.fetch(new OfflineStorageUtil.AsyncCommand<String>() {
            @Override
            public void success(@NotNull String previousKeyToRemove) {
                if (StringUtils.isEmpty(previousKeyToRemove)) return;
                if (previousKeyToRemove.equals(currentDataHash)) {
                    offlineStoragePreviousKeyStore.removeItem(getOfflineStorePreviousUniqueKey(), new Runnable() {
                        @Override
                        public void run() {
                            if (logger.isLoggable(FINE)) {
                                logger.log(FINE,"Old key not removed, as current and previous keys are equal, remove previous key reference instead");
                            }
                        }
                    });
                } else {
                    final String keyForOldHash = createOfflineStorageUniqueKey(previousKeyToRemove);
                    offlineStoragePreviousKeyStore.removeItem(keyForOldHash, new Runnable() {
                        @Override
                        public void run() {
                            if (logger.isLoggable(FINE)) {
                                logger.log(FINE," Old key removed : " + keyForOldHash);
                            }
                            //remove previous key reference, now old key referenced data deleted
                            offlineStoragePreviousKeyStore.removeItem(getOfflineStorePreviousUniqueKey(), new Runnable() {
                                @Override
                                public void run() {
                                if (logger.isLoggable(FINE)) {
                                    logger.log(FINE,"removing previous key, now old key referenced data deleted");
                                }
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void failure() {
                logger.log(FINE,"Offline storage: Unable to remove old key, probably because key not found or already deleted");
            }
        });
    }

    private void storePreviousDataSourceHash(String previousDataSourceHash) {
        if(logger.isLoggable(FINE)) logger.log(FINE,"Attempting to store previous datasourcehash");
        OfflineStorageUtil<String> offlineStoragePreviousKeyStore = getOfflineStoragePreviousKeyStore();
        offlineStoragePreviousKeyStore.store(previousDataSourceHash, new OfflineStorageUtil.AsyncCommand<String>() {
            @Override
            public void success(@NotNull String objectToStore) {
                if (logger.isLoggable(FINE)) logger.log(FINE,"Stored previous datasourcehash - successfully");
                // send an event to inform users to refresh browser as data has been updated.
                clientFactory.getEventBus().fireEvent(new DataUpdatedEvent());
            }

            @Override
            public void failure() {
                logger.log(FINE,"failed to store previous key");
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
                    public void success(QueryResult queryResult) {
                        if (logger.isLoggable(FINE)) {
                            logger.log(FINE,"stored current query result");
                        }
                    }

                    @Override
                    public void failure() {
                        final String message = "Unable to store result in offline store";
                        logger.log(FINE,message);
                        throw new IllegalStateException(message);
                    }
                });
            }

            @Override
            public void failure() {
                final String message = "Unable to store key in offline store";
                logger.log(FINE,message);
                throw new IllegalStateException(message);
            }
        });
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
                Timer scheduleTaskExecutor = new Timer() {
                    @Override
                    public void run() {
                        Scheduler.get().scheduleIncremental(new Scheduler.RepeatingCommand() {
                            @Override
                            public boolean execute() {
                                Scheduler.RepeatingCommand next = iterator.next();
                                try {
                                    GWT.log("ClientSideSearchDataProvider::executeScheduleTasks + attempting to execute task");
                                    boolean isOK = next != null && next.execute();
                                    if (!isOK) {
                                        //handle case where execution failed
                                        GWT.log("ClientSideSearchDataProvider::executeScheduleTasks execution returned false");
                                    } else
                                        GWT.log("ClientSideSearchDataProvider::executeScheduleTasks task execution complete");

                                    return isOK && iterator.hasNext();
                                } catch (Exception e) {
                                    GWT.log("ClientSideSearchDataProvider::executeScheduleTasks"+ "failed to execute task", e);
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
                scheduleTaskExecutor.schedule(20 * 1000); // start 20 seconds after page load
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
                final BitSet restrictedBitSetOfDocumentsAvailableAfterQuery = parseQuery(filterQuery, schema);
                RestrictedRecordResults restrictedRecordResults = restrictRecordList(restrictedBitSetOfDocumentsAvailableAfterQuery, facetFields);
//                final FacetList calculatedFacetFieldsAndDistinctValues = calculateFacetFieldsAndDistinctValues(schema, facetFields, fieldInvertedIndex, restrictedBitSetOfDocumentsAvailableAfterQuery);
                queryResultCallBack.onSuccess(new QueryResult(restrictedRecordResults.getRecordList(), restrictedRecordResults.getFacetFields()));
            }
        });
    }

    class RestrictedRecordResults {
        private final RecordList recordList;
        private final FacetList facetFields;

        public RestrictedRecordResults(RecordList recordList, FacetList facetFields) {
            this.recordList = recordList;
            this.facetFields = facetFields;
        }

        public RecordList getRecordList() {
            return recordList;
        }

        public FacetList getFacetFields() {
            return facetFields;
        }
    }

    private RestrictedRecordResults restrictRecordList(BitSet bitSet, String[] facetFields) {
        // filter recordListCompressedWith
        final List<RecordList.Record> records = recordListCompressedWithInvertedIndex.getRecords();
        List<RecordList.Record> restrictedRecordList = new ArrayList<>();
        facetList = new FacetList();

        TreeSet<String>[] facetFieldToUniqueFacteFieldValues = new TreeSet[schema.size()];
        // array to store all unique facet field values, ordered by index
        // initialise all elements to an empty set, in place of nulls
        for (int i = 0; i < records.size(); i++) {
            if (bitSet.length() < 1 || bitSet.get(i)) {
                final RecordList.Record record = records.get(i);
                restrictedRecordList.add(record);
                // for each facet field in this record, add
                for (String facetField : facetFields) {
                    final int columnSchemaIndexForCurrentField = schema.getColumnIndex(facetField);
                    String uniqueFacetFieldValueAtIndexPosition = record.getValueByFieldName(facetField);
                    if(facetFieldToUniqueFacteFieldValues[columnSchemaIndexForCurrentField] == null){
                        facetFieldToUniqueFacteFieldValues[columnSchemaIndexForCurrentField] = new TreeSet<>();
                    }
                    facetFieldToUniqueFacteFieldValues[columnSchemaIndexForCurrentField].add(uniqueFacetFieldValueAtIndexPosition);
                }
            }
        }

        for (int i = 0; i < facetFields.length; i++) {
            String facetField = facetFields[i];
            final int facetFieldColumnIndex = schema.getColumnIndex(facetField);
            facetList.addFacetField(facetField, facetFieldToUniqueFacteFieldValues[facetFieldColumnIndex]);
        }

        return new RestrictedRecordResults(new RecordListView(restrictedRecordList), facetList);
    }

    private FacetList calculateFacetFieldsAndDistinctValues(DataSchema schema, String[] facetFields, List<Map<String, BitSet>> fieldInvertedIndex, BitSet restrictedBitSetOfDocumentsAvailableAfterQuery) {
        //for each field
        for (String facetField : facetFields) {
            final int columnIndex = schema.getColumnIndex(facetField);
            final Map<String, BitSet> fieldValuesToPostingList = fieldInvertedIndex.get(columnIndex);
            String facetName = facetField;
            Set<String> uniqueFacetValues = new TreeSet<>();
            //for each field value
            for (String fieldValue : fieldValuesToPostingList.keySet()) {
                // get postings list
                final BitSet bitSetPostingList = fieldValuesToPostingList.get(fieldValue);
                // this code doesn't work when deployed
//                final BitSet bitSet1 = new BitSet();
//                bitSet1.or(bitSet);
//                bitSet1.and(restrictedBitSetOfDocumentsAvailableAfterQuery); //todo is this is most efficient way of doing this, consider storing this from previous run in query parsing?
                if(bitSetPostingList.cardinality() > 0){
                    uniqueFacetValues.add(fieldValue);
                }
            }
            facetList.addFacetField(facetName, uniqueFacetValues);
        }
        return facetList;
    }

    /**
     * Calculates which documents are available given the current FilterQuery selection
     * @param filterQuery
     * @param schema
     * @return a restrictedBitSetOfDocumentsAvailableAfterQuery document order is preserved
     */
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
                        queryBitSet = processRangeQueryIntegerTypes(queryBitSet, map, filterQueryElement, type);
                    } else if (filterQueryElement instanceof FilterQuery.FilterFieldGreaterThanInteger) {
                        queryBitSet = processIntegerGreaterThanDefaultTypes(queryBitSet, map, filterQueryElement, type);
                    } else {
                        // does most of the commons multi value types
                        queryBitSet = processMultiValueQueryDefaultTypes(queryBitSet, map, filterQueryElement, type);
                    }
                    break;
                case Date:

                    if (filterQueryElement instanceof FilterQuery.FilterFieldRangeDate) {
                        queryBitSet = processRangeQueryISODateTypes(queryBitSet, map, filterQueryElement, type);
                    } else {
                        queryBitSet = processRangeQueryMultiValueQueryDateYear(queryBitSet, map, filterQueryElement, type);
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

    private BitSet processRangeQueryIntegerTypes(BitSet queryBitSet, Map<String, BitSet> map, FilterQuery.FilterQueryElement filterQueryElement, DataType type) {
        String minValue = leftPaddedInteger(Integer.parseInt(((FilterQuery.FilterFieldRange) filterQueryElement).getMinValue()));
        String maxValue = leftPaddedInteger(Integer.parseInt(((FilterQuery.FilterFieldRange) filterQueryElement).getMaxValue()));
        //sorted set assumes all keys are of same type and sorted..
        final TreeMap treeMap = (TreeMap) map;
        final NavigableMap navigableMap = treeMap.subMap(minValue, true, maxValue, true);
        return processMultiValueQueryAllTypes(queryBitSet, map, navigableMap.keySet(), type);
    }

    private BitSet processRangeQueryISODateTypes(BitSet queryBitSet, Map<String, BitSet> map, FilterQuery.FilterQueryElement filterQueryElement, DataType type) {
        String minValue = (dateTimeFormat.format(((FilterQuery.FilterFieldRangeDate) filterQueryElement).getMinValue()));
        String maxValue = (dateTimeFormat.format(((FilterQuery.FilterFieldRangeDate) filterQueryElement).getMaxValue()));
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
        return processRangeQueryIntegerTypes(queryBitSet, map, filterQueryElement, type);
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
            if(bitSet != null) {
                multiValueSet.or(bitSet);
            }
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

    public static String leftPaddedInteger(int i) {
        if(i < 0){
            throw new IllegalArgumentException("Natural numbers only, does not support negative integers");
        }
        //max int length is  (2^31)-1
        final int maxSize = Integer.toString(Integer.MAX_VALUE).length()+1;
        final int length = maxSize - Integer.toString(i).length();
        char[] padArray = new char[length];
        Arrays.fill(padArray, '0');
        return (new String(padArray)) + Integer.toString(i);
    }

}
