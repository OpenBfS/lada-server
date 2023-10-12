/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 * Container for violations detected during validation.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class Violation {

    private MultivaluedMap<String, String> warnings;

    private MultivaluedMap<String, String> errors;

    private MultivaluedMap<String, Integer> notifications;

    public Violation() {
        this.warnings = new MultivaluedHashMap<>();
        this.errors = new MultivaluedHashMap<>();
        this.notifications = new MultivaluedHashMap<>();
    }

    public MultivaluedMap<String, String> getWarnings() {
        return this.warnings;
    }

    public MultivaluedMap<String, String> getErrors() {
        return this.errors;
    }

    public MultivaluedMap<String, Integer> getNotifications() {
      return this.notifications;
    }

    public void addWarning(String key, String value) {
        this.warnings.add(key, value);
    }

    public void addWarning(String key, Integer value) {
        this.warnings.add(key, value.toString());
    }

    public void addError(String key, String value) {
        this.errors.add(key, value);
    }

    public void addError(String key, Integer value) {
        this.errors.add(key, value.toString());
    }

    public void addNotification(String key, Integer value) {
      this.notifications.add(key, value);
    }

    public void addWarnings(MultivaluedMap<String, String> w) {
        for (String key: w.keySet()) {
            this.warnings.addAll(key, w.get(key));
        }
    }

    public void addErrors(MultivaluedMap<String, String> e) {
        for (String key: e.keySet()) {
            this.errors.addAll(key, e.get(key));
        }
    }

    public void addNotifications(MultivaluedMap<String, Integer> n) {
     for (String key: n.keySet()) {
       this.notifications.addAll(key, n.get(key));
     }
    }

    public boolean hasWarnings() {
        return this.warnings.size() > 0;
    }

    public boolean hasErrors() {
        return this.errors.size() > 0;
    }

    public boolean hasNotifications() {
      return this.notifications.size() > 0;
    }

}
