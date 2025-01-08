/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.lock;

import de.intevation.lada.model.lada.Sample;


/**
 * Data object locker using a timestamp to lock data access.
 */
public class SampleLocker extends TimestampLocker<Sample> {

    @Override
    boolean checkIsLocked(Sample newProbe) {
        Sample oldProbe = repository.getById(
            Sample.class, newProbe.getId());
        return oldProbe.getTreeMod().getTime()
            > newProbe.getTreeMod().getTime();
    }
}
