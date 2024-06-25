/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import java.util.List;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import static org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel.BEAN_METHODS;

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
import de.intevation.lada.validation.Violation;


/**
 * Validation rule for status.
 */
public class ValidDependenciesValidator
    implements ConstraintValidator<ValidDependencies, StatusProt> {

    private String message;

    private Repository repository;

    private Validator<MeasVal> messwertValidator;
    private Validator<Measm> messungValidator;
    private Validator<Sample> probeValidator;
    private Validator<Site> ortValidator;

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
        this.messwertValidator = inst.select(
            new TypeLiteral<Validator<MeasVal>>() { }).get();
        this.messungValidator = inst.select(
            new TypeLiteral<Validator<Measm>>() { }).get();
        this.probeValidator = inst.select(
            new TypeLiteral<Validator<Sample>>() { }).get();
        this.ortValidator = inst.select(
            new TypeLiteral<Validator<Site>>() { }).get();

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
            Violation violationCollection = new Violation();
            Sample probe = repository.getById(
                Sample.class, messung.getSampleId());

            // init violation_collection with probe validation
            probeValidator.validate(probe);
            violationCollection.addErrors(probe.getErrors());
            violationCollection.addWarnings(probe.getWarnings());
            violationCollection.addNotifications(probe.getNotifications());

            //validate messung object
            messungValidator.validate(messung);
            violationCollection.addErrors(messung.getErrors());
            violationCollection.addWarnings(messung.getWarnings());
            violationCollection.addNotifications(messung.getNotifications());

            //validate messwert objects
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
                        violationCollection.addError(
                            "status", StatusCodes.STATUS_RO);
                    }

                    messwertValidator.validate(messwert);
                    violationCollection.addErrors(messwert.getErrors());
                    violationCollection.addWarnings(messwert.getWarnings());
                    violationCollection.addNotifications(
                        messwert.getNotifications());
                }
            } else if (newStatusWert != 7) {
                violationCollection.addError(
                    "measVal", StatusCodes.VALUE_MISSING);
            }

            // validate orte
            QueryBuilder<Geolocat> ortBuilder = repository
                .queryBuilder(Geolocat.class)
                .and(Geolocat_.sampleId, probe.getId());
            List<Geolocat> assignedOrte = repository.filter(
                ortBuilder.getQuery());
            for (Geolocat o : assignedOrte) {
                Site site = repository.getById(Site.class, o.getSiteId());
                ortValidator.validate(site);
                violationCollection.addErrors(site.getErrors());
                violationCollection.addWarnings(site.getWarnings());
                violationCollection.addNotifications(site.getNotifications());
            }

            if (newStatusWert != 7
                && (violationCollection.hasErrors()
                    || violationCollection.hasWarnings())
                || newStatusWert == 7
                && (probe.hasErrors() || probe.hasWarnings())
            ) {
                HibernateConstraintValidatorContext hibernateCtx = ctx.unwrap(
                    HibernateConstraintValidatorContext.class
                );
                hibernateCtx.disableDefaultConstraintViolation();
                hibernateCtx.addExpressionVariable(
                        "errs", violationCollection.getErrors())
                    .addExpressionVariable(
                        "wrns", violationCollection.getWarnings())
                    .addExpressionVariable(
                        "nots", violationCollection.getNotifications())
                    .buildConstraintViolationWithTemplate(this.message)
                    .enableExpressionLanguage(BEAN_METHODS)
                    .addPropertyNode("status")
                    .addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
