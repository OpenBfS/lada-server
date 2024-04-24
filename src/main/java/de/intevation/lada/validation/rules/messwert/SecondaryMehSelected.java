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
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
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
public class SecondaryMehSelected implements Rule {

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
        Measm measm = repository.getById(Measm.class, messwert.getMeasmId());
        if (measm == null) {
            return null;
        }
        EnvMedium umwelt = measm.getSample().getEnvMedium();
        if (umwelt == null) {
            return null;
        }
        Integer secMehId = umwelt.getUnit2();
        if (secMehId == null) {
            return null;
        }

        Violation violation = new Violation();
        violation.addNotification(
            "measUnitId", StatusCodes.VAL_SEC_UNIT);

        // Check if secondary unit is selected
        if (secMehId.equals(messwert.getMeasUnitId())) {
            return violation;
        }

        // Check if the measVal is convertable into the primary unit
        QueryBuilder<UnitConvers> primaryBuilder = repository
            .queryBuilder(UnitConvers.class)
            .and("fromUnitId", messwert.getMeasUnitId())
            .and("toUnitId", umwelt.getUnit1());
        if (!repository.filter(primaryBuilder.getQuery()).isEmpty()) {
            return null;
        }

        // Check if the measVal is convertable into the secondary unit
        QueryBuilder<UnitConvers> secondaryBuilder = repository
            .queryBuilder(UnitConvers.class)
            .and("fromUnitId", messwert.getMeasUnitId())
            .and("toUnitId", secMehId);
        if (!repository.filter(secondaryBuilder.getQuery()).isEmpty()) {
            return violation;
        }
        return null;
    }
}
