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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.locationtech.jts.geom.Coordinate;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * Utilities for kda transformations
 *
 */
public class KdaUtil {

    /* Represents coordinates in Gauß-Krüger CRS */
    public static final int KDA_GK = 1;

    /* Represents geodetic coordinates in decimal notation */
    public static final int KDA_GD = 4;

    /* Represents coordinates in UTM CRS with WGS84 datum */
    public static final int KDA_UTM_WGS84 = 5;

    /* Represents coordinates in UTM CRS with ETRS89 datum */
    public static final int KDA_UTM_ETRS89 = 6;

    /* Represents coordinates in UTM CRS with ED50 datum (Hayford ellipsoid) */
    public static final int KDA_UTM_ED50 = 8;

    /*
     * UTM zone number with given prefix gives the EPSG code for CRS
     * 'ETRS89 / UTM zone <zone number>N'
     */
    private static final String EPSG_UTM_ETRS89_PREFIX = "EPSG:258";

    /*
     * If eastings should be prefixed with a zone number, multiply zone
     * number with this value and add it to the easting
     */
    private static final double ZONE_PREFIX_MULTIPLIER = 1e6;

    /*
     * DecimalFormat pattern for eastings including zone prefix
     */
    private static final String EASTING_PATTERN = "0000000.###";

    /*
     * DecimalFormat pattern for northings
     */
    private static final String NORTHING_PATTERN = "0.###";


    ObjectMapper builder;
    public ObjectNode transform(int kdaFrom, int kdaTo, String x, String y) {
        x = x.replace(',', '.');
        y = y.replace(',', '.');
        builder = new ObjectMapper();
        Transform t;
        switch (kdaFrom) {
            case KDA_GK: t = this.new Transform1(); break;
            case 2: t = this.new Transform2(); break;
            case KDA_GD: t = this.new Transform4(); break;
            case KDA_UTM_WGS84: t = this.new Transform5(); break;
            case KDA_UTM_ETRS89: t = this.new Transform6(); break;
            case KDA_UTM_ED50: t = this.new Transform8(); break;
            default: return null;
        }
        return t.transform(kdaTo, x, y);
    }

    private interface Transform {
        ObjectNode transform(int to, String x, String y);
        ObjectNode transformTo1(String x, String y);
        ObjectNode transformTo2(String x, String y);
        ObjectNode transformTo4(String x, String y);
        ObjectNode transformTo5(String x, String y);
        ObjectNode transformTo6(String x, String y);
        ObjectNode transformTo8(String x, String y);
    }

    private abstract class AbstractTransform implements Transform {
        public ObjectNode transform(int to, String x, String y) {
            switch (to) {
                case KDA_GK: return transformTo1(x, y);
                case 2: return transformTo2(x, y);
                case KDA_GD: return transformTo4(x, y);
                case KDA_UTM_WGS84: return transformTo5(x, y);
                case KDA_UTM_ETRS89: return transformTo6(x, y);
                case KDA_UTM_ED50: return transformTo8(x, y);
                default: return null;
            }
        }
    }

    private class Transform1 extends AbstractTransform {
        @Override
        public ObjectNode transformTo1(String x, String y) {
            ObjectNode response = builder.createObjectNode();
            response.put("x", x);
            response.put("y", y);
            return response;
        }

        @Override
        public ObjectNode transformTo2(String x, String y) {
            String epsg = getEpsgForGK(x);
            ObjectNode degrees = jtsTransform(epsg, "EPSG:4326", y, x);
            if (degrees == null) {
                return null;
            }
            return degreeToArc(
                degrees.get("y").asText(),
                degrees.get("x").asText());
        }

        @Override
        public ObjectNode transformTo4(String x, String y) {
            String epsg = getEpsgForGK(x);
            ObjectNode coords = jtsTransform(epsg, "EPSG:4326", y, x);
            if (coords == null) {
                return null;
            }
            String coordX = coords.get("x").asText();
            String coordY = coords.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 7 ? maxLenX : 7;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 7 ? maxLenY : 7;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coords.put("x", coordY);
            coords.put("y", coordX);
            return coords;
        }

