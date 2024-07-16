/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules;

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import de.intevation.lada.model.master.EnvDescripEnvMediumMp;
import de.intevation.lada.util.data.EnvMedia;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;


/**
 * Validates if the umwelt id fits the deskriptor string.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public abstract class DeskriptorToUmweltImpl implements Rule {

    @Inject
    private EnvMedia envMediaUtil;

    @Override
    public abstract Violation execute(Object object);

    protected Violation doExecute(
        String envDescripDisplay,
        String umwId,
        Integer regulationId
    ) {
        Map<String, Integer> media;
        try {
            media = envMediaUtil.findEnvDescripIds(envDescripDisplay);
        } catch (EnvMedia.InvalidEnvDescripDisplayException e) {
            // Leave validation of combination of levels up to other constraint
            return null;
        }

        final boolean isREIor161 = regulationId != null
            && (regulationId == 4 || regulationId == 1);

        Violation violation = new Violation();
        violation.addWarning("envMediumId", StatusCodes.VALUE_NOT_MATCHING);

        List<EnvDescripEnvMediumMp> data =
            envMediaUtil.findEnvDescripEnvMediumMps(media, !isREIor161);
        if (isREIor161) {
            // Any mapping should match envMediumId
            if (data.stream()
                .filter(mp -> umwId.equals(mp.getEnvMediumId()))
                .count() == 0
            ) {
                return violation;
            }
        } else if (!umwId.equals(EnvMedia.findEnvMediumId(media, data))) {
            // The most closely matching mapping should match envMediumId
            return violation;
        }
        return null;
    }
}
