package org.wwarn.surveyor.client.mvp.view.plot;

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
import com.google.gwt.user.client.ui.*;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.corechart.AxisOptions;
import com.google.gwt.visualization.client.visualizations.corechart.BarChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;
import org.wwarn.surveyor.client.core.DataSchema;
import org.wwarn.surveyor.client.core.DataType;
import org.wwarn.surveyor.client.core.RecordList;
import org.wwarn.surveyor.client.mvp.ClientFactory;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GWTVizPlot
 */
public class GWTVizPlot  implements QPlot {
    private final DataSchema schema;
    protected ClientFactory clientFactory = SimpleClientFactory.getInstance();

    public interface GeometryHandler {
        public PlotOptions.Geometry getGeometry();
        public Widget draw(PlotOptions plotOptions);
    }

    private Map<PlotOptions.Geometry, GeometryHandler> geometryHandlerMap = new HashMap<PlotOptions.Geometry, GeometryHandler>();

    protected GWTVizPlot() {
        this.schema = clientFactory.getSchema();
        addDrawHandler(new BarChartHandler());
        addDrawHandler(new PieChartHandler());
    }

    @Override
    public Widget qplot(PlotOptions plotOptions) {
        // do some processing
        if(plotOptions.getGeom() == null){
            throw new IllegalArgumentException("geom cannot be empty");
        }
        // dispatch to appropriate class
        return geometryHandlerMap.get(plotOptions.getGeom()).draw(plotOptions);
    }

    public void addDrawHandler(GeometryHandler handler) {
        geometryHandlerMap.put(handler.getGeometry(), handler);
    }

    // the module instance; instantiate it behind a runAsync
    private static GWTVizPlot instance = null;

    // A callback for using the module instance once it's loaded
    public interface GWTVizPlotClient {
        void onSuccess(GWTVizPlot instance);
        void onUnavailable();
    }

    /**
     *  Access the module's instance.  The callback
     *  runs asynchronously, once the necessary
     *  code has downloaded.
     */
    public static void createAsync(final GWTVizPlotClient client) {
        GWT.runAsync(new RunAsyncCallback() {
            public void onFailure(Throwable err) {
                client.onUnavailable();
            }

            public void onSuccess() {
                if (instance == null) {
                    instance = new GWTVizPlot();
                }
                client.onSuccess(instance);
            }
        });
    }

    private AbstractDataTable createWithDummyData() {
        final DataTable table = DataTable.create();
        int recordCount = (int) (10 * Math.random())+5;
        table.addColumn(AbstractDataTable.ColumnType.STRING, "orgname");
        table.addColumn(AbstractDataTable.ColumnType.NUMBER, "orgcount");
        table.addRows(recordCount);
        for (int i = 0; i < recordCount; i++) {
            table.setValue(i,0, "orgName"+i);
            table.setValue(i,1, i* 10);
        }
        return table;
    }
    private AbstractDataTable createDataTable(PlotOptions plotOptions) {
        final DataTable table = DataTable.create();
        final String yFieldName = plotOptions.getY();
        table.addColumn(getTypeFrom(yFieldName), yFieldName);
        final String xFieldName = plotOptions.getX();
        table.addColumn(getTypeFrom(xFieldName), xFieldName);
        final RecordList data = plotOptions.getData();
        final List<RecordList.Record> records = data.getRecords();
        table.addRows(records.size());
        for (int i = 0; i < records.size(); i++) {
            final RecordList.Record record = records.get(i);
            final String xValue = parseFromType(record.getValueByFieldName(xFieldName), schema.getType(xFieldName));
            final String yValue = parseFromType(record.getValueByFieldName(yFieldName), schema.getType(yFieldName));
            table.setValue(i,0, yValue); // y
            table.setValue(i,1, xValue); // x
        }
        return table;
    }

    private String parseFromType(String valueByFieldName, DataType type) {
        switch (type) {
            case String:
                break;
            case CoordinateLat:
                break;
            case CoordinateLon:
                break;
            case Integer:
                break;
            case Boolean:
                break;
            case Date:
            case DateYear:
                final Date date = DataType.ParseUtil.tryParseDate(valueByFieldName, "1970");
                valueByFieldName = DataType.ParseUtil.getDateFormatFrom("yyyy").format(date);
                break;
        }
        return valueByFieldName;
    }

    private AbstractDataTable.ColumnType getTypeFrom(String fieldName) {
        final DataType type = schema.getType(fieldName);
        switch (type) {
            case Date:
            case DateYear:
                return AbstractDataTable.ColumnType.NUMBER;
            case Integer:
            case CoordinateLat:
            case CoordinateLon:
                return AbstractDataTable.ColumnType.NUMBER;
            default:
                return AbstractDataTable.ColumnType.STRING;
        }
    }

    private class BarChartHandler implements GeometryHandler {
        @Override
        public PlotOptions.Geometry getGeometry() {
            return PlotOptions.Geometry.BAR;
        }

        @Override
        public Widget draw(PlotOptions plotOptions) {
            VerticalPanel result = new VerticalPanel();
            Options options = BarChart.createOptions();
            options.setHeight(240);
            options.setTitle(plotOptions.getSub());
            options.setWidth(400);

            AxisOptions axisOptions = AxisOptions.create();
            axisOptions.setTitle(plotOptions.getYlab());
            options.setVAxisOptions(axisOptions);
            axisOptions = AxisOptions.create();
            axisOptions.setTitle(plotOptions.getXlab());
            options.setHAxisOptions(axisOptions);

            AbstractDataTable data = createDataTable(plotOptions);
//            AbstractDataTable data = createWithDummyData();

            BarChart widget = new BarChart(data, options);
            Label status = new Label(plotOptions.getMain());
            result.add(status);
            result.add(widget);
            return result;
        }
    }

    private class PieChartHandler implements GeometryHandler {

        @Override
        public PlotOptions.Geometry getGeometry() {
            return PlotOptions.Geometry.PIE;
        }

        @Override
        public Widget draw(PlotOptions plotOptions) {
            VerticalPanel result = new VerticalPanel();
            Options options = PieChart.createOptions();
            options.setHeight(240);
            options.setTitle(plotOptions.getSub());
            options.setWidth(400);

            final AxisOptions axisOptions = AxisOptions.create();
            axisOptions.setTitle(plotOptions.getYlab());
            options.setVAxisOptions(axisOptions);
            axisOptions.setTitle(plotOptions.getXlab());
            options.setHAxisOptions(axisOptions);

            AbstractDataTable data = createDataTable(plotOptions);
//            AbstractDataTable data = createWithDummyData();


            PieChart widget = new PieChart(data, options);
            Label status = new Label(plotOptions.getMain());
            result.add(status);
            result.add(widget);
            return result;
        }
    }
}
