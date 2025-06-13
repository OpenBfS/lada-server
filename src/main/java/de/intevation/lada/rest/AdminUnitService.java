/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.List;

import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.model.master.AdminUnit;
import de.intevation.lada.model.master.AdminUnit_;

/**
 * REST service for AdminUnit objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "adminunit")
public class AdminUnitService extends LadaStringIdEntityService {

    /**
     * Get AdminUnit objects.
     *
     * @param name The result list can be filtered using the URL parameter
     * 'query'. A filter is defined as the first letters of the 'name'.
     * Might be null (i.e. not given at all) but not an empty string.
     *
     * @return requested objects.
     */
    @GET
    public List<AdminUnit> get(
        @QueryParam("name") @Pattern(regexp = ".+") String name
    ) {
        if (name == null) {
            return repository.getAll(AdminUnit.class);
        }
        QueryBuilder<AdminUnit> builder =
            repository.queryBuilder(AdminUnit.class);
        builder.andLike(AdminUnit_.name, name + "%");
        return repository.filter(builder.getQuery());
    }

    /**
     * Get a single AdminUnit object by id.
     *
     * @return a single AdminUnit.
     */
    @GET
    @Path("{id}")
    public AdminUnit getById() {
        return repository.getById(AdminUnit.class, id);
    }
}