        @Override
        public ObjectNode transformTo5(String x, String y) {
            String epsgGK = getEpsgForGK(x);
            ObjectNode degrees = jtsTransform(epsgGK, "EPSG:4326", y, x);
            String epsgWGS = getWgsUtmEpsg(
                degrees.get("y").asDouble(),
                degrees.get("x").asDouble());
            ObjectNode coord = jtsTransform(epsgGK,
                epsgWGS,
                y,
                x);
            if (coord == null) {
                return null;
            }
            coord.put("x", epsgWGS.substring(
                epsgWGS.length() - 2,
                epsgWGS.length()) + coord.get("x").asText());
            String coordX = coord.get("x").asText();
            String coordY = coord.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coord.put("x", coordX);
            coord.put("y", coordY);
            return coord;
        }

        @Override
        public ObjectNode transformTo6(String x, String y) {
            String epsgGK = getEpsgForGK(x);
            ObjectNode degrees = jtsTransform(epsgGK, "EPSG:4326", y, x);
            // TODO: explain why x and y are interchanged here
            String epsgEtrs = getEtrsEpsg(
                degrees.get("y").asDouble(),
                degrees.get("x").asDouble());
            ObjectNode coord = jtsTransform(epsgGK,
                epsgEtrs,
                y,
                x);
            if (coord == null) {
                return null;
            }
            coord.put("x", epsgEtrs.substring(
                    epsgEtrs.length() - 2,
                    epsgEtrs.length()) + coord.get("x").asText());
            String coordX = coord.get("x").asText();
            String coordY = coord.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coord.put("x", coordX);
            coord.put("y", coordY);
            return coord;
        }

        @Override
        public ObjectNode transformTo8(String x, String y) {
            String epsgGK = getEpsgForGK(x);
            ObjectNode degrees = jtsTransform(epsgGK, "EPSG:4326", y, x);
            String epsgEd50 = getEpsgForEd50UtmFromDegree(
                degrees.get("y").asText());
            ObjectNode coord = jtsTransform(epsgGK, epsgEd50, y, x);
            if (coord == null) {
                return null;
            }
            String coordX = coord.get("x").asText();
            String coordY = coord.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            String zone = epsgEd50.substring(
                epsgEd50.length() - 2, epsgEd50.length());
            coord.put("x", zone + coordX);
            coord.put("y", coordY);
            return coord;
        }
    }

    private class Transform2 extends AbstractTransform {
        @Override
        public ObjectNode transformTo1(String x, String y) {
            ObjectNode degrees = arcToDegree(x, y);
            if (degrees == null) {
                return null;
            }
            String epsgGk = getGkEpsg(
                degrees.get("x").asDouble(),
                degrees.get("y").asDouble());

            ObjectNode coord = jtsTransform(
                "EPSG:4326",
                epsgGk,
                degrees.get("y").asText(),
                degrees.get("x").asText());
            if (coord == null) {
                return null;
            }
            String coordX = coord.get("x").asText();
            String coordY = coord.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 2 ? maxLenX : 2;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 2 ? maxLenY : 2;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coord.put("x", coordY);
            coord.put("y", coordX);
            return coord;
        }

        @Override
        public ObjectNode transformTo2(String x, String y) {
            ObjectNode response = builder.createObjectNode();
            response.put("x", x);
            response.put("y", y);
            return response;
        }

        @Override
        public ObjectNode transformTo4(String x, String y) {
            return arcToDegree(x, y);
        }

        @Override
        public ObjectNode transformTo5(String x, String y) {
            ObjectNode degrees = arcToDegree(x, y);
            if (degrees == null) {
                return null;
            }
            String epsgWgs = getWgsUtmEpsg(
                degrees.get("x").asDouble(),
                degrees.get("y").asDouble());
            ObjectNode coord = jtsTransform("EPSG:4326",
                epsgWgs,
                degrees.get("y").asText(),
                degrees.get("x").asText());
            if (coord == null) {
                return null;
            }
            coord.put("x", epsgWgs.substring(
                epsgWgs.length() - 2,
                epsgWgs.length()) + coord.get("x").asText());
            String coordX = coord.get("x").asText();
            String coordY = coord.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coord.put("x", coordX);
            coord.put("y", coordY);
            return coord;
        }

