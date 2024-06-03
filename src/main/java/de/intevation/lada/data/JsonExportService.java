/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.apache.commons.io.IOUtils;

import de.intevation.lada.exporter.ExportConfig;
import de.intevation.lada.exporter.ExportFormat;
import de.intevation.lada.exporter.Exporter;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
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
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_DATA + "export/json")
public class JsonExportService extends LadaService {

    private static final String ERROR_MSG = "Failed exporting JSON data";

    /**
     * The exporter.
     */
    @Inject
    @ExportConfig(format = ExportFormat.JSON)
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
     * The service takes JSON formatted POST data containing sample IDs and
     * exports the Sample objects filtered by these ids.
     *
     * @param ids List of sample IDs.
     * @return The JSON to export.
     */
    @POST
    @Path("samples")
    public String downloadSamples(
        @NotEmpty List<Integer> ids
    ) {
        return createResultString(exporter.exportProben(
                ids,
                new ArrayList<Integer>(),
                StandardCharsets.UTF_8,
                authorization.getInfo()));
    }

    /**
     * Export Measm objects.
     *
     * The service takes JSON formatted POST data containing Measm IDs and
     * exports the Measm objects filtered by these ids.
     *
     * @param ids List of Measm IDs.
     * @return The JSON to export.
     */
    @POST
    @Path("measms")
    public String downloadMeasms(
        @NotEmpty List<Integer> ids
    ) {
        return createResultString(exporter.exportMessungen(
                new ArrayList<Integer>(),
                ids,
                StandardCharsets.UTF_8,
                authorization.getInfo()));
    }

    private String createResultString(InputStream exported) {
        if (exported == null) {
            throw new InternalServerErrorException(
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ERROR_MSG).build());
        }
        try {
            return IOUtils.toString(exported, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new InternalServerErrorException(
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ERROR_MSG).build(),
                e);
        }
    }
}
