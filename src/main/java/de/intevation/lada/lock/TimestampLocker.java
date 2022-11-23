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
import java.sql.Timestamp;

import javax.inject.Inject;

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
     * @return True if the object is locked else false.
     */
    @Override
    public boolean isLocked(Object o) {
        if (o instanceof Sample) {
            Sample newProbe = (Sample) o;
            Sample oldProbe = repository.getByIdPlain(
                Sample.class, newProbe.getId());
            if (oldProbe.getTreeMod().getTime()
                > newProbe.getTreeMod().getTime()) {
                return true;
            }
        } else {
            Method[] methods = o.getClass().getMethods();
            for (Method m: methods) {
                if (m.getName().equals("getProbeId")) {
                    Integer id;
                    try {
                        id = (Integer) m.invoke(o);
                    } catch (IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException e) {
                        return true;
                    }
                    Sample probe = repository.getByIdPlain(Sample.class, id);
                    return isNewer(o, probe.getTreeMod());
                }
                if (m.getName().equals("getMessungsId")) {
                    Integer id;
                    try {
                        id = (Integer) m.invoke(o);
                    } catch (IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException e) {
                        return true;
                    }
                    Measm messung =
                        repository.getByIdPlain(Measm.class, id);
                    return isNewer(o, messung.getTreeMod());
                }
            }
        }
        return false;
    }

    /**
     * Test whether an object is newer than the given timestamp.
     *
     * @param o     The object to test.
     * @param t     The timestamp.
     * @return True if the object is newer.
     */
    private boolean isNewer(Object o, Timestamp t) {
        Method m;
        try {
            m = o.getClass().getMethod("getParentModified");
            Timestamp ot = (Timestamp) m.invoke(o);
            if (ot == null) {
                return true;
            }
            return t.getTime() > ot.getTime();
        } catch (NoSuchMethodException
            | SecurityException
            | IllegalAccessException
            | IllegalArgumentException
            | InvocationTargetException e) {
            return true;
        }
    }
}