        @Override
        public ObjectNode transformTo6(String x, String y) {
            ObjectNode degrees = arcToDegree(x, y);
            if (degrees == null) {
                return null;
            }
            String epsgEtrs = getEtrsEpsg(
                degrees.get("x").asDouble(),
                degrees.get("y").asDouble());
            ObjectNode coord = jtsTransform("EPSG:4326",
                epsgEtrs,
                degrees.get("y").asText(),
                degrees.get("x").asText());
            if (coord == null) {
                return null;
            }
            coord.put("x", epsgEtrs.substring(
                    epsgEtrs.length() - 2,
                    epsgEtrs.length()) + coord.get("x").asText());
            String coordX = coord.get("x").asText();
            String coordY = coord.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coord.put("x", coordX);
            coord.put("y", coordY);
            return coord;
        }

        @Override
        public ObjectNode transformTo8(String x, String y) {
            ObjectNode degrees = arcToDegree(x, y);
            if (degrees == null) {
                return null;
            }
            String epsgEd50 = getEpsgForEd50UtmFromDegree(
                degrees.get("x").asText());
            ObjectNode coord = jtsTransform("EPSG:4326",
                epsgEd50,
                degrees.get("y").asText(),
                degrees.get("x").asText());
            if (coord == null) {
                return null;
            }
            String coordX = coord.get("x").asText();
            String coordY = coord.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            String zone = epsgEd50.substring(
                epsgEd50.length() - 2, epsgEd50.length());
            coord.put("x", zone + coordX);
            coord.put("y", coordY);
            return coord;
        }
    }

    private class Transform4 extends AbstractTransform {

        @Override
        public ObjectNode transformTo1(String x, String y) {
            x = x.replaceAll(",", ".");
            y = y.replaceAll(",", ".");
            String epsgGk = getGkEpsg(Double.valueOf(x), Double.valueOf(y));
            ObjectNode coord = jtsTransform("EPSG:4326", epsgGk, y, x);
            if (coord == null) {
                return null;
            }
            String coordX = coord.get("x").asText();
            String coordY = coord.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coord.put("x", coordY);
            coord.put("y", coordX);
            return coord;
        }

        @Override
        public ObjectNode transformTo2(String x, String y) {
            return degreeToArc(x, y);
        }

        @Override
        public ObjectNode transformTo4(String x, String y) {
            ObjectNode response = builder.createObjectNode();
            response.put("x", x);
            response.put("y", y);
            return response;
        }

        @Override
        public ObjectNode transformTo5(String x, String y) {
            x = x.replaceAll(",", ".");
            y = y.replaceAll(",", ".");
            String epsgWgs = getWgsUtmEpsg(
                Double.valueOf(x), Double.valueOf(y));
            ObjectNode coord = jtsTransform("EPSG:4326", epsgWgs, y, x);
            if (coord == null) {
                return null;
            }
            coord.put("x", epsgWgs.substring(
                epsgWgs.length() - 2,
                epsgWgs.length()) + coord.get("x").asText());
            String coordX = coord.get("x").asText();
            String coordY = coord.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coord.put("x", coordX);
            coord.put("y", coordY);
            return coord;
        }

        @Override
        public ObjectNode transformTo6(String x, String y) {
            x = x.replaceAll(",", ".");
            y = y.replaceAll(",", ".");
            String epsgEtrs = getEtrsEpsg(
                Double.valueOf(x), Double.valueOf(y));
            ObjectNode coord = jtsTransform("EPSG:4326", epsgEtrs, y, x);
            if (coord == null) {
                return null;
            }
            coord.put("x", epsgEtrs.substring(
                    epsgEtrs.length() - 2,
                    epsgEtrs.length()) + coord.get("x").asText());
            String coordX = coord.get("x").asText();
            String coordY = coord.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coord.put("x", coordX);
            coord.put("y", coordY);
            return coord;
        }

