/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.json.bind.annotation.JsonbTransient;

/**
 * Provides shared attributes for model entities.
 */
public abstract class BaseModel {
    private Map<String, Set<String>> errors = new HashMap<>();
    private Map<String, Set<String>> warnings = new HashMap<>();
    private Map<String, Set<String>> notifications = new HashMap<>();

    private boolean readonly;

    public Map<String, Set<String>>getErrors() {
        return this.errors;
    }

    @JsonbTransient
    public void setErrors(Map<String, Set<String>> errors) {
        this.errors = errors;
    }

    public Map<String, Set<String>> getWarnings() {
        return this.warnings;
    }

    @JsonbTransient
    public void setWarnings(Map<String, Set<String>> warnings) {
        this.warnings = warnings;
    }

    public Map<String, Set<String>> getNotifications() {
        return this.notifications;
    }

    @JsonbTransient
    public void setNotifications(
        Map<String, Set<String>> notifications
    ) {
        this.notifications = notifications;
    }

    /**
     * Add given validation errors to this object's errors.
     *
     * @param e Errors to add
     */
    public void addErrors(Map<String, Set<String>> e) {
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
    public void addWarnings(Map<String, Set<String>> w) {
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
    public void addNotifications(Map<String, Set<String>> n) {
        for (String key: n.keySet()) {
            if (this.notifications.containsKey(key)) {
                this.notifications.get(key).addAll(n.get(key));
            } else {
                this.notifications.put(key, n.get(key));
            }
        }
    }

    /**
     * Add given validation error to this object's errors.
     *
     * @param key Key of error to add
     * @param value Message of error to add
     */
    public void addError(String key, String value) {
        if (!this.errors.containsKey(key)) {
            this.errors.put(key, new HashSet<String>());
        }
        this.errors.get(key).add(value);
    }

    /**
     * Add given validation warning to this object's warnings.
     *
     * @param key Key of warning to add
     * @param value Message of warning to add
     */
    public void addWarning(String key, String value) {
        if (!this.warnings.containsKey(key)) {
            this.warnings.put(key, new HashSet<String>());
        }
        this.warnings.get(key).add(value);
    }

    /**
     * Add given validation notification to this object's notifications.
     *
     * @param key Key of notification to add
     * @param value Message of notification to add
     */
    public void addNotification(String key, String value) {
        if (!this.notifications.containsKey(key)) {
            this.notifications.put(key, new HashSet<String>());
        }
        this.notifications.get(key).add(value);
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

    public boolean isReadonly() {
        return readonly;
    }

    @JsonbTransient
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }
}
