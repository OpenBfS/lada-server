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

import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation for Ortszuordnung objects.
 *
 * Instantiates the set of rules for Ortszuordnung objects and uses these
 * rules to validate the object.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
@Dependent
public class OrtszuordnungValidator extends Validator<Geolocat> {

    @Inject
    OrtszuordnungValidator(HttpServletRequest request) {
        super(request);
    }

    @Inject
    @ValidationRule("Ortszuordnung")
    private Instance<Rule> rules;

    @Override
    public Geolocat validate(Object object) {
        return validate((Geolocat) object, rules);
    }
}
