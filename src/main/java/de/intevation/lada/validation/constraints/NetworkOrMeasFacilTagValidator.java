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

import de.intevation.lada.model.master.Tag;
import de.intevation.lada.model.master.Tag_;


/**
 * Checks if the validated entity references either Network or MeasFacil
 * or none.
 */
public class NetworkOrMeasFacilTagValidator
    implements ConstraintValidator<NetworkOrMeasFacil, Tag> {

    private String message;

    @Override
    public void initialize(NetworkOrMeasFacil constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Tag tag, ConstraintValidatorContext ctx) {
        if (tag == null
            || tag.getNetworkId() == null
            || tag.getMeasFacilId() == null
        ) {
            return true;
        }

        final String networkIdKey = Tag_.NETWORK_ID,
            measFacilIdKey = Tag_.MEAS_FACIL_ID;
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(this.message)
            .addPropertyNode(networkIdKey)
            .addConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(this.message)
            .addPropertyNode(measFacilIdKey)
            .addConstraintViolation();
        return false;
    }
}
