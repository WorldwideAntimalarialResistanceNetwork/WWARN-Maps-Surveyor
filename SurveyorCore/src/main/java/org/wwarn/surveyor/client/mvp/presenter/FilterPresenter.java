package org.wwarn.surveyor.client.mvp.presenter;

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

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import org.wwarn.surveyor.client.core.*;
import org.wwarn.surveyor.client.model.FilterConfig;
import org.wwarn.surveyor.client.mvp.ClientFactory;
import org.wwarn.surveyor.client.event.FilterChangedEvent;
import org.wwarn.surveyor.client.event.ResultChangedEvent;
import org.wwarn.surveyor.client.mvp.InitialFields;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;
import org.wwarn.surveyor.client.mvp.view.filter.FilterView;
import org.wwarn.surveyor.client.mvp.view.MainPanelView;

import java.util.*;

/**
 * Filter presenter, responsible for :
 * handling filterChange event, queries DataProvider with changes
 * fires a resultChangedEvent with results from DataProvider
 *
 * User: nigel
 * Date: 01/08/13
 */
public class FilterPresenter implements Presenter {
    private FilterView filterView;
    private FilterChangeHandler filterChangeHandler = new FilterChangeHandler();
    private ResultChangeHandler resultChangeHandler = new ResultChangeHandler(this);
    private ClientFactory clientFactory = SimpleClientFactory.getInstance();
    private final ApplicationContext applicationContext = clientFactory.getApplicationContext();
    public static final String DEFAULT_CATCH_ALL_OPTION = "All";
    private static InitialFields initialFields;


    public FilterPresenter(FilterView filterView) {
        this.filterView = filterView;
        bind();
    }

    public void go(MainPanelView container) {
        // add filter view to filter panel
        container.getFilterContainerPanel().add(filterView.asWidget());
        // setup up surveyor
        setupFilters();
    }

    private void setupFilters() {

//        EventLogger.logEvent("org.wwarn.surveyor.client.mvp.presenter.FilterPresenter.setupFilters", "getLastQueryResult", "begin");
        QueryResult queryResult = clientFactory.getLastQueryResult();
//        EventLogger.logEvent("org.wwarn.surveyor.client.mvp.presenter.FilterPresenter.setupFilters", "getLastQueryResult", "end");

        FacetList facetFields = queryResult.getFacetFields();

        FilterConfig config = applicationContext.getConfig(FilterConfig.class);
        filterView.setupFilterDisplay(facetFields, config);
    }

    private void updateFilters() {
//        EventLogger.logEvent("org.wwarn.surveyor.client.mvp.presenter.FilterPresenter.updateFilters", "getLastQueryResult", "begin");
        QueryResult queryResult = clientFactory.getLastQueryResult();
//        EventLogger.logEvent("org.wwarn.surveyor.client.mvp.presenter.FilterPresenter.updateFilters", "getLastQueryResult", "end");

        FacetList facetFields = queryResult.getFacetFields();

        FilterConfig config = applicationContext.getConfig(FilterConfig.class);
        filterView.updateFilterDisplay(facetFields, config);
    }

    public void bind() {
        filterView.setPresenter(this);
    }

    public void onFilterChange(String facetField, Set<String> selectedListItems) {
        clientFactory.getEventBus().fireEvent(new FilterChangedEvent(facetField, selectedListItems));
//        filterView.setupFilterDisplay(clientFactory.getLastQueryResult().getFacetFields());
    }

    /**
     * Holds responsibility for handling result changes
     */
    static public class ResultChangeHandler{
        private FilterPresenter presenter;

        // result change handler
        interface ResultChangedEventBinder extends EventBinder<ResultChangeHandler> {};
        private ResultChangedEventBinder eventBinder = GWT.create(ResultChangedEventBinder.class);

        private ClientFactory clientFactory = SimpleClientFactory.getInstance();

        public ResultChangeHandler(FilterPresenter presenter) {
            this.presenter = presenter;
            eventBinder.bindEventHandlers(this, clientFactory.getEventBus());
        }

        @EventHandler
        public void onResultChanged(ResultChangedEvent resultChangedEvent){
            final QueryResult queryResult = resultChangedEvent.getQueryResult();
            clientFactory.setLastQueryResult(queryResult);
            // on result change reload filters..
            presenter.updateFilters();
        }
    }


