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

    private Measm found;

    @Override
    public Identified find(Measm messung) {
        found = null;
        QueryBuilder<Measm> builder = repository.queryBuilder(Measm.class);

        if (messung.getExtId() == null
            && messung.getMinSampleId() != null
        ) {
            builder.and(Measm_.sampleId, messung.getSampleId())
                .and(Measm_.minSampleId, messung.getMinSampleId());
            List<Measm> messungen =
                repository.filter(builder.getQuery());
            if (messungen.size() > 1) {
                // Should never happen. DB has unique constraint for
                // "nebenprobenNr"
                return Identified.REJECT;
            }
            if (messungen.isEmpty()) {
                //TODO: QueryBuilder instance can not be reused here
                //This may be a hibernate 6 bug, see:
                //https://hibernate.atlassian.net/browse/HHH-15951
                builder = repository.queryBuilder(Measm.class)
                    .and(Measm_.sampleId, messung.getSampleId())
                    .and(Measm_.mmtId, messung.getMmtId());
                messungen =
                    repository.filter(builder.getQuery());
                if (messungen.size() == 1
                    && messungen.get(0).getMinSampleId() == null
                ) {
                    found = messungen.get(0);
                    return Identified.UPDATE;
                }
                return Identified.NEW;
            }
            found = messungen.get(0);
            return Identified.UPDATE;
        } else if (messung.getExtId() != null) {
            builder.and(Measm_.sampleId, messung.getSampleId())
                .and(Measm_.extId, messung.getExtId());
            List<Measm> messungen =
                repository.filter(builder.getQuery());
            if (messungen.size() > 1) {
                // Should never happen. DB has unique constraint for
                // "externeMessungsId"
                return Identified.REJECT;
            }
            if (messungen.isEmpty()) {
                return Identified.NEW;
            }
            found = messungen.get(0);
            return Identified.UPDATE;
        } else if (messung.getMmtId() != null) {
            builder.and(Measm_.sampleId, messung.getSampleId())
                .and(Measm_.mmtId, messung.getMmtId());
            List<Measm> messungen =
                repository.filter(builder.getQuery());
            if (messungen.isEmpty() || messungen.size() > 1) {
                return Identified.NEW;
            }
            found = messungen.get(0);
            return Identified.UPDATE;
        }
        return Identified.REJECT;
    }

    @Override
    public Measm getExisting() {
        return found;
    }
}
