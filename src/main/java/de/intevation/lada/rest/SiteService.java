/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.Query;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.QueryParam;

import org.jboss.logging.Logger;

import de.intevation.lada.factory.OrtFactory;
import de.intevation.lada.importer.ReportItem;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.Violation;

/**
 * REST service for Site objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("site")
public class SiteService extends LadaService {

    private static final String QUERY_TEMPLATE =
        "SELECT site.* FROM master.site "
        + "LEFT JOIN master.admin_unit AS au ON admin_unit_id = au.id "
        + "%s";

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
    private Validator<Site> validator;

    /**
     * Get Site objects.
     *
     * @param networkId URL parameter to filter using Network.
     * Might be null (i.e. not given at all) but not an empty string.
     * @param search URL parameter to filter using given pattern. Might be null
     * (i.e. not given at all) but not an empty string.
     * @param start URL parameter used as offset for paging
     * @param limit URL parameter used as limit for paging
     * @return Response object containing all (filtered) Site objects.
     */
    @GET
    public Response get(
        @QueryParam("networkId")
        @Pattern(regexp = ".+") String networkId,
        @QueryParam("search") @Pattern(regexp = ".+") String search,
        @QueryParam("start") Integer start,
        @QueryParam("limit") Integer limit
    ) {
        // Build SQL query string
        List<String> whereClauseParts = new ArrayList<>();
        if (networkId != null) {
            whereClauseParts.add("network_id IN(:networkId)");
        }
        if (search != null) {
            List<String> filters = new ArrayList<>();
            for (String attr: List.of(
                    "ext_id", "short_text", "long_text", "name")) {
                filters.add(attr + " LIKE(:pattern)");
            }
            whereClauseParts.add(String.join(" OR ", filters));
        }
        String whereClause = "";
        if (!whereClauseParts.isEmpty()) {
            whereClause =
                "WHERE (" + String.join(") AND (", whereClauseParts) + ")";
        }
        String queryString = String.format(QUERY_TEMPLATE, whereClause);

        // Build queries
        Query siteQuery = repository.entityManager().createNativeQuery(
            queryString, Site.class);
        Query countQuery = repository.entityManager().createNativeQuery(
            "SELECT count(*) FROM (" + queryString + ") as query");
        List<Query> queries = List.of(siteQuery, countQuery);
        if (networkId != null) {
            for (Query query: queries) {
                query.setParameter("networkId", networkId);
            }
        }
        if (search != null) {
            for (Query query: queries) {
                query.setParameter("pattern", "%" + search + "%");
            }
        }
        if (start != null) {
            siteQuery.setFirstResult(start);
        }
        if (limit != null) {
            siteQuery.setMaxResults(limit);
        }

        @SuppressWarnings("unchecked")
        List<Site> orte = siteQuery.getResultList();
        for (Site o : orte) {
            o.setReadonly(
                !authorization.isAuthorized(
                    o,
                    RequestMethod.PUT,
                    Site.class));
            validator.validate(o);
        }

        int size = Math.toIntExact((Long) countQuery.getSingleResult());

        return new Response(true, StatusCodes.OK, orte, size);
    }

    /**
     * Get a single Site object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object containing a single Site.
     */
    @GET
    @Path("{id}")
    public Response getById(
        @PathParam("id") Integer id
    ) {
        Response response = repository.getById(Site.class, id);
        Site ort = (Site) response.getData();
        if (ort == null) {
            return new Response(false, StatusCodes.NOT_EXISTING, null);
        }
        ort.setReadonly(
            !authorization.isAuthorized(
                ort,
                RequestMethod.PUT,
                Site.class
            )
        );
        validator.validate(ort);
        return authorization.filter(response, Site.class);
    }

    /**
     * Create a Site object.
     *
     * @return A response object containing the created Site.
     */
    @POST
    public Response create(
        @Valid Site ort
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
            ort.setErrors(factoryErrs.getErrors());
            return new Response(false, StatusCodes.ERROR_VALIDATION, ort);
        }

        validator.validate(ort);
        if (ort.hasErrors()) {
            return new Response(false, StatusCodes.ERROR_VALIDATION, ort);
        }

        Response response = new Response(true, StatusCodes.OK, ort);
        if (ort.getId() == null) {
            response = repository.create(ort);
        }
        return response;
    }

    /**
     * Update an existing Site object.
     *
     * @return Response object containing the updated Site object.
     */
    @PUT
    @Path("{id}")
    public Response update(
        @PathParam("id") Integer id,
        @Valid Site ort
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

        ortFactory.transformCoordinates(ort);
        if (ortFactory.hasErrors()) {
            Violation factoryErrs = new Violation();
            for (ReportItem err : ortFactory.getErrors()) {
                factoryErrs.addError(err.getKey(), err.getCode());
            }
            ort.setErrors(factoryErrs.getErrors());
            return new Response(false, StatusCodes.ERROR_VALIDATION, ort);
        }

        validator.validate(ort);
        if (ort.hasErrors()) {
            return new Response(false, StatusCodes.ERROR_VALIDATION, ort);
        }

        Response response = repository.update(ort);
        validator.validate(response.getData());
        return response;
    }

    /**
     * Delete an existing Site object by id.
     *
     * @param id The id is appended to the URL as a path parameter.
     * @return Response object.
     */
    @DELETE
    @Path("{id}")
    public Response delete(
        @PathParam("id") Integer id
    ) {
        Response response = repository.getById(Site.class, id);
        if (!response.getSuccess()) {
            return response;
        }
        Site ort = (Site) response.getData();
        if (ort.getReferenceCount() > 0
                || ort.getReferenceCountMp() > 0) {
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
     * Retrieve image associated with site.
     *
     * @param id ID of site
     * @param type Type of image (img or map)
     * @return Image for given site of given type
     * @throws NotFoundException if no site exists with given ID
     */
    @GET
    @Path("{id}/{type}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] getSiteImage(
            @PathParam("id") Integer id,
            @PathParam("type") @Pattern(regexp = "img|map") String type
    ) {
        Site site = repository.getByIdPlain(Site.class, id);
        if (site == null) {
            throw new NotFoundException();
        }
        return type.equals("map") ? site.getMap() : site.getImg();
    }

    /**
     * Associate image with site.
     *
     * @param id ID of site
     * @param type Type of image (img or map)
     * @param request The upload request
     * @throws ForbiddenException if updating the site is not allowed
     * @throws IOException if length of request body cannot be retrieved
     * or other I/O errors occur.
     */
    @POST
    @Path("{id}/{type}")
    @Consumes(MediaType.TEXT_PLAIN)
    public void uploadSiteImage(
            @PathParam("id") Integer id,
            @PathParam("type") @Pattern(regexp = "img|map") String type,
            @Context HttpServletRequest request,
            @NotBlank String dataUrl
    ) throws IOException {
        Site site = repository.getByIdPlain(Site.class, id);
        if (!authorization.isAuthorized(
                site,
                RequestMethod.PUT,
                Site.class)) {
            throw new ForbiddenException();
        }
        int contentLength = request.getContentLength();
        if (contentLength == -1) {
            throw new IOException();
        }
        String encodingPrefix = "base64,";
        int contentStartIndex = dataUrl.indexOf(encodingPrefix)
                + encodingPrefix.length();

        byte[] img = Base64.getDecoder().decode(
                dataUrl.substring(contentStartIndex));
        if (type.equals("map")) {
            site.setMap(img);
        } else {
            site.setImg(img);
        }
        repository.update(site);
    }

    /**
     * Delete image associated with site.
     *
     * @param id ID of site
     * @param type Type of image (img or map)
     * @throws ForbiddenException if updating the site is not allowed
     */
    @DELETE
    @Path("{id}/{type}")
    public void deleteSiteImage(
            @PathParam("id") Integer id,
            @PathParam("type") @Pattern(regexp = "img|map") String type
    ) {
        Site site = repository.getByIdPlain(Site.class, id);
        if (!authorization.isAuthorized(
                site,
                RequestMethod.PUT,
                Site.class)) {
            throw new ForbiddenException();
        }
        if (type.equals("map")) {
            site.setMap(null);
        } else {
            site.setImg(null);
        }
        repository.update(site);
    }
}
