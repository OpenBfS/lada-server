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

import de.intevation.lada.model.master.Site;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.Violation;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * Test validation rules for Site objects.
 */
@Transactional
public class SiteTest {
    //Validator keys
    private static final String COORD_X_EXT = "coordXExt";
    private static final String COORD_Y_EXT = "coordYExt";
    private static final String EXT_ID = "extId";
    private static final String MUNIC_ID = "municId";
    private static final String KTA_GRUPPE_ID = "ktaGruppeId";
    private static final String REI_NUCL_FACIL_GR_ID = "reiNuclFacilGrId";
    private static final String SITE_CLASS_ID = "siteClassId";

    //Other constants
    private static final String INVALID_ADMIN_UNIT_ID = "420042";
    private static final String VALID_ADMIN_UNIT_ID = "11000000";

    private static final String EXISTING_NETWORK_ID = "06";
    private static final int SITE_CLASS_REI = 3;

    private static final String NUCL_FACIL_EXT_ID_UNMAPPED = "Othr";
    private static final String NUCL_FACIL_EXT_ID_SHORT = "123";
    private static final String NUCL_FACIL_EXT_ID_MAPPED = "A123";
    private static final int NUCL_FACIL_GR_ID_MAPPED = 1;

    private static final double COORDINATE_OUTSIDE_X = 48.1579;
    private static final double COORDINATE_OUTSIDE_Y = 11.535333;
    private static final double COORDINATE_JUST_OUTSIDE_X = 52.401365;
    private static final double COORDINATE_JUST_OUTSIDE_Y = 13.108697;
    private static final double COORDINATE_INSIDE_X = 52.514818;
    private static final double COORDINATE_INSIDE_Y = 13.402758;

    @Inject
    private Validator<Site> validator;

    /**
     * Test site object with adminUnit without border view entry.
     */
    public void adminUnitWithoutAdminBorders() {
        GeometryFactory gf = new GeometryFactory();
        Point insideBorder = gf.createPoint(
                new Coordinate(COORDINATE_INSIDE_X, COORDINATE_INSIDE_Y));
        Site site = new Site();
        site.setAdminUnitId(INVALID_ADMIN_UNIT_ID);
        site.setGeom(insideBorder);
        site.setNetworkId(EXISTING_NETWORK_ID);

        Violation violation = validator.validate(site);
        Assert.assertTrue(violation.hasWarnings());
        Assert.assertTrue(violation.getWarnings().containsKey(MUNIC_ID));
        Assert.assertTrue(violation.getWarnings()
            .get(MUNIC_ID).contains(StatusCodes.GEO_COORD_UNCHECKED));
    }

    /**
     * Test fuzzy site object close to the admin border.
     */
    public void fuzzySiteOutsiteAdminBorders() {
        GeometryFactory gf = new GeometryFactory();
        Point justOutsideBorder = gf.createPoint(new Coordinate(
            COORDINATE_JUST_OUTSIDE_X, COORDINATE_JUST_OUTSIDE_Y));
        Site site = new Site();
        site.setAdminUnitId(VALID_ADMIN_UNIT_ID);
        site.setIsFuzzy(true);
        site.setGeom(justOutsideBorder);
        site.setNetworkId(EXISTING_NETWORK_ID);

        Violation violation = validator.validate(site);
        if (violation.hasWarnings()) {
            Assert.assertTrue(
                !violation.getWarnings().containsKey(COORD_X_EXT)
                || !violation.getWarnings().get(COORD_X_EXT)
                    .contains(StatusCodes.GEO_POINT_OUTSIDE));
            Assert.assertTrue(
                !violation.getWarnings().containsKey(COORD_Y_EXT)
                || !violation.getWarnings().get(COORD_Y_EXT)
                    .contains(StatusCodes.GEO_POINT_OUTSIDE));
        }
    }

    /**
     * Test site object far outside to the admin border.
     */
    public void siteOutsiteAdminBorders() {
        GeometryFactory gf = new GeometryFactory();
        Point justOutsideBorder = gf.createPoint(new Coordinate(
            COORDINATE_OUTSIDE_X, COORDINATE_OUTSIDE_Y));
        Site site = new Site();
        site.setAdminUnitId(VALID_ADMIN_UNIT_ID);
        site.setIsFuzzy(false);
        site.setGeom(justOutsideBorder);
        site.setNetworkId(EXISTING_NETWORK_ID);

        Violation violation = validator.validate(site);
        Assert.assertTrue(violation.hasWarnings());
        Assert.assertTrue(violation.getWarnings().containsKey(COORD_X_EXT));
        Assert.assertTrue(violation.getWarnings()
            .get(COORD_X_EXT).contains(StatusCodes.GEO_NOT_MATCHING));
        Assert.assertTrue(violation.getWarnings().containsKey(COORD_Y_EXT));
        Assert.assertTrue(violation.getWarnings()
            .get(COORD_Y_EXT).contains(StatusCodes.GEO_NOT_MATCHING));
    }

    /**
     * Test site inside admin borders.
     */
    public void siteInsideAdminBorders() {
        GeometryFactory gf = new GeometryFactory();
        Point insideBorder = gf.createPoint(
                new Coordinate(COORDINATE_INSIDE_X, COORDINATE_INSIDE_Y));
        Site site = new Site();
        site.setAdminUnitId(VALID_ADMIN_UNIT_ID);
        site.setIsFuzzy(false);
        site.setGeom(insideBorder);
        site.setNetworkId(EXISTING_NETWORK_ID);

        Violation violation = validator.validate(site);
        if (violation.hasWarnings()) {
            Assert.assertTrue(
                !violation.getWarnings().containsKey(COORD_X_EXT)
                || !violation.getWarnings().get(COORD_X_EXT)
                    .contains(StatusCodes.GEO_POINT_OUTSIDE));
            Assert.assertTrue(
                !violation.getWarnings().containsKey(COORD_Y_EXT)
                || !violation.getWarnings().get(COORD_Y_EXT)
                    .contains(StatusCodes.GEO_POINT_OUTSIDE));
        }
    }

