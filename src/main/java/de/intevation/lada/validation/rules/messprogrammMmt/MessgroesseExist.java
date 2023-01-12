/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messprogrammMmt;

import javax.inject.Inject;

import de.intevation.lada.model.lada.MpgMmtMp;
import de.intevation.lada.model.master.Measd;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for MessprogrammMmt.
 * Validates if Messgroesse objects exist.
 */
@ValidationRule("MessprogrammMmt")
public class MessgroesseExist implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        MpgMmtMp messprogrammMmt = (MpgMmtMp) object;
        Violation violation = new Violation();

        Integer[] mIds = messprogrammMmt.getMeasds();
        if (mIds != null) {
            for (Integer mId: mIds) {
                if (
                    repository.getByIdPlain(Measd.class, mId) == null
                ) {
                    violation.addError(
                        "measds", StatusCodes.NOT_EXISTING);
                }
            }
        }

        return violation.hasErrors()
            ? violation
            : null;
    }
}
