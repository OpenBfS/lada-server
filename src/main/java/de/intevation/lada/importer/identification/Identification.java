/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.identification;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Set;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;


/**
 * Facility for identification of entity instances.
 */
public class Identification {

    @Inject
    private Identifier<Sample> sampleIdentifier;

    @Inject
    private Identifier<Measm> measmIdentifier;

    @Inject
    private Repository repository;

    /**
     * Get persistent entity identified by attributes of given object.
     *
     * @param object object carrying identifying attributes
     * @return the found object or null
     * @throws IdentificationException in case of ambiguous identifying attributes
     */
    @SuppressWarnings("unchecked")
    public <T, I> T getExisting(T object) throws IdentificationException {
        // Use type-specific identifier, if available
        if (object instanceof Sample sample) {
            return (T) sampleIdentifier.getExisting(sample);
        } else if (object instanceof Measm measm) {
            return (T) measmIdentifier.getExisting(measm);
        }

        // Otherwise identify using ID attributes
        Class<T> type = (Class<T>) object.getClass();
        EntityType<T> entityType = repository.entityManager().getMetamodel()
            .entity(type);
        Set<SingularAttribute<? super T, ?>> idAttributes;
        if (entityType.hasSingleIdAttribute()) {
            idAttributes =
                Set.of(entityType.getId(entityType.getIdType().getJavaType()));
        } else {
            idAttributes = entityType.getIdClassAttributes();
        }
        QueryBuilder<T> queryBuilder = repository.queryBuilder(type);
        for (SingularAttribute<? super T, ?> idAttr : idAttributes) {
            SingularAttribute<? super T, I> idAttribute =
                (SingularAttribute<? super T, I>) idAttr;
            try {
                I id = (I) new PropertyDescriptor(idAttr.getName(), type)
                    .getReadMethod().invoke(object);
                queryBuilder.and(idAttribute, id);
            } catch (IntrospectionException | ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return repository.getSingle(queryBuilder.getQuery());
        } catch (NoResultException e) {
            return null;
        }
    }
}
