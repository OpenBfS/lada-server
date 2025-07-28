/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.laf;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.validation.groups.Default;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.intevation.lada.factory.OrtFactory;
import de.intevation.lada.i18n.I18n;
import de.intevation.lada.importer.ObjectMerger;
import de.intevation.lada.importer.identification.Identification;
import de.intevation.lada.importer.identification.IdentificationException;
import de.intevation.lada.importer.Report;
import de.intevation.lada.importer.ReportItem;
import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.BelongsToMeasm;
import de.intevation.lada.model.lada.BelongsToSample;
import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.Geolocat_;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Names;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.Sample_;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.lada.Taggable;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.JSONBConfig;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.groups.CreateErrors;
import de.intevation.lada.validation.groups.Notifications;
import de.intevation.lada.validation.groups.Warnings;


public class Laf9ImportJob extends ImportJob<Collection<JsonObject>> {

    private static final String MSG_KEY_PREFIX = "validation#";

    @Inject
    private Identification identification;

    @Inject
    private Repository repository;

    @Inject
    private ObjectMerger merger;

    @Inject
    private Validator validator;

    @Inject
    private I18n i18n;

    @Inject
    private OrtFactory ortFactory;

    private Authorization authorization;

    private Map<String, Method> belongsToSampleGetters;

    private Map<Class<?>, Method> idSetters;

    private Report fileResponseData;
    private String currentReportKey;

    @PostConstruct
    private void init() {
        /* Collect getters for lists of associated child objects
           and setters for IDs of associated child objects */
        Map<String, Method> collectGetters = new HashMap<>();
        Map<Class<?>, Method> collectSetters = new HashMap<>();
        Set<PluralAttribute<? super Sample, ?, ?>> attrs = repository
            .entityManager().getMetamodel().entity(Sample.class)
            .getPluralAttributes();
        for (PluralAttribute<? super Sample, ?, ?> attr : attrs) {
            if (attr instanceof ListAttribute<?, ?>
                && BelongsToSample.class.isAssignableFrom(
                    attr.getElementType().getJavaType())
            ) {
                String attrName = attr.getName();
                try {
                    collectGetters.put(attrName,
                        new PropertyDescriptor(attrName, Sample.class)
                        .getReadMethod());

                    Class<?> elementType = attr.getElementType().getJavaType();
                    collectSetters.put(elementType,
                        repository.idProperty(elementType).getWriteMethod());
                } catch (IntrospectionException e) {
                    // Avoids warning during startup
                    throw new RuntimeException(e);
                }
            }
        }
        this.belongsToSampleGetters = Map.copyOf(collectGetters);
        this.idSetters = Map.copyOf(collectSetters);
    }

     @Override
     public void setUserInfo(UserInfo userInfo) {
        super.setUserInfo(userInfo);

        this.authorization = new Authorization(
            this.userInfo, this.i18n, this.repository);
     }

    /**
     * Run the import job.
     */
    @Override
    public void runWithTx() {
        // IDs of all imported samples
        List<Integer> importedSampleIds = new ArrayList<>();

        // Import each file
        this.files.forEach((fileName, content) -> {
            this.fileResponseData = new Report();
            for (JsonObject rawSample: content) {
                Sample inputSample = JSONBConfig.JSONB.fromJson(
                    rawSample.toString(), Sample.class);

                this.currentReportKey = inputSample.getExtId() != null
                    ? inputSample.getExtId() : inputSample.getMainSampleId();

                try {
                    Sample finalSample =
                        identification.getExisting(inputSample);
                    boolean isNewSample = finalSample == null;
                    final String msgKey = "probe";
                    if (isNewSample) {
                        /* Ignore IDs in input to prevent Hibernate from
                           considering new objects as transient */
                        inputSample.setId(null);
                        finalSample = create(inputSample, msgKey);
                    } else {
                        finalSample = merge(finalSample, rawSample, msgKey);
                    }
                    /* Merge child objects if parent has no errors,
                       i.e. was persisted */
                    if (repository.entityManager().contains(finalSample)) {
                        fileResponseData.addSampleId(finalSample.getId());
                        mergeSampleChilds(
                            finalSample,
                            isNewSample,
                            rawSample,
                            fileResponseData);
                    }

                    /* Add warnings and notifications to final state
                       with child objects merged */
                    reportValidationMessages(
                        validator.validate(
                            finalSample, Warnings.class, Notifications.class),
                        MSG_KEY_PREFIX + msgKey);

                } catch (IdentificationException e) {
                    reportIdentificationException(e);
                }
            }

            // Reporting
            if (!fileResponseData.getErrors().isEmpty()) {
                this.currentStatus.setErrors(true);
            }
            if (!fileResponseData.getWarnings().isEmpty()) {
                this.currentStatus.setWarnings(true);
            }
            if (!fileResponseData.getNotifications().isEmpty()) {
                this.currentStatus.setNotifications(true);
            }
            importData.put(fileName, fileResponseData);
            importedSampleIds.addAll(fileResponseData.getSampleIds());
        });

        tagImportedData(importedSampleIds, this.mst);
    }

