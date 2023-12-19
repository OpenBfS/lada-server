/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.junit.Assert;

import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.GeolocatMpg;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;

@Transactional
public class GeolocatTest {

    // Validation keys
    private static final String TYPE_REGULATION = "typeRegulation";

    // Other contstants
    public static final int SAMPLE_WITH_E_GEOLOCAT = 1000;
    public static final int SAMPLE_WITHOUT_E_GEOLOCAT = 1002;
    public static final int MPG_WITH_E_GEOLOCAT = 999;
    public static final int MPG_WITHOUT_E_GEOLOCAT = 998;

    private static final String TYPE_REGULATION_E = "E";

    @Inject
    private Validator<Geolocat> sampleVal;
    @Inject
    private Validator<GeolocatMpg> mpgVal;

    /**
     * Test geolocat with sample which already has a E-Type geolocat.
     */
    public void geolocatDuplicateETypeRegulation() {
        Geolocat loc = new Geolocat();
        loc.setTypeRegulation(TYPE_REGULATION_E);
        loc.setSampleId(SAMPLE_WITH_E_GEOLOCAT);

        sampleVal.validate(loc);
        Assert.assertTrue(loc.hasWarnings());
        Assert.assertTrue(loc.getWarnings().containsKey(TYPE_REGULATION));
        Assert.assertTrue(loc.getWarnings()
                .get(TYPE_REGULATION).contains(StatusCodes.VALUE_AMBIGOUS));
    }

    /**
     * Test valid geolocat.
     */
    public void validGeolocat() {
        Geolocat loc = new Geolocat();
        loc.setTypeRegulation(TYPE_REGULATION_E);
        loc.setSampleId(SAMPLE_WITHOUT_E_GEOLOCAT);

        sampleVal.validate(loc);
        Assert.assertFalse(loc.hasErrors());
    }

    /**
     * Test geolocat with mpg which already has a E-Type geolocat.
     */
    public void geolocatMpgDuplicateETypeRegulation() {
        GeolocatMpg loc = new GeolocatMpg();
        loc.setTypeRegulation(TYPE_REGULATION_E);
        loc.setMpgId(MPG_WITH_E_GEOLOCAT);

        mpgVal.validate(loc);
        Assert.assertTrue(loc.hasWarnings());
        Assert.assertTrue(loc.getWarnings().containsKey(TYPE_REGULATION));
        Assert.assertTrue(loc.getWarnings()
                .get(TYPE_REGULATION).contains(StatusCodes.VALUE_AMBIGOUS));
    }

    /**
     * Test valid geolocatMpg.
     */
    public void validGeolocatMpg() {
        GeolocatMpg loc = new GeolocatMpg();
        loc.setTypeRegulation(TYPE_REGULATION_E);
        loc.setMpgId(MPG_WITHOUT_E_GEOLOCAT);
        mpgVal.validate(loc);
        Assert.assertFalse(loc.hasErrors());
    }
}
