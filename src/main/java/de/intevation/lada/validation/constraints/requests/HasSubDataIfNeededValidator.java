/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints.requests;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;

import de.intevation.lada.data.requests.QueryExportParameters;

public class HasSubDataIfNeededValidator
    implements ConstraintValidator<HasSubDataIfNeeded, QueryExportParameters> {

    private String message;

    @Override
    public void initialize(HasSubDataIfNeeded constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }
    @Override
    public boolean isValid(QueryExportParameters value, ConstraintValidatorContext context) {
        boolean valid = (value.isExportSubData() && value.getSubDataColumns() != null
                && value.getSubDataColumns().length > 0)
            || (!value.isExportSubData() && (value.getSubDataColumns() == null
                || value.getSubDataColumns().length == 0));

        HibernateConstraintValidatorContext hibernateCtx = context.unwrap(
            HibernateConstraintValidatorContext.class
        );
        hibernateCtx.disableDefaultConstraintViolation();
        hibernateCtx.addExpressionVariable("shouldHaveSubData", value.isExportSubData())
            .buildConstraintViolationWithTemplate(this.message)
            .enableExpressionLanguage(ExpressionLanguageFeatureLevel.BEAN_METHODS)
            .addPropertyNode("subDataColumns")
            .addConstraintViolation();
        return valid;
    }
}
