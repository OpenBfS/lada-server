/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.data;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * Utilities for coordinate transformations.
 *
 */
public class KdaUtil {

    /**
     * Coordinate reference systems supported for transformation.
     */
    public enum KDA {
        /* Represents coordinates in Gauß-Krüger CRS */
        GK,

        /* Represents geodetic coordinates in sexagesimal notation */
        GS,

        /* Represents geodetic coordinates in decimal notation */
        GD,

        /* Represents coordinates in UTM CRS with WGS84 datum */
        UTM_WGS84,

        /* Represents coordinates in UTM CRS with ETRS89 datum */
        UTM_ETRS89,

        /* Represents coordinates in UTM CRS with ED50 datum
         * (Hayford ellipsoid) */
        UTM_ED50;
    };

    // Known database ID of KDA.GD
    public static final int KDA_GD = 4;

    // Map enum constants to known database IDs
    public static final Map<Integer, KDA> KDAS = Map.of(
        1,      KDA.GK,
        2,      KDA.GS,
        KDA_GD, KDA.GD,
        5,      KDA.UTM_WGS84,
        6,      KDA.UTM_ETRS89,
        8,      KDA.UTM_ED50
    );

    /* Expected format of projected input coordinates */
    public static final Pattern X_GK = Pattern.compile(
        "\\d{7,9}(\\.\\d*)?");
    public static final Pattern X_UTM = Pattern.compile(
        "\\d{7,8}(\\.\\d*)?");
    public static final Pattern Y = Pattern.compile(
        "(\\+|-)?\\d{1,7}(\\.\\d*)?");

    /* Expected format of sexagesimal input coordinates */
    // with decimal separator
    public static final Pattern LON_DEC = Pattern.compile(
        "([+|\\-|W|E]?)(\\d{1,3})(\\d{2})(\\d{2})\\.(\\d{1,5})([W|E]?)");
    public static final Pattern LAT_DEC = Pattern.compile(
        "([+|\\-|N|S]?)(\\d{1,2})(\\d{2})(\\d{2})\\.(\\d{1,5})([N|S]?)");
    // Without decimal separator, can include leading zeros
    public static final Pattern LON = Pattern.compile(
        "([+|\\-|W|E]?)(\\d{3})(\\d{0,2})(\\d{0,2})([W|E]?)");
    public static final Pattern LAT = Pattern.compile(
        "([+|\\-|N|S]?)(\\d{2})(\\d{0,2})(\\d{0,2})([N|S]?)");

    /*
     * UTM zone number with given prefix gives the EPSG code for CRS
     * 'ETRS89 / UTM zone <zone number>N'
     */
    private static final String EPSG_UTM_ETRS89_PREFIX = "EPSG:258";

    /*
     * If eastings should be prefixed with a zone number, multiply zone
     * number with this value and add it to the easting
     */
    static final double ZONE_PREFIX_MULTIPLIER = 1e6;

    /*
     * DecimalFormat pattern for eastings including zone prefix
     */
    static final String EASTING_PATTERN = "0000000.###";

    /*
     * DecimalFormat pattern for northings
     */
    static final String NORTHING_PATTERN = "0.###";

    /*
     * Maximum allowed values for longitude and latitude
     */
    public static final double MAX_LON = 180, MAX_LAT = 90;

    /**
     * Representation of transformation result.
     */
    public class Result {
        // Easting or longitude
        private String x;

        // Northing or latitude
        private String y;

        Result(String x, String y) {
            this.x = x;
            this.y = y;
        }

        public String getX() {
            return this.x;
        }

        void setX(String x) {
            this.x = x;
        }

        public String getY() {
            return this.y;
        }

        void setY(String y) {
            this.y = y;
        }
    }

    /**
     * Transform coordinates.
     * @param kdaFrom KDA of given coordinates
     * @param kdaTo KDA to be transformed to
     * @param x Easting or longitude
     * @param y Northing or latitude
     * @throws RuntimeException Thrown if transformation fails with an
     *                          Exception
     * @return Result with transformed coordinates or null if coordinates are
     *         null
     */
    public Result transform(
        KDA kdaFrom, KDA kdaTo, String x, String y
    ) {
        x = x.replace(',', '.');
        y = y.replace(',', '.');

        Transform t;
        try {
            t = switch (kdaFrom) {
                case GK -> new Transform1(x, y);
                case GS -> this.new Transform2(x, y);
                case GD -> this.new Transform4(x, y);
                case UTM_WGS84 -> this.new Transform5(x, y);
                case UTM_ETRS89 -> this.new Transform6(x, y);
                case UTM_ED50 -> this.new Transform8(x, y);
            };
            return t.transform(kdaTo);
        } catch (FactoryException | TransformException e) {
            throw new RuntimeException("Invalid transformation input", e);
        }
    }

