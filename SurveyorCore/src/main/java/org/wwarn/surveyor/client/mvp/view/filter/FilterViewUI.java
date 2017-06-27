package org.wwarn.surveyor.client.mvp.view.filter;

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
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import org.wwarn.mapcore.client.common.types.FilterConfigVisualization;
import org.wwarn.mapcore.client.component.MainSectionPanel;
import org.wwarn.mapcore.client.component.UiHelpToolTip;
import org.wwarn.mapcore.client.components.customwidgets.facet.*;
import org.wwarn.mapcore.client.components.customwidgets.map.OfflineMapWidget;
import org.wwarn.surveyor.client.core.FacetList;
import org.wwarn.surveyor.client.event.ResetFilterActionEvent;
import org.wwarn.surveyor.client.event.SelectFilterEvent;
import org.wwarn.surveyor.client.event.ToggleLayerEvent;
import org.wwarn.surveyor.client.i18nstatic.TextConstants;
import org.wwarn.surveyor.client.model.FilterByDateRangeSettings;
import org.wwarn.surveyor.client.model.FilterConfig;
import org.wwarn.mapcore.client.utils.EventLogger;
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.surveyor.client.model.FilterSetting;
import org.wwarn.surveyor.client.mvp.ClientFactory;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;
import org.wwarn.surveyor.client.mvp.presenter.FilterPresenter;

import java.util.*;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;

/**
 * Add filter widget
 * Get filter event changes and delegate
 */
public class FilterViewUI extends Composite implements  FilterView {
    private static Logger logger = Logger.getLogger("SurveyorCore.FilterViewUI");
    protected FilterPresenter presenter;
    protected final Panel panel;
    private LinkedFilterSelectionState linkedFilterSelectionState = new LinkedFilterSelectionState();

    // Event Bus bindings
    interface SomeEventBinder extends EventBinder<FilterViewUI> {};

    @UiField(provided = true)
    public Anchor resetAnchor = new Anchor();
    protected final List<FacetWidget> filterList = new ArrayList<FacetWidget>();
    private String lastSelectedFilterField;
    private ClientFactory clientFactory = SimpleClientFactory.getInstance();
    interface FilterViewUIUiBinder extends UiBinder<Panel, FilterViewUI> {
    }

    private static FilterViewUIUiBinder ourUiBinder = GWT.create(FilterViewUIUiBinder.class);

    public void setPresenter(FilterPresenter presenter) {
        this.presenter = presenter;
    }

    public FilterViewUI() {
        panel = ourUiBinder.createAndBindUi(this);
        initWidget(panel);
        SomeEventBinder eventBinder = GWT.create(SomeEventBinder.class);
        eventBinder.bindEventHandlers(this, clientFactory.getEventBus());
    }

    public void setupFilterDisplay(final FacetList facetList, FilterConfig filterConfig){
//        EventLogger.logEvent("org.wwarn.surveyor.client.mvp.view.FilterView", "renderListBox", "begin");
        panel.clear();
        final VerticalPanel  facetWidgetsPanel = new VerticalPanel();
        panel.add(createMainSectionPanel(filterConfig, facetWidgetsPanel));
        addFilters(facetList, filterConfig, facetWidgetsPanel);
        setupResetAnchor();
        panel.add(resetAnchor);
    }

    protected void addFilters(final FacetList facetList, FilterConfig filterConfig, VerticalPanel  facetWidgetsPanel){
        for (final FacetList.FacetField facetField : facetList) {
            List<FilterSetting> filterSettings = filterConfig.getFilterConfigBy(facetField.getFacetField());
            if(filterSettings.size() < 1){ continue;}
            final FilterSetting filterSetting = filterSettings.get(0);

            if(filterSettings.size() > 0 && !(filterSetting instanceof FilterByDateRangeSettings)
                    && !(filterSetting instanceof FilterConfig.FilterBySampleSizeSettings)
                    && !(filterSetting instanceof FilterConfig.FilterByIntegerRangeSettings) ){
                final FacetWidget facetWidget = createFacetWidget(filterSetting,facetField,facetList);
                filterList.add(facetWidget);
                facetWidgetsPanel.add((IsWidget) facetWidget);
            }
        }
    }

    protected void setupResetAnchor(){
       resetAnchor = addResetLinkClickHandler(filterList);
       TextConstants textConstants = GWT.create(TextConstants.class);
       resetAnchor.setText(textConstants.resetFilter());
    }


    protected MainSectionPanel createMainSectionPanel(FilterConfig filterConfig, VerticalPanel facetWidgetsPanel){
        TextConstants textConstants = GWT.create(TextConstants.class);
        final MainSectionPanel mainSectionPanel = new MainSectionPanel(
                facetWidgetsPanel,
                textConstants.selectFilter(),
                new UiHelpToolTip(new HTML(filterConfig.getFilterLabel())));
        return mainSectionPanel;
    }

    @EventHandler
    public void onResetFilterActionEvent(ResetFilterActionEvent resetFilterActionEvent){
        resetAllFilters();
    }



