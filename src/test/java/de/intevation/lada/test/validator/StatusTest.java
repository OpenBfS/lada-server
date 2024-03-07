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
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;

/**
 * Test Status entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class StatusTest extends ValidatorBaseTest {

    //Validator keys
    private static final String STATUS = "status";
    private static final String STATUS_MP_ID = "statusMpId";

    //Other constants
    private static final int ID1 = 1;
    private static final int ID2 = 2;
    private static final int ID3 = 3;
    private static final int ID7 = 7;

    private static final int EXISTING_MEASM_ID = 1200;
    private static final int EXISTING_MEASM_ID_VALID_REI_SAMPLE = 4200;
    private static final int INVALID_STATUS_MP_ID = 42;
    private static final String EXISTING_MEAS_FACIL_ID = "06010";

    @Inject
    private Validator<StatusProt> validator;

    /**
     * Test if status kombi is not existing.
     */
    @Test
    public void checkKombiNegative() {
        StatusProt status = new StatusProt();
        status.setMeasmId(EXISTING_MEASM_ID);
        status.setStatusLev(ID2);
        status.setStatusVal(ID7);
        status.setStatusMpId(INVALID_STATUS_MP_ID);
        status.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        validator.validate(status);
        Assert.assertTrue(status.hasErrors());
        MatcherAssert.assertThat(status.getErrors().keySet(),
            CoreMatchers.hasItem(STATUS_MP_ID));
        MatcherAssert.assertThat(
            status.getErrors().get(STATUS_MP_ID),
            CoreMatchers.hasItem(
                "'" + INVALID_STATUS_MP_ID + "' is no valid primary key"));
    }

    /**
     * Test if status kombi is existing.
     */
    @Test
    public void checkKombiPositive() {
        StatusProt status = new StatusProt();
        status.setMeasmId(EXISTING_MEASM_ID);
        status.setStatusLev(ID1);
        status.setStatusVal(ID1);
        status.setStatusMpId(2);
        status.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        validator.validate(status);
        assertNoWarningsOrErrors(status);
    }

    /**
     * Test status with invalid order.
     */
    @Test
    public void invalidStatusOrder() {
        StatusProt status = new StatusProt();
        status.setMeasmId(EXISTING_MEASM_ID);
        status.setStatusMpId(ID3);
        status.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        validator.validate(status);
        Assert.assertTrue(status.hasErrors());
        Assert.assertTrue(status.getErrors().containsKey(STATUS));
        Assert.assertTrue(
            status.getErrors().get(STATUS).contains(
                String.valueOf(StatusCodes.VALUE_NOT_MATCHING)));
    }

    /**
     * Test status with valid order.
     */
    @Test
    public void validStatusOrder() {
        StatusProt status = new StatusProt();
        status.setMeasmId(EXISTING_MEASM_ID);
        status.setStatusMpId(ID2);
        status.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        validator.validate(status);
        assertNoWarningsOrErrors(status);
    }

    /**
     * Test setting status of measm connected to valid REI sample.
     */
    @Test
    public void statusReiCompleteSample() {
        StatusProt status = new StatusProt();
        status.setMeasmId(EXISTING_MEASM_ID_VALID_REI_SAMPLE);
        status.setStatusMpId(ID2);
        status.setMeasFacilId(EXISTING_MEAS_FACIL_ID);
        validator.validate(status);
        assertNoWarningsOrErrors(status);
    }
}
