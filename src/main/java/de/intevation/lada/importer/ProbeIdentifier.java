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

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.Sample_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;

/**
 * Class to identify a probe object.
 */
public class ProbeIdentifier implements Identifier<Sample> {

    @Inject
    private Repository repository;

    private Sample found;

    @Override
    public Identified find(Sample probe) {
        found = null;
        QueryBuilder<Sample> builder = repository.queryBuilder(Sample.class);

        if (probe.getExtId() == null
            && probe.getMainSampleId() != null
            && probe.getMeasFacilId() != null
        ) {
            builder.and(Sample_.measFacilId, probe.getMeasFacilId())
                .and(Sample_.mainSampleId, probe.getMainSampleId());
            List<Sample> proben =
                repository.filter(builder.getQuery());
            if (proben.size() > 1) {
                // Should never happen. DB has unique constraint for
                // "mainSampleId"
                return Identified.REJECT;
            }
            if (proben.isEmpty()) {
                return Identified.NEW;
            }
            found = proben.get(0);
            return Identified.UPDATE;
        } else if (probe.getExtId() != null
            && (probe.getMainSampleId() == null
                || probe.getMeasFacilId() == null)
        ) {
            builder.and(Sample_.extId, probe.getExtId());
            List<Sample> proben =
                repository.filter(builder.getQuery());
            if (proben.size() > 1) {
                // Should never happen. DB has unique constraint for
                // "sampleExtId"
                return Identified.REJECT;
            }
            if (proben.isEmpty()) {
                return Identified.NEW;
            }
            found = proben.get(0);
            return Identified.UPDATE;
        }
        builder.and(Sample_.extId, probe.getExtId());
        List<Sample> proben =
            repository.filter(builder.getQuery());
        if (proben.size() > 1) {
            // Should never happen. DB has unique constraint for
            // "sampleExtId"
            return Identified.REJECT;
        }
        if (proben.isEmpty()) {
            return Identified.NEW;
        }
        if (proben.get(0).getMainSampleId() == null
            || proben.get(0).getMainSampleId().equals(
                probe.getMainSampleId())
            || probe.getMainSampleId().isEmpty()
            || proben.get(0).getMainSampleId().isEmpty()
        ) {
            found = proben.get(0);
            return Identified.UPDATE;
        }
        return Identified.REJECT;
    }

    /**
     * @return the found probe
     */
    public Sample getExisting() {
        return found;
    }
}
