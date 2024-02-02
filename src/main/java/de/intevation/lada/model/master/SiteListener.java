/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.model.master;

import de.intevation.lada.util.data.Repository;
import jakarta.inject.Inject;
import jakarta.persistence.PostLoad;

/**
 * Listener class setting plausibleReferenceCount of site objects after
 * loading.
 */
public class SiteListener {
    private static final String PLAUSIBLE_REFERENCE_COUNT_QUERY =
    """
        SELECT COUNT(DISTINCT sa.id)
        FROM master.site s
        INNER JOIN lada.geolocat g ON s.id=g.site_id
        INNER JOIN lada.sample sa ON g.sample_id=sa.id
        INNER JOIN lada.measm m ON m.sample_id=sa.id
        INNER JOIN lada.status_prot sp ON m.status=sp.id
        WHERE s.id = :siteId and sp.status_mp_id IN (2,6,10);
    """;

    @Inject
    private Repository repo;

    /**
     * Constructor.
     */
    public SiteListener() { }

    /**
     * Listener setting plausible reference count post load.
     * @param site Site to update
     */
    @PostLoad
    private void setPlausibleReferenceCount(Site site) {
        Integer count = (Integer) repo.entityManager()
            .createNativeQuery(
                PLAUSIBLE_REFERENCE_COUNT_QUERY, Integer.class)
            .setParameter("siteId", site.getId())
            .getSingleResult();
        site.setPlausibleReferenceCount(count);
    }
}
