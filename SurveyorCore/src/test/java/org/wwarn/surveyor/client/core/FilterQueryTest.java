package org.wwarn.surveyor.client.core;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by suay on 1/21/15.
 */
public class FilterQueryTest {

    @Test
    public void testEqualsFilterQueries() throws Exception {
        FilterQuery filterQuery = new FilterQuery();
        Map<String, FilterQuery.FilterQueryElement> filterQueryElementMap = new HashMap<>();
        filterQueryElementMap.put("field", new FilterQuery.FilterFieldValue("field", new HashSet<String>(Arrays.asList("myValue"))));
        filterQuery.filterQueries = filterQueryElementMap;

        FilterQuery filterQuery2 = new FilterQuery();
        Map<String, FilterQuery.FilterQueryElement> filterQueryElementMap2 = new HashMap<>();
        filterQueryElementMap2.put("field", new FilterQuery.FilterFieldValue("field", new HashSet<String>(Arrays.asList("myValue"))));
        filterQuery2.filterQueries = filterQueryElementMap2;

        assertEquals(filterQuery, filterQuery2);

    }
}
