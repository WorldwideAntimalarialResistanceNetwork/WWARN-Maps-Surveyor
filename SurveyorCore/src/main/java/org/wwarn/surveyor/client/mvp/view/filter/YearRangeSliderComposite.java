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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ShowRangeEvent;
import com.google.gwt.event.logical.shared.ShowRangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import org.wwarn.mapcore.client.common.types.Range;
import org.wwarn.mapcore.client.components.customwidgets.YuiDualSliderGwtWidget;
import org.wwarn.mapcore.client.components.customwidgets.YuiDualSliderGwtWidgetImplWithLabelMarkers;
import org.wwarn.surveyor.client.core.DataType;
import org.wwarn.surveyor.client.event.ResetFilterActionEvent;
import org.wwarn.surveyor.client.model.FilterByDateRangeSettings;
import org.wwarn.surveyor.client.model.FilterConfig;
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.surveyor.client.model.FilterSetting;
import org.wwarn.surveyor.client.mvp.ClientFactory;
import org.wwarn.surveyor.client.event.FilterChangedEvent;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Year range slider
 * Requires start year and end year in configured format (defaults to yyyy)
 * and the name of the schema field to update
 * use current year as max year optional boolean flag
 * User: nigelthomas
 * Date: 08/10/2013
 * Time: 15:17
 */
public class YearRangeSliderComposite extends Composite{
    public static final String DEFAULT_YEAR_VALUE = "yyyy";
    private final String dateFormat;
    private Integer startYear = 1975;
    private final FlowPanel flowPanel;
    private final FilterConfig filterConfig;
    private ClientFactory clientFactory = SimpleClientFactory.getInstance();
    private Integer endYear = 2025;
    private int initialStart, initialEnd;

    private final FilterByDateRangeSettings filterByDateRangeSettings;


    interface YearRangeSliderCompositeUiBinder extends UiBinder<FlowPanel, YearRangeSliderComposite> {
    }

    private static YearRangeSliderCompositeUiBinder ourUiBinder = GWT.create(YearRangeSliderCompositeUiBinder.class);

    @UiField(provided = true)
    final Label startYearLabel = new Label(startYear.toString());

    @UiField(provided = true)
    final Label endYearLabel = new Label(endYear.toString());

    @UiField(provided = true)
    FlowPanel yearRangeSliderPanel = new FlowPanel();

    @UiField(provided = true)
    FlowPanel yearRangeDescriptionPanel = new FlowPanel();

    @UiField(provided = true)
    final Label yearRangeLabel = new Label();

    // Event Bus bindings
    interface SomeEventBinder extends EventBinder<YearRangeSliderComposite> {};

    YuiDualSliderGwtWidget yuiDualSliderGwtWidget;

    /**
     *
     * @param fieldName must be of type date in schema, and a filterByDateRange element of filters
     * @param dateFormat see <a href="http://www.gwtproject.org/javadoc/latest/com/google/gwt/i18n/client/DateTimeFormat.html">datetimeformat</a>
     */
    public @UiConstructor YearRangeSliderComposite(String fieldName, String dateFormat) {
        if(StringUtils.isEmpty(fieldName, dateFormat)){
            throw new IllegalArgumentException("fieldName and dateFormat required");
        }
        this.dateFormat = StringUtils.ifEmpty(dateFormat, DEFAULT_YEAR_VALUE);

        flowPanel = ourUiBinder.createAndBindUi(this);


        initWidget(flowPanel);
        SomeEventBinder eventBinder = GWT.create(SomeEventBinder.class);
        eventBinder.bindEventHandlers(this, clientFactory.getEventBus());
        this.filterConfig = clientFactory.getApplicationContext().getConfig(FilterConfig.class);
        List<FilterSetting> filterSettings = filterConfig.getFilterConfigBy(fieldName);

        FilterSetting filterSetting = filterSettings.get(0);
        if(filterSettings.size() < 1 ||  !(filterSetting instanceof FilterByDateRangeSettings)){
            throw new IllegalArgumentException("Field must be a date compatible type, check field is configured as filterByDateRange");
        }

        filterByDateRangeSettings = (FilterByDateRangeSettings) filterSetting;
        this.startYear = Integer.valueOf(filterByDateRangeSettings.getDateStart());
        this.endYear = calculateEndYear(filterByDateRangeSettings);
        yearRangeLabel.setText(filterByDateRangeSettings.getTextLabel());
        getInitialSliderPosition();
        updateRangeLabels(startYear, endYear);
    }

    private void getInitialSliderPosition(){
        String initialStartStr = filterByDateRangeSettings.getInitialStart();
        String initialEndStr = filterByDateRangeSettings.getInitialEnd();
        this.initialStart = initialStartStr != null  && !initialStartStr.isEmpty() ? Integer.valueOf(initialStartStr): startYear;
        this.initialEnd = initialEndStr != null  && !initialEndStr.isEmpty() ? Integer.valueOf(initialEndStr): endYear;
    }

