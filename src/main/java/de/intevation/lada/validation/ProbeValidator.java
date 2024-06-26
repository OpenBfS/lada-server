/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
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
import jakarta.servlet.http.HttpServletRequest;

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation for probe objects.
 *
 * Instantiates the set of rules for probe objects and uses these rules to
 * validate the object.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
@Dependent
public class ProbeValidator extends Validator<Sample> {

    @Inject
    ProbeValidator(HttpServletRequest request) {
        super(request);
    }

    @Inject
    @ValidationRule("Sample")
    private Instance<Rule> rules;

    @Override
    public Sample validate(Object object) {
        return validate((Sample) object, rules);
    }
}
