package org.wwarn.mapcore.client.components.customwidgets.facet;

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

import org.wwarn.mapcore.client.common.types.FilterConfigVisualization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



/**
 * Created by suay on 6/12/14.
 */
public class FacetBuilder {

    private String name;

    private List<FacetWidgetItem> listItems;

    private String facetTitle;

    private String facetLabel;

    private String facetName;

    private int visibleItemCount;

    //All, Available, None
    private FilterConfigVisualization filterConfigVisualization;

    //CHECK_LIST, LABEL_LIST
    private FacetType facetType;

    private IllegalArgumentException illegalArgumentException;

    public FacetBuilder setFacetName(String facetName) {
        this.facetName = facetName;
        return this;
    }

    public FacetBuilder setFacetTitle(String facetTitle) {
        this.facetTitle = facetTitle;
        return this;
    }

    public FacetBuilder setFacetLabel(String facetLabel) {
        this.facetLabel = facetLabel;
        return this;
    }

    public FacetBuilder setVisibleItemCount(int visibleItemCount) {
        this.visibleItemCount = visibleItemCount;
        return this;
    }

    public FacetBuilder setFilterConfigVisualization(FilterConfigVisualization filterConfigVisualization){
        this.filterConfigVisualization = filterConfigVisualization;
        return this;
    }

    public FacetBuilder setFacetType(FacetType facetType) {
        this.facetType = facetType;
        return this;
    }

    /**
     * Takes a map of value -> labels and the unique name of the list
     * @param items map of value -> labels
     * @return a Builder instance to allow chaining
     */
    public FacetBuilder setItemsList(Map<String, String> items) {
        ArrayList<FacetWidgetItem> facetWidgetItems = new ArrayList<FacetWidgetItem>();

        for (String key : items.keySet()){
            String label = items.get(key);
            FacetWidgetItem facetWidgetItem = new FacetWidgetItem(key, label);
            facetWidgetItems.add(facetWidgetItem);
        }
        this.listItems = facetWidgetItems;
        return this;
    }


    public FacetWidget build(){
        if(facetType == FacetType.CHECK_LIST){
            return new FacetCheckBoxWidget(this);
        }else if(facetType == FacetType.SEARCHABLE_CHECK_BOX){
            return new FacetSearchableCheckBoxWidget(this);
        }else{
            return new FacetListBoxWidget(this);
        }
    }

    public String getName() {
        return name;
    }

    public List<FacetWidgetItem> getListItems() {
        return listItems;
    }

    public String getFacetTitle() {
        return facetTitle;
    }

    public String getFacetLabel() {
        return facetLabel;
    }

    public String getFacetName() {
        return facetName;
    }

    public int getVisibleItemCount() {
        return visibleItemCount;
    }

    public FilterConfigVisualization getFilterConfigVisualization() {
        return filterConfigVisualization;
    }

    public FacetType getFacetType() {
        return facetType;
    }

}
