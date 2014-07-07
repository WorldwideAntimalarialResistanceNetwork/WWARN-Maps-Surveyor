package org.wwarn.mapcore.client.panel;

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

/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * User: apayne
 * Date: 11/11/11
 * Time: 13:03
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.Widget;
import org.wwarn.mapcore.client.panel.resources.TooltipResources;

public class Tooltip {

    private static final TooltipResources res = GWT.create(TooltipResources.class);

    public void setWidth(String s) {
        panel.setWidth(s);
    }

    public static enum TooltipPosition {
        CURSOR,
        BELOW_LEFT,
        BELOW_CENTER,
        BELOW_RIGHT,
        ABOVE_LEFT,
        ABOVE_CENTER,
        ABOVE_RIGHT,
        LEFT_TOP,
        LEFT_MIDDLE,
        LEFT_BOTTOM,
        RIGHT_TOP,
        RIGHT_MIDDLE,
        RIGHT_BOTTOM
    }

    private final PopupPanel panel = new PopupPanel();
    private HandlerRegistration moveHandlerRegistration;

    private int mouseX, mouseY;
    private Widget widget;

    private final Timer timer = new Timer() {

        @Override
        public void run() {

            if (position == TooltipPosition.CURSOR) {
                moveHandlerRegistration.removeHandler();
            }

            showTooltip();
        }
    };

    private final MouseOutHandler mouseOutHandler = new MouseOutHandler() {

        @Override
        public void onMouseOut(MouseOutEvent event) {
            timer.cancel();

            if (panel.isShowing() && isMouseOutOfWidget(event, widget) && isMouseOutOfWidget(event, panel)) {
                panel.hide();
            }

        }
    };

    private boolean isMouseOutOfWidget(MouseEvent<?> event, Widget widget) {
        int x = event.getClientX();
        int y = event.getClientY();
        return x <= widget.getAbsoluteLeft() || x >= widget.getAbsoluteLeft() + widget.getOffsetWidth() ||
                y <= widget.getAbsoluteTop() || y >= widget.getAbsoluteTop() + widget.getOffsetHeight();
    }

    private final MouseOverHandler handler = new MouseOverHandler() {

        @Override
        public void onMouseOver(final MouseOverEvent event) {

            if (widget == event.getSource() && panel.isShowing()) {
                return;
            }

            widget = (Widget) event.getSource();

            if (position == TooltipPosition.CURSOR) {
                mouseX = event.getClientX();
                mouseY = event.getClientY();

                moveHandlerRegistration = widget.addDomHandler(new MouseMoveHandler() {

                    @Override
                    public void onMouseMove(MouseMoveEvent event) {
                        mouseX = event.getClientX();
                        mouseY = event.getClientY();
                    }
                }, MouseMoveEvent.getType());
            }

            timer.schedule(400);

        }
    };

    private TooltipPosition position = TooltipPosition.CURSOR;

    public Tooltip() {
        panel.addDomHandler(mouseOutHandler, MouseOutEvent.getType());
        res.style().ensureInjected();
        panel.setStyleName(res.style().tooltip());
    }

    public void setHtml(SafeHtml html) {
        panel.setWidget(new HTML(html));
        panel.setWidth("350");
    }

    public void setPosition(TooltipPosition position) {
        this.position = position;
    }

    public void attachTo(Widget widget) {
        HandlerRegistration mouseOverHandlerRegistration = widget.addDomHandler(handler, MouseOverEvent.getType());
        HandlerRegistration mouseOutHandlerRegistration = widget.addDomHandler(mouseOutHandler, MouseOutEvent.getType());
    }

    private void showTooltip() {
        panel.setPopupPositionAndShow(new PositionCallback() {
            public void setPosition(int offsetWidth, int offsetHeight) {

                int x = 0, y = 0;

                final int widgetX = widget.getAbsoluteLeft();
                final int widgetY = widget.getAbsoluteTop();
                final int widgetWidth = widget.getOffsetWidth();
                final int widgetHeight = widget.getOffsetHeight();

                switch (position) {
                    case CURSOR:
                        x = mouseX;
                        y = mouseY;
                        break;
                    case ABOVE_CENTER:
                        x = widgetX + (widgetWidth - offsetWidth) / 2;
                        y = widgetY - offsetHeight + 1;
                        break;
                    case ABOVE_LEFT:
                        x = widgetX;
                        y = widgetY - offsetHeight + 1;
                        break;
                    case ABOVE_RIGHT:
                        x = widgetX + widgetWidth - offsetWidth;
                        y = widgetY - offsetHeight + 1;
                        break;
                    case BELOW_CENTER:
                        x = widgetX + (widgetWidth - offsetWidth) / 2;
                        y = widgetY + widgetHeight - 1;
                        break;
                    case BELOW_LEFT:
                        x = widgetX;
                        y = widgetY + widgetHeight - 1;
                        break;
                    case BELOW_RIGHT:
                        x = widgetX + widgetWidth - offsetWidth;
                        y = widgetY + widgetHeight - 1;
                        break;
                    case LEFT_TOP:
                        x = widgetX - offsetWidth + 1;
                        y = widgetY;
                        break;
                    case LEFT_MIDDLE:
                        x = widgetX - offsetWidth + 1;
                        y = widgetY + (widgetHeight - offsetHeight) / 2;
                        break;
                    case LEFT_BOTTOM:
                        x = widgetX - offsetWidth + 1;
                        y = widgetY + widgetHeight - offsetHeight;
                        break;
                    case RIGHT_TOP:
                        x = widgetX + widgetWidth - 1;
                        y = widgetY;
                        break;
                    case RIGHT_MIDDLE:
                        x = widgetX + widgetWidth - 1;
                        y = widgetY + (widgetHeight - offsetHeight) / 2;
                        break;
                    case RIGHT_BOTTOM:
                        x = widgetX + widgetWidth - 1;
                        y = widgetY + widgetHeight - offsetHeight;
                        break;
                }

                panel.setPopupPosition(x, y);
            }
        });
    }
}
