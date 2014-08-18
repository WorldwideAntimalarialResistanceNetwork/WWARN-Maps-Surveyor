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
import org.wwarn.surveyor.client.model.TableViewConfig;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Setup index, from some data source and schema
 */
public class LuceneSearchServiceImpl implements SearchServiceLayer {
    public static final String DATE_FORMAT_YEAR = "yyyy";
    private static LuceneSearchServiceImpl ourInstance = new LuceneSearchServiceImpl();
    private Directory indexDirectory = new RAMDirectory();
    private Directory taxonomyDirectory = new RAMDirectory();
    private static final String ISO8601_PATTERN = DataType.ISO_DATE_FORMAT;
    private AtomicBoolean hasInitialised = new AtomicBoolean(false);
    private DataSchema dataSchema;
    private final FacetsConfig config = new FacetsConfig();
    public static final int MAX_RESULTS = 100;

    public static LuceneSearchServiceImpl getInstance() {
        return ourInstance;
    }

    private LuceneSearchServiceImpl() {
    }

    public void init(final DataSchema dataSchema, final GenericDataSource dataSource) throws SearchException {
        if(this.hasInitialised.get()){return;}
        Objects.requireNonNull(dataSchema); this.dataSchema = dataSchema;
        //setup index
        JSONArray jsonContent = getJsonArrayFrom(dataSource);
        setupIndex(dataSchema, jsonContent);
        this.hasInitialised.getAndSet(true);
        setupIndexMonitor(dataSchema, dataSource);
    }

    private void setupIndexMonitor(final DataSchema dataSchema, final GenericDataSource dataSource) throws SearchException {
        FileChangeMonitor fileChangeMonitor;
        if(dataSource.getDataSourceType()== GenericDataSource.DataSourceType.ServletRelativeDataSource && dataSource.getLocation()!=null) {
            assertFilePathExists(dataSource.getLocation(), "jsonPath invalid:");
            File json = new File(dataSource.getLocation());
            try {
                fileChangeMonitor = FileChangeMonitor.getInstance();
                fileChangeMonitor.init(json.toPath());
            } catch (IOException e) {
                throw new SearchException("unable to initialize file change monitor",e);
            }
        }else{
            // only able to monitor servlet relative datasources
            return;
        }
        fileChangeMonitor.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                JSONArray jsonContent;
                try {
                    jsonContent = getJsonArrayFrom(dataSource);
                } catch (SearchException e) {
                    throw new IllegalStateException(e);
                }
                setupIndex(dataSchema, jsonContent);
            }
        });
    }

    private JSONArray getJsonArrayFrom(GenericDataSource dataSource) throws SearchException {
        JSONArray jsonContent;
        if(dataSource.getDataSourceType()== GenericDataSource.DataSourceType.ServletRelativeDataSource && dataSource.getLocation()!=null) {
            assertFilePathExists(dataSource.getLocation(), "jsonPath invalid:");
            File json = new File(dataSource.getLocation());
            jsonContent = parseJSON(json);
        }else{
            Objects.requireNonNull(dataSource.getResource());
            jsonContent = parseJSON(dataSource.getResource());
        }
        return jsonContent;
    }

    private JSONArray parseJSON(String resource) {
        return (JSONArray) org.json.simple.JSONValue.parse(resource);
    }

    public static JSONArray parseJSON(File f) throws SearchException {
        JSONArray arrayObjects;
        try (
            Reader reader = new InputStreamReader(new FileInputStream(f));
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

    private void setupIndex(DataSchema schema, JSONArray jsonContent) {
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_48, new StandardAnalyzer(Version.LUCENE_48));
        try (
            IndexWriter indexWriter = new IndexWriter(indexDirectory, conf);
            DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(taxonomyDirectory);
        ){
            // parse json into index using schema
            List<Document> indexDocuments = parseJSONToIndexDocument(jsonContent, schema);
            for (Document indexDocument : indexDocuments) {
                indexWriter.addDocument(config.build(taxoWriter, indexDocument));
            }
            indexWriter.commit();

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<Document> parseJSONToIndexDocument(JSONArray jsonArray, DataSchema schema) {
        final ArrayList<Document> documents = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++){
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            Set<String> fieldNamesSet = jsonObject.keySet();

            final Document indexDocument = new Document();
            for (String fieldName : fieldNamesSet) {
                String fieldValue = jsonObject.get(fieldName).toString();
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
        if(Files.notExists(Paths.get(path), LinkOption.NOFOLLOW_LINKS)){
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
            recordList = convertSearchResultToRecordList(indexSearcher, hits, filterQuery.getFields());
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
                            String minValue = ((FilterQuery.FilterFieldRange) filterQueryElement).getMinValue();
                            String maxValue = ((FilterQuery.FilterFieldRange) filterQueryElement).getMaxValue();
                            query.add(filterField, TermRangeQuery.newStringRange(filterField, minValue, maxValue, true, true));
                        }else if(filterQueryElement instanceof FilterQuery.FilterFieldGreaterThanInteger){
                            int minValue = ((FilterQuery.FilterFieldGreaterThanInteger) filterQueryElement).getFieldValue();
                            query.add(filterField, NumericRangeFilter.newIntRange(filterField, minValue, Integer.MAX_VALUE, true, true));
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



    private Set<String> getFieldValues(FilterQuery.FilterQueryElement filterQueryElement) {
        return ((FilterQuery.FilterFieldValue) filterQueryElement).getFieldsValue();
    }

    private RecordList convertSearchResultToRecordList(IndexSearcher indexSearcher, List<ScoreDoc> docs, Set<String> filterFields) throws IOException {
        final RecordListBuilder recordListBuilder = new RecordListBuilder(RecordListBuilder.CompressionMode.CANONICAL, dataSchema);
        for (ScoreDoc doc : docs) {
            final Document document = indexSearcher.doc(doc.doc);
            String[] fieldValues = new String[dataSchema.size()];
            for (IndexableField indexableField : document) {
                final String fieldName = indexableField.name();
                final int columnIndex = dataSchema.getColumnIndex(fieldName);
                final DataType type = dataSchema.getType(fieldName);
                if(columnIndex < 0 || !isFieldSelected(fieldName, filterFields)){continue;}
                fieldValues[columnIndex] = mapIndexFieldValueToDataSchemaType(type, indexableField);
            }
            recordListBuilder.addRecord(fieldValues);
        }
        return recordListBuilder.createRecordList();
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

    public List<RecordList.Record> queryTable(FilterQuery filterQuery,String[] facetFields, int start, int length) throws SearchException{
        try{
            QueryResult queryResult = this.query(filterQuery, facetFields);
            RecordList recordList = queryResult.getRecordList();
            List<RecordList.Record> searchedRecords = recordList.getRecords();
            List<RecordList.Record> uniqueRecords = subsetUniqueRecords(filterQuery,searchedRecords);
            return getPageRecords(uniqueRecords, start,length);
        }catch(Exception e){
            throw new SearchException("Unable to query the table",e);
        }
    }

    private List<RecordList.Record> subsetUniqueRecords(FilterQuery filterQuery, List<RecordList.Record> searchedRecords) throws IllegalArgumentException {

        if(filterQuery.getFields() == null){
            throw new IllegalArgumentException("Table fields cannot be null. Please see them into the FilterQuery");
        }

        int uniqueRecordsCount = 0;
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
