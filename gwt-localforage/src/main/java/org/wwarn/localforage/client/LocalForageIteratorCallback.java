package org.wwarn.localforage.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * function(value, key, iterationNumber)
 */
public interface LocalForageIteratorCallback {
    /**
     * iteratorCallback is called once for each pair, with the following arguments:
     * iterate supports early exit by returning non undefined value inside iteratorCallback callback. Resulting value will be passed to successCallback as the result of iteration.
     * @param value
     * @param key
     * @param iterationNumber  - one-based number
     */
    JavaScriptObject iteratorCallback(String value, String key, Integer iterationNumber);
}
