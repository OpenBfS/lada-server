/* Copyright (C) 2026 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.identification;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.model.lada.CommSample_;
import de.intevation.lada.util.data.Repository;


/**
 * Class to identify {@link CommSample} objects.
 */
class CommSampleIdentifier implements Identifier<CommSample> {

    @Inject
    private Repository repository;

    @Override
    public CommSample getExisting(
        CommSample comment
    ) throws IdentificationException {
        try {
            return repository.getSingle(repository
                .queryBuilder(CommSample.class)
                .and(CommSample_.sample, comment.getSample())
                .and(CommSample_.text, comment.getText())
                .getQuery());
        } catch (NoResultException e) {
            return null;
        }
    }
}
