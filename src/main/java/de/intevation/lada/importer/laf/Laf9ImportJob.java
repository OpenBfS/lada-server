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
import java.util.Objects;
import java.util.Set;

import de.intevation.lada.factory.OrtFactory;
import de.intevation.lada.importer.ObjectMerger;
import de.intevation.lada.importer.Report;
import de.intevation.lada.importer.identification.Identification;
import de.intevation.lada.importer.identification.IdentificationException;
import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.BelongsToSample;
import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.Geolocat_;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.lada.TagLink;
import de.intevation.lada.model.lada.Taggable;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.data.MesswertNormalizer;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.JSONBConfig;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.groups.CreateErrors;
import de.intevation.lada.validation.groups.Notifications;
import de.intevation.lada.validation.groups.PostAuthorization;
import de.intevation.lada.validation.groups.Warnings;


public class Laf9ImportJob extends ImportJob<Collection<JsonObject>> {

    public static final String ERR_AUTHORIZATION_KEY = "authorization";

    public static final String ERR_IDENTIFICATION_KEY = "identification";

    @Inject
    private Identification identification;

    @Inject
    private Repository repository;

    @Inject
    private ObjectMerger merger;

    @Inject
    private Validator validator;

    @Inject
    private OrtFactory ortFactory;

    @Inject
    private MesswertNormalizer normalizer;

    @Inject
    private Authorization authorization;

    private Map<String, Method> belongsToSampleGetters;

    private Map<Class<?>, Method> idSetters;

    private Laf9Report fileResponseData;

    private record Raw(JsonObject object, boolean isNew) { };

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

    /**
     * Run the import job.
     */
    @Override
    public Map<String, Report> callWithTx() {
        // IDs of all imported samples
        List<Integer> importedSampleIds = new ArrayList<>();

        // Import each file
        this.files.forEach((fileName, content) -> {
            this.fileResponseData = new Laf9Report();
            for (JsonObject rawSample: content) {
                Sample inputSample = JSONBConfig.JSONB.fromJson(
                    rawSample.toString(), Sample.class);

                Sample finalSample;
                try {
                    finalSample = identification.getExisting(inputSample);
                    boolean isNewSample = finalSample == null;
                    if (isNewSample) {
                        /* Ignore IDs in input to prevent Hibernate from
                           considering new objects as transient */
                        inputSample.setId(null);
                        finalSample = create(inputSample);
                    } else {
                        finalSample = merge(finalSample, rawSample);
                    }
                    /* Merge child objects if parent has no errors,
                       i.e. was persisted */
                    if (repository.entityManager().contains(finalSample)) {
                        importedSampleIds.add(finalSample.getId());
                        mergeSampleChilds(
                            finalSample,
                            isNewSample,
                            rawSample);
                    }

                    /* Add warnings and notifications to final state
                       with child objects merged */
                    validator.validate(
                        finalSample, Warnings.class, Notifications.class);
                } catch (IdentificationException e) {
                    reportIdentificationException(inputSample);
                    finalSample = inputSample;
                }

                fileResponseData.getSamples().add(
                    getInstanceForReport(finalSample, inputSample));
            }

            // Reporting
            importData.put(fileName, fileResponseData);
        });

        tagImportedData(importedSampleIds, this.mst);

        return importData;
    }