    /**
     * Defines the methods to be implemented for coordinate transformation.
     */
    private interface Transform {
        Result transform(KDA to) throws NoSuchAuthorityCodeException,
            TransformException, FactoryException;
        Result transformTo1() throws NoSuchAuthorityCodeException,
            TransformException, FactoryException;
        Result transformTo2() throws NoSuchAuthorityCodeException,
            TransformException, FactoryException;
        Result transformTo4() throws NoSuchAuthorityCodeException,
            TransformException, FactoryException;
        Result transformTo5() throws NoSuchAuthorityCodeException,
            TransformException, FactoryException;
        Result transformTo6() throws NoSuchAuthorityCodeException,
            TransformException, FactoryException;
        Result transformTo8() throws NoSuchAuthorityCodeException,
            TransformException, FactoryException;
    }

    /**
     * Delegates to a class per input KDA.
     */
    private abstract class AbstractTransform implements Transform {
        // Input coordinates
        protected String x;
        protected String y;

        // CRS of input coordinates
        protected CoordinateReferenceSystem crs;

        AbstractTransform(String x, String y) {
            this.x = x;
            this.y = y;
        }

        public Result transform(KDA to)
                throws FactoryException, TransformException {
            switch (to) {
                case GK: return transformTo1();
                case GS: return transformTo2();
                case GD: return transformTo4();
                case UTM_WGS84: return transformTo5();
                case UTM_ETRS89: return transformTo6();
                case UTM_ED50: return transformTo8();
                default: throw new IllegalArgumentException(
                    "Unsupported spatial reference system");
            }
        }
    }

    /**
     * Implements coordinate transformations for Gauß-Krüger input.
     */
    private class Transform1 extends AbstractTransform {

        Transform1(
            String x,
            String y
        ) throws FactoryException {
            super(x, y);
            this.crs = getCRSForGK(x);
        }

        @Override
        public Result transformTo1() {
            return new Result(x, y);
        }

        @Override
        public Result transformTo2() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            Result degrees = jtsTransform(crs, "EPSG:4326", y, x);
            if (degrees == null) {
                return null;
            }
            return degreeToArc(degrees.getY(), degrees.getX());
        }

