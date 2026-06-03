/* Copyright (C) 2026 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import de.intevation.lada.model.lada.GeolocatMpg;
import de.intevation.lada.model.lada.GeolocatMpg_;
import de.intevation.lada.model.master.MunicDiv;
import jakarta.validation.ConstraintValidatorContext;


/**
 * Validates if referenced {@link MunicDiv}
 * belongs to indirectly referenced
 * {@link de.intevation.lada.model.master.AdminUnit}.
 */
public class MunicDivMatchesAdminUnitGeolocatMpgValidator
    extends MunicDivMatchesAdminUnitValidator<GeolocatMpg> {

    @Override
    public boolean isValid(
        GeolocatMpg value, ConstraintValidatorContext context
    ) {
        return doValidate(
            value.getSite().getAdminUnitId(),
            value.getMunicDivId(),
            context,
            GeolocatMpg_.MUNIC_DIV_ID);
    }
}
