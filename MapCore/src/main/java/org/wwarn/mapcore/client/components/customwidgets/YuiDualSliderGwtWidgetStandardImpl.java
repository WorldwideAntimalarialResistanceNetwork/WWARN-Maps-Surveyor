package org.wwarn.mapcore.client.components.customwidgets;

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


import org.wwarn.mapcore.client.common.types.Range;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ShowRangeEvent;
import com.google.gwt.event.logical.shared.ShowRangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * User: raok
 * Date: 08-Feb-2010
 * Time: 12:53:39
 */
public class YuiDualSliderGwtWidgetStandardImpl extends AbstractYuiSliderGwtWidget implements YuiDualSliderGwtWidget {

    private String maxSliderDivId;
    private Integer maxRangeStartupValue;

    public YuiDualSliderGwtWidgetStandardImpl(Integer sliderWidthPx, Integer sliderHeightPx, Integer minValue, Integer maxValue, Integer tickNumInterval) {
        super(sliderWidthPx, sliderHeightPx, minValue, maxValue, tickNumInterval);
        GWT.log("YuiDualSliderGwtWidgetStandardImpl::constructor");

    }

    @Override
    protected void createSliderHtmlElements(Integer sliderWidth, Integer sliderHeight) {
        GWT.log("createSliderHtmlElements");

        maxSliderDivId = "max_slider_div_" + thisYuiSliderId;
        String sliderHeightPx = sliderHeight+"px", sliderWidthPx = sliderWidth + "px";
        //Set up divs as needed by YUI
        this.getElement().setInnerHTML(
                "<div id='" + yuiSliderDivId + "' " +
                        "style='height:" + sliderHeightPx + "; width:" + sliderWidthPx + ";  position:relative;" +
                            " background: url(" + SLIDER_IMAGE_LOCATION + ") repeat-x scroll 0px " + SLIDER_TOP_POSITION + ";' >" +
                    "<div id='" + minSliderDivId + "' style='position:absolute; cursor:pointer; cursor:hand;'>" +
                        "<img src='" + THUMB_IMAGE_LOCATION + "' alt='Min value'/>" +
                    "</div>" +
                    "<div id='" + maxSliderDivId + "' style='position:absolute; cursor:pointer; cursor:hand;'>" +
                        "<img src='" + THUMB_IMAGE_LOCATION + "' alt='Max value'/>" +
                    "</div>" +
                "</div>"
        );

    }

    @Override
    protected void createSlider() {
        GWT.log("YuiDualSliderGwtWidgetStandardImpl::createSlider");

        //couldn't find a way of accessing GWT Java variables within JSNI. Refactor if possible.
        createYuiDualSliderWidget(yuiSliderDivId, minSliderDivId, maxSliderDivId, sliderWidthPx, tickSizePx, sliderNamespace);

        sliderCreated = true;

        //setValue if required
        if (minRangeStartupValue != null) {
            setRange(minRangeStartupValue, maxRangeStartupValue);
        }
    }

    private native void createYuiDualSliderWidget(String yuiSliderDivId, String minSliderDivId,
                                              String maxSliderDivId, int sliderWidthPx, int tickSizePx, String thisYuiSliderNamespace) /*-{
        var thisGwtObj = this;

        //Create YUI dual slider
        (function(){

            var namespace = $wnd.YAHOO.namespace(thisYuiSliderNamespace);

            var dualRangeSlider = $wnd.YAHOO.widget.Slider.getHorizDualSlider(
                    yuiSliderDivId, minSliderDivId, maxSliderDivId, sliderWidthPx, tickSizePx);

            //fire whenever slider moves
            var changeHandler = function() {
              thisGwtObj.@org.wwarn.mapcore.client.components.customwidgets.YuiDualSliderGwtWidgetStandardImpl::fireValueChangeEvent(II)(dualRangeSlider.minVal, dualRangeSlider.maxVal);
            };

            //fire when user has finished moving slider
            var slideEndHandler = function() {
                thisGwtObj.@org.wwarn.mapcore.client.components.customwidgets.YuiDualSliderGwtWidgetStandardImpl::fireRangeChangeEvent(II)(dualRangeSlider.minVal, dualRangeSlider.maxVal);
            };

            //subscribe handlers
            dualRangeSlider.subscribe('change', changeHandler);
            dualRangeSlider.subscribe('slideEnd', slideEndHandler);

            //disable click on background to move thumb
            dualRangeSlider.maxSlider.backgroundEnabled = false;
            dualRangeSlider.minSlider.backgroundEnabled = false;

            // Attach the slider to the YAHOO.example namespace for public probing
            namespace.slider = dualRangeSlider;

        })();

    }-*/;

    @Override
    public void setRange(Integer minRangeValue, Integer maxRangeValue)
    {
        GWT.log("setRange, minRangeValue: " + minRangeValue + " , maxRangeValue: " + maxRangeValue);
        // ensure an invalid range isn't given
        maxRangeValue = (maxRangeValue > minRangeValue ? maxRangeValue : maxValue);

        //defer setting Range if required
        if (sliderCreated) {
            updateSliderRange(calcValuePosition(minRangeValue), calcValuePosition(maxRangeValue), sliderNamespace);
        } else {
            minRangeStartupValue = minRangeValue;
            maxRangeStartupValue = maxRangeValue;
        }
    }

    private native void updateSliderRange(int minRangeValuePx, int maxRangeValuePx, String thisYuiSliderNamespace) /*-{

        //get namespace
        var namespace = $wnd.YAHOO.namespace(thisYuiSliderNamespace);

        // Set the max/min selected vals on the slider    
        var skipAnim = true;
        var force = false;
        var silent = true;

        //NOTE: setValues ignores the silent param! So need to explicitly set each one separately.
        namespace.slider.setMinValue(minRangeValuePx, skipAnim, force, silent);
        namespace.slider.setMaxValue(maxRangeValuePx, skipAnim, force, silent);
        
    }-*/;

    @Override
    public HandlerRegistration addShowRangeHandler(ShowRangeHandler<Integer> integerShowRangeHandler) {
        GWT.log("addShowRangeHandler");
        return addHandler(integerShowRangeHandler, ShowRangeEvent.getType());
    }

    private void fireRangeChangeEvent(int minPx, int maxPx) {
        GWT.log("fireRangeChangeEvent: minPx:" + minPx  + ", maxPx:" + maxPx);

        int minVal = calcValue(minPx);
        int maxVal = calcValue(maxPx);

        GWT.log("fireRangeChangeEvent fired: minVal:" + minVal  + ", maxVal:" + maxVal);

        ShowRangeEvent.fire(this, minVal, maxVal);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Range<Integer>> rangeValueChangeHandler) {
        GWT.log("addValueChangeHandler");
        return addHandler(rangeValueChangeHandler, ValueChangeEvent.getType());
    }

    private void fireValueChangeEvent(int minRangeValuePx, int maxRangeValuePx) {
        GWT.log("fireValueChangeEvent: minRangeValuePx:" + minRangeValuePx + ", maxRangeValuePx:" + maxRangeValuePx);

        int minRangeVal = calcValue(minRangeValuePx);
        int maxRangeVal = calcValue(maxRangeValuePx);

        GWT.log("fireRangeChangeEvent fired: minRangeVal:" + minRangeVal + ", maxRangeVal:" + maxRangeVal);

        ValueChangeEvent.fire(this, new Range<Integer>(minRangeVal, maxRangeVal));

    }

}
