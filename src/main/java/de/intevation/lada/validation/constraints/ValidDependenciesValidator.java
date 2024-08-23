/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import static org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel.BEAN_METHODS;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.Geolocat_;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;


/**
 * Validation rule for status.
 */
public abstract class ValidDependenciesValidator {

    protected boolean doValidation(
        StatusProt status,
        ConstraintValidatorContext ctx,
        String message,
        boolean checkNotifications
    ) {
        if (status == null
            || status.getStatusMpId() == null
            || status.getMeasmId() == null
        ) {
            return true;
        }

        // Get instance programmatically because dependency injection
        // is not guaranteed to work in ConstraintValidator implementations
        Repository repository = CDI.current().getBeanContainer()
            .createInstance().select(Repository.class).get();

        StatusMp newKombi = repository.entityManager().find(
            StatusMp.class, status.getStatusMpId());
        Measm messung = repository.entityManager().find(
            Measm.class, status.getMeasmId());
        if (newKombi == null || messung == null) {
            return true;
        }

        int newStatusWert = newKombi.getStatusVal().getId();
        if (newStatusWert == 1
            || newStatusWert == 2
            || newStatusWert == 7
        ) {
            Sample probe = repository.getById(
                Sample.class, messung.getSampleId());

            Validator validator = new Validator();

            Map<String, Set<String>> errors = new HashMap<>();
            Map<String, Set<String>> warnings = new HashMap<>();
            Map<String, Set<String>> notifications = new HashMap<>();

            // Validate sample
            addMessages(
                validator.validate(probe), errors, warnings, notifications);

            // Validate Measm
            addMessages(
                validator.validate(messung), errors, warnings, notifications);

            // Validate measVals
            QueryBuilder<MeasVal> builder = repository
                .queryBuilder(MeasVal.class)
                .and(MeasVal_.measmId, messung.getId());
            List<MeasVal> messwerte = repository.filter(
                builder.getQuery());
            if (!messwerte.isEmpty()) {
                for (MeasVal messwert: messwerte) {
                    if (newStatusWert == 7
                        && !(messwert.getMeasVal() == null
                            && messwert.getLessThanLOD() == null)
                    ) {
                        addError("status", StatusCodes.STATUS_RO, errors);
                    }

                    addMessages(
                        validator.validate(messwert),
                        errors,
                        warnings,
                        notifications);
                }
            } else if (newStatusWert != 7) {
                addError("measVal", StatusCodes.VALUE_MISSING, errors);
            }

            // Validate sites
            QueryBuilder<Geolocat> ortBuilder = repository
                .queryBuilder(Geolocat.class)
                .and(Geolocat_.sampleId, probe.getId());
            List<Geolocat> assignedOrte = repository.filter(
                ortBuilder.getQuery());
            for (Geolocat o : assignedOrte) {
                Site site = repository.getById(Site.class, o.getSiteId());
                addMessages(
                    validator.validate(site),
                    errors,
                    warnings,
                    notifications);
            }

            HibernateConstraintValidatorContext hibernateCtx = ctx.unwrap(
                HibernateConstraintValidatorContext.class
            );
            hibernateCtx.disableDefaultConstraintViolation();
            hibernateCtx.addExpressionVariable("errs", errors)
                .addExpressionVariable("wrns", warnings)
                .addExpressionVariable("nots", notifications)
                .buildConstraintViolationWithTemplate(message)
                .enableExpressionLanguage(BEAN_METHODS)
                .addPropertyNode("status")
                .addConstraintViolation();

            if (checkNotifications) {
                return notifications.isEmpty();
            }

            return !(newStatusWert != 7
                && (!errors.isEmpty() || !warnings.isEmpty())
                || newStatusWert == 7
                && (probe.hasErrors() || probe.hasWarnings()));
        }
        return true;
    }

    private void addError(
        String key, int value, Map<String, Set<String>> errors
    ) {
        if (!errors.containsKey(key)) {
            errors.put(key, new HashSet<String>());
        }
        errors.get(key).add(String.valueOf(value));
    }

    private void addMessages(
        BaseModel validated,
        Map<String, Set<String>> errors,
        Map<String, Set<String>> warnings,
        Map<String, Set<String>> notifications
    ) {
        addMessages(validated.getErrors(), errors);
        addMessages(validated.getWarnings(), warnings);
        addMessages(validated.getNotifications(), notifications);
    }

    private void addMessages(
        Map<String, Set<String>> from,
        Map<String, Set<String>> to
    ) {
        for (String key: from.keySet()) {
            if (to.containsKey(key)) {
                to.get(key).addAll(from.get(key));
            } else {
                to.put(key, from.get(key));
            }
        }
    }
}
