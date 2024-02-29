/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.lock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import jakarta.inject.Inject;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

import de.intevation.lada.i18n.I18n;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.data.Repository;


/**
 * Data object locker using a timestamp to lock data access.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@LockConfig(type = LockType.TIMESTAMP)
public class TimestampLocker implements ObjectLocker {

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
     */
    @Override
    public void isLocked(Object o) {
        if (checkIsLocked(o)) {
            throw new ClientErrorException(Response
                .status(Response.Status.CONFLICT)
                .entity(i18n.getString("dataset_changed")).build());
        }
        return;
    }

    private boolean checkIsLocked(Object o) {
        if (o instanceof Sample) {
            Sample newProbe = (Sample) o;
            Sample oldProbe = repository.getByIdPlain(
                Sample.class, newProbe.getId());
            return oldProbe.getTreeMod().getTime()
                > newProbe.getTreeMod().getTime();
        } else {
            try {
                try {
                    Method m = o.getClass().getMethod("getSampleId");
                    Integer id = (Integer) m.invoke(o);
                    Sample probe =
                        repository.getByIdPlain(Sample.class, id);
                    return isNewer(o, probe.getTreeMod());
                } catch (NoSuchMethodException e) {
                    Method m = o.getClass().getMethod("getMeasmId");
                    Integer id = (Integer) m.invoke(o);
                    Measm messung =
                        repository.getByIdPlain(Measm.class, id);
                    return isNewer(o, messung.getTreeMod());
                }
            } catch (NoSuchMethodException
                | IllegalAccessException
                | InvocationTargetException e
                ) {
                // TODO: Use types instead of reflection
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Test whether an object is newer than the given timestamp.
     *
     * @param o     The object to test.
     * @param t     The timestamp.
     * @return True if the object is newer.
     */
    private boolean isNewer(Object o, Date t) throws
        // TODO: Use types instead of reflection
        IllegalAccessException,
        InvocationTargetException {
        Method m;
        try {
            m = o.getClass().getMethod("getParentModified");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        Date ot = (Date) m.invoke(o);
        if (ot == null) {
            return true;
        }
        return t.getTime() > ot.getTime();
    }
}
