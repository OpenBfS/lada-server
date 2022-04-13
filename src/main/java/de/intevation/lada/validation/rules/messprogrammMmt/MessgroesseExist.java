/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messprogrammMmt;

import javax.inject.Inject;

import de.intevation.lada.model.land.MessprogrammMmt;
import de.intevation.lada.model.stammdaten.Messgroesse;
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
        MessprogrammMmt messprogrammMmt = (MessprogrammMmt) object;
        Violation violation = new Violation();

        for (Integer mId: messprogrammMmt.getMessgroessen()) {
            if (
                repository.getByIdPlain(Messgroesse.class, mId) == null
            ) {
                violation.addError("messgroessen", StatusCodes.NOT_EXISTING);
            }
        }

        return violation.hasErrors()
            ? violation
            : null;
    }
}
