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

import com.google.gwt.junit.client.GWTTestCase;
import org.wwarn.mapcore.client.common.types.FilterConfigVisualization;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by suay on 6/12/14.
 */
public class GwtTestFacetWidget extends GWTTestCase {


    public void testFacetListBoxWidget() throws  Exception{

        FacetListBoxWidget facetListBoxWidget = new FacetListBoxWidget(createBuilder());

        assertNotNull(facetListBoxWidget);
        assertNotNull(facetListBoxWidget.buildDisplay());
        assertNotNull(facetListBoxWidget.buildHTMLHeader());
        assertEquals(facetListBoxWidget.getFacetField(),"Test facet name");
        assertEquals(facetListBoxWidget.getFacetTitle(),"Test facet title");
        assertEquals(facetListBoxWidget.getFacetLabel(),"Test facet label");
        assertEquals(facetListBoxWidget.getVisibleItemCount(),  5);

    }

    public void testFacetCheckBoxWidget() throws  Exception{

        FacetCheckBoxWidget facetListBoxWidget = new FacetCheckBoxWidget(createBuilder());

        assertNotNull(facetListBoxWidget);
        assertNotNull(facetListBoxWidget.buildDisplay());
        assertNotNull(facetListBoxWidget.buildHTMLHeader());
        assertEquals(facetListBoxWidget.getFacetField(),"Test facet name");
        assertEquals(facetListBoxWidget.getFacetTitle(),"Test facet title");
        assertEquals(facetListBoxWidget.getFacetLabel(),"Test facet label");
        assertEquals(facetListBoxWidget.getVisibleItemCount(),  5);

    }


    private FacetBuilder createBuilder() {
        FacetBuilder builder = new FacetBuilder();
        Map<String, String> items = new HashMap<String, String>();
        items.put("Key","Value");
        builder.setFacetName("Test facet name");
        builder.setFacetTitle("Test facet title");
        builder.setFacetLabel("Test facet label");
        builder.setFacetType(FacetType.LABEL_LIST);
        builder.setFilterConfigVisualization(FilterConfigVisualization.ALL);
        builder.setVisibleItemCount(5);
        builder.setItemsList(items);

        return builder;
    }


    @Override
    public String getModuleName() {
        return "org.wwarn.mapcore.Map";
    }
}
