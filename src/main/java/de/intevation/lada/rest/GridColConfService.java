/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.util.auth.UserInfo;
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
public class GridColConfService
    extends LadaIntegerIdEntityEditingService<GridColConf> {

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

    @Override
    public GridColConf create(
        GridColConf gridColumnValue
    ) throws BadRequestException {
        UserInfo userInfo = authorization.getInfo();

        // TODO: Move to authorization
        if (gridColumnValue.getLadaUserId() != null
            && !gridColumnValue.getLadaUserId().equals(userInfo.getUserId())
        ) {
            throw new ForbiddenException();
        }
        gridColumnValue.setLadaUserId(userInfo.getUserId());
        GridColMp gridColumn = new GridColMp();
        gridColumn.setId(gridColumnValue.getGridColMpId());
        gridColumnValue.setGridColMp(gridColumn);

        QueryUser queryUser = repository.getById(
            QueryUser.class, gridColumnValue.getQueryUserId());
        gridColumnValue.setQueryUser(queryUser);

        return super.create(gridColumnValue);
    }

    @Override
    public GridColConf update(
        GridColConf gridColumnValue
    ) throws BadRequestException {
        // TODO: Really authorize with an Authorizer implementation.
        // Currently any object can be hijacked by passing it with
        // userId set to the users ID.
        UserInfo userInfo = authorization.getInfo();
        if (!userInfo.getUserId().equals(gridColumnValue.getLadaUserId())) {
            throw new ForbiddenException();
        }
        GridColMp gridColumn = repository.getById(
            GridColMp.class, gridColumnValue.getGridColMpId());
        QueryUser queryUser = repository.getById(
            QueryUser.class, gridColumnValue.getQueryUserId());
        gridColumnValue.setGridColMp(gridColumn);
        gridColumnValue.setQueryUser(queryUser);

        return super.update(gridColumnValue);
    }

    /**
     * Delete the given GridColConf.
     */
    @DELETE
    @Path("{id}")
    public void delete() {
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
