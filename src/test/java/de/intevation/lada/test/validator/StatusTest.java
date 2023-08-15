/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
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

import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.Violation;

/**
 * Test Status entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Transactional
public class StatusTest {

    private static final int ID1 = 1;
    private static final int ID2 = 2;
    private static final int ID7 = 7;

    private final String statusMpKey = "statusMp";

    // Value corresponds with dataset dbUnit_probe.json
    private final int existingMeasmId = 1200;

    @Inject
    private Validator<StatusProt> validator;

    /**
     * Test if status kombi is not existing.
     */
    public void checkKombiNegative() {
        StatusProt status = new StatusProt();
        status.setMeasmId(existingMeasmId);
        status.setStatusLev(ID2);
        status.setStatusVal(ID7);
        Violation violation = validator.validate(status);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors().containsKey(statusMpKey));
        Assert.assertTrue(
            violation.getErrors().get(statusMpKey).contains(
                StatusCodes.VALUE_NOT_MATCHING));
    }

    /**
     * Test if status kombi is existing.
     */
    public void checkKombiPositive() {
        StatusProt status = new StatusProt();
        status.setStatusLev(ID1);
        status.setStatusVal(ID1);
        Violation violation = validator.validate(status);
        if (violation.hasErrors()) {
            Assert.assertFalse(violation.getErrors().containsKey(statusMpKey));
        }
    }
}
