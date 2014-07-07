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

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.junit.client.GWTTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static junit.framework.Assert.assertEquals;

/**
 * Created by nigelthomas on 10/06/2014.
 */
public class ParseUtilTest{

    public static final int DATE_IN_MILLISECOND_SINCE_EPOCH = 1022886000;

    @Test
    public void testParseDateYearOnly() throws Exception {
        final Date date = DataType.ParseUtil.parseDateYearOnly("2002");
        final DateTimeFormat dateTimeFormat = DataType.ParseUtil.getDateFormatFrom(DataType.DATE_FORMAT_YEAR_ONLY);
        assertEquals("2002", dateTimeFormat.format(date));
    }

    @Test
    public void testParseDate() throws Exception {
        final Date date = DataType.ParseUtil.parseDate("17/02/2002", DataType.DATE_FORMAT_DAY_MONTH_YEAR, "01/01/1970");
        final DateTimeFormat dateTimeFormat = DataType.ParseUtil.getDateFormatFrom(DataType.DATE_FORMAT_DAY_MONTH_YEAR);
        assertEquals("17/02/2002", dateTimeFormat.format(date));
    }

    public String getModuleName() {
        return "org.wwarn.surveyor.surveyorJUnit";
    }
}
