package org.wwarn.surveyor.server.core;

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
import com.googlecode.luceneappengine.GaeDirectory;
import com.googlecode.luceneappengine.GaeLuceneUtil;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.facet.*;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.surveyor.client.core.*;
import org.wwarn.surveyor.client.model.DataSourceProvider;
import org.wwarn.surveyor.client.model.TableViewConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;

/**
 * Setup index, from some data source and schema
 */
public class LuceneSearchServiceImpl implements SearchServiceLayer {
    private static Logger logger = Logger.getLogger("SurveyorCore.LuceneSearchServiceImpl");

    public static final String DATE_FORMAT_YEAR = "yyyy";
    private static LuceneSearchServiceImpl ourInstance = new LuceneSearchServiceImpl();
    private Directory indexDirectory = null;
    private Directory taxonomyDirectory = null;
    private static final String ISO8601_PATTERN = DataType.ISO_DATE_FORMAT;
    private AtomicBoolean hasInitialised = new AtomicBoolean(false);
    private DataSchema dataSchema;
    private final FacetsConfig config = new FacetsConfig();
    public static final int MAX_RESULTS = 100;
    private String location;
    private String dataSourceHash = "";
    private FileChangeMonitor fileChangeMonitor;

    public static LuceneSearchServiceImpl getInstance() {
        return ourInstance;
    }

    private LuceneSearchServiceImpl() {
    }

    public void init(final DataSchema dataSchema, final GenericDataSource dataSource) throws SearchException {
        try {
            if(this.hasInitialised.get() && dataSource != null && dataSource.getLocation() == location){
                logger.log(FINE,"LuceneSearchServiceImpl::init", "skipping initialisation, already called previously");
                return;
            }
            Objects.requireNonNull(dataSchema);
            this.dataSchema = dataSchema;
            location = dataSource.getLocation();
            //setup index
            JSONWithMetaData jsonContent = getJsonArrayFrom(dataSource);
            setupIndex(dataSource, dataSchema, jsonContent);
            logger.log(FINE,"LuceneSearchServiceImpl::init", "Finished setup of index from json data");
            this.hasInitialised.getAndSet(true);
            setupIndexMonitor(dataSchema, dataSource);
        } catch (Exception e) {
            logger.log(SEVERE,"Failed to initialise index", e);
            throw e;
        }
    }

    /**
     * setup a monitor to watch for changes in datasource
     * @param dataSchema
     * @param dataSource
     */
    private void setupIndexMonitor(final DataSchema dataSchema, final GenericDataSource dataSource) {
        logger.log(FINE,"LuceneSearchServiceImpl::setupIndexMonitor Attempting to setup Index monitor...");

        try {
            if(GoogleAppEngineUtil.isGaeEnv()){
                return; // only use for pure lucene tomcat implementation, doesn't work on GAE.
            }
            if (dataSource.getDataSourceType() != GenericDataSource.DataSourceType.ServletRelativeDataSource || dataSource.getLocation() == null) {
                // only able to monitor servlet relative datasources
                return;
            } else {
//                assertFilePathExists(dataSource.getLocation(), "jsonPath invalid:");
//                File json = new File(dataSource.getLocation());
//                try {
//                    fileChangeMonitor = FileChangeMonitor.getInstance();
//                    fileChangeMonitor.init(json.toPath());
//                    if(Log.isDebugEnabled()) Log.debug("LuceneSearchServiceImpl::setupIndexMonitor","Index monitor setup completed.");
//                } catch (IOException e) {
//                    throw new SearchException("Unable to initialize file change monitor",e);
//                }
            }
            fileChangeMonitor = FileChangeMonitor.getInstance();
            fileChangeMonitor.addObserver(new Observer() {
                @Override
                public void update(Observable o, Object arg) {
                    JSONWithMetaData jsonContent;
                    try {
                        logger.log(FINE,"LuceneSearchServiceImpl::setupIndexMonitor", "Index monitor change noted.");
                        jsonContent = getJsonArrayFrom(dataSource);
                    } catch (SearchException e) {
                        throw new IllegalStateException(e);
                    }
                    setupIndex(dataSource, dataSchema, jsonContent);
                }
            });
        } catch (Exception e) {
            logger.log(SEVERE,"LuceneSearchServiceImpl::setupIndexMonitor Failed to setup monitor for indexed file changes", e);
        }
    }


