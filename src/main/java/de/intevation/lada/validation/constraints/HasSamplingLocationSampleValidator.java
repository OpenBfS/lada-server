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
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.Geolocat_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


/**
 * Validation rule for Sample.
 * Validates if a sampling location is given.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class HasSamplingLocationSampleValidator
    implements ConstraintValidator<HasSamplingLocation, Sample> {

    private static final int REG_REI_I = 4;
    private static final int REG_REI_X = 3;
    private static final String TYPE_REG_E = "E";
    private static final String TYPE_REG_R = "R";

    private String message;

    @Override
    public void initialize(HasSamplingLocation constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Transactional
    @Override
    public boolean isValid(Sample probe, ConstraintValidatorContext ctx) {
        if (probe == null || probe.getRegulationId() == null) {
            return true;
        }
        Integer id = probe.getId();

        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(message)
            .addPropertyNode("geolocats")
            .addConstraintViolation();
        if (id == null) {
            return false;
        }

        Repository repository = CDI.current().getBeanContainer()
            .createInstance().select(Repository.class).get();

        final int regulation = probe.getRegulationId();
        final List<String> expectedTypeRegs =
            probe.getReiAgGrId() != null
            || regulation == REG_REI_X
            || regulation == REG_REI_I
            ? List.of(TYPE_REG_R)
            : List.of(TYPE_REG_E, TYPE_REG_R);
        QueryBuilder<Geolocat> builder = repository
            .queryBuilder(Geolocat.class)
            .and(Geolocat_.sampleId, id)
            .andIn(Geolocat_.typeRegulation, expectedTypeRegs);
        if (repository.filter(builder.getQuery()).isEmpty()) {
            return false;
        }
        return true;
    }
}
