package org.wwarn.localforage.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.core.shared.GWT;

/**
 * Data API : http://mozilla.github.io/localForage/#data-api
 * -getItem(key, successCallback)
 * -setItem(key, value, successCallback)
 * -removeItem(key, successCallback)
 * -clear(successCallback)
 * -length(successCallback)
 * -key(keyIndex, successCallback)
 * -keys(successCallback)
 * x iterate(iteratorCallback, successCallback)
 */
public class LocalForage {

    static {
        if (!isLoaded()) {
            load();
        }
    }

    /**
     * From JSArrayString implementation https://code.google.com/p/gwt-in-the-air/source/browse/trunk/src/net/ltgt/gwt/jscollections/client/JsArrays.java
     *
     * @param value
     * @return
     */
    private static native String[] reinterpretCast(JsArrayString value) /*-{
        return value;
    }-*/;

    static <T> void fireCallback(LocalForageCallback successCallback, Boolean err, T value) {
        if (successCallback == null) {
            return;
        }
        if (err == null) err = false;
        if (value instanceof String && value.equals("null")) value = null;
        successCallback.onComplete(err, value);
    }

    static JavaScriptObject fireInteratorCallback(LocalForageIteratorCallback localForageIteratorCallback, String value, String key, String iterationNumber) {
        if (value instanceof String && value.equals("null")) value = null;
        return localForageIteratorCallback.iteratorCallback(value, key, Integer.valueOf(iterationNumber));
    }

    static void fireCallbackString(LocalForageCallback successCallback, Boolean err, String value) {
        fireCallback(successCallback, null, (value));
    }

    static void fireCallbackInt(LocalForageCallback successCallback, Boolean err, String value) {
        fireCallback(successCallback, null, Integer.parseInt(value));
    }

    static void fireCallbackArrayStr(LocalForageCallback successCallback, Boolean err, JsArrayString values) {
        fireCallback(successCallback, null, toArray(values));
    }

    static void fireCallback(LocalForageCallback successCallback) {
        fireCallback(successCallback, null, new String());
    }

    /**
     * From JSArrays implementation https://code.google.com/p/gwt-in-the-air/source/browse/trunk/src/net/ltgt/gwt/jscollections/client/JsArrays.java
     *
     * @param values
     * @return
     */
    static String[] toArray(JsArrayString values) {
        if (GWT.isScript()) {
            return reinterpretCast(values);
        } else {
            int length = values.length();
            String[] ret = new String[length];
            for (int i = 0, l = length; i < l; i++) {
                ret[i] = values.get(i);
            }
            return ret;
        }
    }


    public native void getItem(String key, LocalForageCallback successCallback) /*-{
        var successCallbackFn = $entry(function (err, val) {
            @org.wwarn.localforage.client.LocalForage::fireCallbackString(Lorg/wwarn/localforage/client/LocalForageCallback;Ljava/lang/Boolean;Ljava/lang/String;)(successCallback, err, val)
        });
        $wnd.localforage.getItem(key, successCallbackFn);
    }-*/;

    public native void setItem(String key, String value, LocalForageCallback successCallback) /*-{
        var successCallbackFn = $entry(function (err, val) {
            @org.wwarn.localforage.client.LocalForage::fireCallbackString(Lorg/wwarn/localforage/client/LocalForageCallback;Ljava/lang/Boolean;Ljava/lang/String;)(successCallback, err, val);
        });
        $wnd.localforage.setItem(key, value, successCallbackFn);
    }-*/;

    public native void removeItem(String key, LocalForageCallback successCallback) /*-{
        var successCallbackFn = $entry(function (err, val) {
            @org.wwarn.localforage.client.LocalForage::fireCallbackString(Lorg/wwarn/localforage/client/LocalForageCallback;Ljava/lang/Boolean;Ljava/lang/String;)(successCallback, err, val)
        });
        $wnd.localforage.removeItem(key, successCallbackFn);
    }-*/;

    public native void clear(LocalForageCallback successCallback) /*-{
        var successCallbackFn = $entry(function (val) {
            @org.wwarn.localforage.client.LocalForage::fireCallback(Lorg/wwarn/localforage/client/LocalForageCallback;)(successCallback);
        });
        $wnd.localforage.clear(successCallbackFn);
    }-*/;

