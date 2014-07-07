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


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * User: raok
 * Date: 25-Feb-2010
 * Time: 08:59:25
 */
public class UiHelpToolTip extends Composite {

    final private Image helpIcon = new Image(GWT.getModuleBaseForStaticFiles()+"images/helpicon.png");
    final private PopupPanel toolTip = new PopupPanel(true);

    final private HTML helpHTML;
    private final boolean showBottomLeft;

    public UiHelpToolTip(HTML helpHTML) {
        this(helpHTML, false);
    }

    public UiHelpToolTip(HTML helpHTML, boolean showBottomLeft) {

        this.helpHTML = helpHTML;
        this.showBottomLeft = showBottomLeft;

        setupToolTip();

        setupIcon();

        initWidget(helpIcon);
    }

    private void setupToolTip() {
        toolTip.setWidget(helpHTML);
        toolTip.setStyleName("popups");
    }

    private void setupIcon() {
        helpIcon.setStyleName("linkLabel-fainter");
        helpIcon.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                toolTip.show();

                if (showBottomLeft) {
                    toolTip.setPopupPosition(helpIcon.getAbsoluteLeft() - toolTip.getOffsetWidth(), helpIcon.getAbsoluteTop() + helpIcon.getOffsetHeight());
                } else {
                    toolTip.setPopupPosition(helpIcon.getAbsoluteLeft() + helpIcon.getOffsetWidth(), helpIcon.getAbsoluteTop() - toolTip.getOffsetHeight() + 30);
                }
            }
        });
    }
}