/* Copyright (C) 2026 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import de.intevation.lada.model.lada.Measm;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Path;

/**
 * Wrap service method in order to test internal state.
 */
@Path("test/measm")
public class MeasmTestService
    extends LadaIntegerIdEntityEditingService<Measm> {
    /**
     * Ensure that {@link de.intevation.lada.model.lada.Sample} retrieved
     * during deserialization in
     * {@link de.intevation.lada.model.lada.BelongsToSample.SampleToId}
     * is still linked to the persistence context.
     */
    @Override
    public Measm create(Measm measm) throws BadRequestException {
        if (!repository.entityManager().contains(measm.getSample())) {
            throw new IllegalStateException(
                "Sample not in persistence context");
        }
        return super.create(measm);
    }
}
