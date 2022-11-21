/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messprogramm;

import de.intevation.lada.model.land.Mpg;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for Messprogramm.
 * Validates if the Messprogramm has all mandatory fields set.
 */
@ValidationRule("Messprogramm")
public class HasAllMandatory implements Rule {

    @Override
    public Violation execute(Object object) {
        Mpg messprogramm = (Mpg) object;
        Violation violation = new Violation();

        if (messprogramm.getMeasFacilId() == null
            || "".equals(messprogramm.getMeasFacilId())) {
            violation.addError("mstlabor", StatusCodes.VALUE_MISSING);
        }
        if (messprogramm.getApprLabId() == null
            || "".equals(messprogramm.getApprLabId())) {
            violation.addError("mstlabor", StatusCodes.VALUE_MISSING);
        }
        if (messprogramm.getRegulationId() == null) {
            violation.addError("datenbasisId", StatusCodes.VALUE_MISSING);
        }
        if (messprogramm.getSampleMethId() == null) {
            violation.addError("probenartId", StatusCodes.VALUE_MISSING);
        }
        if (messprogramm.getSamplePd() == null
            || "".equals(messprogramm.getSamplePd())) {
            violation.addError("probenintervall", StatusCodes.VALUE_MISSING);
        }
        if (messprogramm.getSamplePdStartDate() == null) {
            violation.addError("teilintervallVon", StatusCodes.VALUE_MISSING);
        }
        if (messprogramm.getSamplePdEndDate() == null) {
            violation.addError("teilintervallBis", StatusCodes.VALUE_MISSING);
        }
        if (messprogramm.getValidStartDate() == null) {
            violation.addError("gueltigVon", StatusCodes.VALUE_MISSING);
        }
        if (messprogramm.getValidEndDate() == null) {
            violation.addError("gueltigBis", StatusCodes.VALUE_MISSING);
        }

        return violation.hasErrors()
            ? violation
            : null;
    }
}
