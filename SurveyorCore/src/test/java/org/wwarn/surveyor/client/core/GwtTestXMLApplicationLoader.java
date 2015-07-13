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

import com.google.gwt.junit.client.GWTTestCase;
import org.wwarn.surveyor.client.model.*;

import java.util.Iterator;
import java.util.List;

/**
 * User: nigel
 * Date: 25/07/13
 * Time: 15:34
 */
public class GwtTestXMLApplicationLoader extends GWTTestCase {

    private XMLApplicationLoader xmlApplicationLoader;

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        xmlApplicationLoader = new XMLApplicationLoader(Constants.XML_CONFIG);
    }

    @Override
    public String getModuleName() {
        return "org.wwarn.surveyor.surveyorJUnit";
    }

    public void testGetFilterOptions() throws Exception {
        FilterConfig filterConfig = xmlApplicationLoader.getConfig(FilterConfig.class);
        assertNotNull("Filter is null", filterConfig);
        assertEquals(6, filterConfig.getFilterCount());

        List<FilterSetting> filters = filterConfig.getFilters();
        assertEquals("DN", filters.get(0).filterFieldName);
        assertTrue(filters.get(0) instanceof FilterConfig.FilterMultipleFields);
        assertNotNull(((FilterConfig.FilterMultipleFields) filters.get(0)).getFilterColumns());
        assertEquals(2, ((FilterConfig.FilterMultipleFields) filters.get(0)).getFilterColumns().length);
        assertEquals("PTN", filters.get(1).filterFieldName);
        assertEquals("STN", filters.get(2).filterFieldName);

        assertEquals("Medicines", filters.get(0).filterTitle);
        assertEquals("Report type", filters.get(1).filterTitle);
        assertEquals("Collection type", filters.get(2).filterTitle);

        assertEquals("\n" +
                "                Allows the user to select the medicines, based on International Nonproprietary Names (INN)\n" +
                "                or categories of medicines. Medicines are classified in three categories Artemisinin derivatives\n" +
                "                (including all the artemisinin derivative monotherapies), Artemisinin based Combination Therapies (ACTs)\n" +
                "                and Non-artemisinins. These categories are mutually exclusive and individual drugs or combinations are\n" +
                "                listed below. The selection of all publications that contain information on each individual medicine or\n" +
                "                medicine categories can be obtained by filtering.\n" +
                "            ", filters.get(0).filterFieldLabel);
        assertEquals("\n" +
                "                Lists the different types of publication found describing antimalarial drug quality.\n" +
                "                Papers on techniques, drug legislation, reviews, and other reports usually\n" +
                "                do not contain location information and therefore will not appear in the map.\n" +
                "                They can however be found in the report table.\n" +
                "            ", filters.get(1).filterFieldLabel);
        assertEquals("\n" +
                "                Lists the type of sampling methodology used in each report.\n" +
                "                Only studies with evidence describing how randomisation was performed have been included as 'Random Survey'\n" +
                "            ", filters.get(2).filterFieldLabel);

        assertEquals("Expected filterTitle did not match", filters.get(0).filterTitle, filterConfig.getFilterFieldTitleBy(filters.get(0).filterFieldName));
        assertEquals("Expected filterFieldLabel did not match", filters.get(0).filterFieldLabel, filterConfig.getFilterFieldLabelBy(filters.get(0).filterFieldName));


        //test filter field label value map
        assertEquals("All drugs and combination therapies", filters.get(0).getValueLabel("All"));

    }

    public void testGetFilterRange(){
        FilterConfig filterConfig = xmlApplicationLoader.getConfig(FilterConfig.class);
        List<FilterSetting> dateRangeFilters = filterConfig.getFilterConfigBy("PY");
        assertEquals(1, dateRangeFilters.size());
        FilterSetting filterSetting = dateRangeFilters.get(0);
        assertTrue(filterSetting instanceof FilterByDateRangeSettings);
        assertEquals("1975", ((FilterByDateRangeSettings) filterSetting).getDateStart());
        assertEquals("currentYear", ((FilterByDateRangeSettings) filterSetting).getDateEnd());
    }

    public void testDataSourceConfig() throws Exception {
        DatasourceConfig datasource = xmlApplicationLoader.getConfig(DatasourceConfig.class);
        assertNotNull(datasource);
        assertNotNull(datasource.getFilename());
        DatasourceConfig.SchemaConfig schemaConfig = datasource.getConfig();
        assertNotNull(schemaConfig);
        assertEquals(16 ,schemaConfig.getFields().size());
        DatasourceConfig.SchemaConfig.FieldConfig country_lon = schemaConfig.getFields().get("CLON");
        assertEquals("CoordinateLon", country_lon.getFileType());

        // check data schema
        DataSchema dataSchema = new DataSchema(datasource);
        assertNotNull(dataSchema);
        assertNotNull(dataSchema.getUniqueId());
        assertEquals("fldatasurveyorCorepublications.json",dataSchema.getUniqueId());
        assertTrue(dataSchema.hasColumn("CLON"));
    }

    public void testViewConfig() throws Exception {
        ResultsViewConfig resultsViewConfig = xmlApplicationLoader.getConfig(ResultsViewConfig.class);
        assertNotNull(resultsViewConfig);
        // only expecting three elements
        final Iterator<ViewConfig> iterator = resultsViewConfig.iterator();
        ViewConfig viewConfig = iterator.next();
        assertNotNull(viewConfig);
        assertTrue(viewConfig instanceof MapViewConfig);
        MapViewConfig mapViewConfig = (MapViewConfig) viewConfig;
        assertEquals("Medicine Quality Map", viewConfig.getViewName());
        assertEquals("CLAT", ((MapViewConfig) viewConfig).getMarkerLatitudeField());
        assertEquals("CLON", ((MapViewConfig) viewConfig).getMarkerLongitudeField());
        assertEquals("\n" +
                "            \n" +
                "            \n" +
                "            <strong>Medicine Quality Map</strong><p> The Medicine Quality Map tab shows a single pin for each study with\n" +
                "            antimalarial quality data for that country or location. Pins are associated with tabular text explaining \n" +
                "            each selected survey. The pin colour represents the maximum medicine failure rate reported in the survey. \n" +
                "            Once a pin is clicked the number of reports found in the country of the selected pin will be listed below \n" +
                "            the map. Pins associated with the same location will be spread so the second and subsequent pins at the \n" +
                "            same location will be shown at a slightly different locations.<br><br>\n" +
                "            <span style=\"color:#D80009\">\n" +
                "            <strong>Warning</strong> - the pin colour represents the maximum failure rate for the\n" +
                "            medicine with the highest failure rate, not an average. Other medicines, if\n" +
                "            assayed, will therefore be of better quality.<br><br>These data cannot, because of the sampling methodology \n" +
                "            used in most of the\n" +
                "            individual studies, be used to give aggregated estimates of the percentage\n" +
                "            of antimalarials in individual countries or globally that are poor quality.\n" +
                "            As more objective data becomes available we hope that this will be possible.\n" +
                "            </span></p>\n" +
                "            \n" +
                "            ", viewConfig.getViewLabel());
        testInfoWindowTemplate(mapViewConfig.getTemplateViewNodesConfig());

        viewConfig = iterator.next();
        assertTrue(viewConfig instanceof TableViewConfig);
        testTableConfig(viewConfig);

        viewConfig = iterator.next();
        assertTrue(viewConfig instanceof TemplateViewConfig);
        testTemplateConfig((TemplateViewConfig) viewConfig);
    }

    private void testTemplateConfig(TemplateViewConfig viewConfig) {
        assertNotNull(viewConfig.getViewName());
        assertNotNull(viewConfig.getViewLabel());
        final TemplateViewNodesConfig templateViewNodesConfig = viewConfig.getTemplateViewNodesConfig();
        assertNotNull(templateViewNodesConfig);
        assertNotNull(templateViewNodesConfig.getRootTemplateNode());
        assertNull(templateViewNodesConfig.getDataSource());
    }

    /**
     <splitLayout>
     <left>
     <!--
     plot is inspired by qplot from ggplot2
     Attributes
     geom: Short for geometry, supports bar, jitter, line, boxplot
     data: in this context data fetches all related records by matching to current context record on number of field properties, effectively calling getRelatedRecordsByFields(). Default behaviour for data attribute when absent is to get all records.
     TODO:
     * Data transformations may be helpful, such as sorting
     * Support faceting to break up the data into subsets and allow user to select a subset
     * Stats support to summarise the data into useful ways, binning and counting observations for histograms
     * Supporting layer based composition like ggplot2 does!! Would require using a more low level drawing api and migrating ggplot logic to js..
     -->
     <plot geom="bar" data="CLON, CLAT, PID" x="STN" y="OTN" xLabel="x axis" yLabel="y axis" mainTitle="Bar chart example" subTitle="Chart sub title"/>
     </left>
     <right>
     The bar chart shows the the decline in drug effectiveness as time increases
     </right>
     </splitLayout>
     * @param templateViewNodesConfig
     */
    private void testInfoWindowTemplate(TemplateViewNodesConfig templateViewNodesConfig) {
        assertNotNull(templateViewNodesConfig);
        assertEquals("CLON,CLAT", templateViewNodesConfig.getDataSource());
        final TemplateViewNodesConfig.TemplateNode rootTemplateNode = templateViewNodesConfig.getRootTemplateNode();
        assertNotNull(rootTemplateNode);
        final TemplateViewNodesConfig.TemplateNode left = rootTemplateNode.getChild(0);
        assertEquals("left", left.getName());
        final TemplateViewNodesConfig.TemplateNode plot = left.getChild(1);
        assertEquals("plot", plot.getName());
        final TemplateViewNodesConfig.TemplateNode right = rootTemplateNode.getChild(1);
        assertEquals("right", right.getName());
        final TemplateViewNodesConfig.TemplateNode htmlNode = right.getChild(0);
        assertEquals("htmlNode", htmlNode.getName());

    }

    private void testTableConfig(ViewConfig viewConfig) {
        assertEquals("Report table", viewConfig.getViewName());
        TableViewConfig tableViewConfig = (TableViewConfig) viewConfig;
        assertEquals("PY",tableViewConfig.getSortColumn());
        assertEquals("desc",tableViewConfig.getSortOrder());
        assertEquals(5, tableViewConfig.getColumns().size());
        List<TableViewConfig.TableColumn> tableColumns = tableViewConfig.getColumns();


        /**
         <column field-name="FA" field-title="1st Author"/>
         <column field-name="PY" field-title="Year"/>
         <column field-name="TTL" field-title="Title" hyperlink-field="URL"/>
         <column field-name="PUB" field-title="Publication"/>
         <column field-name="CN" field-title="Countries"/>
         */

        assertEquals("FA", tableColumns.get(0).getFieldName());
        assertEquals("1st Author", tableColumns.get(0).getFieldTitle());
        assertNull(tableColumns.get(0).getHyperLinkField());

        assertEquals("PY", tableColumns.get(1).getFieldName());
        assertEquals("Year", tableColumns.get(1).getFieldTitle());
        assertEquals("yyyy", tableColumns.get(1).getDateFormat());
        assertNull(tableColumns.get(1).getHyperLinkField());


        assertEquals("TTL", tableColumns.get(2).getFieldName());
        assertEquals("Title", tableColumns.get(2).getFieldTitle());
        assertNotNull(tableColumns.get(2).getHyperLinkField());
        assertEquals("URL", tableColumns.get(2).getHyperLinkField());


        assertEquals("PUB", tableColumns.get(3).getFieldName());
        assertEquals("Publication", tableColumns.get(3).getFieldTitle());
        assertNull(tableColumns.get(3).getHyperLinkField());


        assertEquals("CN", tableColumns.get(4).getFieldName());
        assertEquals("Countries", tableColumns.get(4).getFieldTitle());
        assertNull(tableColumns.get(4).getHyperLinkField());

    }
}
