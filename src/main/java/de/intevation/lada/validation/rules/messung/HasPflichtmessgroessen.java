/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messung;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Messwert;
import de.intevation.lada.model.land.Probe;
import de.intevation.lada.model.stammdaten.Messgroesse;
import de.intevation.lada.model.stammdaten.PflichtMessgroesse;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for messungen.
 * Validates if the messung has all "pflichtmessgroessen".
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Messung")
public class HasPflichtmessgroessen implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Messung messung = (Messung) object;
        Probe probe = repository.getByIdPlain(
            Probe.class, messung.getProbeId());

        QueryBuilder<PflichtMessgroesse> builder =
            repository.queryBuilder(PflichtMessgroesse.class);
        builder.and("mmtId", messung.getMmtId());
        builder.and("umwId", probe.getEnvMediumId());
        builder.and("datenbasisId", probe.getRegulationId());
        Response response =
            repository.filter(builder.getQuery());
        @SuppressWarnings("unchecked")
        List<PflichtMessgroesse> pflicht =
            (List<PflichtMessgroesse>) response.getData();

        if (pflicht.isEmpty()) {
            QueryBuilder<PflichtMessgroesse> builderGrp =
                repository.queryBuilder(PflichtMessgroesse.class);
            builderGrp.and("mmtId", messung.getMmtId());
            builderGrp.and(
                "umwId",
                probe.getEnvMediumId() == null
                    ? null : probe.getEnvMediumId().substring(0, 1));
            builderGrp.and("datenbasisId", probe.getRegulationId());
            Response responseGrp =
                repository.filter(builderGrp.getQuery());
            @SuppressWarnings("unchecked")
            List<PflichtMessgroesse> pflichtGrp =
                (List<PflichtMessgroesse>) responseGrp.getData();
            pflicht.addAll(pflichtGrp);
        }

        if (pflicht.isEmpty()) {
            QueryBuilder<PflichtMessgroesse> builderGrpS2 =
                repository.queryBuilder(PflichtMessgroesse.class);
            builderGrpS2.and("mmtId", messung.getMmtId());
            builderGrpS2.and(
                "umwId",
                probe.getEnvMediumId() == null
                    ? null : probe.getEnvMediumId().length() >= 1
                        ? null : probe.getEnvMediumId().substring(0, 2));
            builderGrpS2.and("datenbasisId", probe.getRegulationId());
            Response responseGrpS2 =
                repository.filter(builderGrpS2.getQuery());
            @SuppressWarnings("unchecked")
            List<PflichtMessgroesse> pflichtGrpS2 =
                (List<PflichtMessgroesse>) responseGrpS2.getData();
            pflicht.addAll(pflichtGrpS2);
        }

        QueryBuilder<Messwert> wertBuilder =
            repository.queryBuilder(Messwert.class);
        wertBuilder.and("messungsId", messung.getId());
        Response wertResponse =
            repository.filter(wertBuilder.getQuery());
        @SuppressWarnings("unchecked")
        List<Messwert> messwerte = (List<Messwert>) wertResponse.getData();
        Violation violation = new Violation();
        List<PflichtMessgroesse> tmp = new ArrayList<PflichtMessgroesse>();
        for (Messwert wert : messwerte) {
            for (PflichtMessgroesse p : pflicht) {
                if (p.getMessgroesseId().equals(wert.getMessgroesseId())) {
                    tmp.add(p);
                }
            }
        }
        pflicht.removeAll(tmp);
        if (!pflicht.isEmpty()) {
            for (PflichtMessgroesse p : pflicht) {
                Messgroesse mg =
                    repository.getByIdPlain(
                        Messgroesse.class, p.getMessgroesseId());
                violation.addNotification(
                    "messgroesse#" + mg.getMessgroesse(),
                    StatusCodes.VAL_OBL_MEASURE);
            }
        }
        return violation.hasNotifications() ? violation : null;
    }
}
