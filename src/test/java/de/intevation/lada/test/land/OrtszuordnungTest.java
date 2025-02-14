/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;

import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.Geolocat_;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.Site_;
import de.intevation.lada.rest.GeolocatService;
import de.intevation.lada.rest.SiteService;
import de.intevation.lada.test.ServiceTest;

/**
 * Test ortzuordnung entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class OrtszuordnungTest extends ServiceTest {

    private JsonObject expectedSite;
    private JsonObject expectedById;
    private JsonObject create;

    private final int expectedId = 1000;

    final String urlPath = UriBuilder.fromResource(GeolocatService.class)
        .build().getPath() + "/";

    @Override
    public void init(WebTarget t) {
        super.init(t);

        // Attributes to be converted
        timestampAttributes = List.of("lastMod");
        geomPointAttributes = List.of("geom");

        final int expectedSiteId = 1000;
        expectedSite = convertObject(filterJsonArrayById(
                readXmlResource("datasets/dbUnit_lada.xml", Site.class),
                expectedSiteId)).build();
        expectedById = convertObject(filterJsonArrayById(
                readXmlResource("datasets/dbUnit_lada.xml", Geolocat.class),
                expectedId))
            .add(Geolocat_.SITE, expectedSite)
            .add("readonly", JsonValue.FALSE)
            .add("owner", JsonValue.TRUE)
            .build();

        // Load probe object to test POST request
        create = Json.createObjectBuilder(
            readJsonResource("/datasets/ortszuordnung.json"))
            .add(Geolocat_.SITE, expectedSite)
            .build();
        Assert.assertNotNull(create);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        get(urlPath + "?sampleId=1000");
        getById(urlPath + expectedId, expectedById);

        // Test creating Geolocat including associated Site
        JsonObject created = create(urlPath, create);
        verify(expectedSite, created.getJsonObject(Geolocat_.SITE));
        // Ensure Site is persistently associated
        final int newId = created.getInt(Geolocat_.ID);
        getById(urlPath + newId, created, "referenceCount");

        // Update basic value
        update(
            urlPath + expectedId,
            Geolocat_.ADD_SITE_TEXT,
            expectedById.getString(Geolocat_.ADD_SITE_TEXT),
            "Test geändert");
        // Update association
        final int newSiteId = 1001;
        update(
            urlPath + expectedId,
            Geolocat_.SITE,
            expectedSite,
            convertObject(filterJsonArrayById(
                    readXmlResource("datasets/dbUnit_lada.xml", Site.class),
                    newSiteId)).build());

        delete(urlPath + newId);
        // Ensure previously associated site is still persistent
        get(UriBuilder.fromResource(SiteService.class)
            .path(SiteService.class, "getById")
            .resolveTemplate("id", expectedSite.getInt(Site_.ID))
            .build().getPath());

        // Ensure that pre-existing site is a prerequisite
        Geolocat loc = get(urlPath + expectedId, Geolocat.class);
        // set sampleId for serialization
        final int expectedSampleId = 1000;
        loc.getSample().setId(expectedSampleId);

        // Associate site without ID
        Site site = new Site();
        site.setNetworkId("06");
        loc.setSite(site);
        assertSiteMustExist(loc);

        // Associate site with not existing ID
        final int notExistingSiteId = 9999;
        site.setId(notExistingSiteId);
        assertSiteMustExist(loc);
    }

    private void assertSiteMustExist(Geolocat loc) {
        create(urlPath, loc, Status.NOT_FOUND);
        BaseTest.parseResponse(target.path(urlPath + expectedId)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .accept(MediaType.APPLICATION_JSON)
            .put(Entity.entity(loc, MediaType.APPLICATION_JSON)),
            Status.NOT_FOUND);
    }
}
