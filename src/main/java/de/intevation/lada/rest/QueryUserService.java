/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.jboss.logging.Logger;

import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.model.master.QueryMeasFacilMp;
import de.intevation.lada.model.master.QueryMeasFacilMp_;
import de.intevation.lada.model.master.QueryUser;
import de.intevation.lada.model.master.QueryUser_;


/**
 * REST-Service for preconfigured queries.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "queryuser")
public class QueryUserService extends LadaService {

    @Inject
    private Repository repository;

    @Inject
    Logger logger;

    static final Integer DEFAULT_USER_ID = 0;

    /**
     * Request all queries (query_user table).
     * @return All queries owned by the user, connected to the user's
     *         messstelle or owned by the default user.
     */
    @GET
    public List<QueryUser> getQueries() {
        UserInfo userInfo = authorization.getInfo();
        EntityManager em = repository.entityManager();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<QueryUser> criteriaQuery =
            builder.createQuery(QueryUser.class);
        Root<QueryUser> root = criteriaQuery.from(QueryUser.class);
        Join<QueryUser, QueryMeasFacilMp> mess =
            root.join(QueryUser_.messStelles, JoinType.LEFT);
        Predicate filter =
            builder.equal(
                root.get(QueryUser_.ladaUserId),
                userInfo.getUserId());
        filter = builder.or(filter, root.get(QueryUser_.ladaUserId)
            .in(DEFAULT_USER_ID));
        if (userInfo.getMessstellen() != null
            && !userInfo.getMessstellen().isEmpty()
        ) {
            filter = builder.or(
                filter,
                mess.get(QueryMeasFacilMp_.measFacilId)
                    .in(userInfo.getMessstellen()));
        }
        criteriaQuery.where(filter);

        List<QueryUser> queries =
            repository.filter(criteriaQuery);
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
        return queries;
    }

    /**
     * Create a new query_user object in the database.
     * @param Query object
     * @return created query object
     * @throws BadRequestException if any constraint violations are detected.
     */
    @POST
    public QueryUser create(
        @Valid QueryUser query
    ) throws BadRequestException {
        UserInfo userInfo = authorization.getInfo();
        // TODO: Move to authorization
        if (query.getLadaUserId() != null
            && !query.getLadaUserId().equals(userInfo.getUserId())
        ) {
            throw new ForbiddenException();
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
     *
     * @param query The query to be updated
     * @return Updated query
     * @throws BadRequestException if any constraint violations are detected.
     */
    @PUT
    @Path("{id}")
    public QueryUser update(
        @Valid QueryUser query
    ) throws BadRequestException {
        UserInfo userInfo = authorization.getInfo();
        // TODO: Move to authorization
        if (query.getLadaUserId() != null
            && !query.getLadaUserId().equals(userInfo.getUserId())
        ) {
            throw new ForbiddenException();
        }

        query.setLadaUserId(userInfo.getUserId());
        QueryBuilder<QueryMeasFacilMp> builder = repository
            .queryBuilder(QueryMeasFacilMp.class)
            .and(QueryMeasFacilMp_.queryUser, query);
        List<QueryMeasFacilMp> qms =
            repository.filter(builder.getQuery());
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
            repository.getById(
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
    @Path("{id}")
    public void delete(
        @PathParam("id") Integer id
    ) {
        UserInfo userInfo = authorization.getInfo();
        QueryUser query = repository.getById(QueryUser.class, id);
        // TODO: Move to authorization
        if (query.getLadaUserId().equals(userInfo.getUserId())) {
            repository.delete(query);
            return;
        }
        throw new ForbiddenException();
    }
}
