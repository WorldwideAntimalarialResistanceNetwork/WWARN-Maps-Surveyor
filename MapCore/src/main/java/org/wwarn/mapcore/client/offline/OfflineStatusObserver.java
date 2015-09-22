package org.wwarn.mapcore.client.offline;

/*
 * #%L
 * MapCore
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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import org.wwarn.mapcore.client.resources.Resources;

/**
 * A wrapper around https://github.com/HubSpot/offline to check for offline status
 * Would use http://caniuse.com/#feat=online-status, however, "online" does not always mean connection to the internet, it can also just mean connection to some network.
 * Need to determine access to our server, not just some network..
 */
public class OfflineStatusObserver {

    public static final String STATUS_ONLINE = "up";

    static{
        if (!isLoaded()) {
            String text = Resources.IMPL.offlineStatusScript().getText();
            ScriptInjector.fromString(text).setWindow(ScriptInjector.TOP_WINDOW).inject();
        }
    }


    public static native boolean isLoaded()/*-{
        if (typeof $wnd.Offline === "undefined" || $wnd.Offline === null) {
            return false;
        }
        return true;
    }-*/;

    private JavaScriptObject offlineStatusObserver;

    public OfflineStatusObserver() {

        OfflineStatusOptions offlineStatusOptions = (OfflineStatusOptions) OfflineStatusOptions.createObject();
        initialise(this, offlineStatusOptions);
    }

    public OfflineStatusObserver(OfflineStatusOptions offlineStatusOptions) {
        initialise(this, offlineStatusOptions);
    }


    public final native String check()
            throws RuntimeException /*-{
        return $entry($wnd.Offline.check());
    }-*/
    ;
    
    public boolean isOnline(){
        return this.check().equals(STATUS_ONLINE);
    }

    public final native String getState()
            throws RuntimeException /*-{
        return $entry($wnd.Offline.state);
    }-*/
    ;

    public static native void initialise(OfflineStatusObserver offlineStatusObserver, OfflineStatusOptions offlineStatusOptions)/*-{
        //$wnd.console.log("offlineStatusOptions");
        //$wnd.console.log(offlineStatusOptions);
        $wnd.Offline.options = offlineStatusOptions;
        offlineStatusObserver.@org.wwarn.mapcore.client.offline.OfflineStatusObserver::offlineStatusObserver = $wnd.Offline;
    }-*/;

    public static class OfflineStatusOptions extends JavaScriptObject{

        /**
         * {

         // Should we automatically retest periodically when the connection is down (set to false to disable).
         reconnect: {
         // How many seconds should we wait before rechecking.
         initialDelay: 3,

         // How long should we wait between retries.
         delay: (1.5 * last delay, capped at 1 hour)
         },


         }
         */
        protected OfflineStatusOptions() {
        }

        public final native boolean checkOnLoad()
                throws RuntimeException /*-{
            return this["checkOnLoad"] = (this["checkOnLoad"] || true);
        }-*/
        ;

        /**
         *  Should we check the connection status immediately on page load.
         *  defaults to true
         * @param checkOnLoad
         * @return
         * @throws RuntimeException
         */
        public final native OfflineStatusOptions checkOnLoad(boolean checkOnLoad)
                throws RuntimeException /*-{
            this["enabled"] = checkOnLoad;
            return this;
        }-*/
        ;

        public final native boolean interceptRequests()
                throws RuntimeException /*-{
            return this["interceptRequests"] = (this["interceptRequests"] || false);
        }-*/
        ;

        /**
         * Should we monitor AJAX requests to help decide if we have a connection.
         * defaults to false
         * @param interceptRequests
         * @return
         * @throws RuntimeException
         */
        public final native OfflineStatusOptions interceptRequests(boolean interceptRequests)
                throws RuntimeException /*-{
            this["interceptRequests"] = interceptRequests;
            return this;
        }-*/
        ;

        public final native boolean requests()
                throws RuntimeException /*-{
            return this["requests"] = (this["requests"] || false);
        }-*/
        ;


        /**
         * Should we store and attempt to remake requests which fail while the connection is down.
            default: false,
         * @param requests
         * @return
         * @throws RuntimeException
         */
        public final native OfflineStatusOptions requests(boolean requests)
                throws RuntimeException /*-{
            this["requests"] = requests;
            return this;
        }-*/
        ;

        /**
         * No snake game support
         * @return
         * @throws RuntimeException
         */
        public final native boolean game()
                throws RuntimeException /*-{
            return this["game"] = (this["game"] || false);
        }-*/
        ;

    }

}
