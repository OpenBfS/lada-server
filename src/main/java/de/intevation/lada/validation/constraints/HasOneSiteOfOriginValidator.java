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
 * Validates if the sample has a single site of origin
 * (site with typeRegulation "U" or "R")
 */
public class HasOneSiteOfOriginValidator
    implements ConstraintValidator<HasOneSiteOfOrigin, Sample> {

    private String message;

    @Override
    public void initialize(HasOneSiteOfOrigin constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Transactional
    @Override
    public boolean isValid(Sample probe, ConstraintValidatorContext ctx) {
        Integer id = probe.getId();

        if (id == null) {
            return true;
        }

        Repository repository = CDI.current().getBeanContainer()
            .createInstance().select(Repository.class).get();
        QueryBuilder<Geolocat> builder = repository
            .queryBuilder(Geolocat.class)
            .and(Geolocat_.sampleId, id)
            .andIn(Geolocat_.typeRegulation, List.of("U", "R"));
        if (repository.filter(builder.getQuery()).size() > 1) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(this.message)
                .addPropertyNode("geolocats")
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
