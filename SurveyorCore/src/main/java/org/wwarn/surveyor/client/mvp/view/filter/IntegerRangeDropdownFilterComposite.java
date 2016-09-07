package org.wwarn.surveyor.client.mvp.view.filter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import org.wwarn.mapcore.client.components.customwidgets.YuiSingleSliderGwtWidgetWithLabelMarkersImpl;
import org.wwarn.surveyor.client.event.FilterChangedEvent;
import org.wwarn.surveyor.client.model.FilterConfig;
import org.wwarn.surveyor.client.model.FilterSetting;
import org.wwarn.surveyor.client.mvp.ClientFactory;
import org.wwarn.surveyor.client.mvp.SimpleClientFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steven on 05/09/16.
 * This Composite is used to create a panel with a ListBox
 * that will hold a series of integer ranges to filter all records with
 */
public class IntegerRangeDropdownFilterComposite extends Composite{

    private ClientFactory clientFactory = SimpleClientFactory.getInstance();
    public static ArrayList<String> enroledNumbers;
    public static String increment, end;
    private String fieldName;
    private String initialValue, maxSampleSize, minSampleSize;
    private FilterChangedEvent filterChangedEvent;
    private FilterConfig.FilterByIntegerRangeSettings filterByIntegerRangeSettings;

    interface IntegerRangeDropdownFilterCompositeUiBinder extends UiBinder<Widget, IntegerRangeDropdownFilterComposite> {}

    private static IntegerRangeDropdownFilterCompositeUiBinder uiBinder = GWT.create(IntegerRangeDropdownFilterCompositeUiBinder.class);

    @UiField(provided = true)
    FlowPanel integerRangeDropdownFilterPanel = new FlowPanel();

    @UiField
    ListBox integerRangeDropdownListBox = new ListBox();

    @UiField
    Label integerRangeDropdownLabel = new Label();

    @UiConstructor
    public IntegerRangeDropdownFilterComposite (String fieldName, String labelText){
        this.initWidget(uiBinder.createAndBindUi(this));

        filterChangedEvent = new FilterChangedEvent(fieldName);

        FilterConfig filterConfig = clientFactory.getApplicationContext().getConfig(FilterConfig.class);
        List<FilterSetting> filterSettings = filterConfig.getFilterConfigBy(fieldName);
        FilterSetting filterSetting = filterSettings.get(0);
        filterByIntegerRangeSettings = (FilterConfig.FilterByIntegerRangeSettings) filterSetting;

        this.increment = Integer.toString(filterByIntegerRangeSettings.getIncrement());
        this.minSampleSize = Integer.toString(filterByIntegerRangeSettings.getInitialValue());
        this.maxSampleSize = Integer.toString(filterByIntegerRangeSettings.getEnd());

        this.setupConfig(fieldName);
    }

    private void setupConfig(String fieldName){
        setupRange(fieldName);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        integerRangeDropdownFilterPanel.add((Widget) integerRangeDropdownListBox);
    }

    public void resetIntegerRangeDropdownFilter() {
        filterChangedEvent.resetField();
    }

    @UiHandler("integerRangeDropdownListBox")
    public void clickEnroled(ChangeEvent changeEvent){
        String selectedItem = integerRangeDropdownListBox.getSelectedItemText();
        minSampleSize = selectedItem.substring(0,(selectedItem.indexOf('-'))).trim();
        maxSampleSize = selectedItem.substring((selectedItem.indexOf('-')+1)).trim();
        resetIntegerRangeDropdownFilter();
        filterChangedEvent.addFilter(Integer.parseInt(minSampleSize), Integer.parseInt(maxSampleSize));
        clientFactory.getEventBus().fireEvent(filterChangedEvent);
    }

    private void setupRange(String fieldName){
        this.fieldName = fieldName;
        enroledNumbers=calculateRangeFromParameters();
        integerRangeDropdownListBox.addItem("Select a Range");
        for(String fieldValues : enroledNumbers){
            integerRangeDropdownListBox.addItem(fieldValues);
        }
    }

    /**
     * Calculates the ranges that the developer implemented in config.xml and
     * in MainContentPanel.ui.xml and puts them in an ArrayList that will be used
     * in setupRange(String fieldName) to fill the values of the ListBox
     */
    private ArrayList<String> calculateRangeFromParameters(){
        ArrayList<String>allTheValues = new ArrayList<String>();
        int minSampleSizeInteger = Integer.parseInt(minSampleSize);
        int maxSampleSizeInteger = Integer.parseInt(maxSampleSize);
        int incrementInteger = Integer.parseInt(increment);
        for(int i=minSampleSizeInteger; i<maxSampleSizeInteger; i=i+incrementInteger){
            String value = Integer.toString(i) + " - " + Integer.toString(i+incrementInteger);
            allTheValues.add(value);
        }
        return allTheValues;
    }
}
