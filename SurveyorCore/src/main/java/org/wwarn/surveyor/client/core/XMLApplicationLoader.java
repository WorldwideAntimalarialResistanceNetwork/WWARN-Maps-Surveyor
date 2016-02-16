package org.wwarn.surveyor.client.core;

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

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.xml.client.*;
import org.wwarn.mapcore.client.common.types.FilterConfigVisualization;
import org.wwarn.mapcore.client.components.customwidgets.facet.FacetType;
import org.wwarn.mapcore.client.components.customwidgets.map.MapBuilder;
import org.wwarn.surveyor.client.model.*;
import org.wwarn.mapcore.client.utils.StringUtils;
import org.wwarn.mapcore.client.utils.XMLUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One time parser, parses XML and loads into relevant config
 * Requires that if the application config is changed, app has to be reloaded
 * User: nigel
 * Date: 25/07/13
 * Time: 15:20
 */
public class XMLApplicationLoader implements ApplicationContext {

    Map<String, Config> configs = new HashMap<String, Config>();

    public XMLApplicationLoader(String xmlConfig) throws XMLUtils.ParseException {
        parseXML(xmlConfig);
    }

    private void parseXML(String xmlConfig) throws XMLUtils.ParseException {

        Document document = XMLParser.parse(xmlConfig);
        Element documentElement = document.getDocumentElement();

//        EventLogger.logEvent("org.wwarn.mapcore.client.core.XMLApplicationLoader", "xmlConfigParse", "begin");
        (new ConfigElementParser()).parse(documentElement);
//        EventLogger.logEvent("org.wwarn.mapcore.client.core.XMLApplicationLoader", "xmlConfigParse", "begin");
    }


    public <T extends Config> T getConfig(Class<T> cls) {
        Config config = configs.get(cls.getName());
        return (T) config;
    }

    /**
     * Every top level node is parsed and mapped onto relevant config
     * intended as a observer subject, which delegates to individual implementations
     */
    private class ConfigElementParser {
        public void parse(Element documentElement) throws XMLUtils.ParseException {
            // node items to parse and related parsers by index
            String[] configElements = {"datasource", "views", "filters"};
            XMLUtils.NodeElementParser[] configElementsParsers = {new DataSourceParser(), new ViewParser(), new FilterParser()};

            for (int i = 0; i < configElements.length; i++) {
                String configElement = configElements[i];
                XMLUtils.NodeElementParser nodeElementParser = configElementsParsers[i];
                NodeList nodeList = documentElement.getElementsByTagName(configElement);
                if(nodeList.getLength() != 1){
                    throw new IllegalArgumentException("Expected a single "+configElement+" node, but found :"+nodeList.getLength());
                }
                nodeElementParser.parse(nodeList.item(0));
            }
        }
    }

