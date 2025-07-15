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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.BadRequestException;
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

import de.intevation.lada.factory.OrtFactory;
import de.intevation.lada.model.master.Names;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.validation.Validator;


/**
 * REST service for Site objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "site")
public class SiteService extends LadaIntegerIdEntityService {

    private static final String IMG_PATH = "img";
    private static final String MAP_PATH = "map";

    private static final String PATH_PATTERN = IMG_PATH + "|" + MAP_PATH;

    private static final Map<String, String> UPDATE_QUERIES = Map.of(
        IMG_PATH, Names.QUERY_UPDATE_SITE_IMG,
        MAP_PATH, Names.QUERY_UPDATE_SITE_MAP);

    @Inject
    private OrtFactory ortFactory;

    public static class Response {
        private Collection<Site> data;
        private long totalCount;

        /**
         * Default constructor for JSON-B.
         */
        public Response() { };

        private Response(Collection<Site> data, long totalCount) {
            this.data = data;
            this.totalCount = totalCount;
        }

        public Collection<Site> getData() {
            return this.data;
        }

        public void setData(Collection<Site> data) {
            this.data = data;
        }

        public long getTotalCount() {
            return this.totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }
    }

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
            whereClauseParts.add("networkId in(:networkId)");
        }
        if (search != null) {
            List<String> filters = new ArrayList<>();
            for (String attr: List.of(
                    "extId", "shortText", "longText", "adminUnit.name")) {
                filters.add(attr + " LIKE(:pattern)");
            }
            whereClauseParts.add(String.join(" OR ", filters));
        }
        String whereClause = "";
        if (!whereClauseParts.isEmpty()) {
            whereClause =
                "WHERE (" + String.join(") AND (", whereClauseParts) + ")";
        }

        // Build queries
        TypedQuery<Site> siteQuery = repository.entityManager().createQuery(
            String.format("select s from Site s %s", whereClause),
            Site.class);
        TypedQuery<Long> countQuery = repository.entityManager().createQuery(
            String.format("select count(s) from Site s %s", whereClause),
            Long.class);
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

        Collection<Site> orte =
            new Validator().validate(siteQuery.getResultList());

        return new Response(authorization.filter(orte),
            countQuery.getSingleResult());
    }

    /**
     * Get a single Site object by id.
     *
     * @return a single Site.
     */
    @GET
    @Path("{id}")
    public Site getById() {
        return repository.getById(Site.class, id);
    }

    /**
     * Create a Site object.
     *
     * @return A response object containing the created Site.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @POST
    public Site create(
        @Valid Site ort
    ) throws BadRequestException {
        Site existing = ortFactory.findExistingSite(ort);
        if (existing != null) {
            ort = existing;
        } else {
            ortFactory.completeSite(ort);
        }

        if (ort.getId() == null) {
            repository.create(ort);
        }
        return ort;
    }

    /**
     * Update an existing Site object.
     *
     * @return The updated Site object.
     * @throws BadRequestException if any constraint violations are detected.
     */
    @PUT
    @Path("{id}")
    public Site update(
        @Valid Site ort
    ) throws BadRequestException {
        ortFactory.completeSite(ort);

        return repository.update(ort);
    }

    /**
     * Delete an existing Site object by id.
     */
    @DELETE
    @Path("{id}")
    public void delete() {
        Site ort = repository.getById(Site.class, id);
        authorization.authorize(ort, RequestMethod.DELETE);
        repository.delete(ort);
    }

    /**
     * Retrieve image associated with site.
     *
     * @param type Type of image (img or map)
     * @return Image for given site of given type
     * @throws NotFoundException if no site exists with given ID
     */
    @GET
    @Path("{id}/{type}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] getSiteImage(
        @PathParam("type") @Pattern(regexp = PATH_PATTERN) String type
    ) {
        Site site = repository.getById(Site.class, id);
        return type.equals("map") ? site.getMap() : site.getImg();
    }

    /**
     * Associate image with site.
     *
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
        @PathParam("type") @Pattern(regexp = PATH_PATTERN) String type,
        @Context HttpServletRequest request,
        @NotBlank String dataUrl
    ) throws IOException {
        Site site = repository.getById(Site.class, id);
        authorization.authorize(site, RequestMethod.PUT);
        int contentLength = request.getContentLength();
        if (contentLength == -1) {
            throw new IOException();
        }
        String encodingPrefix = "base64,";
        int contentStartIndex = dataUrl.indexOf(encodingPrefix)
                + encodingPrefix.length();

        byte[] img = Base64.getDecoder().decode(
                dataUrl.substring(contentStartIndex));
        updateDataColumn(site.getId(), img, type);
    }

    private void updateDataColumn(Integer id, byte[] img, String type) {
        repository.entityManager()
            .createNamedQuery(UPDATE_QUERIES.get(type))
            .setParameter("data", img)
            .setParameter("siteId", id)
            .executeUpdate();
    }

    /**
     * Delete image associated with site.
     *
     * @param type Type of image (img or map)
     * @throws ForbiddenException if updating the site is not allowed
     */
    @DELETE
    @Path("{id}/{type}")
    public void deleteSiteImage(
            @PathParam("type") @Pattern(regexp = "img|map") String type
    ) {
        Site site = repository.getById(Site.class, id);
        authorization.authorize(site, RequestMethod.PUT);
        updateDataColumn(site.getId(), null, type);
    }
}
