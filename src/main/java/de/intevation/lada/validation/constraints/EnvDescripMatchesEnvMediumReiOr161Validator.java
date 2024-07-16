/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
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
 * Validates that EnvMedium unambiguously matches the envDescripDisplay
 * in REI and §161 contexts.
 *
 * @param <T> The type which a concrete implementation validates.
 */
public abstract class EnvDescripMatchesEnvMediumReiOr161Validator<T>
    implements ConstraintValidator<EnvDescripMatchesEnvMediumReiOr161, T> {

    private static final int REG_161 = 1;
    private static final int REG_REI = 4;

    protected String message;

    @Override
    public void initialize(
        EnvDescripMatchesEnvMediumReiOr161 constraintAnnotation
    ) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public abstract boolean isValid(T object, ConstraintValidatorContext ctx);

    @Transactional
    protected boolean doValidation(
        Integer regulationId,
        String envDescripDisplay,
        String envMediumId
    ) {
        if (regulationId == null
            || envMediumId == null
            || regulationId != null
            && regulationId != REG_161
            && regulationId != REG_REI
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

        List<EnvDescripEnvMediumMp> mappings =
            envMediaUtil.findEnvDescripEnvMediumMps(media, false);
        int match = 0, notMatch = 0;
        for (EnvDescripEnvMediumMp mp: mappings) {
            if (envMediumId.equals(mp.getEnvMediumId())) {
                match++;
            } else {
                notMatch++;
            }
            if (match > 0 && notMatch > 0) {
                // There are mappings matching envMediumId
                // as well as non-matching ones
                return false;
            }
        }
        // Leave no match at all up to other constraint
        return true;
    }
}
