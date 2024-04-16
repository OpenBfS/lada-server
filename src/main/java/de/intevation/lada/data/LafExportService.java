/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;

import de.intevation.lada.exporter.ExportConfig;
import de.intevation.lada.exporter.ExportFormat;
import de.intevation.lada.exporter.Exporter;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.rest.LadaService;

/**
 * REST service to export probe objects and the child objects associated with
 * the selected Sample objects.
 * <p>
 * To request objects post a JSON formatted string with an array of probe ids.
 * <pre>
 * <code>
 * {
 *  "proben": [[number], [number], ...]
 * }
 * </code>
 * </pre>
 *
 * @author <a href = "mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("export")
public class LafExportService extends LadaService {

    @Inject
    private Repository repository;

    /**
     * The exporter.
     */
    @Inject
    @ExportConfig(format = ExportFormat.LAF)
    private Exporter exporter;

    /**
     * The authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;


    /**
     * Export Sample objects.
     *
     * The service takes JSON formatted  POST data containing probe ids and
     * exports the Sample objects filtered by these ids.
     *
     * @param objects    JSON formatted string with an array of probe ids.
     * @return The LAF file to export.
     */
    @POST
    @Path("laf")
    @Produces("application/octet-stream")
    public Response download(
        JsonObject objects,
        @Context HttpServletRequest request
    ) {
        List<Integer> probeIds = new ArrayList<Integer>();
        List<Integer> messungIds = new ArrayList<Integer>();
        if (objects.getJsonArray("proben") != null) {
            for (JsonValue id : objects.getJsonArray("proben")) {
                if (id instanceof JsonNumber) {
                    probeIds.add(((JsonNumber) id).intValue());
                }
            }
        }
        if (objects.getJsonArray("messungen") != null) {
            for (JsonValue id : objects.getJsonArray("messungen")) {
                if (id instanceof JsonNumber) {
                    messungIds.add(((JsonNumber) id).intValue());
                }
            }
        }

        List<Integer> pIds = new ArrayList<Integer>();
        if (!probeIds.isEmpty()) {
            QueryBuilder<Sample> pBuilder =
                repository.queryBuilder(Sample.class);
            pBuilder.andIn("id", probeIds);
            List<Sample> pObjects = repository.filter(
                pBuilder.getQuery());
            for (Sample p : pObjects) {
                pIds.add(p.getId());
            }
        }

        List<Integer> mIds = new ArrayList<Integer>();
        if (!messungIds.isEmpty()) {
            QueryBuilder<Measm> mBuilder =
                repository.queryBuilder(Measm.class);
            mBuilder.andIn("id", messungIds);
            List<Measm> mObjects = repository.filter(
                mBuilder.getQuery());
            for (Measm m : mObjects) {
                mIds.add(m.getId());
            }
        }

        if (pIds.isEmpty() && mIds.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String encoding = request.getHeader("X-FILE-ENCODING");
        if (encoding == null || encoding.equals("")) {
            encoding = "iso-8859-15";
        }
        Charset charset;
        try {
            charset = Charset.forName(encoding);
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            return Response.status(Status.BAD_REQUEST)
                .entity((Object) "Invalid or unknown encoding requested")
                .type(MediaType.TEXT_PLAIN)
                .build();
        }

        UserInfo userInfo = authorization.getInfo();
        InputStream exported =
            exporter.exportProben(pIds, mIds, charset, userInfo);

        ResponseBuilder response = Response.ok((Object) exported);
        response.header(
            "Content-Disposition",
            "attachment; filename = \"export.laf\"");
        response.encoding(encoding);
        response.header(
            "Content-Type", "application/octet-stream; charset = " + encoding);
        return response.build();
    }
}