        @Override
        public Result transformTo4() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            Result coords = jtsTransform(crs, "EPSG:4326", y, x);
            if (coords == null) {
                return null;
            }
            String coordX = coords.getX();
            String coordY = coords.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 7 ? maxLenX : 7;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 7 ? maxLenY : 7;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            return new Result(coordY, coordX);
        }

        @Override
        public Result transformTo5() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            Result degrees = jtsTransform(crs, "EPSG:4326", y, x);
            String epsgWGS = getWgsUtmEpsg(
                Double.parseDouble(degrees.getY()),
                Double.parseDouble(degrees.getX()));
            Result coords = jtsTransform(crs,
                epsgWGS,
                y,
                x);
            if (coords == null) {
                return null;
            }
            coords.setX(epsgWGS.substring(
                epsgWGS.length() - 2,
                epsgWGS.length()) + coords.getX());
            String coordX = String.valueOf(Math.round(Double.valueOf(coords.getX())));
            String coordY = String.valueOf(Math.round(Double.valueOf(coords.getY())));
            coords.setX(coordX);
            coords.setY(coordY);
            return coords;
        }

        @Override
        public Result transformTo6() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            Result degrees = jtsTransform(crs, "EPSG:4326", y, x);
            // TODO: explain why x and y are interchanged here
            String epsgEtrs = getEtrsEpsg(
                Double.parseDouble(degrees.getY()),
                Double.parseDouble(degrees.getX()));
            Result coords = jtsTransform(crs,
                epsgEtrs,
                y,
                x);
            if (coords == null) {
                return null;
            }
            coords.setX(epsgEtrs.substring(
                epsgEtrs.length() - 2,
                epsgEtrs.length()) + coords.getX());
            String coordX = String.valueOf(Math.round(Double.valueOf(coords.getX())));
            String coordY = String.valueOf(Math.round(Double.valueOf(coords.getY())));
            coords.setX(coordX);
            coords.setY(coordY);
            return coords;
        }

        @Override
        public Result transformTo8() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            Result degrees = jtsTransform(crs, "EPSG:4326", y, x);
            String epsgEd50 = getEpsgForEd50UtmFromDegree(
                degrees.getY());
            Result coords = jtsTransform(crs, epsgEd50, y, x);
            if (coords == null) {
                return null;
            }
            String coordX = String.valueOf(Math.round(Double.valueOf(coords.getX())));
            String coordY = String.valueOf(Math.round(Double.valueOf(coords.getY())));
            String zone = epsgEd50.substring(
                epsgEd50.length() - 2, epsgEd50.length());
            coords.setX(zone + coordX);
            coords.setY(coordY);
            return coords;
        }
    }

    /**
     * Implements coordinate transformations for sexagesimal geodetic input.
     */
    private class Transform2 extends AbstractTransform {

        Transform2(String x, String y) {
            super(x, y);
        }

        @Override
        public Result transformTo1() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            Result degrees = arcToDegree(x, y);
            if (degrees == null) {
                return null;
            }
            String epsgGk = getGkEpsg(
                Double.parseDouble(degrees.getX()),
                Double.parseDouble(degrees.getY()));

            Result coords = jtsTransform(
                "EPSG:4326",
                epsgGk,
                degrees.getY(),
                degrees.getX());
            if (coords == null) {
                return null;
            }
            String coordX = String.valueOf(Math.round(Double.valueOf(coords.getX())));
            String coordY = String.valueOf(Math.round(Double.valueOf(coords.getY())));
            coords.setX(coordY);
            coords.setY(coordX);
            return coords;
        }

        @Override
        public Result transformTo2() {
            return new Result(x, y);
        }

        @Override
        public Result transformTo4() {
            return arcToDegree(x, y);
        }

        @Override
        public Result transformTo5() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            Result degrees = arcToDegree(x, y);
            if (degrees == null) {
                return null;
            }
            String epsgWgs = getWgsUtmEpsg(
                Double.parseDouble(degrees.getX()),
                Double.parseDouble(degrees.getY()));
            Result coords = jtsTransform("EPSG:4326",
                epsgWgs,
                degrees.getY(),
                degrees.getX());
            if (coords == null) {
                return null;
            }
            coords.setX(epsgWgs.substring(
                epsgWgs.length() - 2,
                epsgWgs.length()) + coords.getX());
            String coordX = String.valueOf(Math.round(Double.valueOf(coords.getX())));
            String coordY = String.valueOf(Math.round(Double.valueOf(coords.getY())));
            coords.setX(coordX);
            coords.setY(coordY);
            return coords;
        }

        @Override
        public Result transformTo6() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            Result degrees = arcToDegree(x, y);
            String epsgEtrs = getEtrsEpsg(
                Double.parseDouble(degrees.getX()),
                Double.parseDouble(degrees.getY()));
            Result coords = jtsTransform("EPSG:4326",
                epsgEtrs,
                degrees.getY(),
                degrees.getX());
            coords.setX(epsgEtrs.substring(
                epsgEtrs.length() - 2,
                epsgEtrs.length()) + coords.getX());
            String coordX = String.valueOf(Math.round(Double.valueOf(coords.getX())));
            String coordY = String.valueOf(Math.round(Double.valueOf(coords.getY())));
            coords.setX(coordX);
            coords.setY(coordY);
            return coords;
        }

        @Override
        public Result transformTo8() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            Result degrees = arcToDegree(x, y);
            String epsgEd50 = getEpsgForEd50UtmFromDegree(
                degrees.getX());
            Result coords = jtsTransform("EPSG:4326",
                epsgEd50,
                degrees.getY(),
                degrees.getX());
            String coordX = String.valueOf(Math.round(Double.valueOf(coords.getX())));
            String coordY = String.valueOf(Math.round(Double.valueOf(coords.getY())));
            String zone = epsgEd50.substring(
                epsgEd50.length() - 2, epsgEd50.length());
            coords.setX(zone + coordX);
            coords.setY(coordY);
            return coords;
        }
    }

    /**
     * Implements coordinate transformations for decimal geodetic input.
     */
    private class Transform4 extends AbstractTransform {

        Transform4(String x, String y) {
            super(x, y);
        }

        @Override
        public Result transformTo1() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            String epsgGk = getGkEpsg(Double.valueOf(x), Double.valueOf(y));
            Result coords = jtsTransform("EPSG:4326", epsgGk, y, x);
            if (coords == null) {
                return null;
            }
            String coordX = String.valueOf(Math.round(Double.valueOf(coords.getX())));
            String coordY = String.valueOf(Math.round(Double.valueOf(coords.getY())));
            coords.setX(coordY);
            coords.setY(coordX);
            return coords;
        }

        @Override
        public Result transformTo2() {
            return degreeToArc(x, y);
        }

        @Override
        public Result transformTo4() {
            return new Result(x, y);
        }

        @Override
        public Result transformTo5() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            String epsgWgs = getWgsUtmEpsg(
                Double.valueOf(x), Double.valueOf(y));
            Result coords = jtsTransform("EPSG:4326", epsgWgs, y, x);
            if (coords == null) {
                return null;
            }
            coords.setX(epsgWgs.substring(
                epsgWgs.length() - 2,
                epsgWgs.length()) + coords.getX());
            String coordX = String.valueOf(Math.round(Double.valueOf(coords.getX())));
            String coordY = String.valueOf(Math.round(Double.valueOf(coords.getY())));
            coords.setX(coordX);
            coords.setY(coordY);
            return coords;
        }

        @Override
        public Result transformTo6() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            String epsgEtrs = getEtrsEpsg(
                Double.valueOf(x), Double.valueOf(y));
            Result coords = jtsTransform("EPSG:4326", epsgEtrs, y, x);
            if (coords == null) {
                return null;
            }
            coords.setX(epsgEtrs.substring(
                epsgEtrs.length() - 2,
                epsgEtrs.length()) + coords.getX());
            String coordX = String.valueOf(Math.round(Double.valueOf(coords.getX())));
            String coordY = String.valueOf(Math.round(Double.valueOf(coords.getY())));
            coords.setX(coordX);
            coords.setY(coordY);
            return coords;
        }

        @Override
        public Result transformTo8() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            String epsgEd50 = getEpsgForEd50UtmFromDegree(x);
            Result coords = jtsTransform("EPSG:4326", epsgEd50, y, x);
            if (coords == null) {
                return null;
            }
            String coordX = String.valueOf(Math.round(Double.valueOf(coords.getX())));
            String coordY = String.valueOf(Math.round(Double.valueOf(coords.getY())));
            String zone = epsgEd50.substring(
                epsgEd50.length() - 2, epsgEd50.length());
            coords.setX(zone + coordX);
            coords.setY(coordY);
            return coords;
        }
    }

    /**
     * Implements coordinate transformations for UTM-WGS84 input.
     */
    private class Transform5 extends AbstractTransform {
        Transform5(
            String x,
            String y
        ) throws FactoryException {
            super(x, y);
            this.crs = getCRSForWgsUtm(x);
        }

        @Override
        public Result transformTo1() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            x = x.substring(2, x.length());
            Result degrees = jtsTransform(crs, "EPSG:4326", x, y);
            if (degrees == null) {
                return null;
            }
            String epsgGk = getGkEpsg(
                Double.parseDouble(degrees.getY()),
                Double.parseDouble(degrees.getX()));
            Result coords = jtsTransform(crs, epsgGk, x, y);
            if (coords == null) {
                return null;
            }
            String coordX = String.valueOf(Math.round(Double.valueOf(coords.getX())));
            String coordY = String.valueOf(Math.round(Double.valueOf(coords.getY())));
            coords.setX(coordY);
            coords.setY(coordX);
            return coords;
        }

        @Override
        public Result transformTo2() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            x = x.substring(2, x.length());
            Result degrees = jtsTransform(crs, "EPSG:4326", x, y);
            Result coords = degreeToArc(degrees.getY(), degrees.getX());
            return coords;
        }

        @Override
        public Result transformTo4() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            x = x.substring(2, x.length());
            Result coords = jtsTransform(crs, "EPSG:4326", x, y);
            if (coords == null) {
                return null;
            }
            String coordX = coords.getX();
            String coordY = coords.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 7 ? maxLenX : 7;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 7 ? maxLenY : 7;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coords.setX(coordY);
            coords.setY(coordX);
            return coords;
        }

        @Override
        public Result transformTo5() {
            return new Result(x, y);
        }

        @Override
        public Result transformTo6() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            x = x.substring(2, x.length());
            Result degrees = jtsTransform(crs, "EPSG:4326", x, y);
            if (degrees == null) {
                return null;
            }
            String epsgEtrs = getEtrsEpsg(
                Double.parseDouble(degrees.getY()),
                Double.parseDouble(degrees.getX()));
            Result response = jtsTransform(crs, epsgEtrs, x, y);
            if (response == null) {
                return response;
            }

            // Format output
            formatUTM(response, getUTMZone(Double.parseDouble(degrees.getY())));
            return response;
        }

        @Override
        public Result transformTo8() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            x = x.substring(2, x.length());
            Result coords4326 = jtsTransform(crs, "EPSG:4326", x, y);
            if (coords4326 == null) {
                return null;
            }
            String epsgEd50 =
                getEpsgForEd50UtmFromDegree(coords4326.getY());
            Result coords = jtsTransform(crs, epsgEd50, x, y);
            if (coords == null) {
                return null;
            }
            String coordX = String.valueOf(Math.round(Double.valueOf(coords.getX())));
            String coordY = String.valueOf(Math.round(Double.valueOf(coords.getY())));
            String zone = epsgEd50.substring(
                epsgEd50.length() - 2, epsgEd50.length());
            coords.setX(zone + coordX);
            coords.setY(coordY);
            return coords;
        }
    }

    /**
     * Implements coordinate transformations for UTM-ETRS89 input.
     */
    private class Transform6 extends AbstractTransform {

        Transform6(
            String x,
            String y
        ) throws FactoryException {
            super(x, y);
            this.crs = getCRSForEtrs89(x);
        }

        @Override
        public Result transformTo1() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            x = x.substring(2, x.length());
            Result degrees = jtsTransform(crs, "EPSG:4326", x, y);
            if (degrees == null) {
                return null;
            }
            String epsgGk = getGkEpsg(
                Double.parseDouble(degrees.getY()),
                Double.parseDouble(degrees.getX()));
            Result coords = jtsTransform(crs, epsgGk, x, y);
            if (coords == null) {
                return null;
            }
            String coordX = String.valueOf(Math.round(Double.valueOf(coords.getX())));
            String coordY = String.valueOf(Math.round(Double.valueOf(coords.getY())));
            coords.setX(coordY);
            coords.setY(coordX);
            return coords;
        }

        @Override
        public Result transformTo2() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            x = x.substring(2, x.length());
            Result degrees = jtsTransform(crs, "EPSG:4326", x, y);
            Result coords = degreeToArc(degrees.getY(), degrees.getX());
            return coords;
        }

        @Override
        public Result transformTo4() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            x = x.substring(2, x.length());
            Result coords = jtsTransform(crs, "EPSG:4326", x, y);
            if (coords == null) {
                return null;
            }
            String coordX = coords.getX();
            String coordY = coords.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 7 ? maxLenX : 7;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 7 ? maxLenY : 7;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coords.setX(coordY);
            coords.setY(coordX);
            return coords;
        }

        @Override
        public Result transformTo5() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            x = x.substring(2, x.length());
            Result degrees = jtsTransform(crs, "EPSG:4326", x, y);
            if (degrees == null) {
                return null;
            }
            String epsgWgs = getWgsUtmEpsg(
                Double.parseDouble(degrees.getY()),
                Double.parseDouble(degrees.getX()));
            Result response = jtsTransform(crs, epsgWgs, x, y);
            if (response == null) {
                return null;
            }

            // Format output
            formatUTM(response, getUTMZone(Double.parseDouble(degrees.getY())));
            return response;
        }

        @Override
        public Result transformTo6() {
            return new Result(x, y);
        }

        @Override
        public Result transformTo8() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            x = x.substring(2, x.length());
            Result coords4326 = jtsTransform(crs, "EPSG:4326", x, y);
            if (coords4326 == null) {
                return null;
            }
            String epsgEd50 = getEpsgForEd50UtmFromDegree(
                coords4326.getY());
            Result coords = jtsTransform(crs, epsgEd50, x, y);
            if (coords == null) {
                return null;
            }
            String coordX = String.valueOf(Math.round(Double.valueOf(coords.getX())));
            String coordY = String.valueOf(Math.round(Double.valueOf(coords.getY())));
            String zone = epsgEd50.substring(
                epsgEd50.length() - 2, epsgEd50.length());
            coords.setX(zone + coordX);
            coords.setY(coordY);
            return coords;
        }
    }

    /**
     * Implements coordinate transformations for UTM-ED50 input.
     */
    private class Transform8 extends AbstractTransform {

        Transform8(
            String x,
            String y
        ) throws FactoryException {
            super(x, y);
            this.crs = getCRSForEd50Utm(x);
        }

        @Override
        public Result transformTo1() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            x = x.substring(2, x.length());
            Result degrees = jtsTransform(crs, "EPSG:4326", x, y);
            if (degrees == null) {
                return null;
            }
            String epsgGk = getGkEpsg(
                Double.parseDouble(degrees.getY()),
                Double.parseDouble(degrees.getX()));
            Result coords = jtsTransform(crs, epsgGk, x, y);
            if (coords == null) {
                return null;
            }
            String coordX = String.valueOf(Math.round(Double.valueOf(coords.getX())));
            String coordY = String.valueOf(Math.round(Double.valueOf(coords.getY())));
            coords.setX(coordY);
            coords.setY(coordX);
            return coords;
        }

        @Override
        public Result transformTo2() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            x = x.substring(2, x.length());
            Result degrees = jtsTransform(crs, "EPSG:4326", x, y);
            Result coords = degreeToArc(degrees.getY(), degrees.getX());
            return coords;
        }

        @Override
        public Result transformTo4() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            x = x.substring(2, x.length());
            Result coords = jtsTransform(crs, "EPSG:4326", x, y);
            if (coords == null) {
                return null;
            }
            String coordX = coords.getX();
            String coordY = coords.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 7 ? maxLenX : 7;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 7 ? maxLenY : 7;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coords.setX(coordY);
            coords.setY(coordX);
            return coords;
        }

        @Override
        public Result transformTo5() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            String x1 = x.substring(2, x.length());
            Result coords4326 = jtsTransform(crs, "EPSG:4326", x1, y);
            if (coords4326 == null) {
                return null;
            }
            String epsgWgs = getEpsgForWgsUtmFromDegree(
                coords4326.getY());
            Result coords = jtsTransform(crs, epsgWgs, x1, y);
            if (coords == null) {
                return null;
            }
            String coordX = String.valueOf(Math.round(Double.valueOf(coords.getX())));
            String coordY = String.valueOf(Math.round(Double.valueOf(coords.getY())));
            String zone = epsgWgs.substring(
                epsgWgs.length() - 2, epsgWgs.length());
            coords.setX(zone + coordX);
            coords.setY(coordY);
            return coords;
        }

        @Override
        public Result transformTo6() throws NoSuchAuthorityCodeException,
                TransformException, FactoryException {
            String x1 = x.substring(2, x.length());
            Result coords4326 = jtsTransform(crs, "EPSG:4326", x1, y);
            if (coords4326 == null) {
                return null;
            }
            // TODO: explain why x and y are interchanged here
            String epsgEtrs = getEtrsEpsg(
                Double.parseDouble(coords4326.getY()),
                Double.parseDouble(coords4326.getX()));
            Result coords = jtsTransform(crs, epsgEtrs, x1, y);
            if (coords == null) {
                return null;
            }
            String coordX = String.valueOf(Math.round(Double.valueOf(coords.getX())));
            String coordY = String.valueOf(Math.round(Double.valueOf(coords.getY())));
            String zone = epsgEtrs.substring(
                epsgEtrs.length() - 2, epsgEtrs.length());
            coords.setX(zone + coordX);
            coords.setY(coordY);
            return coords;
        }

        @Override
        public Result transformTo8() {
            return new Result(x, y);
        }

    }

    /**
     * Transform given coordinates from epsgFrom to epsgTo.
     * Returns null in case a given EPSG code is invalid.
     * @throws TransformException Thrown if transformations fails
     * @throws FactoryException Thrown if no transformation could be found
     * @throws NoSuchAuthorityCodeException Thrown if epsg code is unknown
     */
    private Result jtsTransform(
        String epsgFrom,
        String epsgTo,
        String x,
        String y
    ) throws NoSuchAuthorityCodeException, TransformException, FactoryException {
        CoordinateReferenceSystem src = CRS.decode(epsgFrom);
        return jtsTransform(src, epsgTo, x, y);
    }

    /**
     * Transform given coordinates from CRS to epsgTo.
     * Returns null in case the given EPSG code is invalid.
     * @throws TransformException Thrown if transformations fails
     * @throws FactoryException Thrown if no transformation could be found
     * @throws NoSuchAuthorityCodeException Thrown if epsg code is unknown
     */
    private Result jtsTransform(
        CoordinateReferenceSystem src,
        String epsgTo,
        String x,
        String y
    ) throws TransformException, NoSuchAuthorityCodeException,
            FactoryException {
        CoordinateReferenceSystem target = CRS.decode(epsgTo);

        MathTransform transform = CRS.findMathTransform(src, target);
        Coordinate srcCoord = new Coordinate();
        srcCoord.y = Double.valueOf(y);
        srcCoord.x = Double.valueOf(x);
        Coordinate targetCoord = new Coordinate();
        JTS.transform(srcCoord, targetCoord, transform);
        return new Result(
            String.valueOf(targetCoord.x),
            String.valueOf(targetCoord.y));
    }

    private Result degreeToArc(String x, String y) {
        //Check if input is parsable and in range
        double xDouble = Double.parseDouble(x);
        double yDouble = Double.parseDouble(y);
        if (Math.abs(xDouble) > MAX_LON || Math.abs(yDouble) > MAX_LAT) {
            throw new IllegalArgumentException(
                String.format("Invalid coordinates: %d - %d", xDouble, yDouble));
        }
        String[] xParts = x.split("\\.");
        String[] yParts = y.split("\\.");

        // Convert fractions of degrees to arc seconds
        final double secondsPerDegree = 3600;
        double wsX = 0;
        double wsY = 0;
        if (xParts.length == 2) {
            wsX = Double.parseDouble("0." + xParts[1]) * secondsPerDegree;
        }
        if (yParts.length == 2) {
            wsY = Double.parseDouble("0." + yParts[1]) * secondsPerDegree;
        }

        // Append arc minutes and seconds as MMSS.sssss to degrees
        final String minSecFormat = "%02d%08.5f";
        String xRes = xParts[0]
            + String.format(minSecFormat, (int) Math.floor(wsX / 60), wsX % 60);
        String yRes = yParts[0]
            + String.format(minSecFormat, (int) Math.floor(wsY / 60), wsY % 60);

        xRes = xRes.replaceAll("\\.", ",");
        yRes = yRes.replaceAll("\\.", ",");
        if (xParts[0].startsWith("-")) {
            xRes = xRes.replace("-", "W");
        } else {
            xRes = "E" + xRes;
        }
        if (yParts[0].startsWith("-")) {
            yRes = yRes.replace("-", "S");
        } else {
            yRes = "N" + yRes;
        }
        return new Result(xRes.toString(), yRes.toString());
    }

    /**
     * Convert degrees in sexagesimal notation into decimal notation.
     * @param x Longitude in sexagesimal notation.
     * @param y Latitude in sexagesimal notation.
     * @return Result with coordinates in decimal notation.
     */
    public Result arcToDegree(String x, String y) {
        int xDegree = 0;
        int xMin = 0;
        int yDegree = 0;
        int yMin = 0;
        double xSec = 0;
        double ySec = 0;
        String xPrefix = "";
        String xSuffix = "";
        String yPrefix = "";
        String ySuffix = "";
        if (x.contains(".")) {
            Matcher m = LON_DEC.matcher(x);
            m.matches();
            xPrefix = m.group(1);
            xDegree = Integer.valueOf(m.group(2));
            xMin = Integer.valueOf(m.group(3));
            xSec = Double.valueOf(m.group(4) + "." + m.group(5));
            xSuffix = m.group(6);
        } else {
            Matcher m = LON.matcher(x);
            m.matches();
            xPrefix = m.group(1);
            xDegree = Integer.valueOf(m.group(2));
            xMin = Integer.valueOf(
                !m.group(3).isEmpty() ? m.group(3) : "0");
            xSec = Double.valueOf(
                !m.group(4).isEmpty() ? m.group(4) : "0.0");
            xSuffix = m.group(5);
        }
        if (y.contains(".")) {
            Matcher m = LAT_DEC.matcher(y);
            m.matches();
            yPrefix = m.group(1);
            yDegree = Integer.valueOf(m.group(2));
            yMin = Integer.valueOf(m.group(3));
            ySec = Double.valueOf(m.group(4) + "." + m.group(5));
            ySuffix = m.group(6);
        } else {
            Matcher m = LAT.matcher(y);
            m.matches();
            yPrefix = m.group(1);
            yDegree = Integer.valueOf(m.group(2));
            yMin = Integer.valueOf(
                !m.group(3).isEmpty() ? m.group(3) : "0");
            ySec = Double.valueOf(
                !m.group(4).isEmpty() ? m.group(4) : "0.0");
            ySuffix = m.group(5);
        }

        double ddX = xDegree + ((xMin / 60d) + (xSec / 3600d));
        double ddY = yDegree + ((yMin / 60d) + (ySec / 3600d));

        if ((xPrefix != null && (xPrefix.equals("-") || xPrefix.equals("W")))
            || (xSuffix != null && xSuffix.equals("W"))
        ) {
            ddX = ddX * -1;
        }
        if ((yPrefix != null && (yPrefix.equals("-") || yPrefix.equals("S")))
            || (ySuffix != null && (ySuffix.equals("S")))
        ) {
            ddY = ddY * -1;
        }
        if (Math.abs(ddX) > MAX_LON || Math.abs(ddY) > MAX_LAT) {
            throw new IllegalArgumentException(
                String.format("Invalid coordinates: %d - %d", ddX, ddY));
        }
        return new Result(String.valueOf(ddX), String.valueOf(ddY));
    }

    private String getWgsUtmEpsg(double x, double y) {
        int pref;
        if (y > 0) {
            pref = 32600;
        } else {
            pref = 32700;
        }
        int code = pref + getUTMZone(x);
        return "EPSG:" + code;
    }

    private String getGkEpsg(double x, double y) {
        int code = 31460;
        int ref = (int) Math.round(x / 3);
        switch (ref) {
            case 2: code += 6; break;
            case 3: code += 7; break;
            case 4: code += 8; break;
            case 5: code += 9; break;
            default: break;
        }
        return "EPSG:" + code;
    }

    private CoordinateReferenceSystem getCRSForWgsUtm(
        String x
    ) throws FactoryException {
        String epsg = "EPSG:326";
        String part = x.split("\\.")[0];
        String zone = part.length() == 7
            ? ("0" + part.substring(0, 1))
            : part.substring(0, 2);
        return CRS.decode(epsg + zone);
    }

    private String getEpsgForWgsUtmFromDegree(String x) {
        Double xCoord = Double.valueOf(x);
        String zone;
        if (xCoord < -12 && xCoord > -18) {
            zone = "28";
        } else if (xCoord <= -6 && xCoord > -12) {
            zone = "29";
        } else if (xCoord <= 0 && xCoord > -6) {
            zone = "30";
        } else if (xCoord <= 6 && xCoord > 0) {
            zone = "31";
        } else if (xCoord <= 12 && xCoord > 6) {
            zone = "32";
        } else if (xCoord <= 18 && xCoord > 12) {
            zone = "33";
        } else if (xCoord <= 24 && xCoord > 18) {
            zone = "34";
        } else {
            return "";
        }
        return "EPSG:326" + zone;
    }

    private CoordinateReferenceSystem getCRSForGK(
        String x
    ) throws FactoryException {
        // Only one-digit zone numbers match the expected EPSG codes
        final int acceptedXLength = 7;
        if (x.split("\\.")[0].length() != acceptedXLength) {
            throw new FactoryException();
        }
        String zoneNumber = x.substring(0, 1);

        String epsgSuffix;
        switch (zoneNumber) {
            case "2": epsgSuffix = "6"; break;
            case "3": epsgSuffix = "7"; break;
            case "4": epsgSuffix = "8"; break;
            case "5": epsgSuffix = "9"; break;
            default: throw new FactoryException();
        }

        return CRS.decode("EPSG:3146" + epsgSuffix);
    }

    private CoordinateReferenceSystem getCRSForEd50Utm(
        String x
    ) throws FactoryException {
        String epsg = "EPSG:230";
        String part = x.split(",")[0];
        String zone = part.length() == 7 ? ("0" + part.substring(0, 1))
            : part.substring(0, 2);
        return CRS.decode(epsg + zone);
    }

    private String getEpsgForEd50UtmFromDegree(String x) {
        Double xCoord = Double.valueOf(x);
        String zone;
        if (xCoord <= -12 && xCoord > -18) {
            zone = "28";
        } else if (xCoord <= -6 && xCoord > -12) {
            zone = "29";
        } else if (xCoord <= 0 && xCoord > -6) {
            zone = "30";
        } else if (xCoord <= 6 && xCoord > 0) {
            zone = "31";
        } else if (xCoord <= 12 && xCoord > 6) {
            zone = "32";
        } else if (xCoord <= 18 && xCoord > 12) {
            zone = "33";
        } else if (xCoord <= 24 && xCoord > 18) {
            zone = "34";
        } else {
            return "";
        }

        return "EPSG:230" + zone;
    }

    /*
     * Get CRS 'ETRS89 / UTM zone <zone number>N' from easting with zone prefix.
     */
    private CoordinateReferenceSystem getCRSForEtrs89(
        String x
    ) throws FactoryException {
        String part = x.split("\\.")[0];
        String zone = part.length() == 7 ? ("0" + part.substring(0, 1))
            : part.substring(0, 2);
        return CRS.decode(EPSG_UTM_ETRS89_PREFIX + zone);
    }

    /*
     * Get EPSG code for CRS 'ETRS89 / UTM zone <zone number>N'
     * from geodetic coordinates for use with jtsTransform().
     * Does not guarantee to return a valid EPSG code.
     */
    private String getEtrsEpsg(double lon, double lat) {
        if (lat < 0) {
            // No CRS with ETRS89 available for the southern hemisphere
            throw new RuntimeException(
                String.format("Invalid negative latitude: %d", lat));
        }
        return EPSG_UTM_ETRS89_PREFIX + getUTMZone(lon);
    }

    /*
     * Get UTM zone for given longitude
     */
    private static int getUTMZone(double lon) {
        if (Math.abs(lon) > MAX_LON) {
            throw new IllegalArgumentException(
                String.format("Invalid lon value %d", lon));
        }
        return (int) Math.floor((lon + MAX_LON) / 6) + 1;
    }

    /*
     * Format UTM coordinates in Result o with zone prefix
     */
    private void formatUTM(Result o, int zone) {
        // Output is supposed to have "," as decimal separator
        DecimalFormat df = (DecimalFormat) NumberFormat
            .getNumberInstance(Locale.GERMAN);

        df.applyPattern(EASTING_PATTERN);
        o.setX(df.format((double) zone * ZONE_PREFIX_MULTIPLIER
                + Double.parseDouble(o.getX())));

        df.applyPattern(NORTHING_PATTERN);
        o.setY(df.format(Double.parseDouble(o.getY())));
    }
}
