/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import jakarta.inject.Inject;

import java.util.ResourceBundle;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import de.intevation.lada.model.master.Site;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * Test validation rules for Site objects.
 */
public class SiteTest extends ValidatorBaseTest {
    //Validator keys
    private static final String COORD_X_EXT = "coordXExt";
    private static final String COORD_Y_EXT = "coordYExt";
    private static final String EXT_ID = "extId";
    private static final String MUNIC_ID = "municId";
    private static final String KTA_GRUPPE_ID = "nuclFacilGrId";
    private static final String REI_NUCL_FACIL_GR_ID = "reiNuclFacilGrId";
    private static final String SITE_CLASS_ID = "siteClassId";

    //Other constants
    private static final int SPAT_REF_SYS_ID = 5;
    private static final String INVALID_ADMIN_UNIT_ID = "420042";
    private static final String VALID_ADMIN_UNIT_ID = "11000000";

    private static final String EXAMPLE_EXT_ID = "1234";
    private static final int SITE_CLASS_REI = 3;

    private static final String NUCL_FACIL_EXT_ID_UNMAPPED = "Othr";
    private static final String NUCL_FACIL_EXT_ID_SHORT = "123";
    private static final String NUCL_FACIL_EXT_ID_MAPPED = "A1234";
    private static final int NUCL_FACIL_GR_ID_MAPPED = 1;

    private static final double COORDINATE_OUTSIDE_Y = 48.1579;
    private static final double COORDINATE_OUTSIDE_X = 11.535333;
    private static final double COORDINATE_JUST_OUTSIDE_Y = 52.401365;
    private static final double COORDINATE_JUST_OUTSIDE_X = 13.108697;
    private static final double COORDINATE_INSIDE_Y = 52.409959;
    private static final double COORDINATE_INSIDE_X = 13.10257;

    @Inject
    private Validator<Site> validator;

    //Expected validation messages
    private static final String UNIQUE_PLACEHOLDER = "{fields}";
    private final String valMessageUniqueExtId;

    /**
     * Constructor.
     */
    public SiteTest() {
        super();
        ResourceBundle validationMessages
            = ResourceBundle.getBundle("ValidationMessages");
        String uniquePattern = validationMessages.getString(
            "de.intevation.lada.validation.constraints.Unique.message");
        valMessageUniqueExtId = uniquePattern
            .replace(UNIQUE_PLACEHOLDER, "[extId, networkId]");

    }

    /**
     * Test site object with adminUnit without border view entry.
     */
    @Test
    public void adminUnitWithoutAdminBorders() {
        GeometryFactory gf = new GeometryFactory();
        Point insideBorder = gf.createPoint(
                new Coordinate(COORDINATE_INSIDE_X, COORDINATE_INSIDE_Y));
        Site site = createMinimalSite();
        site.setAdminUnitId(INVALID_ADMIN_UNIT_ID);
        site.setGeom(insideBorder);
        site.setExtId(EXAMPLE_EXT_ID);

        validator.validate(site);
        Assert.assertTrue(site.hasWarnings());
        Assert.assertTrue(site.getWarnings().containsKey(MUNIC_ID));
        Assert.assertTrue(site.getWarnings().get(MUNIC_ID).contains(
                String.valueOf(StatusCodes.GEO_COORD_UNCHECKED)));
    }

    /**
     * Test fuzzy site object close to the admin border.
     */
    @Test
    public void fuzzySiteOutsiteAdminBorders() {
        GeometryFactory gf = new GeometryFactory();
        Point justOutsideBorder = gf.createPoint(new Coordinate(
            COORDINATE_JUST_OUTSIDE_X, COORDINATE_JUST_OUTSIDE_Y));
        Site site = createMinimalSite();
        site.setAdminUnitId(VALID_ADMIN_UNIT_ID);
        site.setIsFuzzy(true);
        site.setGeom(justOutsideBorder);

        validator.validate(site);
        assertNoWarningsOrErrors(site);
    }

