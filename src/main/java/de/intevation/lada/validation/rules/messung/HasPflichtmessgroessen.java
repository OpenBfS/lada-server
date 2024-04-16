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

import jakarta.inject.Inject;

import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.ObligMeasdMp;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
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
        Measm messung = (Measm) object;
        Sample probe = repository.getById(
            Sample.class, messung.getSampleId());

        QueryBuilder<ObligMeasdMp> builder = repository
            .queryBuilder(ObligMeasdMp.class)
            .and("mmtId", messung.getMmtId())
            .and("envMediumId", probe.getEnvMediumId())
            .and("regulationId", probe.getRegulationId());
        List<ObligMeasdMp> pflicht = repository.filter(builder.getQuery());

        if (pflicht.isEmpty()) {
            QueryBuilder<ObligMeasdMp> builderGrp = repository
                .queryBuilder(ObligMeasdMp.class)
                .and("mmtId", messung.getMmtId())
                .and("envMediumId",
                    probe.getEnvMediumId() == null
                    ? null
                    : probe.getEnvMediumId().substring(0, 1))
                .and("regulationId", probe.getRegulationId());
            List<ObligMeasdMp> pflichtGrp =
                repository.filter(builderGrp.getQuery());
            pflicht.addAll(pflichtGrp);

            QueryBuilder<ObligMeasdMp> builderGrpS2 = repository
                .queryBuilder(ObligMeasdMp.class)
                .and("mmtId", messung.getMmtId())
                .and("envMediumId",
                    probe.getEnvMediumId() == null
                    ? null : probe.getEnvMediumId().length() >= 1
                        ? null : probe.getEnvMediumId().substring(0, 2))
                .and("regulationId", probe.getRegulationId());
            List<ObligMeasdMp> pflichtGrpS2 =
                repository.filter(builderGrpS2.getQuery());
            pflicht.addAll(pflichtGrpS2);
        }

        QueryBuilder<MeasVal> wertBuilder = repository
            .queryBuilder(MeasVal.class)
            .and("measmId", messung.getId());
        List<MeasVal> messwerte =
            repository.filter(wertBuilder.getQuery());
        Violation violation = new Violation();
        List<ObligMeasdMp> tmp = new ArrayList<ObligMeasdMp>();
        for (MeasVal wert : messwerte) {
            for (ObligMeasdMp p : pflicht) {
                if (p.getMeasdId().equals(wert.getMeasdId())) {
                    tmp.add(p);
                }
            }
        }
        pflicht.removeAll(tmp);

        if (!pflicht.isEmpty()) {
            violation.addNotification(
                "measdId",
                StatusCodes.VAL_OBL_MEASURE);
        }
        return violation.hasNotifications() ? violation : null;
    }
}