    /**
     * Datasource parser
     */
    protected class DataSourceParser extends XMLUtils.NodeElementParser {
        public void parse(Node item) throws XMLUtils.ParseException {
            if (item == null) {
                throw new XMLUtils.ParseException("Datasource node empty");
            }
            NodeList childNodes = item.getChildNodes();

            String nodeValue = item.getNodeName();
            if(!nodeValue.equals("datasource")){ // schema should validate this not manually in code.
                throw new XMLUtils.ParseException("Expected datasource node");
            }

            String fileLocation = null;
            DatasourceConfig datasourceConfig = null;

            final String type = getAttributeByName(item, "type");
            final String uniqueId = getAttributeByName(item, "id");
            String dataSourceType = StringUtils.ifEmpty(DataSourceProvider.valueOf(type).name(), DataSourceProvider.LocalClientSideDataProvider.name());
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node currentNode = childNodes.item(i);
                if (currentNode.getNodeName().equals("property")) {
                    String name = getAttributeByName(currentNode, "name");
                    if(name==null){
                        throw new XMLUtils.ParseException("missing attribute 'name' on property element");
                    }
                    if(name.equals("fileLocation")) {
                        fileLocation = getAttributeByName(currentNode, "value");
                    }

                    if(fileLocation == null){
                        throw new XMLUtils.ParseException("fileLocation is null");
                    }
                    String generatedName = "fl"+SafeHtmlUtils.htmlEscape(fileLocation.replaceAll("/", ""));
                    String parsedUniqueId = StringUtils.ifEmpty(uniqueId, generatedName);
                    datasourceConfig = new DatasourceConfig(parsedUniqueId, fileLocation, dataSourceType);
                }

                if(currentNode.getNodeName().equals("schema")){
                    NodeList fieldNodes = currentNode.getChildNodes();

                    if(fieldNodes.getLength() < 3){
                        throw new XMLUtils.ParseException("Expected at least three field nodes");
                    }
                    for (int j = 0; j < fieldNodes.getLength(); j++) {
                        if(fieldNodes.item(j).getNodeName().equals("field")){
                            Node fieldNode = fieldNodes.item(j);
                            String fieldNodeType = getAttributeByName(fieldNode, "type");
                            String fieldNodeName = getAttributeByName(fieldNode, "name");
                            datasourceConfig.getConfig().add(fieldNodeName, fieldNodeType);
                        }
                    }
                }
            }
            configs.put(DatasourceConfig.class.getName(), datasourceConfig);
        }
    }

    /**
     * View parser
     */
    protected class ViewParser extends XMLUtils.NodeElementParser {
        public void parse(Node item) throws XMLUtils.ParseException {
            String nodeValue = item.getNodeName();
            if(!nodeValue.equals("views")){ // schema should validate this not manually in code.
                throw new XMLUtils.ParseException("Expected datasource node");
            }
            //parse views
            NodeList childNodes = item.getChildNodes();

            ResultsViewConfig viewConfigs = new ResultsViewConfig();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node viewNode = childNodes.item(i);
                //map
                if(viewNode.getNodeName().equals("map")){
                    viewConfigs.add(parseMapNode(viewNode));
                }
                //table
                else if(viewNode.getNodeName().equals("table")){
                    viewConfigs.add(parseTableNode(viewNode));
                }
                //Panel
                else if(viewNode.getNodeName().equals("panel")){
                    viewConfigs.add(parsePanelNode(viewNode));
                }
                // View template
                else if(viewNode.getNodeName().equals("viewTemplate")){
                    viewConfigs.add(parseViewTemplateNode(viewNode));
                }
            }

            configs.put(ResultsViewConfig.class.getName(), viewConfigs);
        }

        private ViewConfig parseViewTemplateNode(Node viewNode) throws XMLUtils.ParseException {
            if(viewNode == null){ throw new IllegalArgumentException("Expected non null view node");}
            final NodeList childNodes = viewNode.getChildNodes();
            boolean hasVisitedLayoutNode = false;
            Node layoutNode = null;
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                //layout node
                if(!node.getNodeName().toLowerCase().contains("layout") && node.getNodeType()== Node.ELEMENT_NODE){
                    if(hasVisitedLayoutNode) break;
                    //only singe layout node expected
                    layoutNode = node;
                    hasVisitedLayoutNode = true;
                }

            }
            if(layoutNode==null)throw new IllegalArgumentException("Expected a layout node, not found");
            TemplateViewNodesConfig templateViewNodesConfig = new TemplateViewNodesConfig();
            try {
                parseTemplateViewNodes(viewNode, templateViewNodesConfig);
            } catch (XMLUtils.ParseException e) {
                throw new IllegalStateException(e);
            }
            String name = getAttributeByName(viewNode, "name");

            final String label = getNodeCDATAValue(getNodeByName(viewNode, "label"));
            return new TemplateViewConfig(templateViewNodesConfig, name, label);
        }

        /**
         <table name="Report table">
             <columns>
                 <column fieldName="FA" fieldTitle="1st Author"/>
                 <column fieldName="PY" fieldTitle="Year"/>
                 <column fieldName="TTL" fieldTitle="Title" hyperlink-field="URL"/>
                 <column fieldName="PUB" fieldTitle="Publication"/>
                 <column fieldName="CN" fieldTitle="Countries"/>
             </columns>
         </table>
         * @param tableNode
         * @return
         */
        private ViewConfig parseTableNode(Node tableNode) throws XMLUtils.ParseException {
            String name = getAttributeByName(tableNode, "name");
            Node labelNode = getNodeByName(tableNode, "label");
            final String nodeCDATAValue = getNodeCDATAValue(labelNode);
            TableViewConfig tableViewConfig = new TableViewConfig(name, nodeCDATAValue);
            Node columnsNode = getNodeByName(tableNode, "columns");

            if(columnsNode == null || !columnsNode.getNodeName().equals("columns")){
                throw new XMLUtils.ParseException("Unable to parse table node, expected table>columns");
            }
            final String sortOnColumn = getAttributeByName(columnsNode, "sortOnColumn");
            final String sortOrder = StringUtils.ifEmpty(getAttributeByName(columnsNode, "sortOrder"), "desc");
            final String pageSize = StringUtils.ifEmpty(getAttributeByName(tableNode, "pageSize"), "0");
            final String filterBy = StringUtils.ifEmpty(getAttributeByName(tableNode, "filterBy"), "");
            final String type = StringUtils.ifEmpty(getAttributeByName(tableNode, "type"), "CLIENT_TABLE");

            tableViewConfig.setSortColumn(sortOnColumn);
            tableViewConfig.setSortOrder(sortOrder);
            tableViewConfig.setPageSize(Integer.parseInt(pageSize));
            tableViewConfig.setFilterBy(filterBy);
            tableViewConfig.setType(TableViewConfig.TableType.valueOf(type));
            NodeList childNodes = columnsNode.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node columnNode = childNodes.item(i);
                if(columnNode.getNodeName().equals("column")){
                    String fieldName = getAttributeByName(columnNode, "fieldName");
                    String fieldTitle = getAttributeByName(columnNode, "fieldTitle");
                    String dateFormat = StringUtils.ifEmpty(getAttributeByName(columnNode, "dateFormat"), "yyyy");
                    String hyperLinkField = hasAttributeByName(columnNode, "hyperlinkField")?getAttributeByName(columnNode, "hyperlinkField"):null;
                    TableViewConfig.TableColumn tableColumn = new TableViewConfig.TableColumn(fieldName, fieldTitle, hyperLinkField, dateFormat);
                    tableViewConfig.add(tableColumn);
                }
            }

            return tableViewConfig;
        }

        /**
         *     Sample map node being parsed
         *        <map name=\"Medicine Quality Map\" maxZoomOutLevel=\"2\" range=\"World\" mapLegendRelativePath=\"images/LegendQbL.png\"> <!-- alternative type may be tabular data -->\n" +
         "            <!-- Assuming the range is a enumerated list of places, like World, Asia, Africa, Europe etc-->\n" +
         "            <marker>\n" +
         "                <lonField fieldName=\"CLON\"/>\n" +
         "                <latField fieldName=\"CLAT\"/>\n" +
         "            </marker>\n" +
         "            <legend relativeImagePath=\"images/LegendQbL.png\" positionFromTopInPixels=\"250\"/>\n" +
         "        </map>\n" +
         * @param mapNode
         * @return
         * @throws org.wwarn.mapcore.client.utils.XMLUtils.ParseException
         */
        private ViewConfig parseMapNode(Node mapNode) throws XMLUtils.ParseException {
            String name = getAttributeByName(mapNode, "name");
            String implementation = StringUtils.ifEmpty(getAttributeByName(mapNode, "impl"), MapBuilder.MapImplementation.GOOGLE_V3.toString());
            String initialZoomLevelRaw = StringUtils.ifEmpty(getAttributeByName(mapNode, "initialZoomLevel"), "2");
            String initialMapCenterCoordsLatLonRaw = StringUtils.ifEmpty(getAttributeByName(mapNode, "initialMapCenterCoordsLatLon"), "1.0,1.0");
            final String[] coords = initialMapCenterCoordsLatLonRaw.split(",");
            String doClusterRaw = StringUtils.ifEmpty(getAttributeByName(mapNode, "doCluster"), "false");
            if (coords.length < 2) {
                throw new IllegalArgumentException("Coordinates should be of the format \"lat,lon\"");
            }
            final double initialLat = Double.parseDouble(coords[0]);
            final double initialLon = Double.parseDouble(coords[1]);

            int initialZoomLevel = Integer.parseInt(initialZoomLevelRaw);
            String lonFieldName = "";
            String latFieldName = "";
            String imageLegendRelativePath = "";
            Integer imageLegendPositionFromTopInPixels = null;
            String imageLegendPosition = null;
            String legendToTab = null;
            NodeList childNodes = mapNode.getChildNodes();
            TemplateViewNodesConfig templateViewNodesConfig = null;
            boolean doCluster = Boolean.parseBoolean(doClusterRaw);
            for (int i = 0; i < childNodes.getLength(); i++) {
                try {
                    Node mapChildNode = childNodes.item(i);
                    if (mapChildNode.getNodeName().equals("legend")) {
                        Node mapLegendNode = mapChildNode;
                        imageLegendRelativePath = getAttributeByName(mapLegendNode, "relativeImagePath");
                        imageLegendPositionFromTopInPixels = Integer.valueOf(StringUtils.ifEmpty(getAttributeByName(mapLegendNode, "positionFromTopInPixels"),"0"));
                        imageLegendPosition = (StringUtils.ifEmpty(getAttributeByName(mapLegendNode, "imageLegendPosition"),"BOTTOM_LEFT"));
                        legendToTab = getAttributeByName(mapLegendNode, "legendToTab");
                    }
                    if (mapChildNode.getNodeName().equals("marker")) {
                        Node markerNode = mapChildNode;

                        NodeList markerNodeChildren = markerNode.getChildNodes();
                        for (int j = 0; j < markerNodeChildren.getLength(); j++) {
                            Node markerChildNode = markerNodeChildren.item(j);
                            if (markerChildNode.getNodeName().equals("lonField")) {
                                Node nodeLonField = markerChildNode;
                                lonFieldName = getAttributeByName(nodeLonField, "fieldName");
                            }
                            if (markerChildNode.getNodeName().equals("latField")) {
                                Node nodeLatField = markerChildNode;
                                latFieldName = getAttributeByName(nodeLatField, "fieldName");
                            }
                            if (markerChildNode.getNodeName().equals("InfoWindowTemplate")) {
                                Node infoWindowTemplate = markerChildNode;
                                templateViewNodesConfig = parseInfoWindowTemplate(infoWindowTemplate);
                            }
                        }
                    }

                } catch (Exception e) {
                    XMLUtils.ParseException parseException = new XMLUtils.ParseException("Unable to parse map node", e);
                    e.printStackTrace();
                    throw parseException;
                }
            }

            final MapViewConfig mapViewConfig = new MapViewConfig(name, initialZoomLevel, initialLat, initialLon, lonFieldName, latFieldName, imageLegendRelativePath, imageLegendPosition, imageLegendPositionFromTopInPixels, getNodeCDATAValue(getNodeByName(mapNode, "label")), templateViewNodesConfig, convertTotMapImplementation(implementation), doCluster);
            mapViewConfig.setLegendToTab(legendToTab);
            String mapType = StringUtils.ifEmpty(getAttributeByName(mapNode, "mapType"), "TERRAIN");
            mapViewConfig.setMapType(resolveMapType(mapType));
            configs.put(MapViewConfig.class.getName(), mapViewConfig);
            return mapViewConfig;
        }

        private MapBuilder.MapTypeId resolveMapType(String mapType){
            return MapBuilder.MapTypeId.valueOf(mapType);
        }

        private TemplateViewNodesConfig parseInfoWindowTemplate(Node infoWindowTemplate) throws XMLUtils.ParseException{
            final TemplateViewNodesConfig templateViewNodesConfig = new TemplateViewNodesConfig();
            //data="CLON, CLAT, PID"
            final String data = getAttributeByName(infoWindowTemplate, "dataSourceRestrictedByCurrentMarkerContextFields");
            final String normaliseDataNode = normaliseDataNode(data);
            templateViewNodesConfig.setDataSource(normaliseDataNode);

            parseTemplateViewNodes(infoWindowTemplate, templateViewNodesConfig);

            return templateViewNodesConfig;
        }

        private void parseTemplateViewNodes(Node infoWindowTemplate, final TemplateViewNodesConfig templateViewNodesConfig) throws XMLUtils.ParseException {
            try{
                // parse as a tree based structure
                // walk the nodes
                final TemplateViewNodesConfig.TemplateNode[] lastNode = new TemplateViewNodesConfig.TemplateNode[1];

                XMLUtils.nodeWalker(infoWindowTemplate.getChildNodes(), new XMLUtils.NodeElementParser() {
                    @Override
                    public void parse(Node item) throws XMLUtils.ParseException {
                        final String nodeName = item.getNodeName();
                        switch (nodeName){
                            case "simpleLayout":
                            case "splitLayout":
                                // build branch
                                lastNode[0] = new TemplateViewNodesConfig.LayoutNode(nodeName);
                                templateViewNodesConfig.setRootTemplateNode(lastNode[0]);
                                break;
                            case "left":
                            case "right":
                                //build branch and check for text children
                                final TemplateViewNodesConfig.LayoutNode innerLayoutNode = new TemplateViewNodesConfig.LayoutNode(nodeName);
                                if(lastNode[0].getName().equals("splitLayout")) {
                                    lastNode[0].add(innerLayoutNode);
                                }else{
                                    lastNode[0].getParent().add(innerLayoutNode);
                                }
                                final NodeList childNodes = item.getChildNodes();
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i = 0; i < childNodes.getLength(); i++) {
                                    final Node node = childNodes.item(i);
                                    if(node.getNodeType() == Node.TEXT_NODE){
                                        if(!StringUtils.isEmpty(node.getNodeValue())) {
                                            stringBuilder.append(node.getNodeValue());
                                        }
                                    }
                                }
                                final String html = stringBuilder.toString();
                                if(!StringUtils.isEmpty(html)){
                                    innerLayoutNode.add(new TemplateViewNodesConfig.HtmlNode(html));
                                }
                                lastNode[0] = innerLayoutNode;

                                break;
                            case "plot":
                                final String geom = getAttributeByName(item, "geom");
                                final String data = getAttributeByName(item, "data");
                                final String x = getAttributeByName(item, "x");
                                final String y = getAttributeByName(item, "y");
                                final String xLabel = getAttributeByName(item, "xLabel");
                                final String yLabel = getAttributeByName(item, "yLabel");
                                final String mainTitle = getAttributeByName(item, "mainTitle");
                                final String subTitle = getAttributeByName(item, "subTitle");
                                final TemplateViewNodesConfig.PlottingNode plottingNode = new TemplateViewNodesConfig.PlottingNode(geom, data, x, y, xLabel, yLabel, mainTitle, subTitle);
                                lastNode[0].add(plottingNode);
                                break;
                            case "label":
                                break;
                            default:
                                throw new XMLUtils.ParseException("Node unexpected"+ nodeName);

                        }
                    }
                });

            }catch (Exception e){
                XMLUtils.ParseException parseException = new XMLUtils.ParseException("Unable to parse InfoWindowTemplate node", e);
                e.printStackTrace();
                throw parseException;
            }

        }

        private String normaliseDataNode(String data) {
            final String[] dataRaw = data.split(",");
            StringBuilder sbStr = new StringBuilder();
            for (int i = 0; i < dataRaw.length; i++) {
                if(i>0) sbStr.append(",");
                sbStr.append(dataRaw[i].trim());
            }
            return sbStr.toString();
        }

        private ViewConfig parsePanelNode(Node panelNode) throws XMLUtils.ParseException {
            String name = getAttributeByName(panelNode, "name");
            Node labelNode = getNodeByName(panelNode, "label");
            final String nodeCDATAValue = getNodeCDATAValue(labelNode);

            PanelViewConfig panelViewConfig = new PanelViewConfig(name, nodeCDATAValue);
            String startFilePath = getAttributeByName(panelNode, "startFile");
            panelViewConfig.setStartFile(startFilePath);

            Node pagesNode = getNodeByName(panelNode, "pages");

            NodeList childNodes = pagesNode.getChildNodes();

            List<PanelViewConfig.Page> pages = new ArrayList<PanelViewConfig.Page>();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node pageNode = childNodes.item(i);
                if(pageNode.getNodeName().equals("page")){
                    String whenFilterValue = getAttributeByName(pageNode, "whenFilterValue");
                    String filePath = getAttributeByName(pageNode, "filePath");
                    pages.add(new PanelViewConfig.Page(whenFilterValue,filePath));
                }
            }
            panelViewConfig.setPages(pages);

            return panelViewConfig;
        }
    }

    private MapBuilder.MapImplementation convertTotMapImplementation(String implementation) {
        return MapBuilder.MapImplementation.fromValue(implementation);
    }

    /**
     * Filter parser
     */
    protected class FilterParser extends XMLUtils.NodeElementParser {

        public static final String DEFAULT_START_DATE = "1975";
        public static final String DEFAULT_CURRENT_YEAR = "currentYear";
        public static final String SHOW_HIDE_FEATURE_DISABLED = "showHideFeatureDisabled";

        String filterColumn, filterFieldName,  filterFieldLabel;
        Boolean showHideFeatureEnabled = false, showHideIsVisible = false;

        public void parse(Node item) throws XMLUtils.ParseException {
            // item cannot be null
            NodeList childNodes = item.getChildNodes();
            if(item == null){
                throw new XMLUtils.ParseException("Filter node is empty");
            }
            if(!item.getNodeName().equals("filters")){
                throw new XMLUtils.ParseException("Filter node not found");
            }

            if(childNodes.getLength() < 1){
                throw new XMLUtils.ParseException("No filters configured");
            }

            FilterConfig filterConfig = new FilterConfig();

            NodeList filterChildNodes = item.getChildNodes();

            for (int i = 0; i < filterChildNodes.getLength(); i++) {
                try{
                    Node filterNode = filterChildNodes.item(i);

                    if(filterNode.getNodeName().startsWith("label")){
                        String filterNodeLabel = getNodeCDATAValue(filterNode);
                        filterConfig.setFilterLabel(filterNodeLabel);
                    }

                    if(filterNode.getNodeName().startsWith("filter")){
                        filterColumn = getAttributeByName(filterNode, "field");
                        filterFieldName = getAttributeByName(filterNode, "name");
                        String showItemsOption = getAttributeByName(filterNode, "showItemsOption");
                        final String showHideToggle = StringUtils.ifEmpty(getAttributeByName(filterNode, "showHideToggle"), SHOW_HIDE_FEATURE_DISABLED);
                        if(!showHideToggle.equals(SHOW_HIDE_FEATURE_DISABLED)){
                            showHideFeatureEnabled = true;
                            showHideIsVisible = showHideToggle.toLowerCase().equals("visible");
                        }
//                        Boolean enableShowHideToggle = (Boolean) showHideToggle;
                        FilterConfigVisualization filterConfigVisualization = FilterConfigVisualization.valueOf(StringUtils.ifEmpty(showItemsOption, "AVAILABLE"));
                        Node filterFieldlabelNode = getNodeByName(filterNode, "label");
                        Node visibleItemNode = getNodeByName(filterNode, "visibleItemCount");
                        int visibleItemCount = 0;

                        if(filterFieldlabelNode != null ){
                            filterFieldLabel = getNodeValue(filterFieldlabelNode);
                        }
                        if(visibleItemNode != null){
                            String vcount = StringUtils.ifEmpty(getNodeValue(visibleItemNode), "0");
                            visibleItemCount = Integer.parseInt(vcount);
                        }
                        FacetType facetType = getFilterType(filterNode);

                        if(filterNode.getNodeName().equals("filterByDateRange")){
                            parseFilterByDateRange(filterConfig,filterNode);

                        }else if(filterNode.getNodeName().equals("filterBySampleSize")){
                            parseFilterBySampleSize(filterConfig,filterNode);

                        }else{
                            final HashMap<String, String> filterFieldValueToLabelMap = new HashMap<String, String>();

                            final Node filterValueLabelMap = getNodeByName(filterNode, "filterValueLabelMap");
                            if(filterValueLabelMap != null){
                                final NodeList filterLabelChildNodes = filterValueLabelMap.getChildNodes();
                                for (int j = 0; j < filterLabelChildNodes.getLength(); j++) {
                                    final Node filterLabelNode = filterLabelChildNodes.item(j);
                                    if(filterLabelNode!= null && filterLabelNode.getNodeName().equals("filterLabel")){
                                        final String filterLabelNodeFieldValue = getAttributeByName(filterLabelNode, "fieldValue");
                                        Node textNode = filterLabelNode.getFirstChild();
                                        String filterLabelNodeLabelValue = textNode.getNodeValue();
                                        if(!StringUtils.isEmpty(filterLabelNodeFieldValue, filterLabelNodeLabelValue)){
                                            filterFieldValueToLabelMap.put(filterLabelNodeFieldValue, filterLabelNodeLabelValue);
                                        }
                                    }
                                }
                            }
                            if(filterNode.getNodeName().equals("filterMultipleFields")){
                                String filterColumnsRaw = getAttributeByName(filterNode, "fields");
                                String[] filterColumns = filterColumnsRaw.split(",");
                                if(filterColumns.length < 2){
                                    throw new IllegalArgumentException("Expected attribute fields to be contain more than one comma separated value");
                                }
                                filterConfig.addFilterSetting(new FilterConfig.FilterMultipleFields(filterColumns, filterFieldName, filterFieldLabel, filterFieldValueToLabelMap));
                            }else{
                                filterConfig.addFilterSetting(new FilterSetting.FilterSettingsBuilder(filterColumn, filterFieldName, filterFieldLabel).
                                        setFilterFieldValueToLabelMap(filterFieldValueToLabelMap).setVisibleItemCount(visibleItemCount).
                                        setFilterShowItemsOptions(filterConfigVisualization).setFacetType(facetType).
                                        setIsShowHideToggleEnabled(showHideFeatureEnabled).setDefaultShowHideToggleStateIsVisible(showHideIsVisible).build());
                            }
                        }
                    }
                }catch (Exception e){
                    XMLUtils.ParseException parseException = new XMLUtils.ParseException("Unable to parse filter", e);
                    throw parseException;
                }
            }

            configs.put(FilterConfig.class.getName(), filterConfig);
        }

        private FacetType getFilterType(Node filterNode){
            String filterType = getAttributeByName(filterNode, "type");
            if (filterType != null){
                FacetType facetType = FacetType.valueOf(filterType);
                if (facetType != null){
                    return facetType;
                }
            }

            return FacetType.LABEL_LIST;
        }

        private void parseFilterByDateRange(FilterConfig filterConfig, Node filterNode) throws Exception{
            String endYear = StringUtils.ifEmpty(getAttributeByName(filterNode, "endDate"), DEFAULT_CURRENT_YEAR);
            String startYear = StringUtils.ifEmpty(getAttributeByName(filterNode, "startDate"), DEFAULT_START_DATE);
            boolean isPlayable = Boolean.parseBoolean(StringUtils.ifEmpty(getAttributeByName(filterNode, "playable"), "FALSE"));
            String fieldFrom = StringUtils.ifEmpty(getAttributeByName(filterNode, "fieldFrom"), "");
            String fieldTo = StringUtils.ifEmpty(getAttributeByName(filterNode, "fieldTo"), "");
            String initialStart = StringUtils.ifEmpty(getAttributeByName(filterNode, "initialStart"), null);
            String initialEnd = StringUtils.ifEmpty(getAttributeByName(filterNode, "initialEnd"), null);
            final Node labelNode = getNodeByName(filterNode, "label");
            String label = getNodeValue(labelNode);

            FilterByDateRangeSettings filterByDateRangeSettings =
                    new FilterByDateRangeSettings.DateRangeSettingsBuilder(filterColumn, filterFieldName, filterFieldLabel).
                            setDateStart(startYear).setDateEnd(endYear).setFieldFrom(fieldFrom).setFieldTo(fieldTo)
                            .setPlayable(isPlayable).setTextLabel(label).setInitialStart(initialStart).setInitialEnd(initialEnd).build();

            filterConfig.addDateRangeFilter(filterByDateRangeSettings);
        }

        private void parseFilterBySampleSize(FilterConfig filterConfig, Node filterNode){
            int start = Integer.parseInt(StringUtils.ifEmpty(getAttributeByName(filterNode, "start"), "0"));
            int end = Integer.parseInt(StringUtils.ifEmpty(getAttributeByName(filterNode, "end"), "400"));
            int initialValue = Integer.parseInt(StringUtils.ifEmpty(getAttributeByName(filterNode, "initialValue"), "0"));
            filterConfig.addSampleSizeFilter(filterColumn, filterFieldName, filterFieldLabel, start, end, initialValue);
        }

    }
}