    /**
     * Test site object far outside to the admin border.
     */
    @Test
    public void siteOutsiteAdminBorders() {
        GeometryFactory gf = new GeometryFactory();
        Point justOutsideBorder = gf.createPoint(new Coordinate(
            COORDINATE_OUTSIDE_X, COORDINATE_OUTSIDE_Y));
        Site site = createMinimalSite();
        site.setAdminUnitId(VALID_ADMIN_UNIT_ID);
        site.setIsFuzzy(false);
        site.setGeom(justOutsideBorder);

        validator.validate(site);
        Assert.assertTrue(site.hasWarnings());
        Assert.assertTrue(site.getWarnings().containsKey(COORD_X_EXT));
        Assert.assertTrue(site.getWarnings().get(COORD_X_EXT).contains(
                String.valueOf(StatusCodes.GEO_POINT_OUTSIDE)));
        Assert.assertTrue(site.getWarnings().containsKey(COORD_Y_EXT));
        Assert.assertTrue(site.getWarnings().get(COORD_Y_EXT).contains(
                String.valueOf(StatusCodes.GEO_POINT_OUTSIDE)));
    }

    /**
     * Test site inside admin borders.
     */
    @Test
    public void siteInsideAdminBorders() {
        GeometryFactory gf = new GeometryFactory();
        Point insideBorder = gf.createPoint(
                new Coordinate(COORDINATE_INSIDE_X, COORDINATE_INSIDE_Y));
        Site site = createMinimalSite();
        site.setAdminUnitId(VALID_ADMIN_UNIT_ID);
        site.setIsFuzzy(false);
        site.setGeom(insideBorder);

        validator.validate(site);
        assertNoWarningsOrErrors(site);
    }

    /**
     * Test site with duplicate extId.
     */
    @Test
    public void duplicateExtId() {
        Site site = createMinimalSite();
        site.setExtId("D_00191");

        validator.validate(site);
        Assert.assertTrue(site.hasErrors());
        Assert.assertTrue(site.getErrors().containsKey(EXT_ID));
        MatcherAssert.assertThat(
            site.getErrors().get(EXT_ID),
            CoreMatchers.hasItem(valMessageUniqueExtId));
    }

    /**
     * Test site with unique extId.
     */
    @Test
    public void uniqueExtId() {
        Site site = createMinimalSite();
        site.setExtId("D_00192");

        validator.validate(site);
        assertNoWarningsOrErrors(site);
    }

    /**
     * Test site with a non existing site class.
     */
    @Test
    public void siteClassDoesNotExist() {
        Site site = createMinimalSite();
        site.setSiteClassId(0);

        assertHasError(
            validator.validate(site),
            SITE_CLASS_ID,
            "'0' is no valid primary key");
    }

    /**
     * Test site with valid site class.
     */
    @Test
    public void siteClassDoesExist() {
        Site site = createMinimalSite();

        validator.validate(site);
        assertNoWarningsOrErrors(site);
    }

