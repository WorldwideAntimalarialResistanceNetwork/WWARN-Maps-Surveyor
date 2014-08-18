package org.wwarn.surveyor.client.model;

/*
 * #%L
 * SurveyorCore
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
 * Holds map model
 * User: nigel
 * Date: 15/08/13
 * Time: 15:29
 */
public class MapViewConfig implements ViewConfig {
    private TemplateViewNodesConfig templateViewNodesConfig = new TemplateViewNodesConfig();
    private double initialLat = 1.0;
    private double initialLon = 1.0;
    private int initialZoomLevel = 2;
    private Integer imageLegendPositionFromTopInPixels;
    String viewName = "";
    String markerLongitudeField = "";
    String markerLatitudeField = "";
    String mapImageRelativePath = "";
    private String viewLabel;

    private MapViewConfig(){};

    public MapViewConfig(String viewName, int initialZoomLevel, double initialLat, double initialLon, String markerLongitudeField, String markerLatitudeField, String mapImageRelativePath, Integer imageLegendPositionFromTopInPixels, String mapTabLabel, TemplateViewNodesConfig templateViewNodesConfig) {
        this.viewName = viewName;
        this.markerLongitudeField = markerLongitudeField;
        this.markerLatitudeField = markerLatitudeField;
        this.mapImageRelativePath = mapImageRelativePath;
        this.imageLegendPositionFromTopInPixels = imageLegendPositionFromTopInPixels;
        this.viewLabel = mapTabLabel;
        this.initialZoomLevel = initialZoomLevel;
        this.initialLat = initialLat;
        this.initialLon = initialLon;
        if(templateViewNodesConfig !=null) {
            this.templateViewNodesConfig = templateViewNodesConfig;
        }
    }

    public String getMapImageRelativePath() {
        return mapImageRelativePath;
    }

    public String getMarkerLongitudeField() {
        return markerLongitudeField;
    }

    public String getMarkerLatitudeField() {
        return markerLatitudeField;
    }

    @Override
    public String getViewName() {
        return viewName;
    }

    @Override
    public String getViewLabel() {
        return viewLabel;
    }

    public Integer getImageLegendPositionFromTopInPixels() {
        return imageLegendPositionFromTopInPixels;
    }

    public int getInitialZoomLevel() {
        return initialZoomLevel;
    }

    public double getInitialLat() {
        return initialLat;
    }

    public double getInitialLon() {
        return initialLon;
    }

    public TemplateViewNodesConfig getTemplateViewNodesConfig() {
        return templateViewNodesConfig;
    }
}
