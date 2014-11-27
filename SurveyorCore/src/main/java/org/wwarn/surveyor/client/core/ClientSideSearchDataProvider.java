package org.wwarn.surveyor.client.core;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.wwarn.surveyor.client.model.DataSourceProvider;

/**
 *
 */
public class ClientSideSearchDataProvider extends ServerSideSearchDataProvider implements DataProvider{
    public ClientSideSearchDataProvider(GenericDataSource dataSource, DataSchema dataSchema, String[] fieldList) {
        super(dataSource, dataSchema, fieldList);
        if(dataSource.getDataSourceProvider()!= DataSourceProvider.ClientSideSeachDataProvider){
            throw new IllegalArgumentException("Expected data source provider client side data provider");
        }
    }

    @Override
    public void onLoad(final Runnable callOnLoad) {
        try {
            InitialFilterQuery initialFilterQuery = getInitialFilterQuery();
            clientFactory.setLastFilterQuery(initialFilterQuery.getInitialFilterQuery());

            searchServiceAsync.preFetchData(schema, this.dataSource, this.facetFieldList, initialFilterQuery.getInitialFilterQuery(), new AsyncCallback<QueryResult>() {
                @Override
                public void onFailure(Throwable throwable) {
                    throw new IllegalStateException(throwable);
                }

                @Override
                public void onSuccess(QueryResult queryResult) {
                    clientFactory.setLastQueryResult(queryResult);
                    callOnLoad.run();
                }
            });
        } catch (SearchException e) {
            throw new IllegalStateException(e);
        }

    }

    @Override
    public void query(FilterQuery filterQuery, String[] facetFields, AsyncCallback<QueryResult> queryResultCallBack) throws SearchException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void query(FilterQuery filterQuery, AsyncCallback<QueryResult> queryResultCallBack) throws SearchException {
        throw new UnsupportedOperationException();
    }
}
