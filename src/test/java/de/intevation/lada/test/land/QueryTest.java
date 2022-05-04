/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import de.intevation.lada.test.ServiceTest;

/**
 * Test query entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class QueryTest extends ServiceTest {

    /**
     * Execute the tests.
     */
    public final void execute() {
        get("query", "rest/query/probe");
        get("query", "rest/query/messprogramm");
        get("query", "rest/query/stammdaten");
    }
}
