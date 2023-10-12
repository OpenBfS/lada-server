/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model;

import java.util.List;
import java.util.Map;

import jakarta.json.bind.annotation.JsonbTransient;

/**
 * Provides shared attributes for model entities.
 */
public abstract class BaseModel {
    private Map<String, List<String>> errors;
    private Map<String, List<String>> warnings;
    private Map<String, List<Integer>> notifications;

    public Map<String, List<String>>getErrors() {
        return this.errors;
    }

    @JsonbTransient
    public void setErrors(Map<String, List<String>> errors) {
        this.errors = errors;
    }

    public Map<String, List<String>> getWarnings() {
        return this.warnings;
    }

    @JsonbTransient
    public void setWarnings(Map<String, List<String>> warnings) {
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
}
