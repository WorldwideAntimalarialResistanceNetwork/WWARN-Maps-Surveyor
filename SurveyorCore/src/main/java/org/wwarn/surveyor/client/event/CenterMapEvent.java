package org.wwarn.surveyor.client.event;

import com.google.web.bindery.event.shared.binder.GenericEvent;
import org.wwarn.mapcore.client.components.customwidgets.map.CoordinatesLatLon;

/**
 * Created by suay on 9/5/16.
 */
public class CenterMapEvent extends GenericEvent {

    CoordinatesLatLon coordinatesLatLon;

    int zoomLevel;

    public CenterMapEvent(CoordinatesLatLon coordinatesLatLon, int zoomLevel) {
        this.coordinatesLatLon = coordinatesLatLon;
        this.zoomLevel = zoomLevel;
    }

    public CoordinatesLatLon getCoordinatesLatLon() {
        return coordinatesLatLon;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }
}
