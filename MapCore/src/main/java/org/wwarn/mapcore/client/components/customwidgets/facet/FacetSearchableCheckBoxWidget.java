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
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;
import org.gwtbootstrap3.client.shared.event.ModalHideEvent;
import org.gwtbootstrap3.client.shared.event.ModalHideHandler;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.Button;
import org.jetbrains.annotations.NotNull;
import org.wwarn.mapcore.client.common.types.FilterConfigVisualization;
import org.wwarn.mapcore.client.panel.Tooltip;
import org.wwarn.mapcore.client.utils.StringUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Checkbox implementation of a filter widget
 */
public class FacetSearchableCheckBoxWidget extends Composite implements FacetWidget, KeyUpHandler {
    private static Logger logger = Logger.getLogger("MapCore.FacetSearchableCheckBoxWidget");

    public static final String STYLE_CHECKBOXLIST_ITEM_CHECKED = "searchCheckBoxListItemChecked";
    public static final String STYLE_CHECKBOXLIST_ITEM_DISABLED = "searchCheckBoxListItemDisabled";
    public static final String STYLE_INITIAL_SEARCHBOX_TEXT = "initialSearchBoxText";
    public static final String STYLE_SEARCH_BOX = "searchBox";
    public static final int DEFAULT_VISIBLE_ITEM_COUNT = 5;
    public static final String DEFAULT_SEARCH_TEXT = "Search...";
    private static final PopupPanel toolTipPopup = new PopupPanel(true);
    private static final Integer TOOL_TIP_POPUP_WIDTH = 250;
    private static FacetSearchableCheckBoxWidgetsUiBinder ourUiBinder = GWT.create(FacetSearchableCheckBoxWidgetsUiBinder.class);
    String facetTitle = null;
    //    public static final boolean isShown = true;
    String facetLabel = null;
    int visibleItemCount = 0;
    FilterConfigVisualization filterConfigVisualization;
    List<FacetWidgetItem> facetWidgetItems = new ArrayList<FacetWidgetItem>();
    VerticalPanel panel;
    List<CheckBox> checkBoxes = new ArrayList<CheckBox>();
    MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
    ValueChangeHandler valueChangeHandler;
    @UiField
    Anchor clearSelectionControl;
    @UiField
    TextBox searchBox;
    @UiField
    ScrollPanel scrollpanel;
    @UiField
    FlowPanel filterMainBody;
    @UiField
    SpanElement toggleFilterField;
    @UiField
    HTMLPanel heading;
    @UiField
    SpanElement headingValue;
    @UiField
    Anchor toggleFilter;
    @UiField
    FocusPanel focusPanel;
    @UiField
    VerticalPanel checkBoxContainer;
    @UiField
    Anchor resizeControl;
    /**
     * Once the user adds a text into the textBox, this method is responsible to update
     * the listBox with the suggestions.
     */
    private final SuggestOracle.Callback callback = new SuggestOracle.Callback() {
        public void onSuggestionsReady(SuggestOracle.Request request, SuggestOracle.Response response) {

            Collection<MultiWordSuggestOracle.MultiWordSuggestion> sugs = (Collection<MultiWordSuggestOracle.MultiWordSuggestion>) response.getSuggestions();

            List<FacetWidgetItem> suggestedList = new ArrayList<FacetWidgetItem>();
            for (FacetWidgetItem facetWidgetItem : facetWidgetItems) {
                for (MultiWordSuggestOracle.MultiWordSuggestion sug : sugs) {
                    if (facetWidgetItem.getValue().equalsIgnoreCase(sug.getReplacementString())) {
                        suggestedList.add(facetWidgetItem);
                    }
                }
            }
            //setupListPanel(suggestedList);
            updatePanelList(suggestedList);
        }
    };
    private boolean showHideToggleEnabled;
    private boolean defaultShowHideToggleStateIsVisible;
    private String facetField = null;
    private Set<String> selectedListItems = new HashSet<String>();
    private int lastKnownIndices = 0;

