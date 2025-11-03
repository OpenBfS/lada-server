/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.persistence.Id;
import jakarta.persistence.NoResultException;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.Geolocat_;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Names;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.JSONBConfig;


/**
 * Merges the attributes of objects.
 */
public class ObjectMerger {

    @Inject
    private Repository repository;

    /**
     * Merge attribute values from JSON object into
     * {@link jakarta.persistence.Entity} instance.
     *
     * Attributes in {@code src} that do not correspond to any entity
     * attribute, those that correspond to {@link Id} attributes,
     * associations and read-only attributes (without setter method)
     * are ignored.
     *
     * Attributes are set to a new value if the old value in {@code target}
     * and the deserialized value from {@code src} differ in terms of
     * {@link Objects#equals(Object, Object)}.
     *
     * @param target entity instance at which attributes are set
     * @param src JSON object from which attribute values are taken
     * @return true, if any attribute in {@code target} was changed
     * @throws IllegalArgumentException if {@code target} is not an entity
     */
    public boolean merge(Object target, JsonObject src) {
        Class<?> clazz = target.getClass();
        EntityType<?> type = repository.entityManager().getMetamodel()
            .entity(clazz);
        Object srcObject = JSONBConfig.JSONB.fromJson(src.toString(), clazz);
        boolean anyAttrChanged = false;
        for (String name : src.keySet()) {
            Attribute<?, ?> attr;
            try {
                 attr = type.getAttribute(name);
            } catch (IllegalArgumentException e) {
                // Attribute of the given name does not exist. Ignore
                continue;
            }

            if (
                // target is the identified entity instance. Do not override ID
                attr instanceof SingularAttribute<?, ?> sing && sing.isId()
                // Do not recurse into associations
                || attr.isAssociation()
            ) {
                continue;
            }

            PropertyDescriptor p;
            try {
                p = new PropertyDescriptor(name, clazz);
            } catch (IntrospectionException e) {
                // Attribute is no Bean property with pair of accessor methods
                continue;
            }
            try {
                Method setter = p.getWriteMethod();
                Method getter = p.getReadMethod();
                Object newValue = getter.invoke(srcObject);
                if (!Objects.equals(getter.invoke(target), newValue)) {
                    setter.invoke(target, newValue);
                    anyAttrChanged = true;
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        return anyAttrChanged;
    }

    /**
     * Merge sample objects.
     * @param target the resulting probe objects
     * @param src the source object
     */
    public void merge(Sample target, Sample src) {
        if (src.getOprModeId() != null) {
            target.setOprModeId(src.getOprModeId());
        }
        if (src.getRegulationId() != null) {
            target.setRegulationId(src.getRegulationId());
        }
        if (src.getDatasetCreatorId() != null) {
            target.setDatasetCreatorId(src.getDatasetCreatorId());
        }
        if (src.getMainSampleId() != null
            && !src.getMainSampleId().isEmpty()
        ) {
            target.setMainSampleId(src.getMainSampleId());
        }
        if (src.getApprLabId() != null) {
            target.setApprLabId(src.getApprLabId());
        }
        if (src.getEnvDescripName() != null) {
            target.setEnvDescripName(src.getEnvDescripName());
        }
        if (src.getEnvDescripDisplay() != null) {
            target.setEnvDescripDisplay(src.getEnvDescripDisplay());
        }
        if (src.getMidSampleDate() != null) {
            target.setMidSampleDate(src.getMidSampleDate());
        }
        if (src.getMpgCategId() != null) {
            target.setMpgCategId(src.getMpgCategId());
        }
        if (src.getSampleStartDate() != null) {
            target.setSampleStartDate(src.getSampleStartDate());
        }
        if (src.getSampleEndDate() != null) {
            target.setSampleEndDate(src.getSampleEndDate());
        }
        if (src.getSampleMethId() != null) {
            target.setSampleMethId(src.getSampleMethId());
        }
        if (src.getSamplerId() != null) {
            target.setSamplerId(src.getSamplerId());
        }
        if (src.getSchedStartDate() != null) {
            target.setSchedStartDate(src.getSchedStartDate());
        }
        if (src.getSchedEndDate() != null) {
            target.setSchedEndDate(src.getSchedEndDate());
        }
        if (src.getOrigDate() != null) {
            target.setOrigDate(src.getOrigDate());
        }
        if (src.getEnvMediumId() != null) {
            target.setEnvMediumId(src.getEnvMediumId());
        }
        repository.update(target);
    }

    /**
     * Merge messung objects.
     * @param target the resulting object
     * @param src the source object
     * @return the merger instance
     */
    public ObjectMerger mergeMessung(Measm target, Measm src) {
        if (target.getMinSampleId() == null
            || target.getMinSampleId().isEmpty()
        ) {
            target.setMinSampleId(src.getMinSampleId());
        }
        if (src.getIsCompleted() != null) {
            target.setIsCompleted(src.getIsCompleted());
        } else if (target.getIsCompleted() == null) {
            target.setIsCompleted(false);
        }
        if (src.getIsScheduled() != null) {
            if (target.getIsScheduled() == null) {
                target.setIsScheduled(src.getIsScheduled());
            }
        } else if (target.getIsScheduled() == null) {
            target.setIsScheduled(false);
        }
        if (src.getMeasPd() != null) {
            target.setMeasPd(src.getMeasPd());
        }
        if (src.getMeasmStartDate() != null) {
            target.setMeasmStartDate(src.getMeasmStartDate());
        }
        if (src.getMmtId() != null) {
            target.setMmtId(src.getMmtId());
        }
        return this;
    }

    /**
     * Merge messwerte.
     * @param target the resulting object
     * @param measVals the source object
     * @return the merger instance
     */
    public ObjectMerger mergeMeasVals(
        Measm target,
        Collection<MeasVal> measVals
    ) {
        target.getMeasVals().clear();
        repository.entityManager()
            .createNamedQuery(Names.QUERY_DELETE_MEAS_VALS)
            .setParameter("m", target)
            .executeUpdate();
        for (MeasVal m: measVals) {
            m.setMeasm(target);
            repository.create(m);
        }
        return this;
    }

    /**
     * Merge entnahme orte.
     * @param sample the sample
     * @param ort the ortszuordnung
     * @return the merger instance
     */
    public ObjectMerger mergeEntnahmeOrt(
        Sample sample,
        Geolocat ort
    ) {
        try {
            // Replace existing location
            QueryBuilder<Geolocat> builder = repository
            .queryBuilder(Geolocat.class)
            .and(Geolocat_.sample, sample)
            .and(Geolocat_.typeRegulation, "E");
            Geolocat found = repository.getSingle(builder.getQuery());
            repository.delete(found);
        } catch (NoResultException e) {
            // Nothing to replace
        }
        repository.create(ort);
        return this;
    }

    /**
     * Merge entnahme orte.
     * @param sample the sample
     * @param orte the ortszuordnung list
     * @return the merger instance
     */
    public ObjectMerger mergeUrsprungsOrte(
        Sample sample,
        List<Geolocat> orte
    ) {
        QueryBuilder<Geolocat> builder =
            repository.queryBuilder(Geolocat.class);
        for (Geolocat loc: orte) {
            builder.and(Geolocat_.sample, sample)
                .and(Geolocat_.typeRegulation, "U")
                .and(Geolocat_.site, loc.getSite());
            List<Geolocat> found =
                repository.filter(builder.getQuery());
            if (found.isEmpty()) {
                repository.create(loc);
            }
            builder = builder.getEmptyBuilder();
        }
        return this;
    }
}
