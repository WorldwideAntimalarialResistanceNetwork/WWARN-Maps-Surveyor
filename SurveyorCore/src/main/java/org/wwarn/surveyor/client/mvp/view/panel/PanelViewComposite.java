package org.wwarn.surveyor.client.mvp.view.panel;

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
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import org.wwarn.surveyor.client.model.PanelViewConfig;

/**
 * Created by suay on 5/16/14.
 */
public class PanelViewComposite extends Composite {

    interface PanelViewUIUiBinder extends UiBinder<FlowPanel, PanelViewComposite> {
    }
    private static PanelViewUIUiBinder ourUiBinder = GWT.create(PanelViewUIUiBinder.class);

    private String HTML_FILES_PATH = GWT.getModuleBaseForStaticFiles()+ "htmlFiles/";

    PanelViewConfig panelViewConfig;

    private final FlowPanel panel;

    @UiField
    ListBox listBox = new ListBox();

    @UiField
    Frame frame = new Frame();

    public PanelViewComposite(PanelViewConfig panelViewConfig){

        this.panelViewConfig = panelViewConfig;
        panel = ourUiBinder.createAndBindUi(this);
        panel.setWidth("100%");
        initWidget(panel);
        String path = HTML_FILES_PATH + panelViewConfig.getStartFile();
        frame.setUrl(path);
        setupListBox();

    }

    void setupListBox(){
        for(PanelViewConfig.Page page: panelViewConfig.getPages()){
            listBox.addItem(page.getFilterValue());
        }
    }

    @UiHandler("listBox")
    public void onFilterChanged(ChangeEvent topicChanged){
        String topic = listBox.getValue(listBox.getSelectedIndex());
        for(PanelViewConfig.Page page : panelViewConfig.getPages()){
            if(page.getFilterValue().equalsIgnoreCase(topic)){

                String filePath = HTML_FILES_PATH + page.getFilePath();
                frame.setUrl(filePath);
            }
        }
    }

}
