/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.stamm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import de.intevation.lada.factory.OrtFactory;
import de.intevation.lada.importer.ReportItem;
import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Ortszuordnung;
import de.intevation.lada.model.land.StatusProtokoll;
import de.intevation.lada.model.stammdaten.Ort;
import de.intevation.lada.model.stammdaten.Verwaltungseinheit;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.data.Strings;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationConfig;

/**
 * REST service for Ort objects.
 * <p>
 * The services produce data in the application/json media type.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean];
 *  "message": [string],
 *  "data":[{
 *      "id": [number],
 *      "beschreibung": [string],
 *      "bezeichnung": [string],
 *      "hoeheLand": [number],
 *      "koordXExtern": [string],
 *      "koordYExtern": [string],
 *      "latitude": [number],
 *      "letzteAenderung": [timestamp],
 *      "longitude": [number],
 *      "nutsCode": [string],
 *      "unscharf": [boolean],
 *      "koordinatenartId": [number],
 *      "netzbetreiberId": [number],
 *      "staatId": [number],
 *      "verwaltungseinheitId": [string],
 *      "otyp": [string]
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
@Path("rest/ort")
@RequestScoped
public class OrtService {

    @Inject
    private Logger logger;

    /**
     * The data repository granting read/write access.
     */
    @Inject
    @RepositoryConfig(type = RepositoryType.RW)
    private Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    @Inject
    private OrtFactory ortFactory;

    @Inject
    @ValidationConfig(type = "Ort")
    private Validator validator;

    /**
     * Get Ort objects.
     * <p>
     * The requested objects can be filtered e.g. using a URL parameter named
     * ortId.
     * <p>
     * Example: http://example.com/rest/ort?ortId=[ID]
     *
     * @return Response object containing all (filtered) Ort objects.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
        @Context HttpServletRequest request,
        @Context UriInfo info
    ) {
        MultivaluedMap<String, String> params = info.getQueryParameters();

        //If a single ort is requested
        if (params.containsKey("ortId")) {
            Integer id;
            try {
                id = Integer.valueOf(params.getFirst("ortId"));
            } catch (NumberFormatException e) {
                return new Response(
                    false,
                    StatusCodes.ERROR_DB_CONNECTION, "Not a valid filter id");
            }

            Ort o = repository.getByIdPlain(Ort.class, id, Strings.STAMM);
            List<Ortszuordnung> zuordnungs = getOrtsZuordnungs(o);
            o.setReferenceCount(zuordnungs.size());
            o.setPlausibleReferenceCount(getPlausibleRefCount(zuordnungs));
            o.setReadonly(
                !authorization.isAuthorized(
                    request,
                    o,
                    RequestMethod.POST,
                    Ort.class));
            Violation violation = validator.validate(o);
            if (violation.hasErrors() || violation.hasWarnings()) {
                o.setErrors(violation.getErrors());
                o.setWarnings(violation.getWarnings());
            }
            return new Response(true, StatusCodes.OK, o);
        }

        List<Ort> orte = new ArrayList<>();
        UserInfo user = authorization.getInfo(request);
        EntityManager em = repository.entityManager(Strings.STAMM);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Ort> query = builder.createQuery(Ort.class);
        Root<Ort> root = query.from(Ort.class);
        Predicate filter = null;
        if (params.containsKey("netzbetreiberId")) {
            Predicate netzbetreiberFilter =
                builder.equal(
                    root.get("netzbetreiberId"),
                    params.getFirst("netzbetreiberId"));
            filter = builder.and(netzbetreiberFilter);
        } else {
            for (String nb : user.getNetzbetreiber()) {
                builder.or(builder.equal(root.get("netzbetreiberId"), nb));
            }
        }
        if (params.containsKey("search")) {
            Join<Ort, Verwaltungseinheit> join =
                root.join("gemeinde", JoinType.LEFT);
            String pattern = "%" + params.getFirst("search") + "%";
            Predicate idFilter = builder.like(root.get("ortId"), pattern);
            Predicate kurzTextFilter =
                builder.like(root.get("kurztext"), pattern);
            Predicate langtextFilter =
                builder.like(root.get("langtext"), pattern);
            Predicate bezFilter =
                builder.like(join.get("bezeichnung"), pattern);
            Predicate searchFilter =
                builder.or(idFilter, kurzTextFilter, langtextFilter, bezFilter);
            filter =
                filter == null
                ? searchFilter : builder.and(filter, searchFilter);
        }
        if (filter != null) {
            query.where(filter);
        }
        orte = repository.filterPlain(query, Strings.STAMM);

        int size = orte.size();
        if (params.containsKey("start") && params.containsKey("limit")) {
            int start = Integer.valueOf(params.getFirst("start"));
            int limit = Integer.valueOf(params.getFirst("limit"));
            int end = limit + start;
            if (limit == 0 || (start + limit > orte.size())) {
                end = orte.size();
            }
            orte = orte.subList(start, end);
        }
        for (Ort o : orte) {
            List<Ortszuordnung> zuordnungs = getOrtsZuordnungs(o);
            o.setReferenceCount(zuordnungs.size());
            o.setPlausibleReferenceCount(getPlausibleRefCount(zuordnungs));
            o.setReadonly(
                !authorization.isAuthorized(
                    request,
                    o,
                    RequestMethod.PUT,
                    Ort.class));
            Violation violation = validator.validate(o);
            if (violation.hasErrors() || violation.hasWarnings()) {
                o.setErrors(violation.getErrors());
                o.setWarnings(violation.getWarnings());
            }
        }
        return new Response(true, StatusCodes.OK, orte, size);
    }

    /**
     * Get a single Ort object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/rest/ort/{id}
     *
     * @return Response object containing a single Ort.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(
        @Context HttpServletRequest request,
        @PathParam("id") int id
    ) {
        Ort ort = repository.getByIdPlain(
            Ort.class,
            id,
            Strings.STAMM
        );

        QueryBuilder<Ortszuordnung> builder =
            new QueryBuilder<Ortszuordnung>(
                repository.entityManager(Strings.LAND), Ortszuordnung.class);
        builder.and("ortId", ort.getId());
        List<Ortszuordnung> zuordnungs =
            repository.filterPlain(builder.getQuery(), Strings.LAND);
        ort.setReferenceCount(zuordnungs.size());
        ort.setPlausibleReferenceCount(getPlausibleRefCount(zuordnungs));
        ort.setReadonly(
            !authorization.isAuthorized(
                request,
                ort,
                RequestMethod.PUT,
                Ort.class
            )
        );
        return new Response(true, StatusCodes.OK, ort);
    }

    /**
     * Get multiple Ort object by ids.
     * <p>
     * The ids are send as array in the POST request.
     * <p>
     * Example: http://example.com/rest/ort/getbyids
     *     payload: "[1,2,3]"
     *
     * @return Response object containing multiple Ort objects.
     */
    @POST
    @Path("/getbyids")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getByIds(
        @Context HttpServletRequest request,
        JsonArray ids
    ) {
        QueryBuilder<Ort> builder =
            new QueryBuilder<Ort>(
                repository.entityManager(Strings.STAMM),
                Ort.class
            );
        List<JsonNumber> idList = ids.getValuesAs(JsonNumber.class);
        if (idList.size() > 0) {
            List<Integer> intList = new ArrayList<>();
            for (JsonNumber id : idList) {
                intList.add(id.intValue());
            }

            builder.andIn("id", intList);

            List<Ort> orte =
                repository.filterPlain(builder.getQuery(), Strings.STAMM);
            for (Ort o : orte) {
                List<Ortszuordnung> zuordnungs = getOrtsZuordnungs(o);
                o.setReferenceCount(zuordnungs.size());
                o.setPlausibleReferenceCount(getPlausibleRefCount(zuordnungs));

                o.setReadonly(
                    !authorization.isAuthorized(
                        request,
                        o,
                        RequestMethod.PUT,
                        Ort.class));
            }
            return new Response(true, StatusCodes.OK, orte, orte.size());
        }
        return new Response(true, StatusCodes.OK, null, 0);
    }

    /**
     * Create a Ort object.
     * <p>
     * The new object is embedded in the post data as JSON formatted string.
     * <p>
     * <pre>
     * <code>
     * {
     *  "bezeichnung": [string],
     *  "beschreibung": [string],
     *  "unscharf": [boolean],
     *  "nutsCode": [string],
     *  "koordXExtern": [string],
     *  "koordYExtern": [string],
     *  "hoeheLand": [string],
     *  "longitude": [number],
     *  "latitude": [number],
     *  "staatId": [number],
     *  "verwaltungseinheitId": [string],
     *  "otyp": [string],
     *  "letzteAenderung": [date]
     * }
     * </code>
     * </pre>
     * @return A response object containing the created Ort.
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(
        @Context HttpServletRequest request,
        Ort ort
    ) {
        if (!authorization.isAuthorized(
            request,
            ort,
            RequestMethod.POST,
            Ort.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, ort);
        }

        ort = ortFactory.completeOrt(ort);
        if (ortFactory.hasErrors()) {
            Violation factoryErrs = new Violation();
            for (ReportItem err : ortFactory.getErrors()) {
                factoryErrs.addError(err.getKey(), err.getCode());
            }
            Response response =
                new Response(false, StatusCodes.ERROR_VALIDATION, ort);
            response.setErrors(factoryErrs.getErrors());
            return response;
        }

        Violation violation = validator.validate(ort);
        if (violation.hasErrors()) {
            Response response =
                new Response(false, StatusCodes.ERROR_VALIDATION, ort);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            return response;
        }

        Response response = new Response(true, StatusCodes.OK, ort);
        if (ort.getId() == null) {
            response = repository.create(ort, Strings.STAMM);
        }
        if (violation.hasWarnings()) {
            response.setWarnings(violation.getWarnings());
        }

        return response;
    }

    /**
     * Update an existing Ort object.
     * <p>
     * The object to update should come as JSON formatted string.
     * <pre>
     * <code>
     * {
     *  "id": [number],
     *  "bezeichnung": [string],
     *  "beschreibung": [string],
     *  "unscharf": [boolean],
     *  "nutsCode": [string],
     *  "koordXExtern": [string],
     *  "koordYExtern": [string],
     *  "hoeheLand": [number],
     *  "longitude": [number],
     *  "latitude": [number],
     *  "staatId": [number],
     *  "verwaltungseinheitId": [string],
     *  "otyp": [string],
     *  "letzteAenderung": [date]
     * }
     * </code>
     * </pre>
     *
     * @return Response object containing the updated Ort object.
     */
    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
        @Context HttpServletRequest request,
        @PathParam("id") String id,
        Ort ort
    ) {
        if (!authorization.isAuthorized(
            request,
            ort,
            RequestMethod.PUT,
            Ort.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, ort);
        }

        Ort dbOrt = repository.getByIdPlain(
            Ort.class, ort.getId(), Strings.STAMM);
        if (dbOrt == null) {
            return new Response(false, StatusCodes.NOT_EXISTING, ort);
        }
        String dbCoordX = dbOrt.getKoordXExtern();
        String dbCoordY = dbOrt.getKoordYExtern();

        if (getPlausibleRefCount(getOrtsZuordnungs(dbOrt)) > 0
                && (!dbCoordX.equals(ort.getKoordXExtern())
                || !dbCoordY.equals(ort.getKoordYExtern()))) {
            MultivaluedMap<String, Integer> error =
                new MultivaluedHashMap<String, Integer>();
            if (!dbCoordX.equals(ort.getKoordXExtern())) {
                error.add("koordXExtern", StatusCodes.GEO_UNCHANGEABLE_COORD);
            }
            if (!dbCoordY.equals(ort.getKoordYExtern())) {
                error.add("koordYExtern", StatusCodes.GEO_UNCHANGEABLE_COORD);
            }
            Response response =
                new Response(false, StatusCodes.ERROR_VALIDATION, ort);
            response.setErrors(error);
            return response;
        }

        ortFactory.transformCoordinates(ort);
        if (ortFactory.hasErrors()) {
            Violation factoryErrs = new Violation();
            for (ReportItem err : ortFactory.getErrors()) {
                factoryErrs.addError(err.getKey(), err.getCode());
            }
            Response response =
                new Response(false, StatusCodes.ERROR_VALIDATION, ort);
            response.setErrors(factoryErrs.getErrors());
            return response;
        }

        Violation violation = validator.validate(ort);
        if (violation.hasErrors()) {
            Response response =
                new Response(false, StatusCodes.ERROR_VALIDATION, ort);
            response.setErrors(violation.getErrors());
            response.setWarnings(violation.getWarnings());
            return response;
        }

        Response response = repository.update(ort, Strings.STAMM);
        if (violation.hasWarnings()) {
            response.setWarnings(violation.getWarnings());
        }

        return response;
    }

    /**
     * Delete an existing Ort object by id.
     * <p>
     * The id is appended to the URL as a path parameter.
     * <p>
     * Example: http://example.com/rest/ort/{id}
     *
     * @return Response object.
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
        @Context HttpServletRequest request,
        @PathParam("id") String id
    ) {
        Response response =
            repository.getById(Ort.class, Integer.valueOf(id), Strings.STAMM);
        if (!response.getSuccess()) {
            return response;
        }
        Ort ort = (Ort) response.getData();
        if (getOrtsZuordnungs(ort).size() > 0) {
            return new Response(false, StatusCodes.ERROR_DELETE, ort);
        }
        if (!authorization.isAuthorized(
            request,
            ort,
            RequestMethod.DELETE,
            Ort.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, ort);
        }

        return repository.delete(ort, Strings.STAMM);
    }

    /**
     * Return the Ortszuordnung instances referencing the given ort
     * @param o Ort instance
     * @return Ortszuordnung instances as list
     */
    public List<Ortszuordnung> getOrtsZuordnungs(Ort o) {
        QueryBuilder<Ortszuordnung> refBuilder =
            new QueryBuilder<Ortszuordnung>(
                repository.entityManager(Strings.LAND), Ortszuordnung.class);
        refBuilder.and("ortId", o.getId());
        List<Ortszuordnung> zuordnungs =
            repository.filterPlain(refBuilder.getQuery(), Strings.LAND);
        return zuordnungs;
    }

    /**
     * Get the number of plausible Messung objects referencing an ort.
     * @param zuordnungs List of Ortszuordnung objects referencing
     *                   the ort to check
     * @return Number of references as int
     */
    public int getPlausibleRefCount(List<Ortszuordnung> zuordnungs) {
        Map<Integer, Integer> plausibleMap = new HashMap<Integer, Integer>();
        for (Ortszuordnung zuordnung: zuordnungs) {
            EntityManager em = repository.entityManager(Strings.LAND);

            CriteriaBuilder mesBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Messung> criteriaQuery =
                mesBuilder.createQuery(Messung.class);
            Root<Messung> root = criteriaQuery.from(Messung.class);
            Join<Messung, StatusProtokoll> join =
                root.join("statusProtokoll", JoinType.LEFT);
            Predicate filter =
                mesBuilder.equal(root.get("probeId"), zuordnung.getProbeId());
            filter = mesBuilder
                .and(filter, join.get("statusKombi")
                .in(Arrays.asList("2", "6", "10")));
            criteriaQuery.where(filter);
            List<Messung> messungs =
                repository.filterPlain(criteriaQuery, Strings.LAND);
            if (messungs.size() > 0) {
                plausibleMap.put(zuordnung.getProbeId(), 1);
            }
        }
        return plausibleMap.size();
    }
}
