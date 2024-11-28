/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import java.util.ResourceBundle;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.Site_;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * Test validation rules for Site objects.
 */
public class SiteTest extends ValidatorBaseTest {

    //Other constants
    private static final int SPAT_REF_SYS_ID = 5;
    private static final String INVALID_ADMIN_UNIT_ID = "420042";
    private static final String VALID_ADMIN_UNIT_ID = "11000000";

    private static final String EXAMPLE_EXT_ID = "1234";

    private static final String NUCL_FACIL_EXT_ID_UNMAPPED = "Othr";
    private static final int NUCL_FACIL_GR_ID_MAPPED = 1;

    private static final double COORDINATE_OUTSIDE_Y = 48.0;
    private static final double COORDINATE_OUTSIDE_X = 11.0;
    private static final double COORDINATE_JUST_OUTSIDE_Y = 52.399;
    private static final double COORDINATE_JUST_OUTSIDE_X = 13.099;
    private static final double COORDINATE_INSIDE_Y = 52.5;
    private static final double COORDINATE_INSIDE_X = 13.4;

    private static final String VALID_UTM_X = "12345678.1";
    private static final String VALID_UTM_Y = "1234567.1";
    private static final String INVALID_UTM_X = "123456789.1";
    private static final String INVALID_UTM_Y = "12345.1";

    private static final int SPAT_REF_SYS_ID_GK = 1;
    private static final int SPAT_REF_SYS_ID_GS = 2;
    private static final int SPAT_REF_SYS_ID_GD = 4;
    private static final int SPAT_REF_SYS_ID_UTM_WGS_84 = 5;
    private static final int SPAT_REF_SYS_ID_UTM_ETRS89 = 6;
    private static final int SPAT_REF_SYS_ID_UTM_ED50 = 8;

    //Expected validation messages
    private static final String GEO_POINT_OUTSIDE =
        "Coordintes outside of border of administrative unit";
    private static final String VALUE_MISSING = "A value must be provided";
    private static final String EXT_ID_NOT_MATCHING_NUCL_FACIL =
        "First four characters must match nuclear facility in given group";
    private static final String UNIQUE_PLACEHOLDER = "{fields}";
    private final String valMessageUniqueExtId;
    private final String valMessageCoords;

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
        valMessageCoords = validationMessages.getString(
            "de.intevation.lada.validation.constraints.ValidCoordinates."
            + "message");

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