    /**
     * Test site with duplicate extId.
     */
    public void duplicateExtId() {
        Site site = new Site();
        site.setNetworkId(EXISTING_NETWORK_ID);
        site.setExtId("D_00191");

        Violation violation = validator.validate(site);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors().containsKey(EXT_ID));
        Assert.assertTrue(violation.getErrors().get(EXT_ID)
            .contains(String.valueOf(StatusCodes.IMP_DUPLICATE)));
    }

    /**
     * Test site with unique extId.
     */
    public void uniqueExtId() {
        Site site = new Site();
        site.setNetworkId(EXISTING_NETWORK_ID);
        site.setExtId("D_00192");

        Violation violation = validator.validate(site);
        if (violation.hasErrors()) {
            Assert.assertTrue(
                !violation.getErrors().containsKey(EXT_ID)
                || !violation.getErrors().get(EXT_ID)
                    .contains(String.valueOf(StatusCodes.IMP_DUPLICATE)));
        }
    }

    /**
     * Test site with a non existing site class.
     */
    public void siteClassDoesNotExist() {
        Site site = new Site();
        site.setSiteClassId(0);
        site.setNetworkId(EXISTING_NETWORK_ID);

        Violation violation = validator.validate(site);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors().containsKey(SITE_CLASS_ID));
        Assert.assertTrue(violation.getErrors().get(SITE_CLASS_ID)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test site with valid site class.
     */
    public void siteClassDoesExist() {
        Site site = new Site();
        site.setSiteClassId(1);
        site.setNetworkId(EXISTING_NETWORK_ID);

        Violation violation = validator.validate(site);
        if (violation.hasErrors()) {
            Assert.assertTrue(
                !violation.getErrors().containsKey(SITE_CLASS_ID)
                || !violation.getErrors().get(SITE_CLASS_ID)
                    .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
        }
    }

    /**
     * Test REI site with 3 character extId.
     */
    public void reiSiteExtIdTooShort() {
        Site site = new Site();
        site.setExtId(NUCL_FACIL_EXT_ID_SHORT);
        site.setSiteClassId(SITE_CLASS_REI);
        site.setNuclFacilGrId(NUCL_FACIL_GR_ID_MAPPED);
        site.setNetworkId(EXISTING_NETWORK_ID);

        Violation violation = validator.validate(site);
        Assert.assertTrue(violation.hasWarnings());
        Assert.assertTrue(violation.getWarnings().containsKey(EXT_ID));
        Assert.assertTrue(violation.getWarnings().get(EXT_ID)
            .contains(StatusCodes.VALUE_OUTSIDE_RANGE));
    }

    /**
     * Test REI site without nucl facils.
     */
    public void reiSiteNoNuclFacils() {
        Site site = new Site();
        site.setExtId("1234");
        site.setSiteClassId(SITE_CLASS_REI);
        site.setNuclFacilGrId(NUCL_FACIL_GR_ID_MAPPED);
        site.setNetworkId(EXISTING_NETWORK_ID);

        Violation violation = validator.validate(site);
        Assert.assertTrue(violation.hasWarnings());
        Assert.assertTrue(violation.getWarnings().containsKey(EXT_ID));
        Assert.assertTrue(violation.getWarnings().get(EXT_ID)
            .contains(StatusCodes.ORT_ANLAGE_MISSING));
    }

    /**
     * Test REI site which ext id points to NuclFacil that is not connected to
     * its nuclFacilGrId.
     */
    public void reiSiteNuclFacilWithoutMappingEntry() {
        Site site = new Site();
        site.setExtId(NUCL_FACIL_EXT_ID_UNMAPPED);
        site.setNuclFacilGrId(NUCL_FACIL_GR_ID_MAPPED);
        site.setSiteClassId(SITE_CLASS_REI);
        site.setNetworkId(EXISTING_NETWORK_ID);

        Violation violation = validator.validate(site);
        Assert.assertTrue(violation.hasWarnings());
        Assert.assertTrue(violation.getWarnings()
            .containsKey(REI_NUCL_FACIL_GR_ID));
        Assert.assertTrue(violation.getWarnings().get(REI_NUCL_FACIL_GR_ID)
            .contains(StatusCodes.VALUE_NOT_MATCHING));
    }

    /**
     * Test rei site without nuclFacilGrId.
     */
    public void reiSiteWithoutNuclFacilGrId() {
        Site site = new Site();
        site.setSiteClassId(SITE_CLASS_REI);
        site.setNetworkId(EXISTING_NETWORK_ID);

        Violation violation = validator.validate(site);
        Assert.assertTrue(violation.hasWarnings());
        Assert.assertTrue(violation.getWarnings()
            .containsKey(KTA_GRUPPE_ID));
        Assert.assertTrue(violation.getWarnings().get(KTA_GRUPPE_ID)
            .contains(StatusCodes.VALUE_MISSING));
    }

    /**
     * Test rei site.
     */
    public void reiSite() {
        Site site = new Site();
        site.setSiteClassId(SITE_CLASS_REI);
        site.setExtId(NUCL_FACIL_EXT_ID_MAPPED);
        site.setNuclFacilGrId(1);
        site.setNetworkId(EXISTING_NETWORK_ID);

        Violation violation = validator.validate(site);
        if (violation.hasWarnings()) {
            Assert.assertFalse(violation.getWarnings()
                .containsKey(REI_NUCL_FACIL_GR_ID));
        }
    }
}
