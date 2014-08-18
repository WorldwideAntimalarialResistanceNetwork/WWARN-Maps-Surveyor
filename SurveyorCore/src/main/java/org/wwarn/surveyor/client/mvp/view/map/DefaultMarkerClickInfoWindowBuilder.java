package org.wwarn.surveyor.client.mvp.view.map;

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

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.wwarn.mapcore.client.components.customwidgets.GenericMarker;
import org.wwarn.surveyor.client.core.*;
import org.wwarn.surveyor.client.model.TemplateViewNodesConfig;
import org.wwarn.surveyor.client.model.MapViewConfig;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;
import org.wwarn.surveyor.client.mvp.view.template.TemplateBasedViewBuilder;

import java.util.Set;

/**
* Created by nigelthomas on 16/07/2014.
*/
public class DefaultMarkerClickInfoWindowBuilder implements GenericMarker.MarkerClickInfoWindowBuilderAsync<RecordList.Record>{
    SimpleClientFactory simpleClientFactory = SimpleClientFactory.getInstance();
    DataSchema schema = simpleClientFactory.getSchema();
    TemplateViewNodesConfig config;

    private TemplateBasedViewBuilder templateBasedViewBuilder;

    public DefaultMarkerClickInfoWindowBuilder() {
        TemplateBasedViewBuilder.createAsync(new TemplateBasedViewBuilder.TemplateBasedViewBuilderClient() {
            @Override
            public void onSuccess(TemplateBasedViewBuilder instance) {
                templateBasedViewBuilder = instance;
            }

            @Override
            public void onUnavailable() {
                throw new IllegalStateException("unable ot load plotter");
            }
        });
    }

    @Override
    public Widget build(RecordList.Record markerContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void build(final RecordList.Record markerContext, final AsyncCallback<Widget> result) {
        //fetch data from server async...
        // pass recordlist to Template based view builder get widget and return
        try {
            getRelatedRecords(markerContext, new AsyncCallback<QueryResult>() {
                @Override
                public void onFailure(Throwable throwable) {
                    result.onFailure(throwable);
                }

                @Override
                public void onSuccess(QueryResult queryResult) {
                    Panel panel;

                    final RecordList recordList = queryResult.getRecordList();
                    panel = templateBasedViewBuilder.draw(getConfig(), recordList);
                    final Set<String> fieldNames = schema.getColumns();
                    if (panel == null) {
                        panel = getDefaultMarkup(markerContext, fieldNames);
                    }
                    VerticalPanel verticalPanel = new VerticalPanel();
                    verticalPanel.add(panel);
                    verticalPanel.setHeight("300px");
                    verticalPanel.setWidth("650px");
                    result.onSuccess(verticalPanel);

                }
            });

        } catch (Exception e) {
            result.onFailure(e);
        }
    }

    private void getRelatedRecords(RecordList.Record context, final AsyncCallback<QueryResult> queryResultAsyncCallback) throws SearchException {
        final TemplateViewNodesConfig templateViewNodesConfig = getConfig();
        final String dataSource = templateViewNodesConfig.getDataSource();
        final String[] fieldsToMatchCurrentContext = dataSource.split(",");
        FilterQuery filterQuery = new FilterQuery();
        for (String field : fieldsToMatchCurrentContext) {
            final String valueToFilter = context.getValueByFieldName(field);
            filterQuery.addFilter(field, valueToFilter);
        }
        SimpleClientFactory.getInstance().getDataProvider().query(filterQuery, queryResultAsyncCallback);
    }

    private TemplateViewNodesConfig getConfig() {
        if(config == null){
            final MapViewConfig mapConfig = simpleClientFactory.getApplicationContext().getConfig(MapViewConfig.class);
            config = mapConfig.getTemplateViewNodesConfig();
        }
        return config;
    }

    private Panel getDefaultMarkup(RecordList.Record markerContext, Set<String> fieldNames) {
        Panel panel;SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
        safeHtmlBuilder.appendHtmlConstant("<ul>");
        for (String fieldName : fieldNames) {
            safeHtmlBuilder.appendHtmlConstant("<li>");
            safeHtmlBuilder.appendEscapedLines(fieldName + " : " + markerContext.getValueByFieldName(fieldName));
            safeHtmlBuilder.appendHtmlConstant("</li>");
        }
        safeHtmlBuilder.appendHtmlConstant("</ul>");
        panel = new HTMLPanel(safeHtmlBuilder.toSafeHtml());
        return panel;
    }

}
