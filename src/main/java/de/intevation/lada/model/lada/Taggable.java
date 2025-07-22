/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.intevation.lada.model.master.Tag;

/**
 * Represents entities to which tags can be associated.
 */
public interface Taggable<T extends TagLink> {

    /**
     * Get managed tag associations.
     */
    public Set<T> getTagLinks();

    /**
     * Create {@link TagLink} representing association between this
     * {@code Taggable} and the given {@link Tag}. Should not actually
     * associate the tag.
     */
    public T createTagLink(Tag tag);

    /**
     * Get associated tags.
     */
    public List<Tag> getTags();

    /**
     * Set associated tags.
     */
    public void setTags(List<Tag> tags);

    /**
     * Associate tag.
     */
    default void addTag(Tag tag) {
        if (getTagLinks().add(createTagLink(tag))) {
            if (getTags() == null) {
                setTags(new ArrayList<>());
            }
            getTags().add(tag);
        }
    }
}
