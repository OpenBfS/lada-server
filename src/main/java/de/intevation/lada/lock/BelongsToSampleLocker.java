/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.lock;

import java.util.Date;

import de.intevation.lada.model.lada.BelongsToSample;


/**
 * Data object locker using a timestamp to lock data access.
 */
public class BelongsToSampleLocker extends TimestampLocker<BelongsToSample> {

    @Override
    boolean checkIsLocked(BelongsToSample o) {
        Date ot = o.getParentModified();
        return ot == null
            || o.getSample().getTreeMod().getTime() > ot.getTime();
    }
}
