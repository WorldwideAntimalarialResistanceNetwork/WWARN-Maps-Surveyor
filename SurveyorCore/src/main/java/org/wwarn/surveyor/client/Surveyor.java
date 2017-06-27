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


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.Table;
import org.wwarn.mapcore.client.utils.MapLoadUtil;
import org.wwarn.surveyor.client.mvp.ClientFactory;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;
import org.wwarn.surveyor.client.mvp.SurveyorAppController;
import org.wwarn.surveyor.client.mvp.view.MainPanelView;

import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Surveyor implements EntryPoint {
    protected ClientFactory clientFactory = SimpleClientFactory.getInstance();
    private static Logger logger = Logger.getLogger("SurveyorCore.Surveyor");

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable e) {
                logger.log(SEVERE, "UncaughtException", e);
            }
        });
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                //load map v3 api
                MapLoadUtil.loadMapApi(new Runnable() {
                    public void run() {
                        loadVisualisationApi();
                    }
                });
            }
        });
    }

    static class LoggingUnCaughtExceptionHandler implements GWT.UncaughtExceptionHandler{

        @Override
        public void onUncaughtException(Throwable throwable) {
            logger.log(SEVERE,"Uncaught exception", throwable);
        }
    }

    private void loadVisualisationApi() {
        // load visualisation api ... before loading any panels..
        VisualizationUtils.loadVisualizationApi(new Runnable() {
            public void run() {
//                EventLogger.logEvent("org.wwarn.surveyor.client.Surveyor", "onModuleLoad", "begin");
                // ensure that data provider onload is called as early as possible, to ensure all subsequent calls complete properly
                clientFactory.getDataProvider().onLoad(new Runnable() {
                    @Override
                    public void run() {
                        // setup controller with reference to main panel
                        new SurveyorAppController(new MainContentPanel());
                    }
                });
//                EventLogger.logEvent("org.wwarn.surveyor.client.Surveyor", "onModuleLoad", "end");
            }
        }, Table.PACKAGE);
    }

}
