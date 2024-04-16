/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.probe;

import java.util.List;
import jakarta.inject.Inject;

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.model.master.EnvSpecifMp;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;

/**
 * Validation rule for probe.
 * Validates if the probe has valid "Probenzusatzwert" objects for the chosen
 * "Umweltbereich".
 *
 * @author <a href="mailto:jbuermeyer@bfs.de">Jonas Buermeyer</a>
 */
@ValidationRule("Sample")
public class UmweltToProbezusatz implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Sample probe = (Sample) object;
        if (probe.getEnvMediumId() != null) {
            String umwId = probe.getEnvMediumId();
            QueryBuilder<EnvSpecifMp> builderUmwZus = repository
                .queryBuilder(EnvSpecifMp.class)
                .and("envMediumId", umwId);
            List <EnvSpecifMp> umwZus = repository.filter(
                builderUmwZus.getQuery());

            QueryBuilder<SampleSpecifMeasVal> builderZusatz = repository
                .queryBuilder(SampleSpecifMeasVal.class)
                .and("sampleId", probe.getId());
            List<SampleSpecifMeasVal> zusWert = repository.filter(
                builderZusatz.getQuery());
            Violation violation = new Violation();
            for (SampleSpecifMeasVal zusW: zusWert) {
                boolean zusWertFound = umwZus.stream().anyMatch(
                    u -> u.getSampleSpecifId().equals(
                        zusW.getSampleSpecifId()));
                if (!zusWertFound) {
                    violation.addWarning(
                        "sampleSpecifMeasVals", StatusCodes.VAL_PZW);
                }
            }
            if (violation.hasWarnings()) {
                return violation;
            }
        }
        return null;
    }
}
