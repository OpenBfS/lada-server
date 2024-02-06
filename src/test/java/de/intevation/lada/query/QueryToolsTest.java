/* Copyright (C) 2021 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.query;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.intevation.lada.model.master.Filter;
import de.intevation.lada.model.master.FilterType;
import de.intevation.lada.model.master.GridColConf;
import de.intevation.lada.model.master.GridColMp;

/**
 * Unit test for QueryTools.
 */
public class QueryToolsTest {

    private static final String TEST_QUERY_BASE = "Would be SQL in real life";

    private FilterType filterType = new FilterType();
    private Filter filter = new Filter();
    private final String filterSql = "test = :param";

    private GridColMp column1 = new GridColMp(),
        column2 = new GridColMp(),
        column3 = new GridColMp();

    private GridColConf columnValue1 = new GridColConf(),
        columnValue2 = new GridColConf(),
        columnValue3 = new GridColConf();
    private final String filterValue = "'test'";

    private List<GridColConf> columnValues = List.of(
        columnValue1, columnValue2, columnValue3);

    /**
     * Test preparation of SQL statement if no filter and sorting is requested.
     */
    @Test
    public void prepareSqlNoFilterTest() {
        assertEquals(
            TEST_QUERY_BASE,
            QueryTools.prepareSql(columnValues, TEST_QUERY_BASE)
        );
    }

    /**
     * Test preparation of ORDER BY clause for sorting by one column.
     */
    @Test
    public void prepareSortSqlOneColTest() {
        column1.setDataIndex("test");
        columnValue1.setGridColMp(column1);
        // TODO: Schema should allow only ASC/DESC or just make it a boolean!
        columnValue1.setSort("xxx");
        assertEquals(
            " ORDER BY test xxx ",
            QueryTools.prepareSortSql(columnValues)
        );
    }

    /**
     * Test preparation of ORDER BY clause for sorting by two columns.
     */
    @Test
    public void prepareSortSqlTwoColTest() {
        column1.setDataIndex("test");
        columnValue1.setGridColMp(column1);
        // TODO: Schema should allow only ASC/DESC or just make it a boolean!
        columnValue1.setSort("xxx");

        column2.setDataIndex("another");
        columnValue2.setGridColMp(column2);
        // TODO: Schema should allow only ASC/DESC or just make it a boolean!
        columnValue2.setSort("yyy");

        assertEquals(
            " ORDER BY test xxx , another yyy ",
            QueryTools.prepareSortSql(columnValues)
        );
    }

    /**
     * Test preparation of ORDER BY clause for sorting in specified order.
     */
    @Test
    public void prepareSortSqlOrderedColTest() {
        column1.setDataIndex("test");
        columnValue1.setGridColMp(column1);
        // TODO: Schema should allow only ASC/DESC or just make it a boolean!
        columnValue1.setSort("xxx");

        column2.setDataIndex("second");
        columnValue2.setGridColMp(column2);
        // TODO: Schema should allow only ASC/DESC or just make it a boolean!
        columnValue2.setSort("yyy");
        columnValue2.setSortIndex(2);

        column3.setDataIndex("first");
        columnValue3.setGridColMp(column3);
        // TODO: Schema should allow only ASC/DESC or just make it a boolean!
        columnValue3.setSort("zzz");
        columnValue3.setSortIndex(1);

        assertEquals(
            " ORDER BY first zzz , second yyy , test xxx ",
            QueryTools.prepareSortSql(columnValues)
        );
    }

    /**
     * Test preparation of WHERE clause for filtering by one column.
     */
    @Test
    public void prepareSqlOneColFilterTest() {
        setFilterWithType("test");

        assertEquals(
            TEST_QUERY_BASE + " WHERE " + filterSql,
            QueryTools.prepareSql(columnValues, TEST_QUERY_BASE)
        );
    }

    /**
     * Test preparation of WHERE clause for one "generic" filter.
     */
    @Test
    public void prepareSqlOneColGenericFilterTest() {
        setFilterWithType(QueryTools.GENERICID_FILTER_TYPE);

        assertEquals(
            "SELECT * FROM (" + TEST_QUERY_BASE
            + ") AS inner_query WHERE " + filterSql,
            QueryTools.prepareSql(columnValues, TEST_QUERY_BASE)
        );
    }

    private void setFilterWithType(String type) {
        filterType.setType(type);

        filter.setFilterType(filterType);
        filter.setSql(filterSql);

        column1.setFilter(filter);

        columnValue1.setGridColMp(column1);
        columnValue1.setFilterVal(filterValue);
        columnValue1.setIsFilterActive(true);
        columnValue1.setIsFilterNull(false);
    }
}
