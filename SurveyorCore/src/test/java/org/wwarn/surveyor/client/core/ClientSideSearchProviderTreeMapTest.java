package org.wwarn.surveyor.client.core;

/*
 * #%L
 * SurveyorCore
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

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class ClientSideSearchProviderTreeMapTest {

    private TreeMap<Integer, BitSet> map = new TreeMap<>();
    private TreeMap<String, BitSet> stringmap = new TreeMap<>();

    @Before
    public void setUp() throws Exception {

        for (int i = 200; i < 210; i++) {
            map.put(i, null);
            stringmap.put(leftPaddedInteger(i), null);
        }

        for (int i = 0; i < 100; i++) {
            map.put((i), null);
            stringmap.put(leftPaddedInteger(i), null);
        }
    }

    private String leftPaddedInteger(int i) {
        return ClientSideSearchDataProvider.leftPaddedInteger(i);
    }

    @Test
    public void testSortOrderInteger() throws Exception {
        final NavigableMap<Integer, BitSet> stringBitSetNavigableMap = map.tailMap((200), true);
        System.out.println(stringBitSetNavigableMap.keySet());
        assertEquals(10, stringBitSetNavigableMap.size());
    }

    @Test()
    public void testSortOrderString() throws Exception {
        final NavigableMap<String, BitSet> stringBitSetNavigableMap = stringmap.tailMap(leftPaddedInteger(200), true);
        System.out.println(stringBitSetNavigableMap.keySet());
        assertEquals(10, stringBitSetNavigableMap.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSortOrderStringWithNegativeIntegers() throws Exception {
        for (int i = -200; i < 0; i++) {
            map.put(i, null);
            stringmap.put(leftPaddedInteger(i), null);
        }
        final NavigableMap<String, BitSet> stringBitSetNavigableMap = stringmap.tailMap(leftPaddedInteger(200), true);
        System.out.println(stringBitSetNavigableMap.keySet());
        assertEquals(10, stringBitSetNavigableMap.size());
    }
}