/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import static org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel.BEAN_METHODS;

import de.intevation.lada.util.data.EnvMedia;


/**
 * Validates if the given string contains valid parts.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class EnvDescripDisplayValidator
    implements ConstraintValidator<EnvDescripDisplay, String> {

    private String message;

    @Override
    public void initialize(EnvDescripDisplay constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    @Transactional
    public boolean isValid(
        String envDescripDisplay, ConstraintValidatorContext ctx
    ) {
        try {
            CDI.current().getBeanContainer()
                .createInstance().select(EnvMedia.class).get()
                .findEnvDescripIds(envDescripDisplay);
        } catch (EnvMedia.InvalidEnvDescripDisplayException e) {
            HibernateConstraintValidatorContext hibernateCtx = ctx.unwrap(
                HibernateConstraintValidatorContext.class
            );
            hibernateCtx.disableDefaultConstraintViolation();
            hibernateCtx.addExpressionVariable("field", e.getField())
                .buildConstraintViolationWithTemplate(this.message)
                .enableExpressionLanguage(BEAN_METHODS)
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
