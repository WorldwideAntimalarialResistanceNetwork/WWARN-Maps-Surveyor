package org.wwarn.surveyor.client.mvp.view.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Created by nigelthomas on 22/05/2017.
 */
public class FeedbackWidgetComposite extends Composite {
    interface FeedbackWidgetCompositeUiBinder extends UiBinder<FlowPanel, FeedbackWidgetComposite> {
    }

    private static FeedbackWidgetCompositeUiBinder ourUiBinder = GWT.create(FeedbackWidgetCompositeUiBinder.class);
    @UiField
    AnchorElement link;

    /**
     * Create a feedback button which hovers horizontally to the page
     * @param url
     */
    public @UiConstructor FeedbackWidgetComposite(String url) {
        initWidget(ourUiBinder.createAndBindUi(this));
        link.setHref(url);
    }
}