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
import de.intevation.lada.factory.OrtFactory;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.Site_;
import de.intevation.lada.util.data.Repository;


/**
 * Class to identify {@link Site} objects.
 */
class SiteIdentifier implements Identifier<Site> {

    @Inject
    private Repository repository;

    @Inject
    private OrtFactory ortFactory;

    @Override
    public Site getExisting(Site site) throws IdentificationException {
        try {
            return repository.getSingle(repository.queryBuilder(Site.class)
                .and(Site_.networkId, site.getNetworkId())
                .and(Site_.extId, site.getExtId())
                .getQuery());
        } catch (NoResultException e) {
            /* Not really identification, but might find something
               more or less similar to input */
            return ortFactory.findExistingSite(site);
        }
    }
}
