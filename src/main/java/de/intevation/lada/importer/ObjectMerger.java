/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import org.jboss.logging.Logger;

import de.intevation.lada.model.land.CommMeasm;
import de.intevation.lada.model.land.CommSample;
import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Messwert;
import de.intevation.lada.model.land.Ortszuordnung;
import de.intevation.lada.model.land.Sample;
import de.intevation.lada.model.land.ZusatzWert;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.Response;

/**
 * Merges the attributes of objects.
 */
public class ObjectMerger {

    @Inject
    Logger logger;

    @Inject
    private Repository repository;

    /**
     * Merge probe objects.
     * @param target the resulting probe objects
     * @param src the source object
     * @return returns true on success
     */
    public boolean merge(Sample target, Sample src) {
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
        if (src.getStateMpgId() != null) {
            target.setStateMpgId(src.getStateMpgId());
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
        Response r = repository.update(target);
        return r.getSuccess();
    }

    /**
     * Merge messung objects.
     * @param target the resulting object
     * @param src the source object
     * @return the merger instance
     */
    public ObjectMerger mergeMessung(Messung target, Messung src) {
        if (target.getNebenprobenNr() == null
            || target.getNebenprobenNr().isEmpty()
        ) {
            target.setNebenprobenNr(src.getNebenprobenNr());
        }
        if (src.getFertig() != null) {
            target.setFertig(src.getFertig());
        } else if (target.getFertig() == null) {
            target.setFertig(false);
        }
        if (src.getGeplant() != null) {
            if (target.getGeplant() == null) {
                target.setGeplant(src.getGeplant());
            }
        } else if (target.getGeplant() == null) {
            target.setGeplant(false);
        }
        if (src.getMessdauer() != null) {
            target.setMessdauer(src.getMessdauer());
        }
        if (src.getMesszeitpunkt() != null) {
            target.setMesszeitpunkt(src.getMesszeitpunkt());
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
        List<ZusatzWert> zusatzwerte
    ) {
        QueryBuilder<ZusatzWert> builder =
            repository.queryBuilder(ZusatzWert.class);
        for (int i = 0; i < zusatzwerte.size(); i++) {
            builder.and("probeId", target.getId());
            builder.and("pzsId", zusatzwerte.get(i).getPzsId());
            List<ZusatzWert> found =
                repository.filterPlain(builder.getQuery());
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
            found.get(0).setMessfehler(zusatzwerte.get(i).getMessfehler());
            found.get(0).setMesswertPzs(zusatzwerte.get(i).getMesswertPzs());
            repository.update(found.get(0));
            builder = builder.getEmptyBuilder();
        }
        return this;
    }

    /**
     * Merge kommentare.
     * @param target the resulting object
     * @param kommentare the source object
     * @return the merge instance
     */
    public ObjectMerger mergeKommentare(
        Sample target,
        List<CommSample> kommentare
    ) {
        QueryBuilder<CommSample> builder =
            repository.queryBuilder(CommSample.class);
        for (int i = 0; i < kommentare.size(); i++) {
            builder.and("sampleId", target.getId());
            builder.and("measFacilId", kommentare.get(i).getMeasFacilId());
            builder.and("date", kommentare.get(i).getDate());
            List<CommSample> found =
                repository.filterPlain(builder.getQuery());
            if (found.isEmpty()) {
                repository.create(kommentare.get(i));
                continue;
            } else if (found.size() > 1) {
                // something is wrong (probeId and mstId and datum should
                // be unique).
                // Continue and skip this kommentar.
                continue;
            }
            builder = builder.getEmptyBuilder();
        }
        return this;
    }

    /**
     * Merge messung kommentare.
     * @param target the resulting object
     * @param kommentare the source object
     * @return the merger instance
     */
    public ObjectMerger mergeMessungKommentare(
        Messung target,
        List<CommMeasm> kommentare
    ) {
        QueryBuilder<CommMeasm> builder =
            repository.queryBuilder(CommMeasm.class);
        for (int i = 0; i < kommentare.size(); i++) {
            builder.and("measmId", target.getId());
            builder.and("measFacilId", kommentare.get(i).getMeasFacilId());
            builder.and("date", kommentare.get(i).getDate());
            List<CommMeasm> found =
                repository.filterPlain(builder.getQuery());
            if (found.isEmpty()) {
                repository.create(kommentare.get(i));
                continue;
            } else if (found.size() > 1) {
                // something is wrong (probeId and mstId and datum should
                // be unique).
                // Continue and skip this zusatzwert.
                continue;
            }
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
        Messung target,
        List<Messwert> messwerte
    ) {
        QueryBuilder<Messwert> builder =
            repository.queryBuilder(Messwert.class);
        builder.and("messungsId", target.getId());
        List<Messwert> found =
            repository.filterPlain(builder.getQuery());
        if (found.isEmpty()) {
            for (int i = 0; i < messwerte.size(); i++) {
                repository.create(messwerte.get(i));
            }
            return this;
        }
        try {
            for (int i = 0; i < found.size(); i++) {
                repository.delete(found.get(i));
            }
            for (int i = 0; i < messwerte.size(); i++) {
                repository.create(messwerte.get(i));
            }
        } catch (SecurityException
            | IllegalStateException
            | PersistenceException e
        ) {
            // Restore messwerte.
            logger.debug("exception: ", e);
            for (int i = 0; i < found.size(); i++) {
                repository.update(found.get(i));
            }
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
        Ortszuordnung ort
    ) {
        QueryBuilder<Ortszuordnung> builder =
            repository.queryBuilder(Ortszuordnung.class);
        builder.and("probeId", probeId);
        builder.and("ortszuordnungTyp", "E");
        List<Ortszuordnung> found =
            repository.filterPlain(builder.getQuery());
        if (found.isEmpty()) {
            repository.create(ort);
            return this;
        }
        try {
            for (int i = 0; i < found.size(); i++) {
                repository.delete(found.get(i));
            }
            repository.create(ort);
        } catch (SecurityException
            | IllegalStateException
            | PersistenceException e
        ) {
            // Restore orte.
            logger.debug("exception: ", e);
            for (int i = 0; i < found.size(); i++) {
                repository.update(found.get(i));
            }
        }
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
        List<Ortszuordnung> orte
    ) {
        QueryBuilder<Ortszuordnung> builder =
            repository.queryBuilder(Ortszuordnung.class);
        for (int i = 0; i < orte.size(); i++) {
            builder.and("probeId", probeId);
            builder.and("ortszuordnungTyp", "U");
            builder.and("ortId", orte.get(i).getOrtId());
            List<Ortszuordnung> found =
                repository.filterPlain(builder.getQuery());
            if (found.isEmpty()) {
                repository.create(orte.get(i));
            }
            builder = builder.getEmptyBuilder();
        }
        return this;
    }
}
