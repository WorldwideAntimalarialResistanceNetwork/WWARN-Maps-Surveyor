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

/**
 * Test for SearchableFacetWidget using GwtMockito.
 */

import com.google.gwt.junit.client.GWTTestCase;
import org.wwarn.mapcore.client.common.types.FilterConfigVisualization;
import org.wwarn.mapcore.client.components.customwidgets.facet.FacetBuilder;
import org.wwarn.mapcore.client.components.customwidgets.facet.FacetListBoxWidget;
import org.wwarn.mapcore.client.components.customwidgets.facet.FacetWidget;
import org.wwarn.mapcore.client.components.customwidgets.facet.SearchableFacetWidget;

import java.util.HashMap;
import java.util.Map;

/**
 * Test SearcheableFacetWidget
 * I have tried to run this test using GwtMockito with no luck
 * There is a known problem
 */
public class GwtTestSearchableFacetWidget extends GWTTestCase {

    private SearchableFacetWidget searchableFacetWidget;

    FacetWidget facetWidget;

    public void gwtSetUp () {
        Map<String, String> items = new HashMap<String, String>();
        items.put("Key","Value");
        facetWidget = new FacetBuilder().setFacetName("Test facet name").setFacetTitle("Test facet title").
                setFacetLabel("Test facet label").setItemsList(items).setVisibleItemCount(4).
                setFilterConfigVisualization(FilterConfigVisualization.ALL).setFacetType(FacetType.LABEL_LIST).build();
    }

    public void testBuildSearchableFacetWidget(){
        searchableFacetWidget = new SearchableFacetWidget(facetWidget);
        assertNotNull(searchableFacetWidget.listItems);
        assertNotNull(searchableFacetWidget.facetWidget);
        assertNotNull(searchableFacetWidget.oracle);
        assertNotNull(searchableFacetWidget.textBox);
    }

    @Override
    public String getModuleName() {
        return "org.wwarn.mapcore.Map";
    }


}
