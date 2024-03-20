/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.data;

import static org.junit.Assume.assumeTrue;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.referencing.FactoryException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Unit tests for KdaUtil.
 */

@RunWith(Parameterized.class)
public class KdaUtilTest {

    private static Logger logger = Logger.getLogger(KdaUtilTest.class);

    /* Tolerance in meter for coordinate comparison */
    private static final double EPSILON = 1.05;

    /* Expected coordinates for KdaUtil.KDA.* (except KdaUtil.KDA.GS)
     * retrieved with PostGIS (2.5.5 with Proj 4.9.3) using
     * SELECT ST_AsText(ST_Transform(ST_SetSRID(
     *     'POINT(7.0998138888889 50.733991666667)'::geometry, 4326), <CRS>))
     */
    private static final Map<KdaUtil.KDA, Map<String, String>> COORDS = Map.of(
        // 1: CRS = 31466
        /*
         * Before transformation, put datum shift grid from
         * http://crs.bkg.bund.de/crseu/crs/descrtrans/BeTA/BETA2007.gsb
         * into /usr/share/proj/ and updated spatial_ref_sys accordingly:
         * UPDATE spatial_ref_sys
         *     SET proj4text = proj4text || ' +nadgrids=BETA2007.gsb'
         *     WHERE srid = 31466
         */
        KdaUtil.KDA.GK,
        Map.of("x", "2577688", "y", "5622632"),
        //Map.of("x", "2577687.65820815", "y", "5622631.72513064"),

        // 2:
        // SELECT ST_AsLatLonText(ST_SetSRID(
        //     'POINT(7.0998138888889 50.733991666667)'::geometry, 4326))
        // with non-digit characters removed and leading zeros complemented
        // in the resulting 50°44'2.370"N 7°5'59.330"E
        KdaUtil.KDA.GS,
        Map.of("x", "70559.330", "y", "504402.370"),

        // 4: CRS = 4326
        KdaUtil.KDA.GD,
        Map.of("x", "7.0998138888889", "y", "50.733991666667"),

        // 5: CRS = 32632, zone prepended to "x"
        KdaUtil.KDA.UTM_WGS84,
        Map.of("x", "32365909", "y", "5621966"),
        //Map.of("x", "32365908.607704498", "y", "5621966.21754899"),

        // 6: CRS = 25832, zone prepended to "x"
        KdaUtil.KDA.UTM_ETRS89,
        Map.of("x", "32365909", "y", "5621966"),
        //Map.of("x", "32365908.607703176", "y", "5621966.21742558"),

        // 8: CRS = 23032, zone prepended to "x"
        KdaUtil.KDA.UTM_ED50,
        Map.of("x", "32365991", "y", "5622169")
        //Map.of("x", "32365990.950936107", "y", "5622168.57949754")
    );

    private final String decimalPoint = ".", decimalComma = ",";

    private final String messageAssertNotNull = "Transformation result is null";

    /**
     * @return All combinations of KdaUtil.KDA.* as input and output.
     */
    @Parameters(name = "from {0} to {1}")
    public static List<Object[]> fromToCombinations() {
        List<Object[]> combinations = new ArrayList<Object[]>();
        for (KdaUtil.KDA from : COORDS.keySet()) {
            for (KdaUtil.KDA to : COORDS.keySet()) {
                combinations.add(new Object[]{from, to});
            }
        }
        return combinations;
    }

    @Parameter(0)
    public KdaUtil.KDA fromKda;

    @Parameter(1)
    public KdaUtil.KDA toKda;

