/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.identification;

import java.beans.IntrospectionException;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;


/**
 * Facility for identification of entity instances.
 */
public class Identification {

    @Inject
    private Identifier<Sample> sampleIdentifier;

    @Inject
    private Identifier<Measm> measmIdentifier;

    @Inject
    private Identifier<Tag> tagIdentifier;

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
        if (object instanceof Sample sample) {
            return (T) sampleIdentifier.getExisting(sample);
        } else if (object instanceof Measm measm) {
            return (T) measmIdentifier.getExisting(measm);
        } else if (object instanceof Tag tag) {
            return (T) tagIdentifier.getExisting(tag);
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
