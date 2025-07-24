/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.model.lada.SampleSpecifMeasVal_;
import de.intevation.lada.util.data.Repository;


/**
 * Class to identify {@link SampleSpecifMeasVal} objects.
 */
public class SampleSpecifMeasValIdentifier
    implements Identifier<SampleSpecifMeasVal> {

    @Inject
    private Repository repository;

    @Override
    public SampleSpecifMeasVal getExisting(
        SampleSpecifMeasVal sampleSpecifMeasVal
    ) throws IdentificationException {
        try {
            return repository.getSingle(repository
                .queryBuilder(SampleSpecifMeasVal.class)
                .and(SampleSpecifMeasVal_.sampleId,
                    sampleSpecifMeasVal.getSampleId())
                .and(SampleSpecifMeasVal_.sampleSpecifId,
                    sampleSpecifMeasVal.getSampleSpecifId())
                .getQuery());
        } catch (NoResultException e) {
            return null;
        }
    }
}
