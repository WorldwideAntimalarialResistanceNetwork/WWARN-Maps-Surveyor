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

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.events.closeclick.CloseClickMapEvent;
import com.google.gwt.maps.client.events.closeclick.CloseClickMapHandler;
import com.google.gwt.maps.client.mvc.MVCObject;
import com.google.gwt.maps.client.overlays.InfoWindow;
import com.google.gwt.maps.client.overlays.InfoWindowOptions;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

/**
 * Making basic marker a shared instance, this forces only a single popup window to appear on the display
 * based on <a href="http://stackoverflow.com/a/15111870">recommendation</a>
 */
public class MarkerClickInfoWindowSingleton {
    private static MarkerClickInfoWindowSingleton ourInstance = new MarkerClickInfoWindowSingleton();

    public static MarkerClickInfoWindowSingleton getInstance() {
        return ourInstance;
    }
    private final InfoWindow infoWindow;
    private List<MapInfoWindowOpenCloseHandler> listeners = new ArrayList<MapInfoWindowOpenCloseHandler>();

    public MarkerClickInfoWindowSingleton() {
        InfoWindowOptions options = InfoWindowOptions.newInstance();
        options.setMaxWidth(800);
        infoWindow = InfoWindow.newInstance(options);

        infoWindow.addCloseClickHandler(new CloseClickMapHandler() {
            @Override
            public void onEvent(CloseClickMapEvent event) {
                for(MapInfoWindowOpenCloseHandler listener : listeners) {
                    listener.onInfoWindowClose();
                }
            }
        });

    }

    void display(Widget widget, MapWidget mapWidget, MVCObject<?> markerWidget) {
        infoWindow.setContent(widget);
        infoWindow.open(mapWidget, markerWidget);
        for(MapInfoWindowOpenCloseHandler listener : listeners) {
            listener.onInfoWindowOpen();
        }

    }

    public void addInfoWindowOpenCloseHandler(MapInfoWindowOpenCloseHandler listener) {
        listeners.add(listener);
    }

}
