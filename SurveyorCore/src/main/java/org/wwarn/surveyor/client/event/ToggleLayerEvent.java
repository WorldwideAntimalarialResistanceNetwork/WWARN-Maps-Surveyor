package org.wwarn.surveyor.client.event;

import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * Turns the specified layer on/off
 */
public class ToggleLayerEvent extends GenericEvent {
    private String layerName;

    public ToggleLayerEvent(String layerName) {
        this.layerName = layerName;
    }

    public String getLayerName() {
        return layerName;
    }
}
