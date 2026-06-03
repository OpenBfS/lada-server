/* Copyright (C) 2026 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.Geolocat_;
import de.intevation.lada.model.master.MunicDiv;
import jakarta.validation.ConstraintValidatorContext;


/**
 * Validates if referenced {@link MunicDiv}
 * belongs to indirectly referenced
 * {@link de.intevation.lada.model.master.AdminUnit}.
 */
public class MunicDivMatchesAdminUnitGeolocatValidator
    extends MunicDivMatchesAdminUnitValidator<Geolocat> {

    @Override
    public boolean isValid(
        Geolocat value, ConstraintValidatorContext context
    ) {
        return doValidate(
            value.getSite().getAdminUnitId(),
            value.getMunicDivId(),
            context,
            Geolocat_.MUNIC_DIV_ID);
    }
}
