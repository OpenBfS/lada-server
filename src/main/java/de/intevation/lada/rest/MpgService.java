/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

import de.intevation.lada.factory.ProbeFactory;
import de.intevation.lada.i18n.I18n;
import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.lada.Mpg_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;


/**
 * REST service for Mpg objects.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path(LadaService.PATH_REST + "mpg")
public class MpgService extends LadaIntegerIdEntityEditingService<Mpg> {

    @Inject
    private ProbeFactory factory;

    @Inject
    private I18n i18n;

    /**
     * Expected format for payload in PUT request to setActive.
     */
    public static class SetActive {
        @NotNull
        private Boolean active;

        @NotNull
        private List<@NotNull @IsValidPrimaryKey(
            clazz = Mpg.class) Integer> ids;

        public Boolean isActive() {
            return this.active;
        }
        public void setActive(Boolean active) {
            this.active = active;
        }

        public List<Integer> getIds() {
            return this.ids;
        }
        public void setIds(List<Integer> ids) {
            this.ids = ids;
        }
    }

    /**
     * Get a Mpg object by id.
     *
     * @return a single Mpg.
     */
    @GET
    @Path("{id}")
    public Mpg getById() {
        return repository.getById(Mpg.class, id);
    }

    @Override
    public Mpg create(Mpg mpg) throws BadRequestException {
        setEnvAttrs(mpg);

        return super.create(mpg);
    }

    @Override
    public Mpg update(Mpg mpg) throws BadRequestException {
        setEnvAttrs(mpg);

        return super.update(mpg);
    }

    /**
     * Update the active attribute of existing Mpg objects as bulk
     * operation.
     *
     * @param data Object representing active status and list of IDs
     * @return A map of IDs with error messages for failed Mpg objects
     * @throws BadRequestException if any constraint violations are detected.
     */
    @PUT
    @Path("active")
    public Map<Integer, String> setActive(
        @Valid SetActive data
    ) throws BadRequestException {
        QueryBuilder<Mpg> builder = repository.queryBuilder(Mpg.class)
            .orIn(Mpg_.id, data.getIds());
        List<Mpg> messprogramme =
            repository.filter(builder.getQuery());

        Map<Integer, String> result = HashMap.newHashMap(messprogramme.size());
        for (Mpg m : messprogramme) {
            if (authorization.isAuthorized(m, RequestMethod.PUT)) {
                m.setIsActive(data.isActive());
                repository.update(m);
            } else {
                result.put(m.getId(), i18n.getString(I18n.KEY_FORBIDDEN));
            }
        }

        return result;
    }

    /**
     * Delete an existing Mpg object by id.
     */
    @DELETE
    @Path("{id}")
    public void delete() {
        Mpg messprogrammObj = repository.getById(
            Mpg.class, id);
        authorization.authorize(messprogrammObj, RequestMethod.DELETE);
        repository.delete(messprogrammObj);
    }

    private void setEnvAttrs(Mpg messprogramm) {
        if (messprogramm.getEnvMediumId() == null) {
            messprogramm.setEnvMediumId(
                factory.findEnvMediumId(messprogramm.getEnvDescripDisplay()));
        } else if (messprogramm.getEnvDescripDisplay() == null) {
            messprogramm.setEnvDescripDisplay(
                factory.getInitialMediaDesk(messprogramm.getEnvMediumId()));
        }
    }
}
