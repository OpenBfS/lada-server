/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.rest;

import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Mapper for Jsonb Exceptions during request processing.
 *
 * JsonbException during request processing will be mapped to a bad request
 * response.
 */
@Provider
public class JsonbExceptionMapper
        implements ExceptionMapper<ProcessingException> {
    @Override
    public Response toResponse(ProcessingException exception) {
        if (exception.getCause() instanceof JsonbException) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        throw exception;
    }
}
