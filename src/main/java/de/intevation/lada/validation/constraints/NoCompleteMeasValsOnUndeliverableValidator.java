/* Copyright (C) 2026 by Bundesamt fuer Strahlenschutz
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
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.Names;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.lada.StatusProt_;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.util.data.Repository;


/**
 * Prevent status "undeliverable" for Measm with valid measVals.
 */
public class NoCompleteMeasValsOnUndeliverableValidator
    implements ConstraintValidator<NoCompleteMeasValsOnUndeliverable, StatusProt> {
    private String message;

    @Override
    public void initialize(
        NoCompleteMeasValsOnUndeliverable constraintAnnotation
    ) {
        this.message = constraintAnnotation.message();
    }

    @Transactional
    @Override
    public boolean isValid(StatusProt status, ConstraintValidatorContext ctx) {
        if (status == null
            || status.getStatusMpId() == null
            || status.getMeasm() == null
        ) {
            return true;
        }

        Repository repository = CDI.current().getBeanContainer()
            .createInstance().select(Repository.class).get();
        StatusMp statusMp = repository.entityManager()
            .find(StatusMp.class, status.getStatusMpId());
        if (statusMp == null || statusMp.getStatusVal().getId() != 7) {
            return true;
        }

        boolean isValid = !repository.entityManager()
            .createNamedQuery(
                Measm_.QUERY_HAS_COMPLETE_MEAS_VALS, boolean.class)
            .setParameter(Names.QUERY_MEASM_PARAM, status.getMeasm())
            .getSingleResult();
        if (!isValid) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(this.message)
                .addPropertyNode(StatusProt_.STATUS_MP)
                .addConstraintViolation();
        }
        return isValid;
    }
}
