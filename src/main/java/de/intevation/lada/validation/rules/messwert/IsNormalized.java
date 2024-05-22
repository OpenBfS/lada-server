/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messwert;

import jakarta.inject.Inject;

import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.master.EnvMedium;
import de.intevation.lada.model.master.UnitConvers;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for messwert.
 * Validates if the "messeinheit" is the secondary "messeinheit" of to
 * umweltbereich connected to this messwert
 */
@ValidationRule("Messwert")
public class IsNormalized implements Rule {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        MeasVal messwert = (MeasVal) object;

        if (messwert == null || messwert.getMeasmId() == null) {
            return null;
        }

        EnvMedium umwelt = repository
            .getById(Measm.class, messwert.getMeasmId())
            .getSample().getEnvMedium();
        if (umwelt == null) {
            return null;
        }

        Integer mehId = umwelt.getUnit1();
        Integer secMehId = umwelt.getUnit2();
        if (mehId == null && secMehId == null) {
            return null;
        }

        Integer fromUnit = messwert.getMeasUnitId();
        if (mehId != null && mehId.equals(fromUnit)) {
            // Unit of measured value is primary unit of envMedium
            return null;
        }

        // Check if measuring unit of measured value can be converted
        // to primary or secondary measuring unit of envMedium
        Boolean convert = false;
        if (mehId != null && !mehId.equals(fromUnit)) {
            QueryBuilder<UnitConvers> builder = repository
                .queryBuilder(UnitConvers.class)
                .and("toUnitId", mehId)
                .and("fromUnitId", fromUnit);
            convert = !repository.filter(builder.getQuery()).isEmpty();
        } else if (secMehId != null && !secMehId.equals(fromUnit)) {
            QueryBuilder<UnitConvers> builder = repository
                .queryBuilder(UnitConvers.class)
                .and("toUnitId", secMehId)
                .and("fromUnitId", fromUnit);
            convert = !repository.filter(builder.getQuery()).isEmpty();
        }

        Violation violation = new Violation();
        if (convert) {
            violation.addWarning("measUnitId", StatusCodes.VAL_UNIT_NORMALIZE);
        } else if (secMehId != null && secMehId.equals(fromUnit)) {
            return null;
        } else {
            violation.addWarning("measUnitId", StatusCodes.VAL_UNIT_UMW);
        }
        return violation;
    }
}
