/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.master.EnvMedium;
import de.intevation.lada.model.master.UnitConvers;
import de.intevation.lada.model.master.UnitConvers_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


/**
 * Validation rule for MeasVal.
 * Validates if the measuring unit is the primary unit of the environmental
 * medium or is convertible into it.
 */
public class IsMeasdPrimaryOrConvertibleToValidator
    implements ConstraintValidator<IsMeasdPrimaryOrConvertibleTo, MeasVal> {

    private String message;

    @Override
    public void initialize(IsMeasdPrimaryOrConvertibleTo constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Transactional
    @Override
    public boolean isValid(MeasVal messwert, ConstraintValidatorContext ctx) {
        if (messwert == null || messwert.getMeasmId() == null) {
            return true;
        }

        Repository repository = CDI.current().getBeanContainer()
            .createInstance().select(Repository.class).get();
        Measm measm = repository.entityManager().find(
            Measm.class, messwert.getMeasmId());
        if (measm == null) {
            return true;
        }
        EnvMedium umwelt = measm.getSample().getEnvMedium();
        if (umwelt == null) {
            return true;
        }

        Integer primaryUnit = umwelt.getUnit1();
        Integer selectedUnit = messwert.getMeasUnitId();
        if (primaryUnit != null && primaryUnit.equals(selectedUnit)) {
            return true;
        }

        Integer secMehId = umwelt.getUnit2();
        if (secMehId == null) {
            return true;
        }

        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(this.message)
            .addPropertyNode(MeasVal_.MEAS_UNIT_ID)
            .addConstraintViolation();

        // Check if secondary unit is selected
        if (secMehId.equals(selectedUnit)) {
            return false;
        }

        // Check if the measVal is convertable into the primary unit
        QueryBuilder<UnitConvers> primaryBuilder = repository
            .queryBuilder(UnitConvers.class)
            .and(UnitConvers_.fromUnitId, selectedUnit)
            .and(UnitConvers_.toUnitId, umwelt.getUnit1());
        if (!repository.filter(primaryBuilder.getQuery()).isEmpty()) {
            return true;
        }

        // Check if the measVal is convertable into the secondary unit
        QueryBuilder<UnitConvers> secondaryBuilder = repository
            .queryBuilder(UnitConvers.class)
            .and(UnitConvers_.fromUnitId, selectedUnit)
            .and(UnitConvers_.toUnitId, secMehId);
        if (!repository.filter(secondaryBuilder.getQuery()).isEmpty()) {
            return false;
        }
        return true;
    }
}
