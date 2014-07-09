package com.example.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.wwarn.surveyor.client.mvp.view.MainPanelView;

/**
 * Panel for surveyor, uses UIBinder to make distinction between layout and style clearer
 * User: nigel
 * Date: 30/07/13
 * Time: 10:13
 */
public class MainContentPanel extends Composite implements MainPanelView {
    interface MainPanelUiBinder extends UiBinder<Widget, MainContentPanel> {
    }

    @UiField(provided = true)
    VerticalPanel filterContainerPanel = new VerticalPanel();

    @UiField(provided = true)
    VerticalPanel resultsContainerPanel = new VerticalPanel();

    private static MainPanelUiBinder uiBinder = GWT.create(MainPanelUiBinder.class);

    public MainContentPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        RootPanel.get("mainContent").add(this);
    }

    /**
     * Get filter panel this is used in controller to setup filters
     * @return
     */
    public VerticalPanel getFilterContainerPanel() {
        return filterContainerPanel;
    }

    public VerticalPanel getResultsContainerPanel() {
        return resultsContainerPanel;
    }

}