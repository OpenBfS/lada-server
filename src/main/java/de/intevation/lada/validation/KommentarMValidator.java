/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
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

import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation for kommentarM objects.
 *
 * Instantiates the set of rules for kommentarM objects and uses these rules to
 * validate the object.
 *
 */
@ApplicationScoped
public class KommentarMValidator extends Validator<CommMeasm> {

    @Inject
    @ValidationRule("KommentarM")
    private Instance<Rule> rules;

    @Override
    public Violation validate(Object object) {
        return validate((CommMeasm) object, rules);
    }
}
