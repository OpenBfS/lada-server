/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import jakarta.inject.Inject;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.lada.StatusProt_;
import de.intevation.lada.validation.Validator;

/**
 * Test Status entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class StatusTest extends ValidatorBaseTest {

    //Other constants
    private static final int ID1 = 1;
    private static final int ID3 = 3;
    private static final int ID7 = 7;

    private static final int INVALID_STATUS_MP_ID = 42;

    @Inject
    private Validator validator;

    /**
     * Constructor.
     * Sets test dataset.
     */
    public StatusTest() {
        this.testDatasetName = "datasets/dbUnit_status_validator.xml";
    }

    /**
     * Test if status kombi is not existing.
     */
    @Test
    public void checkKombiNegative() {
        StatusProt status = minimalStatusProt();
        status.setStatusLev(2);
        status.setStatusVal(ID7);
        status.setStatusMpId(INVALID_STATUS_MP_ID);
        validator.validate(status);
        Assert.assertTrue(status.hasErrors());
        MatcherAssert.assertThat(status.getErrors().keySet(),
            CoreMatchers.hasItem(StatusProt_.STATUS_MP_ID));
        MatcherAssert.assertThat(
            status.getErrors().get(StatusProt_.STATUS_MP_ID),
            CoreMatchers.hasItem(
                "'" + INVALID_STATUS_MP_ID + "' is no valid primary key"));
    }

    /**
     * Test if status kombi is existing.
     */
    @Test
    public void checkKombiPositive() {
        StatusProt status = minimalStatusProt();
        status.setStatusLev(ID1);
        status.setStatusVal(ID1);

        validator.validate(status);
        assertNoMessages(status);
    }

    /**
     * Test status with invalid order.
     */
    @Test
    public void invalidStatusOrder() {
        StatusProt status = minimalStatusProt();
        status.setStatusMpId(ID3);

        assertHasErrors(
            validator.validate(status),
            StatusProt_.STATUS_MP_ID,
            "Values do not match");
    }

    /**
     * Test status with valid order and dependencies.
     */
    @Test
    public void validStatus() {
        StatusProt status = minimalStatusProt();

        validator.validate(status);
        assertNoMessages(status);
    }

    /**
     * Test status of measm connected to valid REI sample.
     */
    @Test
    public void statusReiCompleteSample() {
        StatusProt status = minimalStatusProt();
        final int existingMeasmIdValidReiSample = 4200;
        status.setMeasmId(existingMeasmIdValidReiSample);

        validator.validate(status);
        assertNoMessages(status);
    }

    /**
     * Test status of measm with error, warning and notification at measm.
     */
    @Test
    public void measmWithMessages() {
        StatusProt status = minimalStatusProt();
        final int invalidMeasmId = 1201;
        status.setMeasmId(invalidMeasmId);
        assertHasErrors(
            validator.validate(status),
            "status",
            "Operation not possible due to constraint violations\n"
            + "Errors:\n"
            + "- measVal: [631]\n"
            + "Warnings:\n"
            + "- measmStartDate: [A value must be provided]\n"
            + "Notifications:\n"
            + "- minSampleId: [must not be blank]"
        );
    }

    private StatusProt minimalStatusProt() {
        StatusProt status = new StatusProt();
        final int existingMeasmId = 1200;
        status.setMeasmId(existingMeasmId);
        status.setMeasFacilId("06010");
        status.setStatusMpId(2);
        return status;
    }
}