    private void mergeSampleChilds(
        Sample targetSample,
        boolean isNewSample,
        JsonObject rawSample,
        Report report
    ) {
        Sample srcSample = JSONBConfig.JSONB.fromJson(
            rawSample.toString(), Sample.class);
        Map<Measm, JsonObject> importedMeasms = new HashMap<>();
        for (String attrName : belongsToSampleGetters.keySet()) {
            List<BelongsToSample> srcObjects =
                getChildList(attrName, srcSample);
            if (srcObjects == null) {
                continue;
            }
            for (int i = 0; i < srcObjects.size(); i++) {
                BelongsToSample srcObject = srcObjects.get(i);
                srcObject.setSample(targetSample);
                JsonObject rawObject =
                    rawSample.getJsonArray(attrName).getJsonObject(i);

                // Merge Site for geolocats
                if (srcObject instanceof Geolocat loc) {
                    Site srcSite = loc.getSite();
                    if (srcSite.getNetworkId() == null) {
                        // Retrieve default from sample
                        srcSite.setNetworkId(
                            repository.getById(
                                MeasFacil.class, targetSample.getMeasFacilId())
                            .getNetworkId());
                    }
                    Site finalSite = null;
                    try {
                        finalSite = identification.getExisting(srcSite);
                    } catch (IdentificationException e) {
                        reportIdentificationException(e);
                        continue;
                    }

                    final String msgKey = Geolocat_.SITE;
                    if (finalSite == null) {
                        reportValidationMessages(
                            validator.validate(srcSite, CreateErrors.class),
                            MSG_KEY_PREFIX + msgKey);
                        if (!srcSite.hasErrors()
                            && isAuthorized(srcSite, RequestMethod.POST)
                        ) {
                            /* Ignore IDs in input to prevent Hibernate from
                               considering new objects as transient */
                            srcSite.setId(null);

                            ortFactory.completeSite(srcSite);
                            finalSite = repository.create(srcSite);
                        }
                    } else if (
                        /* If identification found something not identified
                           by extId, use it as is */
                        finalSite.getExtId().equals(srcSite.getExtId())
                    ) {
                        finalSite = merge(
                            finalSite,
                            rawObject.getJsonObject(Geolocat_.SITE),
                            msgKey);
                    }
                     // Set site for identification of Geolocat
                    loc.setSite(finalSite);
                }

                BelongsToSample finalObject = null;
                if (!isNewSample) {
                    // Identify
                    try {
                        finalObject = identification.getExisting(srcObject);
                    } catch (IdentificationException e) {
                        reportIdentificationException(e);
                        continue;
                    }
                }

                // Merge existent or add new object
                if (finalObject == null) {
                    /* Ignore IDs in input to prevent Hibernate from
                       considering new objects as transient */
                    try {
                        this.idSetters.get(srcObject.getClass()).invoke(
                            srcObject, (Object) null);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                    finalObject = create(srcObject, attrName);
                } else {
                    finalObject = merge(finalObject, rawObject, attrName);
                }

                /* Merge Measm child objects if parent has no errors,
                   i.e. was persisted */
                if (repository.entityManager().contains(finalObject)
                    && finalObject instanceof Measm targetMeasm
                ) {
                    importedMeasms.put(targetMeasm, rawObject);
                } else {
                    reportValidationMessages(
                        validator.validate(
                            finalObject, Warnings.class, Notifications.class),
                        MSG_KEY_PREFIX + attrName);
                }
            }
        }

        mergeTags(srcSample, rawSample, targetSample);

        // Merge and validate child objects and validate imported measms
        for (Measm importedMeasm : importedMeasms.keySet()) {
            mergeMeasmChilds(importedMeasm, importedMeasms.get(importedMeasm));
            reportValidationMessages(
                validator.validate(
                    importedMeasm, Warnings.class, Notifications.class),
                MSG_KEY_PREFIX + Sample_.MEASMS);
        }
    }

    private void mergeTags(
        Taggable<?> src, JsonObject raw, Taggable<?> target
    ) {
        List<Tag> srcTags = src.getTags();
        if (srcTags != null && !srcTags.isEmpty()) {
            for (int i = 0; i < srcTags.size(); i++) {
                Tag srcTag = srcTags.get(i);
                Tag finalTag = null;
                try {
                    finalTag = identification.getExisting(srcTag);
                } catch (IdentificationException e) {
                    reportIdentificationException(e);
                    continue;
                }
                final String tagsKey = "tags";
                if (finalTag == null) {
                    /* Ignore IDs in input to prevent Hibernate from
                       considering new objects as transient */
                    srcTag.setId(null);
                    finalTag = create(srcTag, tagsKey);
                } else {
                    JsonObject rawTag
                        = raw.getJsonArray(tagsKey).getJsonObject(i);
                    finalTag = merge(finalTag, rawTag, tagsKey);
                }
                reportValidationMessages(
                    validator.validate(
                        finalTag, Warnings.class, Notifications.class),
                    MSG_KEY_PREFIX + tagsKey);
                if (repository.entityManager().contains(finalTag)
                    && isAuthorized(target.createTagLink(finalTag),
                        RequestMethod.POST)
                ) {
                    target.addTag(finalTag);
                    // TODO: Extend tag expiring time?
                }
            }
            // Persist added tag links
            repository.update(target);
        }
    }

    private <T extends BaseModel> T create(T inputObject, String msgKey) {
        reportValidationMessages(
            validator.validate(inputObject, CreateErrors.class),
            MSG_KEY_PREFIX + msgKey);
        if (!inputObject.hasErrors()
            && isAuthorized(inputObject, RequestMethod.POST)
        ) {
            return repository.create(inputObject);
        }
        return inputObject;
    }

    private <T extends BaseModel> T merge(
        T persistent, JsonObject rawObject, String msgKey
    ) {
        boolean changed = merger.merge(persistent, rawObject);
        if (changed) {
            reportValidationMessages(
                validator.validate(persistent, Default.class),
                MSG_KEY_PREFIX + msgKey);
            if (!persistent.hasErrors()
                && isAuthorized(persistent, RequestMethod.PUT)
            ) {
                persistent = repository.update(persistent);
            } else {
                repository.entityManager().detach(persistent);
            }
        }
        return persistent;
    }

    private boolean isAuthorized(BaseModel object, RequestMethod method) {
        String err = authorization.isAuthorizedMessage(object, method);
        if (err == null) {
            return true;
        }
        this.fileResponseData.addError(currentReportKey,
            new ReportItem(
                this.userInfo.getName(),
                repository.entityManager().getMetamodel().entity(
                    object.getClass()).getName(),
                err));
        return false;
    }

    private void mergeMeasmChilds(Measm targetMeasm, JsonObject rawMeasm) {
        Measm srcMeasm = JSONBConfig.JSONB.fromJson(
            rawMeasm.toString(), Measm.class);

        mergeTags(srcMeasm, rawMeasm, targetMeasm);

        // measVals
        Collection<MeasVal> newMeasVals = srcMeasm.getMeasVals();
        if (newMeasVals != null) {
            // Existing measVals are completely replaced
            targetMeasm.getMeasVals().clear();
            repository.entityManager()
                .createNamedQuery(Names.QUERY_DELETE_MEAS_VALS)
                .setParameter("m", targetMeasm)
                .executeUpdate();
            for (MeasVal m : newMeasVals) {
                /* Ignore IDs in input to prevent Hibernate from
                   considering new objects as transient */
                m.setId(null);

                m.setMeasm(targetMeasm);
                reportValidationMessages(
                    validator.validate(m), MSG_KEY_PREFIX + "messwert");
                if (!m.hasErrors() && isAuthorized(m, RequestMethod.POST)) {
                    repository.create(m);
                }
            }
        }

        // statusProts and commMeasms can only be added, not updated
        addBelongsToMeasms(targetMeasm, srcMeasm.getCommMeasms());
        /* Put statusProts last, because validating requires
           the final state of all objects */
        addBelongsToMeasms(targetMeasm, srcMeasm.getStatusProts());
    }

    private void addBelongsToMeasms(
        Measm target,
        Collection<? extends BelongsToMeasm> newEntries
    ) {
        if (newEntries != null) {
            for (BelongsToMeasm newEntry : newEntries) {
                /* Ignore IDs in input to prevent Hibernate from
                   considering new objects as transient */
                if (newEntry instanceof CommMeasm cm) {
                    cm.setId(null);
                } else if (newEntry instanceof StatusProt sp) {
                    sp.setId(null);
                }

                newEntry.setMeasm(target);
                reportValidationMessages(
                    validator.validate(newEntry), "Status ");
                if (!newEntry.hasErrors()
                    && isAuthorized(newEntry, RequestMethod.POST)
                ) {
                    repository.create(newEntry);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<BelongsToSample> getChildList(String name, Sample sample) {
        try {
            return (List<BelongsToSample>) belongsToSampleGetters
                .get(name).invoke(sample);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void reportIdentificationException(
        IdentificationException exception
    ) {
        Map<String, Object> failedAttrs = exception.getIdentifyingAttributes();
        ReportItem reportItem;
        if (failedAttrs != null) {
            reportItem = new ReportItem(
                failedAttrs.keySet().toString(),
                failedAttrs.values().toString(),
                StatusCodes.IMP_INVALID_VALUE);
        } else {
            reportItem = new ReportItem(
                "identification", "", StatusCodes.IMP_INVALID_VALUE);
        }
        fileResponseData.addError(currentReportKey, reportItem);
    }

    private void reportValidationMessages(
        BaseModel validatedObject, String key
    ) {
        fileResponseData.addValidationMessages(
            currentReportKey, key, validatedObject);
    }
}
