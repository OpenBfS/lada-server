/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.laf;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
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

    /**
     * Apply configuration to given attributes.
     *
     * @param attributes Attributes representing raw import data.
     */
    void applyConfigs(Map<String, String> attributes) {
        // TODO: default action
        for (ImportConf cfg: this.config) {
            String key = cfg.getAttribute().toUpperCase();
            boolean hasKey = attributes.containsKey(key);
            switch (cfg.getAction()) {
            case CONVERT:
                if (hasKey && cfg.getFromVal().equals(attributes.get(key))) {
                    attributes.put(key, cfg.getToVal());
                }
                continue;
            case TRANSFORM:
                if (hasKey) {
                    attributes.put(key, transform(attributes.get(key), cfg));
                }
                continue;
            default:
                continue;
            }
        }
    }

    void applyConfigs(Sample probe) {
        final String table = "probe";
        applyConfigs(probe, Sample.class, table);
    }

    void applyConfigs(Measm messung) {
        final String table = "messung";
        applyConfigs(messung, Measm.class, table);
    }

    void applyConfigs(MeasVal messwert) {
        final String table = "messwert";
        applyConfigs(messwert, MeasVal.class, table);
    }

    void applyConfigs(SampleSpecifMeasVal zusatzwert) {
        final String table = "zusatzwert";
        applyConfigs(zusatzwert, SampleSpecifMeasVal.class, table);
    }

    void applyConfigs(CommMeasm kommentar) {
        final String table = "kommentarm";
        applyConfigs(kommentar, CommMeasm.class, table);
    }

    void applyConfigs(CommSample kommentar) {
        final String table = "kommentarp";
        applyConfigs(kommentar, CommSample.class, table);
    }

    void applyConfigs(Geolocat ort) {
        final String table = "ortszuordnung";
        applyConfigs(ort, Geolocat.class, table);
    }

    void applyConfigs(Site o) {
        applyConfigs(o, Site.class, "ort");
    }

    <T> void applyConfigs(Object object, Class<T> clazz, String table) {
        for (ImportConf current: config) {
            if (table.equals(current.getName())) {
                String attribute = current.getAttribute();
                PropertyDescriptor beanDesc;
                try {
                    beanDesc = new PropertyDescriptor(attribute, clazz);
                } catch (IntrospectionException e) {
                    logger.warn(
                        "attribute " + attribute + " does not exist");
                    continue;
                }
                Method getter = beanDesc.getReadMethod();
                Method setter = beanDesc.getWriteMethod();
                try {
                    Object value = getter.invoke(object);
                    switch (current.getAction()) {
                    case DEFAULT:
                        if (value == null && setter != null) {
                            Class<?>[] types = setter.getParameterTypes();
                            if (types.length == 1) {
                                // Exactly one parameter, thats fine.
                                if (types[0].isAssignableFrom(
                                        Integer.class)
                                ) {
                                    // the parameter is of type Integer!
                                    // Cast to integer
                                    setter.invoke(
                                        object,
                                        Integer.valueOf(
                                            current.getToVal()));
                                } else {
                                    // we handle the default as string.
                                    // Other types are not implemented!
                                    setter.invoke(
                                        object, current.getToVal());
                                }
                            }
                        }
                        break;
                    case CONVERT:
                        if (value != null
                            && value.equals(current.getFromVal())
                            && setter != null
                        ) {
                            setter.invoke(object, current.getToVal());
                        }
                        break;
                    case TRANSFORM:
                        if (value == null) {
                            logger.warn(
                                "Attribute " + attribute + " is not set");
                            return;
                        }
                        setter.invoke(
                            object,
                            transform(value.toString(), current));
                        break;
                    default:
                        throw new IllegalArgumentException(
                            "Unknown action");
                    }
                } catch (IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException e
                ) {
                    logger.debug("Could not set attribute " + attribute);
                }
            }
        }
    }

    private String transform(String value, ImportConf cfg) {
        final int radix = 16;
        char from = (char) Integer.parseInt(cfg.getFromVal(), radix);
        char to = (char) Integer.parseInt(cfg.getToVal(), radix);
        return value.replaceAll(
            "[" + String.valueOf(from) + "]",
            String.valueOf(to));
    }
}
