/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.data;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;


public class KdaUtilTest {

    /* Tolerance for coordinate comparison */
    private static final double EPSILON = 1e-2;

    /* Expected coordinates for KdaUtil.KDA_* retrieved with PostGIS using
     * SELECT ST_AsText(ST_Transform(ST_SetSRID(
     *     'POINT(7.0998138888889 50.733991666667)'::geometry, 4326), <CRS>))
     */
    private static final Map<Integer, Map<String, Double>> COORDS = Map.of(
        // CRS = 31466
        KdaUtil.KDA_GK, Map.of("x", 2577686.36896957, "y", 5622631.76328874),
        // CRS = 4326
        KdaUtil.KDA_GD, Map.of("x", 7.0998138888889, "y", 50.733991666667)
    );

    @Test
    public void transform1To1() {
        KdaUtil kdaUtil = new KdaUtil();
        ObjectNode result = kdaUtil.transform(
            KdaUtil.KDA_GK,
            KdaUtil.KDA_GK,
            COORDS.get(KdaUtil.KDA_GK).get("x").toString(),
            COORDS.get(KdaUtil.KDA_GK).get("y").toString()
        );
        assertEquals(
            COORDS.get(KdaUtil.KDA_GK).get("x"),
            result.get("x").asDouble(),
            EPSILON
        );
    }
    // TODO: Test all supported transformations
}
