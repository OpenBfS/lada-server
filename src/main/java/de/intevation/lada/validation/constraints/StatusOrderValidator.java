/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import java.util.List;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.master.StatusOrdMp;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


/**
 * Validation rule for status.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class StatusOrderValidator
    implements ConstraintValidator<StatusOrder, StatusProt> {

    private String message;

    @Override
    public void initialize(StatusOrder constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Transactional
    @Override
    public boolean isValid(StatusProt status, ConstraintValidatorContext ctx) {
        if (status == null
            || status.getMeasmId() == null
            || status.getStatusMpId() == null
        ) {
            return true;
        }

        Repository repository = CDI.current().getBeanContainer()
            .createInstance().select(Repository.class).get();

        // Get the previous status
        QueryBuilder<StatusProt> lastFilter = repository
            .queryBuilder(StatusProt.class)
            .and("id", status.getId()).not()
            .and("measmId", status.getMeasmId())
            .orderBy("id", false);
        List<StatusProt> protos =
            repository.filter(lastFilter.getQuery(), 0, 1);
        if (protos.isEmpty()) {
            return true;
        }
        StatusProt last = protos.get(0);
        QueryBuilder<StatusOrdMp> folgeFilter = repository
            .queryBuilder(StatusOrdMp.class)
            .and("fromId", last.getStatusMpId())
            .and("toId", status.getStatusMpId());
        List<StatusOrdMp> reihenfolge =
            repository.filter(folgeFilter.getQuery());
        if (reihenfolge.isEmpty()) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(this.message)
                .addPropertyNode("statusMpId")
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
