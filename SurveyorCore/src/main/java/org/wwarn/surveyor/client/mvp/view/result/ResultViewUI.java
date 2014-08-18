package org.wwarn.surveyor.client.mvp.view.result;

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
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.wwarn.surveyor.client.core.RecordList;
import org.wwarn.surveyor.client.model.*;
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.surveyor.client.mvp.presenter.LoadStatusListener;
import org.wwarn.surveyor.client.mvp.presenter.ResultPresenter;
import org.wwarn.surveyor.client.mvp.view.template.TemplateBasedViewBuilder;
import org.wwarn.surveyor.client.mvp.view.map.MapViewComposite;
import org.wwarn.surveyor.client.mvp.view.panel.PanelViewComposite;
import org.wwarn.surveyor.client.mvp.view.table.CellTableServer;
import org.wwarn.surveyor.client.mvp.view.table.TableViewComposite;
import org.wwarn.surveyor.client.mvp.view.template.TemplateViewComposite;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for taking the view elements and creating tabs corresponding to each view
 */
public class ResultViewUI extends Composite implements ResultView {
    private ResultPresenter presenter;
    private static final PopupPanel toolTipPopup = new PopupPanel(true);

    @UiField(provided = true)
    final DecoratedTabBar vTabBar = new DecoratedTabBar();

    @UiField(provided = true)
    final SimplePanel tabContentHolder = new SimplePanel();
    private Widget[] loadedDisplays;
    private PopupPanel loadingPopup = new PopupPanel(false);

    public void setPresenter(ResultPresenter presenter) {
        this.presenter = presenter;
    }

    public Widget asWidget() {
        return this;
    }

    private List<Widget> widgets = new ArrayList<Widget>();

    public void setup(final ResultsViewConfig viewConfigs) {
        int viewConfigCount = 0;
        for (final ViewConfig viewConfig : viewConfigs) {
            viewConfigCount++;
            String tabName = viewConfig.getViewName();
            final HTMLPanel htmlPanel = new HTMLPanel("<FONT SIZE = 2>" + tabName + "</FONT>");
            htmlPanel.setWidth("150px");
            final FocusPanel focusPanel = new FocusPanel(htmlPanel);
            final String viewLabel = viewConfig.getViewLabel();
            if(!StringUtils.isEmpty(viewLabel)){
                focusPanel.addMouseOverHandler(new MouseOverHandler() {
                    @Override
                    public void onMouseOver(MouseOverEvent mouseOverEvent) {
                    toolTipPopup.setWidth("400px");
                    int left = focusPanel.getAbsoluteLeft();
                    int top = focusPanel.getAbsoluteTop() + focusPanel.getOffsetHeight();
                    toolTipPopup.setPopupPosition(left, top);
                    toolTipPopup.setWidget(new HTMLPanel(viewLabel));
                    toolTipPopup.show();
                    toolTipPopup.setAutoHideEnabled(true);
                    }
                });
                focusPanel.addMouseOutHandler(new MouseOutHandler() {
                    @Override
                    public void onMouseOut(MouseOutEvent mouseOutEvent) {
                        toolTipPopup.hide();
                    }
                });
            }
            vTabBar.addTab(focusPanel);
        }
        loadedDisplays = new Widget[viewConfigCount];
        vTabBar.addSelectionHandler(new SelectionHandler<Integer>() {
            public void onSelection(SelectionEvent<Integer> event) {
                // Determine the tab that has been selected by interrogating the event object.
                Integer tabSelected = event.getSelectedItem();
                presenter.onTabChange(tabSelected);

                initializeWidget(viewConfigs, tabSelected);
            }
        });
        tabContentHolder.getElement().setId("surveyorTabContentHolder");
    }

    private Widget initializeWidget(ResultsViewConfig viewConfigs, Integer tabSelected){
        int indexOfViewConfigs = 0;
        for (ViewConfig viewConfig : viewConfigs) {
            if(tabSelected == indexOfViewConfigs){
                 setupWidget(viewConfig, tabSelected);
            }
            indexOfViewConfigs++;
        }
        return new HTML("No content found");
    }

