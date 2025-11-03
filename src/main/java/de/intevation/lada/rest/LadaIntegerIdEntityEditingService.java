/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;
import jakarta.validation.groups.Default;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import de.intevation.lada.validation.groups.CreateErrors;


/**
 * Abstract base class for LADA REST-services for editing model entities.
 *
 * @param <T> Entity type
 */
// Currently must be public: https://issues.redhat.com/browse/RESTEASY-3621
public abstract class LadaIntegerIdEntityEditingService<T>
    extends LadaIntegerIdEntityService {

    /**
     * Create new object.
     *
     * @param object the new object
     * @return the new persistent object
     * @throws BadRequestException if any constraint violations are detected
     */
    @POST
    public T create(
        @Valid
        @ConvertGroup(from = Default.class, to = CreateErrors.class)
        T object
    ) throws BadRequestException {
        return repository.create(object);
    }

    /**
     * Update existing object.
     *
     * @param object the object to be updated
     * @return the updated persistent object
     * @throws BadRequestException if any constraint violations are detected
     */
    @PUT
    @Path("{id}")
    public T update(@Valid T object) throws BadRequestException {
        return repository.update(object);
    }
}