    protected FacetWidget createFacetWidget(final FilterSetting filterSetting,
                                            final FacetList.FacetField facetField,
                                            final FacetList facetList){

        final Map<String, String> itemAndItemLabelMap = getFilterItemsAndFilterItemLabels(facetField, filterSetting, facetList);
        String facetTitle = StringUtils.ifEmpty(filterSetting.filterTitle, facetField.getFacetField());
        String facetLabel = StringUtils.ifEmpty(filterSetting.filterFieldLabel, facetField.getFacetField());
        int visibleItemCount = filterSetting.visibleItemCount;
        FacetType facetType = filterSetting.facetType;

        final FacetWidget facetWidget = new FacetBuilder().setFacetTitle(facetTitle).
                setFacetLabel(facetLabel).setFacetName(facetField.getFacetField()).
                setItemsList(itemAndItemLabelMap).setVisibleItemCount(visibleItemCount).
                setFilterConfigVisualization(filterSetting.filterShowItemsOptions).setFacetType(facetType).
                setDefaultShowHideToggleStateIsVisible(filterSetting.defaultShowHideToggleStateIsVisible).
                setIsShowHideToggleEnabled(filterSetting.isShowHideToggleEnabled).build();

        facetWidget.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent changeEvent) {
                linkFilters(facetField.getFacetField());
                if(filterSetting instanceof FilterConfig.FilterMultipleFields){
                    onFilterChangeForMultipleFilterFields(filterSetting, facetWidget.getSelectedListItems(), facetList);
                }else{
                    presenter.onFilterChange(facetField.getFacetField(), facetWidget.getSelectedListItems());
                }
            }
        });

        return facetWidget;
    }


    protected void linkFilters(String facetField){
        lastSelectedFilterField = facetField;
        if (!linkedFilterSelectionState.push(facetField)) {
            //if this filter item is already present in linkedFilterSelectionState
            resetSetSuccessiveFilters();
        }
    }

    private void onFilterChangeForMultipleFilterFields(FilterSetting filterSetting,
                                                       Set<String> selectedListItems,
                                                       final FacetList facetList
                                                       ){
        // do the field merge assumes a implicit OR logic between merged fields, not an AND logic as other categories mimics AQSurveyor logic.
        // get all filter columns from settings
        final String[] filterColumns = ((FilterConfig.FilterMultipleFields) filterSetting).getFilterColumns();
        for (String filterColumn : filterColumns) {
            final FacetList.FacetField field = findFieldInList(facetList, filterColumn);
            if(isSubsetContainedIn(selectedListItems, field.getDistinctFacetValues())){
                presenter.onFilterChange(field.getFacetField(), selectedListItems);
            }else{
                //reset filter when the selected element is not present in this filter category
                presenter.onFilterChange(field.getFacetField(), new HashSet<String>());
            }
        }
    }

    private void resetSetSuccessiveFilters() {
        List<FacetWidget> filtersToReset = new ArrayList<FacetWidget>();
        for (FacetWidget facetWidget : filterList) {
            if(!linkedFilterSelectionState.contains(facetWidget.getFacetField())
                    && facetWidget.getFilterConfigVisualization() == FilterConfigVisualization.AVAILABLE
                    && facetWidget instanceof FacetListBoxWidget){
                filtersToReset.add(facetWidget);
            }
        }
        resetFilters(filtersToReset);
    }

    @Override
    public void updateFilterDisplay(FacetList facetFields, FilterConfig config) {
        /*
        * When the initial selection is made in a facet filter,
        * add a clear option / continue with all option, highlight current selection,
        * and keep remaining items in current filter active. Fetch updated facet list
        * from recordset and use this to disable all items absent from the displayed
        * facet filters but current filter.
        */
        // store the filter that was last selected, in order to filter all other records
        //
//        this.setupFilterDisplay(facetFields, config);
        final FacetWidget facetWidget = getFacetWidgetByField(lastSelectedFilterField);
        if(facetWidget == null){return;}
        // update all facets which are not currently selected
        for (FacetWidget widget : filterList) {
            if(facetWidget != widget
                    && widget.getFilterConfigVisualization() == FilterConfigVisualization.AVAILABLE){
                updateFilterWidget(widget, facetFields);
            }
        }
        if(facetWidget.getSelectedListItems().contains("All") || facetWidget.getSelectedListItems().contains("all")){
            linkedFilterSelectionState.removeCurrentAndSuccessive(facetWidget.getFacetField());
        }
    }

    private void updateFilterWidget(FacetWidget widget, FacetList facetFields) {
        //find distinctValues for this widget from facetFields
        final FacetList.FacetField fieldInList = findFieldInList(facetFields, widget.getFacetField());
        if(fieldInList == null){return;}
        Set<String> distinctFacetValues = fieldInList.getDistinctFacetValues();
        final List<FacetWidgetItem> facetWidgetItems = widget.getFacetWidgetItems();
        final List<FacetWidgetItem> facetWidgetItemsToDisable = new ArrayList<FacetWidgetItem>();
        for (FacetWidgetItem widgetItem : facetWidgetItems) {
            if(!distinctFacetValues.contains(widgetItem.getValue())) {
                facetWidgetItemsToDisable.add(widgetItem);
            }
        }
        widget.disableItems(facetWidgetItemsToDisable);
    }

    private FacetWidget getFacetWidgetByField(String lastSelectedFilterField) {
        for (FacetWidget facetWidget : filterList) {
            if(facetWidget.getFacetField().equals(lastSelectedFilterField)){
                return facetWidget;
            }
        }
        return null;
    }

    /**
     * check if *any* of items in selectedListItems is present in distinctFacetValues
     * @param selectedListItems
     * @param distinctFacetValues
     * @return
     */
    protected boolean isSubsetContainedIn(Set<String> selectedListItems, Set<String> distinctFacetValues) {
        boolean isPresent = false;
        for (String selectedListItem : selectedListItems) {
            isPresent = distinctFacetValues.contains(selectedListItem);
        }
        return isPresent;
    }

    protected Anchor addResetLinkClickHandler(final List<FacetWidget> facetWidgetList) {

        resetAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                logger.log(FINE,"Resetting app to default state.");
                clientFactory.getEventBus().fireEvent(new ResetFilterActionEvent());
            }
        });

        return resetAnchor;
    }

    protected void resetAllFilters() {
        linkedFilterSelectionState.clear();
        resetFilters(filterList);
    }

    protected void resetFilters(List<FacetWidget> facetWidgetList) {
        for (FacetWidget facetWidget : facetWidgetList) {
            facetWidget.unSelectAndReset();
            presenter.onFilterChange(facetWidget.getFacetField(), facetWidget.getSelectedListItems());
        }
    }

    protected Map<String, String> getFilterItemsAndFilterItemLabels(FacetList.FacetField facetField, FilterSetting filterSetting, FacetList facetList) {
        Map<String, String> itemAndItemLabelMap = new LinkedHashMap<String, String>();
        if(filterSetting instanceof FilterConfig.FilterMultipleFields){
            // do the field merge
            // get all filter columns from settings
            final String[] filterColumns = ((FilterConfig.FilterMultipleFields) filterSetting).getFilterColumns();
            for (String filterColumn : filterColumns) {
                final FacetList.FacetField field = findFieldInList(facetList, filterColumn);
                buildDistinctFacetValues(field, filterSetting, itemAndItemLabelMap);
            }
        }else{
            if (filterSetting.filterFieldValueMap != null){
                for (Map.Entry<String, String> entry : filterSetting.filterFieldValueMap.entrySet())
                {
                    itemAndItemLabelMap.put(entry.getKey(), entry.getValue());
                }
            }
            buildDistinctFacetValues(facetField, filterSetting, itemAndItemLabelMap);
        }
        return itemAndItemLabelMap;
    }

    protected void buildDistinctFacetValues(FacetList.FacetField facetField, FilterSetting filterSetting, Map<String, String> itemAndItemLabelMap) {
        for (String distinctFacetValue : facetField.getDistinctFacetValues()) {
            if(StringUtils.isEmpty(distinctFacetValue)){ continue; }
            String facetItemLabel = filterSetting.getValueLabel(distinctFacetValue);
            itemAndItemLabelMap.put(distinctFacetValue, facetItemLabel);
        }
    }

    protected FacetList.FacetField findFieldInList(FacetList facetList, String filterColumn) {
        for (FacetList.FacetField field : facetList) {
            if(field.getFacetField().equals(filterColumn)){
                        return field;
            }
        }
        return null;
    }

    class LinkedFilterSelectionState {
        private Set<String> filterSelected = new LinkedHashSet<String>();

        public boolean push(String filterField){
            // if on add, item is already present then don't re-add
            if(filterSelected.contains(filterField)){
                removeSuccessive(filterField);
                return false;
            }
            return filterSelected.add(filterField);
        }

        public void removeSuccessive(String filterField){
            boolean removeSuccessive = false;
            // if pop item, and all fields added since
            final Iterator<String> iterator = filterSelected.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                if(removeSuccessive){
                    iterator.remove();
                }
                if(next.equals(filterField)){
                    removeSuccessive = true;
                }
            }
        }

        public boolean contains(String facetField) {
            return filterSelected.contains(facetField);
        }

        public void clear() {
            filterSelected.clear();
        }

        public void removeCurrentAndSuccessive(String filterField) {
            boolean remove = false;
            // if pop item, and all fields added since
            final Iterator<String> iterator = filterSelected.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                if(next.equals(filterField)){
                    remove = true;
                }
                if(remove){
                    iterator.remove();
                }
            }
        }
    }

    //When a SelectFilterEvent is triggered, this method will look for the filter and value and will select it.
    @EventHandler
    public void onSelectFilter(SelectFilterEvent selectFilterEvent){
        for (FacetWidget facetWidget : filterList) {
            if(facetWidget.getFacetField().equals(selectFilterEvent.getFilterName())){
                for (FacetWidgetItem facetWidgetItem : facetWidget.getFacetWidgetItems()) {
                    if(facetWidgetItem.getValue().equals(selectFilterEvent.getFilterValue())){
                        facetWidget.selectItems(Arrays.asList(facetWidgetItem));
                    }
                }
            }
        }

    }
}
