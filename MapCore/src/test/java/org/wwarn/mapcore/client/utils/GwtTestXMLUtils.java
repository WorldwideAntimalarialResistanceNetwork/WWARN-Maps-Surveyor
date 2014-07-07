package org.wwarn.mapcore.client.utils;

/*
 * #%L
 * MapCore
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
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.XMLParser;

/**
 * XML configuration parser test case
 * User: nigel
 * Date: 24/07/13
 * Time: 16:53
 */
public class GwtTestXMLUtils extends GWTTestCase {
    private XMLUtils xmlUtils;

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        this.xmlUtils = new XMLUtils();
    }

    // parse xml and read nodes
    public void testParseXML() throws Exception {
        Document document = XMLParser.parse(getXML());
        assertNotNull(document);
        Element documentElement = document.getDocumentElement();

        final boolean[] hasFoundNodes = {false};
        xmlUtils.nodeWalker(documentElement.getChildNodes(), new XMLUtils.NodeElementParser() {
            @Override
            public void parse(Node item) throws XMLUtils.ParseException {
                if(item!=null && item.getNodeType() == Node.ELEMENT_NODE){
                    hasFoundNodes[0] = true;
                }else{
                    hasFoundNodes[0] = false;
                    return;
                }

            }
        });
        assertEquals("Expected nodes to be found", true, hasFoundNodes[0]);
    }

    @Override
    public String getModuleName() {
        return "org.wwarn.mapcore.Map";
    }

    public String getXML() {
        return "<?xml version=\"1.0\" ?>\n" +
                "\n" +
                "<surveyor>\n" +
                "    <!--\n" +
                "   A sample data source, default might be the standard JSON data source\n" +
                "    -->\n" +
                "    <datasource type=\"LocalClientSideDataProvider\">\n" +
                "        <property name=\"fileLocation\" value=\"data/publications.json\"/>\n" +
                "        <schema>\n" +
                "            <field name=\"CLON\" type=\"CoordinateLon\"/> <!-- type would be controlled vocabulary containing: Coordinate, Date, String, Integer-->\n" +
                "            <field name=\"CLAT\" type=\"CoordinateLat\"/>\n" +
                "            <field name=\"PY\" type=\"Date\"/>\n" +
                "            <field name=\"URL\" type=\"String\"/>\n" +
                "            <field name=\"PTN\" type=\"String\"/>\n" +
                "            <field name=\"DCN\" type=\"String\"/>\n" +
                "            <field name=\"DN\" type=\"String\"/>\n" +
                "            <field name=\"STN\" type=\"String\"/>\n" +
                "            <field name=\"OTN\" type=\"String\"/>\n" +
                "            <field name=\"QI\" type=\"String\"/>\n" +
                "            <field name=\"FA\" type=\"String\"/>\n" +
                "            <field name=\"TTL\" type=\"String\"/>\n" +
                "            <field name=\"PUB\" type=\"String\"/>\n" +
                "            <field name=\"CN\" type=\"String\"/>\n" +
                "            <field name=\"FR\" type=\"Integer\"/>\n" +
                "            <field name=\"DSN\" type=\"String\"/>\n" +
                "        </schema>\n" +
                "    </datasource>\n" +
                "</surveyor>\n";
    }
}
