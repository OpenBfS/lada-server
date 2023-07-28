/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.laf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.Action;
import de.intevation.lada.model.master.ImportConf;

/**
 * Unit tests for ImportConfigMapper.
 */
public class ImportConfigMapperTest {

    /**
     * Test applying default to sample object.
     */
    @Test
    public void applyConfigDefaultSampleTest() {
        final String expected = "01010";

        ImportConf config = new ImportConf();
        config.setName("probe");
        config.setAttribute("measFacilId");
        config.setToVal(expected);
        config.setAction(Action.DEFAULT);
        ImportConfigMapper mapper = new ImportConfigMapper(List.of(config));

        Sample sample = new Sample();
        mapper.applyConfigs(sample);
        Assert.assertEquals(expected, sample.getMeasFacilId());
    }

    /**
     * Test string conversion.
     */
    @Test
    public void convertStringTest() {
        final String key = "CONVERTME";
        final String value = "BQ/kgFM";
        Map<String, String> input = new HashMap<>();
        input.put(key, value);
        final String expected = "Bq/kg(FM)";

        ImportConf config = new ImportConf();
        config.setAttribute(key);
        config.setFromVal(value);
        config.setToVal(expected);
        config.setAction(Action.CONVERT);
        ImportConfigMapper mapper = new ImportConfigMapper(List.of(config));

        mapper.applyConfigs(input);
        Assert.assertEquals(expected, input.get(key));
    }

    /**
     * Test string transformation.
     */
    @Test
    public void transformStringTest() {
        final String key = "TRANSFORMME";
        Map<String, String> input = new HashMap<>();
        input.put(key, "x x");

        ImportConf config = new ImportConf();
        config.setAttribute(key);
        config.setFromVal("20");
        config.setToVal("30");
        config.setAction(Action.TRANSFORM);
        ImportConfigMapper mapper = new ImportConfigMapper(List.of(config));

        mapper.applyConfigs(input);
        Assert.assertEquals("x0x", input.get(key));
    }
}