    private void mergeSampleChilds(
        Sample targetSample,
        boolean isNewSample,
        JsonObject rawSample
    ) {
        Sample srcSample = JSONBConfig.JSONB.fromJson(
            rawSample.toString(), Sample.class);

        mergeTags(srcSample, rawSample, targetSample);

        Map<Measm, Raw> importedMeasms = srcSample.getMeasms() != null
            ? HashMap.newHashMap(srcSample.getMeasms().size())
            : Map.of();
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
                Site finalSite = null;
                if (srcObject instanceof Geolocat loc) {
                    Site srcSite = loc.getSite();
                    if (srcSite.getNetworkId() == null) {
                        // Retrieve default from sample
                        srcSite.setNetworkId(
                            repository.getById(
                                MeasFacil.class, targetSample.getMeasFacilId())
                            .getNetworkId());
                    }
                    try {
                        finalSite = identification.getExisting(srcSite);
                    } catch (IdentificationException e) {
                        reportIdentificationException(srcSite);
                        targetSample.getGeolocats().add(loc);
                        continue;
                    }

                    if (finalSite == null) {
                        validator.validate(srcSite, CreateErrors.class);
                        if (!srcSite.hasErrors()
                            && isAuthorized(srcSite, RequestMethod.POST)
                            && !validator.validate(
                                srcSite, PostAuthorization.class).hasErrors()
                        ) {
                            /* Ignore IDs in input to prevent Hibernate from
                               considering new objects as transient */
                            srcSite.setId(null);

                            ortFactory.completeSite(srcSite);
                            finalSite = repository.create(srcSite);
                        } else {
                            // Just for error reporting
                            finalSite = srcSite;
                        }
                    } else if (
                        /* If identification found something not identified
                           by extId, use it as is */
                        finalSite.getExtId().equals(srcSite.getExtId())
                    ) {
                        finalSite = merge(
                            finalSite,
                            rawObject.getJsonObject(Geolocat_.SITE));
                    }
                    validator.validate(
                        finalSite, Warnings.class, Notifications.class);
                    if (repository.entityManager().contains(finalSite)) {
                        // Successfully imported site.
                        loc.setSite(finalSite);
                    } else {
                        loc.setSite(getInstanceForReport(finalSite, srcSite));
                        targetSample.getGeolocats().add(loc);
                        continue;
                    }
                }

                // Track whether create or update occurs
                boolean isNewChild;

                BelongsToSample finalObject = null;
                List<BelongsToSample> targetList =
                    getChildList(attrName, targetSample);
                if (!isNewSample) {
                    // Identify
                    try {
                        finalObject = identification.getExisting(srcObject);
                    } catch (IdentificationException e) {
                        reportIdentificationException(srcObject);
                        targetList.add(srcObject); // Reporting
                        continue;
                    }
                }

                // Merge existent or add new object
                if (finalObject == null) {
                    isNewChild = true;
                    /* Ignore IDs in input to prevent Hibernate from
                       considering new objects as transient */
                    try {
                        this.idSetters.get(srcObject.getClass()).invoke(
                            srcObject, (Object) null);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                    finalObject = create(srcObject);
                } else {
                    isNewChild = false;
                    if (finalObject instanceof Geolocat loc
                        && !Objects.equals(loc.getSite(), finalSite)
                    ) {
                        loc.setSite(finalSite);
                        finalObject = merge(loc, rawObject, true);
                    } else {
                        finalObject = merge(finalObject, rawObject);
                    }
                }

                /* Merge Measm child objects if parent has no errors,
                   i.e. was persisted */
                if (repository.entityManager().contains(finalObject)
                    && finalObject instanceof Measm targetMeasm
                ) {
                    importedMeasms.put(
                        targetMeasm, new Raw(rawObject, isNewChild));
                } else {
                    validator.validate(
                        finalObject, Warnings.class, Notifications.class);
                }

                targetList.add(getInstanceForReport(finalObject, srcObject));
            }
        }

