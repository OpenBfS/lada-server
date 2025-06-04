/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;

import org.jboss.logging.Logger;

import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.Geolocat_;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.model.lada.SampleSpecifMeasVal_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


/**
 * Merges the attributes of objects.
 */
public class ObjectMerger {

    @Inject
    Logger logger;

    @Inject
    private Repository repository;

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
        if (src.getIsTest() != null) {
            if (target.getIsTest() == null) {
                target.setIsTest(src.getIsTest());
            }
        } else {
            // Set explicit to false, if is null in src to not violate
            // constraints
            target.setIsTest(false);
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
        repository.update(target);
        return this;
    }

    /**
     * Merge zusatzwerte.
     * @param target the resulting object
     * @param zusatzwerte the source object
     * @return the merge instance
     */
    public ObjectMerger mergeZusatzwerte(
        Sample target,
        List<SampleSpecifMeasVal> zusatzwerte
    ) {
        QueryBuilder<SampleSpecifMeasVal> builder =
            repository.queryBuilder(SampleSpecifMeasVal.class);
        for (int i = 0; i < zusatzwerte.size(); i++) {
            builder.and(SampleSpecifMeasVal_.sampleId, target.getId());
            builder.and(SampleSpecifMeasVal_.sampleSpecifId,
                zusatzwerte.get(i).getSampleSpecifId());
            List<SampleSpecifMeasVal> found =
                repository.filter(builder.getQuery());
            if (found.isEmpty()) {
                repository.create(zusatzwerte.get(i));
                continue;
            } else if (found.size() > 1) {
                // something is wrong (probeId and pzsId should be unique).
                // Continue and skip this zusatzwert.
                continue;
            }
            // Update the objects.
            // direktly update the db or update the list!?
            // Updating the list could be a problem. List objects are detatched.
            //
            // Current solution:
            // Remove all db objects to be able to create new ones.
            found.get(0).setError(zusatzwerte.get(i).getError());
            found.get(0).setMeasVal(zusatzwerte.get(i).getMeasVal());
            repository.update(found.get(0));
            builder = builder.getEmptyBuilder();
        }
        return this;
    }

    /**
     * Merge messwerte.
     * @param target the resulting object
     * @param messwerte the source object
     * @return the merger instance
     */
    public ObjectMerger mergeMesswerte(
        Measm target,
        List<MeasVal> messwerte
    ) {
        QueryBuilder<MeasVal> builder =
            repository.queryBuilder(MeasVal.class)
            .and(MeasVal_.measmId, target.getId());
        List<MeasVal> found =
            repository.filter(builder.getQuery());
        // Replace existing measVals, if any
        for (MeasVal m: found) {
            repository.delete(m);
        }
        for (MeasVal m: messwerte) {
            repository.create(m);
        }
        return this;
    }

    /**
     * Merge entnahme orte.
     * @param probeId the probe id
     * @param ort the ortszuordnung
     * @return the merger instance
     */
    public ObjectMerger mergeEntnahmeOrt(
        int probeId,
        Geolocat ort
    ) {
        try {
            // Replace existing location
            QueryBuilder<Geolocat> builder =
                repository.queryBuilder(Geolocat.class)
                .and(Geolocat_.sampleId, probeId)
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
     * @param probeId the probe id
     * @param orte the ortszuordnung list
     * @return the merger instance
     */
    public ObjectMerger mergeUrsprungsOrte(
        int probeId,
        List<Geolocat> orte
    ) {
        QueryBuilder<Geolocat> builder =
            repository.queryBuilder(Geolocat.class);
        for (int i = 0; i < orte.size(); i++) {
            builder.and(Geolocat_.sampleId, probeId);
            builder.and(Geolocat_.typeRegulation, "U");
            builder.and(Geolocat_.siteId, orte.get(i).getSiteId());
            List<Geolocat> found =
                repository.filter(builder.getQuery());
            if (found.isEmpty()) {
                repository.create(orte.get(i));
            }
            builder = builder.getEmptyBuilder();
        }
        return this;
    }
}
