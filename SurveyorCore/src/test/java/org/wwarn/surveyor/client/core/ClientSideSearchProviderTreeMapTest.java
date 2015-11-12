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

import com.google.gwt.i18n.shared.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class ClientSideSearchProviderTreeMapTest {

    private TreeMap<Integer, BitSet> map = new TreeMap<>();
    private TreeMap<String, BitSet> stringmap = new TreeMap<>();
    private TreeMap<String, BitSet> dateMap = new TreeMap<>();

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
    public void testDateRange() throws Exception {

        final String[] dummyDateData = {
                "1970-01-01T00:00:00.000+01:00",
                "2001-01-08T00:00:00.000+00:00",
                "2002-02-17T00:00:00.000+00:00",
                "2008-10-01T00:00:00.000+01:00",
                "2009-01-01T00:00:00.000+00:00",
                "2009-07-10T00:00:00.000+01:00",
                "2010-01-01T00:00:00.000+00:00",
                "2010-01-13T00:00:00.000+00:00",
                "2010-02-15T00:00:00.000+00:00",
                "2010-05-11T00:00:00.000+01:00",
                "2010-06-10T00:00:00.000+01:00",
                "2011-01-13T00:00:00.000+00:00",
                "2011-10-01T00:00:00.000+01:00",
                "2012-04-02T00:00:00.000+01:00"};
        assertEquals(14, dummyDateData.length);
        for (String date : dummyDateData) {
            dateMap.put(date, null);
        }
        final DateTimeFormat dateTimeFormat = DataType.ParseUtil.getDateFormatFrom(DataType.ISO_DATE_FORMAT);
        final String startDate = dateTimeFormat.format(DataType.ParseUtil.parseDateInEnglishDayMonthYearFormat("01/01/2001"));
        final String endDate = dateTimeFormat.format(DataType.ParseUtil.parseDateInEnglishDayMonthYearFormat("31/12/2003"));
        final NavigableMap navigableMap = dateMap.subMap(startDate, true, endDate, true);
        assertNotNull(navigableMap);
        assertTrue(navigableMap.size() > 0);
        assertEquals(2, navigableMap.size());
    }

    @Test
    public void testDateYearOnlyRange() throws Exception {

        final String[] dummyDateData = {
                "00000002001",
                "00000002002",
                "00000002007",
                "00000002008",
                "00000002009",
                "00000002010",
                "00000002011",
                "00000002012"};
        assertEquals(8, dummyDateData.length);
        for (String date : dummyDateData) {
            dateMap.put(date, null);
        }
        final DateTimeFormat dateTimeFormat = DataType.ParseUtil.getDateFormatFrom(DataType.ISO_DATE_FORMAT);
        final String startDate = ClientSideSearchDataProvider.leftPaddedInteger(2001);
        final String endDate = ClientSideSearchDataProvider.leftPaddedInteger(2007);
        final NavigableMap navigableMap = dateMap.subMap(startDate, true, endDate, true);
        assertNotNull(navigableMap);
        assertTrue(navigableMap.size() > 0);
        assertEquals(3, navigableMap.size());
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