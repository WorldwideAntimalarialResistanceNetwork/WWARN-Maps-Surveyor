package org.wwarn.surveyor.client.mvp.presenter;

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

import com.google.gwt.user.client.History;
import org.wwarn.surveyor.client.core.ApplicationContext;
import org.wwarn.surveyor.client.model.ResultsViewConfig;
import org.wwarn.surveyor.client.mvp.ClientFactory;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;
import org.wwarn.surveyor.client.mvp.view.MainPanelView;
import org.wwarn.surveyor.client.mvp.view.result.ResultView;

import java.util.ArrayList;
import java.util.List;

/**
 * Has logic to setup and maintain tabs
 * should respond to tab selection changes etc
 */
public class ResultPresenter implements Presenter, LoadStatusListener.LoadStatusObserver{
    private ClientFactory clientFactory = SimpleClientFactory.getInstance();
    private final ApplicationContext applicationContext = clientFactory.getApplicationContext();
    private ResultView resultView;
    public Integer currentSelectedTab = 0;

    public ResultPresenter(ResultView resultView) {
        this.resultView = resultView;
        bind();
    }

    public void go(MainPanelView container) {
        container.getResultsContainerPanel().add(resultView.asWidget());
        setupTabs();
    }

    private void setupTabs() {
        ResultsViewConfig config = applicationContext.getConfig(ResultsViewConfig.class);
        resultView.setup(config);
        String currentTabSelection = History.getToken();
        currentSelectedTab = parseUserInputForTabSelection(currentTabSelection);
        resultView.selectTab(currentSelectedTab);
    }

    public void bind() {
        resultView.setPresenter(this);
    }

    /**
     *  If a tab is selected then we want to add a new history item to the History object.
     *  (this effectively changes the token in the URL, which is detected and handled by
     *  GWT's History sub-system.
     * @param tabSelected
     */
    public void onTabChange(Integer tabSelected) {
        // Create a new history item for this tab (using data retrieved from Pages enumeration)
        currentSelectedTab = tabSelected;
        History.newItem(currentSelectedTab.toString());
    }

    /**
     * Handles logic for selecting a tab
     * @param selectedTab
     */
    public void selectTab(String selectedTab) {
        int tabIndex = parseUserInputForTabSelection(selectedTab);
        resultView.selectTab(tabIndex);
    }

    private int parseUserInputForTabSelection(String selectedTab) {
        int tabIndex = 0;
        try{
            tabIndex = Integer.parseInt(selectedTab);
        }catch(NumberFormatException e){}
        return tabIndex;
    }

    @Override
    public void update(LoadingStatus loadingStatus) {
        resultView.onLoadingStatusChange(loadingStatus);
    }
}
