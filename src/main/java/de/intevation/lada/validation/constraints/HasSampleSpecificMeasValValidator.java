/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
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

import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.model.lada.SampleSpecifMeasVal_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


/**
 * Validation rule for Sample.
 * Validates if a SampleSpecificMeasVal is given.
 */
public class HasSampleSpecificMeasValValidator
    implements ConstraintValidator<HasSampleSpecificMeasVal, Sample> {

    private static final int REG_LFGB = 15;

    private String message;

    @Override
    public void initialize(HasSampleSpecificMeasVal constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Transactional
    @Override
    public boolean isValid(Sample sample, ConstraintValidatorContext ctx) {
        if (sample == null || sample.getRegulationId() == null) {
            return true;
        }
        Integer sampleId = sample.getId();
        Integer regulationId = sample.getRegulationId();
        if (regulationId == null || regulationId != REG_LFGB) {
            return true;
        }

        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(message)
            .addPropertyNode("sampleSpecifMeasVals")
            .addConstraintViolation();
        if (sampleId == null) {
            return false;
        }

        Repository repository = CDI.current().getBeanContainer()
            .createInstance().select(Repository.class).get();

        QueryBuilder<SampleSpecifMeasVal> builder =
        repository.queryBuilder(SampleSpecifMeasVal.class)
            .and(SampleSpecifMeasVal_.sampleId, sampleId);
        if (repository.filter(builder.getQuery()).isEmpty()) {
            return false;
        }
        return true;
    }
}
