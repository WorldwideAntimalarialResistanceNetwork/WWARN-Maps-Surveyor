package org.wwarn.mapcore.client.offline;

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

    public static native boolean initialise(OfflineStatusObserver offlineStatusObserver, OfflineStatusOptions offlineStatusOptions)/*-{
        $wnd.console.log("offlineStatusOptions");
        $wnd.console.log(offlineStatusOptions);
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