    private int calculateEndYear(FilterByDateRangeSettings filterByDateRangeSettings1) {
        int endYearCalculated;
        if(!StringUtils.isEmpty(filterByDateRangeSettings1.getDateEnd()) && !filterByDateRangeSettings.getDateEnd().equals("currentYear")){
            endYearCalculated = Integer.valueOf(filterByDateRangeSettings.getDateEnd());
        }else {
            endYearCalculated = Integer.valueOf(getCurrentYear());
        }
        return endYearCalculated;
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        yuiDualSliderGwtWidget = setupYearRangeWidget();
        yearRangeSliderPanel.add((Widget) yuiDualSliderGwtWidget);

        if(filterByDateRangeSettings.isPlayable()){
            Button playButton = new Button("Play!");
            //playButton.setHeight("5px");
            playButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    onClickPlay(event);
                }
            });
            yearRangeDescriptionPanel.setHeight("1px");
            yearRangeDescriptionPanel.add(playButton);
        }
    }


    @EventHandler
    public void onResetFilterActionEvent(ResetFilterActionEvent resetFilterActionEvent){
        resetAllFilters();
    }

    private void resetAllFilters() {
        updateRangeLabels(startYear, endYear);
        yearRangeSliderPanel.clear();
        yuiDualSliderGwtWidget = setupYearRangeWidget();
        yearRangeSliderPanel.add((Widget) yuiDualSliderGwtWidget);

    }


    private YuiDualSliderGwtWidget setupYearRangeWidget() {
        YuiDualSliderGwtWidget dateSliderYuiWidgetImpl = new YuiDualSliderGwtWidgetImplWithLabelMarkers(456, 48, startYear, Integer.valueOf(endYear), 5, 1);
        if(initialStart != startYear || initialEnd != endYear) {
            GWT.log("!!!org.wwarn.surveyor.client.mvp.view.filter.YearRangeSliderComposite Reading initial start year or end year - this will cause a second load - TODO fix me !!!");
            dateSliderYuiWidgetImpl.setRange(initialStart, initialEnd);
        }
        ValueChangeHandler<Range<Integer>> valueChangeHandler = new ValueChangeHandler<Range<Integer>>() {

            public void onValueChange(ValueChangeEvent<Range<Integer>> yearRangeValueChangeEvent) {
                updateRangeLabels(yearRangeValueChangeEvent.getValue().lower, yearRangeValueChangeEvent.getValue().upper);
            }
        };

        ShowRangeHandler<Integer> showRangeChangeHandler = new ShowRangeHandler<Integer>() {
            private String facetField = filterByDateRangeSettings.filterFieldName;
            private DateTimeFormat dateTimeFormatYearOnly = DateTimeFormat.getFormat(dateFormat);

            public void onShowRange(ShowRangeEvent<Integer> integerShowRangeEvent) {
                Integer currentMinYear = startYear;
                Integer currentMaxYear = Integer.valueOf(endYear.toString());
                FilterChangedEvent filterChangedEvent = new FilterChangedEvent(facetField);
                if (!integerShowRangeEvent.getStart().equals(currentMinYear) ||
                        !integerShowRangeEvent.getEnd().equals(currentMaxYear)) {
                    currentMinYear = integerShowRangeEvent.getStart();
                    currentMaxYear = integerShowRangeEvent.getEnd();


                    if(StringUtils.isEmpty(filterByDateRangeSettings.getFieldFrom()) ||
                            StringUtils.isEmpty(filterByDateRangeSettings.getFieldTo())){
                        filterChangedEvent.addFilter(DataType.ParseUtil.parseDateStartYearOnly(currentMinYear), DataType.ParseUtil.parseDateEndYearOnly(currentMaxYear));
                    }else{
                        filterChangedEvent.addFilter(filterByDateRangeSettings.getFieldFrom(), filterByDateRangeSettings.getFieldTo(),
                                DataType.ParseUtil.parseDateStartYearOnly(currentMinYear), DataType.ParseUtil.parseDateEndYearOnly(currentMaxYear));
                    }

                    filterChangedEvent.addFilter(DataType.ParseUtil.parseDateStartYearOnly(currentMinYear), DataType.ParseUtil.parseDateEndYearOnly(currentMaxYear));
                } else {
                    // if filter is set to default values then reset filter options
                    filterChangedEvent.resetField();
                }
                clientFactory.getEventBus().fireEvent(filterChangedEvent);
            }


        };

        dateSliderYuiWidgetImpl.addValueChangeHandler(valueChangeHandler);
        dateSliderYuiWidgetImpl.addShowRangeHandler(showRangeChangeHandler);
        return dateSliderYuiWidgetImpl;
    }

    private void updateRangeLabels(Integer startYear, Integer endYear) {
        startYearLabel.setText(startYear.toString());
        endYearLabel.setText(endYear.toString());
    }

    private String getCurrentYear() {
        Date date = new Date();
        DateTimeFormat dtf = DateTimeFormat.getFormat(dateFormat);
        String currentYear = dtf.format(date);
        return currentYear;
    }


    public void onClickPlay(ClickEvent e){

        Timer timer = new Timer() {
            int minYear = startYear;

            public void run() {
                if (minYear < endYear) {
                    String facetField = filterByDateRangeSettings.filterFieldName;
                    FilterChangedEvent filterChangedEvent = new FilterChangedEvent(facetField);

                    Date maxDate = DataType.ParseUtil.parseDateEndYearOnly(2014);

                    yuiDualSliderGwtWidget.setRange(minYear, 2014);
                    Date minDate = DataType.ParseUtil.parseDateStartYearOnly(minYear);
                    filterChangedEvent.addFilter(minDate, maxDate);
                    clientFactory.getEventBus().fireEvent(filterChangedEvent);
                    minYear = minYear + 3;

                }else{
                    this.cancel();
                }
            }
        };

        // Execute the timer to expire some seconds in the future
        timer.scheduleRepeating(5000);

    }

}
