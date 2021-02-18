/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messprogramm;

import javax.inject.Inject;

import de.intevation.lada.model.land.Messprogramm;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
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

    @Inject
    @RepositoryConfig(type = RepositoryType.RO)
    Repository repository;

    @Override
    public Violation execute(Object object) {
        Messprogramm messprogramm = (Messprogramm) object;
        Violation violation = new Violation();

        if (messprogramm.getMstId() == null
            || "".equals(messprogramm.getMstId())) {
            violation.addError("mstlabor", StatusCodes.VALUE_MISSING);
        }
        if (messprogramm.getLaborMstId() == null
            || "".equals(messprogramm.getLaborMstId())) {
            violation.addError("mstlabor", StatusCodes.VALUE_MISSING);
        }
        if (messprogramm.getDatenbasisId() == null) {
            violation.addError("datenbasisId", StatusCodes.VALUE_MISSING);
        }
        if (messprogramm.getProbenartId() == null) {
            violation.addError("probenartId", StatusCodes.VALUE_MISSING);
        }
        if (messprogramm.getProbenintervall() == null
            || "".equals(messprogramm.getProbenintervall())) {
            violation.addError("probenintervall", StatusCodes.VALUE_MISSING);
        }
        if (messprogramm.getTeilintervallVon() == null) {
            violation.addError("teilintervallVon", StatusCodes.VALUE_MISSING);
        }
        if (messprogramm.getTeilintervallBis() == null) {
            violation.addError("teilintervallBis", StatusCodes.VALUE_MISSING);
        }
        if (messprogramm.getGueltigVon() == null) {
            violation.addError("gueltigVon", StatusCodes.VALUE_MISSING);
        }
        if (messprogramm.getGueltigBis() == null) {
            violation.addError("gueltigBis", StatusCodes.VALUE_MISSING);
        }

        return violation.hasErrors()
            ? violation
            : null;
    }
}
