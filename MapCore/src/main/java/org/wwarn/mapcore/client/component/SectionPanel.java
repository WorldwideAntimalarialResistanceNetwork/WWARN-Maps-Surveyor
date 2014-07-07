package org.wwarn.mapcore.client.component;

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


import com.google.gwt.user.client.ui.*;

/**
 * User: raok
 * Date: 16-Mar-2010
 * Time: 17:47:41
 */
public abstract class SectionPanel extends Composite {
    protected final SimplePanel helpIconPlaceHolder = new SimplePanel();
    protected final Label headingLabel = new Label();
    protected final SimplePanel contentPlaceHolder = new SimplePanel();
    protected final SimplePanel headingPlaceHolder =  new SimplePanel();

    protected abstract void styleSectionPanel(HorizontalPanel titleBar, VerticalPanel mainPanel);

    public SectionPanel(Widget mainContentWidget, String headingText, UiHelpToolTip helpIcon) {

        this();

        headingLabel.setText(headingText);
        headingLabel.setStyleName("sectionPanelHeading");
        helpIconPlaceHolder.setWidget(helpIcon);
        contentPlaceHolder.setWidget(mainContentWidget);

    }

    public SectionPanel() {

        //layout
        HorizontalPanel titleBar = new HorizontalPanel();
        titleBar.add(headingLabel);
        titleBar.add(headingPlaceHolder);
        titleBar.setCellHorizontalAlignment(headingPlaceHolder, HasHorizontalAlignment.ALIGN_LEFT);
        titleBar.setCellVerticalAlignment(headingPlaceHolder, HasVerticalAlignment.ALIGN_MIDDLE);
        //titleBar.setCellWidth(headingLabel, "100%");
        titleBar.add(helpIconPlaceHolder);
        titleBar.setCellHorizontalAlignment(helpIconPlaceHolder, HasHorizontalAlignment.ALIGN_RIGHT);
        titleBar.setCellVerticalAlignment(helpIconPlaceHolder, HasVerticalAlignment.ALIGN_MIDDLE);
        titleBar.setSpacing(3);
        titleBar.setWidth("100%");

        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(titleBar);
        mainPanel.add(contentPlaceHolder);

        styleSectionPanel(titleBar, mainPanel);

        initWidget(mainPanel);
    }

    public void add(Widget mainContentWidget, String headingText, UiHelpToolTip helpIcon) {

        headingLabel.setText(headingText);
        helpIconPlaceHolder.setWidget(helpIcon);
        contentPlaceHolder.setWidget(mainContentWidget);

    }

    public void add(Widget mainContentWidget, String headingText) {

        headingLabel.setText(headingText);
        contentPlaceHolder.setWidget(mainContentWidget);

    }

    public void add(UiHelpToolTip helpIcon) {

        helpIconPlaceHolder.setWidget(helpIcon);

    }
}