    private void setupWidget(final ViewConfig viewConfig, final Integer tabSelected) {
        final Widget[] widget = {new HTML()};
        final Widget defaultWidget = widget[0] = new HTML("<strong>Unable to load widget, async call failed. " +
                "Check network and try reloading.</strong>");
        // TODO tedious repetition of code, sort it out!
        if(viewConfig instanceof TableViewConfig){
            GWT.runAsync(new RunAsyncCallback() {
                @Override
                public void onFailure(Throwable throwable) {
                    widget[0] = defaultWidget;
                }

                @Override
                public void onSuccess() {
                    Widget table = loadedDisplays[tabSelected];
                    if(table == null){
                        TableViewConfig tableViewConfig = (TableViewConfig) viewConfig;
                        if (tableViewConfig.getType() == TableViewConfig.TableType.SERVER_TABLE){
                            table = new CellTableServer(tableViewConfig);
                        }else{
                            table = new TableViewComposite(tableViewConfig);
                        }
                        loadedDisplays[tabSelected] = table;
                    }
                    tabContentHolder.setWidget(table);
                }
            });
        }
        if(viewConfig instanceof MapViewConfig){
            GWT.runAsync(new RunAsyncCallback() {
                @Override
                public void onFailure(Throwable throwable) {
                    widget[0] = defaultWidget;
                }

                @Override
                public void onSuccess() {
                    Widget mapViewComposite = loadedDisplays[tabSelected];
                    if (loadedDisplays[tabSelected] == null) {
                        mapViewComposite = new MapViewComposite((MapViewConfig) viewConfig);
                        loadedDisplays[tabSelected] = mapViewComposite;
                    }
                    tabContentHolder.setWidget(mapViewComposite);
                }
            });
        }else if (viewConfig instanceof TemplateViewConfig){
            GWT.runAsync(new RunAsyncCallback() {
                @Override
                public void onFailure(Throwable throwable) {
                    widget[0] = defaultWidget;
                }

                @Override
                public void onSuccess() {
                    Widget templateViewComposite = loadedDisplays[tabSelected];
                    if (loadedDisplays[tabSelected] == null) {
                        templateViewComposite = new TemplateViewComposite((TemplateViewConfig) viewConfig);
                        loadedDisplays[tabSelected] = templateViewComposite;
                    }
                    tabContentHolder.setWidget(templateViewComposite);
                }
            });
        } else if (viewConfig instanceof PanelViewConfig){
            GWT.runAsync(new RunAsyncCallback() {
                @Override
                public void onFailure(Throwable throwable) {
                    widget[0] = defaultWidget;
                }

                @Override
                public void onSuccess() {
                    Widget panelViewComposite = loadedDisplays[tabSelected];
                    if (loadedDisplays[tabSelected] == null) {
                        panelViewComposite = new PanelViewComposite((PanelViewConfig) viewConfig);
                        loadedDisplays[tabSelected] = panelViewComposite;
                    }
                    tabContentHolder.setWidget(panelViewComposite);
                }
            });
        }


        widget[0].setWidth("100%");
    }

    public void selectTab(final Integer tabSelection){
        vTabBar.selectTab(tabSelection);
    }

    @Override
    public void onLoadingStatusChange(LoadStatusListener.LoadStatusObserver.LoadingStatus loadingStatus) {
        if(loadingStatus== LoadStatusListener.LoadStatusObserver.LoadingStatus.LOADING){
            loadingPopup.setWidget(new HTML("Loading data... please wait"));
            final int leftPosition = (verticalPanel.getAbsoluteLeft() + verticalPanel.getOffsetWidth()) / 2;
            final int absoluteTop = verticalPanel.getAbsoluteTop();
            loadingPopup.setPopupPosition(leftPosition, absoluteTop);
            loadingPopup.show();
        }
        if(loadingStatus== LoadStatusListener.LoadStatusObserver.LoadingStatus.LOADED){
            loadingPopup.hide();
        }
    }

    interface ResultsViewUIUiBinder extends UiBinder<VerticalPanel, ResultViewUI> {}

    VerticalPanel verticalPanel = new VerticalPanel();

    private static ResultsViewUIUiBinder ourUiBinder = GWT.create(ResultsViewUIUiBinder.class);

    public ResultViewUI() {
        verticalPanel = ourUiBinder.createAndBindUi(this);
        initWidget(verticalPanel);
    }

    public DecoratedTabBar getvTabBar() {
        return vTabBar;
    }
}