    /**
     * Class with sole responsibility for handling FilterChangeEvents
     */
    static public class FilterChangeHandler{

        public static final Date START_DATE = DateTimeFormat.getFormat("yyyy").parse("1975");
        public static final int DELAY_MILLIS_BEFORE_QUERYING = 100;
        private Queue<FilterChangedEvent> updateQueue = new LinkedList<FilterChangedEvent>();
        /*stores all previous field selections, this must be kept for filters to work correctly*/
        final HashMap<String, List<FilterChangedEvent.FilterElement>> selectedFacetFieldsAndValues = new HashMap<String, List<FilterChangedEvent.FilterElement>>();

        interface FilterChangedEventBinder extends EventBinder<FilterChangeHandler> {};
        private FilterChangedEventBinder eventBinder = GWT.create(FilterChangedEventBinder.class);

        private ClientFactory clientFactory = SimpleClientFactory.getInstance();

        public FilterChangeHandler() {
            eventBinder.bindEventHandlers(this, clientFactory.getEventBus());
            addInitialFilterQuery();

        }

        private void addInitialFilterQuery(){
            FilterQuery filterQuery = clientFactory.getLastFilterQuery();
            if (filterQuery != null && filterQuery.getFilterQueries() != null){
                for( String key: filterQuery.getFilterQueries().keySet()){
                    FilterChangedEvent filterChangedEvent = new FilterChangedEvent(key);
                    FilterQuery.FilterQueryElement filterQueryElement = filterQuery.getFilterQueries().get(key);
                    if(filterQueryElement instanceof FilterQuery.FilterFieldValue){
                        filterChangedEvent.addFilter(((FilterQuery.FilterFieldValue) filterQueryElement).getFieldsValue());
                    }else if (filterQueryElement instanceof FilterQuery.FilterFieldGreaterThanInteger) {
                        int value = ((FilterQuery.FilterFieldGreaterThanInteger) filterQueryElement).getFieldValue();
                        filterChangedEvent.addFilter(value);
                    }else if (filterQueryElement instanceof FilterQuery.FilterFieldRangeDate) {
                        Date minDate = ((FilterQuery.FilterFieldRangeDate) filterQueryElement).getMinValue();
                        Date maxDate = ((FilterQuery.FilterFieldRangeDate) filterQueryElement).getMaxValue();
                        filterChangedEvent.addFilter(minDate, maxDate);
                    }
                    selectedFacetFieldsAndValues.put(key, filterChangedEvent.getSelectedListItems());
                }
            }

        }

        @EventHandler
        public void onFilterChanged(FilterChangedEvent filterChangedEvent){
            deferredUpdate(filterChangedEvent);
        }


        /**
         * Queue filter changed events to reduce chatter on network
         * maintain a queue of requests here, on first item entry, check wait a few ms for subsequent updates, then fire an update
         * on second item entry, add to queue, repeat
         * on wait complete, work through all items in the queue, merge all requests into a single batch call, empty queue once done
         * @param filterChangedEvent
         */
        public void deferredUpdate(FilterChangedEvent filterChangedEvent){
            com.google.gwt.user.client.Timer timer = new com.google.gwt.user.client.Timer() {
                @Override
                public void run() {
                    while(!updateQueue.isEmpty()){
                        FilterChangedEvent changedEvent = updateQueue.remove();
                        selectedFacetFieldsAndValues.put(changedEvent.getFacetField(), changedEvent.getSelectedListItems());
                    }
                    updateFilter(selectedFacetFieldsAndValues);
                }
            };
            if(updateQueue.isEmpty()) {
                timer.schedule(DELAY_MILLIS_BEFORE_QUERYING);
            }
            updateQueue.add(filterChangedEvent);
        }

