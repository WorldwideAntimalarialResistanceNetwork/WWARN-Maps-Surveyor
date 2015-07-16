package org.wwarn.surveyor.server.core;

/*
 * #%L
 * SurveyorCore
 * %%
 * Copyright (C) 2013 - 2015 University of Oxford
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

import com.allen_sauer.gwt.log.client.Log;
import org.wwarn.mapcore.client.utils.StringUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.*;

/**
 * A servlet listener that helps monitors files changes and inform lucene search service,
 * expects a context attribute
 *     <context-param>
 *     <param-name>org.wwarn.surveyor.server.core.FileChangeContextListener.monitoredFile</param-name>
 *     <param-value>data/feverseriesPublications.json</param-value>
 *     </context-param>
 * code from http://stackoverflow.com/a/4908012
 */
public class FileChangeContextListener implements ServletContextListener {
    public static final String CONTEXT_ID = "FileChangeMonitor_EXECUTOR";
    public static final String CONTEXT_PARAMETER = "org.wwarn.surveyor.server.core.FileChangeContextListener.monitoredFile";
    private ScheduledExecutorService executorService;
    private FileChangeMonitor fileChangeMonitor = FileChangeMonitor.getInstance();
    private void assertFilePathExists(ServletContext context, String path, String messageOnFail) {
        if(!(new File(path)).canRead()){
            ServletContext servletContext = context;
            throw new IllegalArgumentException(messageOnFail + path + "current path relative root"+(servletContext.getRealPath(path)));
        }
    }
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext context = servletContextEvent.getServletContext();
        final String monitoredFile = (String) context.getInitParameter(CONTEXT_PARAMETER);
        if(StringUtils.isEmpty(monitoredFile)) throw new IllegalArgumentException("monitoredFile is empty, please add attribute to web.xml");
        final String monitoredFileRealPath = context.getRealPath(monitoredFile);
        assertFilePathExists(context, monitoredFileRealPath, "jsonPath invalid:");

        executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                // ensure daemon thread is used to monitor files, this since the executor service will block JVM
                // on nondaemon threads, this may prevent tomcat from existing or reloading gracefully
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(false);
                t.setName(getClass().getName()+"::DaemonThread-"+(new Date()).getTime());
                return t;
            }
        });

        // executes task in a daemon thread
        executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.debug("FileChangeContextListener::contextInitialized", "Index monitor setup starting");
                    fileChangeMonitor.initSynchronous(Paths.get(monitoredFileRealPath));
                } catch (Throwable e) {// must catch all exceptions, otherwise, subsequent calls to fails
                    if(e instanceof Exception) {
                        logExceptions((Exception) e);
                    }else logExceptions(new IllegalStateException(e));
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        context.setAttribute(CONTEXT_ID, executorService);
    }

    private void logExceptions(Exception e) {
        Log.error("FileChangeContextListener::contextInitialized", "Unable to initialize file change monitor", e);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        ServletContext context = servletContextEvent.getServletContext();
        System.out.println("The ServletContextListener has been shutdown.");
        if(executorService!=null) {
            executorService.shutdownNow(); // or process/wait until all pending jobs are done
        }
    }}
