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

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import static org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel.BEAN_METHODS;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.MeasVal;
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
public class ValidDependenciesValidator
    implements ConstraintValidator<ValidDependencies, StatusProt> {

    private String message;

    private Repository repository;

    private Validator validator;

    private Map<String, Set<String>> errors = new HashMap<>();

    private Map<String, Set<String>> warnings = new HashMap<>();

    private Map<String, Set<String>> notifications = new HashMap<>();

    @Override
    public void initialize(ValidDependencies constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    @Transactional
    public boolean isValid(StatusProt status, ConstraintValidatorContext ctx) {
        if (status == null
            || status.getStatusMpId() == null
            || status.getMeasmId() == null
        ) {
            return true;
        }

        // Get instances programmatically because dependency injection
        // is not guaranteed to work in ConstraintValidator implementations
        Instance<Object> inst =
            CDI.current().getBeanContainer().createInstance();
        this.repository = inst.select(Repository.class).get();
        this.validator = inst.select(Validator.class).get();

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

            // init violation_collection with probe validation
            addMessages(validator.validate(probe));

            //validate messung object
            addMessages(validator.validate(messung));

            //validate messwert objects
            QueryBuilder<MeasVal> builder = repository
                .queryBuilder(MeasVal.class)
                .and("measmId", messung.getId());
            List<MeasVal> messwerte = repository.filter(
                builder.getQuery());
            if (!messwerte.isEmpty()) {
                for (MeasVal messwert: messwerte) {
                    if (newStatusWert == 7
                        && !(messwert.getMeasVal() == null
                            && messwert.getLessThanLOD() == null)
                    ) {
                        addError("status", StatusCodes.STATUS_RO);
                    }

                    addMessages(validator.validate(messwert));
                }
            } else if (newStatusWert != 7) {
                addError("measVal", StatusCodes.VALUE_MISSING);
            }

            // validate orte
            QueryBuilder<Geolocat> ortBuilder = repository
                .queryBuilder(Geolocat.class)
                .and("sampleId", probe.getId());
            List<Geolocat> assignedOrte = repository.filter(
                ortBuilder.getQuery());
            for (Geolocat o : assignedOrte) {
                Site site = repository.getById(Site.class, o.getSiteId());
                addMessages(validator.validate(site));
            }

            if (newStatusWert != 7
                && (!this.errors.isEmpty() || !this.warnings.isEmpty())
                || newStatusWert == 7
                && (probe.hasErrors() || probe.hasWarnings())
            ) {
                HibernateConstraintValidatorContext hibernateCtx = ctx.unwrap(
                    HibernateConstraintValidatorContext.class
                );
                hibernateCtx.disableDefaultConstraintViolation();
                hibernateCtx.addExpressionVariable("errs", this.errors)
                    .addExpressionVariable("wrns", this.warnings)
                    .addExpressionVariable("nots", this.notifications)
                    .buildConstraintViolationWithTemplate(this.message)
                    .enableExpressionLanguage(BEAN_METHODS)
                    .addPropertyNode("status")
                    .addConstraintViolation();
                return false;
            }
        }
        return true;
    }

    private void addError(String key, int value) {
        if (!this.errors.containsKey(key)) {
            this.errors.put(key, new HashSet<String>());
        }
        this.errors.get(key).add(String.valueOf(value));
    }

    private void addMessages(BaseModel validated) {
        addMessages(validated.getErrors(), this.errors);
        addMessages(validated.getWarnings(), this.warnings);
        addMessages(validated.getNotifications(), this.notifications);
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
