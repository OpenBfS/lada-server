/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import jakarta.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.GeolocatMpg;
import de.intevation.lada.validation.Validator;

public class GeolocatTest extends ValidatorBaseTest {

    // Validation keys
    private static final String TYPE_REGULATION = "typeRegulation";
    private static final String SAMPLE_ID = "sampleId";

    // Other contstants
    public static final int SAMPLE_WITH_E_GEOLOCAT = 1000;
    public static final int SAMPLE_WITHOUT_E_GEOLOCAT = 3000;
    public static final int MPG_WITH_E_GEOLOCAT = 999;
    public static final int MPG_WITHOUT_E_GEOLOCAT = 998;
    public static final int REFERENCED_SITE_ID = 1000;
    public static final int EXISTING_SITE_ID = 1001;

    private static final String TYPE_REGULATION_E = "E";
    private static final String TYPE_REGULATION_U = "U";

    @Inject
    private Validator<Geolocat> sampleVal;
    @Inject
    private Validator<GeolocatMpg> mpgVal;

    /**
     * Test geolocat with sample which already has a E-Type geolocat.
     */
    @Test
    public void geolocatDuplicateETypeRegulation() {
        Geolocat loc = new Geolocat();
        loc.setTypeRegulation(TYPE_REGULATION_E);
        loc.setSampleId(SAMPLE_WITH_E_GEOLOCAT);
        loc.setSiteId(EXISTING_SITE_ID);

        sampleVal.validate(loc);
        assertHasError(
            loc,
            TYPE_REGULATION,
            "Geolocation with typeRegulation \"E\" must be unique");
    }

    @Test
    public void geolocatDuplicate() {
        Geolocat loc = new Geolocat();
        loc.setTypeRegulation(TYPE_REGULATION_E);
        loc.setSampleId(SAMPLE_WITH_E_GEOLOCAT);
        loc.setSiteId(REFERENCED_SITE_ID);

        sampleVal.validate(loc);
        Assert.assertTrue(loc.hasErrors());
        assertHasError(
            loc,
            TYPE_REGULATION,
            "Non-unique value combination for "
                + "[typeRegulation, sampleId, siteId]");
    }

    /**
     * Test valid geolocat.
     */
    @Test
    public void validGeolocat() {
        Geolocat loc = new Geolocat();
        loc.setTypeRegulation(TYPE_REGULATION_E);
        loc.setSampleId(SAMPLE_WITHOUT_E_GEOLOCAT);
        loc.setSiteId(EXISTING_SITE_ID);

        sampleVal.validate(loc);
        Assert.assertFalse(loc.hasErrors());
    }

    /**
     * Ensure that a geolocat of type "U" can be added if
     * one of type "E" is present.
     */
    @Test
    public void canAddUTypeGeolocat() {
        Geolocat loc = new Geolocat();
        loc.setTypeRegulation(TYPE_REGULATION_U);
        loc.setSampleId(SAMPLE_WITH_E_GEOLOCAT);
        loc.setSiteId(EXISTING_SITE_ID);

        assertNoWarningsOrErrors(sampleVal.validate(loc));
    }

    /**
     * Test geolocat with mpg which already has a E-Type geolocat.
     */
    @Test
    public void geolocatMpgDuplicateETypeRegulation() {
        GeolocatMpg loc = new GeolocatMpg();
        loc.setTypeRegulation(TYPE_REGULATION_E);
        loc.setMpgId(MPG_WITH_E_GEOLOCAT);
        loc.setSiteId(EXISTING_SITE_ID);

        mpgVal.validate(loc);
        assertHasError(
            loc,
            TYPE_REGULATION,
            "Geolocation with typeRegulation \"E\" must be unique");
    }

    @Test
    public void geolocatMpgDuplicate() {
        GeolocatMpg loc = new GeolocatMpg();
        loc.setTypeRegulation(TYPE_REGULATION_E);
        loc.setMpgId(MPG_WITH_E_GEOLOCAT);
        loc.setSiteId(REFERENCED_SITE_ID);

        mpgVal.validate(loc);
        assertHasError(
            loc,
            TYPE_REGULATION,
            "Non-unique value combination for [typeRegulation, mpgId, siteId]");
    }

    /**
     * Test valid geolocatMpg.
     */
    @Test
    public void validGeolocatMpg() {
        GeolocatMpg loc = new GeolocatMpg();
        loc.setTypeRegulation(TYPE_REGULATION_E);
        loc.setMpgId(MPG_WITHOUT_E_GEOLOCAT);
        loc.setSiteId(EXISTING_SITE_ID);
        mpgVal.validate(loc);
        Assert.assertFalse(loc.hasErrors());
    }

    /**
     * Ensure that a geolocat of type "U" can be added if
     * one of type "E" is present.
     */
    @Test
    public void canAddUTypeGeolocatMpg() {
        GeolocatMpg loc = new GeolocatMpg();
        loc.setTypeRegulation(TYPE_REGULATION_U);
        loc.setMpgId(MPG_WITH_E_GEOLOCAT);
        loc.setSiteId(EXISTING_SITE_ID);

        assertNoWarningsOrErrors(mpgVal.validate(loc));
    }
}
