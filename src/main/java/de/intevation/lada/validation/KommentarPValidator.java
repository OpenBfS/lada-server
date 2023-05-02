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

import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation for kommentarP objects.
 *
 * Instantiates the set of rules for kommentarP objects and uses these rules to
 * validate the object.
 *
 */
@ApplicationScoped
public class KommentarPValidator extends Validator<CommSample> {

    @Inject
    @ValidationRule("KommentarP")
    private Instance<Rule> rules;

    @Override
    public Violation validate(Object object) {
        return validate((CommSample) object, rules);
    }
}
