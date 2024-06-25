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

import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.master.EnvMedium;
import de.intevation.lada.model.master.UnitConvers;
import de.intevation.lada.model.master.UnitConvers_;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.QueryBuilder;


/**
 * Validation rule for MeasVal.
 * Validates if the measuring unit fits the environmental medium of the sample.
 */
public class IsNormalizedValidator implements
    ConstraintValidator<IsNormalized, MeasVal> {

    private String message;

    @Override
    public void initialize(IsNormalized constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Transactional
    @Override
    public boolean isValid(
        MeasVal messwert,
        ConstraintValidatorContext ctx
    ) {
        if (messwert == null || messwert.getMeasmId() == null) {
            return true;
        }

        Repository repository = CDI.current().getBeanContainer()
            .createInstance().select(Repository.class).get();

        Measm measm = repository.entityManager().find(
            Measm.class, messwert.getMeasmId());
        if (measm == null) {
            return true;
        }

        EnvMedium umwelt = measm.getSample().getEnvMedium();
        if (umwelt == null) {
            return true;
        }

        Integer mehId = umwelt.getUnit1();
        Integer secMehId = umwelt.getUnit2();
        if (mehId == null && secMehId == null) {
            return true;
        }

        Integer fromUnit = messwert.getMeasUnitId();
        if (mehId != null && mehId.equals(fromUnit)
            || secMehId != null && secMehId.equals(fromUnit)) {
            // Unit of measured value is primary or secondary unit of envMedium
            return true;
        }

        // Check if measuring unit of measured value can be converted
        // to primary or secondary measuring unit of envMedium
        boolean convert = false;
        if (mehId != null && !mehId.equals(fromUnit)) {
            QueryBuilder<UnitConvers> builder = repository
                .queryBuilder(UnitConvers.class)
                .and(UnitConvers_.toUnitId, mehId)
                .and(UnitConvers_.fromUnitId, fromUnit);
            convert = !repository.filter(builder.getQuery()).isEmpty();
        }
        if (!convert && secMehId != null && !secMehId.equals(fromUnit)) {
            QueryBuilder<UnitConvers> builder = repository
                .queryBuilder(UnitConvers.class)
                .and(UnitConvers_.toUnitId, secMehId)
                .and(UnitConvers_.fromUnitId, fromUnit);
            convert = !repository.filter(builder.getQuery()).isEmpty();
        }

        HibernateConstraintValidatorContext hibernateCtx = ctx.unwrap(
            HibernateConstraintValidatorContext.class
        );
        hibernateCtx.disableDefaultConstraintViolation();
        hibernateCtx.addExpressionVariable("convert", convert)
            .buildConstraintViolationWithTemplate(this.message)
            .enableExpressionLanguage(BEAN_METHODS)
            .addPropertyNode("measUnitId")
            .addConstraintViolation();
        return false;
    }
}
