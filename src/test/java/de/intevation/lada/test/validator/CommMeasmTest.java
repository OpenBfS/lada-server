/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
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

import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.Violation;

/**
 * Test validation rules for CommMeasm objects.
 */
@Transactional
public class CommMeasmTest {

    private static final String MEAS_FACIL = "06010";

    //Validation keys
    private static final String TEXT = "text";

    //Other constants
    private static final String COMMENT_TEXT_EXISTING = "Testkommentar";
    private static final String COMMENT_TEXT_NEW = "UniqueComment42";
    private static final int EXISTING_MEASM_ID = 1200;

    @Inject
    private Validator<CommMeasm> validator;

    /**
     * Test commMeasm with existing text.
     */
    public void commentDuplicateText() {
        CommMeasm comm = new CommMeasm();
        comm.setMeasmId(EXISTING_MEASM_ID);
        comm.setText(COMMENT_TEXT_EXISTING);
        comm.setMeasFacilId(MEAS_FACIL);

        Violation violation = validator.validate(comm);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors().containsKey(TEXT));
        Assert.assertTrue(violation.getErrors()
            .get(TEXT).contains(String.valueOf(StatusCodes.VAL_EXISTS)));
    }

    /**
     * Test commMeasm with new text.
     */
    public void commentUniqueText() {
        CommMeasm comm = new CommMeasm();
        comm.setMeasmId(EXISTING_MEASM_ID);
        comm.setText(COMMENT_TEXT_NEW);
        comm.setMeasFacilId(MEAS_FACIL);

        Violation violation = validator.validate(comm);
        if (violation.hasErrors() && violation.getErrors().containsKey(TEXT)) {
            Assert.assertFalse(violation.getErrors()
                .get(TEXT).contains(String.valueOf(StatusCodes.VAL_EXISTS)));
        }

    }
}
