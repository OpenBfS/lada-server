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
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.referencing.FactoryException;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test accuracy of coordinate transformations
 */
@RunWith(Parameterized.class)
public class KdaUtilTest {

    /* Tolerance in meter for coordinate comparison */
    private static final double EPSILON = 1;

    /* Expected coordinates for KdaUtil.KDA_* (except KdaUtil.KDA_GS)
     * retrieved with PostGIS (2.5.5 with Proj 4.9.3) using
     * SELECT ST_AsText(ST_Transform(ST_SetSRID(
     *     'POINT(7.0998138888889 50.733991666667)'::geometry, 4326), <CRS>))
     */
    private static final Map<Integer, Map<String, String>> COORDS = Map.of(
        // 1: CRS = 31466
        /*
         * Before transformation, put datum shift grid from
         * http://crs.bkg.bund.de/crseu/crs/descrtrans/BeTA/BETA2007.gsb
         * into /usr/share/proj/ and updated spatial_ref_sys accordingly:
         * UPDATE spatial_ref_sys
         *     SET proj4text = proj4text || ' +nadgrids=BETA2007.gsb'
         *     WHERE srid = 31466
         */
        KdaUtil.KDA_GK,
        Map.of("x", "2577687.65820815", "y", "5622631.72513064"),

        // 2:
        // SELECT ST_AsLatLonText(ST_SetSRID(
        //     'POINT(7.0998138888889 50.733991666667)'::geometry, 4326))
        // with non-digit characters removed and leading zeros complemented
        // in the resulting 50°44'2.370"N 7°5'59.330"E
        KdaUtil.KDA_GS,
        Map.of("x", "70559.330", "y", "504402.370"),

        // 4: CRS = 4326
        KdaUtil.KDA_GD,
        Map.of("x", "7.0998138888889", "y", "50.733991666667"),

        // 5: CRS = 32632, zone prepended to "x"
        KdaUtil.KDA_UTM_WGS84,
        Map.of("x", "32365908.607704498", "y", "5621966.21754899"),

        // 6: CRS = 25832, zone prepended to "x"
        KdaUtil.KDA_UTM_ETRS89,
        Map.of("x", "32365908.607703176", "y", "5621966.21742558"),

        // 8: CRS = 23032, zone prepended to "x"
        KdaUtil.KDA_UTM_ED50,
        Map.of("x", "32365990.950936107", "y", "5622168.57949754")
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

    // TODO: Do not ignore
    @Ignore("Tests involving KDA_UTM_ED50 currently fail. "
        + "Further investigation needed.")
    @Test
    public void transformTest() throws FactoryException {
        if (fromKda == KdaUtil.KDA_GK || toKda == KdaUtil.KDA_GK) {
            assumeTrue(
                "Missing BETA2007.gsb datum shift grid in "
                + "src/main/resources/org/geotools/referencing/"
                + "factory/gridshift",
                CRS.findMathTransform(
                    CRS.decode("EPSG:31466"), CRS.decode("EPSG:4326"))
                .toString().contains("BETA2007.gsb")
            );
        }

        KdaUtil kdaUtil = new KdaUtil();
        ObjectNode result = kdaUtil.transform(
            fromKda,
            toKda,
            COORDS.get(fromKda).get("x"),
            COORDS.get(fromKda).get("y")
        );
        assertNotNull("Transformation result is null", result);

        // Expected coordinates
        int compareWith = toKda;
        if (compareWith == KdaUtil.KDA_GS) {
            // result will be converted to decimal notation before comparison
            compareWith = KdaUtil.KDA_GD;
        }
        double eX = Double.parseDouble(COORDS.get(compareWith).get("x"));
        double eY = Double.parseDouble(COORDS.get(compareWith).get("y"));


        // Transformation result coordinates
        double rX, rY;
        switch (toKda) {
        case KdaUtil.KDA_GS:
            result = kdaUtil.arcToDegree(
                result.get("x").asText(),
                result.get("y").asText());
            assertNotNull("Conversion of transformation result "
                + "to decimal notation failed", result);
        default:
            rX = Double.parseDouble(
                result.get("x").asText().replace(',', '.'));
            rY = Double.parseDouble(
                result.get("y").asText().replace(',', '.'));
        }

        // Distance between expected and result
        double d;
        switch (toKda) {
        case KdaUtil.KDA_GS:
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
                "from %d to %d: "
                + "Distance %.2f m between result POINT(%.10g %.10g) and "
                + "expected POINT(%.10g %.10g) > %.2f m",
                fromKda, toKda, d, rX, rY, eX, eY, EPSILON),
            d <= EPSILON
        );
    }
}
