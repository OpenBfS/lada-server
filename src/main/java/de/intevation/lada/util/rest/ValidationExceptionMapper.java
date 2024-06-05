/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.rest;

import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.resteasy.api.validation.ViolationReport;


/**
 * Just a wrapper to add OpenAPI description.
 */
@Provider
public class ValidationExceptionMapper
    implements ExceptionMapper<BadRequestException> {

    @Override
    @APIResponse(
        responseCode = "400",
        description = "Validation constraint violation",
        content = @Content(
            schema = @Schema(implementation = ViolationReport.class)))
    public Response toResponse(BadRequestException e) {
        return e.getResponse();
    }
}
