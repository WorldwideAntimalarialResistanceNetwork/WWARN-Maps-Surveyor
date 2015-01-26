package org.wwarn.surveyor.client.model;

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

import org.wwarn.mapcore.client.utils.StringUtils;

/**
 * Created by suay on 5/15/14.
 */
public class FilterByDateRangeSettings extends FilterSetting{

    String dateStart, dateEnd;

    String fieldFrom, fieldTo;

    String initialStart, initialEnd;

    boolean isPlayable;

    String textLabel;

    private FilterByDateRangeSettings (DateRangeSettingsBuilder builder){
        super(builder.filterFieldName, builder.filterTitle, builder.filterFieldLabel, null);
        if(StringUtils.isEmpty(builder.dateStart, builder.dateEnd)){
            throw new IllegalArgumentException("parameters may not be empty");
        }

        this.dateStart = builder.dateStart;
        this.dateEnd = builder.dateEnd;
        this.fieldFrom = builder.fieldFrom;
        this.fieldTo = builder.fieldTo;
        this.isPlayable = builder.isPlayable;
        this.textLabel = builder.textLabel;
        this.initialStart = builder.initialStart;
        this.initialEnd = builder.initialEnd;
    }

    @Deprecated
    private FilterByDateRangeSettings(String filterFieldName, String filterTitle, String filterFieldLabel) {
        super(filterFieldName, filterTitle, filterFieldLabel, null);
    }

    @Deprecated
    FilterByDateRangeSettings(String filterFieldName, String filterTitle, String filterFieldLabel, String dateStart,
                              String dateEnd, String fieldFrom, String fieldTo, boolean isPlayable) {
        super(filterFieldName, filterTitle, filterFieldLabel, null);
        if(StringUtils.isEmpty(dateStart, dateEnd)){
            throw new IllegalArgumentException("parameters may not be empty");
        }

        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.fieldFrom = fieldFrom;
        this.fieldTo = fieldTo;
        this.isPlayable = isPlayable;
    }


    public String getDateStart() {
        return dateStart;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public String getFieldFrom() {
        return fieldFrom;
    }

    public String getFieldTo() {
        return fieldTo;
    }

    public boolean isPlayable() {
        return isPlayable;
    }

    public String getTextLabel() {
        return textLabel;
    }

    public String getInitialStart() {
        return initialStart;
    }

    public String getInitialEnd() {
        return initialEnd;
    }

    public static class DateRangeSettingsBuilder{

        String filterFieldName, filterTitle, filterFieldLabel;

        private String dateStart, dateEnd;

        private String fieldFrom, fieldTo;

        private String initialStart, initialEnd;

        private boolean isPlayable;

        private String textLabel;

        //Required parameters
        public DateRangeSettingsBuilder(String filterFieldName, String filterTitle, String filterFieldLabel) {
            this.filterFieldName = filterFieldName;
            this.filterTitle = filterTitle;
            this.filterFieldLabel = filterFieldLabel;
        }

        public FilterByDateRangeSettings build(){
            return new FilterByDateRangeSettings(this);
        }

        public DateRangeSettingsBuilder setDateStart(String dateStart) {
            this.dateStart = dateStart;
            return this;
        }

        public DateRangeSettingsBuilder setDateEnd(String dateEnd) {
            this.dateEnd = dateEnd;
            return this;
        }

        public DateRangeSettingsBuilder setFieldFrom(String fieldFrom) {
            this.fieldFrom = fieldFrom;
            return this;
        }

        public DateRangeSettingsBuilder setFieldTo(String fieldTo) {
            this.fieldTo = fieldTo;
            return this;
        }

        public DateRangeSettingsBuilder setPlayable(boolean isPlayable) {
            this.isPlayable = isPlayable;
            return this;
        }

        public DateRangeSettingsBuilder setTextLabel(String textLabel) {
            this.textLabel = textLabel;
            return this;
        }

        public DateRangeSettingsBuilder setInitialStart(String initialStart) {
            this.initialStart = initialStart;
            return this;
        }

        public DateRangeSettingsBuilder setInitialEnd(String initialEnd) {
            this.initialEnd = initialEnd;
            return this;
        }
    }

}
