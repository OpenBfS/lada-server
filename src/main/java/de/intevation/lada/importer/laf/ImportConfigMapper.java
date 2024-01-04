/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.laf;

import java.util.Map;
import java.util.List;

import org.jboss.logging.Logger;

import de.intevation.lada.model.master.ImportConf;

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
        for (ImportConf cfg: this.config) {
            String key = cfg.getAttribute().toUpperCase();
            boolean hasKey = attributes.containsKey(key);
            switch (cfg.getAction()) {
            case DEFAULT:
                if (!hasKey) {
                    attributes.put(key, cfg.getToVal());
                }
                continue;
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
                logger.error("Unimplemented action");
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
