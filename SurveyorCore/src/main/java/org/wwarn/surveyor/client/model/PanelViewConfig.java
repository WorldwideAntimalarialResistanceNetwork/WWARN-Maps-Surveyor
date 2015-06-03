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

import java.util.List;

/**
 * Setting to load an HTML panel depending on the value of the filters
 * Created by suay on 5/16/14.
 */
public class PanelViewConfig implements ViewConfig {


    String viewName = "";
    String viewLabel = "";

    String startFile;

    List<Page> pages;

    public PanelViewConfig(String viewName, String viewLabel) {
        this.viewName = viewName;
        this.viewLabel = viewLabel;
    }

    @Override
    public String getViewName() {
        return viewName;
    }

    @Override
    public String getViewLabel() {
        return viewLabel;
    }

    public String getStartFile() {
        return startFile;
    }

    public void setStartFile(String startFile) {
        this.startFile = startFile;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public static class Page {
        final String filterValue, filePath;

        public Page(String filterValue, String filePath) {

            this.filterValue = filterValue;
            this.filePath = filePath;
        }

        public String getFilterValue() {
            return filterValue;
        }

        public String getFilePath() {
            return filePath;
        }
    }

    @Override
    public String toString() {
        return "PanelViewConfig{" +
                "viewName='" + viewName + '\'' +
                ", viewLabel='" + viewLabel + '\'' +
                ", startFile='" + startFile + '\'' +
                ", pages=" + pages +
                '}';
    }
}
