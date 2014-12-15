package org.wwarn.mapcore.client.components.customwidgets.map;

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



/**
* Created by nigelthomas on 09/12/2014.
*/
public class MapBuilder {
    public static final String STOCK_NO_STUDIES_MSG = "No studies found matching the filters chosen.<br/>" +
            "Please choose less stringent criteria.";
    int minZoomLevel = 0;
    private String noStudiesFoundMsg = STOCK_NO_STUDIES_MSG;
    private static final int DEFAULT_ZOOM_LEVEL = 2;

    int mapHeight = 0;
    int mapWidth = 0;
    int zoomLevel = DEFAULT_ZOOM_LEVEL;
    CoordinatesLatLon coordinatesLatLon;

    /**
     * set max zoom out level
     * @param minZoomLevel
     * @return
     */
    public MapBuilder setMinZoomLevel(int minZoomLevel) {
        this.minZoomLevel = minZoomLevel;
        return this;
    }

    /**
     * Set map zoom level
     * @param i
     * @return
     */
    public MapBuilder setZoomLevel(int i) {
        this.zoomLevel = i;
        return this;
    }

    public MapBuilder setCenter(double mapCentreLat, double mapCentreLon){
        coordinatesLatLon = CoordinatesLatLon.newInstance(mapCentreLat, mapCentreLon);
        return this;
    }

    /**
     * sent value to show, when no studies found
     * @param noStudiesFoundMsg
     * @return
     */
    public MapBuilder setNoStudiesFoundMsg(String noStudiesFoundMsg) {
        this.noStudiesFoundMsg = noStudiesFoundMsg;
        return this;
    }

    /**
     * Setup map display properties
     * @param width value in px
     * @param height value in px
     */
    public MapBuilder configureMapDimension(Integer width, Integer height) {
        this.mapHeight = height;
        this.mapWidth = width;
        return this;
    }

    /**
     * Call to return a map widget
     * @return
     */
    public GenericMapWidget createMapWidget() {
        validate();
        return new GoogleV3MapWidget(this);
    }

    public GenericMapWidget createMapWidget(MapType mapType) {
        validate();
        switch (mapType) {
            default:
            case GOOGLE_V3:
                return new GoogleV3MapWidget(this);
            case OPEN_LAYERS_OS_OFFLINE:
                return new OfflineMapWidget(this);
        }
    }

    private void validate() {
        if(this.mapHeight == 0 || this.mapWidth == 0){
            throw new IllegalArgumentException("Expected map width and height to be set");
        }

        if(this.coordinatesLatLon == null){
            throw new IllegalArgumentException("Expected centre coordinates to be set");
        }
    }

    public static enum MapType{
        GOOGLE_V3, OPEN_LAYERS_OS_OFFLINE
    }

}
