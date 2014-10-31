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

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.DataView;
import com.google.gwt.visualization.client.visualizations.Table;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import org.wwarn.surveyor.client.core.*;
import org.wwarn.surveyor.client.event.InterfaceLoadCompleteEvent;
import org.wwarn.surveyor.client.model.TableViewConfig;
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.surveyor.client.mvp.ClientFactory;
import org.wwarn.surveyor.client.event.ResultChangedEvent;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;

import java.util.*;

/**
 * A view to show results in a Tabular format
 * User: nigelthomas
 * Date: 11/09/2013
 * Time: 10:52
 */
public class TableViewComposite extends Composite {

    private final FlowPanel rootElement;
    private final TableViewConfig tableConfig;
    private RecordList recordList;
    private ClientFactory clientFactory = SimpleClientFactory.getInstance();
    private final DataSchema schema;
    private TableRecordsFilter tableRecordsFilter;
    private HashMap<String, Integer> columnNameToIndexMap = new HashMap<String, Integer>();

    interface TableViewUIUiBinder extends UiBinder<FlowPanel, TableViewComposite> {
    }
    private static TableViewUIUiBinder ourUiBinder = GWT.create(TableViewUIUiBinder.class);

    interface ResultChangedEventBinder extends EventBinder<TableViewComposite> {};
    private ResultChangedEventBinder eventBinder = GWT.create(ResultChangedEventBinder.class);

    public TableViewComposite(TableViewConfig tableViewConfig) {
        Objects.requireNonNull(tableViewConfig);
        this.tableConfig =  tableViewConfig;
        rootElement = ourUiBinder.createAndBindUi(this);
        rootElement.add(new HTMLPanel("Loading data..."));
        initWidget(rootElement);
        eventBinder.bindEventHandlers(this, clientFactory.getEventBus());
        schema = clientFactory.getSchema();
        setupTableAndFetchData();
    }

    private void setupTableAndFetchData() {
        try {
            final FilterQuery filterQuery;
            if(clientFactory.getLastFilterQuery()!=null){
                filterQuery = clientFactory.getLastFilterQuery();
                filterQuery.setFields(Collections.EMPTY_SET);
            }else {
                filterQuery = new FilterQuery();
            }

            clientFactory.getDataProvider().query(filterQuery, new AsyncCallback<QueryResult>() {
                @Override
                public void onFailure(Throwable throwable) {
                    throw new IllegalStateException(throwable);
                }

                @Override
                public void onSuccess(QueryResult queryResult) {
                    setupDisplay(queryResult);
                }
            });
        } catch (SearchException e) {
            throw new IllegalStateException(e);
        }
    }

    public interface TableRecordsFilter{
        public RecordList filter(RecordList recordList);

        public RecordList filter(RecordList recordList, String field);
    }


    private void setupDisplay(QueryResult queryResult) {
        rootElement.clear();

        final TableRecordsFilter tableRecordsFilter1 = getTableRecordsFilter();
        final RecordList list;
        list = queryResult.getRecordList();

        if(StringUtils.isEmpty(tableConfig.getFilterBy())){
            recordList = tableRecordsFilter1.filter(list);
        }else{
            recordList = tableRecordsFilter1.filter(list, tableConfig.getFilterBy());
        }

        Table.Options options = Table.Options.create();
        options.setAllowHtml(true);
        DataView dataView = createDataViewFrom(tableConfig, recordList);
        if(tableConfig.getSortColumn()!=null){
            options.setSort(Table.Options.Policy.ENABLE);
            options.setSortColumn(getColumnIndex(tableConfig.getSortColumn()));
            options.setSortAscending(!tableConfig.getSortOrder().equals("desc"));
        }

        if(tableConfig.getPageSize() != 0){
            options.setPageSize(tableConfig.getPageSize());
            options.setPage(Table.Options.Policy.ENABLE);
        }



        Table table = new Table(dataView, options);
        rootElement.add(table);
        clientFactory.getEventBus().fireEvent(new InterfaceLoadCompleteEvent());
    }


    private TableRecordsFilter getTableRecordsFilter() {
        if(tableRecordsFilter != null){return tableRecordsFilter;}
        try {
            tableRecordsFilter = GWT.create(TableRecordsFilter.class);
        } catch (RuntimeException e){
            if(!e.getMessage().startsWith("Deferred binding")) throw e;
            //by pass deferred binding error and use default value
            tableRecordsFilter = new DefaultTableRecordsFilter();
        }
        return tableRecordsFilter;
    }

    private DataView createDataViewFrom(TableViewConfig tableViewConfig, RecordList list) {
        DataTable table = DataTable.create();
        setupColumnHeaders(tableViewConfig, list, table);
        setupRows(tableViewConfig, list, table);
        return DataView.create(table);
    }

    private int getColumnIndex(String columnName){
        final Integer o = columnNameToIndexMap.get(columnName);
        if(o !=null ){
            return o;
        }
        int columnIndex = 0;
        for (TableViewConfig.TableColumn tableColumn : tableConfig.getColumns()) {
            if(tableColumn.getFieldName().equals(columnName)){
                columnNameToIndexMap.put(columnName, columnIndex);
                return columnIndex;
            }
            columnIndex++;
        }
        throw new IndexOutOfBoundsException("Column not found");
    }