    private JSONWithMetaData getJsonArrayFrom(GenericDataSource dataSource) throws SearchException {
        JSONArray jsonContent;
        final FileVersionUtil fileVersionUtil = new FileVersionUtil();
        if(dataSource.getDataSourceType()== GenericDataSource.DataSourceType.ServletRelativeDataSource && dataSource.getLocation()!=null) {
            assertFilePathExists(dataSource.getLocation(), "jsonPath invalid:");
            File json = new File(dataSource.getLocation());
            // grab version information
//            final String hashOfJsonFile = "aosfghjpoeuhrgnalsznv" /*fileVersionUtil.calculateVersionFrom(json)*/;
            final String hashOfJsonFile =  fileVersionUtil.calculateVersionFrom(json);
            jsonContent = parseJSON(json);
            return new JSONWithMetaData(jsonContent, hashOfJsonFile);
        }else{
            Objects.requireNonNull(dataSource.getResource());
            final String stringResource = dataSource.getResource();
            final byte[] bytes = stringResource.getBytes();
            final String hashFromInputStream = fileVersionUtil.getHashFromBytes(bytes);
            jsonContent = parseJSON(stringResource);
            return new JSONWithMetaData(jsonContent, hashFromInputStream);
        }
    }

    private class JSONWithMetaData{
        private String dataSourceHash;
        private JSONArray jsonArray;

        public String getDataSourceHash() {
            return dataSourceHash;
        }

        public JSONArray getJsonArray() {
            return jsonArray;
        }

        public JSONWithMetaData(JSONArray jsonArray, String dataSourceHash) {
            this.jsonArray = jsonArray;
            this.dataSourceHash = dataSourceHash;
        }
    }

    private JSONArray parseJSON(String resource) {
        return (JSONArray) org.json.simple.JSONValue.parse(resource);
    }

