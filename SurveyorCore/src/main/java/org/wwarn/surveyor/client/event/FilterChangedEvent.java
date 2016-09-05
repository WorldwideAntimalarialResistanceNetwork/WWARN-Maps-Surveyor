package org.wwarn.surveyor.client.event;

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

import com.google.web.bindery.event.shared.binder.GenericEvent;
import org.wwarn.mapcore.client.utils.StringUtils;

import java.util.*;

/**
 * An event to register changes in filter options, this allows filter elements to be present in different views
 * and changes in any view is propagated through this event to a single handler
 * User: nigelthomas
 * Date: 12/09/2013
 * Time: 23:03
 */
public class FilterChangedEvent extends GenericEvent {
    private String facetField;
    private ArrayList<FilterElement> selectedListItems = new ArrayList<FilterElement>();

    public FilterChangedEvent(String facetField) {
        if(StringUtils.isEmpty(facetField)){
            throw new IllegalArgumentException("facetField may not be empty");
        }
        this.facetField = facetField;
    }

    public FilterChangedEvent(String facetField, Set<String> selectedListItems) {
        this(facetField);
        if(selectedListItems != null){
            /*for(String item : selectedListItems){
                this.selectedListItems.add(new SingleFilterValue(item));
            }*/
            this.selectedListItems.add(new MultipleFilterValue(selectedListItems));
        }
    }

    private <T> void enforceLengthLimit(List<T> selectedListItems) {
        if(selectedListItems.size() > 1){
            throw new IllegalArgumentException("only supports a single facet value");
        }
    }

    public String getFacetField() {
        return facetField;
    }

    public List<FilterElement> getSelectedListItems() {
        return (selectedListItems);
    }

    public void addFilter(String facetFieldValue){
        enforceLengthLimit(selectedListItems);
        selectedListItems.add(new SingleFilterValue(facetFieldValue));
    }

    public void addFilter(Set<String> facetFieldValue){
        selectedListItems.add(new MultipleFilterValue(facetFieldValue));
    }

    public void addFilter(Date minValue, Date maxValue){
        enforceLengthLimit(selectedListItems);
        selectedListItems.add(new DateRange(minValue, maxValue));
    }

    public void addFilter(String fieldFrom, String fieldTo, Date minValue, Date maxValue){
        enforceLengthLimit(selectedListItems);
        selectedListItems.add(new DateRangeAndFields(minValue, maxValue, fieldFrom, fieldTo));
    }

    public void addFilter(int minimumValue){
        enforceLengthLimit(selectedListItems);
        selectedListItems.add(new FilterGreater(minimumValue));
    }

    public void addFilter(int minimumValue, int maximumValue){
        enforceLengthLimit(selectedListItems);
        selectedListItems.add(new FilterGreater(minimumValue, maximumValue));
    }

    public void resetField() {
        selectedListItems = new ArrayList<FilterElement>();
    }

    public abstract class FilterElement {
        public String getFacetField() {
            return facetField;
        }
    }

    public class DateRange extends FilterElement{
        Date start , end;

        public DateRange(Date start, Date end) {
            this.start = start;
            this.end = end;
        }

        public Date getStart() {
            return start;
        }

        public Date getEnd() {
            return end;
        }
    }

    public class SingleFilterValue extends FilterElement{
        String facetFieldValue;

        public SingleFilterValue(String facetFieldValue) {
            this.facetFieldValue = facetFieldValue;
        }

        public String getFacetFieldValue() {
            return facetFieldValue;
        }
    }

    public class MultipleFilterValue extends FilterElement{
        Set<String> facetFieldValues;

        public MultipleFilterValue(Set<String> facetFieldValue) {
            this.facetFieldValues = facetFieldValue;
        }

        public Set<String> getFacetFieldValues() {
            return facetFieldValues;
        }
    }

    public class FilterGreater extends FilterElement{
        int facetFieldValue;
        Integer max;

        public FilterGreater(int facetFieldValue) {
            this.facetFieldValue = facetFieldValue;
        }

        public FilterGreater(int facetFieldValue, int max) {
            this.facetFieldValue = facetFieldValue;
            this.max = (Integer)max;
        }

        public int getFacetFieldValue() {
            return facetFieldValue;
        }
        public Integer getMax() { return max;}
    }

    public class DateRangeAndFields extends FilterElement{
        Date start, end;
        String fieldFrom, fieldTo;

        public DateRangeAndFields(Date start, Date end, String fieldFrom, String fieldTo) {
            this.start = start;
            this.end = end;
            this.fieldFrom = fieldFrom;
            this.fieldTo = fieldTo;
        }

        public String getFieldFrom() {
            return fieldFrom;
        }

        public String getFieldTo() {
            return fieldTo;
        }

        public Date getStart() {
            return start;
        }

        public Date getEnd() {
            return end;
        }
    }


}
