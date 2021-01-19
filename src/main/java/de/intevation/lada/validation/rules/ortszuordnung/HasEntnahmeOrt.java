/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.ortszuordnung;

import java.util.List;

import javax.inject.Inject;

import de.intevation.lada.model.land.Ortszuordnung;
import de.intevation.lada.model.land.OrtszuordnungMp;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;
import de.intevation.lada.util.data.Strings;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

@ValidationRule("Ortszuordnung")
public class HasEntnahmeOrt implements Rule {

    @Inject
    @RepositoryConfig(type = RepositoryType.RO)
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Integer id = null;
        if (object instanceof Ortszuordnung) {
            Ortszuordnung ort = (Ortszuordnung) object;
            if (!"E".equals(ort.getOrtszuordnungTyp())) {
                return null;
            }
            id = ort.getProbeId();
            QueryBuilder<Ortszuordnung> builder =
                new QueryBuilder<Ortszuordnung>(
                    repository.entityManager(Strings.LAND),
                    Ortszuordnung.class);

            builder.and("probeId", id);
            List<Ortszuordnung> orte = repository.filterPlain(
                builder.getQuery(),
                Strings.LAND);
            for (Ortszuordnung o : orte) {
                if ("E".equals(o.getOrtszuordnungTyp())
                    && !o.getId().equals(ort.getId())
                ) {
                    Violation violation = new Violation();
                    violation.addError("ortszuordnungTyp", 611);
                    return violation;
                }
            }
        } else if (object instanceof OrtszuordnungMp) {
            OrtszuordnungMp ort = (OrtszuordnungMp) object;
            if (!"E".equals(ort.getOrtszuordnungTyp())) {
                return null;
            }
            id = ort.getMessprogrammId();
            QueryBuilder<OrtszuordnungMp> builder =
                new QueryBuilder<OrtszuordnungMp>(
                    repository.entityManager(Strings.LAND),
                    OrtszuordnungMp.class);

            builder.and("messprogrammId", id);
            List<OrtszuordnungMp> orte = repository.filterPlain(
                builder.getQuery(),
                Strings.LAND);
            for (OrtszuordnungMp o : orte) {
                if ("E".equals(o.getOrtszuordnungTyp())
                    && !o.getId().equals(ort.getId())
                ) {
                    Violation violation = new Violation();
                    violation.addError("ortszuordnungTyp", 611);
                    return violation;
                }
            }
        }
        return null;
    }
}