    public static JSONArray parseJSON(File f) throws SearchException {
        JSONArray arrayObjects;
//        Reader reader = Files.newBufferedReader(f.toPath(),StandardCharsets.UTF_8);
        try (
            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8.name()));
        ) {
            Object fileObjects = org.json.simple.JSONValue.parse(reader);
            arrayObjects = (JSONArray) fileObjects;
        } catch (FileNotFoundException e) {
            throw new SearchException("Unable to open index or parse json file", e);
        } catch (IOException e) {
            throw new SearchException("Unable to open index or parse json file",e);
        }
        return arrayObjects;
    }

    private void setupIndex(GenericDataSource dataSource, DataSchema schema, JSONWithMetaData jsonWithMetaData) {
        IndexWriterConfig conf = getConfigFrom(dataSource);//get configuration
        indexDirectory = getDirectoryFrom(dataSource);//create a default index
        taxonomyDirectory = new RAMDirectory();//create a default index
        this.dataSourceHash = jsonWithMetaData.getDataSourceHash();
        logger.log(FINE, "indexDirectory and taxonomyDirectory are ready");
        try (
                IndexWriter indexWriter = new IndexWriter(indexDirectory, conf);
                DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(taxonomyDirectory);
        ){
            // parse json into index using schema
            final JSONArray jsonArray = jsonWithMetaData.getJsonArray();
            if(jsonArray == null){
                throw new IllegalArgumentException("Failed to parse JSON, check JSON is valid.");
            }
            List<Document> indexDocuments = parseJSONToIndexDocument(jsonArray, schema);
            for (Document indexDocument : indexDocuments) {
                indexWriter.addDocument(config.build(taxoWriter, indexDocument));
            }
            indexWriter.commit();
            logger.log(FINE,"finished parsing json into index");


        } catch (IOException e) {
            logger.log(SEVERE,"Failed to setup search index",e);
            throw new IllegalStateException(e);
        }
    }

    private IndexWriterConfig getConfigFrom(GenericDataSource dataSource) {
        return (dataSource.getDataSourceProvider() == DataSourceProvider.GoogleAppEngineLuceneDataSource)?GaeLuceneUtil.getIndexWriterConfig(Version.LUCENE_4_9, new StandardAnalyzer(Version.LUCENE_4_9)): new IndexWriterConfig(Version.LUCENE_4_9, new StandardAnalyzer(Version.LUCENE_4_9));
    }

    private Directory getDirectoryFrom(GenericDataSource dataSource) {
        return (dataSource.getDataSourceProvider() == DataSourceProvider.GoogleAppEngineLuceneDataSource)?new GaeDirectory():new RAMDirectory();
    }

    private List<Document> parseJSONToIndexDocument(JSONArray jsonArray, DataSchema schema) {
        final ArrayList<Document> documents = new ArrayList<>();
        Objects.requireNonNull(jsonArray);
        for (int i = 0; i < jsonArray.size(); i++){
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            Set<String> fieldNamesSet = jsonObject.keySet();

            final Document indexDocument = new Document();
            for (String fieldName : fieldNamesSet) {
                if (fieldName == null) {
                    continue;
                }
                Object fieldNameObj = jsonObject.get(fieldName);
                if (fieldNameObj == null) {
                    continue;
                }
                String fieldValue = String.valueOf(fieldNameObj);
                final DataType type = schema.getType(fieldName);
                if(type == null){continue;} // if type not found then it is not defined in schema, so ignore..
                if(StringUtils.isEmpty(fieldValue)){continue;}
                indexDocument.add(new FacetField(fieldName, fieldValue));
                switch (type) {
                    case String:
                        indexDocument.add(new StringField(fieldName, fieldValue, Field.Store.YES));
                        break;
                    case CoordinateLat:
                    case CoordinateLon:
                        indexDocument.add(new DoubleField(fieldName, Double.parseDouble(fieldValue), Field.Store.YES));
                        break;
                    case Integer:
                        indexDocument.add(new IntField(fieldName, Integer.parseInt((fieldValue)), Field.Store.YES));
                        indexDocument.add(new NumericDocValuesField(fieldName, Long.parseLong(fieldValue)));
                        break;
                    case Boolean:
                        indexDocument.add(new StringField(fieldName, fieldValue, Field.Store.YES));
                        break;
                    case Date:
                        Date date = tryParseDate(fieldValue, "01/01/1970");
                        indexDocument.add(new LongField(fieldName, date.getTime(), Field.Store.YES));
                        indexDocument.add(new NumericDocValuesField(fieldName, date.getTime()));
                        break;
                    case DateYear:
                        indexDocument.add(new IntField(fieldName, Integer.parseInt(fieldValue), Field.Store.YES));
                        indexDocument.add(new NumericDocValuesField(fieldName, Long.parseLong(fieldValue)));
                        break;
                }
            }
            if(indexDocument.getFields().size() < 1){ /*Failed to parse all columns*/
                throw new IllegalArgumentException("Failed to parse documents from supplied json, check json matches supplied schema");
            }

            documents.add(indexDocument);
        }
        return documents;
    }

    private Date tryParseDate(String fieldValue, String defaultDate) {
        return DataType.ParseUtil.tryParseDate(fieldValue, defaultDate);
    }

    private Date parseDateFrom(String fieldValue, final String pattern) {
        final DateTimeFormat isoFormat = getDateFormatFrom(pattern);
        return isoFormat.parse(fieldValue);
    }

    private String parseStringFrom(Date date, final String pattern){
        final DateTimeFormat isoFormat = getDateFormatFrom(pattern);
        return isoFormat.format(date);
    }

    private DateTimeFormat getDateFormatFrom(final String pattern) {
        return DataType.ParseUtil.getDateFormatFrom(pattern);
    }

    private void assertFilePathExists(String path, String messageOnFail) {
        if(!(new File(path)).canRead()){
            throw new IllegalArgumentException(messageOnFail + path);
        }
    }

    @Override
    public QueryResult query(FilterQuery filterQuery, String[] facetFields) throws SearchException {
        if (!hasInitialised.get()) {
            throw new IllegalStateException("Search not ready : init method must be called first");
        }
        RecordList recordList;
        FacetList facetList;
        try (IndexReader indexReader = DirectoryReader.open(indexDirectory);
            TaxonomyReader taxoReader = new DirectoryTaxonomyReader(taxonomyDirectory);
        ){
            final IndexSearcher indexSearcher = new IndexSearcher(indexReader);

            // Aggregates the facet counts
            FacetsCollector facetsCollector = new FacetsCollector();

            DrillDownQuery q = parseQuery(filterQuery, dataSchema);

            indexSearcher.search(q, facetsCollector);
            DrillSideways ds = new DrillSideways(indexSearcher, config, taxoReader);
            final SimpleCollector simpleCollector = new SimpleCollector();
            DrillSideways.DrillSidewaysResult result = ds.search(q, simpleCollector);

            // Retrieve results
            List<FacetResult> facets = result.facets.getAllDims(MAX_RESULTS);
            facetList = convertFacetResultToFacetList(facets, facetFields);
            final List<ScoreDoc> hits = simpleCollector.getHits();

            // convert scoredoc to recordlist
            recordList = convertSearchResultToRecordList(indexSearcher, hits, filterQuery);
        } catch (IOException e) {
            throw new SearchException("Unable to open index or error while fetching documents", e);
        }
        return new QueryResult(recordList, facetList);
    }

    private FacetList convertFacetResultToFacetList(List<FacetResult> facets, String[] facetFields) {
        FacetList facetList = new FacetList();
        for (String facetField : facetFields) {
            for (FacetResult facet : facets) {
                if(facet == null || !facetField.equals(facet.dim)){continue;}
                final LabelAndValue[] labelValues = facet.labelValues;
                Set<String> uniqueFacetValues = new TreeSet<>();
                for (LabelAndValue labelValue : labelValues) {
                    uniqueFacetValues.add(labelValue.label);
                }

                facetList.addFacetField(facet.dim, uniqueFacetValues);
            }
        }
        return facetList;
    }

    private DrillDownQuery parseQuery(FilterQuery filterQuery, DataSchema schema) {
        final MatchAllDocsQuery matchAllDocsQuery = new MatchAllDocsQuery();
        DrillDownQuery query = new DrillDownQuery(config, matchAllDocsQuery);
        if (!(filterQuery instanceof MatchAllQuery) && filterQuery.getFilterQueries().size() > 0) {
            //facet drill down happens here
            for (String filterField : filterQuery.getFilterQueries().keySet()) {
                final FilterQuery.FilterQueryElement filterQueryElement = filterQuery.getFilterQueries().get(filterField);
                DataType type = schema.getType(filterField);
                switch (type){
                    case CoordinateLat:
                    case CoordinateLon:
                    case String:
                    case Boolean:
                    case Integer:
                        // if range query
                        if(filterQueryElement instanceof FilterQuery.FilterFieldRange){
                            if(filterQueryElement instanceof FilterQuery.FilterFieldRange.FilterFieldRangeInteger){
                                Integer minValue = ((FilterQuery.FilterFieldRange.FilterFieldRangeInteger) filterQueryElement).getMinValueInteger();
                                Integer maxValue = ((FilterQuery.FilterFieldRange.FilterFieldRangeInteger) filterQueryElement).getMaxValueInteger();
                                query.add(filterField, NumericRangeQuery.newIntRange(filterField, minValue, maxValue, true, true));
                            }
                            else{
                                String minValue = ((FilterQuery.FilterFieldRange) filterQueryElement).getMinValue();
                                String maxValue = ((FilterQuery.FilterFieldRange) filterQueryElement).getMaxValue();
                                query.add(filterField, TermRangeQuery.newStringRange(filterField, minValue, maxValue, true, true));
                            }
                        }else if(filterQueryElement instanceof FilterQuery.FilterFieldGreaterThanInteger) {
                            int minValue = ((FilterQuery.FilterFieldGreaterThanInteger) filterQueryElement).getFieldValue();
                            query.add(filterField, NumericRangeFilter.newIntRange(filterField, minValue, Integer.MAX_VALUE, true, true));
                        }else if(filterQueryElement instanceof FilterQuery.FilterFieldRangeDate){
                            query.add(filterField, setYearYangeQuery(filterField, filterQueryElement));
                        }else{
                            for(String fieldValue : getFieldValues(filterQueryElement)){
                                query.add(filterField, fieldValue);
                            }
                        }
                        break;
                    case Date:
                        if(filterQueryElement instanceof FilterQuery.FilterFieldRangeDate){
                            Date minValue = ((FilterQuery.FilterFieldRangeDate) filterQueryElement).getMinValue();
                            Date maxValue = ((FilterQuery.FilterFieldRangeDate) filterQueryElement).getMaxValue();
                            query.add(filterField, NumericRangeQuery.newLongRange(filterField, minValue.getTime(), maxValue.getTime(), true, true));
                        } else{
                            for(String fieldValue : getFieldValues(filterQueryElement)){
                                final Date date = parseDateFrom(fieldValue, ISO8601_PATTERN);
                                query.add(filterField, String.valueOf(date.getTime()));
                            }
                        }

                        break;
                    case DateYear:
                        // if range query
                        if(filterQueryElement instanceof FilterQuery.FilterFieldRange){
                            String minValue = ((FilterQuery.FilterFieldRange) filterQueryElement).getMinValue();
                            String maxValue = ((FilterQuery.FilterFieldRange) filterQueryElement).getMaxValue();
                            query.add(filterField, NumericRangeQuery.newIntRange(filterField, Integer.parseInt(minValue), Integer.parseInt(maxValue), true, true));
                        }else{
                            for(String fieldValue : getFieldValues(filterQueryElement)){
                                query.add(filterField, String.valueOf(Integer.parseInt(fieldValue)));
                            }
                        }
                        break;
                }
            }
        }

        return query;
    }

    private NumericRangeQuery<Integer> setYearYangeQuery(String filterField, FilterQuery.FilterQueryElement filterQueryElement){
        Date minValue = ((FilterQuery.FilterFieldRangeDate) filterQueryElement).getMinValue();
        Date maxValue = ((FilterQuery.FilterFieldRangeDate) filterQueryElement).getMaxValue();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(minValue);
        int minYear = calendar.get(Calendar.YEAR);
        calendar.setTime(maxValue);
        int maxYear = calendar.get(Calendar.YEAR);
        return NumericRangeQuery.newIntRange(filterField, minYear, maxYear, true, true);
    }

    private Set<String> getFieldValues(FilterQuery.FilterQueryElement filterQueryElement) {
        return ((FilterQuery.FilterFieldValue) filterQueryElement).getFieldsValue();
    }

    private RecordList convertSearchResultToRecordList(IndexSearcher indexSearcher, List<ScoreDoc> docs, FilterQuery filterQuery) throws IOException {
        RecordListBuilder recordListBuilder;
        if(filterQuery.buildInvertedIndex()) {
            recordListBuilder = new RecordListBuilder(RecordListBuilder.CompressionMode.CANONICAL_WITH_INVERTED_INDEX, dataSchema);
        }else{
            recordListBuilder = new RecordListBuilder(RecordListBuilder.CompressionMode.CANONICAL, dataSchema);
        }
        for (ScoreDoc doc : docs) {
            final Document document = indexSearcher.doc(doc.doc);
            String[] fieldValues = new String[dataSchema.size()];
            Arrays.fill(fieldValues, ""); //ensure all fields initialized to empty string
            for (IndexableField indexableField : document) {
                final String fieldName = indexableField.name();
                final int columnIndex = dataSchema.getColumnIndex(fieldName);
                final DataType type = dataSchema.getType(fieldName);
                final Set<String> strings = filterQuery.getFields();
                if(columnIndex < 0 || !isFieldSelected(fieldName, strings)){continue;}
                fieldValues[columnIndex] = mapIndexFieldValueToDataSchemaType(type, indexableField);
            }
            recordListBuilder.addRecord(fieldValues);
        }
        final RecordList recordList = recordListBuilder.createRecordList(dataSourceHash);
        return recordList;
    }


    private boolean isFieldSelected(String field, Set<String> filterFields){
        //If filterFields is null then all the fields are selected
        if(filterFields == null || filterFields.isEmpty()){
            return true;
        }

        for(String filterField : filterFields){
            if(field.equals(filterField)){
                return true;
            }
        }
        return false;
    }

    private String mapIndexFieldValueToDataSchemaType(DataType type, IndexableField indexableField) {
        switch (type) {
            case Date:
                final Number date = indexableField.numericValue();
                final long dateAsLong = date.longValue();
                Date dateObj = new Date(dateAsLong);
                return parseStringFrom(dateObj, ISO8601_PATTERN);
            case DateYear:
                dateObj = tryParseDate(indexableField.stringValue(), "1970");
                return parseStringFrom(dateObj, ISO8601_PATTERN);
        }

        return indexableField.stringValue();

    }
    
    public List<RecordList.Record> queryTable(FilterQuery filterQuery,String[] facetFields, int start, int length, TableViewConfig tableViewConfig) throws SearchException{
        try{
            QueryResult queryResult = this.query(filterQuery, facetFields);
            RecordList recordList = queryResult.getRecordList();
            List<RecordList.Record>  searchedRecords = recordList.getRecords();
            List<RecordList.Record> orderRecords = orderRecords(searchedRecords, tableViewConfig);
            List<RecordList.Record> uniqueRecords = subsetUniqueRecords(filterQuery,orderRecords);
            return getPageRecords(uniqueRecords, start,length);
        }catch(Exception e){
            final String message = "Unable to query the table";
            logger.log(SEVERE,message,e);
            throw new SearchException(message,e);
        }
    }

    private List<RecordList.Record> subsetUniqueRecords(FilterQuery filterQuery, List<RecordList.Record> searchedRecords) throws IllegalArgumentException {

        if(filterQuery.getFields() == null){
            throw new IllegalArgumentException("Table fields cannot be null. Please see them into the FilterQuery");
        }

        List<RecordList.Record> pageRecords = new ArrayList<>();
        int schemaSize = dataSchema.size();

        for (RecordList.Record searchedRecord : searchedRecords){
            String[] fields = new String[schemaSize];
            for(String field : filterQuery.getFields()){
                String columnValue = searchedRecord.getValueByFieldName(field);
                int columnIndex = dataSchema.getColumnIndex(field);
                fields[columnIndex] = columnValue;
            }

            RecordList.Record record = new RecordList.Record(fields, dataSchema);
            if(!pageRecords.contains(record)){
//                if(uniqueRecordsCount >= start){
                    pageRecords.add(record);
//                }
//                uniqueRecordsCount++;
            }

//            if(pageRecords.size() > (length+start)){
//                break;
//            }
        }

        return pageRecords;
    }

    List<RecordList.Record> orderRecords(List<RecordList.Record> records, final TableViewConfig tableViewConfig){

        if(tableViewConfig == null || tableViewConfig.getSortColumn() == null|| tableViewConfig.getSortColumn().isEmpty()){
            return records;
        }

        List<RecordList.Record> orderList = new ArrayList<>(records.size());
        orderList.addAll(records);

        Collections.sort(orderList,new Comparator<RecordList.Record>() {
            @Override
            public int compare(RecordList.Record o1, RecordList.Record o2) {
                if (o1 == o2) {
                    return 0;
                }

                String sortColumn = tableViewConfig.getSortColumn();
                int diff = -1;
                if (o1 != null) {
                    diff = (o2 != null) ? o1.getValueByFieldName(sortColumn).compareTo(o2.getValueByFieldName(sortColumn)) : 1;
                }

                return tableViewConfig.isDescendentOrder() ? -diff : diff;
            }
        });
        return orderList;
    }

    private List<RecordList.Record>  getPageRecords(List<RecordList.Record> uniqueRecords, int start, int length){
        if(start > uniqueRecords.size()){
            return Collections.emptyList();
        }

        List<RecordList.Record> pageRecords = new ArrayList<>(length);
        for(int i = start; i < start + length; i++){
            if(uniqueRecords.size() <= i){
                break;
            }
            pageRecords.add(uniqueRecords.get(i));
        }
        return pageRecords;
    }

    public QueryResult queryUniqueRecords(FilterQuery filterQuery, String[] facetFields) throws SearchException {
        try{
            QueryResult queryResultAux = this.query(filterQuery, facetFields);
            List<RecordList.Record>  searchedRecords = queryResultAux.getRecordList().getRecords();
            List<RecordList.Record> uniqueRecords = subsetUniqueRecords(filterQuery,searchedRecords);
            RecordListBuilder recordListBuilder = new RecordListBuilder(RecordListBuilder.CompressionMode.CANONICAL, dataSchema);
            recordListBuilder.addAllRecords(uniqueRecords);
            final RecordList recordList = recordListBuilder.createRecordList(dataSourceHash);
            QueryResult queryResult = new QueryResult(recordList, null);
            return queryResult;
        }catch(Exception e){
            throw new SearchException("Unable to query unique records",e);
        }
    }

    @Override
    public String fetchDataVersion() {
        if (!hasInitialised.get()) {
            throw new IllegalStateException("Search not ready : init method must be called first");
        }
        return this.dataSourceHash;
    }

    /**
     * Consider moving this to use TopDocsCollector later for paging
     */
    public static class SimpleCollector extends Collector{
        List<ScoreDoc> docs = new ArrayList<ScoreDoc>();
        private int docBase;
        private Scorer scorer;

        @Override
        public void setScorer(Scorer scorer) throws IOException {
            this.scorer = scorer;
        }

        @Override
        public void collect(int doc) throws IOException {
            docs.add(
                    new ScoreDoc(doc+docBase,
                            scorer.score()));
        }

        @Override
        public void setNextReader(AtomicReaderContext atomicReaderContext) throws IOException {
            this.docBase = atomicReaderContext.docBase;
        }

        @Override
        public boolean acceptsDocsOutOfOrder() {
            //Return true if your Collector can handle out-of-order docIDs.
            //Some BooleanQuery instances can collect results faster if this
            //returns true.
            return true;
        }

        public List<ScoreDoc> getHits() {
            return docs;
        }
    }
}
