/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.master.Site;


/**
 * Validation rule for site.
 * Validates if the site has valid REI attributes.
 */
public class IsReiCompleteSiteValidator
    implements ConstraintValidator<IsReiComplete, Site> {

    private static final Integer SITE_CLASS_REI = 3;

    private String message;

    @Override
    public void initialize(IsReiComplete constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Site site, ConstraintValidatorContext ctx) {
        if (site != null
            && SITE_CLASS_REI.equals(site.getSiteClassId())
            && site.getNuclFacilGrId() == null
        ) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(this.message)
                .addPropertyNode("nuclFacilGrId")
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
