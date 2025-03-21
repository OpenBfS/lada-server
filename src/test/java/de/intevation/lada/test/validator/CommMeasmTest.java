/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import org.junit.Test;

import de.intevation.lada.model.lada.CommMeasm;


/**
 * Test validation rules for CommMeasm objects.
 */
public class CommMeasmTest extends ValidatorBaseTest {

    private static final String MEAS_FACIL = "06010";

    //Other constants
    private static final String COMMENT_TEXT_EXISTING = "Testkommentar";
    private static final String COMMENT_TEXT_NEW = "UniqueComment42";
    private static final int EXISTING_MEASM_ID = 1200;

    /**
     * Test commMeasm with existing text.
     */
    @Test
    public void commentDuplicateText() {
        CommMeasm comm = new CommMeasm();
        comm.setMeasmId(EXISTING_MEASM_ID);
        comm.setText(COMMENT_TEXT_EXISTING);
        comm.setMeasFacilId(MEAS_FACIL);

        validator.validate(comm);
        assertHasErrors(
            comm,
            "text",
            "Non-unique value combination for [text, measmId]");
    }

    /**
     * Test commMeasm with new text.
     */
    @Test
    public void commentUniqueText() {
        CommMeasm comm = new CommMeasm();
        comm.setMeasmId(EXISTING_MEASM_ID);
        comm.setText(COMMENT_TEXT_NEW);
        comm.setMeasFacilId(MEAS_FACIL);

        validator.validate(comm);
        assertNoMessages(comm);
    }
}
