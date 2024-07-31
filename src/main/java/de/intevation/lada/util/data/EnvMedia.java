/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.data;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.persistence.metamodel.SingularAttribute;

import de.intevation.lada.model.master.EnvDescrip;
import de.intevation.lada.model.master.EnvDescrip_;
import de.intevation.lada.model.master.EnvDescripEnvMediumMp;


/**
 * Utilities for handling of environmental media and related data.
 */
public class EnvMedia {

    public static final int ENV_DESCRIP_LEVELS = 12;

    public static final String ENV_DESCRIP_PATTERN =
        "D:(( [0-9][0-9]){" + ENV_DESCRIP_LEVELS + "})";

    private static final Pattern ENV_DESCRIP_PATTERN_COMPILED =
        Pattern.compile(ENV_DESCRIP_PATTERN);

    private static final String ENV_DESCRIP_EMPTY = "00";

    private static final int LEV_2 = 2;

    private static final int LEV_4 = 4;

    private static final Map<String, Method> ENV_DESCRIP_LEVEL_GETTERS =
        new HashMap<>(ENV_DESCRIP_LEVELS);
    static {
        try {
            for (int lev = 0; lev < ENV_DESCRIP_LEVELS; lev++) {
                String field = envDescripLevelFieldName(lev);
                ENV_DESCRIP_LEVEL_GETTERS.put(
                    field,
                    new PropertyDescriptor(
                        field,
                        EnvDescripEnvMediumMp.class).getReadMethod());
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    @Inject
    private Repository repository;

    /**
     * Indicates that an invalid envDescripDisplay has been passed
     * as an argument.
     */
    public static class InvalidEnvDescripDisplayException extends Exception {

        /**
         * The first invalid field of the envDescripDisplay.
         */
        private String field;

        InvalidEnvDescripDisplayException() { }

        InvalidEnvDescripDisplayException(String field) {
            this.field = field;
        }

        public String getField() {
            return this.field;
        }
    }

    /**
     * Find EnvMedium ID referenced by EnvDescripEnvMediumMp, which
     * has closest match to fiven map of EnvDescrip IDs.
     *
     * @param envDescripIds Map as returned by findEnvDescripIds
     * @param mappings List of EnvDescripEnvMediumMp instances
     * @return Found EnvMedium ID or null
     */
    public static String findEnvMediumId(
        Map<String, Integer> envDescripIds,
        List<EnvDescripEnvMediumMp> mappings
    ) {
        String found = null;
        int lastMatch = -ENV_DESCRIP_LEVELS;
        for (EnvDescripEnvMediumMp mp: mappings) {
            int matches = -ENV_DESCRIP_LEVELS;
            for (String field: envDescripIds.keySet()) {
                Integer medium = envDescripIds.get(field);
                Integer envDescripId = EnvMedia.getEnvDescripId(field, mp);
                if (medium != null && medium.equals(envDescripId)
                    || medium == null && envDescripId == null
                ) {
                    matches += 1;
                }
            }
            if (matches > lastMatch) {
                lastMatch = matches;
                found = mp.getEnvMediumId();
            }
        }
        return found;
    }

    /**
     * Find EnvDescripEnvMediumMp instances matching given map of levels
     * with associated envDescripIds.
     *
     * An EnvDescripEnvMediumMp is considered a match, if it references
     * the same or no EnvDescrip instance per level compared to the input.
     *
     * @param envDescripIds Map with level field names ("s00" ... "s11")
     * as keys and EnvDescrip IDs as values as returned by findEnvDescripIds.
     * @param matchEmpty If true, only those mappings are matched, which
     * do not reference an EnvDescrip on levels where the input parameter
     * does not reference one, i.e. has null values. Otherwise, a null value
     * in the input matches anything.
     * @return List of matching mappings
     */
    public List<EnvDescripEnvMediumMp> findEnvDescripEnvMediumMps(
        Map<String, Integer> envDescripIds,
        boolean matchEmpty
    ) {
        QueryBuilder<EnvDescripEnvMediumMp> builder =
            repository.queryBuilder(EnvDescripEnvMediumMp.class);
        for (String field: envDescripIds.keySet()) {
            SingularAttribute<EnvDescripEnvMediumMp, Integer> attr =
                EnvDescripEnvMediumMp.getSXXAttributeByName(field);
            if (envDescripIds.get(field) != null) {
                QueryBuilder<EnvDescripEnvMediumMp> tmp = builder
                    .getEmptyBuilder()
                    .and(attr, envDescripIds.get(field))
                    .or(attr, null);
                builder.and(tmp);
            } else if (matchEmpty) {
                builder.and(attr, null);
            }
        }
        return repository.filter(builder.getQuery());
    }

    /**
     * Find the EnvDescrip IDs for a given environment descriptor.
     *
     * @param envDescripDisplay
     *
     * @return Map with level field names ("s00" ... "s11") as keys and
     * EnvDescrip IDs matching the parameter.
     * For empty fields in the parameter ("00"), null is added as ID.
     * @throws InvalidEnvDescripDisplayException
     */
    public Map<String, Integer> findEnvDescripIds(String envDescripDisplay)
        throws InvalidEnvDescripDisplayException {
        if (envDescripDisplay == null) {
            throw new InvalidEnvDescripDisplayException();
        }
        Matcher m = ENV_DESCRIP_PATTERN_COMPILED.matcher(envDescripDisplay);
        if (!m.matches()) {
            throw new InvalidEnvDescripDisplayException();
        }

        String[] mediaDesk = m.group(1).strip().split(" ");
        if (ENV_DESCRIP_EMPTY.equals(mediaDesk[0])) {
            throw new InvalidEnvDescripDisplayException();
        }

        Map<String, Integer> mediaIds = new HashMap<>(ENV_DESCRIP_LEVELS);
        boolean zebs = "01".equals(mediaDesk[0]);
        Integer parent = null;
        Integer hdParent = null;
        Integer ndParent = null;
        for (int i = 0; i < ENV_DESCRIP_LEVELS; i++) {
            final String field = envDescripLevelFieldName(i);
            if (ENV_DESCRIP_EMPTY.equals(mediaDesk[i])) {
                mediaIds.put(field, null);
                continue;
            }
            if (zebs && i < LEV_4 || !zebs && i < LEV_2) {
                parent = hdParent;
            } else {
                parent = ndParent;
            }
            QueryBuilder<EnvDescrip> builder = repository
                .queryBuilder(EnvDescrip.class)
                .and(EnvDescrip_.levVal, Integer.parseInt(mediaDesk[i]))
                .and(EnvDescrip_.lev, i);
            if (parent != null) {
                builder.and(EnvDescrip_.predId, parent);
            }
            try {
                Integer envDescripId = repository.getSingle(builder.getQuery())
                    .getId();
                hdParent = envDescripId;
                mediaIds.put(field, envDescripId);
                if (i == 1) {
                    ndParent = envDescripId;
                }
            } catch (NoResultException e) {
                throw new InvalidEnvDescripDisplayException(
                    envDescripLevelFieldName(i));
            }
        }
        return mediaIds;
    }

    /**
     * Get envDescrip ID referenced at given level in EnvDescripEnvMediumMp.
     * @param lev level
     * @param mp mapping
     * @return envDescrip ID
     */
    public static Integer getEnvDescripId(
        int lev,
        EnvDescripEnvMediumMp mp
    ) {
        return getEnvDescripId(envDescripLevelFieldName(lev), mp);
    }

    /**
     * Get envDescrip ID referenced by given field in EnvDescripEnvMediumMp.
     * @param lev field name
     * @param mp mapping
     * @return envDescrip ID
     */
    public static Integer getEnvDescripId(
        String lev,
        EnvDescripEnvMediumMp mp
    ) {
        try {
            return (Integer) ENV_DESCRIP_LEVEL_GETTERS.get(lev).invoke(mp);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static String envDescripLevelFieldName(int lev) {
        return String.format("s%02d", lev);
    }
}
