/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation for messwert objects.
 *
 * Instantiates the set of rules for messwert objects and uses these rules to
 * validate the object.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
@ApplicationScoped
public class MesswertValidator implements Validator<MeasVal> {

    @Inject
    @ValidationRule("Messwert")
    private Instance<Rule> rules;

    @Override
    public void validate(Object object) {
        validate((MeasVal) object, rules);
    }
}
