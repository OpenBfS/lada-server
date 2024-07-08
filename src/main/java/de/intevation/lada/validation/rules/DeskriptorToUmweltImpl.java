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
                "envMediumId", StatusCodes.VALUE_NOT_MATCHING);
            return violation;
        }

        boolean unique = isUnique(data);
        if (unique && umwId.equals(data.get(0).getEnvMediumId())) {
            return null;
        } else if (unique
            && !umwId.equals(data.get(0).getEnvMediumId())
            && datenbasisId != 4
        ) {
            Violation violation = new Violation();
            violation.addWarning(
                "envMediumId", StatusCodes.VALUE_NOT_MATCHING);
            return violation;
        } else if (!unique && (datenbasisId == 4 || datenbasisId == 1)) {
            if (data.size() != data.stream().filter(
                    element -> element.getEnvMediumId().equals(umwId)).count()
            ) {
                Violation violation = new Violation();
                violation.addNotification(
                    "envMediumId", StatusCodes.VALUE_NOT_MATCHING);
                return violation;
            } else {
                return null;
            }
        } else {
            int found = -1;
            int lastMatch = -12;
            for (int i = 0; i < data.size(); i++) {
                int matches = -12;
                for (int j = 0; j < 12; j++) {
                    switch (j) {
                        case 0: if (media.get(0).equals(data.get(i).getS00())
                                    || media.get(0).equals(-1)
                                    && data.get(i).getS00() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(0).equals(-1)
                                    && data.get(i).getS00() == null) {
                                    break;
                                } else {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        case 1: if (media.get(1).equals(data.get(i).getS01())
                                    || media.get(1).equals(-1)
                                    && data.get(i).getS01() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(1).equals(-1)
                                    && data.get(i).getS01() == null) {
                                    break;
                                } else {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        case 2: if (media.get(2).equals(data.get(i).getS02())
                                    || media.get(2).equals(-1)
                                    && data.get(i).getS02() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(2).equals(-1)
                                    && data.get(i).getS02() == null) {
                                    break;
                                } else {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        case 3: if (media.get(3).equals(data.get(i).getS03())
                                    || media.get(3).equals(-1)
                                    && data.get(i).getS03() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(3).equals(-1)
                                    && data.get(i).getS03() == null) {
                                    break;
                                } else {
                                    j = 12; matches = -12;
                                }
                                break;
                        case 4: if (media.get(4).equals(data.get(i).getS04())
                                    || media.get(4).equals(-1)
                                    && data.get(i).getS04() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(4).equals(-1)
                                    && data.get(i).getS04() == null) {
                                    break;
                                } else {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        case 5: if (media.get(5).equals(data.get(i).getS05())
                                    || media.get(5).equals(-1)
                                    && data.get(i).getS05() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(5).equals(-1)
                                    && data.get(i).getS05() == null) {
                                    break;
                                } else {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        case 6: if (media.get(6).equals(data.get(i).getS06())
                                    || media.get(6).equals(-1)
                                    && data.get(i).getS06() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(6).equals(-1)
                                    && data.get(i).getS06() == null) {
                                    break;
                                } else {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        case 7: if (media.get(7).equals(data.get(i).getS07())
                                    || media.get(7).equals(-1)
                                    && data.get(i).getS07() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(7).equals(-1)
                                    && data.get(i).getS07() == null) {
                                    break;
                                } else {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        case 8: if (media.get(8).equals(data.get(i).getS08())
                                    || media.get(8).equals(-1)
                                    && data.get(i).getS08() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(8).equals(-1)
                                    && data.get(i).getS08() == null) {
                                    break;
                                } else {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        case 9: if (media.get(9).equals(data.get(i).getS09())
                                    || media.get(9).equals(-1)
                                    && data.get(i).getS09() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(9).equals(-1)
                                    && data.get(i).getS09() == null) {
                                    break;
                                } else  {
                                    j = 12;
                                    matches = -12;
                                }
                                break;
                        case 10: if (media.get(10).equals(data.get(i).getS10())
                                    || media.get(10).equals(-1)
                                    && data.get(i).getS10() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(10).equals(-1)
                                    && data.get(i).getS10() == null) {
                                    break;
                                } else {
                                    j = 12; matches = -12;
                                }
                                break;
                        case 11: if (media.get(11).equals(data.get(i).getS11())
                                    || media.get(11).equals(-1)
                                    && data.get(i).getS11() == null
                                ) {
                                    matches += 1;
                                } else if (!media.get(11).equals(-1)
                                    && data.get(i).getS11() == null) {
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
                    found = i;
                }
            }
            if (found >= 0 && data.get(found).getEnvMediumId().equals(umwId)) {
                return null;
            }
            Violation violation = new Violation();
            violation.addWarning(
                "envMediumId", StatusCodes.VALUE_NOT_MATCHING);
            return violation;
        }
    }

    private boolean isUnique(List<EnvDescripEnvMediumMp> list) {
        if (list.isEmpty()) {
            return false;
        }
        String element = list.get(0).getEnvMediumId();
        for (int i = 1; i < list.size(); i++) {
            if (!element.equals(list.get(i).getEnvMediumId())) {
                return false;
            }
        }
        return true;
    }
}