        assertHasWarnings(
            validator.validate(site),
            Site_.ADMIN_UNIT_ID,
            "No border of administrative unit found. Coordinates unchecked");
    }

    /**
     * Test fuzzy site object close to the admin border.
     */
    @Test
    public void fuzzySiteCloseToAdminBorders() {
        GeometryFactory gf = new GeometryFactory();
        Point justOutsideBorder = gf.createPoint(new Coordinate(
            COORDINATE_JUST_OUTSIDE_X, COORDINATE_JUST_OUTSIDE_Y));
        Site site = createMinimalSite();
        site.setAdminUnitId(VALID_ADMIN_UNIT_ID);
        site.setIsFuzzy(true);
        site.setGeom(justOutsideBorder);

        validator.validate(site);
        assertNoMessages(site);
    }

    /**
     * Test fuzzy site object far off from admin border.
     */
    @Test
    public void fuzzySiteFarFromAdminBorders() {
        GeometryFactory gf = new GeometryFactory();
        Point outsideBorder = gf.createPoint(new Coordinate(
            COORDINATE_OUTSIDE_X, COORDINATE_OUTSIDE_Y));
        Site site = createMinimalSite();
        site.setAdminUnitId(VALID_ADMIN_UNIT_ID);
        site.setIsFuzzy(true);
        site.setGeom(outsideBorder);

        validator.validate(site);
        assertHasWarnings(site, Site_.COORD_XEXT, GEO_POINT_OUTSIDE);
        assertHasWarnings(site, Site_.COORD_YEXT, GEO_POINT_OUTSIDE);
    }

    /**
     * Test site object far outside to the admin border.
     */
    @Test
    public void siteOutsiteAdminBorders() {
        GeometryFactory gf = new GeometryFactory();
        Point justOutsideBorder = gf.createPoint(new Coordinate(
            COORDINATE_JUST_OUTSIDE_X, COORDINATE_JUST_OUTSIDE_Y));
        Site site = createMinimalSite();
        site.setAdminUnitId(VALID_ADMIN_UNIT_ID);
        site.setIsFuzzy(false);
        site.setGeom(justOutsideBorder);

        validator.validate(site);
        assertHasWarnings(site, Site_.COORD_XEXT, GEO_POINT_OUTSIDE);
        assertHasWarnings(site, Site_.COORD_YEXT, GEO_POINT_OUTSIDE);
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
        assertNoMessages(site);
    }

    /**
     * Test fuzzy site inside admin borders.
     */
    @Test
    public void fuzzySiteInsideAdminBorders() {
        GeometryFactory gf = new GeometryFactory();
        Point insideBorder = gf.createPoint(
                new Coordinate(COORDINATE_INSIDE_X, COORDINATE_INSIDE_Y));
        Site site = createMinimalSite();
        site.setAdminUnitId(VALID_ADMIN_UNIT_ID);
        site.setIsFuzzy(true);
        site.setGeom(insideBorder);

        validator.validate(site);
        assertNoMessages(site);
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
        Assert.assertTrue(site.getErrors().containsKey(Site_.EXT_ID));
        MatcherAssert.assertThat(
            site.getErrors().get(Site_.EXT_ID),
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
        assertNoMessages(site);
    }

    /**
     * Test REI site with less than five characters in extId.
     */
    @Test
    public void reiSiteExtIdTooShort() {
        Site site = createMinimalSite();
        site.setExtId("xxxx");
        site.setSiteClassId(Site.SiteClassId.REI);
        site.setNuclFacilGrId(NUCL_FACIL_GR_ID_MAPPED);

        assertHasWarnings(
            validator.validate(site),
            Site_.EXT_ID,
            "Must be between five and twelve characters long");
    }

    /**
     * Test REI site for which extId does not point to existing NuclFacil.
     */
    @Test
    public void reiSiteNoNuclFacils() {
        Site site = createMinimalSite();
        site.setExtId(EXAMPLE_EXT_ID);
        site.setSiteClassId(Site.SiteClassId.REI);
        site.setNuclFacilGrId(NUCL_FACIL_GR_ID_MAPPED);

        assertHasWarnings(
            validator.validate(site),
            Site_.EXT_ID,
            EXT_ID_NOT_MATCHING_NUCL_FACIL);
    }

    /**
     * Test REI site for which extId points to NuclFacil not connected to
     * given nuclFacilGrId.
     */
    @Test
    public void reiSiteNuclFacilWithoutMappingEntry() {
        Site site = createMinimalSite();
        site.setExtId(NUCL_FACIL_EXT_ID_UNMAPPED);
        site.setNuclFacilGrId(NUCL_FACIL_GR_ID_MAPPED);
        site.setSiteClassId(Site.SiteClassId.REI);

        assertHasWarnings(
            validator.validate(site),
            Site_.EXT_ID,
            EXT_ID_NOT_MATCHING_NUCL_FACIL);
    }

    /**
     * Test rei site without nuclFacilGrId.
     */
    @Test
    public void reiSiteWithoutNuclFacilGrId() {
        Site site = createMinimalSite();
        site.setExtId(EXAMPLE_EXT_ID);
        site.setSiteClassId(Site.SiteClassId.REI);

        assertHasWarnings(
            validator.validate(site),
            "nuclFacilGrId",
            VALUE_MISSING);
    }

    /**
     * Test rei site.
     */
    @Test
    public void reiSite() {
        Site site = createMinimalSite();
        site.setSiteClassId(Site.SiteClassId.REI);
        site.setExtId("A1234");
        site.setNuclFacilGrId(NUCL_FACIL_GR_ID_MAPPED);

        validator.validate(site);
        assertNoMessages(site);
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
        assertHasErrors(site, "coordinates", msg);
        assertHasErrors(site, Site_.ADMIN_UNIT_ID, msg);
        assertHasErrors(site, "stateId", msg);
    }

    @Test
    public void hasCoords() {
        Site site = createMinimalSite();
        site.setStateId(null);
        site.setSpatRefSysId(SPAT_REF_SYS_ID);
        site.setCoordXExt("5650300.787");
        site.setCoordYExt("570168.862");

        assertNoMessages(validator.validate(site));
    }

    @Test
    public void hasAdminUnit() {
        Site site = createMinimalSite();
        site.setStateId(null);
        site.setAdminUnitId(VALID_ADMIN_UNIT_ID);

        assertNoMessages(validator.validate(site));
    }

    @Test
    public void hasState() {
        Site site = createMinimalSite();

        assertNoMessages(validator.validate(site));
    }

    @Test
    public void routeContainsNewline() {
        Site site = createMinimalSite();
        final String textWithNewline = """
            Test
            with newline""";
        site.setRoute(textWithNewline);

        assertNoMessages(validator.validate(site));
    }

    /**
     * Test valid GK Coords.
     */
    @Test
    public void validGKCoordinates() {
        Site site = createMinimalSite();
        site.setCoordXExt("3570272.656");
        site.setCoordYExt("5652121.859");
        site.setSpatRefSysId(SPAT_REF_SYS_ID_GK);
        validator.validate(site);
        assertNoMessages(site);
    }

    /**
     * Test invalid GK Coords.
     */
    @Test
    public void invalidGKCoordinates() {
        Site site = createMinimalSite();
        site.setCoordXExt("1234567891.656");
        site.setCoordYExt("12345.859");
        site.setSpatRefSysId(SPAT_REF_SYS_ID_GK);
        validator.validate(site);
        assertCoordErrors(site);
    }

    /**
     * Test valid sexagesimal Coordinates with decimal separator.
     */
    @Test
    public void validGSCoordinatesDec() {
        Site site = createMinimalSite();
        site.setCoordXExt("102811.8956E");
        site.setCoordYExt("51612.6792N");
        site.setSpatRefSysId(SPAT_REF_SYS_ID_GS);
        validator.validate(site);
        assertNoMessages(site);
    }

    /**
     * Test invalid sexagesimal coordinates with decimal separator.
     */
    @Test
    public void invalidGSCordinatesDec() {
        Site site = createMinimalSite();
        site.setCoordXExt("51.511111X");
        site.setCoordYExt("51.511111Y");
        site.setSpatRefSysId(SPAT_REF_SYS_ID_GS);
        validator.validate(site);
        assertCoordErrors(site);
    }

    /**
     * Test valid sexagesimal Coordinates without decimal separator.
     */
    @Test
    public void validGSCoordinatesWithoutDec() {
        Site site = createMinimalSite();
        site.setCoordXExt("1231212E");
        site.setCoordYExt("121212N");
        site.setSpatRefSysId(SPAT_REF_SYS_ID_GS);
        validator.validate(site);
        assertNoMessages(site);
    }

    /**
     * Test invalid sexagesimal Coordinates without decimal separator.
     */
    @Test
    public void invalidGSCoordinatesWithoutDec() {
        Site site = createMinimalSite();
        site.setCoordXExt("12312123X");
        site.setCoordYExt("1212123Y");
        site.setSpatRefSysId(SPAT_REF_SYS_ID_GS);
        validator.validate(site);
        assertCoordErrors(site);
    }

    /**
     * Test valid GD coords.
     */
    @Test
    public void validGDCoordinates() {
        Site site = createMinimalSite();
        site.setCoordXExt("51.1111");
        site.setCoordYExt("10.1111");
        site.setSpatRefSysId(SPAT_REF_SYS_ID_GD);
        validator.validate(site);
        assertNoMessages(site);
    }

    /**
     * Test invalid gd coords.
     */
    @Test
    public void invalidGDCoordinates() {
        Site site = createMinimalSite();
        site.setCoordXExt("-181");
        site.setCoordYExt("99");
        site.setSpatRefSysId(SPAT_REF_SYS_ID_GD);
        validator.validate(site);
        assertCoordErrors(site);
    }

    /**
     * Test valid UTM WGS84 coordinates.
     */
    public void validUtmWgs84() {
        testValidUtmCoordinates(SPAT_REF_SYS_ID_UTM_WGS_84);
    }

    /**
     * Test invalid UTM WGS84 coordinates.
     */
    public void invalidUtmWgs84() {
        testInvalidUtmCoordinates(SPAT_REF_SYS_ID_UTM_WGS_84);
    }

    /**
     * Test valid UTM ETRS89 coordinates.
     */
    public void validUtmEtrs89() {
        testValidUtmCoordinates(SPAT_REF_SYS_ID_UTM_ETRS89);
    }

    /**
     * Test invalid UTM ETRS89 coordinates.
     */
    public void invalidEtrs89() {
        testInvalidUtmCoordinates(SPAT_REF_SYS_ID_UTM_ETRS89);
    }

    /**
     * Test valid UTM ED50 coordinates.
     */
    public void validUtmEd50() {
        testValidUtmCoordinates(SPAT_REF_SYS_ID_UTM_ED50);
    }

    /**
     * Test invalid UTM ED50 coordinates.
     */
    public void invalidUtmEd50() {
        testInvalidUtmCoordinates(SPAT_REF_SYS_ID_UTM_ED50);
    }

    private void testValidUtmCoordinates(int spatRefSysId) {
        Site site = createMinimalSite();
        site.setCoordXExt(VALID_UTM_X);
        site.setCoordYExt(VALID_UTM_Y);
        site.setSpatRefSysId(spatRefSysId);
        validator.validate(site);
        assertNoMessages(site);
    }

    private void testInvalidUtmCoordinates(int spatRefSysId) {
        Site site = createMinimalSite();
        site.setCoordXExt(INVALID_UTM_X);
        site.setCoordYExt(INVALID_UTM_Y);
        site.setSpatRefSysId(spatRefSysId);
        validator.validate(site);
        assertCoordErrors(site);
    }

    private Site createMinimalSite() {
        Site site = new Site();
        site.setNetworkId("06");
        site.setStateId(0);
        site.setSiteClassId(Site.SiteClassId.DYN);
        return site;
    }

    private void assertCoordErrors(Site site) {
        assertHasErrors(site, Site_.COORD_XEXT, valMessageCoords);
        assertHasErrors(site, Site_.COORD_YEXT, valMessageCoords);
    }
}