        // Merge and validate child objects and validate imported measms
        for (Measm importedMeasm : importedMeasms.keySet()) {
            mergeMeasmChilds(importedMeasm, importedMeasms.get(importedMeasm));
            validator.validate(
                importedMeasm, Warnings.class, Notifications.class);
        }
    }

    /**
     * Involves synchronizing {@code target} to database, which implies
     * purging any detached associated entities.
     */
    private void mergeTags(
        Taggable<?> src, JsonObject raw, Taggable<?> target
    ) {
        List<Tag> srcTags = src.getTags();
        List<Tag> erroneousTags = new ArrayList<>();
        if (srcTags != null && !srcTags.isEmpty()) {
            for (int i = 0; i < srcTags.size(); i++) {
                Tag srcTag = srcTags.get(i);
                Tag finalTag = null;
                try {
                    finalTag = identification.getExisting(srcTag);
                } catch (IdentificationException e) {
                    reportIdentificationException(srcTag);
                    erroneousTags.add(srcTag);
                    continue;
                }
                final String tagsKey = "tags";
                if (finalTag == null) {
                    /* Ignore IDs in input to prevent Hibernate from
                       considering new objects as transient */
                    srcTag.setId(null);
                    finalTag = create(srcTag);
                } else {
                    JsonObject rawTag
                        = raw.getJsonArray(tagsKey).getJsonObject(i);
                    finalTag = merge(finalTag, rawTag);
                }
                validator.validate(
                    finalTag, Warnings.class, Notifications.class);
                TagLink tagLink = target.createTagLink(finalTag);
                if (repository.entityManager().contains(finalTag)
                    && isAuthorized(tagLink, RequestMethod.POST)
                ) {
                    target.addTag(finalTag);
                } else {
                    Tag reportTag = getInstanceForReport(finalTag, srcTag);
                    reportTag.addErrors(tagLink.getErrors());
                    erroneousTags.add(reportTag);
                }
            }
            // Persist added tag links
            repository.update(target);
            target.getTags().addAll(erroneousTags); // Reporting
        }
    }

    private <T extends BaseModel> T create(
        T inputObject,
        Class<?>... validationGroups
    ) {
        Class<?>[] createErrors = { CreateErrors.class };
        Class<?>[] effectiveValidationGroups = validationGroups.length == 0
            ? createErrors
            : validationGroups;
        validator.validate(inputObject, effectiveValidationGroups);
        if (!inputObject.hasErrors()
            && isAuthorized(inputObject, RequestMethod.POST)
            && !validator.validate(
                inputObject, PostAuthorization.class).hasErrors()
        ) {
            return repository.create(inputObject);
        }
        return inputObject;
    }

    private <T extends BaseModel> T merge(T persistent, JsonObject rawObject) {
        return merge(persistent, rawObject, false);
    }

    private <T extends BaseModel> T merge(
        T persistent, JsonObject rawObject, boolean dirty
    ) {
        boolean changed = merger.merge(persistent, rawObject) || dirty;
        if (changed) {
            validator.validate(persistent, Default.class);
            if (!persistent.hasErrors()
                && isAuthorized(persistent, RequestMethod.PUT)
                && !validator.validate(
                    persistent, PostAuthorization.class).hasErrors()
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
        object.addError(ERR_AUTHORIZATION_KEY, err);
        return false;
    }

    /**
     * Reporting: Use input in case of error to avoid disclosure
     * of merged information.
     */
    private <T extends BaseModel> T getInstanceForReport(
        T withMessages, T input
    ) {
        if (withMessages != input && withMessages.hasErrors()) {
            input.addErrors(withMessages.getErrors());
            input.addWarnings(withMessages.getWarnings());
            input.addNotifications(withMessages.getNotifications());
            return input;
        }
        return withMessages;
    }

    private void mergeMeasmChilds(Measm targetMeasm, Raw raw) {
        JsonObject rawMeasm = raw.object;
        Measm srcMeasm = JSONBConfig.JSONB.fromJson(
            rawMeasm.toString(), Measm.class);

        mergeTags(srcMeasm, rawMeasm, targetMeasm);

        // measVals
        Collection<MeasVal> newMeasVals = normalizer.normalizeMesswerte(
            srcMeasm.getMeasVals(), targetMeasm.getSample().getEnvMediumId());
        if (newMeasVals != null) {
            // Existing measVals are completely replaced
            List<MeasVal> targetMeasVals = targetMeasm.getMeasVals();
            if (targetMeasVals != null && !targetMeasVals.isEmpty()) {
                targetMeasVals.clear();
                repository.entityManager()
                    .createNamedQuery(Measm_.QUERY_DELETE_MEAS_VALS)
                    .setParameter("m", targetMeasm)
                    .executeUpdate();
            }
            for (MeasVal m : newMeasVals) {
                /* Ignore IDs in input to prevent Hibernate from
                   considering new objects as transient */
                m.setId(null);

                m.setMeasm(targetMeasm);
                create(m,
                    CreateErrors.class, Warnings.class, Notifications.class);
            }
            // Just for reporting
            if (targetMeasVals == null) {
                targetMeasm.setMeasVals(new ArrayList<>(newMeasVals));
            } else {
                targetMeasVals.addAll(newMeasVals);
            }
        }

        List<CommMeasm> srcCommMeasms = srcMeasm.getCommMeasms();
        if (srcCommMeasms != null) {
            for (int i = 0; i < srcCommMeasms.size(); i++) {
                CommMeasm srcCommMeasm = srcCommMeasms.get(i);
                srcCommMeasm.setMeasm(targetMeasm);
                CommMeasm finalCommMeasm = null;
                if (!raw.isNew) {
                    // Identify
                    try {
                        finalCommMeasm =
                            identification.getExisting(srcCommMeasm);
                    } catch (IdentificationException e) {
                        // Reporting
                        reportIdentificationException(srcCommMeasm);
                        List<CommMeasm> targetCommMeasms =
                            targetMeasm.getCommMeasms();
                        if (targetCommMeasms == null) {
                            targetMeasm.setCommMeasms(
                                new ArrayList<>(List.of(srcCommMeasm)));
                        } else {
                            targetCommMeasms.add(srcCommMeasm);
                        }
                        continue;
                    }
                }

                // Add new comment or merge existent
                if (finalCommMeasm == null) {
                    /* Ignore IDs in input to prevent Hibernate from
                       considering new objects as transient */
                    srcCommMeasm.setId(null);
                    finalCommMeasm = create(srcCommMeasm);
                } else {
                    finalCommMeasm = merge(
                        finalCommMeasm,
                        rawMeasm.getJsonArray(Measm_.COMM_MEASMS)
                        .getJsonObject(i));
                }
                validator.validate(
                    finalCommMeasm, Warnings.class, Notifications.class);
            }
        }

        /* Put statusProts last, because validating requires
           the final state of all objects.
           statusProts can only be added */
        List<StatusProt> newStatusProts = srcMeasm.getStatusProts();
        if (newStatusProts != null) {
            for (StatusProt sp : newStatusProts) {
                /* Ignore IDs in input to prevent Hibernate from
                   considering new objects as transient */
                sp.setId(null);

                sp.setMeasm(targetMeasm);
                create(sp,
                    CreateErrors.class, Warnings.class, Notifications.class);
            }
            // Reporting
            List<StatusProt> targetStatusProts = targetMeasm.getStatusProts();
            if (targetStatusProts == null) {
                targetMeasm.setStatusProts(newStatusProts);
            } else {
                targetStatusProts.addAll(newStatusProts);
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

    private void reportIdentificationException(BaseModel object) {
        object.addError(ERR_IDENTIFICATION_KEY,
            String.valueOf(StatusCodes.IMP_INVALID_VALUE));
    }
}
