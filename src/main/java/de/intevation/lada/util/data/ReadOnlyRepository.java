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
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;

import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.rest.Response;


/**
 * Repository providing read access.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@RepositoryConfig(type = RepositoryType.RO)
@ApplicationScoped
public class ReadOnlyRepository implements Repository {

    @Inject
    private DataTransaction transaction;

    public ReadOnlyRepository() {
    }

    /**
     * NOT SUPPORTED.
     *
     * @return null
     */
    @Override
    public Response create(Object object) {
        return null;
    }

    /**
     * NOT SUPPORTED.
     *
     * @return null
     */
    @Override
    public Response update(Object object) {
        return null;
    }

    /**
     * NOT SUPPORTED.
     *
     * @return null
     */
    @Override
    public Response delete(Object object) {
        return null;
    }

    /**
     * Get objects from database using the given filter.
     *
     * @param filter Filter used to request objects.
     *
     * @return Response object containing the filtered list of objects.
     */
    @Override
    public <T> Response filter(CriteriaQuery<T> filter) {
        List<T> result =
            transaction.entityManager()
                .createQuery(filter).getResultList();
        return new Response(true, StatusCodes.OK, result);
    }


    /**
     * Get objects from database using the given filter.
     *
     * @param filter Filter used to request objects.
     * @param size The maximum amount of objects.
     * @param start The start index.
     *
     * @return Response object containing the filtered list of objects.
     */
    @Override
    public <T> Response filter(
        CriteriaQuery<T> filter,
        int size,
        int start
    ) {
        List<T> result =
            transaction.entityManager()
                .createQuery(filter).getResultList();
        if (size > 0 && start > -1) {
            List<T> newList = result.subList(start, size + start);
            return new Response(true, StatusCodes.OK, newList, result.size());
        }
        return new Response(true, StatusCodes.OK, result);
    }

    /**
     * Get all objects.
     *
     * @param clazz The type of the objects.
     *
     * @return Response object containg all requested objects.
     */
    public <T> Response getAll(Class<T> clazz) {
        EntityManager manager = transaction.entityManager();
        QueryBuilder<T> builder =
            new QueryBuilder<T>(manager, clazz);
        List<T> result =
            manager.createQuery(builder.getQuery()).getResultList();
        return new Response(true, StatusCodes.OK, result);
    }

    /**
     * Get an object by its id.
     *
     * @param clazz The type of the object.
     * @param id The id of the object.
     *
     * @return Response object containg the requested object.
     */
    @Override
    public <T> Response getById(Class<T> clazz, Object id) {
        T item = transaction.entityManager().find(clazz, id);
        if (item == null) {
            return new Response(false, StatusCodes.NOT_EXISTING, null);
        }
        return new Response(true, StatusCodes.OK, item);
    }

    @Override
    public Query queryFromString(String sql) {
        EntityManager em = transaction.entityManager();
        return em.createNativeQuery(sql);
    }

    @Override
    public EntityManager entityManager() {
        return transaction.entityManager();
    }

    @Override
    public <T> List<T> filterPlain(CriteriaQuery<T> filter) {
        return transaction.entityManager()
            .createQuery(filter).getResultList();
    }

    @Override
    public <T> List<T> filterPlain(
        QueryBuilder<T> query,
        JsonArray filter
    ) {
        for (JsonValue object : filter) {
            JsonObject f = (JsonObject) object;
            JsonString operator = f.getJsonString("operator");
            JsonString value = f.getJsonString("value");
            JsonString property = f.getJsonString("property");
            if (property == null || value == null) {
                continue;
            }
            if ("like".equals(operator.getString())) {
                query.andLike(
                    property.getString(), "%" + value.getString() + "%");
            }
        }
        return transaction.entityManager()
            .createQuery(query.getQuery()).getResultList();
    }

    @Override
    public <T> List<T> filterPlain(
        CriteriaQuery<T> filter, int size, int start
    ) {
        List<T> result =
            transaction.entityManager()
                .createQuery(filter).getResultList();
        if (size > 0 && start > -1) {
            return result.subList(start, size + start);
        }
        return result;
    }

    @Override
    public <T> List<T> getAllPlain(Class<T> clazz) {
        EntityManager manager = transaction.entityManager();
        QueryBuilder<T> builder =
            new QueryBuilder<T>(manager, clazz);
        return manager.createQuery(builder.getQuery()).getResultList();
    }

    @Override
    public <T> T getByIdPlain(Class<T> clazz, Object id) {
        return transaction.entityManager().find(clazz, id);
    }
}
