package org.wwarn.surveyor.client.event;

import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * Created by suay on 6/3/16.
 */
public class SelectFilterEvent extends GenericEvent {

    private String filterName;

    private String filterValue;

    public SelectFilterEvent(String filterName, String filterValue) {
        this.filterName = filterName;
        this.filterValue = filterValue;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }
}
