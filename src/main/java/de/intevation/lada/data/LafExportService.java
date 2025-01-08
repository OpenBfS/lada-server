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
import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import de.intevation.lada.data.requests.LafExportParameters;
import de.intevation.lada.exporter.ExportConfig;
import de.intevation.lada.exporter.ExportFormat;
import de.intevation.lada.exporter.Exporter;
import de.intevation.lada.util.auth.UserInfo;
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
@Path(LadaService.PATH_DATA + "export")
public class LafExportService extends LadaService {

    @Inject
    private Repository repository;

    /**
     * The exporter.
     */
    @Inject
    @ExportConfig(format = ExportFormat.LAF)
    private Exporter<LafExportParameters> exporter;

    /**
     * Export objects as LAF 8.
     *
     * @param objects LafExportParameters
     * @return The LAF file to export.
     * @throws BadRequestException if any constraint violations are detected
     */
    @POST
    @Path("laf")
    @Produces("application/octet-stream")
    public Response download(
        @Valid LafExportParameters objects
    ) throws BadRequestException {
        List<Integer> pIds = objects.getProben();
        List<Integer> mIds = objects.getMessungen();

        Charset charset = objects.getEncoding();

        UserInfo userInfo = authorization.getInfo();
        InputStream exported =
            exporter.exportProben(pIds, mIds, charset, userInfo);

        ResponseBuilder response = Response.ok((Object) exported);
        response.header(
            "Content-Disposition",
            "attachment; filename = \"export.laf\"");
        response.encoding(charset.name());
        response.header(
            "Content-Type",
            "application/octet-stream; charset = " + charset.name());
        return response.build();
    }
}
