package org.wwarn.surveyor.client.mvp;

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

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import org.wwarn.surveyor.client.event.InterfaceLoadCompleteEvent;
import org.wwarn.surveyor.client.mvp.presenter.FilterPresenter;
import org.wwarn.surveyor.client.mvp.presenter.LoadStatusListener;
import org.wwarn.surveyor.client.mvp.presenter.ResultPresenter;
import org.wwarn.surveyor.client.mvp.view.filter.FilterViewUI;
import org.wwarn.surveyor.client.mvp.view.MainPanelView;
import org.wwarn.surveyor.client.mvp.view.result.ResultViewUI;

/**
 * Controller for the application, handles any view state transitions
 * Changes triggered by GWT history tweaks
 * User: nigel
 * Date: 30/07/13
 * Time: 16:33
 */
public class SurveyorAppController implements ValueChangeHandler<String> {

    protected ClientFactory clientFactory = SimpleClientFactory.getInstance();
    protected LoadStatusListener loadStatusListener = LoadStatusListener.getInstance();
    protected MainPanelView layout;
    protected ResultPresenter resultPresenter;

    // Event Bus bindings
    interface MapLoadedEventBinder extends EventBinder<SurveyorAppController> {};
    private MapLoadedEventBinder eventBinder = GWT.create(MapLoadedEventBinder.class);

    public SurveyorAppController(MainPanelView layout) {
        eventBinder.bindEventHandlers(this, clientFactory.getEventBus());
        this.layout = layout;
        clientFactory.getDataProvider().onLoad(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
        // bind to event
        bind();
    }

    @EventHandler
    public void onInterfaceLoaded(InterfaceLoadCompleteEvent interfaceLoadCompleteEvent){
        // do nothing, could handle load sequence here..
//        onLoadCompleteToggleLoadingScreen();
    }



    private void init() {
        // fire default state
        History.fireCurrentHistoryState();
    }

    private void bind() {
        // binds to gwt History to track changes in UI
        History.addValueChangeHandler(this);
    }

    public void onValueChange(ValueChangeEvent<String> event) {
        // on first load (ie when resultPresenter is null) call display
        if(resultPresenter == null){
            display();
        }else{
            String historyToken = event.getValue();
        // after first load delegate changes to resultPresenter
            resultPresenter.selectTab(historyToken);
        }

    }

    /**
     * Display home screen
     */
    protected void display() {
        new FilterPresenter(new FilterViewUI()).go(layout);
        resultPresenter = new ResultPresenter(new ResultViewUI());
        loadStatusListener.registerObserver(resultPresenter);
        resultPresenter.go(layout);

    }

}
