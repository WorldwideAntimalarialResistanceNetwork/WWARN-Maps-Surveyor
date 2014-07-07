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
import com.google.gwt.event.logical.shared.ShowRangeEvent;
import com.google.gwt.event.logical.shared.ShowRangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.wwarn.mapcore.client.components.customwidgets.YuiSingleSliderGwtWidgetWithLabelMarkersImpl;
import org.wwarn.surveyor.client.model.FilterConfig;
import org.wwarn.surveyor.client.model.FilterSetting;
import org.wwarn.surveyor.client.mvp.ClientFactory;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;
import org.wwarn.surveyor.client.event.FilterChangedEvent;

import java.util.List;

/**
 * Created by suay on 5/12/14.
 * This widget filters studies by a minimum sample size,
 * but it could be used to filter any other field.
 */
public class SampleSizeSliderComposite extends Composite {

    private static Integer MIN_SAMPLE_SIZE = 0;
    int minSampleSize, maxSampleSize;
    private int currentMinSampleSize;
    private FlowPanel flowPanel;
    private ClientFactory clientFactory = SimpleClientFactory.getInstance();

    private FilterConfig.FilterBySampleSizeSettings filterBySampleSizeSettings;

    interface SampleSizeSliderCompositeUiBinder extends UiBinder<FlowPanel, SampleSizeSliderComposite> {
    }

    private static SampleSizeSliderCompositeUiBinder ourUiBinder = GWT.create(SampleSizeSliderCompositeUiBinder.class);

    @UiField(provided = true)
    FlowPanel sampleSizeSliderPanel = new FlowPanel();

    @UiField(provided = true)
    final Label startSampleSizeLabel = new Label(MIN_SAMPLE_SIZE.toString());

    YuiSingleSliderGwtWidgetWithLabelMarkersImpl sampleSizeSliderYuiWidgetImpl;


    @UiConstructor
    public SampleSizeSliderComposite(String fieldName) {
        flowPanel = ourUiBinder.createAndBindUi(this);
        initWidget(flowPanel);
        this.setupConfig(fieldName);
    }

    public void setupConfig(String fieldName){
        FilterConfig filterConfig = clientFactory.getApplicationContext().getConfig(FilterConfig.class);
        List<FilterSetting> filterSettings = filterConfig.getFilterConfigBy(fieldName);
        FilterSetting filterSetting = filterSettings.get(0);

        filterBySampleSizeSettings = (FilterConfig.FilterBySampleSizeSettings) filterSetting;
        minSampleSize = filterBySampleSizeSettings.getStart();
        maxSampleSize = filterBySampleSizeSettings.getEnd();
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        sampleSizeSliderYuiWidgetImpl = setupSampleSizeWidget();
        sampleSizeSliderPanel.add((Widget) sampleSizeSliderYuiWidgetImpl);
    }

    public YuiSingleSliderGwtWidgetWithLabelMarkersImpl setupSampleSizeWidget(){
        sampleSizeSliderYuiWidgetImpl = new YuiSingleSliderGwtWidgetWithLabelMarkersImpl(400, 30, minSampleSize, maxSampleSize, 400, 10);

        final ValueChangeHandler<Integer> valueChangeHandler = new ValueChangeHandler<Integer>() {

            public void onValueChange(ValueChangeEvent<Integer> sizeRangeValueChangeEvent) {
                startSampleSizeLabel.setText(sizeRangeValueChangeEvent.getValue().toString());
            }
        };

        final ShowRangeHandler<Integer> showRangeChangeHandler = new ShowRangeHandler<Integer>() {
            private String facetField = filterBySampleSizeSettings.filterFieldName;

            public void onShowRange(ShowRangeEvent<Integer> integerShowRangeEvent) {
                FilterChangedEvent filterChangedEvent = new FilterChangedEvent(facetField);
                if (!integerShowRangeEvent.getStart().equals(currentMinSampleSize)) {
                    currentMinSampleSize = integerShowRangeEvent.getStart();
                    filterChangedEvent.addFilter(currentMinSampleSize);
                }else{
                    filterChangedEvent.resetField();
                }
                clientFactory.getEventBus().fireEvent(filterChangedEvent);
            }
        };
        sampleSizeSliderYuiWidgetImpl.addShowRangeHandler(showRangeChangeHandler);
        sampleSizeSliderYuiWidgetImpl.addValueChangeHandler(valueChangeHandler);

        return sampleSizeSliderYuiWidgetImpl;
    }

    public int getMinSampleSize() {
        return minSampleSize;
    }

    public void setMinSampleSize(int minSampleSize) {
        this.minSampleSize = minSampleSize;
    }

    public int getMaxSampleSize() {
        return maxSampleSize;
    }

    public void setMaxSampleSize(int maxSampleSize) {
        this.maxSampleSize = maxSampleSize;
    }
}
