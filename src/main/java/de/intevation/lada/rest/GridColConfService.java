/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.model.master.GridColConf;
import de.intevation.lada.model.master.GridColMp;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.QueryUser;


/**
 * REST-Service for user defined columns.
 * <p>
 * The services produce data in the application/json media type.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("gridcolconf")
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
    public Response getQueries(
        @QueryParam("queryUser") @NotNull Integer queryUser
    ) {
        UserInfo userInfo = authorization.getInfo();
        EntityManager em = repository.entityManager();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<GridColConf> criteriaQuery =
            builder.createQuery(GridColConf.class);
        Root<GridColConf> root = criteriaQuery.from(GridColConf.class);
        Join<GridColConf, QueryUser> value =
            root.join("queryUser", javax.persistence.criteria.JoinType.LEFT);
        Join<MeasFacil, QueryUser> mess =
            value.join("messStelles", javax.persistence.criteria.JoinType.LEFT);
        Predicate filter = builder.equal(root.get("queryUser"), queryUser);
        Predicate uId = builder.equal(root.get("ladaUserId"), userInfo.getUserId());
        Predicate zeroIdFilter = builder.equal(root.get("ladaUserId"), "0");
        Predicate userFilter = builder.or(uId, zeroIdFilter);
        if (userInfo.getMessstellen() != null
            && !userInfo.getMessstellen().isEmpty()
        ) {
            userFilter = builder.or(
                userFilter,
                mess.get("measFacilId").in(userInfo.getMessstellen()));
        }
        filter = builder.and(filter, userFilter);
        criteriaQuery.where(filter).distinct(true);
        List<GridColConf> queries =
            repository.filterPlain(criteriaQuery);

        for (GridColConf gcv : queries) {
            gcv.setGridColMpId(gcv.getGridColMp().getId());
            gcv.setQueryUserId(gcv.getQueryUser().getId());
        }

        return new Response(true, StatusCodes.OK, queries);
    }

    /**
     * Creates a new GridColConf in the database.
     * @return Response containing the created record.
     */
    @POST
    public Response create(
        @Valid GridColConf gridColumnValue
    ) {
        UserInfo userInfo = authorization.getInfo();
        if (gridColumnValue.getLadaUserId() != null
            && !gridColumnValue.getLadaUserId().equals(userInfo.getUserId())
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        } else {
            gridColumnValue.setLadaUserId(userInfo.getUserId());
            GridColMp gridColumn = new GridColMp();
            gridColumn.setId(gridColumnValue.getGridColMpId());
            gridColumnValue.setGridColMp(gridColumn);


            QueryUser queryUser = repository.getByIdPlain(
                QueryUser.class, gridColumnValue.getQueryUserId());
            gridColumnValue.setQueryUser(queryUser);

            return repository.create(gridColumnValue);
        }

    }

    /**
     * Update an existing GridColConf in the database.
     * @return Response containing the updated record.
     */
    @PUT
    @Path("{id}")
    public Response update(
        @Valid GridColConf gridColumnValue
    ) {
        // TODO: Really authorize with an Authorizer implementation.
        // Currently any object can be hijacked by passing it with
        // userId set to the users ID.
        UserInfo userInfo = authorization.getInfo();
        if (!userInfo.getUserId().equals(gridColumnValue.getLadaUserId())) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        } else {
            GridColMp gridColumn = repository.getByIdPlain(
                GridColMp.class, gridColumnValue.getGridColMpId());
            QueryUser queryUser = repository.getByIdPlain(
                QueryUser.class, gridColumnValue.getQueryUserId());
            if (gridColumn == null || queryUser == null) {
                return new Response(false, StatusCodes.VALUE_MISSING, null);
            }

            gridColumnValue.setGridColMp(gridColumn);
            gridColumnValue.setQueryUser(queryUser);

            return repository.update(gridColumnValue);
        }
    }

    /**
     * Delete the given GridColConf.
     * @param id The id is appended to the URL as a path parameter.
     * @return Response containing the deleted record.
     */
    @DELETE
    @Path("{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        UserInfo userInfo = authorization.getInfo();
        GridColConf gridColumnValue = repository.getByIdPlain(
            GridColConf.class, id);
        if (gridColumnValue.getLadaUserId().equals(userInfo.getUserId())) {
            return repository.delete(gridColumnValue);
        }
        return new Response(false, StatusCodes.NOT_ALLOWED, null);
    }
}
