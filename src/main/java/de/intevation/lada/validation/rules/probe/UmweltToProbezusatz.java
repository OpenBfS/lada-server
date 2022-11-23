/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.probe;

import java.util.List;
import javax.inject.Inject;

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
 * Validates if the probe has a valid "probenzusatzwert" for the chosen "umwelt bereich".
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
        if (probe.getEnvMediumId() == null
            || probe.getEnvMediumId().equals("")
        ) {
            return null;
        } else {
            String umwId = probe.getEnvMediumId();
            QueryBuilder<EnvSpecifMp> builderUmwZus =
                repository.queryBuilder(EnvSpecifMp.class);
                builderUmwZus.and("envMediumId", umwId);
            List <EnvSpecifMp> UmwZus = repository.filterPlain(builderUmwZus.getQuery());

            QueryBuilder<SampleSpecifMeasVal> builderZusatz =
                repository.queryBuilder(SampleSpecifMeasVal.class);
                builderZusatz.and("sampleId", probe.getId());
            List <SampleSpecifMeasVal> ZusWert = repository.filterPlain(builderZusatz.getQuery());
            for (SampleSpecifMeasVal zusW: ZusWert) {
                Boolean ZusWertFound = UmwZus.stream().anyMatch(u -> u.getSampleSpecifId().equals(zusW.getSampleSpecifId()));
                if (ZusWertFound) {
                    return null;
                } else {
                    Violation violation = new Violation();
                    violation.addWarning("zusatzwert", StatusCodes.VAL_PZW);
                    return violation;
                }
            }
        }
        return null;
    }
}
