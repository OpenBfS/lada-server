/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import static org.junit.Assert.assertFalse;

import de.intevation.lada.model.BaseModel;

/**
 * Baseclass for ValidatorTests.
 */
public abstract class ValidatorBaseTest {
    private static final String MSG_UNEXPECTED_VALIDATION_WARNINGS
        = "Unexpected validation warnings: ";
    private static final String MSG_UNEXPECTED_VALIDATION_ERRORS
        = "Unexpected validation errors: ";

    /**
     * Assert that the given entities has no warnings or errors attached.
     * @param entity Entity to check
     */
    protected void assertNoWarningsOrErrors(BaseModel entity) {
        assertFalse(
            MSG_UNEXPECTED_VALIDATION_ERRORS
                + entity.getErrors(),
            entity.hasErrors());
        assertFalse(
            MSG_UNEXPECTED_VALIDATION_WARNINGS
                + entity.getWarnings(),
            entity.hasWarnings());
    }
}
