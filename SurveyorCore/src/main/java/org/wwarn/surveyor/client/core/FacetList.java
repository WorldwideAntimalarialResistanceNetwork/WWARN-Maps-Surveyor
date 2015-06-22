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

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Holds a list of facet fields and its distinct values
 * User: nigel
 * Date: 01/08/13
 * Time: 11:28
 */
public class FacetList implements Iterable<FacetList.FacetField>, IsSerializable {
    List<FacetField> facetFields = new ArrayList<FacetField>();

    public void addFacetField(String fieldname, Set<String> uniqueFacetValues){
        facetFields.add(new FacetField(fieldname, uniqueFacetValues));
    }

    @Override
    public Iterator<FacetField> iterator() {
        return facetFields.iterator();
    }

    public int size(){
        return facetFields.size();
    }

    public static class FacetField implements IsSerializable {
        private String facetField;
        private Set<String> distinctFacetValues;

        public FacetField(String facetField, Set<String> distinctFacetValues) {
            this.facetField = facetField;
            this.distinctFacetValues = distinctFacetValues;
        }

        public FacetField() {
        }

        public String getFacetField() {
            return facetField;
        }

        public Set<String> getDistinctFacetValues() {
            return distinctFacetValues;
        }

        @Override
        public String toString() {
            return "FacetField{" +
                    "facetField='" + facetField + '\'' +
                    ", distinctFacetValues=" + distinctFacetValues +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "FacetList{" +
                "facetFields=" + facetFields +
                '}';
    }
}
