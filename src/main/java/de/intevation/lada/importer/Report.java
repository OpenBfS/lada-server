/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.intevation.lada.model.BaseModel;


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
    private List<Integer> sampleIds = new ArrayList<>();

    /**
     * Errors per sample that occured during import.
     */
    private Map<String, Set<ReportItem>> errors = new HashMap<>();

    /**
     * Warnings per sample that occured during import.
     */
    private Map<String, Set<ReportItem>> warnings = new HashMap<>();

    /**
     * Notifications per sample that occured during import.
     */
    private Map<String, Set<ReportItem>> notifications = new HashMap<>();


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

    public void addSampleId(Integer sampleId) {
        this.sampleIds.add(sampleId);
    }

    public Map<String, Set<ReportItem>> getErrors() {
        return errors;
    }

    public void addErrors(String key, Set<ReportItem> newErrors) {
        if (this.errors.containsKey(key)) {
            this.errors.get(key).addAll(newErrors);
        } else {
            this.errors.put(key, new HashSet<>(newErrors));
        }
    }

    public void addError(String key, ReportItem error) {
        if (this.errors.containsKey(key)) {
            this.errors.get(key).add(error);
        } else {
            this.errors.put(key, new HashSet<>(Set.of(error)));
        }
    }

    public Map<String, Set<ReportItem>> getWarnings() {
        return warnings;
    }

    public void addWarnings(String key, Set<ReportItem> newWarnings) {
        if (this.warnings.containsKey(key)) {
            this.warnings.get(key).addAll(newWarnings);
        } else {
            this.warnings.put(key, new HashSet<>(newWarnings));
        }
    }

    public void addWarning(String key, ReportItem warning) {
        if (this.warnings.containsKey(key)) {
            this.warnings.get(key).add(warning);
        } else {
            this.warnings.put(key, new HashSet<>(Set.of(warning)));
        }
    }

    public Map<String, Set<ReportItem>> getNotifications() {
        return notifications;
    }

    public void addNotifications(String key, Set<ReportItem> newNotifications) {
        if (this.notifications.containsKey(key)) {
            this.notifications.get(key).addAll(newNotifications);
        } else {
            this.notifications.put(key, new HashSet<>(newNotifications));
        }
    }

    public void addNotification(String key, ReportItem notification) {
        if (this.notifications.containsKey(key)) {
            this.notifications.get(key).add(notification);
        } else {
            this.notifications.put(key, new HashSet<>(Set.of(notification)));
        }
    }

    public void addValidationMessages(
        String key,
        String itemKey,
        String suf,
        BaseModel validatedObject
    ) {
        validatedObject.getErrors().forEach(
            (k, v) -> v.forEach(value ->
                addError(key, new ReportItem(itemKey, k + suf, value))));

        validatedObject.getWarnings().forEach(
            (k, v) -> v.forEach(value ->
                addWarning(key, new ReportItem(itemKey, k + suf, value))));

        validatedObject.getNotifications().forEach(
            (k, v) -> v.forEach(value ->
                addNotification(key, new ReportItem(itemKey, k + suf, value))));
    }
}
