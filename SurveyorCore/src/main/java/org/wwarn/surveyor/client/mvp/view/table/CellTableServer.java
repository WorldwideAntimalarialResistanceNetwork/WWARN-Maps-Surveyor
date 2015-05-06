package org.wwarn.surveyor.client.mvp.view.table;

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

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.Range;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.surveyor.client.core.RecordList;
import org.wwarn.surveyor.client.event.ResultChangedEvent;
import org.wwarn.surveyor.client.model.TableViewConfig;
import org.wwarn.surveyor.client.mvp.ClientFactory;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;


import java.util.List;

/**
 * Created by suay on 7/7/14.
 */
public class CellTableServer extends Composite {

    ClientFactory clientFactory = SimpleClientFactory.getInstance();
    interface CellTableServerEventBinder extends EventBinder<CellTableServer> {};

    TableViewConfig tableViewConfig;

    CellTable<RecordList.Record> cellTable;

    SimplePager pager;

    DataAsyncDataProvider dataProviderAsync;

    public CellTableServer(TableViewConfig tableViewConfig) {
        CellTableServerEventBinder eventBinder = GWT.create(CellTableServerEventBinder.class);
        eventBinder.bindEventHandlers(this, clientFactory.getEventBus());
        this.tableViewConfig = tableViewConfig;

        initWidget(setupPanel());
        buildTable();
        createWithAsyncDataProvider();

    }

    private FlowPanel setupPanel(){
        FlowPanel flowPanel = new FlowPanel();
        flowPanel.addStyleName("table-responsive");
        flowPanel.addStyleName("table-condensed");
        VerticalPanel panel = new VerticalPanel();
        panel.addStyleName("table");
        flowPanel.add(panel);
        cellTable = new CellTable<RecordList.Record>();
        cellTable.setPageSize(tableViewConfig.getPageSize());
        pager = new SimplePager();
        pager.setDisplay(cellTable);
        panel.add(cellTable);
        panel.add(pager);
        return flowPanel;
    }


    private void createWithAsyncDataProvider() {
        dataProviderAsync = new DataAsyncDataProvider(tableViewConfig);
        dataProviderAsync.addDataDisplay(cellTable);
    }


    private void buildTable(){

        for (final TableViewConfig.TableColumn column : tableViewConfig.getColumns()){

            Column tableColumn;
            final SafeHtmlCell progressCell = new SafeHtmlCell();

            if(!StringUtils.isEmpty(column.getHyperLinkField())){
                tableColumn = new Column<RecordList.Record, SafeHtml>(progressCell) {

                    @Override
                    public SafeHtml getValue(final RecordList.Record record) {
                        SafeHtml safeHtml = new SafeHtml() {
                            @Override
                            public String asString() {
                                String hyperLinkValue = record.getValueByFieldName(column.getHyperLinkField());
                                String valueString = record.getValueByFieldName(column.getFieldName());
                                return  "<a href=\""+hyperLinkValue+"\" target=\"_blank\" >"+valueString+"</a>";
                            }
                        };
                        return safeHtml;
                    }
                };
            }else{
                tableColumn = new TextColumn<RecordList.Record>() {
                    @Override
                    public String getValue(RecordList.Record record) {
                        String value;
                        if(isFunction(column.getFieldName())){
                            TableFunctionsCellTable tableFunctionsCellTable = new TableFunctionsCellTable(column.getFieldName(), record);
                            value = tableFunctionsCellTable.resolve();

                        }else{
                            value = record.getValueByFieldName(column.getFieldName());
                        }
                        return value;
                    }
                };
            }
            cellTable.addColumn(tableColumn, column.getFieldTitle());
        }

    }

    public static boolean isFunction(String fieldName){
        return fieldName.startsWith("func");
    }

    @EventHandler
    public void onResultChanged(ResultChangedEvent resultChangedEvent){
        pager.firstPage();
    }

}
