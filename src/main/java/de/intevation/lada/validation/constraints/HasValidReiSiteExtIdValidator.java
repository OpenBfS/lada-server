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
 * Validates if extId is valid for REI site.
 */
public class HasValidReiSiteExtIdValidator
    implements ConstraintValidator<HasValidReiSiteExtId, Site> {

    private String message;

    @Override
    public void initialize(HasValidReiSiteExtId constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Site site, ConstraintValidatorContext ctx) {
        if (site != null
            && Site.SiteClassId.REI.equals(site.getSiteClassId())
            && site.getExtId() != null
            && (site.getExtId().length() < 5
                || site.getExtId().length() > 12)
        ) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(this.message)
                .addPropertyNode("extId")
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
