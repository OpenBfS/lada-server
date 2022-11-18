/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.jboss.logging.Logger;

import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.QueryMeasFacilMp;
import de.intevation.lada.model.master.QueryUser;
import de.intevation.lada.rest.LadaService;


/**
 * REST-Service for preconfigured queries.
 * <p>
 * The services produce data in the application/json media type.
 * All HTTP methods use the authorization module to determine if the user is
 * allowed to perform the requested action.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean];
 *  "message": [string],
 *  "data":[{
 *      "id": [string],
 *      "name": [string],
 *      "description": [string],
 *      "sql": [string],
 *      "filters": [array],
 *      "results": [array]
 *  }],
 *  "errors": [object],
 *  "warnings": [object],
 *  "readonly": [boolean],
 *  "totalCount": [number]
 * }
 * </code>
 * </pre>
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("rest/query")
public class QueryService extends LadaService {

    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    @Inject
    Logger logger;

    static final Integer DEFAULT_USER_ID = 0;

    /**
     * Request all queries (query_user table).
     * @return All queries owned by the user, connected to the user's
     *         messstelle or owned by the default user.
     */
    @GET
    @Path("/")
    public Response getQueries() {
        UserInfo userInfo = authorization.getInfo();
        EntityManager em = repository.entityManager();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<QueryUser> criteriaQuery =
            builder.createQuery(QueryUser.class);
        Root<QueryUser> root = criteriaQuery.from(QueryUser.class);
        Join<MeasFacil, QueryUser> mess =
            root.join("messStelles", javax.persistence.criteria.JoinType.LEFT);
        Predicate filter =
            builder.equal(root.get("ladaUserId"), userInfo.getUserId());
        filter = builder.or(filter, root.get("ladaUserId").in(DEFAULT_USER_ID));
        if (userInfo.getMessstellen() != null
            && !userInfo.getMessstellen().isEmpty()
        ) {
            filter = builder.or(
                filter, mess.get("measFacilId").in(userInfo.getMessstellen()));
        }
        criteriaQuery.where(filter);

        List<QueryUser> queries =
            repository.filterPlain(criteriaQuery);
        for (QueryUser query: queries) {
            if (query.getMessStelles() != null
                && query.getMessStelles().size() > 0
            ) {
                List<String> mstIds = new ArrayList<String>();
                for (QueryMeasFacilMp mst: query.getMessStelles()) {
                    mstIds.add(mst.getMeasFacilId());
                }
                query.setMessStellesIds(
                    mstIds.toArray(new String[mstIds.size()]));
            }
        }
        return new Response(true, StatusCodes.OK, queries);
    }

    /**
     * Create a new query_user object in the database.
     */
    @POST
    @Path("/")
    public Response create(
        QueryUser query
    ) {
        UserInfo userInfo = authorization.getInfo();
        if (query.getLadaUserId() != null
            && !query.getLadaUserId().equals(userInfo.getUserId())
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        } else {
            query.setLadaUserId(userInfo.getUserId());
            for (String m : query.getMessStellesIds()) {
                QueryMeasFacilMp qms = new QueryMeasFacilMp();
                qms.setMeasFacilId(m);
                qms.setQueryUser(query);
                query.addMessStelle(qms);
            }
            return repository.create(query);
        }
    }

    /**
     * Update an existing query_user object in the database.
     */
    @PUT
    @Path("/{id}")
    public Response update(
        QueryUser query
    ) {
        UserInfo userInfo = authorization.getInfo();
        if (query.getLadaUserId() != null
            && !query.getLadaUserId().equals(userInfo.getUserId())
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }

        query.setLadaUserId(userInfo.getUserId());
        QueryBuilder<QueryMeasFacilMp> builder =
            repository.queryBuilder(QueryMeasFacilMp.class);
        builder.and("queryUser", query.getId());
        List<QueryMeasFacilMp> qms =
            repository.filterPlain(builder.getQuery());
        List<QueryMeasFacilMp> delete = new ArrayList<>();
        List<String> create = new ArrayList<>();
        for (String mst : query.getMessStellesIds()) {
            boolean hit = false;
            for (QueryMeasFacilMp qm : qms) {
                if (mst.equals(qm.getMeasFacilId())) {
                    hit = true;
                }
            }
            if (!hit) {
                create.add(mst);
            }
        }
        for (QueryMeasFacilMp qm : qms) {
            boolean hit = false;
            for (String mst : query.getMessStellesIds()) {
                if (mst.equals(qm.getMeasFacilId())) {
                    hit = true;
                }
            }
            if (!hit) {
                delete.add(qm);
            }
        }
        List<QueryMeasFacilMp> dbMesstelles =
            repository.getByIdPlain(
                QueryUser.class, query.getId()).getMessStelles();
        query.setMessStelles(dbMesstelles);

        for (QueryMeasFacilMp qm : delete) {
            Iterator<QueryMeasFacilMp> qmIter =
                query.getMessStelles().iterator();
            while (qmIter.hasNext()) {
                QueryMeasFacilMp qmi = qmIter.next();
                if (qmi.getId().equals(qm.getId())) {
                    repository.delete(qmi);
                    qmIter.remove();
                    break;
                }
            }
        }
        for (String mst : create) {
            QueryMeasFacilMp qm = new QueryMeasFacilMp();
            qm.setMeasFacilId(mst);
            qm.setQueryUser(query);
            query.addMessStelle(qm);
        }

        return repository.update(query);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        UserInfo userInfo = authorization.getInfo();
        QueryUser query = repository.getByIdPlain(QueryUser.class, id);
        if (query == null) {
            return new Response(false, StatusCodes.NOT_EXISTING, null);
        }
        if (query.getLadaUserId().equals(userInfo.getUserId())) {
            return repository.delete(query);
        }
        return new Response(false, StatusCodes.NOT_ALLOWED, null);
    }
}
