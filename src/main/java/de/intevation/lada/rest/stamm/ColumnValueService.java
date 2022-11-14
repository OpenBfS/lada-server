/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import de.intevation.lada.model.stammdaten.GridColumn;
import de.intevation.lada.model.stammdaten.GridColumnValue;
import de.intevation.lada.model.stammdaten.MessStelle;
import de.intevation.lada.model.stammdaten.QueryUser;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.rest.LadaService;


/**
 * REST-Service for user defined columns.
 * <p>
 * The services produce data in the application/json media type.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("rest/columnvalue")
public class ColumnValueService extends LadaService {

    @Inject
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Request user defined GridColumnValue objects.
     * @param qid query ID
     * @return GridColumnValue objects referencing the given query ID.
     */
    @GET
    @Path("/")
    public Response getQueries(
        @QueryParam("qid") @NotNull Integer qid
    ) {
        UserInfo userInfo = authorization.getInfo();
        EntityManager em = repository.entityManager();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<GridColumnValue> criteriaQuery =
            builder.createQuery(GridColumnValue.class);
        Root<GridColumnValue> root = criteriaQuery.from(GridColumnValue.class);
        Join<GridColumnValue, QueryUser> value =
            root.join("queryUser", javax.persistence.criteria.JoinType.LEFT);
        Join<MessStelle, QueryUser> mess =
            value.join("messStelles", javax.persistence.criteria.JoinType.LEFT);
        Predicate filter = builder.equal(root.get("queryUser"), qid);
        Predicate uId = builder.equal(root.get("userId"), userInfo.getUserId());
        Predicate zeroIdFilter = builder.equal(root.get("userId"), "0");
        Predicate userFilter = builder.or(uId, zeroIdFilter);
        if (userInfo.getMessstellen() != null
            && !userInfo.getMessstellen().isEmpty()
        ) {
            userFilter = builder.or(
                userFilter,
                mess.get("messStelle").in(userInfo.getMessstellen()));
        }
        filter = builder.and(filter, userFilter);
        criteriaQuery.where(filter).distinct(true);
        List<GridColumnValue> queries =
            repository.filterPlain(criteriaQuery);

        for (GridColumnValue gcv : queries) {
            gcv.setgridColumnId(gcv.getGridColumn().getId());
            gcv.setQueryUserId(gcv.getQueryUser().getId());
        }

        return new Response(true, StatusCodes.OK, queries);
    }

    /**
     * Creates a new grid_column_value in the database.
     * @return Response containing the created record.
     */
    @POST
    @Path("/")
    public Response create(
        GridColumnValue gridColumnValue
    ) {
        UserInfo userInfo = authorization.getInfo();
        if (gridColumnValue.getUserId() != null
            && !gridColumnValue.getUserId().equals(userInfo.getUserId())
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        } else {
            gridColumnValue.setUserId(userInfo.getUserId());
            GridColumn gridColumn = new GridColumn();
            gridColumn.setId(gridColumnValue.getGridColumnId());
            gridColumnValue.setGridColumn(gridColumn);


            QueryUser queryUser = repository.getByIdPlain(
                QueryUser.class, gridColumnValue.getQueryUserId());
            gridColumnValue.setQueryUser(queryUser);

            return repository.create(gridColumnValue);
        }

    }

    /**
     * Update an existing grid_column_value in the database.
     * @return Response containing the updated record.
     */
    @PUT
    @Path("/{id}")
    public Response update(
        GridColumnValue gridColumnValue
    ) {
        // TODO: Really authorize with an Authorizer implementation.
        // Currently any object can be hijacked by passing it with
        // userId set to the users ID.
        UserInfo userInfo = authorization.getInfo();
        if (!userInfo.getUserId().equals(gridColumnValue.getUserId())) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        } else {
            GridColumn gridColumn = repository.getByIdPlain(
                GridColumn.class, gridColumnValue.getGridColumnId());
            QueryUser queryUser = repository.getByIdPlain(
                QueryUser.class, gridColumnValue.getQueryUserId());
            if (gridColumn == null || queryUser == null) {
                return new Response(false, StatusCodes.VALUE_MISSING, null);
            }

            gridColumnValue.setGridColumn(gridColumn);
            gridColumnValue.setQueryUser(queryUser);

            return repository.update(gridColumnValue);
        }
    }

    /**
     * Delete the given column.
     * @param id The id is appended to the URL as a path parameter.
     * @return Response containing the deleted record.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        UserInfo userInfo = authorization.getInfo();
        GridColumnValue gridColumnValue = repository.getByIdPlain(
            GridColumnValue.class, id);
        if (gridColumnValue.getUserId().equals(userInfo.getUserId())) {
            return repository.delete(gridColumnValue);
        }
        return new Response(false, StatusCodes.NOT_ALLOWED, null);
    }
}
