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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.locationtech.jts.geom.Coordinate;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * Utilities for coordinate transformations.
 *
 */
public class KdaUtil {

    /* Represents coordinates in Gauß-Krüger CRS */
    public static final int KDA_GK = 1;

    /* Represents geodetic coordinates in sexagesimal notation */
    public static final int KDA_GS = 2;

    /* Represents geodetic coordinates in decimal notation */
    public static final int KDA_GD = 4;

    /* Represents coordinates in UTM CRS with WGS84 datum */
    public static final int KDA_UTM_WGS84 = 5;

    /* Represents coordinates in UTM CRS with ETRS89 datum */
    public static final int KDA_UTM_ETRS89 = 6;

    /* Represents coordinates in UTM CRS with ED50 datum (Hayford ellipsoid) */
    public static final int KDA_UTM_ED50 = 8;

    /* Expected format of projected input coordinates */
    private static final Pattern X_GK = Pattern.compile(
        "\\d{7,9}(\\.\\d*)?");
    private static final Pattern X_UTM = Pattern.compile(
        "\\d{7,8}(\\.\\d*)?");
    private static final Pattern Y = Pattern.compile(
        "(\\+|-)?\\d{1,7}(\\.\\d*)?");

    /* Expected format of sexagesimal input coordinates */
    // with decimal separator
    private static final Pattern LON_DEC = Pattern.compile(
        "([+|\\-|W|E]?)(\\d{1,3})(\\d{2})(\\d{2})\\.(\\d{1,5})([W|E]?)");
    private static final Pattern LAT_DEC = Pattern.compile(
        "([+|\\-|N|S]?)(\\d{1,2})(\\d{2})(\\d{2})\\.(\\d{1,5})([N|S]?)");
    // Without decimal separator, can include leading zeros
    private static final Pattern LON = Pattern.compile(
        "([+|\\-|W|E]?)(\\d{3})(\\d{0,2})(\\d{0,2})([W|E]?)");
    private static final Pattern LAT = Pattern.compile(
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
    private static final String NORTHING_PATTERN = "0.###";

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
     * @return Result with transformed coordinates
     */
    public Result transform(
        int kdaFrom, int kdaTo, String x, String y
    ) {
        if (x == null || y == null) {
            return null;
        }
        x = x.replace(',', '.');
        y = y.replace(',', '.');
        Transform t;
        try {
            switch (kdaFrom) {
                case KDA_GK: t = new Transform1(x, y); break;
                case KDA_GS: t = this.new Transform2(x, y); break;
                case KDA_GD: t = this.new Transform4(x, y); break;
                case KDA_UTM_WGS84: t = this.new Transform5(x, y); break;
                case KDA_UTM_ETRS89: t = this.new Transform6(x, y); break;
                case KDA_UTM_ED50: t = this.new Transform8(x, y); break;
                default: return null;
            }
        } catch (ValidationException | FactoryException fe) {
            return null;
        }
        return t.transform(kdaTo);
    }

    /**
     * Defines the methods to be implemented for coordinate transformation.
     */
    private interface Transform {
        void isInputValid() throws ValidationException;
        Result transform(int to);
        Result transformTo1();
        Result transformTo2();
        Result transformTo4();
        Result transformTo5();
        Result transformTo6();
        Result transformTo8();
    }

    /**
     * Exception to be thrown on invalid coordinate input.
     */
    private class ValidationException extends Exception { };

    /**
     * Delegates to a class per input KDA.
     */
    private abstract class AbstractTransform implements Transform {
        // Input coordinates
        protected String x;
        protected String y;

        // CRS of input coordinates
        protected CoordinateReferenceSystem crs;

        AbstractTransform(String x, String y) throws ValidationException {
            this.x = x;
            this.y = y;
            isInputValid();
        }

        public Result transform(int to) {
            switch (to) {
                case KDA_GK: return transformTo1();
                case KDA_GS: return transformTo2();
                case KDA_GD: return transformTo4();
                case KDA_UTM_WGS84: return transformTo5();
                case KDA_UTM_ETRS89: return transformTo6();
                case KDA_UTM_ED50: return transformTo8();
                default: return null;
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
        ) throws ValidationException, FactoryException {
            super(x, y);
            this.crs = getCRSForGK(x);
        }

        @Override
        public void isInputValid() throws ValidationException {
            if (!(X_GK.matcher(x).matches() && Y.matcher(y).matches())) {
                throw new ValidationException();
            }
        }

        @Override
        public Result transformTo1() {
            return new Result(x, y);
        }

        @Override
        public Result transformTo2() {
            Result degrees = jtsTransform(crs, "EPSG:4326", y, x);
            if (degrees == null) {
                return null;
            }
            return degreeToArc(degrees.getY(), degrees.getX());
        }

        @Override
        public Result transformTo4() {
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
        public Result transformTo5() {
            Result degrees = jtsTransform(crs, "EPSG:4326", y, x);
            String epsgWGS = getWgsUtmEpsg(
                Double.parseDouble(degrees.getY()),
                Double.parseDouble(degrees.getX()));
            Result coord = jtsTransform(crs,
                epsgWGS,
                y,
                x);
            if (coord == null) {
                return null;
            }
            coord.setX(epsgWGS.substring(
                epsgWGS.length() - 2,
                epsgWGS.length()) + coord.getX());
            String coordX = coord.getX();
            String coordY = coord.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coord.setX(coordX);
            coord.setY(coordY);
            return coord;
        }

        @Override
        public Result transformTo6() {
            Result degrees = jtsTransform(crs, "EPSG:4326", y, x);
            // TODO: explain why x and y are interchanged here
            String epsgEtrs = getEtrsEpsg(
                Double.parseDouble(degrees.getY()),
                Double.parseDouble(degrees.getX()));
            Result coord = jtsTransform(crs,
                epsgEtrs,
                y,
                x);
            if (coord == null) {
                return null;
            }
            coord.setX(epsgEtrs.substring(
                    epsgEtrs.length() - 2,
                    epsgEtrs.length()) + coord.getX());
            String coordX = coord.getX();
            String coordY = coord.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coord.setX(coordX);
            coord.setY(coordY);
            return coord;
        }

        @Override
        public Result transformTo8() {
            Result degrees = jtsTransform(crs, "EPSG:4326", y, x);
            String epsgEd50 = getEpsgForEd50UtmFromDegree(
                degrees.getY());
            Result coord = jtsTransform(crs, epsgEd50, y, x);
            if (coord == null) {
                return null;
            }
            String coordX = coord.getX();
            String coordY = coord.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            String zone = epsgEd50.substring(
                epsgEd50.length() - 2, epsgEd50.length());
            coord.setX(zone + coordX);
            coord.setY(coordY);
            return coord;
        }
    }

    /**
     * Implements coordinate transformations for sexagesimal geodetic input.
     */
    private class Transform2 extends AbstractTransform {

        Transform2(String x, String y) throws ValidationException {
            super(x, y);
        }

        @Override
        public void isInputValid() throws ValidationException {
            if (!(LON_DEC.matcher(x).matches()
                    && LAT_DEC.matcher(y).matches()
                    || LON.matcher(x).matches()
                    && LAT.matcher(y).matches())) {
                throw new ValidationException();
            }
        }

        @Override
        public Result transformTo1() {
            Result degrees = arcToDegree(x, y);
            if (degrees == null) {
                return null;
            }
            String epsgGk = getGkEpsg(
                Double.parseDouble(degrees.getX()),
                Double.parseDouble(degrees.getY()));

            Result coord = jtsTransform(
                "EPSG:4326",
                epsgGk,
                degrees.getY(),
                degrees.getX());
            if (coord == null) {
                return null;
            }
            String coordX = coord.getX();
            String coordY = coord.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 2 ? maxLenX : 2;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 2 ? maxLenY : 2;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coord.setX(coordY);
            coord.setY(coordX);
            return coord;
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
        public Result transformTo5() {
            Result degrees = arcToDegree(x, y);
            if (degrees == null) {
                return null;
            }
            String epsgWgs = getWgsUtmEpsg(
                Double.parseDouble(degrees.getX()),
                Double.parseDouble(degrees.getY()));
            Result coord = jtsTransform("EPSG:4326",
                epsgWgs,
                degrees.getY(),
                degrees.getX());
            if (coord == null) {
                return null;
            }
            coord.setX(epsgWgs.substring(
                epsgWgs.length() - 2,
                epsgWgs.length()) + coord.getX());
            String coordX = coord.getX();
            String coordY = coord.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coord.setX(coordX);
            coord.setY(coordY);
            return coord;
        }

        @Override
        public Result transformTo6() {
            Result degrees = arcToDegree(x, y);
            if (degrees == null) {
                return null;
            }
            String epsgEtrs = getEtrsEpsg(
                Double.parseDouble(degrees.getX()),
                Double.parseDouble(degrees.getY()));
            Result coord = jtsTransform("EPSG:4326",
                epsgEtrs,
                degrees.getY(),
                degrees.getX());
            if (coord == null) {
                return null;
            }
            coord.setX(epsgEtrs.substring(
                    epsgEtrs.length() - 2,
                    epsgEtrs.length()) + coord.getX());
            String coordX = coord.getX();
            String coordY = coord.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coord.setX(coordX);
            coord.setY(coordY);
            return coord;
        }

        @Override
        public Result transformTo8() {
            Result degrees = arcToDegree(x, y);
            if (degrees == null) {
                return null;
            }
            String epsgEd50 = getEpsgForEd50UtmFromDegree(
                degrees.getX());
            Result coord = jtsTransform("EPSG:4326",
                epsgEd50,
                degrees.getY(),
                degrees.getX());
            if (coord == null) {
                return null;
            }
            String coordX = coord.getX();
            String coordY = coord.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            String zone = epsgEd50.substring(
                epsgEd50.length() - 2, epsgEd50.length());
            coord.setX(zone + coordX);
            coord.setY(coordY);
            return coord;
        }
    }

    /**
     * Implements coordinate transformations for decimal geodetic input.
     */
    private class Transform4 extends AbstractTransform {

        Transform4(String x, String y) throws ValidationException {
            super(x, y);
        }

        @Override
        public void isInputValid() throws ValidationException {
            final double maxLon = 180, maxLat = 90;
            try {
                double dX = Double.parseDouble(x), dY = Double.parseDouble(y);
                if (dX < 0 || dX > maxLon || dY < 0 || dY > maxLat) {
                    throw new ValidationException();
                }
            } catch (NumberFormatException nfe) {
                throw new ValidationException();
            }
        }

        @Override
        public Result transformTo1() {
            String epsgGk = getGkEpsg(Double.valueOf(x), Double.valueOf(y));
            Result coord = jtsTransform("EPSG:4326", epsgGk, y, x);
            if (coord == null) {
                return null;
            }
            String coordX = coord.getX();
            String coordY = coord.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coord.setX(coordY);
            coord.setY(coordX);
            return coord;
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
        public Result transformTo5() {
            String epsgWgs = getWgsUtmEpsg(
                Double.valueOf(x), Double.valueOf(y));
            Result coord = jtsTransform("EPSG:4326", epsgWgs, y, x);
            if (coord == null) {
                return null;
            }
            coord.setX(epsgWgs.substring(
                epsgWgs.length() - 2,
                epsgWgs.length()) + coord.getX());
            String coordX = coord.getX();
            String coordY = coord.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coord.setX(coordX);
            coord.setY(coordY);
            return coord;
        }

        @Override
        public Result transformTo6() {
            String epsgEtrs = getEtrsEpsg(
                Double.valueOf(x), Double.valueOf(y));
            Result coord = jtsTransform("EPSG:4326", epsgEtrs, y, x);
            if (coord == null) {
                return null;
            }
            coord.setX(epsgEtrs.substring(
                    epsgEtrs.length() - 2,
                    epsgEtrs.length()) + coord.getX());
            String coordX = coord.getX();
            String coordY = coord.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coord.setX(coordX);
            coord.setY(coordY);
            return coord;
        }

        @Override
        public Result transformTo8() {
            String epsgEd50 = getEpsgForEd50UtmFromDegree(x);
            Result coord = jtsTransform("EPSG:4326", epsgEd50, y, x);
            if (coord == null) {
                return null;
            }
            String coordX = coord.getX();
            String coordY = coord.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            String zone = epsgEd50.substring(
                epsgEd50.length() - 2, epsgEd50.length());
            coord.setX(zone + coordX);
            coord.setY(coordY);
            return coord;
        }
    }

    /**
     * Implements coordinate transformations for UTM-WGS84 input.
     */
    private class Transform5 extends AbstractTransform {
        Transform5(
            String x,
            String y
        ) throws ValidationException, FactoryException {
            super(x, y);
            this.crs = getCRSForWgsUtm(x);
        }

        @Override
        public void isInputValid() throws ValidationException {
            if (!(X_UTM.matcher(x).matches() && Y.matcher(y).matches())) {
                throw new ValidationException();
            }
        }

        @Override
        public Result transformTo1() {
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
            String coordX = coords.getX();
            String coordY = coords.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 2 ? maxLenX : 2;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 2 ? maxLenY : 2;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coords.setX(coordY);
            coords.setY(coordX);
            return coords;
        }

        @Override
        public Result transformTo2() {
            x = x.substring(2, x.length());
            Result degrees = jtsTransform(crs, "EPSG:4326", x, y);
            Result coords = degreeToArc(degrees.getY(), degrees.getX());
            return coords;
        }

        @Override
        public Result transformTo4() {
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
        public Result transformTo6() {
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
        public Result transformTo8() {
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
            String coordX = coords.getX();
            String coordY = coords.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 7 ? maxLenX : 7;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 7 ? maxLenY : 7;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
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
        ) throws ValidationException, FactoryException {
            super(x, y);
            this.crs = getCRSForEtrs89(x);
        }

        @Override
        public void isInputValid() throws ValidationException {
            if (!(X_UTM.matcher(x).matches() && Y.matcher(y).matches())) {
                throw new ValidationException();
            }
        }

        @Override
        public Result transformTo1() {
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
            String coordX = coords.getX();
            String coordY = coords.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 2 ? maxLenX : 2;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 2 ? maxLenY : 2;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coords.setX(coordY);
            coords.setY(coordX);
            return coords;
        }

        @Override
        public Result transformTo2() {
            x = x.substring(2, x.length());
            Result degrees = jtsTransform(crs, "EPSG:4326", x, y);
            Result coords = degreeToArc(degrees.getY(), degrees.getX());
            return coords;
        }

        @Override
        public Result transformTo4() {
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
        public Result transformTo8() {
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
            String coordX = coords.getX();
            String coordY = coords.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 7 ? maxLenX : 7;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 7 ? maxLenY : 7;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
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
        ) throws ValidationException, FactoryException {
            super(x, y);
            this.crs = getCRSForEd50Utm(x);
        }

        @Override
        public void isInputValid() throws ValidationException {
            if (!(X_UTM.matcher(x).matches() && Y.matcher(y).matches())) {
                throw new ValidationException();
            }
        }

        @Override
        public Result transformTo1() {
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
            String coordX = coords.getX();
            String coordY = coords.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 2 ? maxLenX : 2;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 2 ? maxLenY : 2;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coords.setX(coordY);
            coords.setY(coordX);
            return coords;
        }

        @Override
        public Result transformTo2() {
            x = x.substring(2, x.length());
            Result degrees = jtsTransform(crs, "EPSG:4326", x, y);
            Result coords = degreeToArc(degrees.getY(), degrees.getX());
            return coords;
        }

        @Override
        public Result transformTo4() {
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
            String coordX = coords.getX();
            String coordY = coords.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 7 ? maxLenX : 7;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 7 ? maxLenY : 7;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            String zone = epsgWgs.substring(
                epsgWgs.length() - 2, epsgWgs.length());
            coords.setX(zone + coordX);
            coords.setY(coordY);
            return coords;
        }

        @Override
        public Result transformTo6() {
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
            String coordX = coords.getX();
            String coordY = coords.getY();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 7 ? maxLenX : 7;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 7 ? maxLenY : 7;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
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
     */
    private Result jtsTransform(
        String epsgFrom,
        String epsgTo,
        String x,
        String y
    ) {
        CoordinateReferenceSystem src;
        try {
            src = CRS.decode(epsgFrom);
        } catch (FactoryException fe) {
            return null;
        }
        return jtsTransform(src, epsgTo, x, y);
    }

    /**
     * Transform given coordinates from CRS to epsgTo.
     * Returns null in case the given EPSG code is invalid.
     */
    private Result jtsTransform(
        CoordinateReferenceSystem src,
        String epsgTo,
        String x,
        String y
    ) {
        try {
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
        } catch (FactoryException | TransformException e) {
            return null;
        }
    }

    private Result degreeToArc(String x, String y) {
        String[] xParts = x.split("\\.");
        String[] yParts = y.split("\\.");

        // Convert fractions of degrees to arc seconds
        final double secondsPerDegree = 3600;
        double wsX = 0;
        double wsY = 0;
        try {
            if (xParts.length == 2) {
                wsX = Double.parseDouble("0." + xParts[1]) * secondsPerDegree;
            }
            if (yParts.length == 2) {
                wsY = Double.parseDouble("0." + yParts[1]) * secondsPerDegree;
            }
        } catch (NumberFormatException nfe) {
            return null;
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
    protected Result arcToDegree(String x, String y) {
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
        try {
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
        } catch (IllegalStateException e) {
            return null;
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
        Double xCoord;
        try {
            xCoord = Double.valueOf(x);
        } catch (NumberFormatException nfe) {
            return "";
        }
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
        Double xCoord;
        try {
            xCoord = Double.valueOf(x);
        } catch (NumberFormatException nfe) {
            return "";
        }
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
            return "";
        }
        return EPSG_UTM_ETRS89_PREFIX + getUTMZone(lon);
    }

    /*
     * Get UTM zone for given longitude
     */
    private static int getUTMZone(double lon) {
        return (int) Math.floor((lon + 180) / 6) + 1;
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
