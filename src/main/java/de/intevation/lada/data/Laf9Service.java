/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.POST;

import org.eclipse.microprofile.openapi.annotations.Operation;

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.rest.LadaService;
import de.intevation.lada.util.data.Repository;


@Path(LadaService.PATH_DATA + "laf9")
public class Laf9Service extends LadaService {

    @Inject
    private Repository repository;

    /**
     * @param sample sample to create
     * @return created sample
     * @throws BadRequestException if any constraint violations are detected.
     */
    @Operation(description =
        "Provisional service for testing upload of samples "
        + "including associated objects.")
    @POST
    public Sample upload(
        @Valid Sample sample
    ) throws BadRequestException {
        return repository.create(sample);
    }
}
