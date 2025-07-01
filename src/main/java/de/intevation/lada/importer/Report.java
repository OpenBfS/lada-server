/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Container for result of importing a file.
 */
public class Report {

    /**
     * Name of tag that was generated for the import.
     */
    private String tag;

    /**
     * IDs of successfully imported samples.
     */
    private List<Integer> sampleIds = List.of();

    /**
     * Errors per sample that occured during import.
     */
    private Map<String, List<ReportItem>> errors = new HashMap<>();

    /**
     * Warnings per sample that occured during import.
     */
    private Map<String, List<ReportItem>> warnings = new HashMap<>();

    /**
     * Notifications per sample that occured during import.
     */
    private Map<String, List<ReportItem>> notifications = new HashMap<>();


    /**
     * Indicates whether contents of the file could be imported without errors.
     */
    public boolean isSuccess() {
        return this.errors.isEmpty();
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<Integer> getSampleIds() {
        return sampleIds;
    }

    public void setSampleIds(List<Integer> sampleIds) {
        this.sampleIds = sampleIds;
    }

    public Map<String, List<ReportItem>> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, List<ReportItem>> errors) {
        this.errors = errors;
    }

    public void addErrors(Map<String, List<ReportItem>> newErrors) {
        for (Map.Entry<String, List<ReportItem>> entry : newErrors.entrySet()) {
            if (this.errors.containsKey(entry.getKey())) {
                this.errors.get(entry.getKey()).addAll(entry.getValue());
            } else {
                this.errors.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public Map<String, List<ReportItem>> getWarnings() {
        return warnings;
    }

    public void setWarnings(Map<String, List<ReportItem>> warnings) {
        this.warnings = warnings;
    }

    public void addWarnings(Map<String, List<ReportItem>> newWarnings) {
        for (Map.Entry<String, List<ReportItem>> entry
                 : newWarnings.entrySet()) {
            if (this.warnings.containsKey(entry.getKey())) {
                this.warnings.get(entry.getKey()).addAll(entry.getValue());
            } else {
                this.warnings.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public Map<String, List<ReportItem>> getNotifications() {
        return notifications;
    }

    public void setNotifications(Map<String, List<ReportItem>> notifications) {
        this.notifications = notifications;
    }

    public void addNotifications(
        Map<String, List<ReportItem>> newNotifications
    ) {
        for (Map.Entry<String, List<ReportItem>> entry
                 : newNotifications.entrySet()) {
            if (this.notifications.containsKey(entry.getKey())) {
                this.notifications.get(entry.getKey()).addAll(entry.getValue());
            } else {
                this.notifications.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
