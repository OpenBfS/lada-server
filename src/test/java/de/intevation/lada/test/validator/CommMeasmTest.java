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

import org.junit.Assert;

import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.validation.Validator;

/**
 * Test validation rules for CommMeasm objects.
 */
@Transactional
public class CommMeasmTest extends ValidatorBaseTest {

    private static final String MEAS_FACIL = "06010";

    //Validation keys
    private static final String ERROR_KEY = "measmId";
    private static final String ERROR_VALUE
        = "Non-unique value combination for [measmId, text]";

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

        validator.validate(comm);
        Assert.assertTrue(comm.hasErrors());
        Assert.assertTrue(comm.getErrors().containsKey(ERROR_KEY));
        Assert.assertTrue(comm.getErrors()
            .get(ERROR_KEY).contains(ERROR_VALUE));
    }

    /**
     * Test commMeasm with new text.
     */
    public void commentUniqueText() {
        CommMeasm comm = new CommMeasm();
        comm.setMeasmId(EXISTING_MEASM_ID);
        comm.setText(COMMENT_TEXT_NEW);
        comm.setMeasFacilId(MEAS_FACIL);

        validator.validate(comm);
        assertNoWarningsOrErrors(comm);
    }
}
