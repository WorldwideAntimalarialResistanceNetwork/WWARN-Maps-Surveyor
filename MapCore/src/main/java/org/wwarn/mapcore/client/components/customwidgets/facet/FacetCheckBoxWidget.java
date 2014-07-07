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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.*;
import org.wwarn.mapcore.client.common.types.FilterConfigVisualization;
import org.wwarn.mapcore.client.utils.StringUtils;


import java.util.*;

/**
 * Created by suay on 6/11/14.
 */
public class FacetCheckBoxWidget extends Composite implements FacetWidget {

    public static final String STYLE_DISABLED_CHECK_BOX = "disabledCheckBox";

    interface FacetCheckBoxWidgetsUiBinder extends UiBinder<VerticalPanel, FacetCheckBoxWidget> {

    }
    private static FacetCheckBoxWidgetsUiBinder ourUiBinder = GWT.create(FacetCheckBoxWidgetsUiBinder.class);


    public static final String STYLE_CHECKBOXLIST = "checkBoxes";
    public static final String STYLE_CLEAR_LINK = "clearLink";
    public static final String STYLE_CHECKBOXLIST_ITEMS = "checkBoxListItem";
    private static final String TOOL_TIP_POPUP_WIDTH = "190px";
    private static final PopupPanel toolTipPopup = new PopupPanel(true);
    public static final int DEFAULT_VISIBLE_ITEM_COUNT = 5;
    private static final int CHECKBOX_HEIGHT = 19; // height measured at about 19px in firefox..

    private int lastKnownIndices = 0;
    private String facetField = null;
    String facetTitle = null;
    String facetLabel = null;
    int visibleItemCount = 0;
    FilterConfigVisualization filterConfigVisualization;

    private Set<String> selectedListItems = new HashSet<String>();

    List<FacetWidgetItem> facetWidgetItems = new ArrayList<FacetWidgetItem>();


    VerticalPanel checkBoxVerticalPanel = new VerticalPanel();

    final FocusPanel listPanel = new FocusPanel(checkBoxVerticalPanel);

    VerticalPanel panel;

    List<CheckBox> checkBoxes = new ArrayList<CheckBox>();

    private Anchor clearSelectionControl = getClearSelectionAnchor();
    private ScrollPanel scrollpanel = new ScrollPanel();

