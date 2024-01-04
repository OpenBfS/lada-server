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

import java.util.ResourceBundle;

import org.junit.Assert;

import de.intevation.lada.model.master.Site;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;

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
    private static final String KTA_GRUPPE_ID = "nuclFacilGrId";
    private static final String REI_NUCL_FACIL_GR_ID = "reiNuclFacilGrId";
    private static final String SITE_CLASS_ID = "siteClassId";

    //Other constants
    private static final String INVALID_ADMIN_UNIT_ID = "420042";
    private static final String VALID_ADMIN_UNIT_ID = "11000000";

    private static final String EXAMPLE_EXT_ID = "1234";
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

    //Expected validation messages
    private static final String UNIQUE_PLACEHOLDER = "{fields}";
    private final String valMessageUniqueExtId;

    /**
     * Constructor.
     */
    public SiteTest() {
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
    public void adminUnitWithoutAdminBorders() {
        GeometryFactory gf = new GeometryFactory();
        Point insideBorder = gf.createPoint(
                new Coordinate(COORDINATE_INSIDE_X, COORDINATE_INSIDE_Y));
        Site site = new Site();
        site.setAdminUnitId(INVALID_ADMIN_UNIT_ID);
        site.setGeom(insideBorder);
        site.setNetworkId(EXISTING_NETWORK_ID);
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
    public void fuzzySiteOutsiteAdminBorders() {
        GeometryFactory gf = new GeometryFactory();
        Point justOutsideBorder = gf.createPoint(new Coordinate(
            COORDINATE_JUST_OUTSIDE_X, COORDINATE_JUST_OUTSIDE_Y));
        Site site = new Site();
        site.setAdminUnitId(VALID_ADMIN_UNIT_ID);
        site.setIsFuzzy(true);
        site.setGeom(justOutsideBorder);
        site.setNetworkId(EXISTING_NETWORK_ID);

        validator.validate(site);
        if (site.hasWarnings()) {
            Assert.assertTrue(
                !site.getWarnings().containsKey(COORD_X_EXT)
                || !site.getWarnings().get(COORD_X_EXT)
                    .contains(String.valueOf(StatusCodes.GEO_POINT_OUTSIDE)));
            Assert.assertTrue(
                !site.getWarnings().containsKey(COORD_Y_EXT)
                || !site.getWarnings().get(COORD_Y_EXT)
                    .contains(String.valueOf(StatusCodes.GEO_POINT_OUTSIDE)));
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

        validator.validate(site);
        Assert.assertTrue(site.hasWarnings());
        Assert.assertTrue(site.getWarnings().containsKey(COORD_X_EXT));
        Assert.assertTrue(site.getWarnings().get(COORD_X_EXT).contains(
                String.valueOf(StatusCodes.GEO_NOT_MATCHING)));
        Assert.assertTrue(site.getWarnings().containsKey(COORD_Y_EXT));
        Assert.assertTrue(site.getWarnings().get(COORD_Y_EXT).contains(
                String.valueOf(StatusCodes.GEO_NOT_MATCHING)));
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

        validator.validate(site);
        if (site.hasWarnings()) {
            Assert.assertTrue(
                !site.getWarnings().containsKey(COORD_X_EXT)
                || !site.getWarnings().get(COORD_X_EXT)
                    .contains(String.valueOf(StatusCodes.GEO_POINT_OUTSIDE)));
            Assert.assertTrue(
                !site.getWarnings().containsKey(COORD_Y_EXT)
                || !site.getWarnings().get(COORD_Y_EXT)
                    .contains(String.valueOf(StatusCodes.GEO_POINT_OUTSIDE)));
        }
    }

    /**
     * Test site with duplicate extId.
     */
    public void duplicateExtId() {
        Site site = new Site();
        site.setNetworkId(EXISTING_NETWORK_ID);
        site.setExtId("D_00191");

        validator.validate(site);
        Assert.assertTrue(site.hasErrors());
        Assert.assertTrue(site.getErrors().containsKey(EXT_ID));
        Assert.assertEquals(valMessageUniqueExtId,
            site.getErrors().get(EXT_ID).get(0));
    }

    /**
     * Test site with unique extId.
     */
    public void uniqueExtId() {
        Site site = new Site();
        site.setNetworkId(EXISTING_NETWORK_ID);
        site.setExtId("D_00192");

        validator.validate(site);
        if (site.hasErrors()) {
            Assert.assertTrue(
                !site.getErrors().containsKey(EXT_ID)
                || !site.getErrors().get(EXT_ID)
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

        validator.validate(site);
        Assert.assertTrue(site.hasErrors());
        Assert.assertTrue(site.getErrors().containsKey(SITE_CLASS_ID));
        Assert.assertTrue(site.getErrors().get(SITE_CLASS_ID)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test site with valid site class.
     */
    public void siteClassDoesExist() {
        Site site = new Site();
        site.setSiteClassId(1);
        site.setNetworkId(EXISTING_NETWORK_ID);

        validator.validate(site);
        if (site.hasErrors()) {
            Assert.assertTrue(
                !site.getErrors().containsKey(SITE_CLASS_ID)
                || !site.getErrors().get(SITE_CLASS_ID)
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

        validator.validate(site);
        Assert.assertTrue(site.hasWarnings());
        Assert.assertTrue(site.getWarnings().containsKey(EXT_ID));
        Assert.assertTrue(site.getWarnings().get(EXT_ID)
            .contains(String.valueOf(StatusCodes.VALUE_OUTSIDE_RANGE)));
    }

    /**
     * Test REI site without nucl facils.
     */
    public void reiSiteNoNuclFacils() {
        Site site = new Site();
        site.setExtId(EXAMPLE_EXT_ID);
        site.setSiteClassId(SITE_CLASS_REI);
        site.setNuclFacilGrId(NUCL_FACIL_GR_ID_MAPPED);
        site.setNetworkId(EXISTING_NETWORK_ID);

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
    public void reiSiteNuclFacilWithoutMappingEntry() {
        Site site = new Site();
        site.setExtId(NUCL_FACIL_EXT_ID_UNMAPPED);
        site.setNuclFacilGrId(NUCL_FACIL_GR_ID_MAPPED);
        site.setSiteClassId(SITE_CLASS_REI);
        site.setNetworkId(EXISTING_NETWORK_ID);

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
    public void reiSiteWithoutNuclFacilGrId() {
        Site site = new Site();
        site.setExtId(EXAMPLE_EXT_ID);
        site.setSiteClassId(SITE_CLASS_REI);
        site.setNetworkId(EXISTING_NETWORK_ID);

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
    public void reiSite() {
        Site site = new Site();
        site.setSiteClassId(SITE_CLASS_REI);
        site.setExtId(NUCL_FACIL_EXT_ID_MAPPED);
        site.setNuclFacilGrId(1);
        site.setNetworkId(EXISTING_NETWORK_ID);

        validator.validate(site);
        if (site.hasWarnings()) {
            Assert.assertFalse(site.getWarnings()
                .containsKey(REI_NUCL_FACIL_GR_ID));
        }
    }
}
