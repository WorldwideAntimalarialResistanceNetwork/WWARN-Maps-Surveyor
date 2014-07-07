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
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.*;

import org.wwarn.mapcore.client.common.types.FilterConfigVisualization;
import org.wwarn.mapcore.client.panel.Tooltip;
import org.wwarn.mapcore.client.utils.*;

import java.util.*;

/**
 * A widget to display a facet, this is essentially a wrapper around a standard list box.
 * Labels can be set for a facet and each individual item in the facet.
 * Tooltips are the default display option for labels.
 */
public class FacetListBoxWidget extends Composite implements HasChangeHandlers, FacetWidget {
    public static final String STYLE_FACET_LISTBOX= "facetListBox";
    public static final String DEFAULT_ALL_VALUE = "all";
    private String facetField = null;
    String facetTitle = null;
    String facetLabel = null;
    boolean isMultiSelect = true;
    VerticalPanel panel = new VerticalPanel();
    ListBox listBox = new ListBox(true); // default generate a multi list box
    int visibleItemCount = 0;
    FilterConfigVisualization filterConfigVisualization;

    private static final boolean MULTI_SELECT = true;
    private static final String TOOL_TIP_POPUP_WIDTH = "190px";
    public static final String TOOLTIP_WIDTH = "265";
    private static final PopupPanel toolTipPopup = new PopupPanel(true);
    private int lastKnownIndices = 0;
    private List<FacetWidgetItem> facetWidgetItems;
    public static final int DEFAULT_VISIBLE_ITEM_COUNT = 5;



    @Deprecated
    public FacetListBoxWidget(String facetField, String facetTitle, String facetLabel) {
        this.facetTitle = facetTitle;
        this.facetLabel = facetLabel;
        this.isMultiSelect = MULTI_SELECT;

        this.facetField = ifEmptyUseDefault(facetField, facetTitle);
        initWidget(this.panel);
        this.listBox.setVisibleItemCount(DEFAULT_VISIBLE_ITEM_COUNT);
    }

    public FacetListBoxWidget(FacetBuilder builder){
        this.facetField = ifEmptyUseDefault(builder.getFacetName(), builder.getFacetTitle());
        this.facetTitle = builder.getFacetTitle();
        this.facetLabel = builder.getFacetLabel();
        this.isMultiSelect = MULTI_SELECT;
        this.filterConfigVisualization = builder.getFilterConfigVisualization();

        this.addAll(builder.getListItems());
        this.buildDisplay();
        this.unSelectAndReset();

        initWidget(this.panel);

        this.visibleItemCount = (builder.getVisibleItemCount() < 1)?DEFAULT_VISIBLE_ITEM_COUNT:builder.getVisibleItemCount();
        this.listBox.setVisibleItemCount(this.visibleItemCount);

    }

    /*public FacetListBoxWidget(String facetField, String facetTitle, String facetLabel, int visibleIntColumn) {
        this(facetField,facetTitle,facetLabel);
        this.listBox.setVisibleItemCount(visibleIntColumn);
    }*/

    private String ifEmptyUseDefault(String firstValue, String defaultValue) {
        return StringUtils.ifEmpty(firstValue, defaultValue);
    }

    public FacetWidget buildDisplay() {
        HTML heading = buildHTMLHeader();
        buildHeaderToolTip().attachTo(heading);
        listBox.addStyleName(STYLE_FACET_LISTBOX);
        panel.add(heading);
        panel.add(this.listBox);
        return this;
    }

    private Tooltip buildHeaderToolTip() {
        Tooltip mftt = new Tooltip();
        mftt.setWidth(TOOLTIP_WIDTH);
        mftt.setPosition(Tooltip.TooltipPosition.RIGHT_TOP);
        mftt.setHtml(this.buildHTMLLabel());
        return mftt;
    }

    public HTML buildHTMLHeader() {
        HTML heading = new HTML("<br/><strong>"+ facetTitle +"</strong>");
        String headingStyleName = "subHeading";
        heading.setStyleName(headingStyleName);
        return heading;
    }

    //TODO get rid of the ancient HTML markup
    public SafeHtml buildHTMLLabel() {
        return new SafeHtmlBuilder().appendHtmlConstant("<Font Size=2><b>"+ facetTitle +": </b> " +
                this.facetLabel +
                "</Font>").toSafeHtml();
    }

    public String getFacetField() {
        return facetField;
    }



    public List<FacetWidgetItem> getFacetWidgetItems(){return facetWidgetItems;}

    public void disableItems(List<FacetWidgetItem> facetWidgetItems){
        List<FacetWidgetItem> hiddenFacetWidgetItems = new ArrayList<FacetWidgetItem>();
        final List<FacetWidgetItem> facetWidgetItemListSort = new ArrayList<FacetWidgetItem>();
        for (FacetWidgetItem facetWidgetItem : getFacetWidgetItems()) {
            final String value = facetWidgetItem.getValue();
            if(value.toLowerCase().equals(DEFAULT_ALL_VALUE)){
                facetWidgetItemListSort.add(facetWidgetItem);
                continue;
            }
            if(facetWidgetItems.contains(new FacetWidgetItem(value, null))){
                hiddenFacetWidgetItems.add(facetWidgetItem);
            }else{
                facetWidgetItemListSort.add(facetWidgetItem);
            }
        }
        Collections.sort(hiddenFacetWidgetItems);
        facetWidgetItemListSort.addAll(hiddenFacetWidgetItems);
        addAll(facetWidgetItemListSort);
        for (int i = 0; i < listBox.getItemCount(); i++) {
            final String value = listBox.getValue(i);
            if(value.toLowerCase().equals(DEFAULT_ALL_VALUE)){continue;}
            if(hiddenFacetWidgetItems.contains(new FacetWidgetItem(value, null))){
                listBox.getElement().getElementsByTagName("option").getItem(i).setAttribute("disabled", "disabled");
            }else{
                listBox.getElement().getElementsByTagName("option").getItem(i).removeAttribute("disabled");
            }
        }
        //select all after items disabled
        listBox.setItemSelected(0,true);
    }

