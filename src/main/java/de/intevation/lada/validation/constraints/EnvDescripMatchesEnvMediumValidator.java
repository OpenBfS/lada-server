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

import de.intevation.lada.model.master.EnvDescripEnvMediumMp;
import de.intevation.lada.util.data.EnvMedia;


/**
 * Validates if envMediumId fits the descriptor string.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public abstract class EnvDescripMatchesEnvMediumValidator<T>
    implements ConstraintValidator<EnvDescripMatchesEnvMedium, T> {

    private static final int REG_161 = 1;
    private static final int REG_REI = 4;

    protected String message;

    @Override
    public void initialize(EnvDescripMatchesEnvMedium constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public abstract boolean isValid(T object, ConstraintValidatorContext ctx);

    @Transactional
    protected boolean doValidation(
        ConstraintValidatorContext ctx,
        String propertyNode,
        String envDescripDisplay,
        String umwId,
        Integer regulationId
    ) {
        if (umwId == null) {
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

        final boolean isREIor161 = regulationId != null
            && (regulationId == REG_REI || regulationId == REG_161);

        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(this.message)
            .addPropertyNode(propertyNode)
            .addConstraintViolation();

        List<EnvDescripEnvMediumMp> data =
            envMediaUtil.findEnvDescripEnvMediumMps(media, !isREIor161);
        if (isREIor161) {
            // Any mapping should match envMediumId
            if (data.stream()
                .filter(mp -> umwId.equals(mp.getEnvMediumId()))
                .count() == 0
            ) {
                return false;
            }
        } else if (!umwId.equals(EnvMedia.findEnvMediumId(media, data))) {
            // The most closely matching mapping should match envMediumId
            return false;
        }
        return true;
    }
}
