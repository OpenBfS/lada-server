/* Copyright (C) 2026 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import de.intevation.lada.model.lada.GeolocatMpg;
import de.intevation.lada.model.lada.GeolocatMpg_;
import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.data.Repository;


public class ReferencedNetworksMatchGeolocatMpgValidator
    implements ConstraintValidator<ReferencedNetworksMatch, GeolocatMpg> {

    private String message;

    @Override
    public void initialize(ReferencedNetworksMatch constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Transactional
    @Override
    public boolean isValid(
        GeolocatMpg geolocat, ConstraintValidatorContext ctx
    ) {
        if (geolocat == null
            || geolocat.getMpgId() == null
            || geolocat.getSite() == null
        ) {
            return true;
        }

        EntityManager em = CDI.current().getBeanContainer()
            .createInstance().select(Repository.class).get().entityManager();
        Mpg mpg = em.find(Mpg.class, geolocat.getMpgId());
        if (mpg == null) {
            return true;
        }

        MeasFacil measFacil = em.find(MeasFacil.class, mpg.getMeasFacilId());
        boolean isValid = measFacil.getNetworkId().equals(
            geolocat.getSite().getNetworkId());
        if (!isValid) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(GeolocatMpg_.MPG_ID)
                .addConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(GeolocatMpg_.SITE)
                .addConstraintViolation();
        }
        return isValid;
    }
}
