/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
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

import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.data.Repository;


/**
 * Checks if the validated entity references matching Network and MeasFacil.
 */
public class NetworkMatchingMeasFacilTagValidator
    implements ConstraintValidator<NetworkMatchingMeasFacil, Tag> {

    @Override
    @Transactional
    public boolean isValid(Tag tag, ConstraintValidatorContext ctx) {
        if (tag == null) {
            return true;
        }

        String networkId = tag.getNetworkId();
        String measFacilId = tag.getMeasFacilId();
        if (networkId == null || measFacilId == null) {
            return true;
        }

        // Get instance programmatically because dependency injection
        // is not guaranteed to work in ConstraintValidator implementations
        MeasFacil measFacil = CDI.current().getBeanContainer().createInstance()
            .select(Repository.class).get().entityManager()
            .find(MeasFacil.class, measFacilId);
        // Leave this check up to field-level constraint
        if (measFacil == null) {
            return true;
        }

        boolean isValid = true;
        final String networkIdKey = "networkId",
            measFacilIdKey = "measFacilId";
        if (!networkId.equals(measFacil.getNetworkId())) {
            isValid = false;
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(
                NetworkMatchingMeasFacil.MSG)
                .addPropertyNode(networkIdKey)
                .addConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(
                NetworkMatchingMeasFacil.MSG)
                .addPropertyNode(measFacilIdKey)
                .addConstraintViolation();
        }
        return isValid;
    }
}
