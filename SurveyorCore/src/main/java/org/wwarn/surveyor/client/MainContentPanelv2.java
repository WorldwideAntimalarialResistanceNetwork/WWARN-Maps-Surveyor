package org.wwarn.surveyor.client;

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
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.wwarn.surveyor.client.mvp.view.MainPanelView;

/**
 * An alternative to the table based layout, using div based layout instead
 * User: nigelthomas
 * Date: 02/10/2013
 * Time: 20:46
 */
public class MainContentPanelv2 implements MainPanelView {

    interface MainContentPanelv2UiBinder extends UiBinder<DockLayoutPanel, MainContentPanelv2> {
    }

    private static MainContentPanelv2UiBinder ourUiBinder = GWT.create(MainContentPanelv2UiBinder.class);

    @UiField(provided = true)
    FlowPanel filterContainerPanel = new FlowPanel();

    @UiField(provided = true)
    FlowPanel resultsContainerPanel = new FlowPanel();

    @UiField(provided = true)
    FlowPanel rightColumnBottomContainerPanel = new FlowPanel();


    public MainContentPanelv2() {
        DockLayoutPanel dockLayoutPanel = ourUiBinder.createAndBindUi(this);
        // Add the outer panel to the RootLayoutPanel, so that it will be
        // displayed.
        RootLayoutPanel root = RootLayoutPanel.get();
        root.add(dockLayoutPanel);
    }

    /**
     * Get filter panel this is used in controller to setup filters
     * @return
     */
    public FlowPanel getFilterContainerPanel() {
        return filterContainerPanel;
    }

    public FlowPanel getResultsContainerPanel() {
        return resultsContainerPanel;
    }

    public FlowPanel getRightColumnBottomContainerPanel() {
        return rightColumnBottomContainerPanel;
    }

    public void clearLayout(){
        filterContainerPanel.clear();
        resultsContainerPanel.clear();
    }
}
