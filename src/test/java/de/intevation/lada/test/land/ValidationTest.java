/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.land;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Response.Status;

import org.jboss.resteasy.api.validation.Validation;
import org.junit.Assert;

import de.intevation.lada.BaseTest;
import de.intevation.lada.ClientBaseTest;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.rest.SampleService;
import de.intevation.lada.test.ServiceTest;

/**
 * Test aspects of validation of service requests.
 */
public class ValidationTest extends ServiceTest {

    private static final String SAMPLE_SERVICE_URL =
        UriBuilder.fromResource(SampleService.class).build() + "/";

    private static final int SAMPLE_ID = 1000;

    @Override
    public void init(WebTarget t) {
        super.init(t);
    }

    /**
     * Execute the tests.
     */
    public final void execute() {
        // PUT with not existing ID in path
        final Sample sample = get(SAMPLE_SERVICE_URL + SAMPLE_ID, Sample.class);
        final int nonExistantID = 99999999;
        Invocation.Builder builderNonExistant = target
            .path(SAMPLE_SERVICE_URL + nonExistantID)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .accept(MediaType.APPLICATION_JSON);

        // IDs in path and payload do not match
        final Response response400 = builderNonExistant
            .put(Entity.entity(sample, MediaType.APPLICATION_JSON));
        assertNoValidationError(response400);
        ClientBaseTest.parseResponse(response400, Status.BAD_REQUEST);

        // IDs in path and payload match
        sample.setId(nonExistantID);
        sample.setExtId("xxx"); // Prevents validation error
        sample.setIsTest(true); // Prevents validation error
        final Response response404 = builderNonExistant
            .put(Entity.entity(sample, MediaType.APPLICATION_JSON));
        assertNoValidationError(response404);
        ClientBaseTest.parseResponse(response404, Status.NOT_FOUND);


        // PUT with payload not matching ID in path
        Invocation.Builder builder = target
            .path(SAMPLE_SERVICE_URL + SAMPLE_ID)
            .request()
            .header("X-SHIB-user", BaseTest.testUser)
            .header("X-SHIB-roles", BaseTest.testRoles)
            .accept(MediaType.APPLICATION_JSON);

        // existing sample in payload should remain unaltered
        final int anotherSampleId = 999;
        final Sample anotherSample = get(
            SAMPLE_SERVICE_URL + anotherSampleId, Sample.class);
        Integer regIdBefore = anotherSample.getRegulationId();
        anotherSample.setRegulationId(regIdBefore + 1);
        final Response responseExistant = builder
            .put(Entity.entity(anotherSample, MediaType.APPLICATION_JSON));
        assertNoValidationError(responseExistant);
        ClientBaseTest.parseResponse(responseExistant, Status.BAD_REQUEST);
        final Sample anotherSampleAfter = get(
            SAMPLE_SERVICE_URL + anotherSampleId, Sample.class);
        Assert.assertEquals(
            regIdBefore, anotherSampleAfter.getRegulationId());

        // non-existant sample in payload should not become persistent
        sample.setId(nonExistantID);
        sample.setExtId("xxx"); // Prevents validation error
        sample.setIsTest(true); // Prevents validation error
        final Response responseNonExistant = builder
            .put(Entity.entity(sample, MediaType.APPLICATION_JSON));
        assertNoValidationError(responseNonExistant);
        ClientBaseTest.parseResponse(responseNonExistant, Status.BAD_REQUEST);
        get(SAMPLE_SERVICE_URL + nonExistantID, Status.NOT_FOUND);

        // No ID in payload
        sample.setId(null);
        final Response responseNoId = builder
            .put(Entity.entity(sample, MediaType.APPLICATION_JSON));
        assertNoValidationError(responseNoId);
        ClientBaseTest.parseResponse(responseNoId, Status.BAD_REQUEST);
    }

    private void assertNoValidationError(Response response) {
        Assert.assertFalse("Received validation error",
            response.getHeaders().containsKey(Validation.VALIDATION_HEADER));
    }
}
