package org.wwarn.surveyor.client.model;

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

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.wwarn.mapcore.client.utils.StringUtils;

import java.util.ArrayList;

/**
 * Holds nodes for custom template view, used in Template Marker Info Window and Template Panel
 * Generic concepts like basic layout split panel vs others
 * User: nigel
 * Date: 15/08/13
 * Time: 15:29
 */
public class TemplateViewNodesConfig implements ViewConfig {
    String viewName = "";
    private String viewLabel;

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    private String dataSource;
    private TemplateNode rootTemplateNode;

    public void setRootTemplateNode(TemplateNode templateNode) {
        this.rootTemplateNode = templateNode;
    }

    public TemplateNode getRootTemplateNode() {
        return rootTemplateNode;
    }

    public static abstract class TemplateNode {
        protected String nodeName;
        private TemplateNode parentNode;
        protected ArrayList<TemplateNode> templateNodes = new ArrayList<>();

        public void add(TemplateNode templateNode) {
            throw new UnsupportedOperationException();
        }
        public void remove(TemplateNode templateNode) {
            throw new UnsupportedOperationException();
        }
        public TemplateNode getChild(int i) {
            throw new UnsupportedOperationException();
        }

        public String getName() {
            return nodeName;
        }

        public boolean isLeafNode(){
            throw new UnsupportedOperationException();
        }
        public TemplateNode getParent(){
            return this.parentNode;
        }
        public void setParent(LayoutNode parent) {
            this.parentNode = parent;
        }
        @Override
        public String toString() {
            return "TemplateNode{" +
                    "nodeName='" + nodeName + '\'' +
                    ", templateNodes=" + templateNodes +
                    '}';
        }


    }

    public static class LayoutNode extends TemplateNode {

        public void add(TemplateNode templateNode) {
            if(StringUtils.isEmpty(templateNode.getName())){
                return;
            }
            templateNode.setParent(this);
            templateNodes.add(templateNode);
        }
        public void remove(TemplateNode templateNode) {
            templateNodes.remove(templateNode);
        }

        public TemplateNode getChild(int i) {
            return templateNodes.get(i);
        }
        public int getNumberOfChildren() { return templateNodes.size(); }
        public String getName() {
            return nodeName;
        }
        public LayoutNode(String nodeName) {
            this.nodeName = nodeName;
        }

        @Override
        public boolean isLeafNode() {
            return false;
        }
    }

    public static class HtmlNode extends TemplateNode {
        public SafeHtml getHtml() {
            return html;
        }

        SafeHtml html;

        public HtmlNode(String html) {
            this.nodeName = "htmlNode";
            this.html = SafeHtmlUtils.fromString(html);
        }

        @Override
        public boolean isLeafNode() {
            return true;
        }
    }

    public static class PlottingNode extends TemplateNode {
        String geom="bar";
        String data="CLON, CLAT, PID";
        String x="STN";
        String y="OTN";
        String xLabel="x axis";
        String yLabel="y axis";
        String mainTitle="Bar chart example";
        String subTitle="Chart sub title";

        public String getGeom() {
            return geom;
        }

        public String getData() {
            return data;
        }

        public String getX() {
            return x;
        }

        public String getY() {
            return y;
        }

        public String getxLabel() {
            return xLabel;
        }

        public String getyLabel() {
            return yLabel;
        }

        public String getMainTitle() {
            return mainTitle;
        }

        public String getSubTitle() {
            return subTitle;
        }

        public PlottingNode(String geom, String data, String x, String y, String xLabel, String yLabel, String mainTitle, String subTitle) {
            this.geom = geom;
            this.data = data;
            this.x = x;
            this.y = y;
            this.xLabel = xLabel;
            this.yLabel = yLabel;
            this.mainTitle = mainTitle;
            this.subTitle = subTitle;
            this.nodeName = "plot";
        }

        @Override
        public boolean isLeafNode() {
            return true;
        }
    }


    @Override
    public String getViewName() {
        return viewName;
    }

    @Override
    public String getViewLabel() {
        return viewLabel;
    }

    @Override
    public String toString() {
        return "TemplateViewNodesConfig{" +
                "viewName='" + viewName + '\'' +
                ", viewLabel='" + viewLabel + '\'' +
                ", dataSource='" + dataSource + '\'' +
                ", rootTemplateNode=" + rootTemplateNode +
                '}';
    }
}
