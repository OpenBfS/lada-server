/* Copyright (C) 2026 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import de.intevation.lada.model.master.MunicDiv;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.Site_;
import jakarta.validation.ConstraintValidatorContext;


/**
 * Validates if referenced {@link MunicDiv}
 * belongs to referenced {@link de.intevation.lada.model.master.AdminUnit}.
 */
public class MunicDivMatchesAdminUnitSiteValidator
    extends MunicDivMatchesAdminUnitValidator<Site> {

    @Override
    public boolean isValid(Site value, ConstraintValidatorContext context) {
        return doValidate(
            value.getAdminUnitId(),
            value.getMunicDivId(),
            context,
            Site_.MUNIC_DIV_ID);
    }
}
