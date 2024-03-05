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

import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.validation.Validator;

/**
 * Test validation rules for CommSample objects.
 */
@Transactional
public class CommSampleTest extends ValidatorBaseTest {

    //Other constants
    private static final String COMMENT_TEXT_EXISTING = "Testkommentar";
    private static final String COMMENT_TEXT_NEW = "UniqueComment42";
    private static final int EXISTING_SAMPLE_ID = 1000;
    private static final String EXISTING_MEAS_FACIL_ID = "06010";

    @Inject
    private Validator<CommSample> validator;

    /**
     * Test commSample with existing text.
     */
    public void commentDuplicateText() {
        CommSample comm = new CommSample();
        comm.setSampleId(EXISTING_SAMPLE_ID);
        comm.setText(COMMENT_TEXT_EXISTING);
        comm.setMeasFacilId(EXISTING_MEAS_FACIL_ID);

        validator.validate(comm);
        assertHasError(
            comm,
            "text",
            "Non-unique value combination for [text, sampleId]");
    }

    /**
     * Test commSample with new text.
     */
    public void commentUniqueText() {
        CommSample comm = new CommSample();
        comm.setSampleId(EXISTING_SAMPLE_ID);
        comm.setText(COMMENT_TEXT_NEW);
        comm.setMeasFacilId(EXISTING_MEAS_FACIL_ID);

        validator.validate(comm);
        assertNoWarningsOrErrors(comm);
    }
}
