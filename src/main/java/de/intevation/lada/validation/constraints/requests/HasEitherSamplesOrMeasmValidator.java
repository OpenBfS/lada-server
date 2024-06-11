/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints.requests;

import de.intevation.lada.data.requests.LafExportParameters;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class HasEitherSamplesOrMeasmValidator
    implements ConstraintValidator<HasEitherSamplesOrMeasm, LafExportParameters> {

    @Override
    public boolean isValid(LafExportParameters value, ConstraintValidatorContext context) {
        return value.getMessungen() != null || value.getProben() != null;
    }
}
