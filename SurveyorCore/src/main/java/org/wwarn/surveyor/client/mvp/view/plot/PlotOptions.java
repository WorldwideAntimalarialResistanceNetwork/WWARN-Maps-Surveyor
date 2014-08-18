package org.wwarn.surveyor.client.mvp.view.plot;

/*
 * #%L
 * SurveyorCore
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

import org.wwarn.surveyor.client.core.RecordList;

public class PlotOptions {
    private final String x;
    private final String y;
    private final RecordList data;
    private final Geometry geom;
    private final String xlab;
    private final String ylab;
    private final String main;
    private final String sub;

    /**
     * @param x Specifies the variables placed on the horizontal axis.
     * @param y Specifies the variables placed on the vertical axis. For univariate plots (for example, histograms), omit y
     * @param data Specifies a data frame
     * @param geom Specifies the geometric objects that define the graph type. The geom option is expressed as a character vector with one or more entries. geom values include "point", "smooth", "boxplot", "line", "histogram", "density", "bar", and "jitter".
     * @param xlab Horizontal axis labels
     * @param ylab Vertical axis labels
     * @param main Chart title
     * @param sub  Sub title
     */
    public PlotOptions(String x, String y, RecordList data, Geometry geom, String xlab, String ylab, String main, String sub) {
        this.x = x;
        this.y = y;
        this.data = data;
        this.geom = geom;
        this.xlab = xlab;
        this.ylab = ylab;
        this.main = main;
        this.sub = sub;
    }

    public static class Builder{

        private String x;
        private String y;
        private RecordList data;
        private Geometry geom;
        private String xlab;
        private String ylab;
        private String main;
        private String sub;

        public Builder setX(String x) {
            this.x = x;
            return this;
        }

        public Builder setY(String y) {
            this.y = y;
            return this;
        }

        public Builder setData(RecordList data) {
            this.data = data;
            return this;
        }

        public Builder setGeom(Geometry geom) {
            this.geom = geom;
            return this;
        }

        public Builder setXlab(String xlab) {
            this.xlab = xlab;
            return this;
        }

        public Builder setYlab(String ylab) {
            this.ylab = ylab;
            return this;
        }

        public Builder setMain(String main) {
            this.main = main;
            return this;
        }

        public Builder setSub(String sub) {
            this.sub = sub;
            return this;
        }

        public PlotOptions createPlotOptions() {
            return new PlotOptions(x, y, data, geom, xlab, ylab, main, sub);
        }
    }

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }

    public RecordList getData() {
        return data;
    }

    public Geometry getGeom() {
        return geom;
    }

    public String getXlab() {
        return xlab;
    }

    public String getYlab() {
        return ylab;
    }

    public String getMain() {
        return main;
    }

    public String getSub() {
        return sub;
    }

    public static enum Geometry{
        POINT, SMOOTH, BOXPLOT, LINE, HISTOGRAM, DENSITY, BAR, PIE, JITTER /*, SCATTERPLOT, PIECHART **/;
    }
}