        @Override
        public ObjectNode transformTo8(String x, String y) {
            String epsgEd50 = getEpsgForEd50UtmFromDegree(x);
            ObjectNode coord = jtsTransform("EPSG:4326", epsgEd50, y, x);
            if (coord == null) {
                return null;
            }
            String coordX = coord.get("x").asText();
            String coordY = coord.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 3 ? maxLenX : 3;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 3 ? maxLenY : 3;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            String zone = epsgEd50.substring(
                epsgEd50.length() - 2, epsgEd50.length());
            coord.put("x", zone + coordX);
            coord.put("y", coordY);
            return coord;
        }
    }

    private class Transform5 extends AbstractTransform {

        @Override
        public ObjectNode transformTo1(String x, String y) {
            String epsgWgs = getEpsgForWgsUtm(x);
            x = x.substring(2, x.length());
            ObjectNode degrees = jtsTransform(epsgWgs, "EPSG:4326", x, y);
            if (degrees == null) {
                return null;
            }
            String epsgGk = getGkEpsg(
                degrees.get("y").asDouble(),
                degrees.get("x").asDouble());
            ObjectNode coords = jtsTransform(epsgWgs, epsgGk, x, y);
            if (coords == null) {
                return null;
            }
            String coordX = coords.get("x").asText();
            String coordY = coords.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 2 ? maxLenX : 2;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 2 ? maxLenY : 2;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coords.put("x", coordY);
            coords.put("y", coordX);
            return coords;
        }

        @Override
        public ObjectNode transformTo2(String x, String y) {
            String epsgWgs = getEpsgForWgsUtm(x);
            x = x.substring(2, x.length());
            ObjectNode degrees = jtsTransform(epsgWgs, "EPSG:4326", x, y);
            ObjectNode coords = degreeToArc(
                degrees.get("y").asText(),
                degrees.get("x").asText());
            return coords;
        }

        @Override
        public ObjectNode transformTo4(String x, String y) {
            String epsgWgs = getEpsgForWgsUtm(x);
            x = x.substring(2, x.length());
            ObjectNode coords = jtsTransform(epsgWgs, "EPSG:4326", x, y);
            if (coords == null) {
                return null;
            }
            String coordX = coords.get("x").asText();
            String coordY = coords.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 7 ? maxLenX : 7;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 7 ? maxLenY : 7;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coords.put("x", coordY);
            coords.put("y", coordX);
            return coords;
        }

        @Override
        public ObjectNode transformTo5(String x, String y) {
            ObjectNode response = builder.createObjectNode();
            response.put("x", x);
            response.put("y", y);
            return response;
        }

        @Override
        public ObjectNode transformTo6(String x, String y) {
            String epsgWgs = getEpsgForWgsUtm(x);
            x = x.substring(2, x.length());
            ObjectNode degrees = jtsTransform(epsgWgs, "EPSG:4326", x, y);
            if (degrees == null) {
                return null;
            }
            String epsgEtrs = getEtrsEpsg(
                degrees.get("y").asDouble(),
                degrees.get("x").asDouble());
            ObjectNode response = jtsTransform(epsgWgs, epsgEtrs, x, y);
            if (response == null) {
                return response;
            }

            // Format output
            int zone = getUTMZone(
                degrees.get("y").asDouble(),
                degrees.get("x").asDouble());
            formatUTM(response, zone);
            return response;
        }

        @Override
        public ObjectNode transformTo8(String x, String y) {
            String epsgWgs = getEpsgForWgsUtm(x);
            x = x.substring(2, x.length());
            ObjectNode coords4326 = jtsTransform(epsgWgs, "EPSG:4326", x, y);
            if (coords4326 == null) {
                return null;
            }
            String epsgEd50 =
                getEpsgForEd50UtmFromDegree(coords4326.get("y").asText());
            ObjectNode coords = jtsTransform(epsgWgs, epsgEd50, x, y);
            if (coords == null) {
                return null;
            }
            String coordX = coords.get("x").asText();
            String coordY = coords.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 7 ? maxLenX : 7;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 7 ? maxLenY : 7;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            String zone = epsgEd50.substring(
                epsgEd50.length() - 2, epsgEd50.length());
            coords.put("x", zone + coordX);
            coords.put("y", coordY);
            return coords;
        }
    }

