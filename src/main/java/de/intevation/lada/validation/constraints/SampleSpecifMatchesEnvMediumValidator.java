/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import java.util.List;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.model.master.EnvSpecifMp;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


/**
 * Validation rule for SampleSpecifMeasValprobe.
 * Validates if referenced SampleSpecif matches EnvMedium referenced
 * via Sample.
 *
 * @author <a href="mailto:jbuermeyer@bfs.de">Jonas Buermeyer</a>
 */
public class SampleSpecifMatchesEnvMediumValidator implements
    ConstraintValidator<SampleSpecifMatchesEnvMedium, SampleSpecifMeasVal> {

    private String message;

    @Override
    public void initialize(SampleSpecifMatchesEnvMedium constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Transactional
    @Override
    public boolean isValid(
        SampleSpecifMeasVal zusW,
        ConstraintValidatorContext ctx
    ) {
        if (zusW != null && zusW.getSampleId() != null) {
            Repository repository = CDI.current().getBeanContainer()
                .createInstance().select(Repository.class).get();

            Sample probe = repository.entityManager().find(
                Sample.class, zusW.getSampleId());
            if (probe == null) {
                return true;
            }

            QueryBuilder<EnvSpecifMp> builderUmwZus = repository
                .queryBuilder(EnvSpecifMp.class)
                .and("sampleSpecifId", zusW.getSampleSpecifId())
                .and("envMediumId", probe.getEnvMediumId());
            List<EnvSpecifMp> umwZus = repository.filter(
                builderUmwZus.getQuery());
            if (umwZus.isEmpty()) {
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate(this.message)
                    .addPropertyNode("sampleSpecifId")
                    .addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
