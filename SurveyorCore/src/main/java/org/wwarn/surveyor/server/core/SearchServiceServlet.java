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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.wwarn.surveyor.client.core.*;

import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nigelthomas on 28/05/2014.
 */
public class SearchServiceServlet extends RemoteServiceServlet implements SearchService {
    private SearchServiceLayer searchServiceLayer;

    @Override
    public void init() throws ServletException {
        super.init();
        searchServiceLayer = LuceneSearchServiceImpl.getInstance();
    }

    @Override
    public QueryResult query(FilterQuery filterQuery, String[] facetFields) throws SearchException {

        final QueryResult query = searchServiceLayer.query(filterQuery, facetFields);
        return query;
    }

    @Override
    public QueryResult preFetchData(DataSchema schema, GenericDataSource dataSource, String[] facetFields,FilterQuery filterQuery) throws SearchException {
        Objects.requireNonNull(schema);
        Objects.requireNonNull(dataSource);
        //relative path to absolute path
        final String fileInServletContext = (dataSource.getLocation()==null)?null:findFileInServletContext(dataSource.getLocation());
        final GenericDataSource source = new GenericDataSource(fileInServletContext, dataSource.getResource(), dataSource.getDataSourceType());
        searchServiceLayer.init(schema, source);
        return this.query(filterQuery, facetFields);
    }

    private static Map<String, String> filePathCache = new ConcurrentHashMap<>();

    private String findFileInServletContext(final String relativeFilePath) {
        if(filePathCache.containsKey(relativeFilePath)){
            return filePathCache.get(relativeFilePath);
        }
        String realPath = getServletContextFilePath();
        final Path fileToFind = Paths.get(relativeFilePath);
        final String[] publicationsPath = {realPath};
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
            e.printStackTrace();
        }
        return publicationsPath[0];
    }

    private String getServletContextFilePath(){
        return this.getServletContext().getRealPath("/");
    }

}