    private class Transform6 extends AbstractTransform {

        @Override
        public ObjectNode transformTo1(String x, String y) {
            String epsgEtrs = getEpsgForEtrs89(x);
            x = x.substring(2, x.length());
            ObjectNode degrees = jtsTransform(epsgEtrs, "EPSG:4326", x, y);
            if (degrees == null) {
                return null;
            }
            String epsgGk = getGkEpsg(
                degrees.get("y").asDouble(),
                degrees.get("x").asDouble());
            ObjectNode coords = jtsTransform(epsgEtrs, epsgGk, x, y);
            if (coords == null) {
                return null;
            }
            String coordX = coords.get("x").asText();
            String coordY = coords.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 2 ? maxLenX : 2;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 2 ? maxLenY : 2;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coords.put("x", coordY);
            coords.put("y", coordX);
            return coords;
        }

        @Override
        public ObjectNode transformTo2(String x, String y) {
            String epsgEtrs = getEpsgForEtrs89(x);
            x = x.substring(2, x.length());
            ObjectNode degrees = jtsTransform(epsgEtrs, "EPSG:4326", x, y);
            ObjectNode coords = degreeToArc(
                degrees.get("y").asText(),
                degrees.get("x").asText());
            return coords;
        }

        @Override
        public ObjectNode transformTo4(String x, String y) {
            String epsgEtrs = getEpsgForEtrs89(x);
            x = x.substring(2, x.length());
            ObjectNode coords = jtsTransform(epsgEtrs, "EPSG:4326", x, y);
            if (coords == null) {
                return null;
            }
            String coordX = coords.get("x").asText();
            String coordY = coords.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 7 ? maxLenX : 7;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 7 ? maxLenY : 7;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coords.put("x", coordY);
            coords.put("y", coordX);
            return coords;
        }

        @Override
        public ObjectNode transformTo5(String x, String y) {
            String epsgEtrs = getEpsgForEtrs89(x);
            x = x.substring(2, x.length());
            ObjectNode degrees = jtsTransform(epsgEtrs, "EPSG:4326", x, y);
            if (degrees == null) {
                return null;
            }
            String epsgWgs = getWgsUtmEpsg(
                degrees.get("y").asDouble(),
                degrees.get("x").asDouble());
            ObjectNode response = jtsTransform(epsgEtrs, epsgWgs, x, y);
            if (response == null) {
                return null;
            }

            // Format output
            int zone = getUTMZone(
                degrees.get("y").asDouble(),
                degrees.get("x").asDouble());
            formatUTM(response, zone);
            return response;
        }

        @Override
        public ObjectNode transformTo6(String x, String y) {
            ObjectNode response = builder.createObjectNode();
            response.put("x", x);
            response.put("y", y);
            return response;
        }

        @Override
        public ObjectNode transformTo8(String x, String y) {
            String epsgEtrs = getEpsgForEtrs89(x);
            x = x.substring(2, x.length());
            ObjectNode coords4326 = jtsTransform(epsgEtrs, "EPSG:4326", x, y);
            if (coords4326 == null) {
                return null;
            }
            String epsgEd50 = getEpsgForEd50UtmFromDegree(
                coords4326.get("y").asText());
            ObjectNode coords = jtsTransform(epsgEtrs, epsgEd50, x, y);
            if (coords == null) {
                return null;
            }
            String coordX = coords.get("x").asText();
            String coordY = coords.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 7 ? maxLenX : 7;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 7 ? maxLenY : 7;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            String zone = epsgEd50.substring(
                epsgEd50.length() - 2, epsgEd50.length());
            coords.put("x", zone + coordX);
            coords.put("y", coordY);
            return coords;
        }
    }

