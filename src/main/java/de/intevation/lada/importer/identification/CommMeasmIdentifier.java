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
import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.model.lada.CommMeasm_;
import de.intevation.lada.util.data.Repository;


/**
 * Class to identify {@link CommMeasm} objects.
 */
class CommMeasmIdentifier implements Identifier<CommMeasm> {

    @Inject
    private Repository repository;

    @Override
    public CommMeasm getExisting(
        CommMeasm comment
    ) throws IdentificationException {
        try {
            return repository.getSingle(repository
                .queryBuilder(CommMeasm.class)
                .and(CommMeasm_.measm, comment.getMeasm())
                .and(CommMeasm_.text, comment.getText())
                .getQuery());
        } catch (NoResultException e) {
            return null;
        }
    }
}
