/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

import de.intevation.lada.rest.LadaService;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;


/**
 * Activates JAX-RS and defines basic properties of the application.
 */
public class LadaApplication extends Application {

    /**
     * Get all service classes in the given package.
     *
     * Service classes are all classes extending LadaService
     * @param packageName Package name to check
     * @return Set of classes
     */
    protected Set<Class<?>> getServiceClasses(String packageName) {
        Set<Class<?>> classes = new HashSet<>();

        try (ScanResult packageResult = new ClassGraph().enableAllInfo()
                .acceptPackages(packageName).scan()) {
            classes.addAll(packageResult.getAllClasses()
                .filter(info -> info.extendsSuperclass(LadaService.class)
                    && !info.isInnerClass())
                .loadClasses());
        }
        return classes;
    }

    /**
     * Get all classes annotated with javax.ws.rs.ext.Provider
     *
     * Note: Scan results are limited to the de.intevation.lada package.
     * @return Set of classes
     */
    protected Set<Class<?>> getProviderClasses() {
        Set<Class<?>> classes = new HashSet<>();
        try (ScanResult packageResult = new ClassGraph().enableAllInfo()
                .acceptPackages("de.intevation.lada").scan()) {
            classes.addAll(packageResult.getAllClasses()
                .filter(info -> info.hasAnnotation(Provider.class))
                .loadClasses());
        }
        return classes;
    }
}
