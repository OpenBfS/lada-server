/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.model.master.GridColConf;
import de.intevation.lada.model.master.GridColConf_;
import de.intevation.lada.model.master.GridColMp;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.QueryMeasFacilMp_;
import de.intevation.lada.model.master.QueryUser;
import de.intevation.lada.model.master.QueryUser_;


/**
 * REST-Service for user defined columns.
 * <p>
 * The services produce data in the application/json media type.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "gridcolconf")
public class GridColConfService extends LadaService {

    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Request user defined GridColConf objects.
     * @param queryUser query ID
     * @return GridColConf objects referencing the given query ID.
     */
    @GET
    public List<GridColConf> getQueries(
        @QueryParam("queryUser") @NotNull Integer queryUser
    ) {
        UserInfo userInfo = authorization.getInfo();
        EntityManager em = repository.entityManager();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<GridColConf> criteriaQuery =
            builder.createQuery(GridColConf.class);
        Root<GridColConf> root = criteriaQuery.from(GridColConf.class);
        Join<GridColConf, QueryUser> value =
            root.join(GridColConf_.queryUser, jakarta.persistence.criteria.JoinType.LEFT);
        Join<MeasFacil, QueryUser> mess =
            value.join(QueryUser_.MESS_STELLES, jakarta.persistence.criteria.JoinType.LEFT);
        Predicate filter = builder.equal(root.get(GridColConf_.queryUser).get(QueryUser_.id), queryUser);
        Predicate uId = builder.equal(root.get(GridColConf_.ladaUserId), userInfo.getUserId());
        Predicate zeroIdFilter = builder.equal(root.get(GridColConf_.ladaUserId), "0");
        Predicate userFilter = builder.or(uId, zeroIdFilter);
        if (userInfo.getMessstellen() != null
            && !userInfo.getMessstellen().isEmpty()
        ) {
            userFilter = builder.or(
                userFilter,
                mess.get(QueryMeasFacilMp_.MEAS_FACIL_ID).in(userInfo.getMessstellen()));
        }
        filter = builder.and(filter, userFilter);
        criteriaQuery.where(filter).distinct(true);
        List<GridColConf> queries =
            repository.filter(criteriaQuery);

        for (GridColConf gcv : queries) {
            gcv.setGridColMpId(gcv.getGridColMp().getId());
            gcv.setQueryUserId(gcv.getQueryUser().getId());
        }

        return queries;
    }

    /**
     * Creates a new GridColConf in the database.
     * @return the created record.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @POST
    public GridColConf create(
        @Valid GridColConf gridColumnValue
    ) throws BadRequestException {
        UserInfo userInfo = authorization.getInfo();

        // TODO: Move to authorization
        if (gridColumnValue.getLadaUserId() != null
            && !gridColumnValue.getLadaUserId().equals(userInfo.getUserId())
        ) {
            throw new ForbiddenException();
        } else {
            gridColumnValue.setLadaUserId(userInfo.getUserId());
            GridColMp gridColumn = new GridColMp();
            gridColumn.setId(gridColumnValue.getGridColMpId());
            gridColumnValue.setGridColMp(gridColumn);

            QueryUser queryUser = repository.getById(
                QueryUser.class, gridColumnValue.getQueryUserId());
            gridColumnValue.setQueryUser(queryUser);

            return repository.create(gridColumnValue);
        }
    }

    /**
     * Update an existing GridColConf in the database.
     * @return the updated record.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @PUT
    @Path("{id}")
    public GridColConf update(
        @Valid GridColConf gridColumnValue
    ) throws BadRequestException {
        // TODO: Really authorize with an Authorizer implementation.
        // Currently any object can be hijacked by passing it with
        // userId set to the users ID.
        UserInfo userInfo = authorization.getInfo();
        if (!userInfo.getUserId().equals(gridColumnValue.getLadaUserId())) {
            throw new ForbiddenException();
        } else {
            GridColMp gridColumn = repository.getById(
                GridColMp.class, gridColumnValue.getGridColMpId());
            QueryUser queryUser = repository.getById(
                QueryUser.class, gridColumnValue.getQueryUserId());
            gridColumnValue.setGridColMp(gridColumn);
            gridColumnValue.setQueryUser(queryUser);

            return repository.update(gridColumnValue);
        }
    }

    /**
     * Delete the given GridColConf.
     * @param id The id is appended to the URL as a path parameter.
     */
    @DELETE
    @Path("{id}")
    public void delete(
        @PathParam("id") Integer id
    ) {
        UserInfo userInfo = authorization.getInfo();
        GridColConf gridColumnValue = repository.getById(
            GridColConf.class, id);
        // TODO: Move to authorization
        if (gridColumnValue.getLadaUserId().equals(userInfo.getUserId())) {
            repository.delete(gridColumnValue);
            return;
        }
        throw new ForbiddenException();
    }
}
