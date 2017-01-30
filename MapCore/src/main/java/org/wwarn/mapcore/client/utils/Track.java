package org.wwarn.mapcore.client.utils;

/*
 * #%L
 * MapCore
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
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.http.client.URL;

/**
 * Based on APayne's original class
 * Updated to allow setting of trackerID and using the latest google tracking guidelines
 * https://developers.google.com/analytics/devguides/collection/gajs/methods/gaJSApi_gat#_gat._createTracker
 */
public class Track {
    private final String trackingAccount;

    /**
     * constructor - nothing to do
     */

    public Track() {
        trackingAccount = null;
    }

    public Track(String trackingAccount) {
        this.trackingAccount = trackingAccount;
    }

    public void trackEvent(String eventName){
        track(eventName);
    }

    public void trackEventWithGoogleAnalytics(String eventUrl){
        trackGoogleAnalytics(eventUrl);
    }

    /**
     * track an event
     *
     * @param historyToken
     */
    private static void track(String historyToken) {

        if (historyToken == null) {
            historyToken = "historyToken_null";
        }

        historyToken = URL.encode("/WWARN-GWT-Analytics/V1.0/" + historyToken);
        boolean hasErrored = false;
        try{
            trackGoogleAnalytics(historyToken);
        }catch (JavaScriptException e){
            hasErrored = true;
            GWT.log("Unable to track" ,e);
        }
        if(!hasErrored) GWT.log("Tracked " + historyToken);
    }

    /**
     * Trigger google analytic native js - included in the build
     * CHECK - DemoGoogleAnalytics.gwt.xml for -> <script src="../ga.js"/>
     * <p/>
     * http://code.google.com/intl/en-US/apis/analytics/docs/gaJS/gaJSApiEventTracking.html
     *
     * @param historyToken
     */
    private static native void trackGoogleAnalytics(String historyToken) throws JavaScriptException /*-{
        function TrackingException(message, error) {
            this.message = message;
            this.name = "TrackingException";
            this.error = error;
        }
        try {
            var trackingAccount = this.@org.wwarn.mapcore.client.utils.Track::trackingAccount;


            // setup tracking object with account
            var pageTracker = (trackingAccount)?$wnd._gat._createTracker(trackingAccount):$wnd._gat._getTrackerByName();

            pageTracker._setRemoteServerMode();

            // turn on anchor observing
            pageTracker._setAllowAnchor(true)

            // send event to google server
            pageTracker._trackPageview(historyToken);

        } catch (err) {
            // debug
            throw new TrackingException('FAILURE: to send in event to google analytics: ',  err);
        }

    }-*/;


}
