/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;

/**
 * Identifier for messung objects.
 */
public class MessungIdentifier implements Identifier<Measm> {

    @Inject
    private Repository repository;

    @Override
    public Measm getExisting(Measm messung)
        throws Identifier.IdentificationException {
        QueryBuilder<Measm> builder = repository.queryBuilder(Measm.class);

        if (messung.getExtId() == null
            && messung.getMinSampleId() != null
        ) {
            builder.and(Measm_.sampleId, messung.getSampleId())
                .and(Measm_.minSampleId, messung.getMinSampleId());
            try {
                return repository.getSingle(builder.getQuery());
            } catch (NoResultException e) {
                //TODO: QueryBuilder instance can not be reused here
                //This may be a hibernate 6 bug, see:
                //https://hibernate.atlassian.net/browse/HHH-15951
                builder = repository.queryBuilder(Measm.class)
                    .and(Measm_.sampleId, messung.getSampleId())
                    .and(Measm_.mmtId, messung.getMmtId());
                List<Measm> messungen =
                    repository.filter(builder.getQuery());
                if (messungen.size() == 1
                    && messungen.get(0).getMinSampleId() == null
                ) {
                    return messungen.get(0);
                }
                return null;
            }
        } else if (messung.getExtId() != null) {
            builder.and(Measm_.sampleId, messung.getSampleId())
                .and(Measm_.extId, messung.getExtId());
            try {
                return repository.getSingle(builder.getQuery());
            } catch (NoResultException e) {
                return null;
            }
        } else if (messung.getMmtId() != null) {
            builder.and(Measm_.sampleId, messung.getSampleId())
                .and(Measm_.mmtId, messung.getMmtId());
            List<Measm> messungen =
                repository.filter(builder.getQuery());
            if (messungen.isEmpty() || messungen.size() > 1) {
                return null;
            }
            return messungen.get(0);
        }
        throw new Identifier.IdentificationException();
    }
}
