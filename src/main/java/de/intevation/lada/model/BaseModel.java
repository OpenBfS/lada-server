/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model;

import javax.json.bind.annotation.JsonbTransient;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Provides shared attributes for model entities.
 */
public abstract class BaseModel {
    private MultivaluedMap<String, Integer> errors =
        new MultivaluedHashMap<>();

    private MultivaluedMap<String, Integer> warnings =
        new MultivaluedHashMap<>();

    private MultivaluedMap<String, Integer> notifications =
        new MultivaluedHashMap<>();

    public MultivaluedMap<String, Integer> getErrors() {
        return this.errors;
    }

    @JsonbTransient
    public void setErrors(MultivaluedMap<String, Integer> errors) {
        this.errors = errors;
    }

    public MultivaluedMap<String, Integer> getWarnings() {
        return this.warnings;
    }

    @JsonbTransient
    public void setWarnings(MultivaluedMap<String, Integer> warnings) {
        this.warnings = warnings;
    }

    public MultivaluedMap<String, Integer> getNotifications() {
        return this.notifications;
    }

    @JsonbTransient
    public void setNotifications(
        MultivaluedMap<String, Integer> notifications
    ) {
        this.notifications = notifications;
    }

    /**
     * Add given validation errors to this object's errors.
     *
     * @param e Errors to add
     */
    public void addErrors(MultivaluedMap<String, Integer> e) {
        for (String key: e.keySet()) {
            this.errors.addAll(key, e.get(key));
        }
    }

    /**
     * Add given validation warnings to this object's warnings.
     *
     * @param w Warnings to add
     */
    public void addWarnings(MultivaluedMap<String, Integer> w) {
        for (String key: w.keySet()) {
            this.warnings.addAll(key, w.get(key));
        }
    }

    /**
     * Add given validation notifications to this object's notifications.
     *
     * @param n Notifications to add
     */
    public void addNotifications(MultivaluedMap<String, Integer> n) {
        for (String key: n.keySet()) {
            this.notifications.addAll(key, n.get(key));
        }
    }

    /**
     * Check whether this object has any validation errors associated.
     *
     * @return true if errors are associated
     */
    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    /**
     * Check whether this object has any validation errors associated.
     *
     * @return true if warnings are associated
     */
    public boolean hasWarnings() {
        return !this.warnings.isEmpty();
    }

    /**
     * Check whether this object has any validation errors associated.
     *
     * @return true if notifications are associated
     */
    public boolean hasNotifications() {
        return !this.notifications.isEmpty();
    }
}
