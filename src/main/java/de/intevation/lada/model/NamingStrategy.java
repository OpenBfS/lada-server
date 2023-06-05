/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model;

import org.apache.commons.lang3.StringUtils;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.spi.MetadataBuildingContext;

/**
 * Custom naming strategy implementation converting between camelCase (Java)
 * and snake_case (database) names.
 */
public class NamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl {
    @Override
    protected Identifier toIdentifier(
        String stringForm, MetadataBuildingContext buildingContext
    ) {
        return super.toIdentifier(camelToSnake(stringForm), buildingContext);
    }

    /**
     * Convert camelCase to snake_case.
     *
     * @param camel Input string
     * @return Converted input string
     */
    public static String camelToSnake(String camel) {
        // Split input by "_" in case input is already (partly) in snake case
        final String snakeDelim = "_";
        String[] parts = camel.split(snakeDelim);
        //Convert each part into snake case
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            String[] words = StringUtils.splitByCharacterTypeCamelCase(part);
            // Names are not case sensitive, so leave the words capitalized
            parts[i] = String.join(snakeDelim, words);
        }
        return String.join(snakeDelim, parts).toLowerCase();
    }
}
