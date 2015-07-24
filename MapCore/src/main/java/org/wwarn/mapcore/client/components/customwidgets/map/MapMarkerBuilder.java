package org.wwarn.mapcore.client.components.customwidgets.map;

/*
 * #%L
 * MapCore
 * %%
 * Copyright (C) 2013 - 2015 University of Oxford
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
 * Created by nigelthomas on 22/07/2015.
 */
public class MapMarkerBuilder {
    double markerLat = Double.MIN_NORMAL;
    double markerLon = Double.MIN_NORMAL;
    GoogleV3Marker.MarkerIconPathBuilder markerIconPathBuilder;
    Point markerAnchor;
    Integer zIndex;
    GenericMapWidget map;
    String title;
    boolean useDispersion = true;

    boolean preventMarkerRepeatingHorizontally = true;

    public MapMarkerBuilder preventMarkerRepeatingHorizontally(boolean preventMarkerRepeatingHorizontally) {
        this.preventMarkerRepeatingHorizontally = preventMarkerRepeatingHorizontally;
        return this;
    }

    public MapMarkerBuilder setMarkerLat(double markerLat) {
        this.markerLat = markerLat;
        return this;
    }

    public MapMarkerBuilder useDispersion(boolean dispersion) {
        this.useDispersion = dispersion;
        return this;
    }

    public MapMarkerBuilder setMarkerLon(double markerLon) {
        this.markerLon = markerLon;
        return this;
    }

    public MapMarkerBuilder setZindex(Integer zIndex) {
        this.zIndex = zIndex;
        return this;
    }

    public MapMarkerBuilder setMarkerAnchor(Point anchor) {
        this.markerAnchor = markerAnchor;
        return this;
    }

    public MapMarkerBuilder setMarkerIconPathBuilder(GoogleV3Marker.MarkerIconPathBuilder markerIconPathBuilder) {
        this.markerIconPathBuilder = markerIconPathBuilder;
        return this;
    }

    public MapMarkerBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Final build method of the builder, called once all parameters are setup for the Generic Marker
     *
     * @param markerContext the marker context is intended to hold a reference to the record set backing the marker,
     *                      this used later in pop up panel and hover label construction to aid the client in building relevant
     *                      markup from the record set backing the marker
     * @param <T>           A record set backing the marker such as RecordList.Record
     * @return an instance of generic marker
     */
    public <T> GenericMarker<T> createMarker(T markerContext, GenericMapWidget mapWidget) {
        this.map = mapWidget;
        validateRequiredParameters();
        if(map instanceof GoogleV3MapWidget){
            return new GoogleV3Marker<T>(this, markerContext);
        }
        if(map instanceof OfflineMapWidget){
            return new OfflineMapMarker<T>(this, markerContext);
        }
        throw new UnsupportedOperationException();
    }



    private void validateRequiredParameters() {
        if (this.markerLat == Double.MIN_NORMAL || this.markerLon == Double.MIN_NORMAL || this.map == null) {
            throw new IllegalArgumentException("lat and lon, and map must be set..");
        }
    }

}
