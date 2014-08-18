package org.wwarn.mapcore.client.components.customwidgets.facet;

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

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.*;
import org.wwarn.mapcore.client.components.customwidgets.facet.FacetListBoxWidget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by suay on 14/03/14.
 * A widget to display a facet with a text input where you can search for an item
 * The logic of this widget is similar to a suggestedBox, but instead of auto-complete
 * the text box, it filters the results in the list box.
 */
@Deprecated
public class SearchableFacetWidget extends Composite implements KeyUpHandler {

    public static final String STYLE_FILTER_TITLE = "filterTitle";
    public static final String STYLE_FILTER_SEARCH_BOX = "filterSearchBox";
    public static final String STYLE_FILTER_LIST_BOX = "filterListBox";

    protected TextBox textBox = new TextBox();

    protected FacetListBoxWidget facetWidget;

    //This list contains all the items in the Facetwidget
    protected List<FacetWidgetItem> listItems = null;

    MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();

    //Create the widget
    public SearchableFacetWidget(FacetWidget facetWidget) {
        this.facetWidget = (FacetListBoxWidget) facetWidget;
        this.listItems = this.facetWidget.getFacetWidgetItems();

        VerticalPanel facetPanel = this.facetWidget.panel;
        Label title =(Label) facetPanel.getWidget(0);
        ListBox listBox = (ListBox) facetPanel.getWidget(1);

        textBox.addKeyUpHandler(this);

        setOracle();
        initWidget(buildPanel(title, listBox));

    }


    private Panel buildPanel(Label title, ListBox listBox){
        VerticalPanel panel = new VerticalPanel();
        title.setStyleName(STYLE_FILTER_TITLE);
        textBox.setStyleName(STYLE_FILTER_SEARCH_BOX);
        listBox.setStyleName(STYLE_FILTER_LIST_BOX);
        panel.add(title);
        panel.add(textBox);
        panel.add(listBox);
        return panel;
    }

    @Override
    public void onKeyUp(KeyUpEvent keyUpEvent) {
        String text = textBox.getText();

        if(text.isEmpty() ){
            populateListBox();
        }else{
            oracle.requestSuggestions(new SuggestOracle.Request(text, 20), callback);
        }
    }

    /**
     * Once the user adds a text into the textBox, this method is responsible to update
     * the listBox with the suggestions.
     */
    private final SuggestOracle.Callback callback = new SuggestOracle.Callback() {
        public void onSuggestionsReady(SuggestOracle.Request request, SuggestOracle.Response response) {

            Collection<MultiWordSuggestOracle.MultiWordSuggestion> sugs = (Collection<MultiWordSuggestOracle.MultiWordSuggestion>) response.getSuggestions();

            List<FacetWidgetItem> suggestedList = new ArrayList<FacetWidgetItem>();
            for (FacetWidgetItem facetWidgetItem : listItems){
                for (MultiWordSuggestOracle.MultiWordSuggestion sug : sugs) {
                    if(facetWidgetItem.getValue().equalsIgnoreCase(sug.getReplacementString())){
                        suggestedList.add(facetWidgetItem);
                    }
                }
            }
            facetWidget.addAll(suggestedList);

        }
    };

    /**
     * Populate the listBox with the initial values
     */
    private void populateListBox(){
        facetWidget.addAll(listItems);

    }

    /**
     * Initially set the oracle with all the item in the listBox
     * We will use the oracle to get suggestions from a text input
     */
    private void setOracle(){
        for (FacetWidgetItem facetWidgetItem : listItems) {
            oracle.add(facetWidgetItem.getValue());
        }
    }

    public void setListItems(List<FacetWidgetItem> listItems) {
        this.listItems = listItems;
    }

    public FacetListBoxWidget getFacetWidget() {
        return facetWidget;
    }


}
