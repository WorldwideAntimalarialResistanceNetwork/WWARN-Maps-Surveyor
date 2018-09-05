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
import com.google.gwt.i18n.shared.DefaultDateTimeFormatInfo;
import com.google.gwt.user.client.rpc.IsSerializable;
import org.wwarn.mapcore.client.utils.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds recognised types in the system
 * String, Coordinate, Integer, Date
 * User: nigel
 * Date: 19/07/13
 * Time: 14:22
 */
public enum DataType implements IsSerializable {
    String,
    CoordinateLat,
    CoordinateLon,
    Integer,
    Boolean,
    /**
     *ISO date format
     */
    Date,
    DateYear;
    public static String ISO_DATE_FORMAT =  "yyyy-MM-dd'T'HH:mm:ss.SSSZZZ";
    public static final String DATE_FORMAT_YEAR_ONLY = "yyyy";
    public static final String DATE_FORMAT_DAY_MONTH_YEAR = "dd/MM/yyyy";
    public static final String DEFAULT_DATE = "01/01/1970";

    static public class ParseUtil {

        private static DefaultDateTimeFormatInfo info = new DefaultDateTimeFormatInfo();

        public static Date parseDateInEnglishDayMonthYearFormat(String value) {
            return getDateFormatFrom(DATE_FORMAT_DAY_MONTH_YEAR).parse(value);
        }

        @Deprecated
        public static Date parseDateYearOnly(String value) {
            return getDateFormatFrom(DATE_FORMAT_YEAR_ONLY).parse(value);
        }

        public static Date parseDateStartYearOnly(Integer yearOnly) {
            return DataType.ParseUtil.parseDateInEnglishDayMonthYearFormat("01/01/" + yearOnly.toString());
        }

        public static Date parseDateEndYearOnly(Integer yearOnly) {
            return DataType.ParseUtil.parseDateInEnglishDayMonthYearFormat("31/12/"+yearOnly.toString());
        }

        /**
         * Attempt to parse dates, based on all supported formats
         * @param fieldValue
         * @param defaultDate
         * @return date object
         * @see DataType#DATE_FORMAT_YEAR_ONLY
         * @see DataType#DATE_FORMAT_DAY_MONTH_YEAR
         * @see DataType#ISO_DATE_FORMAT
         */
        public static Date tryParseDate(String fieldValue, String defaultDate) {
            String[] patterns = new String[]{DataType.DATE_FORMAT_YEAR_ONLY, DataType.DATE_FORMAT_DAY_MONTH_YEAR, DataType.ISO_DATE_FORMAT};
            if(StringUtils.isEmpty(defaultDate)) throw new IllegalArgumentException("defaultDate cannot be empty");
            if(!StringUtils.isEmpty(fieldValue) && fieldValue.contains("\"")){fieldValue = fieldValue.replaceAll("\"","");}
            fieldValue = StringUtils.ifEmpty(fieldValue, defaultDate);
            java.util.Date date = null;
            for (String pattern : patterns) {
                try{
                    date = getDateFormatFrom(pattern).parseStrict(fieldValue);
                }catch (IllegalArgumentException e){}
                if(date!=null){
                    break;
                }
            }
            if(date == null) {
                try {
                    date = getDateFormatFrom(DataType.DATE_FORMAT_DAY_MONTH_YEAR).parseStrict(fieldValue);
                } catch (IllegalArgumentException e){
                    date = getDateFormatFrom(DataType.DATE_FORMAT_DAY_MONTH_YEAR).parseStrict(defaultDate);
                }
            }
            return date;
        }

        public static Date parseDate(String value, String parseFormat, String defaultValue) {
            parseFormat = StringUtils.ifEmpty(parseFormat, ISO_DATE_FORMAT);
            if(value.contains("\"")) {
                value = value.replaceAll("\"", "");
            }
            value = StringUtils.ifEmpty(value, defaultValue);
            final DateTimeFormat dateTimeFormat = getDateFormatFrom(parseFormat);
            return dateTimeFormat.parseStrict(value);
        }

        static Map<String, DateTimeFormat> cacheOfDateTimeFormat = new HashMap<>();

        public static DateTimeFormat getDateFormatFrom(final String pattern) {

            DateTimeFormat dateTimeFormat = cacheOfDateTimeFormat.get(pattern);
            if(dateTimeFormat == null){
                dateTimeFormat = new DateTimeFormat(pattern, info) {};
                cacheOfDateTimeFormat.put(pattern, dateTimeFormat);
            };
            return dateTimeFormat;
        }
    }
}
