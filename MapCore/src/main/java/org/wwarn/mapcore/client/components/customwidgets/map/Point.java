package org.wwarn.mapcore.client.components.customwidgets.map;

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

/**
 * A point on a two-dimensional plane. <br>
 * <br>
 * See <a href="https://developers.google.com/maps/documentation/javascript/reference#Point">Point API Doc</a>
 */
public class Point extends JavaScriptObject {

    /**
     * A point on a two-dimensional plane. use newInstance();
     */
    protected Point() {
    }

    /**
     * creates A point on a two-dimensional plane.
     *
     * @param x
     * @param y
     */
    public final static Point newInstance(double x, double y) {
        return createJso(x, y).cast();
    }

    private final static native JavaScriptObject createJso(double x, double y) /*-{
        return new $wnd.google.maps.Point(x, y);
    }-*/;

    /**
     * Compares two Points
     *
     * @param other
     */
    public final native boolean equals(Point other) /*-{
        return this.equals(other);
    }-*/;

    /**
     * Returns a string representation of this Point.
     */
    public final native String getToString() /*-{
        return this.toString();
    }-*/;

    /**
     * get X coordinate
     */
    public final native double getX() /*-{
        return this.x;
    }-*/;

    /**
     * get Y coordinate
     */
    public final native double getY() /*-{
        return this.y;
    }-*/;

}
