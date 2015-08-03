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

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.surveyor.client.core.*;
import org.wwarn.surveyor.client.event.InterfaceLoadCompleteEvent;
import org.wwarn.surveyor.client.event.ResultChangedEvent;
import org.wwarn.surveyor.client.model.DataSourceProvider;
import org.wwarn.surveyor.client.model.DatasourceConfig;
import org.wwarn.surveyor.client.model.TableViewConfig;
import org.wwarn.surveyor.client.mvp.ClientFactory;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;
import org.wwarn.surveyor.client.util.AsyncCallbackWithTimeout;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by suay on 7/7/14.
 */
public class CellTableViewComposite extends Composite {

    public static final String DEFAULT_DATE = "2999";
    ClientFactory clientFactory = SimpleClientFactory.getInstance();
    private DatasourceConfig datasourceConfig;
    private DataSchema schema = clientFactory.getSchema();

    private Comparator<RecordList.Record> comparator = new Comparator<RecordList.Record>() {
        @Override
        public int compare(RecordList.Record o1, RecordList.Record o2) {
            if (o1 == o2) {
                return 0;
            }

            String sortColumn = tableViewConfig.getSortColumn();
            int diff = -1;
            if (o1 != null) {
                diff = (o2 != null) ? o1.getValueByFieldName(sortColumn).compareTo(o2.getValueByFieldName(sortColumn)) : 1;
            }
            return tableViewConfig.isDescendentOrder() ? -diff : diff;
        }
    };

    interface CellTableServerEventBinder extends EventBinder<CellTableViewComposite> {};

    TableViewConfig tableViewConfig;

    CellTable<RecordList.Record> cellTable;

    SimplePager pager;

    AbstractDataProvider dataProviderAsync;

    public CellTableViewComposite(TableViewConfig tableViewConfig) {
        CellTableServerEventBinder eventBinder = GWT.create(CellTableServerEventBinder.class);
        eventBinder.bindEventHandlers(this, clientFactory.getEventBus());
        this.tableViewConfig = tableViewConfig;
        this.datasourceConfig = clientFactory.getApplicationContext().getConfig(DatasourceConfig.class);

        initWidget(setupPanel());
        buildTable();
        createWithAsyncDataProvider();

    }

    @Override
    protected void onAttach() {
        super.onAttach();
        clientFactory.getEventBus().fireEvent(new InterfaceLoadCompleteEvent());
    }

    private FlowPanel setupPanel(){
        FlowPanel flowPanel = new FlowPanel();
        final String style = "table-responsive";
        flowPanel.addStyleName(style);
        final String style1 = "table-condensed";
        flowPanel.addStyleName(style1);
        VerticalPanel panel = new VerticalPanel();
        final String table = "table";
        panel.addStyleName(table);
        flowPanel.add(panel);
        if(datasourceConfig.getDataSourceProvider() == DataSourceProvider.ClientSideSearchDataProvider){
            final SortedCellTable<RecordList.Record> recordSortedCellTable = new SortedCellTable<>();
            cellTable = recordSortedCellTable;
        }else {
            cellTable = new CellTable<RecordList.Record>();
        }

        cellTable.setPageSize(tableViewConfig.getPageSize());
        pager = new SimplePager();
        pager.setDisplay(cellTable);
        panel.add(cellTable);
        panel.add(pager);
        return flowPanel;
    }


    private void createWithAsyncDataProvider() {
        if(datasourceConfig.getDataSourceProvider() == DataSourceProvider.ClientSideSearchDataProvider){
            // todo investigate if ListDataProvider can be used instead, inital use found that the filtering didn't work correctly..
//            setupClientSideDataProviderForCellTable();
            dataProviderAsync = new CliendSideAsyncDataProvider(tableViewConfig);

        }else {
            dataProviderAsync = new DataAsyncDataProvider(tableViewConfig);
        }
        if(dataProviderAsync != null) {
            dataProviderAsync.addDataDisplay(cellTable);
        }
    }

