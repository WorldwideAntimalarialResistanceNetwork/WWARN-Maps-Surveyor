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

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import org.wwarn.surveyor.client.event.FilterChangedEvent;
import org.wwarn.surveyor.client.event.ResultChangedEvent;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for listening for filter
 * change events and corresponding results changed events
 * to show and hide the load messages respectively
 */
public class LoadStatusListener {
    private List<LoadStatusObserver> loadStatusObservers = new ArrayList<LoadStatusObserver>();
    LoadStatusObserver.LoadingStatus loadingStatus = LoadStatusObserver.LoadingStatus.UNDETERMINED;

    // Event Bus bindings
    interface LoadEventBinder extends EventBinder<LoadStatusListener> {};

    private static LoadStatusListener ourInstance = new LoadStatusListener();

    public static LoadStatusListener getInstance() {
        return ourInstance;
    }

    LoadStatusListener() {
        LoadEventBinder eventBinder = GWT.create(LoadEventBinder.class);
        eventBinder.bindEventHandlers(this, SimpleClientFactory.getInstance().getEventBus());
    }

    public interface LoadStatusObserver{
        enum LoadingStatus{LOADED, LOADING, UNDETERMINED}
        public void update(LoadStatusObserver.LoadingStatus loadingStatus);
    }

    public void notifyObservers(){
        for (LoadStatusObserver loadStatusObserver : loadStatusObservers) {
            loadStatusObserver.update(loadingStatus);
        }
    }

    public void registerObserver(LoadStatusObserver loadStatusObserver){
        this.loadStatusObservers.add(loadStatusObserver);
    }

    public void removeObserver(LoadStatusObserver loadStatusObserver){
        this.loadStatusObservers.remove(loadStatusObserver);
    }

    @EventHandler
    public void onResultChanged(ResultChangedEvent resultChangedEvent){
        this.loadingStatus = LoadStatusObserver.LoadingStatus.LOADED;
        notifyObservers();
    }

    @EventHandler
    public void onFilterChanged(FilterChangedEvent filterChangedEvent){
        this.loadingStatus = LoadStatusObserver.LoadingStatus.LOADING;
        notifyObservers();
    }

}
