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

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.mouseout.MouseOutMapEvent;
import com.google.gwt.maps.client.events.mouseout.MouseOutMapHandler;
import com.google.gwt.maps.client.events.mouseover.MouseOverMapEvent;
import com.google.gwt.maps.client.events.mouseover.MouseOverMapHandler;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by nigelthomas on 22/07/2015.
 */
public abstract class GenericMarker<T> {
    protected final T context;
    protected final GenericMapWidget genericMapWidget;
    MarkerIconPathBuilder markerIconPathBuilder;

    public GenericMarker(GenericMapWidget mapWidget, T context) {
        this.genericMapWidget = mapWidget;
        this.context = context;
    }

    public abstract void addClickHandler(final MarkerCallBackEventHandler<GenericMarker> clickCallback);

    abstract void addMouseOverHandler(final MarkerCallBackEventHandler<GenericMarker> mouseOverCallback);

    abstract void addMouseOutMoveHandler(final MarkerCallBackEventHandler<GenericMarker> mouseOutCallback);

    public T getContext() {
        return context;
    }

    public void setMap(GenericMapWidget mapWidget){};

    abstract void clear();

    public void setupMarkerClickInfoWindow(final MarkerClickInfoWindowBuilder markerClickInfoWindow){
        addClickHandler(new MarkerCallBackEventHandler<GenericMarker>() {
            @Override
            public void run(GenericMarker sourceElement) {
                if(markerClickInfoWindow instanceof MarkerClickInfoWindowBuilderAsync){
                    ((MarkerClickInfoWindowBuilderAsync) markerClickInfoWindow).build(getContext(),
                            new AsyncCallback<Widget>() {
                                @Override
                                public void onFailure(Throwable throwable) {
                                    throw new IllegalStateException(throwable);
                                }

                                @Override
                                public void onSuccess(Widget widget) {
                                    displayPopup(widget);
                                }
                            });
                }else if(markerClickInfoWindow instanceof MarkerClickInfoWindowBuilder) {
                    displayPopup(markerClickInfoWindow.build(getContext()));
                }
            }
        });
    }

    public static class DefaultMarkerIconPathBuilder implements GenericMarker.MarkerIconPathBuilder {
        @Override
        public String getMarkerIconPath(Object context) {
            return MarkerIcon.RED.getPath();
        }
    }

    public enum MarkerIcon {
        // Markers are stacked such that standard (round) markers appear above other shapes.
        // Red markers appear on top - since users are more likely to be interested in the 'danger' pins
        GREEN("quality0.png",7,18,460),YELLOW("quality1.png",7,18,470),LIGHT_ORANGE("quality3.png",7,18,480),DARK_ORANGE("quality3.png",7,18,490),RED("quality4.png",7,18,500),
        BLUESQUARE("molwhite.png",7,18, 300), BLUESQUAREEAST("molwhiteeast.png",0,6, 300),
        BLUETRIANGLE("inwhite.png",7,18,300),
        BLUEPUSHPIN("pharwhite.png",7,18,300), BLUEPUSHPINWEST("pharwhitewest.png",18,7,300);
        private String ICON_BASE_PATH = GWT.getModuleBaseForStaticFiles()+ "images/icons/";
        private final String path;
        private final Integer anchorX, anchorY, Zrank;

        MarkerIcon(String iconPath,Integer anchorX, Integer anchorY, Integer Zrank){
            this.path = ICON_BASE_PATH+iconPath;
            this.anchorX = anchorX;
            this.anchorY = anchorY;
            this.Zrank = Zrank;
        }

        public String getPath() {
            return path;
        }
        public Integer getAnchorX() { return anchorX; }
        public Integer getAnchorY() { return anchorY; }
        public Integer getZrank() { return Zrank; }
    }
    abstract void displayPopup(Widget infoWindowWidget);

    /**
     * Expose an interface for adding labels from the client, without the client needing to be aware of logic for:
     * * calculating label position,
     * * hover event handlers mapping to show and hide of a label
     * @param markerHoverLabelBuilder see docs for GenericMarker.MarkerHoverLabelBuilder
     * @see GenericMarker.MarkerHoverLabelBuilder
     */
    public abstract void setupMarkerHoverLabel(final MarkerHoverLabelBuilder markerHoverLabelBuilder);

    /**
     * Marker call back event handler
     * @param <U>
     */
    public interface MarkerCallBackEventHandler<U extends GenericMarker> {
        public void run(U sourceElement);
    }

    /**
     * An interface used by clients of Generic Marker to build the contents of a hover label,
     * the backing record for the marker is available
     * @param <T> the backing record for the marker
     */
    public interface MarkerHoverLabelBuilder<T>{

        /**
         *
         * @param markerContext backing record for the marker is available
         * @return the returned widget is added to the main body of the label
         */
        public Widget build(T markerContext);
    }

    /**
     * Logic to map a icon to the underlying data can be specified through this interface
     * @param <T> the backing record for the marker
     */
    public interface MarkerIconPathBuilder<T>{
        String getMarkerIconPath(T context);
    }
    /**
     * An alternative to MarkerIconPathBuilder with async methods
     * @param <T> the backing record for the marker
     * @see GoogleV3Marker.MarkerIconPathBuilder
     */
    public interface MarkerIconPathBuilderAsync<T> extends MarkerIconPathBuilder<T>{
        void getMarkerIconPath(T context, AsyncCallback<String> result);
    }
    /**
     * An interface used by clients of Generic Marker to build the contents of a popup panel,
     * the backing record for the marker is available
     */
    public interface MarkerClickInfoWindowBuilder<T>{
        public Widget build(T markerContext);
    }

    /**
     *  An alternative to MarkerClickInfoWindowBuilder with async methods
     * @see GenericMarker.MarkerClickInfoWindowBuilder
     * @param <T>
     */
    public interface MarkerClickInfoWindowBuilderAsync<T> extends MarkerClickInfoWindowBuilder {
        public void build(T markerContext, AsyncCallback<Widget> result);
    }


}
