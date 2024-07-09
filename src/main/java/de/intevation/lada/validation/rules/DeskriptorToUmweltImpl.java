/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules;

import java.util.List;

import jakarta.inject.Inject;

import de.intevation.lada.model.master.EnvDescripEnvMediumMp;
import de.intevation.lada.util.data.EnvMedia;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;


/**
 * Validates if the umwelt id fits the deskriptor string.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public abstract class DeskriptorToUmweltImpl implements Rule {

    private static final String FIELD_NAME_TEMPLATE = "s%02d";

    @Inject
    private Repository repository;

    @Inject
    private EnvMedia envMediaUtil;

    @Override
    public abstract Violation execute(Object object);

    protected Violation doExecute(
        String envDescripDisplay,
        String umwId,
        Integer datenbasisId
    ) {
        List<Integer> media;
        try {
            media = envMediaUtil.findEnvDescripIds(envDescripDisplay);
        } catch (EnvMedia.InvalidEnvDescripDisplayException e) {
            // Leave validation of combination of levels up to other constraint
            return null;
        }

        final String violationKey = "envMediumId";

        QueryBuilder<EnvDescripEnvMediumMp> builder =
            repository.queryBuilder(EnvDescripEnvMediumMp.class);
        for (int i = 0; i < media.size(); i++) {
            String field = String.format(FIELD_NAME_TEMPLATE, i);
            if (media.get(i) != -1) {
                QueryBuilder<EnvDescripEnvMediumMp> tmp = builder
                    .getEmptyBuilder()
                    .and(field, media.get(i))
                    .or(field, null);
                builder.and(tmp);
            } else {
                if (datenbasisId != null
                    && datenbasisId != 4
                    && datenbasisId != 1
                ) {
                    builder.and(field, null);
                }
            }
        }
        List<EnvDescripEnvMediumMp> data = repository.filter(
            builder.getQuery());
        if (data.isEmpty()) {
            Violation violation = new Violation();
            violation.addWarning(
                violationKey, StatusCodes.VALUE_NOT_MATCHING);
            return violation;
        }

        boolean unique = EnvMedia.isUnique(data);
        if (unique && umwId.equals(data.get(0).getEnvMediumId())) {
            return null;
        } else if (unique
            && !umwId.equals(data.get(0).getEnvMediumId())
            && datenbasisId != 4
        ) {
            Violation violation = new Violation();
            violation.addWarning(
                violationKey, StatusCodes.VALUE_NOT_MATCHING);
            return violation;
        } else if (!unique && (datenbasisId == 4 || datenbasisId == 1)) {
            Violation violation = new Violation();
            violation.addNotification(
                violationKey, StatusCodes.VALUE_NOT_MATCHING);
            return violation;
        } else {
            final int nLevels = 12;
            String found = null;
            int lastMatch = -nLevels;
            for (EnvDescripEnvMediumMp mp: data) {
                int matches = -nLevels;
                for (int j = 0; j < nLevels; j++) {
                    Integer medium = media.get(j);
                    Integer envDescripId = EnvMedia.getEnvDescripId(j, mp);
                    if (medium.equals(envDescripId)
                        || medium.equals(-1) && envDescripId == null
                    ) {
                        matches += 1;
                    } else if (!medium.equals(-1) && envDescripId == null) {
                        continue;
                    } else {
                        matches = -nLevels;
                        break;
                    }
                }
                if (matches > lastMatch) {
                    lastMatch = matches;
                    found = mp.getEnvMediumId();
                }
            }
            if (umwId.equals(found)) {
                return null;
            }
            Violation violation = new Violation();
            violation.addWarning(
                violationKey, StatusCodes.VALUE_NOT_MATCHING);
            return violation;
        }
    }
}
