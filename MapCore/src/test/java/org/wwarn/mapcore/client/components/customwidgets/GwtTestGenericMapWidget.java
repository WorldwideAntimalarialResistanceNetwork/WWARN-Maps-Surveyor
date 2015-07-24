package org.wwarn.mapcore.client.components.customwidgets;

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

import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.wwarn.mapcore.client.components.customwidgets.map.*;
import org.wwarn.mapcore.client.utils.AbstractMapsGWTTestHelper;

/**
 * Test custom map widget
 * User: nigel
 * Date: 09/08/13
 * Time: 15:51
 */
public class GwtTestGenericMapWidget extends AbstractMapsGWTTestHelper {
    @Override
    public LoadApi.LoadLibrary[] getLibraries() {
        return null;
    }

    @Override
    public String getModuleName() {
        return "org.wwarn.mapcore.Map";
    }

    public void testBasicMapBuild() throws Exception {
        asyncLibTest(new Runnable() {
            @Override
            public void run() {
                MapBuilder builder = new MapBuilder();
                GoogleV3MapWidget mapWidget = (GoogleV3MapWidget) builder.configureMapDimension(400, 500).setCenter(1.0, 1.0).createMapWidget();
                assertNotNull(mapWidget);
                assertNotNull(mapWidget.getInternalGoogleMapWidget());

                //test clear marker, with none set
                mapWidget.clearMarkers();


                // add some markers
                addMarkersAndTest(mapWidget);


                // test map legend
                mapWidget.setMapLegend(GenericMapWidget.LegendOptions.createLegendOptions(new Image(""), 323, GenericMapWidget.LegendPosition.BOTTOM_LEFT, false));

                finishTest();
            }
        });
    }

    private void addMarkersAndTest(GoogleV3MapWidget mapWidget) {
        MapMarkerBuilder builder = new MapMarkerBuilder();
        builder.setMarkerLon(47.8);
        builder.setMarkerLat(-121.4);
        builder.setTitle("marker title");
        GenericMarker marker = builder.createMarker(new String(), mapWidget);
        marker.setMap(mapWidget);

        //setup hover label
        marker.setupMarkerHoverLabel(new MarkerHoverLabelTestImpl());

        // setup popup window
        marker.setupMarkerClickInfoWindow(new MarkerClickWindowTestImpl());

        assertNotNull(marker);

    }

    public void testMapType() throws Exception {
        asyncLibTest(new Runnable() {
            @Override
            public void run() {
                MapBuilder builder = new MapBuilder();
                GenericMapWidget mapWidget = builder.configureMapDimension(400, 500).setCenter(1.0, 1.0).setMapTypeId(MapBuilder.MapTypeId.HYBRID).createMapWidget();
                assertEquals(MapBuilder.MapTypeId.HYBRID, builder.mapTypeId);
                assertNotNull(mapWidget);
                assertTrue(mapWidget instanceof GoogleV3MapWidget);
                GoogleV3MapWidget googleV3MapWidget = (GoogleV3MapWidget) mapWidget;
                assertNotNull(googleV3MapWidget.getInternalGoogleMapWidget());
                finishTest();
            }
        });
    }

    public class MarkerHoverLabelTestImpl implements GoogleV3Marker.MarkerHoverLabelBuilder<String> {
        public Widget build(String markerContext) {
            return new Label("hello");
        }
    }
    public class MarkerClickWindowTestImpl implements GoogleV3Marker.MarkerClickInfoWindowBuilder<String> {
        public Widget build(String markerContext) {
            return new Label("hello");
        }

    }


}
