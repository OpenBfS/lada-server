/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.json.bind.annotation.JsonbTransient;

/**
 * Provides shared attributes for model entities.
 */
public abstract class BaseModel {
    private Map<String, List<Integer>> errors
        = new HashMap<String, List<Integer>>();
    private Map<String, List<Integer>> warnings
        = new HashMap<String, List<Integer>>();
    private Map<String, List<Integer>> notifications
        = new HashMap<String, List<Integer>>();

    public Map<String, List<Integer>>getErrors() {
        return this.errors;
    }

    @JsonbTransient
    public void setErrors(Map<String, List<Integer>> errors) {
        this.errors = errors;
    }

    public Map<String, List<Integer>> getWarnings() {
        return this.warnings;
    }

    @JsonbTransient
    public void setWarnings(Map<String, List<Integer>> warnings) {
        this.warnings = warnings;
    }

    public Map<String, List<Integer>> getNotifications() {
        return this.notifications;
    }

    @JsonbTransient
    public void setNotifications(
        Map<String, List<Integer>> notifications
    ) {
        this.notifications = notifications;
    }

    /**
     * Add given validation errors to this object's errors.
     *
     * @param e Errors to add
     */
    public void addErrors(Map<String, List<Integer>> e) {
        for (String key: e.keySet()) {
            if (this.errors.containsKey(key)) {
                this.errors.get(key).addAll(e.get(key));
            } else {
                this.errors.put(key, e.get(key));
            }
        }
    }

    /**
     * Add given validation warnings to this object's warnings.
     *
     * @param w Warnings to add
     */
    public void addWarnings(Map<String, List<Integer>> w) {
        for (String key: w.keySet()) {
            if (this.warnings.containsKey(key)) {
                this.warnings.get(key).addAll(w.get(key));
            } else {
                this.warnings.put(key, w.get(key));
            }
        }
    }

    /**
     * Add given validation notifications to this object's notifications.
     *
     * @param n Notifications to add
     */
    public void addNotifications(Map<String, List<Integer>> n) {
        for (String key: n.keySet()) {
            if (this.notifications.containsKey(key)) {
                this.notifications.get(key).addAll(n.get(key));
            } else {
                this.notifications.put(key, n.get(key));
            }
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