    private Anchor getClearSelectionAnchor() {
        final Anchor clear = new Anchor("clear");
        clear.setVisible(false);
        addStyle(clear);
        clear.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                unSelectAndReset();
                //TODO: resolve this hack
                // fire value change event inform client that reset was completed
                ValueChangeEvent.fire(checkBoxes.iterator().next(), true);
            }
        });
        return clear;
    }

    private void addStyle(Anchor clear) {
        clear.setStyleName(STYLE_CLEAR_LINK);
    }


    public FacetCheckBoxWidget(FacetBuilder builder){
        panel = ourUiBinder.createAndBindUi(this);

        this.facetWidgetItems = builder.getListItems();
        this.facetField = StringUtils.ifEmpty(builder.getFacetName(), builder.getFacetTitle());
        this.facetTitle = builder.getFacetTitle();
        this.facetLabel = builder.getFacetLabel();
        this.filterConfigVisualization = builder.getFilterConfigVisualization();
        this.visibleItemCount = (builder.getVisibleItemCount() < 1)?DEFAULT_VISIBLE_ITEM_COUNT:builder.getVisibleItemCount();
        buildDisplay();
        unSelectAndReset();
        setupToolTip();
        initWidget(panel);

    }

    private void setupToolTip() {
        toolTipPopup.setWidth(TOOL_TIP_POPUP_WIDTH);
        listPanel.addMouseMoveHandler(new MouseMoveHandler() {
            @Override
            public void onMouseMove(MouseMoveEvent mouseMoveEvent) {
                int currentIndices = getItemIndices(mouseMoveEvent.getY(), listPanel);
                if (currentIndices != lastKnownIndices) {
                    toolTipPopup.hide();
                    setToolTipContentWithIndices(currentIndices, listPanel);
                    toolTipPopup.show();
                    lastKnownIndices = currentIndices;
                }
            }
        });

        listPanel.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent mouseOverEvent) {
                final int currentIndices = getItemIndices(mouseOverEvent.getY(), listPanel);
                setToolTipContentWithIndices(currentIndices, listPanel);
                toolTipPopup.show();
            }
        });

        listPanel.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent mouseOutEvent) {
                toolTipPopup.hide();
            }
        });
    }

    public FacetWidget buildDisplay(){
        panel = new VerticalPanel();
        setupListPanel();
        scrollpanel.setHeight(+calculateScrollPanelHeight()+"px");
        scrollpanel.add(listPanel);
        scrollpanel.setStyleName(STYLE_CHECKBOXLIST);
        panel.add(buildHTMLHeader());
        panel.add(scrollpanel);
        return this;
    }

    private FocusPanel setupListPanel(){
        for(FacetWidgetItem item : facetWidgetItems){
            final CheckBox checkBox = new CheckBox(item.getValue());
            checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    check(checkBox);
                }
            });
            checkBox.setStyleName(STYLE_CHECKBOXLIST_ITEMS);
            checkBoxes.add(checkBox);
            checkBoxVerticalPanel.add(checkBox);
        }

        return listPanel;
    }

    private void check(CheckBox checkBox){
        if(checkBox.getValue()){
            selectedListItems.add(checkBox.getText());
            clearSelectionControl.setVisible(true);
        }else{
            removeItem(checkBox.getText());
            if(selectedListItems.isEmpty()){
                clearSelectionControl.setVisible(false);
            }
        }
    }


    private void  removeItem(String item){
        selectedListItems.remove(item);
    }

    public HTMLPanel buildHTMLHeader() {
        HTMLPanel heading = new HTMLPanel("<br/><strong>"+ facetTitle +"</strong>");
        String headingStyleName = "subHeading";
        heading.setStyleName(headingStyleName);
        heading.add(clearSelectionControl);
        return heading;
    }

    //TODO: This is an eyesore, Natxo needs to clean this
    @Override
    public HandlerRegistration addChangeHandler(final ChangeHandler changeHandler) {
        ValueChangeHandler valueChangeHandler = new ValueChangeHandler() {
            @Override
            public void onValueChange(ValueChangeEvent valueChangeEvent) {
                changeHandler.onChange(null);
            }
        };
        for(CheckBox checkBox : checkBoxes){
            checkBox.addValueChangeHandler(valueChangeHandler);
        }
        return null;

    }

    @Override
    public void disableItems(List<FacetWidgetItem> facetWidgetItems) {
        Set<CheckBox> checkBoxesToMove = new LinkedHashSet<>();
        Set<FacetWidgetItem> facetWidgetsItemToMove = new LinkedHashSet<>();

        for(CheckBox checkBox : checkBoxes){
            boolean checkBoxChanged = false;
            for(FacetWidgetItem item : facetWidgetItems){
               if(checkBox.getText().equals(item.getValue())){
                   disableCheckBox(checkBox);
                   checkBoxesToMove.add(checkBox);
                   facetWidgetsItemToMove.add(item);
                   checkBoxChanged = true;
                   break;
               }
            }
            if(!checkBoxChanged){
                 enableCheckBox(checkBox);
            }
        }
        moveCheckBoxToTheBottomFrom(checkBoxesToMove);
        moveFacetWidgetItemsToBottom(facetWidgetsItemToMove);
    }

    private void moveFacetWidgetItemsToBottom(Set<FacetWidgetItem> facetWidgetsItemToMove) {
        if(facetWidgetsItemToMove.size() < 1){return;}
        List<FacetWidgetItem> facetWidgetItemFresh = new ArrayList<>();

        for (FacetWidgetItem item : facetWidgetItems) {
            if(!facetWidgetsItemToMove.contains(item)){
                facetWidgetItemFresh.add(item);
            }
        }

        facetWidgetItemFresh.addAll(facetWidgetsItemToMove);
        facetWidgetItems = facetWidgetItemFresh;
    }


    private void moveCheckBoxToTheBottomFrom(Set<CheckBox> checkBoxesToMove) {
        if(checkBoxesToMove.size() < 1){return;}
        List<CheckBox> checkBoxesFresh = new ArrayList<>();

        for (CheckBox checkBox : checkBoxes) {
            if(!checkBoxesToMove.contains(checkBox)){
                checkBoxesFresh.add(checkBox);
            }else{
                moveCheckBoxToTheBottomFromView(checkBox);
            }
        }

        checkBoxesFresh.addAll(checkBoxesToMove);
        checkBoxes = checkBoxesFresh;
    }

    private void enableCheckBox(CheckBox checkBox) {
        checkBox.setEnabled(true);
        checkBox.removeStyleName(STYLE_DISABLED_CHECK_BOX);
    }

    private void disableCheckBox(CheckBox checkBox) {
        checkBox.setEnabled(false);
        checkBox.addStyleName(STYLE_DISABLED_CHECK_BOX);
    }

    private void moveCheckBoxToTheBottomFromView(CheckBox checkBox){
        checkBoxVerticalPanel.remove(checkBox);
        checkBoxVerticalPanel.add(checkBox);
    }

    @Override
    public void unSelectAndReset() {
        for(CheckBox checkBox : checkBoxes){
            final boolean isChecked = false;
            checkBox.setValue(isChecked);
        }
        selectedListItems = new HashSet<String>();
        clearSelectionControl.setVisible(false);
    }

    private int getItemIndices(int mousePos, FocusPanel focusPanel) {
        //asif
        int visibleItemCount = this.getVisibleItemCount();
        if(visibleItemCount < 1) {
            GWT.log("", new IllegalStateException("Visible item count " + visibleItemCount));
            visibleItemCount = 1;
        }
        int indItemHeight = scrollpanel.getOffsetHeight()/ visibleItemCount;
        final int index = Math.round(mousePos / indItemHeight);
        return index;
    }

    private void setToolTipContentWithIndices(int currentIndices, FocusPanel listBox) {
        FacetWidgetItem facetWidgetItem = null;

        try {
//            GWT.log("index "+ (currentIndices-1));
            facetWidgetItem = facetWidgetItems.get(currentIndices);

        } catch (IndexOutOfBoundsException e) {
            //get last item at the end of list
            facetWidgetItem = facetWidgetItems.get(checkBoxes.size()-1);
        }

        toolTipPopup.setWidget(createTooltip(facetWidgetItem));
        toolTipPopup.setPopupPosition(listBox.getAbsoluteLeft() + 195, listBox.getAbsoluteTop() + ((listBox.getOffsetHeight() / checkBoxes.size()) * currentIndices) - 5);
    }

    private VerticalPanel createTooltip(FacetWidgetItem item) {
        final VerticalPanel tooltipVPanel = new VerticalPanel();
        final HTML htmlLabel = new HTML(item.getValue() + (item.getLabel() == null || item.getLabel().isEmpty() || item.getLabel().equals(item.getValue()) ? "" : " - " + item.getLabel()));
        tooltipVPanel.add(htmlLabel);
        return tooltipVPanel;
    }

    public String getFacetField() {
        return facetField;
    }

    public String getFacetTitle() {
        return facetTitle;
    }

    public String getFacetLabel() {
        return facetLabel;
    }

    public int getVisibleItemCount() {
        return visibleItemCount;
    }

    public FilterConfigVisualization getFilterConfigVisualization() {
        return filterConfigVisualization;
    }

    public Set<String> getSelectedListItems() {
        return selectedListItems;
    }

    public List<FacetWidgetItem> getFacetWidgetItems() {
        return facetWidgetItems;
    }

    public int calculateScrollPanelHeight() {
        return CHECKBOX_HEIGHT*visibleItemCount;
    }
}
