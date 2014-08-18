package org.wwarn.surveyor.client.mvp.view.template;

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
import org.wwarn.mapcore.client.components.customwidgets.*;
import org.wwarn.surveyor.client.core.RecordList;
import org.wwarn.surveyor.client.model.TemplateViewNodesConfig;
import org.wwarn.surveyor.client.mvp.view.plot.GWTVizPlot;
import org.wwarn.surveyor.client.mvp.view.plot.PlotOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Given some configuration data:
 *  a view is generated
 *  <pre>
 *  {@code
 *  <splitLayout>
 *  <left>
 *  <qgplot data="getRelatedRecordsByField(currentRecord.Lat)">
 *  </left>
 *  <right>
 *  The bar chart shows the the decline in drug effectiveness as time increases
 *  </right>
 *  </splitLayout>
 *  }
 *  </pre>
 */
public class TemplateBasedViewBuilder {
    private GWTVizPlot gwtVizPlot;

    public TemplateBasedViewBuilder(GWTVizPlot gwtVizPlot) {
        if(gwtVizPlot == null){
            throw new IllegalArgumentException("gwtVizPlot must be set");
        }
        this.gwtVizPlot = gwtVizPlot;
    }

    // call this with config to initialize the view
    public Panel draw(TemplateViewNodesConfig config, RecordList recordList){
        // setup basic layout
        // has a reference to builders from plot to do rest of the plots
        final TemplateViewNodesConfig.TemplateNode rootTemplateNode = config.getRootTemplateNode();
        if(rootTemplateNode == null){
            throw new IllegalArgumentException("RootTemplate node is empty");
        }
        Panel panel = draw(rootTemplateNode, recordList);
        return panel;
    }

    /**
     * Need fix width and height to ensure info widget panel is drawn correctly
     * @return
     */
    private VerticalPanel fixedWidthAndHeightPanel() {
        final VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setHeight("300px");
        verticalPanel.setWidth("500px");
        return verticalPanel;
    }

    private Panel draw(TemplateViewNodesConfig.TemplateNode node, RecordList recordList) {
        // draw basic layout and delegate
        if(node.isLeafNode() && !(node instanceof TemplateViewNodesConfig.LayoutNode)){
            throw new IllegalArgumentException("Expected a layout node");
        }

        TemplateViewNodesConfig.TemplateNode currentNode =  node;
        final FlowPanel flowPanel = new FlowPanel();
        Panel currentWidget = flowPanel;
        Panel parentWidget = null;
        List<TemplateViewNodesConfig.TemplateNode> nodeList = new ArrayList<>();
        nodeList.add(null); // add empty node
        // todo refactor this an iterator
        // pre-order iterative node walker
        while(currentNode!=null){
            //process current node
            switch (currentNode.getName()){
                case "simpleLayout":
                    final FlowPanel simplePanel = new FlowPanel();
                    currentWidget.add(simplePanel);
                    parentWidget = currentWidget;
                    currentWidget = simplePanel;
                    break;
                case "splitLayout":
                    final HorizontalSplitEqualWidthPanel layoutPanel = setupDisplayOfFixedWidthLayoutPanel();
                    currentWidget.add(layoutPanel);
                    parentWidget = currentWidget;
                    currentWidget = layoutPanel;
                    break;
                case "left":
                    if(parentWidget instanceof org.wwarn.mapcore.client.components.customwidgets.LayoutPanel){
                        currentWidget = parentWidget;
                    }
                    if(currentWidget instanceof HorizontalSplitEqualWidthPanel){
                        final FlowPanel leftWidget = new FlowPanel();
                        ((HorizontalSplitEqualWidthPanel)currentWidget).addLeftWidget(leftWidget); //Left simplePanel
                        parentWidget = currentWidget;
                        currentWidget = leftWidget;
                    }
                    break;
                case "right":
                    if(parentWidget instanceof org.wwarn.mapcore.client.components.customwidgets.LayoutPanel){
                        currentWidget = parentWidget;
                    }
                    if(currentWidget instanceof HorizontalSplitEqualWidthPanel){
                        final FlowPanel rightWidget = new FlowPanel();
                        ((HorizontalSplitEqualWidthPanel)currentWidget).addRightWidget(rightWidget); //Right simplePanel
                        parentWidget = currentWidget;
                        currentWidget = rightWidget;
                    }
                    break;
                case "htmlNode":
                    final TemplateViewNodesConfig.HtmlNode htmlNode = (TemplateViewNodesConfig.HtmlNode)currentNode;
                    currentWidget.add(new HTMLPanel(htmlNode.getHtml()));
                    break;
                case "plot":
                    final PlotOptions.Builder options = getBuilderFromConfig((TemplateViewNodesConfig.PlottingNode) currentNode, recordList);
                    currentWidget.add(gwtVizPlot.qplot(options.createPlotOptions()));
                    break;
                case "label":
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported element found :\"" + currentNode.getName() + "\"");
            }
            //get children
            if(currentNode instanceof TemplateViewNodesConfig.LayoutNode) {
                TemplateViewNodesConfig.LayoutNode layoutNode = (TemplateViewNodesConfig.LayoutNode) currentNode;
                for (int i = 0; i < layoutNode.getNumberOfChildren(); i++) {
                    nodeList.add(currentNode.getChild(i));
                }
            }
            // assign each child to current node
            final int lastNodeIndex = nodeList.size() - 1;
            final TemplateViewNodesConfig.TemplateNode templateNode = nodeList.get(lastNodeIndex);
            nodeList.remove(lastNodeIndex);
            currentNode = templateNode;
        }

        return flowPanel;
    }

    private PlotOptions.Builder getBuilderFromConfig(TemplateViewNodesConfig.PlottingNode node, RecordList recordList) {
        final PlotOptions.Builder options = new PlotOptions.Builder();
        options.setGeom(PlotOptions.Geometry.valueOf(node.getGeom().toUpperCase()));
        options.setData(recordList);
        options.setMain(node.getMainTitle());
        options.setSub(node.getSubTitle());
        options.setX(node.getX());
        options.setY(node.getY());
        options.setXlab(node.getxLabel());
        options.setYlab(node.getyLabel());
        return options;
    }

    private HorizontalSplitEqualWidthPanel setupDisplayOfFixedWidthLayoutPanel() {
        final HorizontalSplitEqualWidthPanel layoutPanel = new HorizontalSplitEqualWidthPanel();
        return layoutPanel;
    }

    // the module instance; instantiate it behind a runAsync
    private static TemplateBasedViewBuilder instance = null;

    // A callback for using the module instance once it's loaded
    public interface TemplateBasedViewBuilderClient {
        void onSuccess(TemplateBasedViewBuilder instance);
        void onUnavailable();
    }

    /**
     *  Access the module's instance.  The callback
     *  runs asynchronously, once the necessary
     *  code has downloaded.
     */
    public static void createAsync(final TemplateBasedViewBuilderClient client) {
        GWT.runAsync(new RunAsyncCallback() {
            public void onFailure(Throwable err) {
                client.onUnavailable();
                throw new IllegalStateException(err);
            }

            public void onSuccess() {
                GWTVizPlot.createAsync(new GWTVizPlot.GWTVizPlotClient() {
                    @Override
                    public void onSuccess(GWTVizPlot gwtVizPlot) {
                        if (instance == null) {
                            instance = new TemplateBasedViewBuilder(gwtVizPlot);
                        }
                        client.onSuccess(instance);
                    }

                    @Override
                    public void onUnavailable() {
                        throw new IllegalStateException("unable to load plotter");
                    }
                });


            }
        });
    }
}
