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

import de.intevation.lada.model.land.KommentarM;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.annotation.ValidationConfig;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation for kommentarM objects.
 *
 * Instantiates the set of rules for kommentarM objects and uses these rules to
 * validate the object.
 *
 */
@ValidationConfig(type = "KommentarM")
@ApplicationScoped
public class KommentarMValidator implements Validator {

    @Inject
    @ValidationRule("KommentarM")
    private Instance<Rule> rules;

    @Override
    public Violation validate(Object object) {
        Violation violations = new Violation();
        if (!(object instanceof KommentarM)) {
            violations.addError("Kommentar", StatusCodes.NOT_A_PROBE);
            return violations;
        }


        for (Rule rule : rules) {
            Violation result = rule.execute(object);
            if (result != null) {
                if (result.hasWarnings()) {
                    violations.addWarnings(result.getWarnings());
                }
                if (result.hasErrors()) {
                    violations.addErrors(result.getErrors());
                }
                if (result.hasNotifications()) {
                  violations.addNotifications(result.getNotifications());
                }
            }
        }
        return violations;
    }
}
