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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.Visualization;

import java.util.ArrayList;
import java.util.List;

/**
 * User: nigel
 * Date: 22/07/13
 * Time: 15:28
 * We need a way of testing with the Visualisation API
 * Using code from gwt visualization api test classes :
 * https://code.google.com/p/gwt-google-apis/source/browse/trunk/visualization/visualization/test/com/google/gwt/visualization/client/VisualizationTest.java
 * Notes: Must override getModuleName AND getVisualizationPackage
 */
public abstract class VisualizationTest extends GWTTestCase {
    public static final int ASYNC_DELAY_MS = 10 * 1000;

    /**
     * Removes all elements in the body, except scripts and iframes.
     */
    public static void cleanDom() {
        Element bodyElem = RootPanel.getBodyElement();

        List<Element> toRemove = new ArrayList<Element>();
        for (int i = 0, n = DOM.getChildCount(bodyElem); i < n; ++i) {
            Element elem = DOM.getChild(bodyElem, i);
            String nodeName = getNodeName(elem);
            if (!"script".equals(nodeName) && !"iframe".equals(nodeName)) {
                toRemove.add(elem);
            }
        }

        for (int i = 0, n = toRemove.size(); i < n; ++i) {
            DOM.removeChild(bodyElem, toRemove.get(i));
        }
    }

    /**
     * Extracts the value of a named parameter from a URL query string.
     *
     * @param url the URL to extract the parameter from
     * @param name the name of the parameter
     * @return the value of the parameter
     */
    public static native String getParameter(String url, String name) /*-{
        var spec = "[\\?&]" + name + "=([^&#]*)";
        var regex = new RegExp(spec);
        var results = regex.exec(url);
        if(results == null) {
            return "";
        } else {
            return results[1];
        }
    }-*/;

    private static native String getNodeName(Element elem) /*-{
        return (elem.nodeName || "").toLowerCase();
    }-*/;

    private boolean loaded = false;

    public VisualizationTest() {
        super();
    }


    @Override
    public String getModuleName() {
        throw new UnsupportedOperationException("Please Override getModuleName");
    }


    protected String getVisualizationPackage() {
        throw new UnsupportedOperationException("Please Override getVisualizationPackage");
    }

    /**
     * Loads the visualization API asynchronously and runs the specified test.
     * When the testRunnable method completes, the test is considered finished
     * successfully.
     *
     * @param testRunnable code to invoke when the API loadeded.
     *
     */
    protected void loadApi(final Runnable testRunnable) {
        loadApi(testRunnable, true);
    }

    /**
     * Loads the visualization API asynchronously and runs the specified test.
     *
     * @param testRunnable code to invoke when the API loadeded.
     * @param callFinishTest if <code>true</code>, the finishTest() method is
     *          called when the test completes. If <code>false</code>, the caller
     *          is responsible for ending the test.
     */
    protected void loadApi(final Runnable testRunnable,
                           final boolean callFinishTest) {
        loadApi(testRunnable, callFinishTest, ASYNC_DELAY_MS);
    }

    /**
     * Loads the visualization API asynchronously and runs the specified test.
     *
     * @param testRunnable code to invoke when the API loadeded.
     * @param callFinishTest if <code>true</code>, the finishTest() method is
     *          called when the test completes. If <code>false</code>, the caller
     *          is responsible for ending the test.
     * @param asyncDelayMs number of milliseconds to wait for the test to finish.
     */
    protected void loadApi(final Runnable testRunnable,
                           final boolean callFinishTest, final int asyncDelayMs) {
        if (loaded) {
            testRunnable.run();
        } else {
            VisualizationUtils.loadVisualizationApi(new Runnable() {
                public void run() {
                    loaded = true;
                    testRunnable.run();
                    if (callFinishTest) {
                        finishTest();
                    }
                }
            }, getVisualizationPackage());
            delayTestFinish(asyncDelayMs);
        }
    }

    /**
     * See <a href=
     * "http://code.google.com/apis/visualization/documentation/dev/events.html" >
     * Google Visualization API documentation</a> .
     *
     * @param viz - the Visualization to trigger the event on
     * @param s - a selection object.
     */
    protected <E extends Visualization<?>, Selectable> void triggerSelection(
            E viz, JsArray<Selection> s) {
        Selection.triggerSelection(viz, s);
    }
}
