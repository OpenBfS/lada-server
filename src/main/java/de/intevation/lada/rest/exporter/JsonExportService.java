/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest.exporter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.io.IOUtils;

import de.intevation.lada.exporter.ExportConfig;
import de.intevation.lada.exporter.ExportFormat;
import de.intevation.lada.exporter.Exporter;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
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
@Path("data/export")
public class JsonExportService extends LadaService {

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
     * The service takes JSON formatted  POST data containing probe ids and
     * exports the Sample objects filtered by these ids.
     *
     * @param proben    JSON formatted string with an array of probe ids.
     * @return The JSON to export.
     */
    @POST
    @Path("/json")
    public String download(
        JsonObject proben
    ) {
        JsonArray array = proben.getJsonArray("proben");
        List<Integer> probeIds = new ArrayList<Integer>();
        UserInfo userInfo = authorization.getInfo();
        for (int i = 0; i < array.size(); i++) {
            Integer probeId = array.getInt(i);
            probeIds.add(probeId);
        }
        InputStream exported =
            exporter.exportProben(
                probeIds,
                new ArrayList<Integer>(),
                StandardCharsets.UTF_8,
                userInfo);
        if (exported == null) {
            return new Response(
                false, StatusCodes.NOT_EXISTING, null).toString();
        }
        try {
            return IOUtils.toString(exported, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return new Response(
                false, StatusCodes.NOT_EXISTING, null).toString();
        }
    }
}
