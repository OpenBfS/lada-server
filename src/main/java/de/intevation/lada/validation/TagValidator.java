/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
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

import de.intevation.lada.model.master.Tag;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation for tag objects.
 *
 * Instantiates the set of rules for tag objects and uses these rules to
 * validate the object.
 */
@Dependent
public class TagValidator extends Validator<Tag> {

    @Inject
    TagValidator(HttpServletRequest request) {
        super(request);
    }

    @Inject
    @ValidationRule("Tag")
    private Instance<Rule> rules;

    @Override
    public Tag validate(Object object) {
        return validate((Tag) object, rules);
    }
}
