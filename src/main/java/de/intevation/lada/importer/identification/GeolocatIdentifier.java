/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.identification;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.Geolocat_;
import de.intevation.lada.util.data.Repository;


/**
 * Class to identify {@link Geolocat} objects.
 */
class GeolocatIdentifier implements Identifier<Geolocat> {

    @Inject
    private Repository repository;

    @Override
    public Geolocat getExisting(Geolocat loc) throws IdentificationException {
        try {
            return repository.getSingle(repository.queryBuilder(Geolocat.class)
                .and(Geolocat_.sample, loc.getSample())
                .and(Geolocat_.site, loc.getSite())
                .and(Geolocat_.typeRegulation, loc.getTypeRegulation())
                .getQuery());
        } catch (NoResultException e) {
            return null;
        }
    }
}
