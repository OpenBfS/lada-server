/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.ObligMeasdMp;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


/**
 * Validation rule for Measm.
 * Validates if the Measm has measVals for all obligatory measurands.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class HasObligMeasdsValidator
    implements ConstraintValidator<HasObligMeasds, Measm> {

    private String message;

    @Override
    public void initialize(HasObligMeasds constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Transactional
    @Override
    public boolean isValid(Measm messung, ConstraintValidatorContext ctx) {
        if (messung == null || messung.getSampleId() == null) {
            return true;
        }

        final String mmtIdKey = "mmtId",
            envMediumIdKey = "envMediumId",
            regulationIdKey = "regulationId";

        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(this.message)
            .addPropertyNode(mmtIdKey)
            .addConstraintViolation();
        // Short path if no measVals can be referenced
        if (messung.getId() == null) {
            return false;
        }

        Repository repository = CDI.current().getBeanContainer()
            .createInstance().select(Repository.class).get();
        Sample probe = repository.entityManager().find(
            Sample.class, messung.getSampleId());
        if (probe == null) {
            return true;
        }

        final Integer regulationId = probe.getRegulationId();
        final String mmtId = messung.getMmtId();
        final String envMediumId = probe.getEnvMediumId();
        if (regulationId == null || mmtId == null || envMediumId == null) {
            return true;
        }

        // Match by complete envMediumId
        QueryBuilder<ObligMeasdMp> builder = repository
            .queryBuilder(ObligMeasdMp.class)
            .and(mmtIdKey, mmtId)
            .and(envMediumIdKey, envMediumId)
            .and(regulationIdKey, regulationId);
        List<ObligMeasdMp> pflicht = repository.filter(builder.getQuery());

        // If matching by complete envMediumId does not find anything,
        // match by first two characters of envMediumId
        if (pflicht.isEmpty() && envMediumId.length() > 1) {
            QueryBuilder<ObligMeasdMp> builderGrp = repository
                .queryBuilder(ObligMeasdMp.class)
                .and(mmtIdKey, mmtId)
                .and(envMediumIdKey, envMediumId.substring(0, 2))
                .and(regulationIdKey, regulationId);
            pflicht = repository.filter(builderGrp.getQuery());
        }

        // If still nothing found, match by first character of envMediumId
        if (pflicht.isEmpty() && !envMediumId.isEmpty()) {
            QueryBuilder<ObligMeasdMp> builderGrp = repository
                .queryBuilder(ObligMeasdMp.class)
                .and(mmtIdKey, mmtId)
                .and(envMediumIdKey, envMediumId.substring(0, 1))
                .and(regulationIdKey, regulationId);
            pflicht = repository.filter(builderGrp.getQuery());
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
            return false;
        }
        return true;
    }
}
