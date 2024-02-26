/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messprogramm;

import java.util.Map;

import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for Messprogramm.
 * Validates if the subintervall period is meaningful.
 */
@ValidationRule("Messprogramm")
public class SubIntervall implements Rule {

    private static final String YEARLY = "J";

    private static final Map<String, Integer> INTERVALL_MAX = Map.of(
        YEARLY, 365,
        "H",    184,
        "Q",    92,
        "M",    31,
        "W4",   28,
        "W2",   14,
        "W",    7,
        "T",    1);

    @Override
    public Violation execute(Object object) {
        Mpg messprogramm = (Mpg) object;
        Violation violation = new Violation();

        String probenintervall = messprogramm.getSamplePd();
        Integer teilVon = messprogramm.getSamplePdStartDate();
        Integer teilBis = messprogramm.getSamplePdEndDate();
        Integer offset = messprogramm.getSamplePdOffset();
        Integer gueltigVon = messprogramm.getValidStartDate();
        Integer gueltigBis = messprogramm.getValidEndDate();

        final String startDateKey = "samplePdStartDate",
            endDateKey = "samplePdEndDate",
            offsetKey = "samplePdOffset";
        if (YEARLY.equals(probenintervall)) {
            if (teilVon < gueltigVon || teilVon > gueltigBis) {
                violation.addError(
                    startDateKey,
                    StatusCodes.VALUE_OUTSIDE_RANGE);
            }
            if (teilBis < gueltigVon || teilBis > gueltigBis) {
                violation.addError(
                    endDateKey,
                    StatusCodes.VALUE_OUTSIDE_RANGE);
            }
            if (offset != null && offset > INTERVALL_MAX.get(YEARLY) - 1) {
                violation.addError(
                    offsetKey, StatusCodes.VALUE_OUTSIDE_RANGE);
            }
        } else {
            // lower limits are independent of intervall type
            if (teilVon < 1) {
                violation.addError(
                    startDateKey, StatusCodes.VALUE_OUTSIDE_RANGE);
            }
            if (teilBis < 1) {
                violation.addError(
                    endDateKey, StatusCodes.VALUE_OUTSIDE_RANGE);
            }
            if (offset != null && offset < 0) {
                violation.addError(
                    offsetKey, StatusCodes.VALUE_OUTSIDE_RANGE);
            }

            // upper limits depend on (valid) intervall type
            if (teilVon > INTERVALL_MAX.get(probenintervall)) {
                violation.addError(
                    startDateKey,
                    StatusCodes.VALUE_OUTSIDE_RANGE);
            }
            if (teilBis > INTERVALL_MAX.get(probenintervall)) {
                violation.addError(
                    endDateKey,
                    StatusCodes.VALUE_OUTSIDE_RANGE);
            }
            if (offset != null
                && offset > INTERVALL_MAX.get(probenintervall) - 1
            ) {
                violation.addError(
                    offsetKey,
                    StatusCodes.VALUE_OUTSIDE_RANGE);
            }
        }

        // lower limit has to be less than or equal to upper limit
        if (teilVon > teilBis) {
            violation.addError(
                startDateKey, StatusCodes.DATE_BEGIN_AFTER_END);
            violation.addError(
                endDateKey, StatusCodes.DATE_BEGIN_AFTER_END);
        }

        return violation.hasErrors()
            ? violation
            : null;
    }
}
