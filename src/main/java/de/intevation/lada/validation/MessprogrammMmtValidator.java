/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import de.intevation.lada.model.lada.MpgMmtMp;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation for MessprogrammMmt objects.
 *
 * Instantiates the set of rules for MessprogrammMmt objects
 * and uses these rules to validate the object.
 *
 */
@Dependent
public class MessprogrammMmtValidator extends Validator<MpgMmtMp> {

    @Inject
    @ValidationRule("MessprogrammMmt")
    private Instance<Rule> rules;

    @Override
    public Violation validate(Object object) {
        return validate((MpgMmtMp) object, rules);
    }
}
