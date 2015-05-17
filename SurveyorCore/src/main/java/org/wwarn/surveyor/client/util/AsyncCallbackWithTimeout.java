package org.wwarn.surveyor.client.util;

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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.wwarn.surveyor.client.event.ExceptionEvent;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;

import java.sql.Timestamp;
import java.util.Date;


/**
 * Wrapper for AsyncCallback to implement a timeout on AsyncCallbacks.
 * Throws a timeout error after the specified (or DEFAULT_TIMEOUT) time elapses.
 * Note: The timer starts on construction of the class, not when the RPC is sent.
 *
 * User: raok
 * Date: 16-Mar-2010
 * Time: 14:28:31
 */
public abstract class AsyncCallbackWithTimeout<T> implements AsyncCallback<T> {

    final private Timer timer;
    public static Timestamp latestServerCallTimeStamps = null;
    protected Timestamp timeSend= null;
    private static final String TIMEOUT_ERROR = "An action timed out. Please try refreshing your browser.";
    private static final int DEFAULT_TIMEOUT = 120000; // 2 minutes
    private boolean hasTimedOut = false;


    public AsyncCallbackWithTimeout() {
        this(DEFAULT_TIMEOUT);
    }

    public AsyncCallbackWithTimeout(int timeoutMillis) {

        //registering latest server call timestamp
        timeSend = new Timestamp(new Date().getTime());
        latestServerCallTimeStamps = timeSend;
        //reset
        SurveyorUncaughtExceptionHandler.alertMsg = true;
        GWT.log("Registering latest server request with time stamp ..." + timeSend.toString());

        GWT.log("AsyncCallbackWithTimeout constructor, timeoutMillis: " + timeoutMillis);
        timer = new Timer() {

            @Override
            public void run() {
                onTimeout();
            }

        };

        timer.schedule(timeoutMillis);
    }

    private void onTimeout() {
        GWT.log("AsyncCallbackWithTimeout::onTimeout");
        final TimedOutException timeOutException = new TimedOutException(TIMEOUT_ERROR);

        //display error only for latest timeout event
        if (latestServerCallTimeStamps!=null && timeSend.before(latestServerCallTimeStamps)) {

            GWT.log("----------------- switch off alert msg ");
            SurveyorUncaughtExceptionHandler.alertMsg = false;
            SimpleClientFactory.getInstance().getEventBus().fireEvent(new ExceptionEvent(timeOutException));
        }
        hasTimedOut = true;
        onTimeOutOrOtherFailure(timeOutException);

    }

    abstract public void onTimeOutOrOtherFailure(Throwable caught);

    abstract public void onNonTimedOutSuccess(T result);


    @Override
    public void onFailure(Throwable caught) {

        if (!hasTimedOut) {
            GWT.log("AsyncCallbackWithTimeout::onFailure, hasNotTimedOut");
            SimpleClientFactory.getInstance().getEventBus().fireEvent(new ExceptionEvent(new IllegalStateException(caught)));

            timer.cancel();
            onTimeOutOrOtherFailure(caught);

        }

    }

    @Override
    public void onSuccess(T result) {

        if (!hasTimedOut) {
            GWT.log("AsyncCallbackWithTimeout::onNonTimedOutSuccess, hasNotTimedOut");

            timer.cancel();
            onNonTimedOutSuccess(result);

        }

    }

}

