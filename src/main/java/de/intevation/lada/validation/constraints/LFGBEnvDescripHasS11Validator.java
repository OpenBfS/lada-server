/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import java.util.List;
import java.util.Map;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.Sample_;
import de.intevation.lada.model.master.EnvDescrip;
import de.intevation.lada.util.data.EnvMedia;
import de.intevation.lada.util.data.Repository;


/**
 * Validates if descriptor S11 is given for regulation "LFGB".
 */
public class LFGBEnvDescripHasS11Validator
    implements ConstraintValidator<LFGBEnvDescripHasS11, Sample> {

    private static final int REG_LFGB = 15;

    private String message;

    @Override
    public void initialize(LFGBEnvDescripHasS11 constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Transactional
    @Override
    public boolean isValid(
        Sample sample, ConstraintValidatorContext ctx
    ) {
        if (sample == null) {
            return true;
        }
        Integer regulationId = sample.getRegulationId();
        String envDescripDisplay = sample.getEnvDescripDisplay();
        if (regulationId == null
            || regulationId != REG_LFGB
            || envDescripDisplay == null
        ) {
            return true;
        }

        EnvMedia envMediaUtil = CDI.current().getBeanContainer()
            .createInstance().select(EnvMedia.class).get();
        Map<String, Integer> media;
        try {
            media = envMediaUtil.findEnvDescripIds(envDescripDisplay);
        } catch (EnvMedia.InvalidEnvDescripDisplayException e) {
            // Leave validation of combination of levels up to other constraint
            return true;
        }

        Integer s0Id = media.get("s00");
                if (s0Id == null) {
            return true;
        }
        EnvDescrip s0 = CDI.current().getBeanContainer()
            .createInstance().select(Repository.class).get()
            .entityManager().find(EnvDescrip.class, s0Id);
        if (s0 != null
            && List.of(1, 2, 18).contains(s0.getLevVal())
            && media.get("s11") == null
        ) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(this.message)
                .addPropertyNode(Sample_.ENV_DESCRIP_DISPLAY)
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
