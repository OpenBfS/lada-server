/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import de.intevation.lada.data.requests.QueryExportParameters;
import de.intevation.lada.exporter.Exporter;
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

    /**
     * The exporter.
     */
    @Inject
    private Exporter<QueryExportParameters> exporter;

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
    public InputStream downloadSamples(
        @NotEmpty List<Integer> ids
    ) {
        return exporter.exportProben(
            ids,
            new ArrayList<Integer>(),
            StandardCharsets.UTF_8,
            authorization.getInfo());
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
    public InputStream downloadMeasms(
        @NotEmpty List<Integer> ids
    ) {
        return exporter.exportMessungen(
            new ArrayList<Integer>(),
            ids,
            StandardCharsets.UTF_8,
            authorization.getInfo());
    }
}
