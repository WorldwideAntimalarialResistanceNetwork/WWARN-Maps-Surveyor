//package org.wwarn.mapcore.client.map;

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

//
//import com.google.gwt.http.client.*;
//import com.google.gwt.json.client.*;
//import com.google.gwt.user.client.ui.HTML;
//import com.google.gwt.user.client.ui.RootPanel;
//import org.wwarn.mapcore.client.map.data.CountryOutline;
//import org.wwarn.mapcore.client.map.data.CountryPartOutline;
//
//import java.util.HashMap;
//
///**
//* User: richardc
//* Date: 23-Sep-2009
//* Time: 18:37:41
//*/
//public class MapPolygonLoader {
//    private MapWidgetWithLayerSupport mapWidget;
//
//    public MapPolygonLoader(MapWidgetWithLayerSupport mapWidget) {
//        this.mapWidget = mapWidget;
//    }
//
//
//    public void fetchCountryOutlines() {
//        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, "countries.JSON");
//        try {
//            requestBuilder.sendRequest(null, new RequestCallback() {
//
//                public void onResponseReceived(Request request, Response response) {
//                    if (200 == response.getStatusCode()) {
//                        try {
//                            // parse the response text into JSON
//                            JSONValue jsonValue = JSONParser.parseLenient(response.getText());
//                            JSONArray jsonArray = jsonValue.isArray();
//
//                            if (jsonArray != null) {
//                                updateTable(jsonArray);
//                            } else {
//                                throw new JSONException();
//                            }
//                        } catch (JSONException e) {
//                            e.toString();
////                            displayError("Could not parse JSON");
//                        }
//                    } else {
////                        displayError("Couldn't retrieve JSON (" + response.getStatusText() + ")");
//                    }
//
//
////                    RootPanel.get().add(new HTML("Response received!!! "));// + response.getText()));
//                }
//
//                public void onError(Request request, Throwable throwable) {
//                    RootPanel.get().add(new HTML("Problem occured: unable to load country outlines."));
//                }
//            });
//        } catch (RequestException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void updateTable(JSONArray array) {
//
//        HashMap<String, CountryOutline> countryEPolylineHashMap = new HashMap<String, CountryOutline>();
//
//        JSONValue jsonValue;
//
//
//        for (int i = 0; i < array.size(); i++) {
//            JSONObject jsStock;
//            JSONString jsCountryISOCode, jsEncodedPolygon;
//
//            if ((jsStock = array.get(i).isObject()) == null) continue;
//
//            if ((jsonValue = jsStock.get("iso")) == null) continue;
//            if ((jsCountryISOCode = jsonValue.isString()) == null) continue;
//
//            if ((jsonValue = jsStock.get("epoly")) == null) continue;
//            if ((jsEncodedPolygon = jsonValue.isString()) == null) continue;
//
//            CountryOutline countryOutline = new CountryOutline();
//
//            String data = jsEncodedPolygon.stringValue();
//            int iStart = 0;
//            Boolean arePoints = true;
//
//            String encodedPoints = "";
//
//            //File structure is [encoded Levels<br>encoded Polylines<br>...]
//            do {
//                int iEnd = data.indexOf("<br>", iStart);
//                if (!arePoints) {//Levels
//                    CountryPartOutline partOutline = new CountryPartOutline();
//                    partOutline.setLevels(data.substring(iStart, iEnd));
//                    partOutline.setEncodedPoints(encodedPoints);
//                    countryOutline.addCountryPartOutline(partOutline);
//
//                    arePoints = true;
//                } else {//Points
//
//
//                    encodedPoints = data.substring(iStart, iEnd);
//
//                    arePoints = false;
//                }
//                iStart = iEnd + 4;//jump above '<br>' to the next polylines
//            } while (data.length() > iStart);
//
//            countryEPolylineHashMap.put(jsCountryISOCode.stringValue(), countryOutline);
//        }
//
//        mapWidget.setCountryEPolylineHashMap(countryEPolylineHashMap);
//    }
//}package org.wwarn.mapcore.client.map;
//
//import com.google.gwt.http.client.*;
//import com.google.gwt.json.client.*;
//import com.google.gwt.user.client.ui.HTML;
//import com.google.gwt.user.client.ui.RootPanel;
//import org.wwarn.mapcore.client.map.data.CountryOutline;
//import org.wwarn.mapcore.client.map.data.CountryPartOutline;
//
//import java.util.HashMap;
//
///**
//* User: richardc
//* Date: 23-Sep-2009
//* Time: 18:37:41
//*/
//public class MapPolygonLoader {
//    private MapWidgetWithLayerSupport mapWidget;
//
//    public MapPolygonLoader(MapWidgetWithLayerSupport mapWidget) {
//        this.mapWidget = mapWidget;
//    }
//
//
//    public void fetchCountryOutlines() {
//        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, "countries.JSON");
//        try {
//            requestBuilder.sendRequest(null, new RequestCallback() {
//
//                public void onResponseReceived(Request request, Response response) {
//                    if (200 == response.getStatusCode()) {
//                        try {
//                            // parse the response text into JSON
//                            JSONValue jsonValue = JSONParser.parseLenient(response.getText());
//                            JSONArray jsonArray = jsonValue.isArray();
//
//                            if (jsonArray != null) {
//                                updateTable(jsonArray);
//                            } else {
//                                throw new JSONException();
//                            }
//                        } catch (JSONException e) {
//                            e.toString();
////                            displayError("Could not parse JSON");
//                        }
//                    } else {
////                        displayError("Couldn't retrieve JSON (" + response.getStatusText() + ")");
//                    }
//
//
////                    RootPanel.get().add(new HTML("Response received!!! "));// + response.getText()));
//                }
//
//                public void onError(Request request, Throwable throwable) {
//                    RootPanel.get().add(new HTML("Problem occured: unable to load country outlines."));
//                }
//            });
//        } catch (RequestException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void updateTable(JSONArray array) {
//
//        HashMap<String, CountryOutline> countryEPolylineHashMap = new HashMap<String, CountryOutline>();
//
//        JSONValue jsonValue;
//
//
//        for (int i = 0; i < array.size(); i++) {
//            JSONObject jsStock;
//            JSONString jsCountryISOCode, jsEncodedPolygon;
//
//            if ((jsStock = array.get(i).isObject()) == null) continue;
//
//            if ((jsonValue = jsStock.get("iso")) == null) continue;
//            if ((jsCountryISOCode = jsonValue.isString()) == null) continue;
//
//            if ((jsonValue = jsStock.get("epoly")) == null) continue;
//            if ((jsEncodedPolygon = jsonValue.isString()) == null) continue;
//
//            CountryOutline countryOutline = new CountryOutline();
//
//            String data = jsEncodedPolygon.stringValue();
//            int iStart = 0;
//            Boolean arePoints = true;
//
//            String encodedPoints = "";
//
//            //File structure is [encoded Levels<br>encoded Polylines<br>...]
//            do {
//                int iEnd = data.indexOf("<br>", iStart);
//                if (!arePoints) {//Levels
//                    CountryPartOutline partOutline = new CountryPartOutline();
//                    partOutline.setLevels(data.substring(iStart, iEnd));
//                    partOutline.setEncodedPoints(encodedPoints);
//                    countryOutline.addCountryPartOutline(partOutline);
//
//                    arePoints = true;
//                } else {//Points
//
//
//                    encodedPoints = data.substring(iStart, iEnd);
//
//                    arePoints = false;
//                }
//                iStart = iEnd + 4;//jump above '<br>' to the next polylines
//            } while (data.length() > iStart);
//
//            countryEPolylineHashMap.put(jsCountryISOCode.stringValue(), countryOutline);
//        }
//
//        mapWidget.setCountryEPolylineHashMap(countryEPolylineHashMap);
//    }
//}