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
import jakarta.ws.rs.core.Response.Status;
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

    /**
     * Test whether a data object is locked or not.
     *
     * @param o The object to test.
     */
    @Override
    public void isLocked(Object o) {
        if (o instanceof Sample) {
            Sample newProbe = (Sample) o;
            Sample oldProbe = repository.getByIdPlain(
                Sample.class, newProbe.getId());
            if (oldProbe.getTreeMod().getTime()
                > newProbe.getTreeMod().getTime()) {
                throw new ClientErrorException(Status.CONFLICT);
            }
        } else {
            Method[] methods = o.getClass().getMethods();
            for (Method m: methods) {
                try {
                    if (m.getName().equals("getSampleId")) {
                        Integer id = (Integer) m.invoke(o);
                        Sample probe =
                            repository.getByIdPlain(Sample.class, id);
                        if (isNewer(o, probe.getTreeMod())) {
                            throw new ClientErrorException(Status.CONFLICT);
                        }
                        return;
                    }
                    if (m.getName().equals("getMeasmId")) {
                        Integer id = (Integer) m.invoke(o);
                        Measm messung =
                            repository.getByIdPlain(Measm.class, id);
                        if (isNewer(o, messung.getTreeMod())) {
                            throw new ClientErrorException(Status.CONFLICT);
                        }
                        return;
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
        NoSuchMethodException,
        IllegalAccessException,
        InvocationTargetException {
        Method m = o.getClass().getMethod("getParentModified");
        Date ot = (Date) m.invoke(o);
        if (ot == null) {
            return true;
        }
        return t.getTime() > ot.getTime();
    }
}
