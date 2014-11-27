package org.wwarn.surveyor.client.core;

import org.junit.Test;
import org.wwarn.surveyor.client.model.DataSourceProvider;

import static org.junit.Assert.*;

public class GwtTestClientSideSearchDataProvider extends GwtTestServerSideSearchDataProvider{
    private static DataProvider dataProvider;
    private final DataProviderTestUtility dataProviderTestUtility = new DataProviderTestUtility();

    public GwtTestClientSideSearchDataProvider() {
        super(new DataProviderTestUtility.DataProviderSource() {
            @Override
            public DataProvider getDataProvider() {
                GenericDataSource dataSource = GwtTestClientSideSearchDataProvider.getGenericDataSource();
                String[] selectorList = testUtility.getSelectorList();
                return new ClientSideSearchDataProvider(dataSource, testUtility.fetchSampleDataSchema(), selectorList);
            }
        });
    }

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        DataSchema schema = dataProviderTestUtility.fetchSampleDataSchema();
        String[] facetFieldList = dataProviderTestUtility.getSelectorList();
        GenericDataSource dataSource = getGenericDataSource();
        dataProvider = new ClientSideSearchDataProvider(dataSource, schema, facetFieldList);
    }

    private static GenericDataSource getGenericDataSource() {
        return new GenericDataSource(null, Constants.JSON_DATA_SOURCE, GenericDataSource.DataSourceType.JSONPropertyList, DataSourceProvider.ClientSideSeachDataProvider);
    }

    @Test
    public void testOnLoad() throws Exception {
        delayTestFinish(10*1000);
        dataProvider.onLoad(new Runnable() {
            @Override
            public void run() {
                finishTest();
            }
        });

    }
}