package org.wwarn.mapcore.client.common.types;

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


import com.google.gwt.core.shared.GWT;

/**
 * User: raok
 * Date: 16-Feb-2010
 * Time: 15:38:24
 */
public class Range<T> {

    public T lower;
    public T upper;
    private final  static Double zStdDev = 1.95996;
    private final  static Double zStdDevSqrd = zStdDev*zStdDev;

    public Range(T lower, T upper) {

        this.lower = lower;
        this.upper = upper;

    }

    public Range() {
    }

    /*
    * Calculate CI95% value using the no continuity correction formula found here: http://faculty.vassar.edu/lowry/prop1.html
    */
    public static Range<Double> calcCI95(Double percentageValue, Integer sampleSize) {
//        GWT.log("calcCI95", null);
        GWT.log("percentageValue: " + percentageValue.toString(), null);
        GWT.log("sampleSize: " + sampleSize, null);
        if (sampleSize==0) {
            return null;
        }
        if ( (sampleSize < 0) ||(percentageValue < 0.0) || (percentageValue > 100.0) ) {
            throw new IllegalArgumentException("sampleSize < 0, percentageValue < 0.0, or percentageValue > 100.0");
        }

        //convert percentageValue to ratio
        Double ratio = percentageValue/100.0;
        Double oneMinusRatio = 1.0 - ratio;

        Double sqrtSD = Math.sqrt(zStdDevSqrd + (4*sampleSize*ratio*oneMinusRatio));
        Double denom = 2*(sampleSize + zStdDevSqrd);

        Double lowerLimit = ((2*sampleSize*ratio) + zStdDevSqrd - (zStdDev *sqrtSD))/denom;

        Double upperLimit = ((2*sampleSize*ratio) + zStdDevSqrd + (zStdDev *sqrtSD))/denom;

        //convert back to percentages, to 1 d.p.
        lowerLimit = Math.rint(lowerLimit*1000)/10.0;
        upperLimit = Math.rint(upperLimit*1000)/10.0;

        if(lowerLimit<0.0) {
            lowerLimit = 0.0;
        }
        if(upperLimit>100.0) {
            upperLimit = 100.0;
        }

//        GWT.log("lowerLimit: " + lowerLimit.toString(), null);
//        GWT.log("upperLimit: " + upperLimit.toString(), null);
        return new Range<Double>(lowerLimit, upperLimit);
    }

    public String toString() {
        return "[" + lower + "-" + upper + "]";
    }

    public static String calcCI95OneDecimalString(Double percentageValue, Integer sampleSize) {
        Range range = Range.calcCI95(percentageValue,sampleSize);

        if (range !=null) {
            Double lower = (Double) range.lower;
            Double upper = (Double) range.upper;
            return "[" + lower + "-" + upper + "]";
        } else {
            return "";
        }
    }
}
