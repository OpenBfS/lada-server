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

import de.intevation.lada.model.land.Probe;
import de.intevation.lada.model.land.ZusatzWert;
import de.intevation.lada.model.stammdaten.UmweltZusatz;
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
@ValidationRule("Probe")
public class UmweltToProbezusatz implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Probe probe = (Probe) object;
        if (probe.getUmwId() == null
            || probe.getUmwId().equals("")
        ) {
            return null;
        } else {
            String umwId = probe.getUmwId();
            QueryBuilder<UmweltZusatz> builderUmwZus =
                repository.queryBuilder(UmweltZusatz.class);
                builderUmwZus.and("umwId", umwId);
            List <UmweltZusatz> UmwZus = repository.filterPlain(builderUmwZus.getQuery());

            QueryBuilder<ZusatzWert> builderZusatz =
                repository.queryBuilder(ZusatzWert.class);
                builderZusatz.and("probeId", probe.getId());
            List <ZusatzWert> ZusWert = repository.filterPlain(builderZusatz.getQuery());
            for (ZusatzWert zusW: ZusWert) {
                Boolean ZusWertFound = UmwZus.stream().anyMatch(u -> u.getPzsId().equals(zusW.getPzsId()));
                if (ZusWertFound) {
                    return null;
                } else {
                    Violation violation = new Violation();
                    violation.addWarning("zusatzwert", StatusCodes.VALUE_NOT_MATCHING);
                    return violation;
                }
            }
        }
        return null;
    }
}
