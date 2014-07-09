package com.example.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.Table;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;
import org.wwarn.mapcore.client.utils.EventLogger;
import org.wwarn.mapcore.client.utils.MapLoadUtil;
import org.wwarn.surveyor.client.mvp.SurveyorAppController;
import org.wwarn.surveyor.client.mvp.view.MainPanelView;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ProjectEntryPoint implements EntryPoint {

  /**
   * Sets up RPC
   */

    public void onModuleLoad() {
        //load map v3 api
        MapLoadUtil.loadMapApi(new Runnable() {
            public void run() {
                loadVisualisationApi();
            }
        });
    }

    private void loadVisualisationApi() {
        // load visualisation api ... before loading any panels..
        VisualizationUtils.loadVisualizationApi(new Runnable() {
            public void run() {
                EventLogger.logEvent("NMFISurveyor", "onModuleLoad", "begin");
                // setup controller with reference to main panel
                new SurveyorAppController(createLayout());

//              RootPanel.get().add(new PieChartDiseaseDistribution(null));
                EventLogger.logEvent("NMFISurveyor", "onModuleLoad", "end");
            }
        }, Table.PACKAGE, PieChart.PACKAGE);
    }

    private MainPanelView createLayout() {
        MainPanelView panel = new MainContentPanel();
        return panel;
    }
}
