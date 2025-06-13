/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer;

import java.util.List;
import java.util.Map;


/**
 * Container for result of importing a file.
 */
public class Report {

    /**
     * Indicates whether contents of the file could be imported.
     */
    private boolean success;

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
    private Map<String, List<ReportItem>> errors = Map.of();

    /**
     * Warnings per sample that occured during import.
     */
    private Map<String, List<ReportItem>> warnings = Map.of();

    /**
     * Notifications per sample that occured during import.
     */
    private Map<String, List<ReportItem>> notifications = Map.of();


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

    public Map<String, List<ReportItem>> getWarnings() {
        return warnings;
    }

    public void setWarnings(Map<String, List<ReportItem>> warnings) {
        this.warnings = warnings;
    }

    public Map<String, List<ReportItem>> getNotifications() {
        return notifications;
    }

    public void setNotifications(Map<String, List<ReportItem>> notifications) {
        this.notifications = notifications;
    }
}
