/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.lada.StatusProt_;


/**
 * Test Status entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class StatusTest extends ValidatorBaseTest {

    //Other constants
    private static final int ID3 = 3;

    private static final int INVALID_STATUS_MP_ID = 42;

    private static final String MSG_KEY = "status";

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
     * Test setting status "plausible" of measm with error, warning
     * and notification at measm.
     */
    @Test
    public void measmWithMessages() {
        StatusProt status = minimalStatusProt();
        final int invalidMeasmId = 1201;
        status.setMeasmId(invalidMeasmId);
        assertHasErrors(
            validator.validate(status),
            MSG_KEY,
            "Operation not possible due to constraint violations\n"
            + "Errors:\n"
            + "- measVal: [631]\n"
            + "Warnings:\n"
            + "- measmStartDate: [A value must be provided]\n"
            + "Notifications:\n"
            + "- minSampleId: [No value provided]"
        );
    }

    /**
     * Test setting status "not plausible" of measm with error, warning
     * and notification at measm.
     */
    @Test
    public void measmWithMessagesNotPlausible() {
        StatusProt status = minimalStatusProt();
        final int notPlausible = 4, invalidMeasmId = 1201;
        status.setStatusMpId(notPlausible);
        status.setMeasmId(invalidMeasmId);
        assertNoMessages(validator.validate(status));
    }

    /**
     * Test status of measm with warning and notification at measm.
     */
    @Test
    public void measmWithWarnings() {
        StatusProt status = minimalStatusProt();
        final int invalidMeasmId = 1202;
        status.setMeasmId(invalidMeasmId);
        assertHasErrors(
            validator.validate(status),
            MSG_KEY,
            "Operation not possible due to constraint violations\n"
            + "Warnings:\n"
            + "- measmStartDate: [A value must be provided]\n"
            + "Notifications:\n"
            + "- minSampleId: [No value provided]"
        );
    }

    /**
     * Test status of measm with notification at measm.
     */
    @Test
    public void measmWithNotifications() {
        StatusProt status = minimalStatusProt();
        final int measmId = 1203;
        status.setMeasmId(measmId);
        assertHasNotifications(
            validator.validate(status),
            MSG_KEY,
            "Notifications:\n"
            + "- minSampleId: [No value provided]"
        );
    }

    /**
     * Test status of measm with class-level warning at associated sample.
     */
    @Test
    public void sampleWithClassWarning() {
        StatusProt status = minimalStatusProt();
        final int measmId = 1210;
        status.setMeasmId(measmId);
        assertHasErrors(
            validator.validate(status),
            MSG_KEY,
            "Operation not possible due to constraint violations\n"
            + "Warnings:\n"
            + "- sampleMethId: [Individual sample expects "
            + "sample start date = sample end date]"
        );
    }

    /**
     * Test status of measm with attribute-level warning at associated sample.
     */
    @Test
    public void sampleWithAttrWarning() {
        StatusProt status = minimalStatusProt();
        final int measmId = 1211;
        status.setMeasmId(measmId);
        assertHasErrors(
            validator.validate(status),
            MSG_KEY,
            "Operation not possible due to constraint violations\n"
            + "Warnings:\n"
            + "- envDescripDisplay: "
            + "[No value provided, Invalid descriptor combination]"
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