    /**
     * Test REI site with 3 character extId.
     */
    @Test
    public void reiSiteExtIdTooShort() {
        Site site = createMinimalSite();
        site.setExtId(NUCL_FACIL_EXT_ID_SHORT);
        site.setSiteClassId(SITE_CLASS_REI);
        site.setNuclFacilGrId(NUCL_FACIL_GR_ID_MAPPED);

        validator.validate(site);
        Assert.assertTrue(site.hasWarnings());
        Assert.assertTrue(site.getWarnings().containsKey(EXT_ID));
        Assert.assertTrue(site.getWarnings().get(EXT_ID)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test REI site without nucl facils.
     */
    @Test
    public void reiSiteNoNuclFacils() {
        Site site = createMinimalSite();
        site.setExtId(EXAMPLE_EXT_ID);
        site.setSiteClassId(SITE_CLASS_REI);
        site.setNuclFacilGrId(NUCL_FACIL_GR_ID_MAPPED);

        validator.validate(site);
        Assert.assertTrue(site.hasWarnings());
        Assert.assertTrue(site.getWarnings().containsKey(EXT_ID));
        Assert.assertTrue(site.getWarnings().get(EXT_ID)
            .contains(String.valueOf(StatusCodes.ORT_ANLAGE_MISSING)));
    }

    /**
     * Test REI site which ext id points to NuclFacil that is not connected to
     * its nuclFacilGrId.
     */
    @Test
    public void reiSiteNuclFacilWithoutMappingEntry() {
        Site site = createMinimalSite();
        site.setExtId(NUCL_FACIL_EXT_ID_UNMAPPED);
        site.setNuclFacilGrId(NUCL_FACIL_GR_ID_MAPPED);
        site.setSiteClassId(SITE_CLASS_REI);

        validator.validate(site);
        Assert.assertTrue(site.hasWarnings());
        Assert.assertTrue(site.getWarnings()
            .containsKey(REI_NUCL_FACIL_GR_ID));
        Assert.assertTrue(site.getWarnings().get(REI_NUCL_FACIL_GR_ID)
            .contains(String.valueOf(StatusCodes.VALUE_NOT_MATCHING)));
    }

    /**
     * Test rei site without nuclFacilGrId.
     */
    @Test
    public void reiSiteWithoutNuclFacilGrId() {
        Site site = createMinimalSite();
        site.setExtId(EXAMPLE_EXT_ID);
        site.setSiteClassId(SITE_CLASS_REI);

        validator.validate(site);
        Assert.assertTrue(site.hasWarnings());
        Assert.assertTrue(site.getWarnings()
            .containsKey(KTA_GRUPPE_ID));
        Assert.assertTrue(site.getWarnings().get(KTA_GRUPPE_ID)
            .contains(String.valueOf(StatusCodes.VALUE_MISSING)));
    }

    /**
     * Test rei site.
     */
    @Test
    public void reiSite() {
        Site site = createMinimalSite();
        site.setSiteClassId(SITE_CLASS_REI);
        site.setExtId(NUCL_FACIL_EXT_ID_MAPPED);
        site.setNuclFacilGrId(1);

        validator.validate(site);
        assertNoWarningsOrErrors(site);
    }

    @Test
    public void noCoordsNorAdminUnitNorState() {
        Site site = createMinimalSite();
        site.setStateId(null);
        assertHasCoordsOrAdminUnitOrState(validator.validate(site));
    }

    @Test
    public void hasIncompleteCoordsNoSpatRefSys() {
        Site site = createMinimalSite();
        site.setStateId(null);
        site.setCoordXExt("0");
        site.setCoordYExt("0");

        assertHasCoordsOrAdminUnitOrState(validator.validate(site));
    }

    @Test
    public void hasIncompleteCoordsNoCoordX() {
        Site site = createMinimalSite();
        site.setStateId(null);
        site.setSpatRefSysId(SPAT_REF_SYS_ID);
        site.setCoordYExt("0");

        assertHasCoordsOrAdminUnitOrState(validator.validate(site));
    }

    @Test
    public void hasIncompleteCoordsNoCoordY() {
        Site site = createMinimalSite();
        site.setStateId(null);
        site.setSpatRefSysId(SPAT_REF_SYS_ID);
        site.setCoordXExt("0");

        assertHasCoordsOrAdminUnitOrState(validator.validate(site));
    }

    private void assertHasCoordsOrAdminUnitOrState(Site site) {
        final String msg =
            "Either coordinates or adminUnitId or stateId must be given";
        assertHasError(site, "coordinates", msg);
        assertHasError(site, "adminUnitId", msg);
        assertHasError(site, "stateId", msg);
    }

    @Test
    public void hasCoords() {
        Site site = createMinimalSite();
        site.setStateId(null);
        site.setSpatRefSysId(SPAT_REF_SYS_ID);
        site.setCoordXExt("0");
        site.setCoordYExt("0");

        assertNoWarningsOrErrors(validator.validate(site));
    }

    @Test
    public void hasAdminUnit() {
        Site site = createMinimalSite();
        site.setStateId(null);
        site.setAdminUnitId(VALID_ADMIN_UNIT_ID);

        assertNoWarningsOrErrors(validator.validate(site));
    }

    @Test
    public void hasState() {
        Site site = createMinimalSite();

        assertNoWarningsOrErrors(validator.validate(site));
    }

    private Site createMinimalSite() {
        Site site = new Site();
        site.setNetworkId("06");
        site.setStateId(0);
        site.setSiteClassId(1);
        return site;
    }
}
