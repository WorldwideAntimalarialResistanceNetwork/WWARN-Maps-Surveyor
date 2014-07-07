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


import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

import java.util.ArrayList;

/**
 * User: raok
 * Date: 18-Jun-2010
 * Time: 11:51:27
 */
public class AbstractYuiSliderGwtWidgetWithLabelMarkers extends Composite {

    protected AbstractYuiSliderGwtWidget yuiSliderGwtWidget;
    protected final AbsolutePanel mainAbsolutePanel = new AbsolutePanel();
    protected final Integer minRange;
    protected final Integer maxRange;
    protected final Integer labelInterval;
    private static final int TICK_LABEL_TOP_POS_CORRECTION = 10;
    protected final Integer tickInterval;

    public AbstractYuiSliderGwtWidgetWithLabelMarkers(Integer minRange, Integer maxRange, Integer tickInterval, Integer labelInterval) {
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.tickInterval = tickInterval;
        this.labelInterval = labelInterval;
    }


    //override onLoad to know when absolutePanel's size can be set
    @Override
    protected void onLoad() {

        mainAbsolutePanel.setSize(
                String.valueOf(yuiSliderGwtWidget.getOffsetWidth()),
                String.valueOf(yuiSliderGwtWidget.getOffsetHeight())
        );

        setupMarkerLabels();

    }

    private void setupMarkerLabels() {

        int numInterval = labelInterval;
        int tickLabelInterval = tickInterval;

        int labelInterval = yuiSliderGwtWidget.getTickSizePx();

        int tickLabelTopPos = yuiSliderGwtWidget.getOffsetHeight() - TICK_LABEL_TOP_POS_CORRECTION;

        int numLabelTopPos = yuiSliderGwtWidget.getOffsetHeight();

        ArrayList<Label> numLabels = new ArrayList<Label>();
        ArrayList<Label> tickLabels = new ArrayList<Label>();
        for (
                int posLeft = 0, tickNum = minRange;
                tickNum <= maxRange;
                posLeft += labelInterval, tickNum += tickLabelInterval
            )
        {

            if (tickNum%numInterval == 0) {

                Label numLabel = new Label(String.valueOf(tickNum));
                numLabel.addStyleName("rangeSliderNumLabel");
                mainAbsolutePanel.add(numLabel, posLeft, numLabelTopPos);

                //store to correct positions later
                numLabels.add(numLabel);
            }

            if (tickNum%tickLabelInterval == 0) {
                Label tickLabel = new Label("|");
                tickLabel.addStyleName("rangeSliderTickLabel");
                mainAbsolutePanel.add(tickLabel, posLeft, tickLabelTopPos);

                //store to correct positions later
                tickLabels.add(tickLabel);
            }

        }

        //shift slider to center on first numLabel center position
        int firstLabelsWidth = numLabels.get(0).getOffsetWidth();
        int centerSliderShift = (int) Math.round((firstLabelsWidth - AbstractYuiSliderGwtWidget.THUMB_IMAGE_WIDTH)/2.0);
        mainAbsolutePanel.setWidgetPosition(yuiSliderGwtWidget, centerSliderShift, 0);

        //shift ticks over to align with slider
        for (Label tickLabel : tickLabels) {

            int labelLeft = mainAbsolutePanel.getWidgetLeft(tickLabel);
            int labelTop = mainAbsolutePanel.getWidgetTop(tickLabel);

            int tickLeftPx = labelLeft + firstLabelsWidth/2 - tickLabel.getOffsetWidth()/2;

            mainAbsolutePanel.setWidgetPosition(tickLabel, tickLeftPx, labelTop);

        }

        //shift numberLabels, other than first, over to center on corresponding tick
        int widthOfFirstLabel = -1;
        for (Label numLabel : numLabels) {

            if (widthOfFirstLabel == -1) {
                widthOfFirstLabel = numLabel.getOffsetWidth();
            } else {

                int widthOfThisLabel = numLabel.getOffsetWidth();
                if (widthOfThisLabel > widthOfFirstLabel) {

                    int labelLeft = mainAbsolutePanel.getWidgetLeft(numLabel);

                    int numLeftPx = labelLeft - (widthOfThisLabel - widthOfFirstLabel)/2;

                    int labelTop = mainAbsolutePanel.getWidgetTop(numLabel);
                    mainAbsolutePanel.setWidgetPosition(numLabel, numLeftPx, labelTop);

                }

            }

        }

        //update mainAbsolutePanel size
        mainAbsolutePanel.setSize(
                String.valueOf(mainAbsolutePanel.getOffsetWidth() + firstLabelsWidth/2),
                String.valueOf(numLabelTopPos + numLabels.get(0).getOffsetHeight())
        );

    }

}
