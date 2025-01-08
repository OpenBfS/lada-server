/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.lock;

import java.util.Date;

import de.intevation.lada.model.lada.BelongsToMeasm;
import de.intevation.lada.model.lada.Measm;


/**
 * Data object locker using a timestamp to lock data access.
 */
public class BelongsToMeasmLocker extends TimestampLocker<BelongsToMeasm> {

    @Override
    boolean checkIsLocked(BelongsToMeasm o) {
        Date ot = o.getParentModified();
        Measm measm = repository.getById(Measm.class, o.getMeasmId());
        return ot == null || measm.getTreeMod().getTime() > ot.getTime();
    }
}
