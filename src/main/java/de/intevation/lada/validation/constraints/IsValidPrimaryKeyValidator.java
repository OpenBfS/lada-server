/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.metamodel.EntityType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.util.data.Repository;


/**
 * Check if value is a valid primary key for an entity of the given class.
 */
public class IsValidPrimaryKeyValidator
    implements ConstraintValidator<IsValidPrimaryKey, Object> {

    private static final String KEY_PARAM = "key";

    private static final String QUERY_TPL =
        "select exists (select 1 from %s where %s = :" + KEY_PARAM + ")";

    private String existsQuery;

    @Override
    public void initialize(IsValidPrimaryKey constraintAnnotation) {
        EntityType<?> type = CDI.current().getBeanContainer().createInstance()
            .select(Repository.class).get().entityManager().getMetamodel()
            .entity(constraintAnnotation.clazz());
        this.existsQuery = String.format(QUERY_TPL,
            type.getName(),
            type.getId(type.getIdType().getJavaType()).getName());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext ctx) {
        return value == null
            /* Get instance programmatically because dependency injection is
               not guaranteed to work in ConstraintValidator implementations */
            || CDI.current().getBeanContainer().createInstance()
                .select(Repository.class).get().entityManager()
                .createQuery(existsQuery, Boolean.class)
                .setParameter(KEY_PARAM, value)
                .getSingleResult();
    }
}
