/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.laf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import org.jboss.logging.Logger;

import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.model.master.ImportConf;
import de.intevation.lada.model.master.Site;

/**
 * Map attributes from ImportConf objects to imported data.
 */
public class ImportConfigMapper {

    private List<ImportConf> config;

    private final Logger logger = Logger.getLogger(ImportConfigMapper.class);

    ImportConfigMapper(List<ImportConf> config) {
        this.config = config;
    }

    void applyConfigs(Sample probe) {
        final String table = "probe";
        doDefaults(probe, Sample.class, table);
        doConverts(probe, Sample.class, table);
        doTransformations(probe, Sample.class, table);
    }

    void applyConfigs(Measm messung) {
        final String table = "messung";
        doDefaults(messung, Measm.class, table);
        doConverts(messung, Measm.class, table);
        doTransformations(messung, Measm.class, table);
    }

    void applyConfigs(MeasVal messwert) {
        final String table = "messwert";
        doDefaults(messwert, MeasVal.class, table);
        doConverts(messwert, MeasVal.class, table);
        doTransformations(messwert, MeasVal.class, table);
    }

    void applyConfigs(SampleSpecifMeasVal zusatzwert) {
        final String table = "zusatzwert";
        doDefaults(zusatzwert, SampleSpecifMeasVal.class, table);
        doConverts(zusatzwert, SampleSpecifMeasVal.class, table);
        doTransformations(zusatzwert, SampleSpecifMeasVal.class, table);
    }

    void applyConfigs(CommMeasm kommentar) {
        final String table = "kommentarm";
        doDefaults(kommentar, CommMeasm.class, table);
        doConverts(kommentar, CommMeasm.class, table);
        doTransformations(kommentar, CommMeasm.class, table);
    }

    void applyConfigs(CommSample kommentar) {
        final String table = "kommentarp";
        doDefaults(kommentar, CommSample.class, table);
        doConverts(kommentar, CommSample.class, table);
        doTransformations(kommentar, CommSample.class, table);
    }

    void applyConfigs(Geolocat ort) {
        final String table = "ortszuordnung";
        doDefaults(ort, Geolocat.class, table);
        doConverts(ort, Geolocat.class, table);
        doTransformations(ort, Geolocat.class, table);
    }

    void applyConfigs(Site o) {
        doDefaults(o, Site.class, "ort");
    }

    <T> void doDefaults(Object object, Class<T> clazz, String table) {
        Iterator<ImportConf> i = config.iterator();
        while (i.hasNext()) {
            ImportConf current = i.next();
            if (table.equals(current.getName())
                && "default".equals(current.getAction())
            ) {
                String attribute = current.getAttribute();
                Method getter;
                Method setter = null;
                try {
                    getter = clazz.getMethod("get"
                        + attribute.substring(0, 1).toUpperCase()
                        + attribute.substring(1));
                    String methodName = "set"
                        + attribute.substring(0, 1).toUpperCase()
                        + attribute.substring(1);
                    for (Method method : clazz.getMethods()) {
                        String name = method.getName();
                        if (!methodName.equals(name)) {
                            continue;
                        }
                        setter = method;
                        break;
                    }
                } catch (NoSuchMethodException | SecurityException e) {
                    logger.debug("attribute " + attribute + " does not exist");
                    return;
                }
                try {
                    Object value = getter.invoke(object);
                    if (value == null && setter != null) {
                        Class<?>[] types = setter.getParameterTypes();
                        if (types.length == 1) {
                            // we have exactly one parameter, thats fine.
                            if (types[0].isAssignableFrom(Integer.class)) {
                                // the parameter is of type Integer!
                                // Cast to integer
                                setter.invoke(
                                    object,
                                    Integer.valueOf(current.getToVal()));
                            } else {
                                // we handle the default as string.
                                // Other parameter types are not implemented!
                                setter.invoke(object, current.getToVal());
                            }
                        }
                    }
                } catch (IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException e
                ) {
                    logger.debug("Could not set attribute " + attribute);
                    return;
                }
            }
        }
    }

    <T> void doConverts(Object object, Class<T> clazz, String table) {
        Iterator<ImportConf> i = config.iterator();
        while (i.hasNext()) {
            ImportConf current = i.next();
            if (table.equals(current.getName())
                && "convert".equals(current.getAction())
            ) {
                String attribute = current.getAttribute();
                Method getter;
                Method setter = null;
                try {
                    getter = clazz.getMethod("get"
                        + attribute.substring(0, 1).toUpperCase()
                        + attribute.substring(1));
                    String methodName = "set"
                        + attribute.substring(0, 1).toUpperCase()
                        + attribute.substring(1);
                    for (Method method : clazz.getMethods()) {
                        String name = method.getName();
                        if (!methodName.equals(name)) {
                            continue;
                        }
                        setter = method;
                        break;
                    }
                } catch (NoSuchMethodException | SecurityException e) {
                    logger.warn("attribute " + attribute + " does not exist");
                    return;
                }
                try {
                    Object value = getter.invoke(object);
                    if (value.equals(current.getFromVal())
                        && setter != null
                    ) {
                        setter.invoke(object, current.getToVal());
                    }
                } catch (IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException e
                ) {
                    logger.warn("Could not convert attribute " + attribute);
                    return;
                }
            }
        }
    }

    <T> void doTransformations(
        Object object,
        Class<T> clazz,
        String table
    ) {
        Iterator<ImportConf> i = config.iterator();
        while (i.hasNext()) {
            ImportConf current = i.next();
            if (table.equals(current.getName())
                && "transform".equals(current.getAction())
            ) {
                String attribute = current.getAttribute();
                Method getter;
                Method setter = null;
                try {
                    getter = clazz.getMethod("get"
                        + attribute.substring(0, 1).toUpperCase()
                        + attribute.substring(1));
                    String methodName = "set"
                        + attribute.substring(0, 1).toUpperCase()
                        + attribute.substring(1);
                    for (Method method : clazz.getMethods()) {
                        String name = method.getName();
                        if (methodName.equals(name)) {
                            setter = method;
                            break;
                        }
                    }
                    if (setter == null) {
                        logger.warn(
                            "Could not transform attribute " + attribute);
                        return;
                    }
                } catch (NoSuchMethodException | SecurityException e) {
                    logger.warn("attribute " + attribute + " does not exist");
                    return;
                }
                try {
                    Object value = getter.invoke(object);
                    if (value == null) {
                        logger.warn("Attribute " + attribute + " is not set");
                        return;
                    }
                    char from = (char) Integer.parseInt(
                        current.getFromVal(), 16);
                    char to = (char) Integer.parseInt(
                        current.getToVal(), 16);
                    value = value.toString().replaceAll(
                        "[" + String.valueOf(from) + "]", String.valueOf(to));
                    setter.invoke(object, value);
                } catch (IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException e
                ) {
                    logger.warn("Could not transform attribute " + attribute);
                    return;
                }
            }
        }
    }
}
