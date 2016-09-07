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

import org.wwarn.mapcore.client.common.types.FilterConfigVisualization;
import org.wwarn.mapcore.client.components.customwidgets.facet.FacetType;
import org.wwarn.mapcore.client.utils.StringUtils;

import java.util.*;

/**
 * Filter config pojo
 */
public class FilterConfig implements Config {
    List<FilterSetting> filters = new ArrayList<FilterSetting>();

    public String getFilterLabel() {
        return filterLabel;
    }

    private String filterLabel;

    public void addFilterSetting(FilterSetting filterSetting){
        filters.add(filterSetting);
    }

    @Deprecated
    public void addFilter(String filterColumn, String filterFieldName, String filterFieldLabel, HashMap<String, String> filterFieldValueToLabelMap) {
        addFilterSetting(new FilterSetting.FilterSettingsBuilder(filterColumn, filterFieldName, filterFieldLabel).setFilterFieldValueToLabelMap(filterFieldValueToLabelMap).build());
    }

    @Deprecated
    public void addFilter(String[] filterColumns, String filterFieldName, String filterFieldLabel, HashMap<String, String> filterFieldValueToLabelMap) {
        addFilterSetting(new FilterMultipleFields(filterColumns, filterFieldName, filterFieldLabel, filterFieldValueToLabelMap));
    }

    @Deprecated
    public void addFilter(String filterColumn, String filterFieldName, String filterFieldLabel,
                          HashMap<String, String> filterFieldValueToLabelMap, int visibleItemCount,
                          FilterConfigVisualization filterConfigVisualization, FacetType type) {
        addFilterSetting(new FilterSetting.FilterSettingsBuilder(filterColumn, filterFieldName, filterFieldLabel).
                setFilterFieldValueToLabelMap(filterFieldValueToLabelMap).setVisibleItemCount(visibleItemCount).
                setFilterShowItemsOptions(filterConfigVisualization).setFacetType(type).build());
    }

    public void setFilterLabel(String filterLabel) {
        if(StringUtils.isEmpty(filterLabel)){
            throw new IllegalArgumentException("Filter label should not be set empty");
        }
        this.filterLabel = filterLabel;
    }

    public List<FilterSetting> getFilters(){
        return (this.filters);
    }

    public int getFilterCount(){
        return filters.size();
    }

    public String getFilterFieldTitleBy(String filterFieldName){
        for (FilterSetting filter : filters) {
            if(filter.filterFieldName.equals(filterFieldName)){
                return filter.filterTitle;
            }
        }
        return null;
    }

    public String getFilterFieldLabelBy(String filterFieldName) {
        for (FilterSetting filter : filters) {
            if(filter.filterFieldName.equals(filterFieldName)){
                return filter.filterFieldLabel;
            }
        }
        return null;
    }

    public List<FilterSetting> getFilterConfigBy(String filterFieldName) {
        List<FilterSetting> filterSettings = new ArrayList<FilterSetting>();
        for (FilterSetting filter : filters) {
            if(filter.filterFieldName.equals(filterFieldName)){
                filterSettings.add(filter);
            }
        }
        return filterSettings;
    }


    public void addDateRangeFilter(FilterByDateRangeSettings filterByDateRangeSettings) {
        addFilterSetting(filterByDateRangeSettings);
    }

    public static class FilterMultipleFields extends FilterSetting{

        private final String[] filterColumns;

        public FilterMultipleFields(String[] filterColumns, String filterFieldName, String filterFieldLabel, HashMap<String, String> filterFieldValueToLabelMap) {
            super(filterColumns[0].trim(), filterFieldName, filterFieldLabel, filterFieldValueToLabelMap);
            filterColumns = trim(filterColumns);
            this.filterColumns = filterColumns;
        }

        private String[] trim(String[] filterColumns) {
            for (int i = 0; i < filterColumns.length; i++) {
                filterColumns[i] = filterColumns[i].trim();
            }
            return filterColumns;
        }

        public String[] getFilterColumns() {
            return filterColumns;
        }
    }


    public class FilterBySampleSizeSettings extends FilterSetting{
        int start, end, initialValue;

        private FilterBySampleSizeSettings(String filterFieldName, String filterTitle, String filterFieldLabel) {
            super(filterFieldName, filterTitle, filterFieldLabel, null);
        }

        FilterBySampleSizeSettings(String filterFieldName, String filterTitle, String filterFieldLabel, int start,
                                  int end, int initialValue) {
            super(filterFieldName, filterTitle, filterFieldLabel, null);

            this.start = start;
            this.end = end;
            this.initialValue = initialValue;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public int getInitialValue() {
            return initialValue;
        }

    }

    public void addSampleSizeFilter(String filterColumn, String filterFieldName, String filterFieldLabel, int start,
                                   int end, int initialValue) {
        addFilterSetting(new FilterBySampleSizeSettings(filterColumn, filterFieldName, filterFieldLabel, start, end, initialValue));
    }

    /**
     * Class created for the purpose of filtering results
     * between a minimum and a maximum value of one of their fields
     */
    public class FilterByIntegerRangeSettings extends FilterSetting{
        int start;
        int end;
        int initialValue;
        int increment;

        private FilterByIntegerRangeSettings(String filterFieldName, String filterTitle, String filterFieldLabel) {
            super(filterFieldName, filterTitle, filterFieldLabel, null);
        }

        FilterByIntegerRangeSettings(String filterFieldName, String filterTitle, String filterFieldLabel, int start,
                                   int end, int initialValue, int increment) {
            super(filterFieldName, filterTitle, filterFieldLabel, null);

            this.start = start;
            this.end = end;
            this.initialValue = initialValue;
            this.increment = increment;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public int getInitialValue() {
            return initialValue;
        }

        public int getIncrement() {return increment;}

    }

    public void addIntegerRangeFilter(String filterColumn, String filterFieldName, String filterFieldLabel, int start,
                                    int end, int initialValue, int increment) {
        addFilterSetting(new FilterByIntegerRangeSettings(filterColumn, filterFieldName, filterFieldLabel, start, end, initialValue, increment));
    }
}
