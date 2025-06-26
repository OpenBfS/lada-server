/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.identification;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;

/**
 * Identifier for {@link Measm} objects.
 */
class MeasmIdentifier implements Identifier<Measm> {

    @Inject
    private Repository repository;

    @Override
    public Measm getExisting(Measm messung)
        throws IdentificationException {
        QueryBuilder<Measm> builder = repository.queryBuilder(Measm.class);

        // Identify using extId if it's given
        if (messung.getExtId() != null) {
            builder.and(Measm_.sample, messung.getSample())
                .and(Measm_.extId, messung.getExtId());
            try {
                return repository.getSingle(builder.getQuery());
            } catch (NoResultException e) {
                return null;
            }
        }

        /* Secondarily, identify using minSampleId.
           Falls back to unique Measm with equal Mmt and no minSampleId. */
        if (messung.getMinSampleId() != null) {
            builder.and(Measm_.sample, messung.getSample())
                .and(Measm_.minSampleId, messung.getMinSampleId());
            try {
                return repository.getSingle(builder.getQuery());
            } catch (NoResultException e) {
                if (messung.getMmtId() != null) {
                    Measm measmWithEqualMmt =
                        findUniqueMeasmWithEqualMmt(messung);
                    if (measmWithEqualMmt == null
                        || measmWithEqualMmt.getMinSampleId() == null
                    ) {
                        return measmWithEqualMmt;
                    }
                }
                return null;
            }
        }

        // Fall back to unique Measm with equal Mmt
        if (messung.getMmtId() != null) {
            return findUniqueMeasmWithEqualMmt(messung);
        }
        throw new IdentificationException();
    }

    private Measm findUniqueMeasmWithEqualMmt(Measm measm) {
        try {
            return repository.getSingle(repository
                .queryBuilder(Measm.class)
                .and(Measm_.sample, measm.getSample())
                .and(Measm_.mmtId, measm.getMmtId())
                .getQuery());
        } catch (NoResultException | NonUniqueResultException e) {
            return null;
        }
    }
}
