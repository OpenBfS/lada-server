/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import org.junit.Test;

import de.intevation.lada.model.lada.SampleSpecifMeasVal;


/**
 * Test SampleSpecifMeasVal validation.
 */
public class SampleSpecifMeasValTest extends ValidatorBaseTest {

    /**
     * Test validation of valid value.
     */
    @Test
    public void validSampleSpecifMeasVal() {
        assertNoMessages(validator.validate(createMinimumValidValue()));
    }

    /**
     * Test sample with sample specif but without matching env medium.
     */
    @Test
    public void sampleSpecifMesValWithoutMatchingEnvMedium() {
        SampleSpecifMeasVal value = createMinimumValidValue();
        value.setSampleSpecifId("A42");

        assertHasWarnings(
            validator.validate(value),
            "sampleSpecifId",
            "Sample specification does not match environmental medium");
    }

    private SampleSpecifMeasVal createMinimumValidValue() {
        SampleSpecifMeasVal value = new SampleSpecifMeasVal();
        value.setSampleId(1000);
        value.setSampleSpecifId("A74");
        return value;
    }
}
