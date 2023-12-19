/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container for violations detected during validation.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class Violation {

    private Map<String, List<Integer>> warnings;

    private Map<String, List<Integer>> errors;

    private Map<String, List<Integer>>notifications;

    public Violation() {
        this.warnings = new HashMap<String, List<Integer>>();
        this.errors = new HashMap<String, List<Integer>>();
        this.notifications = new HashMap<String, List<Integer>>();
    }

    public Map<String, List<Integer>> getWarnings() {
        return this.warnings;
    }

    public Map<String, List<Integer>> getErrors() {
        return this.errors;
    }

    public Map<String, List<Integer>> getNotifications() {
      return this.notifications;
    }

    public void addWarning(String key, Integer value) {
        if (!this.warnings.containsKey(key)) {
            this.warnings.put(key, new ArrayList<Integer>());
        }
        this.warnings.get(key).add(value);
    }

    public void addError(String key, Integer value) {
        if (!this.errors.containsKey(key)) {
            this.errors.put(key, new ArrayList<Integer>());
        }
        this.errors.get(key).add(value);
    }

    public void addNotification(String key, Integer value) {
        if (!this.notifications.containsKey(key)) {
            this.notifications.put(key, new ArrayList<Integer>());
        }
        this.notifications.get(key).add(value);
    }

    public void addWarnings(Map<String, List<Integer>> w) {
        for (String key: w.keySet()) {
            if (this.warnings.containsKey(key)) {
                this.warnings.get(key).addAll(w.get(key));
            } else {
                this.warnings.put(key, w.get(key));
            }
        }
    }

    public void addErrors(Map<String, List<Integer>> e) {
        for (String key: e.keySet()) {
            if (this.errors.containsKey(key)) {
                this.errors.get(key).addAll(e.get(key));
            } else {
                this.errors.put(key, e.get(key));
            }
        }
    }

    public void addNotifications(Map<String, List<Integer>> n) {
        for (String key: n.keySet()) {
            if (this.notifications.containsKey(key)) {
                this.notifications.get(key).addAll(n.get(key));
            } else {
                this.notifications.put(key, n.get(key));
            }
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
