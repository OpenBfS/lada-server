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

import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.master.MmtMeasdView;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


/**
 * Validation rule for MeasVal.
 * Validates if the measurand fits the measuring method.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class MeasdMatchesMmtValidator
    implements ConstraintValidator<MeasdMatchesMmt, MeasVal> {

    private String message;

    @Override
    public void initialize(MeasdMatchesMmt constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Transactional
    @Override
    public boolean isValid(MeasVal messwert, ConstraintValidatorContext ctx) {
        if (messwert == null) {
            return true;
        }

        Repository repository = CDI.current().getBeanContainer()
            .createInstance().select(Repository.class).get();
        Measm messung = repository.entityManager().find(
            Measm.class, messwert.getMeasmId());
        if (messung == null) {
            return true;
        }

        final String measdIdKey = "measdId";
        QueryBuilder<MmtMeasdView> mmtBuilder = repository
            .queryBuilder(MmtMeasdView.class)
            .and("mmtId", messung.getMmtId())
            .and(measdIdKey, messwert.getMeasdId());
        if (repository.filter(mmtBuilder.getQuery()).isEmpty()) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(this.message)
                .addPropertyNode(measdIdKey)
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
