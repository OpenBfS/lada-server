/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.data;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;


/**
 * Provides various methods for database access.
 *
 * Classes calling these methods have to ensure to do this inside
 * a transaction context.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ApplicationScoped
@Transactional(value = Transactional.TxType.MANDATORY,
    dontRollbackOn = {
        /* Although the API documentation states for these Exceptions that
           they "will not cause the current transaction [...] to be marked
           for rollback", they have to be named here to make this real. */
        NoResultException.class,
        NonUniqueResultException.class})
public class Repository {

    @Inject
    private Logger logger;

    @PersistenceContext
    EntityManager em;

    /**
     * Create and persist a new object in the database.
     *
     * @param object The new object.
     * @param <T> Type of object.
     *
     * @return The new object, potentially modified by the database.
     */
    public <T> T create(T object) {
        em.persist(object);
        em.flush();
        /* Refreshing the object is necessary because some objects use
           dynamic-insert, meaning null-valued columns are not INSERTed
           to the DB to take advantage of DB DEFAULT values, or triggers
           modify the object during INSERT. */
        em.refresh(object);
        return object;
    }

    /**
     * Update an existing object in the database.
     *
     * @param object The object.
     * @param <T> Type of object.
     *
     * @return The updated object.
     */
    public <T> T update(T object) {
        T managedObject = em.merge(object);
        /* Flushing and refreshing is necessary because e.g. triggers can modify
           the object in the database during UPDATE. */
        em.flush();
        em.refresh(managedObject);
        return managedObject;
    }

    /**
     * Delete an object from the database.
     *
     * @param object The object.
     */
    public void delete(Object object) {
        em.remove(
            em.contains(object)
            ? object : em.merge(object));
        em.flush();
    }

    /**
     * Get Query from SQL statement.
     *
     * @param sql String representing a native SQL statement.
     *
     * @return Query representing the native SQL statement.
     */
    public Query queryFromString(String sql) {
        return em.createNativeQuery(sql);
    }

    /**
     * @return EntityManager associated with this Repository.
     */
    public EntityManager entityManager() {
        return em;
    }

    /**
     * Get QueryBuilder for given class.
     *
     * @param <T> The class for which a QueryBuilder is requested.
     * @param c The class for which a QueryBuilder is requested.
     *
     * @return QueryBuilder for given class.
     */
    public <T> QueryBuilder<T> queryBuilder(Class<T> c) {
        return new QueryBuilder<T>(em, c);
    }

    /**
     * Get objects from database using the given filter.
     *
     * @param <T> The type of the objects.
     * @param filter Filter used to request objects.
     *
     * @return List<T> with the requested objects.
     */
    public <T> List<T> filter(CriteriaQuery<T> filter) {
        return em.createQuery(filter).getResultList();
    }

    /**
     * Get a single object from database using the given filter.
     *
     * The filter has to select a single entry,
     * e.g. by a column with UNIQUE constraint.
     *
     * @param <T> The type of the objects.
     * @param filter Filter used to request objects.
     *
     * @return T The requested object.
     *
     * @throws NoResultException if there is no result
     * @throws NonUniqueResultException if more than one result
     */
    public <T> T getSingle(
        CriteriaQuery<T> filter
    ) throws NoResultException, NonUniqueResultException {
        return (T) em.createQuery(filter).getSingleResult();
    }

    /**
     * Get all objects.
     *
     * @param <T> The type of the objects.
     * @param clazz The type of the objects.
     *
     * @return List<T> with the objects of the requested type.
     */
    public <T> List<T> getAll(Class<T> clazz) {
        QueryBuilder<T> builder = queryBuilder(clazz);
        return em.createQuery(builder.getQuery()).getResultList();
    }

    /**
     * Get an object by its id.
     *
     * @param <T> The type of the objects.
     * @param clazz The type of the object.
     * @param id The id of the object.
     *
     * @return The requested object or null if not found.
     * @throws NotFoundException if no entity with given ID exists
     */
    public <T> T getById(Class<T> clazz, Object id) {
        T item = em.find(clazz, id);
        if (item == null) {
            throw new NotFoundException();
        }
        return item;
    }
}
