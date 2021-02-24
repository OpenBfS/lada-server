/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.data;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.GeodeticCalculator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.databind.node.ObjectNode;


@RunWith(Parameterized.class)
public class KdaUtilTest {

    /* Tolerance in meter for coordinate comparison */
    private static final double EPSILON = 1e-2;

    /* Expected coordinates for KdaUtil.KDA_* retrieved with PostGIS using
     * SELECT ST_AsText(ST_Transform(ST_SetSRID(
     *     'POINT(7.0998138888889 50.733991666667)'::geometry, 4326), <CRS>))
     */
    private static final Map<Integer, Map<String, Double>> COORDS = Map.of(
        // 1: CRS = 31466
        KdaUtil.KDA_GK,
        Map.of("x", 2577686.36896957, "y", 5622631.76328874),

        // 2:
        // TODO: Add geodetic coordinates in degrees-minutes-seconds notation

        // 4: CRS = 4326
        KdaUtil.KDA_GD,
        Map.of("x", 7.0998138888889, "y", 50.733991666667),

        // 5: CRS = 32632, zone prepended to "x"
        KdaUtil.KDA_UTM_WGS84,
        Map.of("x", 32365908.607704498, "y", 5621966.21754899),

        // 6: CRS = 25832, zone prepended to "x"
        KdaUtil.KDA_UTM_ETRS89,
        Map.of("x", 32365908.607703176, "y", 5621966.21742558),

        // 8: CRS = 23032, zone prepended to "x"
        KdaUtil.KDA_UTM_ED50,
        Map.of("x", 32365990.950936107, "y", 5622168.57949754)
    );

    @Parameters(name = "from {0} to {1}")
    public static List<Object[]> fromToCombinations() {
        // All combinations of KdaUtil.KDA_* as input and output
        List<Object[]> combinations = new ArrayList<Object[]>();
        for (Integer from : COORDS.keySet()) {
            for (Integer to : COORDS.keySet()) {
                combinations.add(new Object[]{from, to});
            }
        }
        return combinations;
    }

    @Parameter(0)
    public int fromKda;

    @Parameter(1)
    public int toKda;

    @Test
    public void transformTest() {
        ObjectNode result = new KdaUtil().transform(
            fromKda,
            toKda,
            String.format("%f", COORDS.get(fromKda).get("x")),
            String.format("%f", COORDS.get(fromKda).get("y"))
        );
        assertNotNull("Transformation result is null", result);

        // Expected coordinates
        double eX = COORDS.get(toKda).get("x");
        double eY = COORDS.get(toKda).get("y");

        // Transformation result coordinates
        double rX = Double.parseDouble(
            result.get("x").asText().replace(',', '.'));
        double rY = Double.parseDouble(
            result.get("y").asText().replace(',', '.'));

        // Distance between expected and result
        double d;
        switch (toKda) {
        case KdaUtil.KDA_GD:
            GeodeticCalculator gc = new GeodeticCalculator(
                DefaultGeographicCRS.WGS84);
            gc.setStartingGeographicPoint(eY, eX);
            gc.setDestinationGeographicPoint(rY, rX);
            d = gc.getOrthodromicDistance();
            break;
        default:
            d = Math.sqrt(
                Math.pow(rX - eX, 2) + Math.pow(rY - eY, 2));
        }
        assertTrue(
            String.format(
                "Distance %.2f m between result POINT(%.10g %.10g) and "
                + "expected POINT(%.10g %.10g) > %.2f m",
                d, rX, rY, eX, eY, EPSILON),
            d <= EPSILON
        );
    }
}
