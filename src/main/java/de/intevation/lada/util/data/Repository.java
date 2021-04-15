/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.data;

import java.util.List;

import javax.json.JsonArray;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;

import de.intevation.lada.util.rest.Response;

/**
 * This generic Container is an interface to request and select Data
 * objects from the connected database.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public interface Repository {

     Response create(Object object);

     Response update(Object object);

     Response delete(Object object);

     <T> Response filter(CriteriaQuery<T> filter);

     <T> List<T> filterPlain(CriteriaQuery<T> filter);

     <T> List<T> filterPlain(
         QueryBuilder<T> query,
         JsonArray filter
     );

     <T> Response filter(
        CriteriaQuery<T> filter,
        int size,
        int start
     );

    <T> List<T> filterPlain(
        CriteriaQuery<T> filter,
        int size,
        int start
    );

    <T> Response getAll(Class<T> clazz);

    <T> List<T> getAllPlain(Class<T> clazz);

    <T> Response getById(Class<T> clazz, Object id);

    <T> T getByIdPlain(Class<T> clazz, Object id);

    Query queryFromString(String sql);

    EntityManager entityManager();
}
