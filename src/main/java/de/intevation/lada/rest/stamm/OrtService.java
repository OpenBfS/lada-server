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

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.Pattern;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.QueryParam;

import org.jboss.logging.Logger;

import de.intevation.lada.factory.OrtFactory;
import de.intevation.lada.importer.ReportItem;
import de.intevation.lada.model.land.Measm;
import de.intevation.lada.model.land.Ortszuordnung;
import de.intevation.lada.model.land.OrtszuordnungMp;
import de.intevation.lada.model.land.StatusProtokoll;
import de.intevation.lada.model.master.AdminUnit;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationConfig;
import de.intevation.lada.rest.LadaService;

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
 *      "aktiv": [boolean],
 *      "berichtstext": [string],
 *      "gemId": [string],
 *      "gemUntId": [string],
 *      "hoeheLand": [number],
 *      "hoeheUeberNn": [number],
 *      "kdaId": [number],
 *      "koordXExtern": [string],
 *      "koordYExtern": [string],
 *      "ktaGruppeId": [number],
 *      "kurztext": [string],
 *      "langtext": [string],
 *      "latitude": [number],
 *      "letzteAenderung": [timestamp],
 *      "longitude": [number],
 *      "mpArt": [string],
 *      "netzbetreiberId": [string],
 *      "ortId": [string],
 *      "ortTyp": [number],
 *      "ozId": [string],
 *      "plausibleReferenceCount": [number],
 *      "readonly": [boolean],
 *      "referenceCount": [number],
 *      "referenceCountMp": [number],
 *      "sektor": [string],
 *      "staatId": [number],
 *      "unscharf": [boolean],
 *      "zone": [string],
 *      "zustaendigkeit": [string]
 *  }],
 *  "errors": [object],
 *  "notifications": [object],
 *  "readonly": [boolean],
 *  "totalCount": [number],
 *  "warnings": [object]
 * }
 * </code>
 * </pre>
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("rest/ort")
public class OrtService extends LadaService {

    @Inject
    private Logger logger;

