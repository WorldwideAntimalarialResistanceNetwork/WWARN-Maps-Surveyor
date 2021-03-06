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

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

import java.util.List;
import java.util.Objects;

/**
 *
 */
public abstract class GenericMapWidget extends Composite {
    int LEGEND_X_INDENT = 42; /*just to the right of the map zoom controls */
    int FILTERS_PANEL_Y_POS = 45;
    List<GenericMarker> markers;

    public abstract void indicateLoading();

    public abstract void removeLoadingIndicator();

    public abstract int getZoomLevel();

    public abstract void setZoomLevel(int zoomLevel);

    public abstract CoordinatesLatLon getCenter();

    public abstract void setCenter(CoordinatesLatLon center);

    public void clearMarkers(){
        if(markers == null){return;}
        for (GenericMarker marker : markers) {
            marker.clear();
        }
    }

    /**
     * Set markers would be a better description of this method behaviour, effectively replaces the references to all markers
     * @param m
     */
    public void setMarkers(List<GenericMarker> m){
        this.markers = m;
        for (GenericMarker marker : m) {
            marker.setMap(this);
        }
        //setup any clustering options
        clusterMarkers();
    }

    public abstract void clusterMarkers();

    public abstract HandlerRegistration onLoadComplete(Runnable onLoadComplete);

    public abstract HandlerRegistration addMapZoomEndHandler(Runnable zoomhandler);

    public abstract HandlerRegistration addDragEndHandler(Runnable draghandler);

    public abstract void justResizeMapWidget();

    public abstract void resizeMapWidget();

    public abstract void setMapLegend(LegendOptions legendOptions);

    public abstract void setMapFiltersDisplay(Widget filtersWidget);

    public static class LegendOptions {
        int legendPixelsFromBottom;
        boolean isOpenedByDefault = false;
        LegendPosition screenPosition;
        Widget legendWidget = null;

        private LegendOptions(Widget legendWidget, int legendPixelsFromBottom, LegendPosition screenPosition, boolean openedByDefault) {
            this.legendPixelsFromBottom = legendPixelsFromBottom;
            this.screenPosition = screenPosition;
            this.isOpenedByDefault = openedByDefault;
            this.legendWidget = legendWidget;
        }

        public static LegendOptions createLegendOptions(Widget legendWidget, int legendPixelsFromBottom, LegendPosition screenPosition, boolean isOpenedByDefault) {
            Objects.requireNonNull(screenPosition, "Screen position must be set");
            return new LegendOptions(legendWidget, legendPixelsFromBottom, screenPosition, isOpenedByDefault);
        }
    }

    public static enum LegendPosition{
        TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT
    }
}
