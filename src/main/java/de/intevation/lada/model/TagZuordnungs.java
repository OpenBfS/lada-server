/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.model;

import java.util.List;

import de.intevation.lada.model.land.TagZuordnung;

/**
 * Persistent class containing tag definitions, used for
 * processing Requests.
 */
public class TagZuordnungs {
    private List<TagZuordnung> tagZuordnungs;

    public List<TagZuordnung> getTagZuordnungs() {
        return tagZuordnungs;
    }

    public void setTagZuordnungs(List<TagZuordnung> tagZuordnungs) {
        this.tagZuordnungs = tagZuordnungs;
    }
}