    public FacetSearchableCheckBoxWidget(FacetBuilder builder) {
        panel = ourUiBinder.createAndBindUi(this);
        this.facetWidgetItems = builder.getListItems();
        this.facetField = StringUtils.ifEmpty(builder.getFacetName(), builder.getFacetTitle());
        this.facetTitle = builder.getFacetTitle();
        this.facetLabel = builder.getFacetLabel();
        this.filterConfigVisualization = builder.getFilterConfigVisualization();
        this.visibleItemCount = (builder.getVisibleItemCount() < 1) ? DEFAULT_VISIBLE_ITEM_COUNT : builder.getVisibleItemCount();
        this.showHideToggleEnabled = builder.isShowHideToggleEnabled();
        this.defaultShowHideToggleStateIsVisible = builder.isDefaultShowHideToggleStateIsVisible();
        buildDisplay();
        setOracle();
        initWidget(panel);

    }

    public FacetWidget buildDisplay() {
        setupFilterElementToolTip();
        setupListPanel(facetWidgetItems);
        setupScrollPanel();
        setupSearchBox();
        buildHTMLHeader();
        return this;
    }

    protected void setupListPanel(List<FacetWidgetItem> facetWidgetItems) {
        checkBoxContainer.clear();
        for (FacetWidgetItem item : facetWidgetItems) {
            final CheckBox checkBox = new CheckBox(item.getValue());
            checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    check(checkBox);
                }
            });
            if (valueChangeHandler != null) {
                checkBox.addValueChangeHandler(valueChangeHandler);
            }

            //if the checkbox is in the selectedItems then check it
            for (String selectedItem : selectedListItems) {
                if (selectedItem.equals(item.getValue())) {
                    checkBox.setValue(true);
                }
            }

            checkBoxes.add(checkBox);
            checkBoxContainer.add(checkBox);
        }

    }

    private void setupScrollPanel() {
        int panelHeight = visibleItemCount * 20;
        scrollpanel.setHeight(panelHeight + "px");
    }

    private void setupSearchBox() {
        searchBox.addKeyUpHandler(this);
        searchBox.addStyleName(STYLE_SEARCH_BOX);
        searchBox.setText(DEFAULT_SEARCH_TEXT);
        searchBox.addStyleName(STYLE_INITIAL_SEARCHBOX_TEXT);
        searchBox.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent focusEvent) {
                if (DEFAULT_SEARCH_TEXT.equals(searchBox.getText())) {
                    searchBox.setText("");
                    searchBox.removeStyleName(STYLE_INITIAL_SEARCHBOX_TEXT);
                }
            }
        });
        searchBox.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent blurEvent) {
                if ("".equals(searchBox.getText())) {
                    searchBox.setText(DEFAULT_SEARCH_TEXT);
                    searchBox.addStyleName(STYLE_INITIAL_SEARCHBOX_TEXT);
                }
            }
        });
    }

    private void check(CheckBox checkBox) {
        if (checkBox.getValue()) {
            selectedListItems.add(checkBox.getText());
            checkBox.addStyleName(STYLE_CHECKBOXLIST_ITEM_CHECKED);
            clearSelectionControl.setVisible(true);
        } else {
            removeItem(checkBox.getText());
            checkBox.removeStyleName(STYLE_CHECKBOXLIST_ITEM_CHECKED);
            if (selectedListItems.size() == 0) {
                clearSelectionControl.setVisible(false);
            }
        }
    }

    private void removeItem(String item) {
        selectedListItems.remove(item);
    }

    public Widget buildHTMLHeader() {
//        HTMLPanel heading = new HTMLPanel("<br/><strong>"+ facetTitle +"</strong>");
        headingValue.setNodeValue(facetTitle);
        updateSpanNode(headingValue, facetTitle);
        panel.getElement().setId(createID(facetTitle));
        setupFilterToggle();
        setupHeatherTooltip();
        return heading;
    }

    private void setupHeatherTooltip() {
        if(isEmptyFacetHeaderTitleOrLabel()){return;}
        buildHeaderToolTip().attachTo(panel);
    }

    private boolean isEmptyFacetHeaderTitleOrLabel() {
        return StringUtils.isEmpty(this.facetLabel) || StringUtils.isEmpty(facetTitle) || facetLabel.equals(facetField);
    }

    private Tooltip buildHeaderToolTip() {
        Tooltip mftt = new Tooltip();
        mftt.setWidth(calculateAvailableWidth());
        mftt.setPosition(Tooltip.TooltipPosition.RIGHT_TOP);
        mftt.setHtml(this.buildHTMLLabel());
        return mftt;
    }

    @NotNull
    private String calculateAvailableWidth() {
        int fontCount = this.facetTitle.length() + this.facetLabel.length();
        int fontWidthEstimate = fontCount * 3;
        int clientWidth = Window.getClientWidth();
        if(clientWidth > 500){
            clientWidth = (int) (clientWidth * 0.6);
            clientWidth = Math.min(fontWidthEstimate, clientWidth);
        }else clientWidth =  Math.min(TOOL_TIP_POPUP_WIDTH, clientWidth);
        return (clientWidth) + "px";
    }

    public Widget buildHTMLLabel() {
        return (new HeaderPopup(this.facetTitle, this.facetLabel));
    }

    private void setupFilterToggle() {
        toggleFilter.setVisible(showHideToggleEnabled);
        if (showHideToggleEnabled) {
            filterMainBody.setVisible(defaultShowHideToggleStateIsVisible);
            updateToggleFilterView();
        }

    }

    private void updateSpanNode(SpanElement spanElement, String value) {
        spanElement.getFirstChild().setNodeValue(value);
    }

    /**
     * ID and NAME tokens must begin with a letter ([A-Za-z]) and may be followed by any number of letters, digits ([0-9]), hyphens ("-"), underscores ("_"), colons (":"), and periods (".").
     *
     * @param facetTitleNormalised
     * @return
     */
    private String createID(String facetTitleNormalised) {
        facetTitleNormalised = facetTitleNormalised.replaceAll("\\W", "_"); //replace non word characters ie [a-zA-Z_0-9]
        facetTitleNormalised = facetTitleNormalised.replaceAll("\\-", "_"); //replace non word characters ie [a-zA-Z_0-9]
        facetTitleNormalised = facetTitleNormalised.replaceAll("\\s", "_"); //replace non word characters ie [a-zA-Z_0-9]
        facetTitleNormalised = facetTitleNormalised.toLowerCase(); //replace non word characters ie [a-zA-Z_0-9]
        return "facetID" + facetTitleNormalised;
    }

    public void onKeyUp(KeyUpEvent keyUpEvent) {
        String text = searchBox.getText();

        if (text.isEmpty()) {
            updatePanelList(facetWidgetItems);
        } else {
            oracle.requestSuggestions(new SuggestOracle.Request(text, 20), callback);
        }
    }

    private void updatePanelList(List<FacetWidgetItem> facetWidgetItems) {
        checkBoxContainer.clear();
        for (CheckBox checkBox : checkBoxes) {
            for (FacetWidgetItem facetWidgetItem : facetWidgetItems) {
                if (facetWidgetItem.getValue().equals(checkBox.getText())) {
                    checkBoxContainer.add(checkBox);
                }
            }
        }
    }

    private void createDialogBox() {
        // Create a dialog box and set the caption text
        final ZoomIntoFacetsModal dialogBox = new ZoomIntoFacetsModal();
        dialogBox.setContent(this);
        dialogBox.setTitle(this.getFacetTitle());
        dialogBox.show();

    }

    @UiHandler("clearSelectionControl")
    public void clearSelectionHandleClick(ClickEvent event) {
        event.preventDefault();
        unSelectAndReset();
        //TODO: resolve this hack
        // fire value change event inform client that reset was completed
        ValueChangeEvent.fire(checkBoxes.iterator().next(), true);
    }

    @UiHandler("toggleFilter")
    public void handleClick(ClickEvent event) {
        event.preventDefault();
        filterMainBody.setVisible(!filterMainBody.isVisible());
        updateToggleFilterView();
    }

    @UiHandler("resizeControl")
    public void resizeControlClick(ClickEvent event) {
        createDialogBox();
    }

    @NotNull
    private String getToggleFilterValue() {
        return (filterMainBody.isVisible()) ? "Hide" : "Show";
    }


    private void updateToggleFilterView() {
//        toggleFilter.setHTML(getToggleFilterValue());
        toggleFilter.setTitle(getTitleForToggle());
        toggleFilterField.setTitle(getTitleForToggle());
        if (filterMainBody.isVisible()) {
            toggleFilterField.removeClassName("glyphicon-plus");
            toggleFilterField.addClassName("glyphicon-minus");
        } else {
            toggleFilterField.removeClassName("glyphicon-minus");
            toggleFilterField.addClassName("glyphicon-plus");
        }
    }

    @NotNull
    private String getTitleForToggle() {
        return getToggleFilterValue() + " "+ facetTitle + " filter";
    }

    /**
     * Initially set the oracle with all the item in the listBox
     * We will use the oracle to get suggestions from a text input
     */
    private void setOracle() {
        for (FacetWidgetItem facetWidgetItem : facetWidgetItems) {
            oracle.add(facetWidgetItem.getValue());
        }
    }

    //TODO: This is an eyesore, Natxo needs to clean this
    @Override
    public HandlerRegistration addChangeHandler(final ChangeHandler changeHandler) {
        this.valueChangeHandler = new ValueChangeHandler() {
            @Override
            public void onValueChange(ValueChangeEvent valueChangeEvent) {
                changeHandler.onChange(null);
            }
        };
        for (CheckBox checkBox : checkBoxes) {
            checkBox.addValueChangeHandler(valueChangeHandler);
        }
        return null;
    }

    @Override
    public void unSelectAndReset() {
        for (CheckBox checkBox : checkBoxes) {
            clearCheckBox(checkBox);
        }
        selectedListItems = new HashSet<String>();
        clearSelectionControl.setVisible(false);
        setupSearchBox();
        updatePanelList(facetWidgetItems);
    }

    private void clearCheckBox(CheckBox checkBox) {
        checkBox.setValue(false);
        checkBox.setEnabled(true);
        checkBox.removeStyleName(STYLE_CHECKBOXLIST_ITEM_CHECKED);
        checkBox.removeStyleName(STYLE_CHECKBOXLIST_ITEM_DISABLED);
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

    public void setFacetWidgetItems(List<FacetWidgetItem> facetWidgetItems) {
        this.facetWidgetItems = facetWidgetItems;
        setOracle();
    }

    private void setToolTipContentWithIndices(FacetWidgetItem facetWidgetItem, FocusPanel focusPanel, int currentIndices) {
        toolTipPopup.setWidget(createTooltip(facetWidgetItem));
        toolTipPopup.setPopupPosition(focusPanel.getAbsoluteLeft() + 195, focusPanel.getAbsoluteTop() + ((focusPanel.getOffsetHeight() / checkBoxes.size()) * currentIndices) - 5);
    }

    private FacetWidgetItem getFacetWidgetItemFromIndex(int currentIndices) {
        FacetWidgetItem facetWidgetItem;
        try {
            if(logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "index " + (currentIndices - 1));
            }
            facetWidgetItem = facetWidgetItems.get(currentIndices);

        } catch (IndexOutOfBoundsException e) {
            //get last item at the end of list
            facetWidgetItem = facetWidgetItems.get(checkBoxes.size()-1);
        }
        return facetWidgetItem;
    }

    private VerticalPanel createTooltip(FacetWidgetItem item) {
        final VerticalPanel tooltipVPanel = new VerticalPanel();
        final HTML htmlLabel = new HTML(item.getValue() + (isFacetLabelEmpty(item) ? "" : " - " + item.getLabel()));
        tooltipVPanel.add(htmlLabel);
        return tooltipVPanel;
    }

    private int getItemIndices(int mousePos, FocusPanel focusPanel) {
        //asif
        int visibleItemCount = this.getVisibleItemCount();
        if(visibleItemCount < 1) {
            logger.log(Level.SEVERE,"", new IllegalStateException("Visible item count " + visibleItemCount));
            visibleItemCount = 1;
        }
        int offset = 80;
        int indItemHeight = (scrollpanel.getOffsetHeight()+offset)/ visibleItemCount;
        final int index = Math.round(mousePos / indItemHeight);
        return index;
    }

    private void setupFilterElementToolTip() {

        toolTipPopup.getElement().setAttribute("style","z-index:100;");
        toolTipPopup.setWidth(calculateAvailableWidth());
        focusPanel.addMouseMoveHandler(new MouseMoveHandler() {
            @Override
            public void onMouseMove(MouseMoveEvent mouseMoveEvent) {
                int currentIndices = getItemIndices(mouseMoveEvent.getY(), focusPanel);
                if (currentIndices != lastKnownIndices) {
                    toolTipPopup.hide();
                    final FacetWidgetItem item = getFacetWidgetItemFromIndex(currentIndices);
                    if(isFacetLabelEmpty(item)){
                        return;
                    }
                    setToolTipContentWithIndices(item, focusPanel, currentIndices);
                    toolTipPopup.show();
                    lastKnownIndices = currentIndices;
                }
            }
        });

        focusPanel.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent mouseOverEvent) {
                final int currentIndices = getItemIndices(mouseOverEvent.getY(), focusPanel);
                final FacetWidgetItem item = getFacetWidgetItemFromIndex(currentIndices);
                if(isFacetLabelEmpty(item)){
                    return;
                }
                setToolTipContentWithIndices(item, focusPanel, currentIndices);
                toolTipPopup.show();
            }
        });

        focusPanel.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent mouseOutEvent) {
                toolTipPopup.hide();
            }
        });
    }

    private boolean isFacetLabelEmpty(FacetWidgetItem item) {
        return item.getLabel() == null || item.getLabel().isEmpty() || item.getLabel().equals(item.getValue());
    }

    @Override
    public void disableItems(List<FacetWidgetItem> facetWidgetItems) {

        for (CheckBox checkBox : checkBoxes) {
            boolean checkBoxChanged = false;
            for (FacetWidgetItem item : facetWidgetItems) {
                if (checkBox.getText().equals(item.getValue())) {
                    checkBox.setEnabled(false);
                    moveCheckBoxToTheBottom(checkBox);
                    checkBoxChanged = true;
                    checkBox.setStyleName(STYLE_CHECKBOXLIST_ITEM_DISABLED);
                    break;
                }
            }
            if (!checkBoxChanged) {
                checkBox.setEnabled(true);
                checkBox.removeStyleName(STYLE_CHECKBOXLIST_ITEM_DISABLED);
            }
        }
    }

    @Override
    public void selectItems(List<FacetWidgetItem> facetWidgetItems) {

        for (CheckBox checkBox : checkBoxes) {
            for (FacetWidgetItem item : facetWidgetItems) {
                if (checkBox.getText().equals(item.getValue())) {
                    checkBox.setValue(true, true);
                    check(checkBox);
                }
            }
        }
    }

    private void moveCheckBoxToTheBottom(CheckBox checkBox) {
        checkBoxContainer.remove(checkBox);
        checkBoxContainer.add(checkBox);
    }

    interface FacetSearchableCheckBoxWidgetsUiBinder extends UiBinder<VerticalPanel, FacetSearchableCheckBoxWidget> {

    }

    static class HeaderPopup extends Composite{
        private static FacetLabelTemplateBinder facetLabelTemplateBinder = GWT.create(FacetLabelTemplateBinder.class);
        @UiField
        SpanElement facetLabel;
        @UiField
        Element facetTitle;
        public HeaderPopup(String facetTitle, String facetLabel) {
            final Widget widget = facetLabelTemplateBinder.createAndBindUi(this);
            initWidget(widget);
            this.facetTitle.setInnerSafeHtml(SafeHtmlUtils.fromString(facetTitle));
            this.facetLabel.setInnerSafeHtml(SafeHtmlUtils.fromString(facetLabel));
        }

        @UiTemplate("FacetLabelTemplate.ui.xml")
        interface FacetLabelTemplateBinder extends UiBinder<Widget, HeaderPopup>{}
    }

    static class ZoomIntoFacetsModal extends Composite{
        private static DialogBoxTemplateBinder dialogBoxTemplateBinder = GWT.create(DialogBoxTemplateBinder.class);
        private VerticalPanel oldParent;

        @UiField
        Modal modal;

        @UiField
        VerticalPanel modalBody;
        @UiField
        Button closeModal;
        private int beforeIndexPosition;

        public ZoomIntoFacetsModal() {
            final Widget widget = dialogBoxTemplateBinder.createAndBindUi(this);
            initWidget(widget);

            modal.addHideHandler(new ModalHideHandler() {
                @Override
                public void onHide(ModalHideEvent modalHideEvent) {
                    closeModalClick(modalHideEvent);
                }
            });
        }

        public void setTitle(String title){
            modal.setTitle(title);
        }

        public void setContent(Widget widget){
            this.oldParent = (VerticalPanel) widget.getParent();
            this.beforeIndexPosition = ((VerticalPanel) widget.getParent()).getWidgetIndex(widget);
            modalBody.add(widget);
        }

        public void show(){
            modal.show();
            final Widget modalBodyWidget = modalBody.getWidget(0);
//            modalBodyWidget.setWidth("400px");

        }

        public void hide(){
            modal.hide();
        }

        public void closeModalClick(ModalHideEvent event) {
            final Widget modalBodyWidget = modalBody.getWidget(0);
            (oldParent).insert(modalBodyWidget, beforeIndexPosition);
        }

        @UiTemplate("FacetDialogBoxTemplate.ui.xml")
        interface DialogBoxTemplateBinder extends UiBinder<Widget, ZoomIntoFacetsModal>{}
    }
}
