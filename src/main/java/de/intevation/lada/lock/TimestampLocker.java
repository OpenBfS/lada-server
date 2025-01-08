/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.lock;

import jakarta.inject.Inject;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

import de.intevation.lada.i18n.I18n;
import de.intevation.lada.util.data.Repository;


/**
 * Data object locker using a timestamp to lock data access.
 *
 * @param <T> Type of object to be tested
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public abstract class TimestampLocker<T> {

    /**
     * The repository used to read data.
     */
    @Inject
    Repository repository;

    @Inject
    private I18n i18n;

    /**
     * Test whether a data object is locked or not.
     *
     * @param o The object to test.
     * @throws ClientErrorException if object is locked
     */
    public void isLocked(T o) {
        if (checkIsLocked(o)) {
            throw new ClientErrorException(Response
                .status(Response.Status.CONFLICT)
                .entity(i18n.getString("dataset_changed")).build());
        }
        return;
    }

    abstract boolean checkIsLocked(T o);
}
