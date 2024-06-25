/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import jakarta.inject.Inject;

import org.junit.Test;

import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.GeolocatMpg;
import de.intevation.lada.model.lada.Geolocat_;
import de.intevation.lada.validation.Validator;

public class GeolocatTest extends ValidatorBaseTest {

    // Other contstants
    public static final int SAMPLE_WITH_E_GEOLOCAT = 1000;
    public static final int SAMPLE_WITH_R_GEOLOCAT = 25001;
    public static final int SAMPLE_WITHOUT_E_GEOLOCAT = 3000;
    public static final int MPG_WITH_E_GEOLOCAT = 999;
    public static final int MPG_WITH_R_GEOLOCAT = 997;
    public static final int MPG_WITHOUT_E_GEOLOCAT = 998;
    public static final int REFERENCED_SITE_ID = 1000;
    public static final int EXISTING_SITE_ID = 1001;

    private static final String TYPE_REGULATION_E = "E";
    private static final String TYPE_REGULATION_R = "R";
    private static final String TYPE_REGULATION_U = "U";

    private static final String MSG_UNIQUE_SAMPLING_LOCATION =
        "Sampling location (typeRegulation \"E\" or \"R\") must be unique";

    @Inject
    private Validator<Geolocat> sampleVal;
    @Inject
    private Validator<GeolocatMpg> mpgVal;

    /**
     * Constructor.
     * Sets test dataset.
     */
    public GeolocatTest() {
        this.testDatasetName = "datasets/dbUnit_geolocat_validator.xml";
    }

    /**
     * Test adding E-type geolocat to sample which already has E-type geolocat.
     */
    @Test
    public void geolocatDuplicateETypeRegulation() {
        Geolocat loc = new Geolocat();
        loc.setTypeRegulation(TYPE_REGULATION_E);
        loc.setSampleId(SAMPLE_WITH_E_GEOLOCAT);
        loc.setSiteId(EXISTING_SITE_ID);

        sampleVal.validate(loc);
        assertHasErrors(
            loc,
            Geolocat_.TYPE_REGULATION,
            MSG_UNIQUE_SAMPLING_LOCATION);
    }

    /**
     * Test adding R-type geolocat to sample which already has R-type geolocat.
     */
    @Test
    public void geolocatDuplicateRTypeRegulation() {
        Geolocat loc = new Geolocat();
        loc.setTypeRegulation(TYPE_REGULATION_R);
        loc.setSampleId(SAMPLE_WITH_R_GEOLOCAT);
        loc.setSiteId(EXISTING_SITE_ID);

        sampleVal.validate(loc);
        assertHasErrors(
            loc,
            Geolocat_.TYPE_REGULATION,
            MSG_UNIQUE_SAMPLING_LOCATION);
    }

    /**
     * Test adding R-type geolocat to sample which already has E-type geolocat.
     */
    @Test
    public void geolocatDuplicateSamplingLocation() {
        Geolocat loc = new Geolocat();
        loc.setTypeRegulation(TYPE_REGULATION_R);
        loc.setSampleId(SAMPLE_WITH_E_GEOLOCAT);
        loc.setSiteId(EXISTING_SITE_ID);

        sampleVal.validate(loc);
        assertHasErrors(
            loc,
            Geolocat_.TYPE_REGULATION,
            MSG_UNIQUE_SAMPLING_LOCATION);
    }

    @Test
    public void geolocatDuplicate() {
        Geolocat loc = new Geolocat();
        loc.setTypeRegulation(TYPE_REGULATION_E);
        loc.setSampleId(SAMPLE_WITH_E_GEOLOCAT);
        loc.setSiteId(REFERENCED_SITE_ID);

        assertHasErrors(
            sampleVal.validate(loc),
            Geolocat_.TYPE_REGULATION,
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

        assertNoMessages(sampleVal.validate(loc));
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

        assertNoMessages(sampleVal.validate(loc));
    }

    /**
     * Test adding E-type geolocat to Mpg which already has E-type geolocat.
     */
    @Test
    public void geolocatMpgDuplicateETypeRegulation() {
        GeolocatMpg loc = new GeolocatMpg();
        loc.setTypeRegulation(TYPE_REGULATION_E);
        loc.setMpgId(MPG_WITH_E_GEOLOCAT);
        loc.setSiteId(EXISTING_SITE_ID);

        mpgVal.validate(loc);
        assertHasErrors(
            loc,
            Geolocat_.TYPE_REGULATION,
            MSG_UNIQUE_SAMPLING_LOCATION);
    }

    /**
     * Test adding R-type geolocat to Mpg which already has R-type geolocat.
     */
    @Test
    public void geolocatMpgDuplicateRTypeRegulation() {
        GeolocatMpg loc = new GeolocatMpg();
        loc.setTypeRegulation(TYPE_REGULATION_R);
        loc.setMpgId(MPG_WITH_R_GEOLOCAT);
        loc.setSiteId(EXISTING_SITE_ID);

        mpgVal.validate(loc);
        assertHasErrors(
            loc,
            Geolocat_.TYPE_REGULATION,
            MSG_UNIQUE_SAMPLING_LOCATION);
    }

    /**
     * Test adding R-type geolocat to Mpg which already has E-type geolocat.
     */
    @Test
    public void geolocatMpgDuplicateSamplingLocation() {
        GeolocatMpg loc = new GeolocatMpg();
        loc.setTypeRegulation(TYPE_REGULATION_R);
        loc.setMpgId(MPG_WITH_E_GEOLOCAT);
        loc.setSiteId(EXISTING_SITE_ID);

        mpgVal.validate(loc);
        assertHasErrors(
            loc,
            Geolocat_.TYPE_REGULATION,
            MSG_UNIQUE_SAMPLING_LOCATION);
    }

    @Test
    public void geolocatMpgDuplicate() {
        GeolocatMpg loc = new GeolocatMpg();
        loc.setTypeRegulation(TYPE_REGULATION_E);
        loc.setMpgId(MPG_WITH_E_GEOLOCAT);
        loc.setSiteId(REFERENCED_SITE_ID);

        mpgVal.validate(loc);
        assertHasErrors(
            loc,
            Geolocat_.TYPE_REGULATION,
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

        assertNoMessages(mpgVal.validate(loc));
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

        assertNoMessages(mpgVal.validate(loc));
    }
}
