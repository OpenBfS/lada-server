/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.Sample_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;

/**
 * Class to identify {@link Sample} objects.
 */
public class SampleIdentifier implements Identifier<Sample> {

    @Inject
    private Repository repository;

    @Override
    public Sample getExisting(Sample probe)
        throws Identifier.IdentificationException {
        QueryBuilder<Sample> builder = repository.queryBuilder(Sample.class);

        if (probe.getExtId() != null) {
            builder.and(Sample_.extId, probe.getExtId());
            try {
                return repository.getSingle(builder.getQuery());
            } catch (NoResultException e) {
                // continue
            }
        }
        if (probe.getMainSampleId() != null
            && probe.getMeasFacilId() != null
        ) {
            builder.and(Sample_.measFacilId, probe.getMeasFacilId())
                .and(Sample_.mainSampleId, probe.getMainSampleId())
                .and(Sample_.isTest, probe.getIsTest());
            try {
                return repository.getSingle(builder.getQuery());
            } catch (NoResultException e) {
                // continue
            }
        }
        return null;
    }
}
