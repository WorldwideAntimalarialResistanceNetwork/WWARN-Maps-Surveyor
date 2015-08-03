package org.wwarn.surveyor.client.core;

import com.google.gwt.user.cellview.client.*;
import com.google.gwt.view.client.ListDataProvider;

import java.util.*;

/**
 * Created by nigelthomas on 03/08/2015.
 */
public class SortedCellTable<T> extends CellTable<T> {
    /**
     * To keep track of the currently sorted column
     */
    private Column<T, ?> currentlySortedColumn;
    /**
     * Tells us which way to sort a column initially
     */
    private Map<Column<T, ?>, Boolean> defaultSortOrderMap = new HashMap<Column<T, ?>, Boolean>();
    /**
     * Comparators associated with their columns
     */
    private Map<Column<T, ?>, Comparator<T>> comparators = new HashMap<Column<T, ?>, Comparator<T>>();
    /**
     * Column to sort when the data provider's list is refreshed using
     * {@link SortedCellTable#setList(List)}
     */
    private Column<T, ?> initialSortColumn;
    /**
     * Data provider we will attach to this table
     */
    private ListDataProvider<T> dataProvider;
    /**
     * Special column sorting handler that will allow us to do more controlled
     * sorting
     */
    ColumnSortEvent.ListHandler<T> columnSortHandler;

    public SortedCellTable() {
        super();
        dataProvider = new ListDataProvider<T>();
        dataProvider.addDataDisplay(this);
        columnSortHandler = new ColumnSortEvent.ListHandler<T>(dataProvider.getList()) {

            @Override
            public void onColumnSort(ColumnSortEvent event) {
                @SuppressWarnings("unchecked")
                Column<T, ?> column = (Column<T, ?>) event.getColumn();
                if (column == null) {
                    return;
                }

                if (column.equals(currentlySortedColumn)) {
                    // Default behavior
                    super.onColumnSort(event);
                } else {
                    // Initial sort; look up which direction we need
                    final Comparator<T> comparator = comparators.get(column);
                    if (comparator == null) {
                        return;
                    }

                    Boolean ascending = defaultSortOrderMap.get(column);
                    if (ascending == null || ascending) {
                        // Default behavior
                        super.onColumnSort(event);
                    } else {
                        // Sort the column descending
                        Collections.sort(getList(), new Comparator<T>() {
                            public int compare(T o1, T o2) {
                                return -comparator.compare(o1, o2);
                            }
                        });
                        // Set the proper arrow in the header
                        getColumnSortList().push(
                                new ColumnSortList.ColumnSortInfo(column, false));
                    }
                    currentlySortedColumn = column;
                }
            }

            @Override
            public void setComparator(Column<T, ?> column,
                                      Comparator<T> comparator) {
                comparators.put(column, comparator);
                super.setComparator(column, comparator);
            }

        };
        addColumnSortHandler(columnSortHandler);
    }

    /**
     * Adds a column to the table and sets its sortable state
     *
     * @param column
     * @param headerName
     * @param sortable
     */
    public void addColumn(Column<T, ?> column, String headerName,
                          boolean sortable) {
        addColumn(column, headerName);
        column.setSortable(sortable);
        if (sortable) {
            defaultSortOrderMap.put(column, true);
        }
    }

    /**
     * Adds a column to the table and sets its sortable state
     *
     * @param column
     * @param header
     * @param sortable
     */
    public void addColumn(Column<T, ?> column, Header<?> header,
                          boolean sortable) {
        addColumn(column, header);
        column.setSortable(sortable);
        if (sortable) {
            defaultSortOrderMap.put(column, true);
        }
    }

    /**
     * Sets the column to sort when the data list is reset using
     * {@link SortedCellTable#setList(List)}
     *
     * @param column
     */
    public void setInitialSortColumn(Column<T, ?> column) {
        initialSortColumn = column;
    }

    /**
     * Sets a comparator to use when sorting the given column
     *
     * @param column
     * @param comparator
     */
    public void setComparator(Column<T, ?> column, Comparator<T> comparator) {
        columnSortHandler.setComparator(column, comparator);
    }

    /**
     * Sets the sort order to use when this column is clicked and it was not
     * previously sorted
     *
     * @param column
     * @param ascending
     */
    public void setDefaultSortOrder(Column<T, ?> column, boolean ascending) {
        defaultSortOrderMap.put(column, ascending);
    }

    /**
     * Sets the table's data provider list and sorts the table based on the
     * column given in {@link SortedCellTable#setInitialSortColumn(Column)}
     *
     * @param list
     */
    public void setList(List<T> list) {
        dataProvider.getList().clear();
        if (list != null) {
            for (T t : list) {
                dataProvider.getList().add(t);
            }
        }

        // Do a first-time sort based on which column was set in
        // setInitialSortColumn()
        if (initialSortColumn != null) {
            Collections.sort(dataProvider.getList(), new Comparator<T>() {

                @Override
                public int compare(T o1, T o2) {
                    return (defaultSortOrderMap.get(initialSortColumn) ? 1 : -1)
                            * comparators.get(initialSortColumn)
                            .compare(o1, o2);
                }

            });
            // Might as well get the little arrow on the header to make it
            // official
            getColumnSortList().push(
                    new ColumnSortList.ColumnSortInfo(initialSortColumn, defaultSortOrderMap
                            .get(initialSortColumn)));
            currentlySortedColumn = initialSortColumn;
        }
    }
}