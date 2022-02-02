/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model;

import java.util.List;

import de.intevation.lada.model.stammdaten.Tag;

/**
 * Persistent class containing tag definitions, used for
 * executing Queries.
 */
public class Tags {

    private List<Tag> tags;

    public Tags() { }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
