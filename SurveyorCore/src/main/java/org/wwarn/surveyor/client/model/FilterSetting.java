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
import org.wwarn.mapcore.client.components.customwidgets.facet.FacetBuilder;
import org.wwarn.mapcore.client.components.customwidgets.facet.FacetType;
import org.wwarn.mapcore.client.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a component of FilterConfig and holds settings for individual filters
 */
public class FilterSetting {

    public final String filterFieldName, filterTitle, filterFieldLabel;

    //Select the way to show the items: All|Available|None
    public FilterConfigVisualization filterShowItemsOptions;

    //Number of items to show inside the listbox
    public int visibleItemCount;

    // Allow user to show or hide filter
    public boolean isShowHideToggleEnabled = false;

    // Set default state of show hide toggle
    public boolean defaultShowHideToggleStateIsVisible = true;

    public FacetType facetType;

    public Map<String, String> filterFieldValueMap = new HashMap<String, String>();

    FilterSetting(String filterFieldName, String filterTitle, String filterFieldLabel, HashMap<String, String> filterFieldValueToLabelMap) {
        if(StringUtils.isEmpty(filterFieldName)){
            throw new IllegalArgumentException("filterFieldName must be set");
        }
        this.filterFieldName = filterFieldName;
        this.filterTitle = filterTitle;
        this.filterFieldLabel = filterFieldLabel;
        if(filterFieldValueToLabelMap!=null){
            this.filterFieldValueMap = filterFieldValueToLabelMap;
        }
    }

    private FilterSetting (FilterSettingsBuilder builder){
        this.filterFieldName = builder.filterFieldName;
        this.filterTitle = builder.filterTitle;
        this.filterFieldLabel = builder.filterFieldLabel;
        this.filterFieldValueMap = builder.filterFieldValueToLabelMap;
        this.visibleItemCount=builder.visibleItemCount;
        this.filterShowItemsOptions = builder.filterShowItemsOptions;
        this.facetType = builder.facetType;
        this.isShowHideToggleEnabled = builder.isShowHideToggleEnabled;
        this.defaultShowHideToggleStateIsVisible = builder.defaultShowHideToggleStateIsVisible;
    }

    public String getValueLabel(String filterFieldValue) {
        String filterFieldValueLabel;
//            if(StringUtils.isEmpty(filterFieldValue)){
//                throw new IllegalArgumentException("filterFieldValue argument required");
//            }
        filterFieldValueLabel = this.filterFieldValueMap.get(filterFieldValue);
        return filterFieldValueLabel;
    }

    public static class FilterSettingsBuilder {

        private final String filterFieldName;

        private final String filterTitle;

        private final String filterFieldLabel;

        private HashMap<String, String> filterFieldValueToLabelMap;

        private FilterConfigVisualization filterShowItemsOptions;

        private int visibleItemCount;

        private FacetType facetType;

        public FilterSettingsBuilder setIsShowHideToggleEnabled(boolean isShowHideToggleEnabled) {
            this.isShowHideToggleEnabled = isShowHideToggleEnabled;
            return this;
        }

        public FilterSettingsBuilder setDefaultShowHideToggleStateIsVisible(boolean defaultShowHideToggleStateIsVisible) {
            this.defaultShowHideToggleStateIsVisible = defaultShowHideToggleStateIsVisible;
            return this;
        }

        public boolean isShowHideToggleEnabled = false;

        public boolean defaultShowHideToggleStateIsVisible = true;

        //Required parameters
        public FilterSettingsBuilder(String filterFieldName, String filterTitle, String filterFieldLabel) {
            this.filterFieldName = filterFieldName;
            this.filterTitle = filterTitle;
            this.filterFieldLabel = filterFieldLabel;
        }

        public FilterSettingsBuilder setFilterShowItemsOptions(FilterConfigVisualization filterShowItemsOptions) {
            this.filterShowItemsOptions = filterShowItemsOptions;
            return this;
        }

        public FilterSettingsBuilder setVisibleItemCount(int visibleItemCount) {
            this.visibleItemCount = visibleItemCount;
            return this;
        }

        public FilterSettingsBuilder setFilterFieldValueToLabelMap(HashMap<String, String> filterFieldValueToLabelMap){
            this.filterFieldValueToLabelMap = filterFieldValueToLabelMap;
            return this;

        }

        public FilterSettingsBuilder setFacetType(FacetType facetType){
            this.facetType = facetType;
            return(this);
        }

        public FilterSetting build(){
            return new FilterSetting(this);
        }

    }

}
