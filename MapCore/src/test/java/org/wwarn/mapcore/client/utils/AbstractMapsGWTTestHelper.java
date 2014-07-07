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
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.maps.client.LoadApi.LoadLibrary;

/**
 * Test helper for common actions taken when testing maps code
 * User: nigel
 * Date: 09/08/13
 * Time: 12:20
 * @see <a href="https://github.com/branflake2267/GWT-Maps-V3-Api/tree/a681c54d3b3bd07178961218d70647a4fba81378/gwt-maps-api/src/test/java/com/google/gwt/maps/client/AbstractMapsGWTTestHelper.java">Code sourced from test helper for maps</a>
 */
public abstract class AbstractMapsGWTTestHelper extends GWTTestCase {
    private int asyncDelayMs = 30000;
    private final double equalsEpsilon = 1e-3;
    private boolean sensor = false;

    /**
     * Runs the test with libraries defined by the {@link #getLibraries()} override loaded and fails if not complete by
     * {@link #getAsyncDelayMs()}.<br>
     * <br>
     * <b>NOTE:</b> You must call {@link #finishTest()} or test will fail.
     *
     * @param test code to run
     */
    public final void asyncLibTest(Runnable test) {
        // handle the nulls
        LoadLibrary[] libs = getLibraries();
        if (libs == null) {
            libs = new LoadLibrary[] {};
        }
        asyncLibTest(test, libs);
    }

    /**
     * Runs the test with the given libraries loaded and fails if not complete by {@link #getAsyncDelayMs()}.<br>
     * <br>
     * <b>NOTE:</b> You must call {@link #finishTest()} or test will fail.
     *
     * @param test code to run
     * @param libs libraries to have loaded
     */
    public final void asyncLibTest(Runnable test, LoadLibrary... libs) {
        // pack
        ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadLibrary>();
        loadLibraries.addAll(Arrays.asList(libs));

        // run
        LoadApi.go(test, loadLibraries, isSensor());

        // ensure expiration is does not reach finishTest()
        delayTest();
    }

    public final void delayTest() {
        delayTestFinish(getAsyncDelayMs());
    }

    /**
     * Get delay to wait until failing the test as incomplete
     *
     * @return
     */
    public final int getAsyncDelayMs() {
        return asyncDelayMs;
    }

    /**
     * Returns the libraries that will be loaded before the test is run if the no library method overload is called.
     *
     * @return
     */
    public abstract LoadLibrary[] getLibraries();

    /**
     * Is the test with run as with a device sensor
     *
     * @return
     */
    public final boolean isSensor() {
        return sensor;
    }

    public final void setAsyncDelayMs(int asyncDelayMs) {
        this.asyncDelayMs = asyncDelayMs;
    }

    public final void setSensor(boolean sensor) {
        this.sensor = sensor;
    }

    public final double getEqualsEpsilon() {
        return equalsEpsilon;
    }
}