    private class Transform8 extends AbstractTransform {

        @Override
        public ObjectNode transformTo1(String x, String y) {
            String epsgEd50 = getEpsgForEd50Utm(x);
            x = x.substring(2, x.length());
            ObjectNode degrees = jtsTransform(epsgEd50, "EPSG:4326", x, y);
            if (degrees == null) {
                return null;
            }
            String epsgGk = getGkEpsg(
                degrees.get("y").asDouble(),
                degrees.get("x").asDouble());
            ObjectNode coords = jtsTransform(epsgEd50, epsgGk, x, y);
            if (coords == null) {
                return null;
            }
            String coordX = coords.get("x").asText();
            String coordY = coords.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 2 ? maxLenX : 2;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 2 ? maxLenY : 2;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coords.put("x", coordY);
            coords.put("y", coordX);
            return coords;
        }

        @Override
        public ObjectNode transformTo2(String x, String y) {
            String epsgWgs = getEpsgForEd50Utm(x);
            x = x.substring(2, x.length());
            ObjectNode degrees = jtsTransform(epsgWgs, "EPSG:4326", x, y);
            ObjectNode coords = degreeToArc(
                degrees.get("y").asText(),
                degrees.get("x").asText());
            return coords;
        }

        @Override
        public ObjectNode transformTo4(String x, String y) {
            String epsgEd50 = getEpsgForEd50Utm(x);
            x = x.substring(2, x.length());
            ObjectNode coords = jtsTransform(epsgEd50, "EPSG:4326", x, y);
            if (coords == null) {
                return null;
            }
            String coordX = coords.get("x").asText();
            String coordY = coords.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 7 ? maxLenX : 7;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 7 ? maxLenY : 7;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            coords.put("x", coordY);
            coords.put("y", coordX);
            return coords;
        }

        @Override
        public ObjectNode transformTo5(String x, String y) {
            String epsgEd50 = getEpsgForEd50Utm(x);
            if (epsgEd50.equals("")) {
                return null;
            }
            String x1 = x.substring(2, x.length());
            ObjectNode coords4326 = jtsTransform(epsgEd50, "EPSG:4326", x1, y);
            if (coords4326 == null) {
                return null;
            }
            String epsgWgs = getEpsgForWgsUtmFromDegree(
                coords4326.get("y").asText());
            ObjectNode coords = jtsTransform(epsgEd50, epsgWgs, x1, y);
            if (coords == null) {
                return null;
            }
            String coordX = coords.get("x").asText();
            String coordY = coords.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 7 ? maxLenX : 7;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 7 ? maxLenY : 7;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            String zone = epsgWgs.substring(
                epsgWgs.length() - 2, epsgWgs.length());
            coords.put("x", zone + coordX);
            coords.put("y", coordY);
            return coords;
        }

        @Override
        public ObjectNode transformTo6(String x, String y) {
            String epsgEd50 = getEpsgForEd50Utm(x);
            if (epsgEd50.equals("")) {
                return null;
            }
            String x1 = x.substring(2, x.length());
            ObjectNode coords4326 = jtsTransform(epsgEd50, "EPSG:4326", x1, y);
            if (coords4326 == null) {
                return null;
            }
            // TODO: explain why x and y are interchanged here
            String epsgEtrs = getEtrsEpsg(
                coords4326.get("y").asDouble(),
                coords4326.get("x").asDouble());
            ObjectNode coords = jtsTransform(epsgEd50, epsgEtrs, x1, y);
            if (coords == null) {
                return null;
            }
            String coordX = coords.get("x").asText();
            String coordY = coords.get("y").asText();
            int maxLenX = coordX.length() - coordX.indexOf(".");
            int precX = maxLenX < 7 ? maxLenX : 7;
            int maxLenY = coordY.length() - coordY.indexOf(".");
            int precY = maxLenY < 7 ? maxLenY : 7;
            coordX = coordX.substring(0, coordX.indexOf(".") + precX);
            coordY = coordY.substring(0, coordY.indexOf(".") + precY);
            String zone = epsgEtrs.substring(
                epsgEtrs.length() - 2, epsgEtrs.length());
            coords.put("x", zone + coordX);
            coords.put("y", coordY);
            return coords;
        }

