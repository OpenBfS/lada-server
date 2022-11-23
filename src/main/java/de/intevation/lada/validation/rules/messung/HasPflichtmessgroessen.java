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

import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.Measd;
import de.intevation.lada.model.master.ObligMeasdMp;
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
        Measm messung = (Measm) object;
        Sample probe = repository.getByIdPlain(
            Sample.class, messung.getSampleId());

        QueryBuilder<ObligMeasdMp> builder =
            repository.queryBuilder(ObligMeasdMp.class);
        builder.and("mmtId", messung.getMmtId());
        builder.and("envMediumId", probe.getEnvMediumId());
        builder.and("regulationId", probe.getRegulationId());
        Response response =
            repository.filter(builder.getQuery());
        @SuppressWarnings("unchecked")
        List<ObligMeasdMp> pflicht =
            (List<ObligMeasdMp>) response.getData();

        if (pflicht.isEmpty()) {
            QueryBuilder<ObligMeasdMp> builderGrp =
                repository.queryBuilder(ObligMeasdMp.class);
            builderGrp.and("mmtId", messung.getMmtId());
            builderGrp.and(
                "envMediumId",
                probe.getEnvMediumId() == null
                    ? null : probe.getEnvMediumId().substring(0, 1));
            builderGrp.and("regulationId", probe.getRegulationId());
            Response responseGrp =
                repository.filter(builderGrp.getQuery());
            @SuppressWarnings("unchecked")
            List<ObligMeasdMp> pflichtGrp =
                (List<ObligMeasdMp>) responseGrp.getData();
            pflicht.addAll(pflichtGrp);
        }

        if (pflicht.isEmpty()) {
            QueryBuilder<ObligMeasdMp> builderGrpS2 =
                repository.queryBuilder(ObligMeasdMp.class);
            builderGrpS2.and("mmtId", messung.getMmtId());
            builderGrpS2.and(
                "envMediumId",
                probe.getEnvMediumId() == null
                    ? null : probe.getEnvMediumId().length() >= 1
                        ? null : probe.getEnvMediumId().substring(0, 2));
            builderGrpS2.and("regulationId", probe.getRegulationId());
            Response responseGrpS2 =
                repository.filter(builderGrpS2.getQuery());
            @SuppressWarnings("unchecked")
            List<ObligMeasdMp> pflichtGrpS2 =
                (List<ObligMeasdMp>) responseGrpS2.getData();
            pflicht.addAll(pflichtGrpS2);
        }

        QueryBuilder<MeasVal> wertBuilder =
            repository.queryBuilder(MeasVal.class);
        wertBuilder.and("measmId", messung.getId());
        Response wertResponse =
            repository.filter(wertBuilder.getQuery());
        @SuppressWarnings("unchecked")
        List<MeasVal> messwerte = (List<MeasVal>) wertResponse.getData();
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
            for (ObligMeasdMp p : pflicht) {
                Measd mg =
                    repository.getByIdPlain(
                        Measd.class, p.getMeasdId());
                violation.addNotification(
                    "messgroesse#" + mg.getName(),
                    StatusCodes.VAL_OBL_MEASURE);
            }
        }
        return violation.hasNotifications() ? violation : null;
    }
}
