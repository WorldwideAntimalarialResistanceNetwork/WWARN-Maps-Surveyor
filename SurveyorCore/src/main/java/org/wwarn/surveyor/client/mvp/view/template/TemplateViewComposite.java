package org.wwarn.surveyor.client.mvp.view.template;

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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.*;
import org.wwarn.surveyor.client.core.QueryResult;
import org.wwarn.surveyor.client.core.RecordList;
import org.wwarn.surveyor.client.model.TemplateViewConfig;
import org.wwarn.surveyor.client.mvp.ClientFactory;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;

/**
 * Template based view
 */
public class TemplateViewComposite extends Composite {

    private FlowPanel rootElement;

    interface TemplateBasedViewUiBinder extends UiBinder<FlowPanel, TemplateViewComposite> {
    }

    private static TemplateBasedViewUiBinder ourUiBinder = GWT.create(TemplateBasedViewUiBinder.class);
    private ClientFactory clientFactory = SimpleClientFactory.getInstance();
    private  TemplateBasedViewBuilder templateBasedViewBuilder;

    public TemplateViewComposite(final TemplateViewConfig viewConfig) {
        rootElement = ourUiBinder.createAndBindUi(this);
        initWidget(rootElement);
        TemplateBasedViewBuilder.createAsync(new TemplateBasedViewBuilder.TemplateBasedViewBuilderClient() {
            @Override
            public void onSuccess(TemplateBasedViewBuilder instance) {
                templateBasedViewBuilder = instance;
                final Panel panel = draw(templateBasedViewBuilder, viewConfig);
                rootElement.add(panel);
            }

            @Override
            public void onUnavailable() {
                throw new IllegalStateException("unable ot load plotter");
            }
        });

    }

    private Panel draw(TemplateBasedViewBuilder templateBasedViewBuilder, TemplateViewConfig viewConfig) {
        final QueryResult lastQueryResult = clientFactory.getLastQueryResult();
        final RecordList recordList = lastQueryResult.getRecordList();
        final Panel panel = templateBasedViewBuilder.draw(viewConfig.getTemplateViewNodesConfig(), recordList);
        return panel;
    }
}