        private void updateFilter(Map<String, List<FilterChangedEvent.FilterElement>> selectedFacetFieldsAndValues) {
//            EventLogger.logEvent("org.wwarn.surveyor.client.mvp.SurveyorAppController", "clientFactory.getDataProvider()", "begin");
            DataProvider dataProvider = clientFactory.getDataProvider();
//            EventLogger.logEvent("org.wwarn.surveyor.client.mvp.SurveyorAppController", "clientFactory.getDataProvider()", "end");
            FilterQuery filterQuery = new FilterQuery();
            filterQuery.setFields(getFilterFields());
            for (String filterField : selectedFacetFieldsAndValues.keySet()) {
                List<FilterChangedEvent.FilterElement> filterElements = selectedFacetFieldsAndValues.get(filterField);
                if(filterElements.size() > 0){
                    FilterChangedEvent.FilterElement valueToFilter = filterElements.get(0);
                    if(valueToFilter instanceof FilterChangedEvent.SingleFilterValue){
                        String facetFieldValue = ((FilterChangedEvent.SingleFilterValue) valueToFilter).getFacetFieldValue();
                        if(!facetFieldValue.equals(DEFAULT_CATCH_ALL_OPTION)){
                            filterQuery.addFilter(filterField, facetFieldValue);
                        }
                    }else if(valueToFilter instanceof FilterChangedEvent.DateRange){
                        filterDateRange(filterQuery, valueToFilter);
                    }else if(valueToFilter instanceof FilterChangedEvent.DateRangeAndFields){
                        filterDateRangeAndFields(filterQuery, valueToFilter);
                    }else if(valueToFilter instanceof FilterChangedEvent.FilterGreater){
                        filterGreater(filterQuery, valueToFilter);
                    }else if(valueToFilter instanceof FilterChangedEvent.MultipleFilterValue){
                        final FilterChangedEvent.MultipleFilterValue multipleFilterValue = (FilterChangedEvent.MultipleFilterValue) valueToFilter;
                        if(!multipleFilterValue.getFacetFieldValues().contains(DEFAULT_CATCH_ALL_OPTION)){
                            filterMultipleValues(filterQuery, valueToFilter);
                        }
                    }

                }
            }
            filterQuery.setFetchAllDistinctFieldValues(false);
            clientFactory.setLastFilterQuery(filterQuery);
            try {
                dataProvider.query(filterQuery, new AsyncCallback<QueryResult>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        throw new IllegalStateException(throwable);
                    }

                    @Override
                    public void onSuccess(QueryResult queryResult) {
                        clientFactory.getEventBus().fireEvent(new ResultChangedEvent(queryResult));
                    }
                });
            } catch (SearchException e) {
                throw new IllegalStateException(e);
            }
        }

        private Set<String> getFilterFields() {
            final InitialFields initialFields = getInitialFields();
            if (initialFields != null) {
                return initialFields.getInitialFields();
            };

            return null;
        }

        private void filterDateRange(FilterQuery filterQuery, FilterChangedEvent.FilterElement valueToFilter){
            FilterChangedEvent.DateRange dateRange = (FilterChangedEvent.DateRange) valueToFilter;
            filterQuery.addRangeFilter(valueToFilter.getFacetField(), dateRange.getStart(), dateRange.getEnd());
        }

        private void filterDateRangeAndFields(FilterQuery filterQuery, FilterChangedEvent.FilterElement valueToFilter){
            FilterChangedEvent.DateRangeAndFields dateRange = (FilterChangedEvent.DateRangeAndFields) valueToFilter;
            filterQuery.addRangeFilter(dateRange.getFieldTo(), dateRange.getStart(), new Date());
            filterQuery.addRangeFilter(dateRange.getFieldFrom(), START_DATE, dateRange.getEnd());
        }



        private void filterGreater(FilterQuery filterQuery, FilterChangedEvent.FilterElement valueToFilter){
            FilterChangedEvent.FilterGreater minimumSize = (FilterChangedEvent.FilterGreater) valueToFilter;
            filterQuery.addFilterGreater(valueToFilter.getFacetField(), minimumSize.getFacetFieldValue());
        }

        private void filterMultipleValues(FilterQuery filterQuery, FilterChangedEvent.FilterElement valueToFilter){
            FilterChangedEvent.MultipleFilterValue listValues = (FilterChangedEvent.MultipleFilterValue) valueToFilter;
            filterQuery.addMultipleValuesFilter(valueToFilter.getFacetField(), listValues.getFacetFieldValues());
        }
    }

    private static InitialFields getInitialFields() {
        if(initialFields!=null) return initialFields;
        try {
            initialFields = GWT.create(InitialFields.class);

        } catch (RuntimeException e) {
            if (!e.getMessage().startsWith("Deferred binding")) throw e;
            initialFields = new DefaultInitialFields();
            GWT.log("Initial fields has not been implemented in the current application");
        }
        return initialFields;
    }

    static private class DefaultInitialFields implements InitialFields{
        @Override
        public Set<String> getInitialFields() {
            return Collections.EMPTY_SET;
        }
    }
}
