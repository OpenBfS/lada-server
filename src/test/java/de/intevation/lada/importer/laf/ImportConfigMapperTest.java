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
}