        @Override
        public ObjectNode transformTo8(String x, String y) {
            ObjectNode response = builder.createObjectNode();
            response.put("x", x);
            response.put("y", y);
            return response;
        }

    }

    /*
     * Transform given coordinates from epsgFrom to epsgTo.
     * Returns null in case a given EPSG code is invalid.
     */
    private ObjectNode jtsTransform(
        String epsgFrom,
        String epsgTo,
        String x,
        String y
    ) {
        try {
            CoordinateReferenceSystem src = CRS.decode(epsgFrom);
            CoordinateReferenceSystem target = CRS.decode(epsgTo);

            MathTransform transform = CRS.findMathTransform(src, target);
            Coordinate srcCoord = new Coordinate();
            srcCoord.y = Double.valueOf(y.replace(",", "."));
            srcCoord.x = Double.valueOf(x.replace(",", "."));
            Coordinate targetCoord = new Coordinate();
            JTS.transform(srcCoord, targetCoord, transform);
            ObjectNode response = builder.createObjectNode();
            response.put("x", String.valueOf(targetCoord.x));
            response.put("y", String.valueOf(targetCoord.y));
            return response;

        } catch (FactoryException | TransformException e) {
            return null;
        }
    }

    private ObjectNode degreeToArc(String x, String y) {
        x = x.replaceAll(",", ".");
        y = y.replaceAll(",", ".");
        String[] xParts = x.split("\\.");
        String[] yParts = y.split("\\.");
        double factorX = 3600;
        double factorY = 3600;
        double wsX = 0;
        double wsY = 0;
        try {
            if (xParts.length == 2) {
                wsX = Double.parseDouble("0." + xParts[1]) * factorX;
            }
            if (yParts.length == 2) {
                wsY = Double.parseDouble("0." + yParts[1]) * factorY;
            }
        } catch (NumberFormatException nfe) {
            return null;
        }

        String xRes = xParts[0]
            + String.format("%02d", (int) Math.floor(wsX / 60))
            + String.format("%02.5f", wsX % 60);
        String yRes = yParts[0]
            + String.format("%02d", (int) Math.floor(wsY / 60))
            + String.format("%02.5f", wsY % 60);
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
        ObjectNode response = builder.createObjectNode();
        response.put("x", xRes.toString());
        response.put("y", yRes.toString());
        return response;
    }

