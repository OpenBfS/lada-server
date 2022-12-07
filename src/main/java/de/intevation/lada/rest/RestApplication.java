/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;

import de.intevation.lada.util.rest.LadaApplication;


/**
 * Base application for rest services.
 */
@ApplicationPath("/rest")
public class RestApplication extends LadaApplication {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.addAll(getServiceClasses(RestApplication.class.getPackageName()));
        classes.addAll(getProviderClasses());
        return classes;
    }
}
