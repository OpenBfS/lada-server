/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.data;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;

import org.apache.log4j.Logger;

import de.intevation.lada.util.rest.Response;


/**
 * Provides various methods for database access.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ApplicationScoped
public class Repository {

    @Inject
    private Logger logger;

    @PersistenceContext
    EntityManager em;

    /**
     * Create and persist a new object in the database.
     *
     * @param object The new object.
     *
     * @return Response object containing the new object, potentially
     *         modified by the database.
     */
    public Response create(Object object) {
        em.persist(object);
        em.flush();
        /* Refreshing the object is necessary because some objects use
           dynamic-insert, meaning null-valued columns are not INSERTed
           to the DB to take advantage of DB DEFAULT values, or triggers
           modify the object during INSERT. */
        em.refresh(object);
        return new Response(true, StatusCodes.OK, object);
    }

    /**
     * Update an existing object in the database.
     *
     * @param object The object.
     *
     * @return Response object containing the upadted object.
     */
    public Response update(Object object) {
        object = em.merge(object);
        /* Flushing and refreshing is necessary because e.g. triggers can modify
           the object in the database during UPDATE. */
        em.flush();
        em.refresh(object);
        return new Response(true, StatusCodes.OK, object);
    }

    /**
     * Delete an object from the database.
     *
     * @param object The object.
     *
     * @return Response object.
     */
    public Response delete(Object object) {
        em.remove(
            em.contains(object)
            ? object : em.merge(object));
        em.flush();
        return new Response(true, StatusCodes.OK, "");
    }

    /**
     * Get objects from database using the given filter.
     *
     * @param <T> The type of the objects.
     * @param filter Filter used to request objects.
     *
     * @return Response object containing the filtered list of objects.
     */
    public <T> Response filter(CriteriaQuery<T> filter) {
        List<T> result = filterPlain(filter);
        return new Response(true, StatusCodes.OK, result);
    }

    /**
     * Get all objects.
     *
     * @param <T> The type of the objects.
     * @param clazz The type of the objects.
     *
     * @return Response object containg all requested objects.
     */
    public <T> Response getAll(Class<T> clazz) {
        List<T> result = getAllPlain(clazz);
        return new Response(true, StatusCodes.OK, result);
    }

    /**
     * Get an object by its id.
     *
     * @param <T> The type of the objects.
     * @param clazz The type of the object.
     * @param id The id of the object.
     *
     * @return Response object containg the requested object.
     */
    public <T> Response getById(Class<T> clazz, Object id) {
        T item = getByIdPlain(clazz, id);
        if (item == null) {
            return new Response(false, StatusCodes.NOT_EXISTING, null);
        }
        return new Response(true, StatusCodes.OK, item);
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
    public <T> List<T> filterPlain(CriteriaQuery<T> filter) {
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
    public <T> T getSinglePlain(
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
    public <T> List<T> getAllPlain(Class<T> clazz) {
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
     */
    public <T> T getByIdPlain(Class<T> clazz, Object id) {
        return em.find(clazz, id);
    }
}
