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

import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation for SampleSpecifMeasVal objects.
 */
@Dependent
public class SampleSpecifMeasValValidator
    extends Validator<SampleSpecifMeasVal> {

    @Inject
    SampleSpecifMeasValValidator(HttpServletRequest request) {
        super(request);
    }

    @Inject
    @ValidationRule("SampleSpecifMeasVal")
    private Instance<Rule> rules;

    @Override
    public SampleSpecifMeasVal validate(Object object) {
        return validate((SampleSpecifMeasVal) object, rules);
    }
}
