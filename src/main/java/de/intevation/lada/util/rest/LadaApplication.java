/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.rest;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.google.common.reflect.ClassPath;


/**
 * Activates JAX-RS and defines basic properties of the application.
 */
public class LadaApplication extends Application {

    protected Set<Class<?>> getClassesInPackage(String packageName) {
        ClassPath cp;
        try {
            cp = ClassPath.from(LadaApplication.class.getClassLoader());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        Set<ClassPath.ClassInfo> infos = cp.getTopLevelClasses(packageName);
        Set<Class<?>> classes = new HashSet<>();
        infos.forEach(info -> classes.add(info.load()));
        return classes;
    }
}
