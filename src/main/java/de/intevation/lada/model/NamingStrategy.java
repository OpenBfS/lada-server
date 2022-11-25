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
import org.hibernate.boot.model.naming.ImplicitJoinColumnNameSource;
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
        String[] words = StringUtils.splitByCharacterTypeCamelCase(stringForm);
        // Names are not case sensitive, so leave the words capitalized
        String snake = String.join("_", words);
        return super.toIdentifier(snake, buildingContext);
    }

    /**
     * Determine join column name from the give source.
     *
     * The join column name is generated using the following format:
     *
     * {name_of_the_referenced_entity}_{referenced_entity_primary_key}
     *
     * and is returned in snake_case
     */
    @Override
    public Identifier determineJoinColumnName(ImplicitJoinColumnNameSource source) {
        final String name;

        //Generate camel case representation of the join column
        if ( source.getNature() == ImplicitJoinColumnNameSource.Nature.ELEMENT_COLLECTION
                || source.getAttributePath() == null ) {
            name = transformEntityName( source.getEntityNaming() )
                    + StringUtils.capitalize(source.getReferencedColumnName().getText());
        }
        else {
            name = transformAttributePath( source.getAttributePath() )
                    + StringUtils.capitalize(source.getReferencedColumnName().getText());
        }
        //Transform to snake case identifier
        return toIdentifier( name, source.getBuildingContext() );
    }
}
