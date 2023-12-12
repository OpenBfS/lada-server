/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.junit.Assert;

import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;

/**
 * Test Status entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Transactional
public class StatusTest {

    //Validator keys
    private static final String NUCL_FACIL_GR_ID = "nuclFacilGrId";
    private static final String REI_AG_GR_ID = "reiAgGrId";
    private static final String STATUS = "status";
    private static final String STATUS_MP = "statusMp";

    //Other constants
    private static final int ID1 = 1;
    private static final int ID2 = 2;
    private static final int ID3 = 3;
    private static final int ID7 = 7;

    private static final int EXISTING_MEASM_ID = 1200;
    private static final int EXISTING_MEASM_ID_VALID_REI_SAMPLE = 4200;
    private static final int EXISTING_MEASM_ID_INVALID_REI_SAMPLE = 5200;

    @Inject
    private Validator<StatusProt> validator;

    /**
     * Test if status kombi is not existing.
     */
    public void checkKombiNegative() {
        StatusProt status = new StatusProt();
        status.setMeasmId(EXISTING_MEASM_ID);
        status.setStatusLev(ID2);
        status.setStatusVal(ID7);
        validator.validate(status);
        Assert.assertTrue(status.hasErrors());
        Assert.assertTrue(status.getErrors().containsKey(STATUS_MP));
        Assert.assertTrue(
            status.getErrors().get(STATUS_MP).contains(
                StatusCodes.VALUE_NOT_MATCHING));
    }

    /**
     * Test if status kombi is existing.
     */
    public void checkKombiPositive() {
        StatusProt status = new StatusProt();
        status.setStatusLev(ID1);
        status.setStatusVal(ID1);
        validator.validate(status);
        if (status.hasErrors()) {
            Assert.assertFalse(status.getErrors().containsKey(STATUS_MP));
        }
    }

    /**
     * Test status with invalid order.
     */
    public void invalidStatusOrder() {
        StatusProt status = new StatusProt();
        status.setMeasmId(EXISTING_MEASM_ID);
        status.setStatusMpId(ID3);
        validator.validate(status);
        Assert.assertTrue(status.hasErrors());
        Assert.assertTrue(status.getErrors().containsKey(STATUS));
        Assert.assertTrue(
            status.getErrors().get(STATUS).contains(
                StatusCodes.VALUE_NOT_MATCHING));
    }

    /**
     * Test status with valid order.
     */
    public void validStatusOrder() {
        StatusProt status = new StatusProt();
        status.setMeasmId(EXISTING_MEASM_ID);
        status.setStatusMpId(ID2);
        validator.validate(status);
        if (status.hasErrors()) {
            Assert.assertFalse(status.getErrors().containsKey(STATUS));
        }
    }

    /**
     * Test setting status of measm connected to invalid REI sample.
     */
    public void statusInvalidReiSample() {
        StatusProt status = new StatusProt();
        status.setMeasmId(EXISTING_MEASM_ID_INVALID_REI_SAMPLE);
        status.setStatusMpId(ID1);
        validator.validate(status);
        Assert.assertTrue(status.hasErrors());
        Assert.assertTrue(status.getErrors().containsKey(REI_AG_GR_ID));
        Assert.assertTrue(status.getErrors().get(REI_AG_GR_ID).contains(
                StatusCodes.VALUE_MISSING));
        Assert.assertTrue(status.getErrors().containsKey(NUCL_FACIL_GR_ID));
        Assert.assertTrue(status.getErrors().get(NUCL_FACIL_GR_ID).contains(
                StatusCodes.VALUE_MISSING));
    }

    /**
     * Test setting status of measm connected to valid REI sample.
     */
    public void statusReiCompleteSample() {
        StatusProt status = new StatusProt();
        status.setMeasmId(EXISTING_MEASM_ID_VALID_REI_SAMPLE);
        status.setStatusMpId(ID1);
        validator.validate(status);
        if (status.hasErrors()) {
            Assert.assertFalse(status.getErrors()
                .containsKey(REI_AG_GR_ID));
            Assert.assertFalse(status.getErrors()
                .containsKey(NUCL_FACIL_GR_ID));
        }
    }
}
