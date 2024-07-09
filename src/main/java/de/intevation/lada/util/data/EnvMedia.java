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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;

import de.intevation.lada.model.master.EnvDescrip;
import de.intevation.lada.model.master.EnvDescripEnvMediumMp;


/**
 * Utilities for handling of environmental media and related data.
 */
public class EnvMedia {

    public static final String ENV_DESCRIP_LEVEL_FIELD_TPL = "s%02d";

    private static final int ENV_DESCRIP_LEVELS = 12;

    public static final String ENV_DESCRIP_PATTERN =
        "D:( [0-9][0-9]){" + ENV_DESCRIP_LEVELS + "}";

    private static final String ENV_DESCRIP_EMPTY = "00";

    private static final int ZEBS3 = 3;

    private static final int ZEBS5 = 5;

    private static final Map<Integer, Method> ENV_DESCRIP_LEVEL_GETTERS =
        new HashMap<>();
    static {
        try {
            for (int lev = 0; lev < ENV_DESCRIP_LEVELS; lev++) {
                ENV_DESCRIP_LEVEL_GETTERS.put(
                    lev,
                    new PropertyDescriptor(
                        String.format(ENV_DESCRIP_LEVEL_FIELD_TPL, lev),
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
     * Find the EnvDescrip IDs for a given environment descriptor.
     *
     * @param envDescripDisplay
     *
     * @return The list of EnvDescrip IDs matching the parameter or null
     * in case the parameter is invalid. For empty fields in the parameter
     * ("00"), -1 is added to the list as a pseudo ID.
     * @throws InvalidEnvDescripDisplayException
     */
    public List<Integer> findEnvDescripIds(String envDescripDisplay)
        throws InvalidEnvDescripDisplayException {
        if (envDescripDisplay == null) {
            throw new InvalidEnvDescripDisplayException();
        }
        String[] mediaDesk = envDescripDisplay.split(" ");
        if (mediaDesk.length <= 1 || ENV_DESCRIP_EMPTY.equals(mediaDesk[1])) {
            throw new InvalidEnvDescripDisplayException();
        }

        List<Integer> mediaIds = new ArrayList<Integer>();
        boolean zebs = "01".equals(mediaDesk[1]);
        Integer parent = null;
        Integer hdParent = null;
        Integer ndParent = null;
        for (int i = 1; i < mediaDesk.length; i++) {
            if (ENV_DESCRIP_EMPTY.equals(mediaDesk[i])) {
                mediaIds.add(-1);
                continue;
            }
            if (zebs && i < ZEBS5 || !zebs && i < ZEBS3) {
                parent = hdParent;
            } else {
                parent = ndParent;
            }
            QueryBuilder<EnvDescrip> builder = repository
                .queryBuilder(EnvDescrip.class)
                .and("levVal", mediaDesk[i])
                .and("lev", i - 1);
            if (parent != null) {
                builder.and("predId", parent);
            }
            try {
                Integer envDescripId = repository.getSingle(builder.getQuery())
                    .getId();
                hdParent = envDescripId;
                mediaIds.add(envDescripId);
                if (i == 2) {
                    ndParent = envDescripId;
                }
            } catch (NoResultException e) {
                throw new InvalidEnvDescripDisplayException(
                    String.format(ENV_DESCRIP_LEVEL_FIELD_TPL, i - 1));
            }
        }
        return mediaIds;
    }

    /**
     * Tests a list of EnvDescripEnvMediumMp instances for uniqueness
     * of associated envMedium IDs.
     * @param list List of EnvDescripEnvMediumMp instances
     * @return True if all envMedium IDs are equal, else false
     * @throws NullPointerException if list is empty
     */
    public static boolean isUnique(List<EnvDescripEnvMediumMp> list) {
        String element = list.get(0).getEnvMediumId();
        for (EnvDescripEnvMediumMp mp: list) {
            if (!element.equals(mp.getEnvMediumId())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get envDescrip ID referenced by given level and EnvDescripEnvMediumMp.
     * @param lev level
     * @param mp mapping
     * @return envDescrip ID
     */
    public static Integer getEnvDescripId(int lev, EnvDescripEnvMediumMp mp) {
        try {
            return (Integer) ENV_DESCRIP_LEVEL_GETTERS.get(lev).invoke(mp);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
