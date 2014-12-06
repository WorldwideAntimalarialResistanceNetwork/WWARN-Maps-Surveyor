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

import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.surveyor.client.core.*;
import org.wwarn.surveyor.client.model.DataSourceProvider;
import org.wwarn.surveyor.client.model.TableViewConfig;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nigelthomas on 28/05/2014.
 */
public class SearchServiceServlet extends RemoteServiceServlet implements SearchService {
    public static final String DEFAULT_DATA_PUBLICATIONS_JSON = "/data/publications.json";
    private SearchServiceLayer searchServiceLayer;
    private static Map<String, String> filePathCache = new ConcurrentHashMap<>();

    @Override
    public void init() throws ServletException {
        super.init();
        searchServiceLayer = LuceneSearchServiceImpl.getInstance();
        // add filePath to publications.json
    }

    @Override
    public QueryResult query(FilterQuery filterQuery, String[] facetFields) throws SearchException {
        final QueryResult query = searchServiceLayer.query(filterQuery, facetFields);
        return query;
    }

    @Override
    public List<RecordList.Record> queryTable(FilterQuery filterQuery,String[] facetFields, int start, int length, TableViewConfig tableViewConfig) throws SearchException {
        final List<RecordList.Record> records = searchServiceLayer.queryTable(filterQuery,facetFields,  start, length, tableViewConfig);
        return records;
    }

    @Override
    public QueryResult preFetchData(DataSchema schema, GenericDataSource dataSource, String[] facetFields,FilterQuery filterQuery) throws SearchException {
        Objects.requireNonNull(schema);
        Objects.requireNonNull(dataSource);
        //relative path to absolute path
        final String fileInServletContext = (dataSource.getLocation() == null) ? null : findFileInServletContext(dataSource.getLocation());
        final GenericDataSource source = new GenericDataSource(fileInServletContext, dataSource.getResource(), dataSource.getDataSourceType(), dataSource.getDataSourceProvider());
        searchServiceLayer.init(schema, source);
        QueryResult queryResult = null;
        if(dataSource.getDataSourceProvider() == DataSourceProvider.ClientSideSearchDataProvider){
            filterQuery.setBuildInvertedIndex(true);
        }
        queryResult = this.query(filterQuery, facetFields);
        return queryResult;
    }


    private String findFileInServletContext(final String relativeFilePath) {
        if(filePathCache.containsKey(relativeFilePath)){
            return filePathCache.get(relativeFilePath);
        }

        final String publicationsPathDefault = getDefaultPublicationPath(relativeFilePath);
        if(!StringUtils.isEmpty(publicationsPathDefault)){ return publicationsPathDefault;}


        final String realPath = getServletContextFilePath();
        final Path fileToFind = Paths.get(relativeFilePath);
        final String[] publicationsPath = new String[]{""};
        try {
            Files.walkFileTree(Paths.get(realPath), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (attrs.isRegularFile() && file.getFileName().equals(fileToFind.getFileName()) && file.getParent().endsWith(fileToFind.getParent())) {
                        publicationsPath[0] = file.toAbsolutePath().toString();
                        filePathCache.put(relativeFilePath, publicationsPath[0]);
                        return FileVisitResult.TERMINATE;
                    }
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return publicationsPath[0];
    }

    private String getDefaultPublicationPath(String relativeFilePath) {
        //relative path to absolute path
        final String resource = this.getServletContext().getRealPath(DEFAULT_DATA_PUBLICATIONS_JSON);
        if(StringUtils.isEmpty(resource)) return null;
        filePathCache.put(relativeFilePath, resource);
        return resource;
    }

    private String getServletContextFilePath(){
        return this.getServletContext().getRealPath("/");
    }

    private static class CacheMap {
        Cache cache;

        private CacheMap() {
            Map props = new HashMap();
            props.put(GCacheFactory.EXPIRATION_DELTA, 3600*24);

            try {
                CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
                cache = cacheFactory.createCache(Collections.emptyMap());
            } catch (CacheException e) {}
        }

    }

}
