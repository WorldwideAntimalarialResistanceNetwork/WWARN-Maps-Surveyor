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

import com.google.gwt.xml.client.CharacterData;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

/**
 * Created with IntelliJ IDEA.
 * User: nigel
 * Date: 24/07/13
 * Time: 16:53
 */
public class XMLUtils {

    /**
     * nodeWalker walker implementation, simple depth first recursive implementation
     * @param childNodes take a node list to iterate
     * @param parser an observer which parses a node element
     * @throws ParseException
     */

    public static void nodeWalker(NodeList childNodes, NodeElementParser parser) throws ParseException {
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            String node = item.getNodeName();
            if(item.getNodeType() == Node.ELEMENT_NODE){
                parser.parse(item);
            }
            nodeWalker(item.getChildNodes(), parser);
        }
    }

    /**
     * Delegated observer pattern
     */
    public static abstract class NodeElementParser {
        /**
         * Intended to accept
         * @param item parse node item
         * @throws ParseException
         */
        public abstract void parse(Node item) throws ParseException;

        /**
         * convenience method to fetch node attribute by name
         * @param filterNode
         * @param fieldName
         * @return
         */
        protected String getAttributeByName(Node filterNode, String fieldName) {
            Node namedItem = filterNode.getAttributes().getNamedItem(fieldName);
            if(namedItem == null){
                return null;
            }
            return namedItem.getNodeValue();
        }
        /**
         * convenience method to check if node attribute by name exists
         * @param filterNode
         * @param fieldName
         * @return
         */
        protected boolean hasAttributeByName(Node filterNode, String fieldName) {
            return !(filterNode.getAttributes().getNamedItem(fieldName) == null);
        }
        /**
         * convenience method to fetch node by name
         * @param node
         * @param nodeName
         * @return
         */
        protected Node getNodeByName(Node node, final String nodeName) throws ParseException {
            final Node[] nodeRequested = new Node[1];
            nodeWalker(node.getChildNodes(), new NodeElementParser() {
                @Override
                public void parse(Node item) throws ParseException {
                    if(item.getNodeName().equals(nodeName)){
                        nodeRequested[0] = item;
                    }
                }
            });
            return nodeRequested[0];
        }

        protected String getNodeValue(Node node) throws ParseException {
            final Node firstChild = node.getFirstChild();
            String nodeContents;
            if(firstChild == null) {return null;}
            nodeContents = firstChild.getNodeValue();

            return nodeContents;
        }

        protected String getNodeCDATAValue(Node cdataNode) throws ParseException {
            if(cdataNode == null) {return null;}
            final Node firstChild = cdataNode.getFirstChild();
            if(firstChild == null) {return null;}
            String nodeContents = firstChild.getNodeValue();
            if(XMLParser.supportsCDATASection()){
                final NodeList filterLabelNodeChildNodes = cdataNode.getChildNodes();
                for (int c = 0; c < filterLabelNodeChildNodes.getLength(); c++) {
                    final Node node = filterLabelNodeChildNodes.item(c);
                    if(firstChild instanceof CharacterData){
                        final String data = ((CharacterData) node).getData();
                        nodeContents += data;
                    }else {
                        nodeContents += node.getNodeValue();
                    }
                }
            }
            return nodeContents;
        }
    }

    public static class ParseException extends Exception {

        public ParseException() {
            super();
        }

        public ParseException(String message) {
            super(message);
        }

        public ParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