    public void addAll(List<FacetWidgetItem> facetWidgetItems){
        // handle the case where facetWidgetItems is empty or null
        listBox.clear();

        for (FacetWidgetItem facetWidgetItem : facetWidgetItems) {
            listBox.addItem(facetWidgetItem.getValue());
        }

        this.facetWidgetItems = facetWidgetItems;

        listBox.setName(this.facetTitle);

        toolTipPopup.setWidth(TOOL_TIP_POPUP_WIDTH);

        listBox.addMouseMoveHandler(new MouseMoveHandler() {
            @Override
            public void onMouseMove(MouseMoveEvent mouseMoveEvent) {
                int currentIndices = getItemIndices(mouseMoveEvent.getY(), listBox);
                if (currentIndices != lastKnownIndices) {
                    toolTipPopup.hide();
                    setToolTipContentWithIndices(currentIndices, listBox);
                    toolTipPopup.show();
                    lastKnownIndices = currentIndices;
                }
            }
        });

        listBox.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent mouseOverEvent) {
                final int currentIndices = getItemIndices(mouseOverEvent.getY(), listBox);
                setToolTipContentWithIndices(currentIndices, listBox);
                toolTipPopup.show();
            }
        });

        listBox.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent mouseOutEvent) {
                toolTipPopup.hide();
            }
        });
    }

    private void setToolTipContentWithIndices(int currentIndices, ListBox listBox) {
        String name =  listBox.getName();
        FacetWidgetItem facetWidgetItem = null;
        try {
//            GWT.log("index "+ (currentIndices-1));
            facetWidgetItem = facetWidgetItems.get(currentIndices);
        } catch (IndexOutOfBoundsException e) {
            //get last item at the end of list
            facetWidgetItem = facetWidgetItems.get(listBox.getItemCount()-1);
        }

        toolTipPopup.setWidget(createTooltip(facetWidgetItem));
        toolTipPopup.setPopupPosition(listBox.getAbsoluteLeft() + 195, listBox.getAbsoluteTop() + ((listBox.getOffsetHeight() / listBox.getItemCount()) * currentIndices) - 5);
    }

    private int getItemIndices(int mousePos, ListBox listBox) {
        int visibleItemCount = listBox.getVisibleItemCount();
        if(visibleItemCount < 1) {
            GWT.log("", new IllegalStateException("Visible item count " + visibleItemCount));
            visibleItemCount = 1;
        }


        int indItemHeight = listBox.getOffsetHeight()/ visibleItemCount;
        return Math.round(mousePos / indItemHeight);
    }

    private VerticalPanel createTooltip(FacetWidgetItem item) {
        final VerticalPanel tooltipVPanel = new VerticalPanel();
        final HTML htmlLabel = new HTML(item.getValue() + (item.getLabel() == null || item.getLabel().isEmpty() || item.getLabel().equals(item.getValue()) ? "" : " - " + item.getLabel()));
        tooltipVPanel.add(htmlLabel);
        return tooltipVPanel;
    }

    public void unSelectAndReset(){
        for(int i = 0; i < listBox.getItemCount(); i++) {
            listBox.getElement().getElementsByTagName("option").getItem(i).removeAttribute("disabled");
            if(i==0) {
                listBox.setItemSelected(i,true);
            } else {
                listBox.setItemSelected(i,false);
            }
        }
    }

    /**
     * A method to check if facet can be reset, ie if index has been changed from default
     * @return
     */
    public boolean canReset(){
        return listBox.getSelectedIndex() > 0;
    }

    @Override
    public HandlerRegistration addChangeHandler(ChangeHandler changeHandler) {
        return listBox.addChangeHandler(changeHandler);
    }

    @Override
    public void fireEvent(GwtEvent<?> gwtEvent) {
        listBox.fireEvent(gwtEvent);
    }

    public Set<String> getSelectedListItems() {
        Set<String> selectedItems  =  new HashSet<String>();
        for (int i = 0; i < listBox.getItemCount(); i++)
        {
            if (listBox.isItemSelected(i))
                selectedItems.add(listBox.getItemText(i));
        }
        return selectedItems;
    }

    // Given a string will find an item in the list box with matching text and select it.
    public void setSelected(String s)
    {
        for (int i = 0; i < listBox.getItemCount(); i++)
        {
            if (listBox.getItemText(i).equals(s))
                listBox.setSelectedIndex(i);
        }
    }

    public FilterConfigVisualization getFilterConfigVisualization() {
        return filterConfigVisualization;
    }

    @Override
    public String getFacetTitle() {
        return facetTitle;
    }

    @Override
    public String getFacetLabel() {
        return facetLabel;
    }

    @Override
    public int getVisibleItemCount() {
        return visibleItemCount;
    }



}
