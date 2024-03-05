/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.ortszuordnung;

import java.util.List;

import jakarta.inject.Inject;

import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.GeolocatMpg;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

@ValidationRule("Ortszuordnung")
public class HasEntnahmeOrt implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        if (object instanceof Geolocat) {
            Geolocat ort = (Geolocat) object;
            if (!"E".equals(ort.getTypeRegulation())) {
                return null;
            }
            QueryBuilder<Geolocat> builder = repository
                .queryBuilder(Geolocat.class)
                .and("sampleId", ort.getSampleId())
                .and("typeRegulation", "E");
            List<Geolocat> orte = repository.filterPlain(
                builder.getQuery());
            for (Geolocat o : orte) {
                if (!o.getId().equals(ort.getId())) {
                    Violation violation = new Violation();
                    violation.addWarning(
                        "typeRegulation", StatusCodes.VALUE_AMBIGOUS);
                    return violation;
                }
            }
        } else if (object instanceof GeolocatMpg) {
            GeolocatMpg ort = (GeolocatMpg) object;
            if (!"E".equals(ort.getTypeRegulation())) {
                return null;
            }
            QueryBuilder<GeolocatMpg> builder = repository
                .queryBuilder(GeolocatMpg.class)
                .and("mpgId", ort.getMpgId())
                .and("typeRegulation", "E");
            List<GeolocatMpg> orte = repository.filterPlain(
                builder.getQuery());
            for (GeolocatMpg o : orte) {
                if (!o.getId().equals(ort.getId())) {
                    Violation violation = new Violation();
                    violation.addWarning(
                        "typeRegulation", StatusCodes.VALUE_AMBIGOUS);
                    return violation;
                }
            }
        }
        return null;
    }
}
