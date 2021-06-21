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

import de.intevation.lada.model.stammdaten.GridColumnValue;
import de.intevation.lada.model.stammdaten.GridColumn;

/**
 * Unit test for QueryTools.
 */
public class QueryToolsTest {

    private static final String TEST_QUERY_BASE = "Would be SQL in real life";

    private GridColumnValue columnValue1 = new GridColumnValue(),
        columnValue2 = new GridColumnValue(),
        columnValue3 = new GridColumnValue();

    private List<GridColumnValue> columnValues = List.of(
        columnValue1, columnValue2, columnValue3);

    /**
     * Test preparation of ORDER BY clause if no specific sorting is requested.
     */
    @Test
    public void prepareSqlNoSortTest() {
        assertEquals(
            TEST_QUERY_BASE,
            QueryTools.prepareSql(columnValues, TEST_QUERY_BASE)
        );
    }

    /**
     * Test preparation of ORDER BY clause for sorting by one column.
     */
    @Test
    public void prepareSqlOneColSortTest() {
        GridColumn column = new GridColumn();
        column.setDataIndex("test");
        columnValue1.setGridColumn(column);
        // TODO: Schema should allow only ASC/DESC or just make it a boolean!
        columnValue1.setSort("xxx");
        assertEquals(
            TEST_QUERY_BASE + " ORDER BY test xxx ",
            QueryTools.prepareSql(columnValues, TEST_QUERY_BASE)
        );
    }

    /**
     * Test preparation of ORDER BY clause for sorting by two columns.
     */
    @Test
    public void prepareSqlTwoColSortTest() {
        GridColumn column1 = new GridColumn();
        column1.setDataIndex("test");
        columnValue1.setGridColumn(column1);
        // TODO: Schema should allow only ASC/DESC or just make it a boolean!
        columnValue1.setSort("xxx");

        GridColumn column2 = new GridColumn();
        column2.setDataIndex("another");
        columnValue2.setGridColumn(column2);
        // TODO: Schema should allow only ASC/DESC or just make it a boolean!
        columnValue2.setSort("yyy");

        assertEquals(
            TEST_QUERY_BASE + " ORDER BY test xxx , another yyy ",
            QueryTools.prepareSql(columnValues, TEST_QUERY_BASE)
        );
    }

    /**
     * Test preparation of ORDER BY clause for sorting in specified order.
     */
    @Test
    public void prepareSqlOrderedColSortTest() {
        GridColumn column1 = new GridColumn();
        column1.setDataIndex("test");
        columnValue1.setGridColumn(column1);
        // TODO: Schema should allow only ASC/DESC or just make it a boolean!
        columnValue1.setSort("xxx");

        GridColumn column2 = new GridColumn();
        column2.setDataIndex("second");
        columnValue2.setGridColumn(column2);
        // TODO: Schema should allow only ASC/DESC or just make it a boolean!
        columnValue2.setSort("yyy");
        columnValue2.setSortIndex(2);

        GridColumn column3 = new GridColumn();
        column3.setDataIndex("first");
        columnValue3.setGridColumn(column3);
        // TODO: Schema should allow only ASC/DESC or just make it a boolean!
        columnValue3.setSort("zzz");
        columnValue3.setSortIndex(1);

        assertEquals(
            TEST_QUERY_BASE + " ORDER BY first zzz , second yyy , test xxx ",
            QueryTools.prepareSql(columnValues, TEST_QUERY_BASE)
        );
    }
}