    /**
     * The data repository granting read/write access.
     */
    @Inject
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
     *
     * @param netzbetreiberId URL parameter to filter using Netzbetreiber.
     * Might be null (i.e. not given at all) but not an empty string.
     * @param search URL parameter to filter using given pattern. Might be null
     * (i.e. not given at all) but not an empty string.
     * @param start URL parameter used as offset for paging
     * @param limit URL parameter used as limit for paging
     * @return Response object containing all (filtered) Ort objects.
     */
    @GET
    @Path("/")
    public Response get(
        @QueryParam("netzbetreiberId")
        @Pattern(regexp = ".+") String netzbetreiberId,
        @QueryParam("search") @Pattern(regexp = ".+") String search,
        @QueryParam("start") Integer start,
        @QueryParam("limit") Integer limit
    ) {
        List<Site> orte = new ArrayList<>();
        UserInfo user = authorization.getInfo();
        EntityManager em = repository.entityManager();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Site> query = builder.createQuery(Site.class);
        Root<Site> root = query.from(Site.class);
        Predicate filter = null;
        if (netzbetreiberId != null) {
            Predicate netzbetreiberFilter =
                builder.equal(root.get("networkId"), netzbetreiberId);
            filter = builder.and(netzbetreiberFilter);
        } else {
            for (String nb : user.getNetzbetreiber()) {
                builder.or(builder.equal(root.get("networkId"), nb));
            }
        }
        if (search != null) {
            Join<Site, AdminUnit> join =
                root.join("munic", JoinType.LEFT);
            String pattern = "%" + search + "%";
            Predicate idFilter = builder.like(root.get("extId"), pattern);
            Predicate kurzTextFilter =
                builder.like(root.get("shortText"), pattern);
            Predicate langtextFilter =
                builder.like(root.get("longText"), pattern);
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
        orte = repository.filterPlain(query);

        int size = orte.size();
        // TODO: Push paging down to database
        if (start != null && limit != null) {
            int end = limit + start;
            if (limit.intValue() == 0 || end > size) {
                end = size;
            }
            orte = orte.subList(start, end);
        }
        for (Site o : orte) {
            List<Ortszuordnung> zuordnungs = getOrtsZuordnungs(o);
            o.setReferenceCount(zuordnungs.size());
            o.setPlausibleReferenceCount(getPlausibleRefCount(zuordnungs));
            List<OrtszuordnungMp> zuordnungsMp = getOrtsZuordnungsMp(o);
            o.setReferenceCountMp(zuordnungsMp.size());
            o.setReadonly(
                !authorization.isAuthorized(
                    o,
                    RequestMethod.PUT,
                    Site.class));
            Violation violation = validator.validate(o);
            if (violation.hasErrors() || violation.hasWarnings()) {
                o.setErrors(violation.getErrors());
                o.setWarnings(violation.getWarnings());
                o.setNotifications(violation.getNotifications());
            }
        }
        return new Response(true, StatusCodes.OK, orte, size);
    }

    /**
     * Get a single Ort object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single Ort.
     */
    @GET
    @Path("/{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        Site ort = repository.getByIdPlain(Site.class, id);
        if (ort == null) {
            return new Response(false, StatusCodes.NOT_EXISTING, null);
        }
        List<Ortszuordnung> zuordnungs = getOrtsZuordnungs(ort);
        ort.setReferenceCount(zuordnungs.size());
        ort.setPlausibleReferenceCount(getPlausibleRefCount(zuordnungs));
        List<OrtszuordnungMp> zuordnungsMp = getOrtsZuordnungsMp(ort);
        ort.setReferenceCountMp(zuordnungsMp.size());
        ort.setReadonly(
            !authorization.isAuthorized(
                ort,
                RequestMethod.PUT,
                Site.class
            )
        );
        Violation violation = validator.validate(ort);
            if (violation.hasErrors() || violation.hasWarnings()) {
                ort.setErrors(violation.getErrors());
                ort.setWarnings(violation.getWarnings());
                ort.setNotifications(violation.getNotifications());
            }
        return new Response(true, StatusCodes.OK, ort);
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
    public Response create(
        Site ort
    ) {
        if (!authorization.isAuthorized(
            ort,
            RequestMethod.POST,
            Site.class)
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
            response.setNotifications(violation.getNotifications());
            return response;
        }

        Response response = new Response(true, StatusCodes.OK, ort);
        if (ort.getId() == null) {
            response = repository.create(ort);
        }
        if (violation.hasWarnings()) {
            response.setWarnings(violation.getWarnings());
            response.setNotifications(violation.getNotifications());
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
    public Response update(
        @PathParam("id") Integer id,
        Site ort
    ) {
        if (!authorization.isAuthorized(
            ort,
            RequestMethod.PUT,
            Site.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, ort);
        }

        Site dbOrt = repository.getByIdPlain(
            Site.class, ort.getId());
        if (dbOrt == null) {
            return new Response(false, StatusCodes.NOT_EXISTING, ort);
        }
        String dbCoordX = dbOrt.getCoordXExt();
        String dbCoordY = dbOrt.getCoordYExt();

        if (getPlausibleRefCount(getOrtsZuordnungs(dbOrt)) > 0
                && (!dbCoordX.equals(ort.getCoordXExt())
                || !dbCoordY.equals(ort.getCoordYExt()))) {
            MultivaluedMap<String, Integer> error =
                new MultivaluedHashMap<String, Integer>();
            if (!dbCoordX.equals(ort.getCoordXExt())) {
                error.add("koordXExtern", StatusCodes.GEO_UNCHANGEABLE_COORD);
            }
            if (!dbCoordY.equals(ort.getCoordYExt())) {
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
            response.setNotifications(violation.getNotifications());
            return response;
        }

        Response response = repository.update(ort);
        if (violation.hasWarnings()) {
            response.setWarnings(violation.getWarnings());
            response.setNotifications(violation.getNotifications());
        }

        return response;
    }

    /**
     * Delete an existing Ort object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        Response response = repository.getById(Site.class, id);
        if (!response.getSuccess()) {
            return response;
        }
        Site ort = (Site) response.getData();
        if (getOrtsZuordnungs(ort).size() > 0
                || getOrtsZuordnungsMp(ort).size() > 0) {
            return new Response(false, StatusCodes.ERROR_DELETE, ort);
        }
        if (!authorization.isAuthorized(
            ort,
            RequestMethod.DELETE,
            Site.class)
        ) {
            return new Response(false, StatusCodes.NOT_ALLOWED, ort);
        }

        return repository.delete(ort);
    }

    /**
     * Return the Ortszuordnung instances referencing the given ort.
     * @param o Ort instance
     * @return Ortszuordnung instances as list
     */
    private List<Ortszuordnung> getOrtsZuordnungs(Site o) {
        QueryBuilder<Ortszuordnung> refBuilder =
            repository.queryBuilder(Ortszuordnung.class);
        refBuilder.and("siteId", o.getId());
        return repository.filterPlain(refBuilder.getQuery());
    }

    /**
     * Get the number of plausible Messung objects referencing an ort.
     * @param zuordnungs List of Ortszuordnung objects referencing
     *                   the ort to check
     * @return Number of references as int
     */
    private int getPlausibleRefCount(List<Ortszuordnung> zuordnungs) {
        Map<Integer, Integer> plausibleMap = new HashMap<Integer, Integer>();
        for (Ortszuordnung zuordnung: zuordnungs) {
            EntityManager em = repository.entityManager();

            CriteriaBuilder mesBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Measm> criteriaQuery =
                mesBuilder.createQuery(Measm.class);
            Root<Measm> root = criteriaQuery.from(Measm.class);
            Join<Measm, StatusProtokoll> join =
                root.join("statusProtocol", JoinType.LEFT);
            Predicate filter =
                mesBuilder.equal(root.get("sampleId"), zuordnung.getSampleId());
            filter = mesBuilder
                .and(filter, join.get("statusKombi")
                .in(Arrays.asList("2", "6", "10")));
            criteriaQuery.where(filter);
            List<Measm> messungs =
                repository.filterPlain(criteriaQuery);
            if (messungs.size() > 0) {
                plausibleMap.put(zuordnung.getSampleId(), 1);
            }
        }
        return plausibleMap.size();
    }

    /**
     * Return the OrtszuordnungMp instances referencing the given ort.
     * @param o Ort instance
     * @return Ortszuordnung instances as list
     */
    private List<OrtszuordnungMp> getOrtsZuordnungsMp(Site o) {
        QueryBuilder<OrtszuordnungMp> refBuilder =
            repository.queryBuilder(OrtszuordnungMp.class);
        refBuilder.and("ortId", o.getId());
        return repository.filterPlain(refBuilder.getQuery());
    }
}
