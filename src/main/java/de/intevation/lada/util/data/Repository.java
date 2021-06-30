/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.data;

import java.util.List;

import javax.ejb.EJBTransactionRolledbackException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;
import javax.persistence.criteria.CriteriaQuery;

import org.apache.log4j.Logger;
import org.hibernate.TransactionException;

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

    @Inject
    private DataTransaction transaction;

    /**
     * Create and persist a new object in the database.
     *
     * @param object The new object.
     *
     * @return Response object containing the new object, potentially
     *         modified by the database.
     */
    public Response create(Object object) {
        try {
            transaction.persistInDatabase(object);
        } catch (EntityExistsException eee) {
            logger.error("Could not persist " + object.getClass().getName()
                + ". Reason: " + eee.getClass().getName() + " - "
                + eee.getMessage());
            return new Response(false, StatusCodes.PRESENT, object);
        } catch (IllegalArgumentException iae) {
            logger.error("Could not persist " + object.getClass().getName()
                + ". Reason: " + iae.getClass().getName() + " - "
                + iae.getMessage());
            return new Response(false, StatusCodes.NOT_A_PROBE, object);
        } catch (TransactionRequiredException tre) {
            logger.error("Could not persist " + object.getClass().getName()
                + ". Reason: " + tre.getClass().getName() + " - "
                + tre.getMessage());
            return new Response(false, StatusCodes.ERROR_DB_CONNECTION, object);
        } catch (EJBTransactionRolledbackException ete) {
            logger.error("Could not persist " + object.getClass().getName()
                + ". Reason: " + ete.getClass().getName() + " - "
                + ete.getMessage());
            return new Response(false, StatusCodes.ERROR_VALIDATION, object);
        }
        Response response = new Response(true, StatusCodes.OK, object);
        return response;
    }

    /**
     * Update an existing object in the database.
     *
     * @param object The object.
     *
     * @return Response object containing the upadted object.
     */
    public Response update(Object object) {
        Response response = new Response(true, StatusCodes.OK, object);
        try {
            transaction.updateInDatabase(object);
        } catch (EntityExistsException eee) {
            return new Response(false, StatusCodes.PRESENT, object);
        } catch (IllegalArgumentException iae) {
            return new Response(false, StatusCodes.NOT_A_PROBE, object);
        } catch (TransactionRequiredException tre) {
            return new Response(false, StatusCodes.ERROR_DB_CONNECTION, object);
        } catch (EJBTransactionRolledbackException ete) {
            return new Response(false, StatusCodes.ERROR_VALIDATION, object);
        } catch (TransactionException te) {
            return new Response(false, StatusCodes.ERROR_VALIDATION, object);
        }
        return response;
    }

    /**
     * Delete an object from the database.
     *
     * @param object The object.
     *
     * @return Response object.
     */
    public Response delete(Object object) {
        Response response = new Response(true, StatusCodes.OK, "");
        try {
            transaction.removeFromDatabase(object);
        } catch (IllegalArgumentException iae) {
            return new Response(false, StatusCodes.NOT_A_PROBE, object);
        } catch (TransactionRequiredException tre) {
            return new Response(false, StatusCodes.ERROR_DB_CONNECTION, object);
        } catch (EJBTransactionRolledbackException ete) {
            return new Response(false, StatusCodes.OP_NOT_POSSIBLE, object);
        }
        return response;
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
        EntityManager em = transaction.entityManager();
        return em.createNativeQuery(sql);
    }

    public EntityManager entityManager() {
        return transaction.entityManager();
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
        return new QueryBuilder<T>(transaction.entityManager(), c);
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
        return transaction.entityManager()
            .createQuery(filter).getResultList();
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
        EntityManager manager = transaction.entityManager();
        QueryBuilder<T> builder = queryBuilder(clazz);
        return manager.createQuery(builder.getQuery()).getResultList();
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
        return transaction.entityManager().find(clazz, id);
    }
}