    private void setupColumnHeaders(TableViewConfig tableViewConfig, RecordList list, DataTable table) {
        // read columns from view config, and map to a column type from abstractDatatable
        List<TableViewConfig.TableColumn> columns = tableViewConfig.getColumns();
        for (TableViewConfig.TableColumn column : columns) {
            final DataType cellType = schema.getType(column.getFieldName());
            AbstractDataTable.ColumnType columnType = AbstractDataTable.ColumnType.STRING;

            if (cellType == DataType.Integer){
                columnType = AbstractDataTable.ColumnType.NUMBER;
            }

            if(isFunction(column.getFieldName())|| cellType == DataType.Date|| cellType == DataType.DateYear){
                columnType = AbstractDataTable.ColumnType.STRING;
            }
            if(isArithFunction(column.getFieldName())){
                columnType = AbstractDataTable.ColumnType.NUMBER;
            }


            table.addColumn(columnType, column.getFieldTitle(), column.getFieldName());
        }
    }

    private void setupRows(TableViewConfig tableViewConfig, RecordList list, DataTable table) {
        final List<RecordList.Record> records = list.getRecords();
        final int numberOfRecords = records.size();
        table.addRows(numberOfRecords);
        for (int rowIndex = 0; rowIndex < numberOfRecords; rowIndex++) {
            int tableColumnIndex = 0;
            final RecordList.Record currentRecord = records.get(rowIndex);
            for (TableViewConfig.TableColumn tableColumn : tableViewConfig.getColumns()) {
                final String columnFieldNameForCurrentRecord = tableColumn.getFieldName();
                if(isFunction(columnFieldNameForCurrentRecord)){
                    TableFunctions tableFunctions = new TableFunctions(columnFieldNameForCurrentRecord, list, table);
                    tableFunctions.resolve(rowIndex, tableColumnIndex);
                }else{
                    // get column type
                    DataType columnType = schema.getType(columnFieldNameForCurrentRecord);
                    final String cellValue = currentRecord.getValueByFieldName(columnFieldNameForCurrentRecord);
                    switch (columnType){
                        case Date:
                        case DateYear:
                            final DateTimeFormat formatter = DateTimeFormat.getFormat(tableColumn.getDateFormat());
                            final DateTimeFormat isoFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.ISO_8601);
                            final Date date = isoFormat.parse(cellValue);
                            final String dateWithSpan = addSpanAttribute(isoFormat.format(date), formatter.format(date));
                            table.setValue(rowIndex, tableColumnIndex, dateWithSpan);
                            break;
                        case Boolean:
                            boolean valueBoolean = cellValue.toLowerCase().equals("true");
                            table.setValue(rowIndex, tableColumnIndex, valueBoolean);
                            break;
                        case Integer:
                            if(cellValue == null)break;
                            double valueDouble = Double.parseDouble(cellValue);
                            table.setValue(rowIndex, tableColumnIndex, valueDouble);
                            break;
                        case String:
                            String valueStringRaw = StringUtils.ifEmpty(cellValue,"");
                            String valueStringFormatted = valueStringRaw;
                            if(!StringUtils.isEmpty(tableColumn.getHyperLinkField())){
                                int abstractTableColumnIndexForHyperLinkField = schema.getColumnIndex(tableColumn.getHyperLinkField());
                                if(abstractTableColumnIndexForHyperLinkField >= 0){
                                    String hyperLinkValue = currentRecord.getValueByFieldName(tableColumn.getHyperLinkField());
                                    if(!StringUtils.isEmpty(hyperLinkValue)){
                                        //if the value is empty, set it to the hyperlink
                                        valueStringRaw=StringUtils.isEmpty(valueStringRaw)?hyperLinkValue:valueStringRaw;
                                        valueStringFormatted = addHyperLink(valueStringRaw, hyperLinkValue);
                                    }
                                }
                            }
                            valueStringFormatted = addSpanAttribute(valueStringRaw, valueStringFormatted);
                            table.setValue(rowIndex, tableColumnIndex, valueStringFormatted);
                            break;
                        default:
                            table.setFormattedValue(rowIndex, tableColumnIndex, cellValue);
                            break;
                    }
                }
                tableColumnIndex++;
            }
        }

    }
    private String addHyperLink(String valueString, String hyperLinkValue) {
        return "<a target=\"_blank\" href=\""+SafeHtmlUtils.htmlEscape(hyperLinkValue)+"\">"+SafeHtmlUtils.htmlEscapeAllowEntities(valueString)+"</a>";
    }

    private String addSpanAttribute(String valueStringRaw, String valueString) {
        return "<span class=\"tableContentHrefOrderHack\" title=\""+SafeHtmlUtils.htmlEscape(valueStringRaw)+"\">"+valueString+"</span>";
    }

    @EventHandler
    public void onResultChanged(ResultChangedEvent resultChangedEvent){
        setupDisplay(resultChangedEvent.getQueryResult());
    }

    private boolean isFunction(String fieldName){
        return fieldName.startsWith("func");
    }

    private boolean isArithFunction(String fieldName){
        return fieldName.startsWith("func(ARITH");
    }

    private static class DefaultTableRecordsFilter implements TableRecordsFilter {
        SimpleClientFactory simpleClientFactory = SimpleClientFactory.getInstance();
        DataSchema schema = simpleClientFactory.getSchema();
        @Override
        public RecordList filter(RecordList recordList) {
            return recordList;
        }

        @Override
        public RecordList filter(RecordList recordList, String field) {
            return recordList;
        }
    }
}
