package org.wwarn.surveyor.client.core;

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

import com.google.gwt.junit.client.GWTTestCase;
import org.junit.Test;

/**
 * User: nigelthomas
 * Date: 22/10/2013
 * Time: 14:14
 */
public class GwtTestBitSetImpl extends GWTTestCase {
    BitSet bitSet = null;

    public String getModuleName() {
        return "org.wwarn.surveyor.surveyorJUnit";
    }

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        bitSet = new BitSet();
        setupDefaultData();
    }

    public void setupDefaultData(){

    }

    @Test
    public void testInitialise() throws Exception {
        assertNotNull(bitSet);
        assertNotNull(bitSet.getBitset());
    }

    @Test
    public void testGetSetClear() throws Exception {
        bitSet = new BitSet();
        assertFalse(bitSet.get(1));
        bitSet.set(1);
        assertTrue(bitSet.get(1));
        bitSet.clear(1);
        assertFalse(bitSet.get(1));
    }

    public void testClear() throws Exception {
        BitSet s = bitSet;
        for (int i = 0; i < 13; i++) {
            s.set(i);
        }
        assertTrue(s.get(0)); // true, as expected.

        s.clear(0);

        assertFalse(s.get(0)); // true, not good!
    }

    @Test
    public void testLength() throws Exception {
        final int expected = 6000;
        bitSet.set(5999); // zero based index, set position 6000
        assertEquals(expected, bitSet.length());
    }

//    @Test
//    public void testWordLength() throws Exception {
//        bitSet = new BitSet();
//        bitSet.set(1);
//        assertEquals(1, bitSet.wordLength());
//
//    }

    @Test
    public void testCardinality() throws Exception {
        bitSet = new BitSet();
        bitSet.set(100);
        assertEquals(1, bitSet.cardinality());
        bitSet.set(0);
        assertEquals(2, bitSet.cardinality());
    }

    @Test
    public void testToString() throws Exception {
        bitSet = new BitSet();
        bitSet.set(0);
        assertEquals("{0}", bitSet.toString());
        bitSet.set(10);
        assertEquals("{0,10}", bitSet.toString());
    }

    @Test
    public void testToBinaryString() throws Exception {
        bitSet = new BitSet();
        bitSet.set(0);
        assertEquals("1", bitSet.toBinaryString());
        bitSet.set(10);
        assertEquals("10000000001", bitSet.toBinaryString());
    }

    @Test
    public void testOr() throws Exception {
        bitSet = new BitSet();
        bitSet.set(0);
        BitSet baseBitset = new BitSet();
        baseBitset.set(2);
        assertEquals("{2}", baseBitset.toString());
        baseBitset.or(bitSet);
        assertEquals("{0,2}", baseBitset.toString());
    }

    @Test
    public void testAnd() throws Exception {
        bitSet = new BitSet();
        bitSet.set(0);
        bitSet.set(1);
        bitSet.set(2);
        bitSet.set(3);
        BitSet baseBitset = new BitSet();
        baseBitset.set(1);
        baseBitset.set(2);
        assertEquals("{1,2}", baseBitset.toString());
        baseBitset.and(bitSet);
        assertEquals("{1,2}", baseBitset.toString());
    }

    @Test
    public void testXOr() throws Exception {
        bitSet = new BitSet();
        bitSet.set(0);
        bitSet.set(1);
        bitSet.set(2);
        bitSet.set(3);
        BitSet baseBitset = new BitSet();
        baseBitset.set(1);
        baseBitset.set(2);
        assertEquals("{1,2}", baseBitset.toString());
        baseBitset.xor(bitSet);
        assertEquals("{0,3}", baseBitset.toString());
    }

    @Test
    public void testAndNot() throws Exception {
        //Clears all of the bits in this BitSet whose corresponding bit is set in the specified BitSet.
        bitSet = new BitSet();
        BitSet bs = bitSet;
        for (int i = 0; i <= 7; i++) {
             bitSet.set(i);
        }
        bs.clear(5);
        BitSet bs2 = new BitSet();
        bs2.set(2);
        bs2.set(3);
        bs.andNot(bs2);
        assertEquals("Incorrect bitset after andNot",
                "{0,1,4,6,7}", bs.toString());
        bs = new BitSet(0);
        bs.andNot(bs2);
        assertEquals("Incorrect size", 0, bs.length());
    }
}
