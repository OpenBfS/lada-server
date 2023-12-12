/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

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
@ApplicationScoped
public class MessprogrammMmtValidator implements Validator<MpgMmtMp> {

    @Inject
    @ValidationRule("MessprogrammMmt")
    private Instance<Rule> rules;

    @Override
    public void validate(Object object) {
        validate((MpgMmtMp) object, rules);
    }
}
