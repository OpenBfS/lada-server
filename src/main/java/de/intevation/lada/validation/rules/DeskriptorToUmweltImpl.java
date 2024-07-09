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
            if (data.size() != data.stream().filter(
                    element -> element.getEnvMediumId().equals(umwId)).count()
            ) {
                Violation violation = new Violation();
                violation.addNotification(
                    violationKey, StatusCodes.VALUE_NOT_MATCHING);
                return violation;
            }
            return null;
        } else {
            String found = null;
            int lastMatch = -12;
            for (EnvDescripEnvMediumMp mp: data) {
                int matches = -12;
                for (int j = 0; j < 12; j++) {
                    switch (j) {
                        case 0: if (media.get(0).equals(mp.getS00())
                                    || media.get(0).equals(-1)
                                    && mp.getS00() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(0).equals(-1)
                                    && mp.getS00() == null) {
                                    break;
                                } else {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        case 1: if (media.get(1).equals(mp.getS01())
                                    || media.get(1).equals(-1)
                                    && mp.getS01() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(1).equals(-1)
                                    && mp.getS01() == null) {
                                    break;
                                } else {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        case 2: if (media.get(2).equals(mp.getS02())
                                    || media.get(2).equals(-1)
                                    && mp.getS02() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(2).equals(-1)
                                    && mp.getS02() == null) {
                                    break;
                                } else {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        case 3: if (media.get(3).equals(mp.getS03())
                                    || media.get(3).equals(-1)
                                    && mp.getS03() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(3).equals(-1)
                                    && mp.getS03() == null) {
                                    break;
                                } else {
                                    j = 12; matches = -12;
                                }
                                break;
                        case 4: if (media.get(4).equals(mp.getS04())
                                    || media.get(4).equals(-1)
                                    && mp.getS04() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(4).equals(-1)
                                    && mp.getS04() == null) {
                                    break;
                                } else {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        case 5: if (media.get(5).equals(mp.getS05())
                                    || media.get(5).equals(-1)
                                    && mp.getS05() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(5).equals(-1)
                                    && mp.getS05() == null) {
                                    break;
                                } else {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        case 6: if (media.get(6).equals(mp.getS06())
                                    || media.get(6).equals(-1)
                                    && mp.getS06() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(6).equals(-1)
                                    && mp.getS06() == null) {
                                    break;
                                } else {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        case 7: if (media.get(7).equals(mp.getS07())
                                    || media.get(7).equals(-1)
                                    && mp.getS07() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(7).equals(-1)
                                    && mp.getS07() == null) {
                                    break;
                                } else {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        case 8: if (media.get(8).equals(mp.getS08())
                                    || media.get(8).equals(-1)
                                    && mp.getS08() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(8).equals(-1)
                                    && mp.getS08() == null) {
                                    break;
                                } else {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        case 9: if (media.get(9).equals(mp.getS09())
                                    || media.get(9).equals(-1)
                                    && mp.getS09() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(9).equals(-1)
                                    && mp.getS09() == null) {
                                    break;
                                } else  {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        case 10: if (media.get(10).equals(mp.getS10())
                                    || media.get(10).equals(-1)
                                    && mp.getS10() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(10).equals(-1)
                                    && mp.getS10() == null) {
                                    break;
                                } else {
                                    j = 12; matches = -12;
                                }
                                break;
                        case 11: if (media.get(11).equals(mp.getS11())
                                    || media.get(11).equals(-1)
                                    && mp.getS11() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(11).equals(-1)
                                    && mp.getS11() == null) {
                                    break;
                                } else {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        default:
                            // Should not happen
                            throw new IndexOutOfBoundsException();
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
