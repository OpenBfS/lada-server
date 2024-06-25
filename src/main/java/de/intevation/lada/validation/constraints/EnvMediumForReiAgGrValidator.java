/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import java.util.List;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.ReiAgGrEnvMediumMp;
import de.intevation.lada.model.master.ReiAgGrEnvMediumMp_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


/**
 * Validation rule for sample.
 * Validates if the EnvMedium fits the ReiAgGr.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class EnvMediumForReiAgGrValidator
    implements ConstraintValidator<EnvMediumForReiAgGr, Sample> {

    private static final int REG_REI_I = 4;
    private static final int REG_REI_X = 3;

    private String message;

    @Override
    public void initialize(EnvMediumForReiAgGr constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Transactional
    @Override
    public boolean isValid(Sample probe, ConstraintValidatorContext ctx) {
        if (probe == null
            || probe.getRegulationId() != null
            && probe.getRegulationId() != REG_REI_X
            && probe.getRegulationId() != REG_REI_I
            || probe.getEnvMediumId() == null
            || probe.getReiAgGrId() == null
        ) {
            return true;
        }

        Repository repository = CDI.current().getBeanContainer()
            .createInstance().select(Repository.class).get();
        final SingularAttribute<ReiAgGrEnvMediumMp, String> envMediumIdKey
            = ReiAgGrEnvMediumMp_.envMediumId;
        QueryBuilder<ReiAgGrEnvMediumMp> builder = repository
            .queryBuilder(ReiAgGrEnvMediumMp.class)
            .and(ReiAgGrEnvMediumMp_.reiAgGrId, probe.getReiAgGrId())
            .and(envMediumIdKey, probe.getEnvMediumId());
        List<ReiAgGrEnvMediumMp> zuord =
            repository.filter(builder.getQuery());
        if (zuord.isEmpty()) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(this.message)
                .addPropertyNode(envMediumIdKey.getName())
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
