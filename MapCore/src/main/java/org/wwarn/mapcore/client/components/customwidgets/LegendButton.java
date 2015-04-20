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

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.wwarn.mapcore.client.i18nstatic.MapTextConstants;

/**
 * Created by suay on 5/23/14.
 */
public class LegendButton extends Composite {

    public static final String minimizeString = " <";
    public static final String maximizeString = " >";

    Button legendButton = new Button();

    Image legendImg;

    boolean isLegendDisplayed;

    HorizontalPanel panel = new HorizontalPanel();

    int legendWidth;

    String legendWord;

    public LegendButton(String relativeImagePath ){

        legendImg = new Image(GWT.getModuleBaseForStaticFiles() + relativeImagePath);
        setLegendButtonText();
        legendButton.addClickHandler(legendClickHandler);
        legendButton.setStyleName("legendButton");

        isLegendDisplayed = true;
        panel.add(legendButton);
        panel.add(legendImg);
        initWidget(panel);
    }

    private void setLegendButtonText(){
        MapTextConstants mapTextConstants = GWT.create(MapTextConstants.class);
        legendWord = mapTextConstants.legend();
        legendButton.setText(legendWord+minimizeString);
    }

    ClickHandler legendClickHandler =  new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    ResizeWidthAnimation resizeAnimation;
                    if(isLegendDisplayed){
                        legendWidth = legendImg.getWidth();
                        isLegendDisplayed = false;
                        resizeAnimation = new ResizeWidthAnimation(0);
                        resizeAnimation.run(500);
                        legendButton.setText(legendWord+maximizeString);
                    }else{
                        isLegendDisplayed = true;
                        resizeAnimation = new ResizeWidthAnimation(legendWidth);
                        resizeAnimation.run(500);
                        legendButton.setText(legendWord+minimizeString);

                    }
                }
            };


    public class ResizeWidthAnimation  extends Animation {
        // initial size of widget
        private int startWidth = 0;
        // desired size of widget. Widget will have this size after animation will stop to run
        private int desiredWidth = 0;

        public ResizeWidthAnimation(int desiredWidth) {
            this.startWidth = legendImg.getOffsetWidth();
            this.desiredWidth = desiredWidth;
        }
        @Override
        protected void onUpdate(double progress) {
            double width = extractProportionalLength(progress) ;
            legendImg.setWidth( width + Style.Unit.PX.getType());
        }
        private double extractProportionalLength(double progress) {
            double outWidth = startWidth - (startWidth - desiredWidth) * progress;
            return outWidth;
        }
    }

}
