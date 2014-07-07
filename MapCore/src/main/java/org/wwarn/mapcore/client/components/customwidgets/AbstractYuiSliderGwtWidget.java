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
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

/**
 * User: raok
 * Date: 18-Jun-2010
 * Time: 09:18:02
 */
public abstract class AbstractYuiSliderGwtWidget extends Widget {

    //Slider image and thumb images need to be chosen carefully due to limitations with CSS2 preventing things lining up properly
    protected final static String SLIDER_IMAGE_LOCATION = GWT.getModuleBaseForStaticFiles()+"images/standard_html_load/bg-h-repeater.gif";
    protected final static String THUMB_IMAGE_LOCATION = GWT.getModuleBaseForStaticFiles()+"images/standard_html_load/slider-thumb.gif";

    //Note these are dependant on the thumb image chosen
    final static int THUMB_IMAGE_WIDTH = 11;
    protected final static String SLIDER_TOP_POSITION = "8px";

    //create unique ids for slider elements
    private static int yuiSliderId = 0;
    protected int thisYuiSliderId = ++yuiSliderId;
    protected final String yuiSliderDivId = "yui_slider_div_" + thisYuiSliderId;
    protected final String minSliderDivId = "min_slider_div_" + thisYuiSliderId;
    protected final String sliderNamespace = "wwarn_" + thisYuiSliderId;  //used in GWT native function

    //constructor params
    protected final Integer sliderWidthPx;
    protected final Integer minValue;
    protected final Integer maxValue;
    protected Integer tickNumInterval;
    protected final Integer tickSizePx;

    //used in handling async loading of YUI dependencies
    private static boolean attemptedLoadDependencies = false;
    protected static boolean loadedYuiDependencies = false;
    protected boolean sliderCreated = false;
    protected Integer minRangeStartupValue;
    protected static List<AbstractYuiSliderGwtWidget> sliders = new ArrayList<AbstractYuiSliderGwtWidget>();

    //NOTE: slider width may be reduced to accommodate a whole number of 'ticks'.
    protected AbstractYuiSliderGwtWidget(Integer sliderWidthPx, Integer sliderHeightPx, Integer minValue, Integer maxValue, Integer tickNumInterval) {
        super();
        GWT.log("AbstractYuiSliderGwtWidget::constructor");
        this.sliderWidthPx = sliderWidthPx;
        GWT.log("sliderWidthPx: " + sliderWidthPx);
        GWT.log("sliderHeightPx: " + sliderHeightPx);
        this.minValue = minValue;
        GWT.log("minValue: " + minValue);
        this.maxValue = maxValue;
        GWT.log("maxValue: " + maxValue);
        this.tickNumInterval = tickNumInterval;
        GWT.log("tickNumInterval: " + tickNumInterval);


        //calculate tick size for the slider
        int maxRange = (maxValue - minValue);
        if (tickNumInterval < sliderWidthPx) {
            tickSizePx = (int)Math.floor((sliderWidthPx*1.0/maxRange)* tickNumInterval);
        } else {
            throw new IllegalArgumentException("tickNumInterval should be less than slider width");
        }
        GWT.log("tickSizePx: " + tickSizePx);

        //reduce width if required
        if ( sliderWidthPx%tickSizePx != 0 ) {
            sliderWidthPx = tickSizePx*maxRange;
            GWT.log("reduced sliderWidthPx: " + sliderWidthPx);
        }

        //setup required html elements.
        this.setElement(Document.get().createDivElement());
        createSliderHtmlElements(sliderWidthPx, sliderHeightPx);

        //set height of slider div
        this.setHeight(sliderHeightPx.toString());

        //create slider if dependencies have loaded, else leave to class to initialize
        if (loadedYuiDependencies) {
            createSlider();
        } else {
            GWT.log("Deferred creation of this slider");
            sliders.add(this);
        }

        invokeYuiDependenciesLoad();

    }

    protected static void invokeYuiDependenciesLoad() {

        //dynamically load dependencies
        if (!attemptedLoadDependencies) {
            attemptedLoadDependencies = true;
            GWT.log("Loading YUI slider dependencies...");
            downloadYuiSliderDependencies();
        }

    }

    private static native void downloadYuiSliderDependencies() /*-{

        var loader = new $wnd.YAHOO.util.YUILoader({
            require: ["slider"],
            loadOptional: false,
            timeout: 120000, //2 minutes
            // Combine YUI files into a single request (per file type) by using the Yahoo! CDN combo service.
            combine: true,
            allowRollup: true,
            onSuccess: function() {
              @org.wwarn.mapcore.client.components.customwidgets.YuiDualSliderGwtWidgetStandardImpl::createSliders()();
            },
            onFailure: function() {
              @org.wwarn.mapcore.client.components.customwidgets.YuiDualSliderGwtWidgetStandardImpl::slidersCreationError()();
            },
            onTimeout: function() {
              @org.wwarn.mapcore.client.components.customwidgets.YuiDualSliderGwtWidgetStandardImpl::slidersCreationError()();
            }
        });

        loader.insert();

    }-*/;

    protected static void slidersCreationError() {
        GWT.log("sliderCreationError: YUI slider could not be created.");
        throw new IllegalStateException("YUI slider could not be created.");
    }

    protected static void createSliders() {
        GWT.log("createSliders");
        loadedYuiDependencies = true;

        for (AbstractYuiSliderGwtWidget slider : sliders) {
            slider.createSlider();
        }

    }

    protected abstract void createSlider();

    protected int calcValue(int posPx) {
        return (posPx/(tickSizePx/ tickNumInterval)) + minValue;
    }

    protected int calcValuePosition(int value)
    {
        return (value - minValue) * (tickSizePx/ tickNumInterval);
    }

    @Override
    public int getOffsetWidth() {
        return super.getOffsetWidth() + THUMB_IMAGE_WIDTH;
    }

    @Override
    public int getOffsetHeight() {
        return super.getOffsetHeight();
    }

    public Integer getTickSizePx() {
        return tickSizePx;
    }

    protected abstract void createSliderHtmlElements(Integer sliderWidthPx, Integer sliderHeightPx);
}
