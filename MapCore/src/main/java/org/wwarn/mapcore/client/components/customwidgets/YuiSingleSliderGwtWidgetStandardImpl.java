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


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ShowRangeEvent;
import com.google.gwt.event.logical.shared.ShowRangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * User: raok
 * Date: 18-Jun-2010
 * Time: 09:46:52
 */
public class YuiSingleSliderGwtWidgetStandardImpl extends AbstractYuiSliderGwtWidget implements YuiSingleSliderGwtWidget {


    public YuiSingleSliderGwtWidgetStandardImpl(Integer sliderWidthPx, Integer sliderHeightPx, Integer minValue, Integer maxValue, Integer tickNumInterval) {
        super(sliderWidthPx, sliderHeightPx, minValue, maxValue, tickNumInterval);
        GWT.log("YuiSingleSliderGwtWidgetStandardImpl::constructor");
    }

    @Override
    protected void createSliderHtmlElements(Integer sliderWidth, Integer sliderHeight) {
        GWT.log("createSliderHtmlElements");
        String sliderHeightPx = sliderHeight+"px", sliderWidthPx = sliderWidth + "px";

        //Set up divs as needed by YUI
        this.getElement().setInnerHTML(
                "<div id='" + yuiSliderDivId + "' " +
                        "style='height:" + sliderHeightPx + "; width:" + sliderWidthPx + ";  position:relative;" +
                            " background: url(" + SLIDER_IMAGE_LOCATION + ") repeat-x scroll 0px " + SLIDER_TOP_POSITION + ";' >" +
                    "<div id='" + minSliderDivId + "' style='position:absolute; cursor:pointer; cursor:hand;'>" +
                        "<img src='" + THUMB_IMAGE_LOCATION + "' alt='Min value'/>" +
                    "</div>" +
                "</div>"
        );

    }

    @Override
    protected void createSlider() {
        GWT.log("YuiSingleSliderGwtWidgetStandardImpl::createSlider");

        createYuiSingleSlider(yuiSliderDivId, minSliderDivId, sliderWidthPx, tickSizePx, sliderNamespace);
        sliderCreated = true;

        //setValue if required
        if (minRangeStartupValue != null) {
            setValue(minRangeStartupValue);
        }

    }

    private native void createYuiSingleSlider(String yuiSliderDivId, String minSliderDivId,
                                              int sliderWidthPx, int tickSizePx, String thisYuiSliderNamespace) /*-{

        var thisGwtObj = this;

        //Create YUI dual slider
        (function(){

            var namespace = $wnd.YAHOO.namespace(thisYuiSliderNamespace);

            var singleRangeSlider = $wnd.YAHOO.widget.Slider.getHorizSlider(
                    yuiSliderDivId, minSliderDivId, 0, sliderWidthPx, tickSizePx);

            //fire whenever slider moves
            var changeHandler = function() {
              thisGwtObj.@org.wwarn.mapcore.client.components.customwidgets.YuiSingleSliderGwtWidgetStandardImpl::fireValueChangeEvent(I)(singleRangeSlider.getValue());
            };

            //fire when user has finished moving slider
            var slideEndHandler = function() {
                thisGwtObj.@org.wwarn.mapcore.client.components.customwidgets.YuiSingleSliderGwtWidgetStandardImpl::fireRangeChangeEvent(I)(singleRangeSlider.getValue());
            };

            //subscribe handlers
            singleRangeSlider.subscribe('change', changeHandler);
            singleRangeSlider.subscribe('slideEnd', slideEndHandler);

            //disable click on background to move thumb
            singleRangeSlider.backgroundEnabled = false;

            // Attach the slider to the YAHOO.example namespace for public probing
            namespace.slider = singleRangeSlider;

        })();


    }-*/;

    @Override
    public void setValue(Integer minRangeValue)
    {
        GWT.log("setValue, minRangeValue: " + minRangeValue);
        // ensure an invalid range isn't given
        minRangeValue = (minRangeValue < maxValue ? minRangeValue : maxValue);

        //defer setting Range if required
        if (sliderCreated) {
            updateSliderValue(calcValuePosition(minRangeValue), sliderNamespace);
        } else {
            minRangeStartupValue = minRangeValue;
        }
    }
               
    private native void updateSliderValue(int minRangeValuePx, String thisYuiSliderNamespace) /*-{

        //get namespace
        var namespace = $wnd.YAHOO.namespace(thisYuiSliderNamespace);

        // Set the max/min selected vals on the slider
        var skipAnim = true;
        var force = false;
        var silent = true;
        namespace.slider.setValue(minRangeValuePx, skipAnim, force, silent);
        
    }-*/;


    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> integerValueChangeHandler) {
        GWT.log("addValueChangeHandler");
        return addHandler(integerValueChangeHandler, ValueChangeEvent.getType());
    }

    private void fireValueChangeEvent(int minValuePx) {
        GWT.log("fireValueChangeEvent: minValuePx:" + minValuePx);

        int minVal = calcValue(minValuePx);

        GWT.log("fireRangeChangeEvent fired: minVal:" + minVal);

        ValueChangeEvent.fire(this, minVal);

    }

    @Override
    public HandlerRegistration addShowRangeHandler(ShowRangeHandler<Integer> integerShowRangeHandler) {
        GWT.log("addShowRangeHandler");
        return addHandler(integerShowRangeHandler, ShowRangeEvent.getType());
    }

    private void fireRangeChangeEvent(int minPx) {
        GWT.log("fireRangeChangeEvent: minPx:" + minPx);

        int minVal = calcValue(minPx);

        GWT.log("fireRangeChangeEvent fired: minVal:" + minVal);

        ShowRangeEvent.fire(this, minVal, minVal);
    }

}
