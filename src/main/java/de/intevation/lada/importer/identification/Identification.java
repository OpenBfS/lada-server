/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.identification;

import java.beans.IntrospectionException;
import java.lang.reflect.ParameterizedType;
import java.util.Iterator;

import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;


/**
 * Facility for identification of entity instances.
 */
public class Identification {

    @Inject
    Instance<Identifier<?>> identifiers;

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
    public <T> T getExisting(T object) throws IdentificationException {
        // Use type-specific identifier, if available
        Iterator<Identifier<?>> iterator = identifiers.iterator();
        while (iterator.hasNext()) {
            Identifier<?> identifier = iterator.next();
            ParameterizedType type = (ParameterizedType) identifier
                .getClass().getGenericInterfaces()[0];
            if (object.getClass().equals(type.getActualTypeArguments()[0])) {
                return ((Identifier<T>) identifier).getExisting(object);
            }
        }

        // Otherwise identify using ID attribute
        Class<T> type = (Class<T>) object.getClass();
        QueryBuilder<T> queryBuilder = repository.queryBuilder(type);
        try {
            queryBuilder.and(repository.idAttribute(type),
                repository.idProperty(type).getReadMethod().invoke(object));
        } catch (IntrospectionException | ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        try {
            return repository.getSingle(queryBuilder.getQuery());
        } catch (NoResultException e) {
            return null;
        }
    }
}
