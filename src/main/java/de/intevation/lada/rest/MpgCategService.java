/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.model.master.MpgCateg;

/**
 * REST service for MpgCateg objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "mpgcateg")
public class MpgCategService
    extends LadaIntegerIdEntityEditingService<MpgCateg> {

    /**
     * Get all MpgCateg objects.
     *
     * @return requested objects.
     */
    @GET
    public List<MpgCateg> get() {
        return repository.getAll(MpgCateg.class);
    }

    /**
     * Get a single object by id.
     *
     * @return a single object.
     */
    @GET
    @Path("{id}")
    public MpgCateg getById() {
        return repository.getById(MpgCateg.class, id);
    }

    @DELETE
    @Path("{id}")
    public void delete() {
        MpgCateg kategorie = repository.getById(
            MpgCateg.class, id);
        authorization.authorize(kategorie, RequestMethod.DELETE);
        repository.delete(kategorie);
    }
}