    private void setupClientSideDataProviderForCellTable() {
        final ClientSideSearchDataProvider dataProvider = (ClientSideSearchDataProvider) clientFactory.getDataProvider();
        try {
            dataProvider.query(new MatchAllQuery(), dataProvider.facetFieldList, new AsyncCallbackWithTimeout<QueryResult>() {
                @Override
                public void onTimeOutOrOtherFailure(Throwable caught) {
                    throw new IllegalStateException(caught);
                }

                @Override
                public void onNonTimedOutSuccess(QueryResult result) {
                    final List<RecordList.Record> records = result.getRecordList().getRecords();
                    Collections.sort(records, comparator); // ensure data is initially sorted
                    final ListDataProvider listDataProvider = new ListDataProvider(records);
                    dataProviderAsync = listDataProvider;
                    dataProviderAsync.addDataDisplay(cellTable);
                }
            });
        } catch (SearchException e) {
            throw new IllegalStateException(e);
        }
    }


    private void buildTable(){

        for (final TableViewConfig.TableColumn column : tableViewConfig.getColumns()){

            Column tableColumn;
            final SafeHtmlCell progressCell = new SafeHtmlCell();

            final boolean isHyperLinkField = !StringUtils.isEmpty(column.getHyperLinkField());
            tableColumn = new Column<RecordList.Record, SafeHtml>(progressCell) {

                @Override
                public SafeHtml getValue(final RecordList.Record record) {
                    SafeHtml safeHtml = new SafeHtml() {
                        @Override
                        public String asString() {
                            if (isHyperLinkField) {
                                String hyperLinkValue = record.getValueByFieldName(column.getHyperLinkField());
                                String valueString = record.getValueByFieldName(column.getFieldName());
                                return  TableViewComposite.addHyperLink(valueString,hyperLinkValue);
                            } else {
                                String value;
                                if(isFunction(column.getFieldName())){
                                    TableFunctionsCellTable tableFunctionsCellTable = new TableFunctionsCellTable(column.getFieldName(), record);
                                    value = tableFunctionsCellTable.resolve();
                                }else{
                                    // format date based on table specfied value
                                    DataType type = schema.getType(column.getFieldName());
                                    if(type == DataType.DateYear || type == DataType.Date){
                                        final String dateFormat = column.getDateFormat();
                                        final String dateValue = record.getValueByFieldName(column.getFieldName());
                                        final Date date = DataType.ParseUtil.tryParseDate(dateValue, "2999");
                                        final DateTimeFormat dateTimeFormat = DataType.ParseUtil.getDateFormatFrom(dateFormat);
                                        final String formattedDate = dateTimeFormat.format(date);
                                        if(!formattedDate.equals(DEFAULT_DATE)) {
                                            value = TableViewComposite.addSpanAttribute(dateValue, formattedDate);
                                        }else {
                                            value = record.getValueByFieldName(column.getFieldName());
                                        }
                                    }
                                    else {
                                        value = record.getValueByFieldName(column.getFieldName());
                                    }

                                }
                                return value;
                            }
                        }
                    };
                    return safeHtml;
                }
            };

            if(column.getFieldName().equals(tableViewConfig.getSortColumn()) && cellTable instanceof SortedCellTable){
                final SortedCellTable sortedCellTable = (SortedCellTable) this.cellTable;

                sortedCellTable.addColumn(tableColumn, column.getFieldTitle(), false);
                sortedCellTable.setDefaultSortOrder(tableColumn, !tableViewConfig.isDescendentOrder()); // sorts ascending on first click
                if(!StringUtils.isEmpty(tableViewConfig.getSortColumn())){
                    sortedCellTable.setComparator(tableColumn, comparator);
                };

            }else {
                cellTable.addColumn(tableColumn, column.getFieldTitle());
            }
        }


    }

    public static boolean isFunction(String fieldName){
        return fieldName.startsWith("func");
    }

    @EventHandler
    public void onResultChanged(ResultChangedEvent resultChangedEvent){
        pager.firstPage();
        if(dataProviderAsync instanceof ListDataProvider){
            ListDataProvider listDataProvider = (ListDataProvider) dataProviderAsync;
            final List<RecordList.Record> records = resultChangedEvent.getQueryResult().getRecordList().getRecords();
            listDataProvider.setList(records);
            listDataProvider.flush();
            listDataProvider.refresh();
        }

    }

}
