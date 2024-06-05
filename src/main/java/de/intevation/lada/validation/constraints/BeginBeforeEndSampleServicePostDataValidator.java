/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import java.time.ZonedDateTime;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.rest.SampleService.PostData;


/**
 * Validation rule for SampleService.PostData.
 * Validates if the requested period is meaningful.
 */
public class BeginBeforeEndSampleServicePostDataValidator
    implements ConstraintValidator<BeginBeforeEnd, PostData> {

    private String message;

    @Override
    public void initialize(BeginBeforeEnd constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(
        PostData postData,
        ConstraintValidatorContext ctx
    ) {
        if (postData == null) {
            return true;
        }

        ZonedDateTime begin = postData.getStart();
        ZonedDateTime end = postData.getEnd();

        // Leave null checks up to field-level constraints
        if (begin == null || end == null) {
            return true;
        }

        return !begin.isAfter(end);
    }
}