    public native void length(LocalForageCallback<Integer> successCallback) /*-{
        var successCallbackFn = $entry(function (err, numberOfKeys) {
            @org.wwarn.localforage.client.LocalForage::fireCallbackInt(Lorg/wwarn/localforage/client/LocalForageCallback;Ljava/lang/Boolean;Ljava/lang/String;)(successCallback, err, numberOfKeys + '');
        });
        $wnd.localforage.length(successCallbackFn);
    }-*/;

    public native void key(Integer keyIndex, LocalForageCallback<String> successCallback) /*-{
        var successCallbackFn = $entry(function (err, keyName) {
            @org.wwarn.localforage.client.LocalForage::fireCallbackString(Lorg/wwarn/localforage/client/LocalForageCallback;Ljava/lang/Boolean;Ljava/lang/String;)(successCallback, err, keyName + '');
        });
        $wnd.localforage.key(keyIndex, successCallbackFn);
    }-*/;

    public native void keys(LocalForageCallback<String[]> successCallback) /*-{
        var successCallbackFn = $entry(function (err, keys) {
            @org.wwarn.localforage.client.LocalForage::fireCallbackArrayStr(Lorg/wwarn/localforage/client/LocalForageCallback;Ljava/lang/Boolean;Lcom/google/gwt/core/client/JsArrayString;)(successCallback, err, keys);
        });
        $wnd.localforage.keys(successCallbackFn);
    }-*/;

    /**
     * Iterate over all value/key pairs in datastore.
     * ** WARNING** note that instead of returning undefined to continue, return null instead, if return value is not null, iteration is halted.
     * iterate supports early exit by returning non undefined value inside iteratorCallback callback. Resulting value will be passed to successCallback as the result of iteration.
     *
     * @param localForageIteratorCallback local forage iterator callback
     * @param successCallback success call back function
     */
    @Deprecated
    public native void iterate(LocalForageIteratorCallback localForageIteratorCallback, LocalForageCallback<String[]> successCallback) /*-{


        var successCallbackFn = $entry(function (err, keys) {
            @org.wwarn.localforage.client.LocalForage::fireCallbackArrayStr(Lorg/wwarn/localforage/client/LocalForageCallback;Ljava/lang/Boolean;Lcom/google/gwt/core/client/JsArrayString;)(successCallback, err, keys);
        });
        // Notice that the reference to the exported method has been wrapped in a call to the $entry function. This implicitly-defined function ensures that the Java-derived method is executed with the uncaught exception handler installed and pumps a number of other utility services. The $entry function is reentrant-safe and should be used anywhere that GWT-derived JavaScript may be called into from a non-GWT context.
        // Source http://www.gwtproject.org/doc/latest/DevGuideCodingBasicsJSNI.html
        // $entry ensures all undefined return values are converted to null a key part of preserving contract for java calls for javascript functions -> https://code.google.com/p/google-web-toolkit/wiki/NullIsUndefined
        var iteratorCallbackFn = $entry(function (value, key, iterationNumber) {
            return @org.wwarn.localforage.client.LocalForage::fireInteratorCallback(Lorg/wwarn/localforage/client/LocalForageIteratorCallback;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(localForageIteratorCallback, value, key, iterationNumber + "");
        });
        $wnd.localforage.iterate(
            function(v,k,i){
                var n = iteratorCallbackFn(v,k,i);
                // since GWT cannot handle returning undefined https://code.google.com/p/google-web-toolkit/wiki/NullIsUndefined
                // test for null and return undefined instead, hence preserving the intention of the class, but not the exact contract
                if(n === null) { return undefined;}
                else return n;
            }
            , successCallbackFn)
    }-*/;

    public native boolean isSupported() /*-{
        return !($wnd.localforage === null || typeof $wnd.localforage === "undefined");
    }-*/;


    /**
     * Loads the offline library. You normally never have to do this manually
     */
    public static void load() {
        if (!isLoaded()) {
            ScriptInjector.fromString(LocalForageResources.INSTANCE.js().getText()).setWindow(ScriptInjector.TOP_WINDOW).inject();
        }
    }

    public static native boolean isLoaded()/*-{
        if (typeof $wnd.localforage === "undefined" || $wnd.localforage === null) {
            return false;
        }
        return true;
    }-*/;

}
