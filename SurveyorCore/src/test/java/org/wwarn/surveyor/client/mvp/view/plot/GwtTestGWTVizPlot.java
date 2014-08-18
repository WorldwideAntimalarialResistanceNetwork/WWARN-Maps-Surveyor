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

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.visualization.client.visualizations.Table;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;
import org.junit.Test;
import org.wwarn.surveyor.client.core.*;

public class GwtTestGWTVizPlot extends VisualizationTest {
    private final DataProviderTestUtility.DataProviderSource dataProviderSource;
    GWTVizPlot gwtVizPlot;
    public static final int FINISH_TEST_DELAY_TIMEOUT_MILLIS = 30000;
    public String getModuleName() {
        return "org.wwarn.surveyor.surveyorJUnit";
    }
    public DataProviderTestUtility dataProviderTestUtility;
    protected JSONArray jsonArray;
    protected DataSchema schema;
    // todo remove this repeated code
    private void runTestWithDefaultDataSetup(final Runnable runnable){
        final boolean callFinishTest = false; // important as if this is called other calls to finishTest fails
        delayTestFinish(FINISH_TEST_DELAY_TIMEOUT_MILLIS);
        loadApi(new Runnable() {
            public DataProvider dataProvider;

            @Override
            public void run() {
                dataProvider = dataProviderSource.getDataProvider();
                dataProvider.onLoad(new Runnable() {
                    @Override
                    public void run() {
                        runnable.run();
                    }
                });
            }
        }, callFinishTest);
    }

    public GwtTestGWTVizPlot() {
        dataProviderTestUtility = new DataProviderTestUtility();
        this.dataProviderSource = new DataProviderTestUtility.DataProviderSource() {
            @Override
            public DataProvider getDataProvider() {
                final GenericDataSource dataSource = new GenericDataSource(null, Constants.JSON_DATA_SOURCE, GenericDataSource.DataSourceType.JSONPropertyList);
                DefaultLocalJSONDataProvider providerSource = new DefaultLocalJSONDataProvider(dataSource, testUtility.fetchSampleDataSchema(), dataProviderTestUtility.getSelectorList());
                return providerSource;
            }
        };
    }

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        gwtVizPlot = new GWTVizPlot();
        runTestWithDefaultDataSetup(new Runnable() {
            @Override
            public void run() {
                GWTVizPlot.createAsync(new GWTVizPlot.GWTVizPlotClient() {
                    @Override
                    public void onSuccess(GWTVizPlot instance) {
                        gwtVizPlot = instance;
                        System.out.println("assigned instance");
                        finishTest();
                    }

                    @Override
                    public void onUnavailable() {
                        finishTest();
                        fail("unable to load class");
                    }
                });
            }
        });
    }

    @Override
    protected String[] getVisualizationPackage() {
        return new String[]{Table.PACKAGE, CoreChart.PACKAGE};    //Must Override
    }

    @Test
    public void testQplot() throws Exception {
        assertNotNull("gwtVizPlot cannot be null", gwtVizPlot);

        runTestWithDefaultDataSetup(new Runnable() {
            @Override
            public void run() {
                DataSchema dataschema = dataProviderTestUtility.fetchSampleDataSchema();
                final RecordListBuilder recordListBuilder = new RecordListBuilder(RecordListBuilder.CompressionMode.CANONICAL, dataschema);
                final RecordList data = recordListBuilder.createRecordList();
                gwtVizPlot.qplot(new PlotOptions.Builder().setX("CLAT").setY("CLON").setData(data).setGeom(PlotOptions.Geometry.BAR).setXlab("x label").setYlab("y label").setMain("Main title").setSub("Sub Title").createPlotOptions());
            }
        });
    }
}