    private ObjectNode arcToDegree(String x, String y) {
        //Replace decimal separator
        x = x.replaceAll("\\.", ",");
        y = y.replaceAll("\\.", ",");
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
            if (x.contains(",")) {
                // with decimal separator
                Pattern p = Pattern.compile(
                    "([+|-|W|E]?)(\\d{1,3})(\\d{2})(\\d{2}),(\\d{1,5})([W|E]?)");
                Matcher m = p.matcher(x);
                m.matches();
                xPrefix = m.group(1);
                xDegree = Integer.valueOf(m.group(2));
                xMin = Integer.valueOf(m.group(3));
                xSec = Double.valueOf(m.group(4) + "." + m.group(5));
                xSuffix = m.group(6);
            } else {
                //Without decimal separator, can include leading zeros
                Pattern p = Pattern.compile(
                    "([+|-|W|E]?)(\\d{3})(\\d{0,2})(\\d{0,2})([W|E]?)");
                Matcher m = p.matcher(x);
                m.matches();
                xPrefix = m.group(1);
                xDegree = Integer.valueOf(m.group(2));
                xMin = Integer.valueOf(
                    !m.group(3).isEmpty() ? m.group(3) : "0");
                xSec = Double.valueOf(
                    !m.group(4).isEmpty() ? m.group(4) : "0.0");
                xSuffix = m.group(5);
            }
            if (y.contains(",")) {
                // with decimal separator
                Pattern p = Pattern.compile(
                    "([+|-|N|S]?)(\\d{1,2})(\\d{2})(\\d{2}),(\\d{1,5})([N|S]?)");
                Matcher m = p.matcher(y);
                m.matches();
                yPrefix = m.group(1);
                yDegree = Integer.valueOf(m.group(2));
                yMin = Integer.valueOf(m.group(3));
                ySec = Double.valueOf(m.group(4) + "." + m.group(5));
                ySuffix = m.group(6);
            } else {
                //Without decimal separator, can include leading zeros
                Pattern p = Pattern.compile(
                    "([+|-|N|S]?)(\\d{2})(\\d{0,2})(\\d{0,2})([N|S]?)");
                Matcher m = p.matcher(y);
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
        ObjectNode response = builder.createObjectNode();
        response.put("x", String.valueOf(ddX));
        response.put("y", String.valueOf(ddY));
        return response;
    }

    private String getWgsUtmEpsg(double x, double y) {
        int pref;
        if (y > 0) {
            pref = 32600;
        } else {
            pref = 32700;
        }
        int code = pref + getUTMZone(x, y);
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

    private String getEpsgForWgsUtm(String x) {
        String epsg = "EPSG:326";
        x = x.replaceAll(",", ".");
        String part = x.split("\\.")[0];
        String zone = part.length() == 7
            ? ("0" + part.substring(0, 1))
            : part.substring(0, 2);
        return epsg + zone;
    }

    private String getEpsgForWgsUtmFromDegree(String x) {
        x = x.replaceAll(",", ".");
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

    private String getEpsgForGK(String y) {
        y = y.replaceAll(",", ".");
        String part = y.split("\\.")[0];
        String zone = part.length() == 7 ? (part.substring(0, 1)) : null;
        if (zone == null) {
            return "";
        }
        try {
            Integer iZone = Integer.valueOf(zone);
            String epsg = "EPSG:3146";
            switch (iZone) {
                case 2: return epsg + "6";
                case 3: return epsg + "7";
                case 4: return epsg + "8";
                case 5: return epsg + "9";
                default: return "";
            }
        } catch (NumberFormatException e) {
            return "";
        }
    }

    private String getEpsgForEd50Utm(String x) {
        String epsg = "EPSG:230";
        String part = x.split(",")[0];
        String zone = part.length() == 7 ? ("0" + part.substring(0, 1))
            : part.substring(0, 2);
        return epsg + zone;
    }

    private String getEpsgForEd50UtmFromDegree(String x) {
        x = x.replaceAll(",", ".");
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
     * Get EPSG code for CRS 'ETRS89 / UTM zone <zone number>N'
     * from easting with zone prefix for use with jtsTransform().
     * Does not guarantee to return a valid EPSG code.
     */
    private String getEpsgForEtrs89(String x) {
        x = x.replaceAll(",", ".");
        String part = x.split("\\.")[0];
        String zone = part.length() == 7 ? ("0" + part.substring(0, 1))
            : part.substring(0, 2);
        return EPSG_UTM_ETRS89_PREFIX + zone;
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
        return EPSG_UTM_ETRS89_PREFIX + getUTMZone(lon, lat);
    }

    /*
     * Get UTM zone for given geodetic coordinates
     */
    private static int getUTMZone(double lon, double lat) {
        return (int) Math.floor((lon + 180) / 6) + 1;
    }

    /*
     * Format UTM coordinates in ObjectNode o with zone prefix
     */
    private void formatUTM(ObjectNode o, int zone) {
        // Output is supposed to have "," as decimal separator
        DecimalFormat df = (DecimalFormat) NumberFormat
            .getNumberInstance(Locale.GERMAN);

        df.applyPattern(EASTING_PATTERN);
        o.put("x",
            df.format((double) zone * ZONE_PREFIX_MULTIPLIER
                + o.get("x").asDouble()));

        df.applyPattern(NORTHING_PATTERN);
        o.put("y",
            df.format(o.get("y").asDouble()));
    }
}
