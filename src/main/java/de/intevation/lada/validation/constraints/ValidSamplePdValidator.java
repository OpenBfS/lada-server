/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import java.util.Map;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.lada.Mpg_;


/**
 * Validation rule for Mpg.
 * Validates if the subintervall period is meaningful.
 */
public class ValidSamplePdValidator
    implements ConstraintValidator<ValidSamplePd, Mpg> {

    private static final Map<String, Integer> INTERVALL_MAX = Map.of(
        Mpg.YEARLY,      Mpg.DOY_MAX,
        Mpg.HALF_YEARLY, 184,
        Mpg.QUARTERLY,   92,
        Mpg.MONTHLY,     31,
        Mpg.FOUR_WEEKLY, 28,
        Mpg.TWO_WEEKLY,  14,
        Mpg.WEEKLY,      7,
        Mpg.DAILY,       1);

    @Override
    public boolean isValid(Mpg messprogramm, ConstraintValidatorContext ctx) {
        if (messprogramm == null) {
            return true;
        }

        String probenintervall = messprogramm.getSamplePd();
        Integer teilVon = messprogramm.getSamplePdStartDate();
        Integer teilBis = messprogramm.getSamplePdEndDate();
        Integer offset = messprogramm.getSamplePdOffset();
        Integer gueltigVon = messprogramm.getValidStartDate();
        Integer gueltigBis = messprogramm.getValidEndDate();

        // Leave these checks up to field-level constraints
        if (probenintervall == null
            || !INTERVALL_MAX.keySet().contains(probenintervall)
            || teilVon == null
            || teilBis == null
            || gueltigVon == null
            || gueltigBis == null
        ) {
            return true;
        }

        boolean isValid = true;
        final String startDateKey = Mpg_.SAMPLE_PD_START_DATE,
            endDateKey = Mpg_.SAMPLE_PD_END_DATE,
            offsetKey = Mpg_.SAMPLE_PD_OFFSET;
        if (Mpg.YEARLY.equals(probenintervall)) {
            if (teilVon < gueltigVon || teilVon > gueltigBis) {
                isValid = false;
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate(ValidSamplePd.MSG)
                    .addPropertyNode(startDateKey)
                    .addConstraintViolation();
            }
            if (teilBis < gueltigVon || teilBis > gueltigBis) {
                isValid = false;
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate(ValidSamplePd.MSG)
                    .addPropertyNode(endDateKey)
                    .addConstraintViolation();
            }
            if (offset != null && offset > INTERVALL_MAX.get(Mpg.YEARLY) - 1) {
                isValid = false;
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate(ValidSamplePd.MSG)
                    .addPropertyNode(offsetKey)
                    .addConstraintViolation();
            }
        } else {
            // upper limits depend on (valid) intervall type
            if (teilVon > INTERVALL_MAX.get(probenintervall)) {
                isValid = false;
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate(ValidSamplePd.MSG)
                    .addPropertyNode(startDateKey)
                    .addConstraintViolation();
            }
            if (teilBis > INTERVALL_MAX.get(probenintervall)) {
                isValid = false;
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate(ValidSamplePd.MSG)
                    .addPropertyNode(endDateKey)
                    .addConstraintViolation();
            }
            if (offset != null
                && offset > INTERVALL_MAX.get(probenintervall) - 1
            ) {
                isValid = false;
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate(ValidSamplePd.MSG)
                    .addPropertyNode(offsetKey)
                    .addConstraintViolation();
            }
        }

        return isValid;
    }
}
