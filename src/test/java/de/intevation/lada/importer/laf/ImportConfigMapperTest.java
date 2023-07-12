/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.laf;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.intevation.lada.model.lada.Sample;
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
        config.setAction("default");
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
        final String key = "convertme";
        final String input = "BQ/kgFM";
        final String expected = "Bq/kg(FM)";

        ImportConf config = new ImportConf();
        config.setAttribute(key);
        config.setFromVal(input);
        config.setToVal(expected);
        config.setAction("convert");
        ImportConfigMapper mapper = new ImportConfigMapper(List.of(config));

        Assert.assertEquals(
            expected, mapper.applyConfigByAttribute(key, input));
    }

    /**
     * Test string transformation.
     */
    @Test
    public void transformStringTest() {
        final String key = "TRANSFORMME";

        ImportConf config = new ImportConf();
        config.setAttribute(key);
        config.setFromVal("20");
        config.setToVal("30");
        config.setAction("transform");
        ImportConfigMapper mapper = new ImportConfigMapper(List.of(config));

        Assert.assertEquals("x0x", mapper.applyConfigByAttribute(key, "x x"));
    }
}
