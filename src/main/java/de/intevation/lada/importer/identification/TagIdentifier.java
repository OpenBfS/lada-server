/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.identification;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;

import de.intevation.lada.model.master.Tag;
import de.intevation.lada.model.master.Tag_;
import de.intevation.lada.util.data.Repository;


/**
 * Class to identify {@link Tag} objects.
 */
class TagIdentifier implements Identifier<Tag> {

    @Inject
    private Repository repository;

    @Override
    public Tag getExisting(Tag tag)
        throws IdentificationException {
        try {
            return repository.getSingle(repository.queryBuilder(Tag.class)
                .and(Tag_.name, tag.getName())
                .and(Tag_.networkId, tag.getNetworkId())
                .and(Tag_.measFacilId, tag.getMeasFacilId())
                .getQuery());
        } catch (NoResultException e) {
            return null;
        }
    }
}
