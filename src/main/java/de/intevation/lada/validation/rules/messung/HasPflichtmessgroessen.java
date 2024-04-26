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
        if (messung == null || messung.getSampleId() == null) {
            return null;
        }
        Sample probe = repository.getById(
            Sample.class, messung.getSampleId());

        final Integer regulationId = probe.getRegulationId();
        final String mmtId = messung.getMmtId();
        final String envMediumId = probe.getEnvMediumId();
        if (regulationId == null || mmtId == null || envMediumId == null) {
            return null;
        }

        final String mmtIdKey = "mmtId",
            envMediumIdKey = "envMediumId",
            regulationIdKey = "regulationId";
        // Match by complete envMediumId
        QueryBuilder<ObligMeasdMp> builder = repository
            .queryBuilder(ObligMeasdMp.class)
            .and(mmtIdKey, mmtId)
            .and(envMediumIdKey, envMediumId)
            .and(regulationIdKey, regulationId);
        List<ObligMeasdMp> pflicht = repository.filter(builder.getQuery());

        // If matching by complete envMediumId does not find anything,
        // match by first character of envMediumId
        if (pflicht.isEmpty() && !envMediumId.isEmpty()) {
            QueryBuilder<ObligMeasdMp> builderGrp = repository
                .queryBuilder(ObligMeasdMp.class)
                .and(mmtIdKey, mmtId)
                .and(envMediumIdKey, envMediumId.substring(0, 1))
                .and(regulationIdKey, regulationId);
            pflicht = repository.filter(builderGrp.getQuery());
        }

        // If still nothing found, match by first two characters of envMediumId
        if (pflicht.isEmpty() && envMediumId.length() > 1) {
            QueryBuilder<ObligMeasdMp> builderGrpS2 = repository
                .queryBuilder(ObligMeasdMp.class)
                .and(mmtIdKey, mmtId)
                .and(envMediumIdKey, envMediumId.substring(0, 2))
                .and(regulationIdKey, regulationId);
            pflicht = repository.filter(builderGrpS2.getQuery());
        }

        QueryBuilder<MeasVal> wertBuilder = repository
            .queryBuilder(MeasVal.class)
            .and("measmId", messung.getId());
        List<MeasVal> messwerte =
            repository.filter(wertBuilder.getQuery());
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
            Violation violation = new Violation();
            violation.addNotification(
                mmtIdKey,
                StatusCodes.VAL_OBL_MEASURE);
            return violation;
        }
        return null;
    }
}
