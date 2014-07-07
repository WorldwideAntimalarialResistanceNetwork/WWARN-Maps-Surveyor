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

import junit.framework.TestCase;

/**
* User: richardc
* Date: 27/10/11
* Time: 11:31
*/
public class TestRange extends TestCase {

    //handle sample size of zero
    public void testInvalidSampleException() {
        try {
            Range.calcCI95(0.0, -5);
            fail("IllegalArgumentException should have been thrown.");
        } catch (IllegalArgumentException e) {
            //pass
        } catch (Exception e) {
            fail("Unknown exception thrown.");
        }

    }

    //test accepts percentageValues above 0 and 1 only
    public void testInValidPercentageValuesException() {

        try {
            Range.calcCI95(-0.1, 5);
            fail("IllegalArgumentException should have been thrown.");
        } catch (IllegalArgumentException e) {
            //pass
        } catch (Exception e) {
            fail("Unknown exception thrown.");
        }

        try {
            Range.calcCI95(100.1, 5);
            fail("IllegalArgumentException should have been thrown.");
        } catch (IllegalArgumentException e) {
            //pass
        } catch (Exception e) {
            fail("Unknown exception thrown.");
        }

    }

    public void testLimits() {

        for (double pV = 0.0; pV <= 100.0; ++pV) {

            Range<Double> range = Range.calcCI95(pV, 100);

            //test upper's upperlimit is 100
            assertTrue("failed for pV: " + pV + "  range: " + range, range.upper <= 100.0);

            //test lower's lowerlimit is 0
            assertTrue("failed for pV: " + pV + " range: " + range, range.lower >= 0.0);

            //test upperLimit equal to or above value
            assertTrue("failed for pV: " + pV + " range: " + range, range.upper >= pV);

            //lower limit equal or below value
            assertTrue("failed for pV: " + pV + " range: " + range, range.lower <= pV);

        }


    }
}