    /**
     * Accuracy of coordinate transformations.
     */
    @Test
    public void transformTest() throws FactoryException {
        if (fromKda == KdaUtil.KDA.GK || toKda == KdaUtil.KDA.GK) {
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
        KdaUtil.Result result = kdaUtil.transform(
            fromKda,
            toKda,
            COORDS.get(fromKda).get("x"),
            COORDS.get(fromKda).get("y")
        );
        Assert.assertNotNull(messageAssertNotNull, result);
        logger.trace("Transformation result: x=" + result.getX()
            + " y=" + result.getY());

        // Expected coordinates
        KdaUtil.KDA compareWith = toKda;
        if (compareWith == KdaUtil.KDA.GS) {
            // result will be converted to decimal notation before comparison
            compareWith = KdaUtil.KDA.GD;
        }
        double eX = Double.parseDouble(COORDS.get(compareWith).get("x"));
        double eY = Double.parseDouble(COORDS.get(compareWith).get("y"));


        // Transformation result coordinates
        double rX, rY;
        switch (toKda) {
        case GS:
            result = kdaUtil.arcToDegree(
                result.getX().replace(decimalComma, decimalPoint),
                result.getY().replace(decimalComma, decimalPoint));
            Assert.assertNotNull("Conversion of transformation result "
                + "to decimal notation failed", result);
        default:
            rX = Double.parseDouble(
                result.getX().replace(decimalComma, decimalPoint));
            rY = Double.parseDouble(
                result.getY().replace(decimalComma, decimalPoint));
        }

        // Distance between expected and result
        double d;
        switch (toKda) {
        case GS:
        case GD:
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
        // TODO: Better results also for KDA.UTM_ED50
        double epsilon = EPSILON
            * (toKda == KdaUtil.KDA.UTM_ED50
                || fromKda == KdaUtil.KDA.UTM_ED50
                ? 5 : 1);
        Assert.assertTrue(
            String.format(
                "from %s to %s: "
                + "Distance %.2f m between result POINT(%.10g %.10g) and "
                + "expected POINT(%.10g %.10g) > %.2f m",
                fromKda.toString(), toKda.toString(),
                d, rX, rY, eX, eY, epsilon),
            d <= epsilon
        );
    }

    /**
     * Input with comma as decimal separator.
     */
    @Test
    public void commaInputTest() {
        KdaUtil.Result result = new KdaUtil().transform(
            fromKda,
            toKda,
            COORDS.get(fromKda).get("x").replace(decimalPoint, decimalComma),
            COORDS.get(fromKda).get("y").replace(decimalPoint, decimalComma)
        );
        Assert.assertNotNull(messageAssertNotNull, result);
    }

    /**
     * Invalid zone numbers.
     */
    @Test
    public void invalidZoneTest() {
        double invalidZone;
        final double invalidGKZone = 999, invalidUTMZone = 99;
        switch (fromKda) {
        case GS:
        case GD:
            // No zone number in input
            return;
        case GK:
            invalidZone = invalidGKZone;
            break;
        default:
            invalidZone = invalidUTMZone;
        }
        String x = COORDS.get(fromKda).get("x");
        String formattedX = new DecimalFormat(KdaUtil.EASTING_PATTERN).format(
            invalidZone * KdaUtil.ZONE_PREFIX_MULTIPLIER
            + Double.parseDouble(x) % KdaUtil.ZONE_PREFIX_MULTIPLIER);
        Assert.assertThrows(RuntimeException.class,
        () -> {
            new KdaUtil().transform(
            fromKda,
            toKda,
            formattedX,
            COORDS.get(fromKda).get("y"));
        });
    }

    /**
     * Negative sexagesimal input.
     */
    @Test
    public void sexagesimalNegativeInputTest() {
        // Compare sexagesimal input with decimal geodetic output only
        if (fromKda != KdaUtil.KDA.GS || toKda != KdaUtil.KDA.GD) {
            return;
        }
        KdaUtil.Result result = new KdaUtil().transform(
            fromKda,
            toKda,
            "-70000.000",
            "-500000.000");
        Assert.assertNotNull(messageAssertNotNull, result);
        Assert.assertEquals("-7.0", result.getX());
        Assert.assertEquals("-50.0", result.getY());
    }

    /**
     * Out of range longitude/easting.
     *
     * Transformation functions are expected to throw a RuntimeException
     */
    @Test
    public void invalidXTest() {
        String x;
        switch (fromKda) {
        case GS:
            // Valid longitude is between -180 and 180 degrees
            x = "-1810000.000";
            break;
        case GD:
            // Valid longitude is between -180 and 180 degrees
            x = "-181";
            break;
        default:
            x = new DecimalFormat(KdaUtil.EASTING_PATTERN).format(
                // Negative easting is invalid
                -Double.parseDouble(COORDS.get(fromKda).get("x")));
        }
        if (fromKda != toKda) {
            Assert.assertThrows(RuntimeException.class,
            () -> {
                new KdaUtil().transform(
                    fromKda,
                    toKda,
                    x,
                    COORDS.get(fromKda).get("y"));
            });
        }
    }

    /**
     * Out of range latitude/northing.
     *
     * Transformation functions are expected to throw a RuntimeException
     */
    @Test
    public void invalidYTest() {
        String y;
        switch (fromKda) {
        case GS:
            // Valid latitude is between -90 and 90 degrees
            y = "990000.000";
            break;
        case GD:
            // Valid latitude is between -90 and 90 degrees
            y = "99";
            break;
        default:
            // Absolute value of northing is < 1e7
            final double invalidN = 1e7;
            y = new DecimalFormat(KdaUtil.NORTHING_PATTERN).format(invalidN);
        }
        if (fromKda != toKda) {
            Assert.assertThrows(RuntimeException.class, () -> {
                new KdaUtil().transform(
                fromKda,
                toKda,
                COORDS.get(fromKda).get("x"),
                y);
            });
        }
    }
}
