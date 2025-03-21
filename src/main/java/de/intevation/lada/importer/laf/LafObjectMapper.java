/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.laf;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.persistence.metamodel.SingularAttribute;

import org.jboss.logging.Logger;

import de.intevation.lada.factory.OrtFactory;
import de.intevation.lada.factory.ProbeFactory;
import de.intevation.lada.i18n.I18n;
import de.intevation.lada.importer.Identifier;
import de.intevation.lada.importer.ObjectMerger;
import de.intevation.lada.importer.ReportItem;
import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.model.lada.CommMeasm_;
import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.model.lada.CommSample_;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.Geolocat_;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.lada.TagLinkMeasm;
import de.intevation.lada.model.lada.TagLinkMeasm_;
import de.intevation.lada.model.lada.TagLinkSample;
import de.intevation.lada.model.lada.TagLinkSample_;
import de.intevation.lada.model.master.AdminUnit;
import de.intevation.lada.model.master.AdminUnit_;
import de.intevation.lada.model.master.DatasetCreator;
import de.intevation.lada.model.master.DatasetCreator_;
import de.intevation.lada.model.master.EnvMedium;
import de.intevation.lada.model.master.EnvMedium_;
import de.intevation.lada.model.master.ImportConf;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.MeasUnit;
import de.intevation.lada.model.master.MeasUnit_;
import de.intevation.lada.model.master.Measd;
import de.intevation.lada.model.master.Measd_;
import de.intevation.lada.model.master.Mmt;
import de.intevation.lada.model.master.Mmt_;
import de.intevation.lada.model.master.MpgCateg;
import de.intevation.lada.model.master.MpgCateg_;
import de.intevation.lada.model.master.MpgTransf;
import de.intevation.lada.model.master.MpgTransf_;
import de.intevation.lada.model.master.NuclFacilGr;
import de.intevation.lada.model.master.NuclFacilGr_;
import de.intevation.lada.model.master.Poi;
import de.intevation.lada.model.master.Regulation;
import de.intevation.lada.model.master.Regulation_;
import de.intevation.lada.model.master.ReiAgGr;
import de.intevation.lada.model.master.ReiAgGr_;
import de.intevation.lada.model.master.SampleMeth;
import de.intevation.lada.model.master.SampleMeth_;
import de.intevation.lada.model.master.SampleSpecif;
import de.intevation.lada.model.master.SampleSpecif_;
import de.intevation.lada.model.master.Sampler;
import de.intevation.lada.model.master.Sampler_;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.Site_;
import de.intevation.lada.model.master.SpatRefSys;
import de.intevation.lada.model.master.SpatRefSys_;
import de.intevation.lada.model.master.State;
import de.intevation.lada.model.master.State_;
import de.intevation.lada.model.master.StatusAccessMpView;
import de.intevation.lada.model.master.StatusAccessMpView_;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.model.master.Tag_;
import de.intevation.lada.model.master.Tz;
import de.intevation.lada.model.master.Tz_;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.MesswertNormalizer;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.validation.Validator;

/**
 * Create database objects and map the attributes from laf raw data.
 */
public class LafObjectMapper {

    @Inject
    private Logger logger;

    private Authorization authorizer;

    private Validator validator;

    @Inject
    private Identifier<Sample> probeIdentifier;

    @Inject
    private Identifier<Measm> messungIdentifier;

    @Inject
    private ObjectMerger merger;

    @Inject
    private Repository repository;

    @Inject
    private I18n i18n;

    @Inject
    private ProbeFactory factory;

    @Inject OrtFactory ortFactory;

    @Inject
    private MesswertNormalizer messwertNormalizer;

    private Map<String, List<ReportItem>> errors;
    private Map<String, List<ReportItem>> warnings;
    private Map<String, List<ReportItem>> notifications;
    private Set<ReportItem> currentErrors;
    private Set<ReportItem> currentWarnings;
    private Set<ReportItem> currentNotifications;
    private List<Integer> importProbeIds;

    private int currentZeitbasis;

    private UserInfo userInfo;

    private String measFacilId;

    private List<ImportConf> config;
    private ImportConfigMapper configMapper;

    /**
     * Map the raw data to database objects.
     * @param data the raw data from laf parser
     */
    public void mapObjects(LafRawData data) {
        validator = new Validator();
        errors = new HashMap<>();
        warnings = new HashMap<>();
        notifications = new HashMap<>();
        importProbeIds = new ArrayList<>();
        for (LafRawData.Sample sample: data.getProben()) {
            create(sample);
        }
    }

    private void create(LafRawData.Sample object) {
        currentWarnings = new HashSet<>();
        currentErrors = new HashSet<>();
        currentNotifications = new HashSet<>();
        Sample probe = new Sample();
        String netzbetreiberId = null;

        this.configMapper.applyConfigs(object.getAttributes());

        if (object.getAttributes().containsKey("MESSSTELLE")) {
            probe.setMeasFacilId(object.getAttributes().get("MESSSTELLE"));
        }
        if (probe.getMeasFacilId() == null) {
            if (measFacilId == null) {
                currentErrors.add(
                    new ReportItem(
                        "MESSSTELLE", "", StatusCodes.IMP_MISSING_VALUE));
                errors.put(object.getIdentifier(),
                    new ArrayList<ReportItem>(currentErrors));
                return;
            }
            probe.setMeasFacilId(measFacilId);
        } else {
            MeasFacil mst = repository.entityManager().find(
                MeasFacil.class, probe.getMeasFacilId());
            if (mst == null) {
                currentErrors.add(
                    new ReportItem(
                        "MESSSTELLE",
                        probe.getMeasFacilId(), StatusCodes.IMP_INVALID_VALUE));
                errors.put(
                    object.getIdentifier(),
                    new ArrayList<ReportItem>(currentErrors));
                return;
            }
            netzbetreiberId = mst.getNetworkId();
        }

        if (object.getAttributes().containsKey("ZEITBASIS")) {
            String attribute = object.getAttributes().get("ZEITBASIS");
            QueryBuilder<Tz> builder = repository.queryBuilder(Tz.class)
                .and(Tz_.name, attribute);
            try {
                currentZeitbasis = repository.getSingle(builder.getQuery())
                    .getId();
            } catch (NoResultException e) {
                currentWarnings.add(
                    new ReportItem(
                        "ZEITBASIS",
                        object.getAttributes().get(
                            "ZEITBASIS"), StatusCodes.IMP_INVALID_VALUE));
            }
        } else if (object.getAttributes().containsKey("ZEITBASIS_S")) {
            currentZeitbasis =
                Integer.valueOf(object.getAttributes().get("ZEITBASIS_S"));
            Tz timezone = repository.entityManager()
                .find(Tz.class, currentZeitbasis);
            if (timezone == null) {
                currentWarnings.add(
                    new ReportItem(
                        "ZEITBASIS_S",
                        object.getAttributes().get(
                            "ZEITBASIS_S"), StatusCodes.IMP_INVALID_VALUE));
            }
        }

        // Fill the object with data
        for (Entry<String, String> attribute
                 : object.getAttributes().entrySet()) {
            addProbeAttribute(attribute, probe, netzbetreiberId);
        }
        if (probe.getApprLabId() == null) {
            probe.setApprLabId(probe.getMeasFacilId());
        }
        // Use the deskriptor string to find the medium
        factory.findMedia(probe);
        if (probe.getEnvMediumId() == null) {
            probe.setEnvMediumId(
                factory.findEnvMediumId(probe.getEnvDescripDisplay()));
        }

        // Check if the user is authorized to create the probe
        if (!authorizer.isAuthorized(probe, RequestMethod.POST)) {
            ReportItem err = new ReportItem();
            err.setCode(StatusCodes.NOT_ALLOWED);
            err.setKey(userInfo.getName());
            err.setValue("Messstelle " + probe.getMeasFacilId());
            currentWarnings.clear();
            currentErrors.add(err);
            errors.put(
                object.getIdentifier(),
                new ArrayList<ReportItem>(currentErrors));
            return;
        }

        // Compare the probe with objects in the db
        Sample newProbe = null;
        boolean oldProbeIsReadonly = false;
        try {
            Sample old = probeIdentifier.getExisting(probe);
            if (old != null) {
                // Matching probe was found in the db. Update it!
                if (
                    // Check if user belongs to matching measFacil
                    authorizer.isAuthorized(old, RequestMethod.POST)
                ) {
                    // Check if sample is read-only due to status
                    oldProbeIsReadonly = authorizer.isAuthorized(
                        old, RequestMethod.PUT);
                    if (oldProbeIsReadonly) {
                        newProbe = old;
                        currentNotifications.add(
                            new ReportItem(
                                "probe",
                                old.getExtId(),
                                StatusCodes.IMP_UNCHANGABLE));
                    } else {
                        merger.merge(old, probe);
                        newProbe = old;
                    }
                } else {
                    ReportItem err = new ReportItem();
                    err.setCode(StatusCodes.NOT_ALLOWED);
                    err.setKey(userInfo.getName());
                    err.setValue("Messstelle " + old.getMeasFacilId());
                    currentWarnings.clear();
                    currentErrors.add(err);
                    errors.put(
                        object.getIdentifier(),
                        new ArrayList<ReportItem>(currentErrors));
                    return;
                }
            } else {
                // It is a brand new probe!
                validator.validate(probe);
                if (!probe.hasErrors()) {
                    repository.create(probe);
                    newProbe = probe;

                    // Messages might be obsolete after importing other objects
                    newProbe.clearMessages();
                } else {
                    validate(probe, "validation#probe", false, true);
                }
            }
        } catch (Identifier.IdentificationException e) {
            // Sample was found but some data does not match
            ReportItem err = new ReportItem();
            err.setCode(StatusCodes.IMP_PRESENT);
            err.setKey("duplicate");
            err.setValue("");
            currentErrors.add(err);
            if (!currentErrors.isEmpty()) {
                errors.put(object.getIdentifier(),
                    new ArrayList<ReportItem>(currentErrors));
            }
            if (!currentWarnings.isEmpty()) {
                warnings.put(object.getIdentifier(),
                    new ArrayList<ReportItem>(currentWarnings));
            }
            if (!currentNotifications.isEmpty()) {
                notifications.put(object.getIdentifier(),
                    new ArrayList<ReportItem>(currentNotifications));
            }
            return;
        }
        if (newProbe != null) {
            importProbeIds.add(newProbe.getId());
        } else if (probe != null) {
            importProbeIds.add(probe.getId());
        }

        if (newProbe != null) {
            if (!oldProbeIsReadonly) {
                // Create kommentar objects
                for (Map<String, String> commRaw: object.getKommentare()) {
                    createProbeKommentar(commRaw, newProbe);
                }

                // Create zusatzwert objects
                List<SampleSpecifMeasVal> zusatzwerte = new ArrayList<>();
                for (Map<String, String> raw: object.getZusatzwerte()) {
                    SampleSpecifMeasVal tmp =
                        createZusatzwert(raw, newProbe.getId());
                    if (tmp != null) {
                        zusatzwerte.add(tmp);
                    }
                }
                // Persist zusatzwert objects
                merger.mergeZusatzwerte(newProbe, zusatzwerte);

                // Create site objects
                this.configMapper.applyConfigs(object.getEntnahmeOrt());
                for (Map<String, String> uOrt: object.getUrsprungsOrte()) {
                    this.configMapper.applyConfigs(uOrt);
                }
                // Special things for REI-Messpunkt
                if (probe.getReiAgGrId() != null
                    || Integer.valueOf(3).equals(probe.getRegulationId())
                    || Integer.valueOf(4).equals(probe.getRegulationId())
                ) {
                    createReiMesspunkt(object, newProbe);
                } else {
                    // Check if we have EOrte present
                    QueryBuilder<Geolocat> builderPresentEOrte = repository
                        .queryBuilder(Geolocat.class)
                        .and(Geolocat_.sampleId, newProbe.getId())
                        .and(Geolocat_.typeRegulation, "E");
                    List<Geolocat> presentEOrte =
                        repository.filter(builderPresentEOrte.getQuery());

                    // Check if we have UOrte present
                    QueryBuilder<Geolocat> builderPresentUOrte = repository
                        .queryBuilder(Geolocat.class)
                        .and(Geolocat_.sampleId, newProbe.getId())
                        .and(Geolocat_.typeRegulation, "U");
                    List<Geolocat> presentUOrte =
                        repository.filter(builderPresentUOrte.getQuery());

                    // Check if we have ROrte present
                    QueryBuilder<Geolocat> builderPresentROrte = repository
                        .queryBuilder(Geolocat.class)
                        .and(Geolocat_.sampleId, newProbe.getId())
                        .and(Geolocat_.typeRegulation, "R");
                    List<Geolocat> presentROrte =
                        repository.filter(builderPresentROrte.getQuery());

                    //Switch if we need to create an R-Ort
                    Boolean rOrt = false;
                    // First create or find entnahmeOrte and ursprungsOrte
                    // Create the new entnahmeOrt but do not persist
                    Geolocat eOrt = createOrtszuordnung(
                        object.getEntnahmeOrt(), "E", newProbe);

                    //Create/Find Ursprungsort(e) from LAF
                    List<Geolocat> uOrte = new ArrayList<>();
                    //If object.getUrsprungsOrte().size() > 1
                    for (Map<String, String> raw: object.getUrsprungsOrte()) {
                        Geolocat tmp = createOrtszuordnung(raw, "U", newProbe);
                        if (tmp != null) {
                            uOrte.add(tmp);
                        }
                    }

                    // If the LAF delivers eOrt and uOrt and those are a match
                    // by created/found - Id -- we need to create an R-Ort
                    if (uOrte.size() > 0
                        && eOrt != null
                        && uOrte.stream().anyMatch(
                            uOrt -> uOrt.getSiteId().equals(
                                eOrt.getSiteId()))) {
                        rOrt = true;
                    }

                    //further conditionals for eOrt
                    if (eOrt != null) {
                        //Check if the new Ort matches an U-Ort if exists
                        if (presentUOrte.size() > 0
                            && eOrt != null
                            && presentUOrte.stream().anyMatch(
                                uOrt -> uOrt.getSiteId().equals(
                                    eOrt.getSiteId()))) {
                            rOrt = true;
                        } else if (presentROrte.size() > 0
                            && eOrt != null
                            && presentROrte.stream().anyMatch(
                                rtypeOrt -> rtypeOrt.getSiteId().equals(
                                    eOrt.getSiteId()))) {
                            rOrt = true;
                        } else if (presentROrte.size() > 0 && eOrt != null) {
                            for (Geolocat loc: presentROrte) {
                                loc.setTypeRegulation("U");
                                repository.update(loc);
                            }
                            rOrt = false;
                        }
                    }

                    //conditionals for the ursprungsOrte
                    if (uOrte.size() == 1) {
                        // Check if the new Ort matches the U-Ort if exists,
                        // this only works if we have 1 Ursprungsort in the LAF,
                        // if we have more we must assume multiple ursprungsorte
                        if (presentEOrte.size() > 0
                            && uOrte.size() > 0
                            && presentEOrte.stream().anyMatch(
                                etypeOrt -> etypeOrt.getSiteId().equals(
                                    uOrte.get(0).getSiteId()))) {
                            rOrt = true;
                        } else if (presentROrte.size() > 0
                            && uOrte.size() > 0
                            && presentROrte.stream().anyMatch(
                                rtypeOrt -> rtypeOrt.getSiteId().equals(
                                    uOrte.get(0).getSiteId()))) {
                            //ToDo: We need to handle R-Orte!
                            rOrt = true;
                        } else if (presentROrte.size() > 0
                            && uOrte.size() > 0) {
                            for (Geolocat loc: presentROrte) {
                                loc.setTypeRegulation("E");
                                repository.update(loc);
                            }
                            rOrt = false;
                        }
                    }

                    if (!rOrt) {
                        //persist general entnahmeOrt
                        if (eOrt != null) {
                            merger.mergeEntnahmeOrt(newProbe.getId(), eOrt);
                        }
                        if (uOrte.size() > 0) {
                            //remove present U-Orte
                            QueryBuilder<Geolocat> builderUOrt = repository
                                .queryBuilder(Geolocat.class)
                                .and(Geolocat_.sampleId, newProbe.getId())
                                .and(Geolocat_.typeRegulation, "U");
                            List<Geolocat> uOrteProbe =
                                repository.filter(builderUOrt.getQuery());
                            if (!uOrteProbe.isEmpty()) {
                                for (Geolocat elemOrt : uOrteProbe) {
                                    repository.delete(elemOrt);
                                }
                            }
                            merger.mergeUrsprungsOrte(newProbe.getId(), uOrte);
                        }
                    } else {
                        if (rOrt && presentROrte.size() == 1) {
                            // we may have additional information for
                            // the ortszuordnung such as an ortszusatz,
                            // we make an update.
                            QueryBuilder<Geolocat> builderUOrt = repository
                                .queryBuilder(Geolocat.class)
                                .and(Geolocat_.sampleId, newProbe.getId())
                                .and(Geolocat_.typeRegulation, "R");
                            List<Geolocat> uOrteProbe =
                                repository.filter(builderUOrt.getQuery());
                            if (!uOrteProbe.isEmpty()) {
                                for (Geolocat elemOrt : uOrteProbe) {
                                    repository.delete(elemOrt);
                                }
                            }

                            if ((eOrt != null)) {
                                eOrt.setTypeRegulation(("R"));
                                merger.mergeEntnahmeOrt(newProbe.getId(), eOrt);
                            }
                            if (uOrte.size() == 1 && eOrt == null) {
                                uOrte.get(0).setTypeRegulation("R");
                                merger.mergeUrsprungsOrte(
                                    newProbe.getId(), uOrte);
                            }
                        }
                        // clean up ursprungsorte before!
                        if (object.getUrsprungsOrte().size() > 0
                            || presentUOrte.size() > 0) {
                            QueryBuilder<Geolocat> builderUOrt = repository
                                .queryBuilder(Geolocat.class)
                                .and(Geolocat_.sampleId, newProbe.getId())
                                .and(Geolocat_.typeRegulation, "U");
                            List<Geolocat> uOrteProbe =
                                repository.filter(builderUOrt.getQuery());
                            if (!uOrteProbe.isEmpty()) {
                                for (Geolocat elemOrt : uOrteProbe) {
                                    repository.delete(elemOrt);
                                }
                            }
                        }
                        if (eOrt != null) {
                            eOrt.setTypeRegulation("R");
                            //Merging the entnahmeOrt cleans it up!
                            merger.mergeEntnahmeOrt(newProbe.getId(), eOrt);
                        } else if (uOrte.size() == 1) {
                            // Clean-up entnahmeOrte before merge
                            QueryBuilder<Geolocat> builderEOrt = repository
                                .queryBuilder(Geolocat.class)
                                .and(Geolocat_.sampleId, newProbe.getId())
                                .and(Geolocat_.typeRegulation, "E");
                            List<Geolocat> eOrteProbe =
                                repository.filter(builderEOrt.getQuery());
                            if (!eOrteProbe.isEmpty()) {
                                for (Geolocat elemOrt : eOrteProbe) {
                                    repository.delete(elemOrt);
                                }
                            }

                            uOrte.get(0).setTypeRegulation("R");
                            merger.mergeUrsprungsOrte(newProbe.getId(), uOrte);
                        }
                    }
                }
            }

            // Validate probe object
            validate(newProbe, "validation#probe");

            // Create measms
            for (LafRawData.Messung measmRaw: object.getMessungen()) {
                create(measmRaw, newProbe);
            }

            // If key SZENARIO is present in imported file, assign
            // global tag to probe and its messung objects
            if (object.getAttributes().containsKey("SZENARIO")) {
                //assign to probe object
                assignGlobalTag(
                    object.getAttributes().get("SZENARIO"), newProbe);
                //assign to messung objects
                QueryBuilder<Measm> builderMessung = repository
                    .queryBuilder(Measm.class)
                    .and(Measm_.sampleId, newProbe.getId());
                List<Measm> messungen =
                    repository.filter(builderMessung.getQuery());
                for (Measm messung: messungen) {
                    assignGlobalTag(
                        object.getAttributes().get("SZENARIO"), messung);
                }
            }
        }
        if (!currentErrors.isEmpty()) {
            if (errors.containsKey(object.getIdentifier())) {
                errors.get(object.getIdentifier()).addAll(currentErrors);
            } else {
                errors.put(object.getIdentifier(),
                    new ArrayList<ReportItem>(currentErrors));
            }
        }
        if (!currentWarnings.isEmpty()) {
            if (warnings.containsKey(object.getIdentifier())) {
                warnings.get(object.getIdentifier()).addAll(currentWarnings);
            } else {
                warnings.put(object.getIdentifier(),
                    new ArrayList<ReportItem>(currentWarnings));
            }
        }
        if (!currentNotifications.isEmpty()) {
            if (notifications.containsKey(object.getIdentifier())) {
                notifications.get(
                    object.getIdentifier()).addAll(currentNotifications);
            } else {
                notifications.put(object.getIdentifier(),
                    new ArrayList<ReportItem>(currentNotifications));
            }
        }
    }

    private void create(LafRawData.Messung object, Sample probe) {
        Measm messung = new Measm();
        messung.setSampleId(probe.getId());

        // Fill the new messung with data
        this.configMapper.applyConfigs(object.getAttributes());
        for (Entry<String, String> attribute
                 : object.getAttributes().entrySet()
        ) {
            addMessungAttribute(attribute, messung);
        }
        // Check if the user is authorized to create the object
        if (!authorizer.isAuthorized(messung, RequestMethod.POST)) {
            ReportItem warn = new ReportItem();
            warn.setCode(StatusCodes.NOT_ALLOWED);
            warn.setKey(userInfo.getName());
            warn.setValue("Messung: " + messung.getMinSampleId());
            currentErrors.add(warn);
            return;
        }

        // Compare with messung objects in the db
        Measm newMessung;
        try {
            Measm old = messungIdentifier.getExisting(messung);
            if (old != null) {
                if (!authorizer.isAuthorized(old, RequestMethod.PUT)) {
                    currentNotifications.add(
                        new ReportItem(
                            "messung",
                            old.getExtId(),
                            StatusCodes.IMP_UNCHANGABLE));
                    return;
                } else {
                    merger.mergeMessung(old, messung);
                    newMessung = old;
                }
            } else {
                // Check if Messung has all fields that have db constraints
                // (validation rule?)
                if (messung.getMmtId() == null) {
                    ReportItem err2 = new ReportItem();
                    err2.setCode(StatusCodes.VALUE_MISSING);
                    err2.setKey("not valid (missing Messmethode)");
                    err2.setValue("Messung: " + messung.getMinSampleId());
                    currentErrors.add(err2);
                    return;
                }

                // Create a new messung and the first status
                newMessung = repository.create(messung);
            }
        } catch (Identifier.IdentificationException e) {
            ReportItem err = new ReportItem();
            err.setCode(StatusCodes.VALUE_MISSING);
            err.setKey("identification");
            err.setValue("Messung");
            currentErrors.add(err);
            return;
        }

        // Add commMeasms
        for (Map<String, String> commRaw: object.getKommentare()) {
            createMessungKommentar(commRaw, newMessung.getId(), probe);
        }

        // Add measVals
        List<MeasVal> messwerte = new ArrayList<MeasVal>();
        List<Integer> messgroessenListe = new ArrayList<Integer>();
        for (Map<String, String> measValRaw: object.getMesswerte()) {
            MeasVal tmp =
                createMesswert(measValRaw, newMessung.getId());
            if (tmp != null) {
                //find duplicates
                if (messgroessenListe.contains(tmp.getMeasdId())) {
                    currentWarnings.add(new ReportItem(
                            measValRaw.get("MESSGROESSE_ID") == null
                            ? "MESSWERT - MESSGROESSE"
                            : "MESSWERT - MESSGROESSE_ID",
                            measValRaw.get("MESSGROESSE_ID") == null
                            ? measValRaw.get("MESSGROESSE")
                            : measValRaw.get("MESSGROESSE_ID"),
                            StatusCodes.IMP_DUPLICATE));
                } else {
                    messwerte.add(tmp);
                    messgroessenListe.add(tmp.getMeasdId());
                }
            }
        }
        messwerte = messwertNormalizer.normalizeMesswerte(
            messwerte, probe.getEnvMediumId());
        //persist messwerte
        merger.mergeMesswerte(newMessung, messwerte);

        // Check for warnings and errors for messung ...
        validate(newMessung, "validation#messung");
        // ... and messwerte
        for (MeasVal messwert: messwerte) {
            validate(messwert, "validation#messwert");
        }

        // Validate / Create Status
        if (!object.hasErrors()) {
            if (object.getAttributes().containsKey("BEARBEITUNGSSTATUS")) {
                createStatusProtokoll(
                    object.getAttributes().get("BEARBEITUNGSSTATUS"),
                    newMessung,
                    probe.getMeasFacilId());
            }
        }
    }

    private void createProbeKommentar(
        Map<String, String> attributes,
        Sample probe
    ) {
        this.configMapper.applyConfigs(attributes);

        if (attributes.get("TEXT").equals("")) {
            currentWarnings.add(
                new ReportItem(
                    "PROBENKOMMENTAR", "Text", StatusCodes.VALUE_MISSING));
            return;
        }

        // Duplicates validation rule because the rule generates an error
        // and it should only be a notification here
        QueryBuilder<CommSample> kommentarBuilder = repository
            .queryBuilder(CommSample.class)
            .and(CommSample_.sampleId, probe.getId());
        List<CommSample> kommentarExist = repository.filter(
            kommentarBuilder.getQuery());
        // TODO: Should be the job of EXISTS and a WHERE-clause in database
        if (kommentarExist.stream().anyMatch(
                elem -> elem.getText().trim().replace(" ", "").toUpperCase()
                .equals(attributes.get("TEXT").trim().replace(" ", "")
                    .toUpperCase()))
        ) {
            currentNotifications.add(
                new ReportItem(
                    "PROBENKOMMENTAR",
                    attributes.get("TEXT"),
                    StatusCodes.IMP_DUPLICATE));
            return;
        }

        CommSample kommentar = new CommSample();
        kommentar.setSampleId(probe.getId());
        kommentar.setText(attributes.get("TEXT"));
        if (attributes.containsKey("MST_ID")) {
            kommentar.setMeasFacilId(attributes.get("MST_ID"));
        } else {
            kommentar.setMeasFacilId(probe.getMeasFacilId());
        }
        if (attributes.containsKey("DATE")) {
            String date = attributes.get("DATE") + " " + attributes.get("TIME");
            kommentar.setDate(getDate(date));
        } else {
            kommentar.setDate(
                Timestamp.from(
                    Instant.now().atZone(ZoneOffset.UTC).toInstant()));
        }
        if (!userInfo.getMessstellen().contains(kommentar.getMeasFacilId())) {
            currentWarnings.add(
                new ReportItem(
                    userInfo.getName(),
                    "Kommentar: " + kommentar.getMeasFacilId(),
                    StatusCodes.NOT_ALLOWED));
            return;
        }

        validate(kommentar, "Status ");
        if (kommentar.hasErrors() || kommentar.hasWarnings()) {
            return;
        }

        repository.create(kommentar);
    }

    private SampleSpecifMeasVal createZusatzwert(
        Map<String, String> attributes,
        int probeId
    ) {
        this.configMapper.applyConfigs(attributes);

        SampleSpecifMeasVal zusatzwert = new SampleSpecifMeasVal();
        zusatzwert.setSampleId(probeId);

        if (attributes.containsKey("MESSFEHLER")) {
            zusatzwert.setError(
                Float.valueOf(
                    attributes.get("MESSFEHLER").replaceAll(",", ".")));
        }

        String wert = attributes.get("MESSWERT_PZS");
        if (wert.startsWith("<")) {
            wert = wert.substring(1);
            zusatzwert.setSmallerThan("<");
        }
        zusatzwert.setMeasVal(Double.valueOf(wert.replaceAll(",", ".")));

        String attribute = attributes.get("PZS");
        boolean isId = false;
        if (attribute == null) {
            attribute = attributes.get("PZS_ID");
            isId = true;
        }

        SingularAttribute<SampleSpecif, String> field = isId
            ? SampleSpecif_.id : SampleSpecif_.extId;
        QueryBuilder<SampleSpecif> builder = repository
            .queryBuilder(SampleSpecif.class)
            .and(field, attribute);
        List<SampleSpecif> zusatz = repository.filter(builder.getQuery());
        if (zusatz == null || zusatz.isEmpty()) {
            currentWarnings.add(new ReportItem(
                    isId ? "PROBENZUSATZBESCHREIBUNG" : "PZB_S",
                    attribute,
                    StatusCodes.IMP_INVALID_VALUE));
            return null;
        }
        zusatzwert.setSampleSpecifId(zusatz.get(0).getId());

        validate(zusatzwert, "validation#probe");

        return zusatzwert;
    }

    private MeasVal createMesswert(
        Map<String, String> attributes,
        int messungsId
    ) {
        this.configMapper.applyConfigs(attributes);

        MeasVal messwert = new MeasVal();
        messwert.setMeasmId(messungsId);

        if (attributes.containsKey("MESSGROESSE_ID")) {
            Measd measd = repository.entityManager().find(
                Measd.class,
                Integer.valueOf(attributes.get("MESSGROESSE_ID"))
            );
            if (measd == null) {
                currentWarnings.add(
                    new ReportItem(
                        "MESSWERT - MESSGROESSE_ID",
                        attributes.get("MESSGROESSE_ID"),
                        StatusCodes.IMP_INVALID_VALUE));
                return null;
            }
            messwert.setMeasdId(
                Integer.valueOf(attributes.get("MESSGROESSE_ID")));
        } else if (attributes.containsKey("MESSGROESSE")) {
            String attribute = attributes.get("MESSGROESSE");
            // accept various nuclide notations (e.g.
            // "Cs-134", "CS 134", "Cs134", "CS134", ...)
            String messgroesseString = attribute;
            if (attribute.matches("^[A-Za-z]+( |-)?[0-9].*")) {
                messgroesseString = attribute.substring(0, 1).toUpperCase()
                    + attribute.replaceAll("(-| )?[0-9].*", "")
                        .substring(1).toLowerCase()
                    + '-'
                    + attribute.replaceFirst("^[A-Za-z]*(-| )?", "")
                        .toLowerCase();
            }

            QueryBuilder<Measd> builder = repository.queryBuilder(Measd.class)
                .and(Measd_.name, messgroesseString);
            List<Measd> groesse = repository.filter(builder.getQuery());
            if (groesse == null || groesse.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        "MESSWERT - MESSGROESSE",
                        attributes.get("MESSGROESSE"),
                        StatusCodes.IMP_INVALID_VALUE));
                return null;
            }
            messwert.setMeasdId(groesse.get(0).getId());
        }
        if (attributes.containsKey("MESSEINHEIT_ID")) {
            MeasUnit measUnit = repository.entityManager().find(
                MeasUnit.class,
                Integer.valueOf(attributes.get("MESSEINHEIT_ID"))
            );
            if (measUnit == null) {
                currentWarnings.add(
                    new ReportItem(
                        "MESSWERT - MESSEINHEIT_ID",
                        attributes.get("MESSEINHEIT_ID"),
                        StatusCodes.IMP_INVALID_VALUE));
                return null;
            }
            messwert.setMeasUnitId(
                Integer.valueOf(attributes.get("MESSEINHEIT_ID")));
        } else if (attributes.containsKey("MESSEINHEIT")) {
            String attribute = attributes.get("MESSEINHEIT");
            QueryBuilder<MeasUnit> builder = repository
                .queryBuilder(MeasUnit.class)
                .and(MeasUnit_.unitSymbol, attribute);
            List<MeasUnit> einheit = repository.filter(builder.getQuery());
            if (einheit == null || einheit.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        "MESSWERT - MESSEINHEIT",
                        attribute,
                        StatusCodes.IMP_INVALID_VALUE));
                return null;
            }
            messwert.setMeasUnitId(einheit.get(0).getId());
        }

        String wert = attributes.get("MESSWERT");
        if (wert.startsWith("<")) {
            wert = wert.substring(1);
            messwert.setLessThanLOD("<");
        }
        messwert.setMeasVal(Double.valueOf(wert.replaceAll(",", ".")));
        if (attributes.containsKey("MESSFEHLER")) {
            messwert.setError(
                Double.valueOf(
                    attributes.get("MESSFEHLER")
                        .replaceAll(",", ".")).floatValue());
        }
        if (attributes.containsKey("NWG")) {
            messwert.setDetectLim(
                Double.valueOf(attributes.get("NWG").replaceAll(",", ".")));
        }
        if (attributes.containsKey("GRENZWERT")) {
            messwert.setIsThreshold(
                attributes.get("GRENZWERT").equalsIgnoreCase("J"));
        }
        if (messwert.getLessThanLOD() != null
            && messwert.getDetectLim() == null
        ) {
            messwert.setDetectLim(messwert.getMeasVal());
            messwert.setMeasVal(null);
        } else if (messwert.getLessThanLOD() != null
            && messwert.getMeasVal().equals(messwert.getDetectLim())
            || messwert.getLessThanLOD() != null
            && messwert.getMeasVal() == 0.0
        ) {
            messwert.setMeasVal(null);
        }
        if (messwert.getError() != null) {
            if (messwert.getLessThanLOD() != null
                && messwert.getError() == 0
            ) {
                messwert.setError(null);
            }
        }
        return messwert;
    }

    private void createMessungKommentar(
        Map<String, String> attributes,
        int messungsId,
        Sample probe
    ) {
        this.configMapper.applyConfigs(attributes);

        if (attributes.get("TEXT").equals("")) {
            currentWarnings.add(
                new ReportItem("KOMMENTAR", "Text", StatusCodes.VALUE_MISSING));
            return;
        }
        CommMeasm kommentar = new CommMeasm();
        kommentar.setMeasmId(messungsId);
        if (attributes.containsKey("MST_ID")) {
            kommentar.setMeasFacilId(attributes.get("MST_ID"));
        } else {
            kommentar.setMeasFacilId(probe.getMeasFacilId());
        }
        if (attributes.containsKey("DATE")) {
            String date = attributes.get("DATE") + " " + attributes.get("TIME");
            kommentar.setDate(getDate(date));
        } else {
            kommentar.setDate(
                Timestamp.from(
                    Instant.now().atZone(ZoneOffset.UTC).toInstant()));
        }

        // Duplicates validation rule because the rule generates an error
        // and it should only be a notification here
        QueryBuilder<CommMeasm> kommentarBuilder = repository
            .queryBuilder(CommMeasm.class)
            .and(CommMeasm_.measmId, messungsId);
        List<CommMeasm> kommentarExist = repository.filter(
            kommentarBuilder.getQuery());
        // TODO: Should be the job of EXISTS and a WHERE-clause in database
        if (kommentarExist.stream().anyMatch(
                elem -> elem.getText().trim().replace(" ", "").toUpperCase()
                .equals(attributes.get("TEXT").trim().replace(" ", "")
                    .toUpperCase()))
        ) {
            currentNotifications.add(
                new ReportItem(
                    "MESSUNGKOMMENTAR",
                    attributes.get("TEXT"),
                    StatusCodes.IMP_DUPLICATE));
            return;
        }

        kommentar.setText(attributes.get("TEXT"));
        if (!userInfo.getMessstellen().contains(kommentar.getMeasFacilId())) {
            currentWarnings.add(
                new ReportItem(
                    userInfo.getName(),
                    "Messungs Kommentar: " + kommentar.getMeasFacilId(),
                    StatusCodes.NOT_ALLOWED));
            return;
        }

        validate(kommentar, "Status ", true, false);
        if (kommentar.hasErrors() || kommentar.hasWarnings()) {
            return;
        }
        repository.create(kommentar);
    }

    private void createStatusProtokoll(
        String status,
        Measm messung,
        String mstId
    ) {
        //check for warnings in Probeobject - if true prevent status 7
        Boolean probeWarnings = true;
        probeWarnings = currentWarnings.stream().anyMatch(
            elem -> (elem.getKey().equals("validation#probe")));

        for (int i = 1; i <= 3; i++) {
            if (status.substring(i - 1, i).equals("0")) {
                // no further status settings
                return;
            } else if (currentErrors.isEmpty() && currentWarnings.isEmpty()) {
                if (!addStatusProtokollEntry(
                        i,
                        Integer.valueOf(status.substring(i - 1, i)),
                        messung,
                        mstId)
                ) {
                    return;
                }
            } else if (status.substring(i - 1, i).equals("7")
                && !probeWarnings
            ) {
                if (!addStatusProtokollEntry(
                        i,
                        Integer.valueOf(status.substring(i - 1, i)),
                        messung,
                        mstId)
                ) {
                    return;
                }
            } else {
                currentErrors.add(
                    new ReportItem(
                        "Statusvergabe", "Status", StatusCodes.VALUE_MISSING));
                return;
            }
        }
    }

    private boolean addStatusProtokollEntry(
        int statusStufe,
        int statusWert,
        Measm messung,
        String mstId
    ) {
        // validation check of new status entries
        int newKombi = 0;
        try {
            newKombi = ((StatusMp) repository.entityManager()
                .createNativeQuery("SELECT * FROM master.status_mp "
                    + "WHERE status_lev_id = :statusLev "
                    + "AND status_val_id = :statusVal",
                    StatusMp.class)
                .setParameter("statusVal", statusWert)
                .setParameter("statusLev", statusStufe)
                .getSingleResult()).getId();
        } catch (NoResultException nre) {
            currentWarnings.add(
                new ReportItem(
                    "status#" + statusStufe,
                    statusWert,
                    StatusCodes.IMP_INVALID_VALUE));
            return false;
        }
        // get current status kombi
        StatusMp currentKombi = repository.getById(
            StatusMp.class, messung.getStatusProt().getStatusMpId());
        // check if erreichbar
        QueryBuilder<StatusAccessMpView> errFilter = repository
            .queryBuilder(StatusAccessMpView.class)
            .and(StatusAccessMpView_.statusLevId, statusStufe)
            .and(StatusAccessMpView_.statusValId, statusWert)
            .and(StatusAccessMpView_.curLevId,
                currentKombi.getStatusLev().getId())
            .and(StatusAccessMpView_.curValId,
                currentKombi.getStatusVal().getId());
        List<StatusAccessMpView> erreichbar =
            repository.filter(errFilter.getQuery());
        if (erreichbar.isEmpty()) {
            currentWarnings.add(
                new ReportItem(
                    "status#" + statusStufe,
                    statusWert,
                    StatusCodes.IMP_INVALID_VALUE));
            return false;
        }

        //Cleanup Messwerte for Status 7
        QueryBuilder<MeasVal> builderMW = repository
            .queryBuilder(MeasVal.class)
            .and(MeasVal_.measmId, messung.getId());
        List<MeasVal> messwerte =
            repository.filter(builderMW.getQuery());
        boolean hasValidMesswerte = false;
        if (!messwerte.isEmpty() && statusWert == 7) {
            for (MeasVal messwert: messwerte) {

                boolean hasNoMesswert = false;

                if (messwert.getMeasVal() == null
                    && messwert.getLessThanLOD() == null) {
                    hasNoMesswert = true;
                }
                if (!hasNoMesswert) {
                    hasValidMesswerte = true;
                    currentWarnings.add(
                        new ReportItem(
                            "status#" + statusStufe,
                            statusWert,
                            StatusCodes.STATUS_RO));
                }
                if (hasValidMesswerte) {
                    return false;
                }
            }

            if (statusWert == 7 && !hasValidMesswerte) {
                for (MeasVal mv: messwerte) {
                    repository.delete(mv);
                }
            }
        }

        // Validator: StatusAssignment
        StatusProt newStatus = new StatusProt();
        newStatus.setDate(new Timestamp(new Date().getTime()));
        newStatus.setMeasmId(messung.getId());
        newStatus.setMeasFacilId(mstId);
        newStatus.setStatusMpId(newKombi);

        validate(newStatus, "Status ");
        if (newStatus.hasErrors() || newStatus.hasWarnings()) {
            return false;
        }

        // check auth
        if (authorizer.isAuthorized(newStatus, RequestMethod.POST)) {
            //persist newStatus if authorized to do so
            repository.create(newStatus);
            if (newKombi == 0 || newKombi == 9 || newKombi == 13) {
                messung.setIsCompleted(false);
            } else {
                messung.setIsCompleted(true);
            }
            repository.update(messung);
            return true;
        } else {
            currentWarnings.add(
                new ReportItem(
                    "status#" + statusStufe,
                    statusWert,
                    StatusCodes.NOT_ALLOWED));
            return false;
        }
    }

    private void createReiMesspunkt(LafRawData.Sample object, Sample probe) {
        QueryBuilder<Geolocat> builder = repository.queryBuilder(Geolocat.class)
            .and(Geolocat_.sampleId, probe.getId());
        List<Geolocat> zuordnungen =
            repository.filter(builder.getQuery());
        if (!zuordnungen.isEmpty()) {
            // Sample already has an ort.
            return;
        }

        List<Map<String, String>> uort = object.getUrsprungsOrte();
        if (uort.size() > 0
            && uort.get(0).containsKey("U_ORTS_ZUSATZCODE")
        ) {
            // WE HAVE A REI-MESSPUNKT!
            // Search for the ort in db
            Map<String, String> uo = uort.get(0);
            QueryBuilder<Site> builder1 = repository.queryBuilder(Site.class)
                .and(Site_.extId, uo.get("U_ORTS_ZUSATZCODE"));
            List<Site> messpunkte =
                repository.filter(builder1.getQuery());
            if (!messpunkte.isEmpty()) {
                Geolocat ort = new Geolocat();
                ort.setTypeRegulation("R");
                ort.setSampleId(probe.getId());
                ort.setSiteId(messpunkte.get(0).getId());
                ort.setPoiId(messpunkte.get(0).getPoiId());
                if (uo.containsKey("U_ORTS_ZUSATZTEXT")) {
                    ort.setAddSiteText(uo.get("U_ORTS_ZUSATZTEXT"));
                }
                repository.create(ort);
                probe.setNuclFacilGrId(messpunkte.get(0).getNuclFacilGrId());
                repository.update(probe);
            } else if (uo.get("U_ORTS_ZUSATZCODE").length() == 4) {
                QueryBuilder<NuclFacilGr> builderKta = repository
                    .queryBuilder(NuclFacilGr.class)
                    .and(NuclFacilGr_.extId, uo.get("U_ORTS_ZUSATZCODE"));
                List<NuclFacilGr> ktaGrp =
                    repository.filter(builderKta.getQuery());
                if (!ktaGrp.isEmpty()) {
                    Site o = null;
                    // Check for Koordinates U_Ort (primary):
                    // If none are present, assume Koordinates
                    //in P_Ort. If P_Ort is not valid - this import must fail.
                    if (uort.get(0).get("U_KOORDINATEN_ART_S") != null
                        && !uort.get(0).get("U_KOORDINATEN_ART_S").equals("")
                        && uort.get(0).get("U_KOORDINATEN_X") != null
                        && !uort.get(0).get("U_KOORDINATEN_X").equals("")
                        && uort.get(0).get("U_KOORDINATEN_Y") != null
                        && !uort.get(0).get("U_KOORDINATEN_Y").equals("")
                    ) {
                        o = findOrCreateOrt(uort.get(0), "U_", probe);
                    }

                    if (o == null) {
                        Site oE = findOrCreateOrt(
                            object.getEntnahmeOrt(), "P_", probe);
                        if (oE == null) {
                            ReportItem warn = new ReportItem();
                            warn.setCode(StatusCodes.VALUE_MISSING);
                            warn.setKey("Ort");
                            warn.setValue("Kein Messpunkt angelegt");
                            currentWarnings.add(warn);
                            return;
                        } else {
                            o = oE;
                        }
                    } else {
                        o.setSiteClassId(Site.SiteClassId.DYN);
                        o.setNuclFacilGrId(ktaGrp.get(0).getId());
                        repository.update(o);

                        Geolocat ort = new Geolocat();
                        ort.setSiteId(o.getId());
                        ort.setTypeRegulation("R");
                        ort.setSampleId(probe.getId());
                        ort.setPoiId(o.getPoiId());

                        repository.create(ort);

                        probe.setNuclFacilGrId(ktaGrp.get(0).getId());
                        repository.update(probe);
                    }
                } else {
                    ReportItem warn = new ReportItem();
                    warn.setCode(StatusCodes.VALUE_NOT_MATCHING);
                    warn.setKey("Ort");
                    warn.setValue(uo.get("U_ORTS_ZUSATZCODE"));
                    currentWarnings.add(warn);
                }
            } else {
                ReportItem warn = new ReportItem();
                warn.setCode(StatusCodes.VALUE_NOT_MATCHING);
                warn.setKey("Ort");
                warn.setValue(uo.get("U_ORTS_ZUSATZCODE"));
                currentWarnings.add(warn);
            }
        } else {
            Site o = null;
            if (uort.size() > 0) {
                o = findOrCreateOrt(uort.get(0), "U_", probe);
            }
            if (o == null) {
                o = findOrCreateOrt(object.getEntnahmeOrt(), "P_", probe);
            }
            if (o == null) {
                return;
            }
            o.setSiteClassId(Site.SiteClassId.REI);
            repository.update(o);
            Geolocat ort = new Geolocat();
            ort.setSiteId(o.getId());
            ort.setTypeRegulation("R");
            ort.setSampleId(probe.getId());
            if (uort.size() > 0
                && uort.get(0).containsKey("U_ORTS_ZUSATZCODE")
            ) {
                Map<String, String> uo = uort.get(0);
                o.setExtId(uo.get("U_ORTS_ZUSATZCODE"));
                if (uo.containsKey("U_ORTS_ZUSATZTEXT")) {
                    ort.setAddSiteText(uo.get("U_ORTS_ZUSATZTEXT"));
                }
            }
            repository.create(ort);
        }
        return;
    }

    private Geolocat createOrtszuordnung(
        Map<String, String> rawOrt,
        String type,
        Sample probe
    ) {
        if (rawOrt.isEmpty()) {
            return null;
        }
        Geolocat ort = new Geolocat();
        ort.setTypeRegulation(type);
        ort.setSampleId(probe.getId());
        if (type.equals("E")) {
            type = "P";
        }
        Site o = findOrCreateOrt(rawOrt, type + "_", probe);
        if (o == null) {
            return null;
        }
        ort.setSiteId(o.getId());
        ort.setPoiId(o.getPoiId());
        if (rawOrt.containsKey(type + "_ORTS_ZUSATZCODE")) {
            Poi zusatz = repository.entityManager().find(
                Poi.class,
                rawOrt.get(type + "_ORTS_ZUSATZCODE")
            );
            if (zusatz == null) {
                currentWarnings.add(
                    new ReportItem(
                        type + "_ORTS_ZUSATZCODE",
                        rawOrt.get(type + "_ORTS_ZUSATZCODE"),
                        StatusCodes.IMP_INVALID_VALUE));
            } else {
                ort.setPoiId(zusatz.getId());
            }
        }
        if (rawOrt.containsKey(type + "_ORTS_ZUSATZTEXT")) {
            ort.setAddSiteText(rawOrt.get(type + "_ORTS_ZUSATZTEXT"));
        }
        return ort;
    }

    private Site findOrCreateOrt(
        Map<String, String> attributes,
        String type,
        Sample probe
    ) {
        Site o = new Site();

        if ((attributes.get(type + "KOORDINATEN_ART") != null
                || attributes.get(type + "KOORDINATEN_ART_S") != null)
            && !attributes.get(type + "KOORDINATEN_X").equals("")
            && attributes.get(type + "KOORDINATEN_X") != null
            && !attributes.get(type + "KOORDINATEN_Y").equals("")
            && attributes.get(type + "KOORDINATEN_Y") != null
        ) {
            if (attributes.get(type + "KOORDINATEN_ART_S") != null) {
                o.setSpatRefSysId(Integer.valueOf(
                        attributes.get(type + "KOORDINATEN_ART_S")));
                SpatRefSys spatRefSys = repository.entityManager().find(
                    SpatRefSys.class, o.getSpatRefSysId());
                if (spatRefSys == null) {
                    currentWarnings.add(
                        new ReportItem(
                            type + "KOORDINATEN_ART_S",
                            attributes.get(type + "KOORDINATEN_ART_S"),
                            StatusCodes.IMP_INVALID_VALUE));
                    o.setSpatRefSysId(null);
                }
            } else {
                QueryBuilder<SpatRefSys> kdaBuilder = repository
                    .queryBuilder(SpatRefSys.class)
                    .and(SpatRefSys_.name, attributes.get(type + "KOORDINATEN_ART"));
                List<SpatRefSys> arten =
                    repository.filter(kdaBuilder.getQuery());
                if (arten == null || arten.isEmpty()) {
                    currentWarnings.add(
                        new ReportItem(
                            type + "KOORDINATEN_ART",
                            attributes.get(type + "KOORDINATEN_ART"),
                            StatusCodes.IMP_INVALID_VALUE));
                    o.setSpatRefSysId(null);
                } else {
                    o.setSpatRefSysId(arten.get(0).getId());
                }
            }
            o.setCoordXExt(attributes.get(type + "KOORDINATEN_X"));
            o.setCoordYExt(attributes.get(type + "KOORDINATEN_Y"));
        }

        if (attributes.get(type + "GEMEINDENAME") != null) {
            QueryBuilder<AdminUnit> builder = repository
                .queryBuilder(AdminUnit.class)
                .and(AdminUnit_.name, attributes.get(type + "GEMEINDENAME"));
            List<AdminUnit> ves =
                repository.filter(builder.getQuery());
            if (ves == null || ves.size() == 0) {
                currentWarnings.add(
                    new ReportItem(
                        "GEMEINDENAME",
                        attributes.get(type + "GEMEINDENAME"),
                        StatusCodes.IMP_INVALID_VALUE));
            } else {
                o.setAdminUnitId(ves.get(0).getId());
            }
        } else if (attributes.get(type + "GEMEINDESCHLUESSEL") != null) {
            o.setAdminUnitId(attributes.get(type + "GEMEINDESCHLUESSEL"));
            AdminUnit adminUnit = repository.entityManager().find(
                    AdminUnit.class, o.getAdminUnitId());
            if (adminUnit == null) {
                currentWarnings.add(
                    new ReportItem(
                        type + "GEMEINDESCHLUESSEL", o.getAdminUnitId(),
                        StatusCodes.IMP_INVALID_VALUE));
                o.setAdminUnitId(null);
            }
        }
        String key = "";
        String hLand = "";
        QueryBuilder<State> builderStaat = repository
            .queryBuilder(State.class);
        if (attributes.get(type + "HERKUNFTSLAND_S") != null) {
            key = "HERKUNFTSLAND_S";
            hLand = attributes.get(type + "HERKUNFTSLAND_S");
            builderStaat = builderStaat.and(State_.id, Integer.parseInt(hLand));
        } else if (attributes.get(type + "HERKUNFTSLAND_KURZ") != null) {
            key = "HERKUNFTSLAND_KURZ";
            hLand = attributes.get(type + "HERKUNFTSLAND_KURZ");
            builderStaat = builderStaat.and(State_.intVehRegCode, hLand);
        } else if (attributes.get(type + "HERKUNFTSLAND_LANG") != null) {
            key = "HERKUNFTSLAND_LANG";
            hLand = attributes.get(type + "HERKUNFTSLAND_LANG");
            builderStaat = builderStaat.and(State_.ctry, hLand);
        }

        if (key.length() > 0) {
            List<State> staat =
                repository.filter(builderStaat.getQuery());
            if (staat == null || staat.size() == 0) {
                currentWarnings.add(
                    new ReportItem(key, hLand, StatusCodes.IMP_INVALID_VALUE));
            } else if (staat.size() > 0) {
                o.setStateId(staat.get(0).getId());
            }
        }
        if (attributes.containsKey(type + "HOEHE_NN")) {
            o.setHeightAsl(Float.valueOf(attributes.get(type + "HOEHE_NN")));
        }

        // Check if all attributes are empty
        if (o.getSpatRefSysId() == null
            && o.getAdminUnitId() == null
            && o.getStateId() == null) {
            return null;
        }

        MeasFacil mst = repository.getById(
            MeasFacil.class, probe.getMeasFacilId());
        o.setNetworkId(mst.getNetworkId());
        validate(o, "validation", true, false);
        if (o.hasErrors()) {
            return null;
        }
        Site existing = ortFactory.findExistingSite(o);
        if (existing != null) {
            return existing;
        }
        ortFactory.completeSite(o);
        repository.create(o);
        return o;
    }

    private Timestamp getDate(String date) {
        ZoneId fromLaf = ZoneId.of("UTC");
        switch (currentZeitbasis) {
            case 1: fromLaf = ZoneId.of("UTC+2");
                break;
            case 3: fromLaf = ZoneId.of("UTC+1");
                break;
            case 4: fromLaf = ZoneId.of("CET");
                break;
            default: break;
        }
        DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyyMMdd HHmm").withZone(fromLaf);
        ZonedDateTime orig = ZonedDateTime.parse(date, formatter);
        ZonedDateTime utc = orig.withZoneSameInstant(ZoneOffset.UTC);
        return Timestamp.from(utc.toInstant());
    }

    //Assign global Tag based on LAF field SZENARIO
    private void assignGlobalTag(String szenario, Object object) {
        QueryBuilder<Tag> builderTag = repository.queryBuilder(Tag.class)
            .and(Tag_.name, szenario);
        List<Tag> globalTag = repository.filter(builderTag.getQuery());

        if (globalTag.isEmpty()) {
            ReportItem note = new ReportItem();
            note.setCode(StatusCodes.VALUE_NOT_MATCHING);
            note.setKey("globalTag");
            note.setValue(szenario);
            currentWarnings.add(note);
        } else {
            if (object instanceof Sample) {
                TagLinkSample tagZuord = new TagLinkSample();
                Sample probe = (Sample) object;
                QueryBuilder<TagLinkSample> builderZuord = repository
                    .queryBuilder(TagLinkSample.class)
                    .and(TagLinkSample_.sampleId, probe.getId());
                List<TagLinkSample> globalTagZuord =
                    repository.filter(builderZuord.getQuery());
                if (globalTagZuord.stream().anyMatch(
                        z -> z.getTagId().equals(globalTag.get(0).getId()))) {
                    ReportItem note = new ReportItem();
                    note.setCode(StatusCodes.VAL_EXISTS);
                    note.setKey("globalTag#probe");
                    note.setValue(szenario);
                    currentNotifications.add(note);
                } else {
                    tagZuord.setTagId(globalTag.get(0).getId());
                    tagZuord.setSampleId(probe.getId());
                    repository.create(tagZuord);
                }
            } else if (object instanceof Measm) {
                TagLinkMeasm tagZuord = new TagLinkMeasm();
                Measm messung = (Measm) object;
                QueryBuilder<TagLinkMeasm> builderZuord = repository
                    .queryBuilder(TagLinkMeasm.class)
                    .and(TagLinkMeasm_.measmId, messung.getId());
                List<TagLinkMeasm> globalTagZuord =
                    repository.filter(builderZuord.getQuery());

                if (globalTagZuord.stream().anyMatch(
                        z -> z.getTagId().equals(globalTag.get(0).getId()))) {
                    ReportItem note = new ReportItem();
                    note.setCode(StatusCodes.VAL_EXISTS);
                    note.setKey("globalTag#messung");
                    note.setValue(szenario);
                    currentNotifications.add(note);
                } else {
                    tagZuord.setTagId(globalTag.get(0).getId());
                    tagZuord.setMeasmId(messung.getId());
                    repository.create(tagZuord);
                }
            }
        }
    }

    private void addProbeAttribute(
        Entry<String, String> attribute,
        Sample probe,
        String netzbetreiberId
    ) {
        String key = attribute.getKey();
        String value = attribute.getValue();

        if ("DATENBASIS_S".equals(key)
            && probe.getRegulationId() == null
        ) {
            Regulation regulation = repository.entityManager().find(
                Regulation.class,
                Integer.valueOf(value.toString())
            );
            if (regulation == null) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            Integer v = Integer.valueOf(value.toString());
            probe.setRegulationId(v);
        } else if ("DATENBASIS_S".equals(key)
            && probe.getRegulationId() != null) {
            currentWarnings.add(
                new ReportItem(
                    key, value.toString(), StatusCodes.IMP_DUPLICATE));
        }


        if ("DATENBASIS".equals(key)
            && probe.getRegulationId() == null
        ) {
            QueryBuilder<Regulation> builder = repository
                .queryBuilder(Regulation.class)
                .and(Regulation_.name, value);
            List<Regulation> datenbasis =
                repository.filter(builder.getQuery());
            if (datenbasis == null || datenbasis.isEmpty()) {
                currentErrors.add(
                    new ReportItem(key, value, StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            Integer v = datenbasis.get(0).getId();
            probe.setRegulationId(v);
        } else if ("DATENBASIS".equals(key)
            && probe.getRegulationId() != null
        ) {
            currentWarnings.add(
                new ReportItem(
                    key, value.toString(), StatusCodes.IMP_DUPLICATE));
        }

        if ("PROBE_ID".equals(key)) {
            probe.setExtId(value);
        }

        if ("HAUPTPROBENNUMMER".equals(key)) {
            probe.setMainSampleId(value.toString());
        }

        if ("MPR_ID".equals(key)) {
            Integer v = Integer.valueOf(value.toString());
            probe.setMpgId(v);
        }

        if ("MESSLABOR".equals(key)) {
            MeasFacil measFacil = repository.entityManager().find(
                MeasFacil.class, value.toString());
            if (measFacil == null) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setApprLabId(value.toString());
        }

        if ("MESSPROGRAMM_S".equals(key)
            && probe.getOprModeId() == null
        ) {
            QueryBuilder<MpgTransf> builder = repository
                .queryBuilder(MpgTransf.class)
                .and(MpgTransf_.extId, value);
            List<MpgTransf> transfer = repository.filter(
                    builder.getQuery());
            if (transfer == null || transfer.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setOprModeId(transfer.get(0).getOprModeId());
            if (probe.getRegulationId() == null) {
                probe.setRegulationId(transfer.get(0).getRegulationId());
            }
        }
        if ("MESSPROGRAMM_C".equals(key)) {
            QueryBuilder<MpgTransf> builder = repository
                .queryBuilder(MpgTransf.class)
                .and(MpgTransf_.name, value);
            List<MpgTransf> transfer =
                repository.filter(builder.getQuery());
            if (transfer == null || transfer.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setOprModeId(transfer.get(0).getOprModeId());
            if (probe.getRegulationId() == null) {
                probe.setRegulationId(transfer.get(0).getRegulationId());
            }
        }

        if ("ERZEUGER".equals(key)) {
            QueryBuilder<DatasetCreator> builder = repository
                .queryBuilder(DatasetCreator.class)
                .and(DatasetCreator_.networkId, netzbetreiberId)
                .and(DatasetCreator_.measFacilId, probe.getMeasFacilId())
                .and(DatasetCreator_.extId, value);
            List<DatasetCreator> datensatzErzeuger =
                repository.filter(builder.getQuery());
            if (datensatzErzeuger == null || datensatzErzeuger.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setDatasetCreatorId(datensatzErzeuger.get(0).getId());
        }

        if ("MESSPROGRAMM_LAND".equals(key)) {
            QueryBuilder<MpgCateg> builder = repository
                .queryBuilder(MpgCateg.class)
                .and(MpgCateg_.networkId, netzbetreiberId)
                .and(MpgCateg_.extId, value);
            List<MpgCateg> kategorie =
                repository.filter(builder.getQuery());
            if (kategorie == null || kategorie.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setMpgCategId(kategorie.get(0).getId());
        }

        if ("PROBENAHMEINSTITUTION".equals(key)) {
            QueryBuilder<Sampler> builder = repository
                .queryBuilder(Sampler.class)
                .and(Sampler_.networkId, netzbetreiberId)
                .and(Sampler_.extId, value);
            List<Sampler> prn = repository.filter(builder.getQuery());
            if (prn == null || prn.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setSamplerId(prn.get(0).getId());
        }

        if ("SOLL_DATUM_UHRZEIT_A".equals(key)) {
            probe.setSchedStartDate(getDate(value.toString()));
        }
        if ("SOLL_DATUM_UHRZEIT_E".equals(key)) {
            probe.setSchedEndDate(getDate(value.toString()));
        }
        if ("PROBENAHME_DATUM_UHRZEIT_A".equals(key)) {
            probe.setSampleStartDate(getDate(value.toString()));
        }
        if ("PROBENAHME_DATUM_UHRZEIT_E".equals(key)) {
            probe.setSampleEndDate(getDate(value.toString()));
        }
        if ("URSPRUNGS_DATUM_UHRZEIT".equals(key)) {
            probe.setOrigDate(getDate(value.toString()));
        }

        if ("UMWELTBEREICH_S".equals(key)
            && probe.getEnvMediumId() == null
        ) {
            EnvMedium envMedium = repository.entityManager().find(
                EnvMedium.class, value.toString());
            if (envMedium == null){
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setEnvMediumId(value.toString());
        } else if ("UMWELTBEREICH_S".equals(key)
            && probe.getEnvMediumId() != null
        ) {
            currentWarnings.add(
                new ReportItem(
                    key, value.toString(), StatusCodes.IMP_DUPLICATE));
        }
        if ("UMWELTBEREICH_C".equals(key)
            && probe.getEnvMediumId() == null
            && value != null
        ) {
            int length = value.toString().length() > 80
                ? 80
                : value.toString().length();
            QueryBuilder<EnvMedium> builder = repository
                .queryBuilder(EnvMedium.class)
                .and(EnvMedium_.name, value.toString().substring(0, length));
            List<EnvMedium> umwelt = repository.filter(builder.getQuery());
            if (umwelt == null || umwelt.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setEnvMediumId(umwelt.get(0).getId());
        } else if ("UMWELTBEREICH_C".equals(key)
            && probe.getEnvMediumId() != null
        ) {
            currentWarnings.add(
                new ReportItem(
                    key, value.toString(), StatusCodes.IMP_DUPLICATE));
        }

        if ("DESKRIPTOREN".equals(key)) {
            // ignore deskriptor S12 at the laf import
            if (value.length() > 24) {
                value = value.substring(0, 24);
            }
            if (value.length() < 26) {
                for (int i = value.length(); i <= 26; i++) {
                    value += " ";
                }
            }
            value = value.replace(" ", "0");
            value = value.replace("-", "0");
            List<String> tmp = new ArrayList<String>();
            tmp.add("D:");
            for (int i =  0; i < value.length() - 4; i += 2) {
                tmp.add(value.substring(i, i + 2));
            }
            probe.setEnvDescripDisplay(String.join(" ", tmp));
        }

        if ("TESTDATEN".equals(key)) {
            if (value.toString().equals("1")) {
                probe.setIsTest(true);
            } else if (value.toString().equals("0")) {
                probe.setIsTest(false);
            }
        }

        if ("REI_PROGRAMMPUNKTGRUPPE".equals(key)
            || "REI_PROGRAMMPUNKT".equals(key)) {
            QueryBuilder<ReiAgGr> builder = repository
                .queryBuilder(ReiAgGr.class)
                .and(ReiAgGr_.name, value.toString());
            List<ReiAgGr> list =
                repository.filter(builder.getQuery());
            if (!list.isEmpty()) {
                probe.setReiAgGrId(list.get(0).getId());
            } else {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.VALUE_NOT_MATCHING));
            }
        }

        if ("MEDIUM".equals(key)) {
            probe.setEnvDescripName(value.toString());
        }

        if ("PROBENART".equals(key) && value != null) {
            QueryBuilder<SampleMeth> builder = repository
                .queryBuilder(SampleMeth.class)
                .and(SampleMeth_.extId, value);
            List<SampleMeth> probenart =
                repository.filter(builder.getQuery());
            if (probenart == null || probenart.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setSampleMethId(probenart.get(0).getId());
        }
    }

    /**
     * Add an attribute to the given LMessung object.
     *
     * @param attribute The attributes to map
     * @param messung   The entity object.
     * @return The updated entity object.
     */
    private Measm addMessungAttribute(
        Entry<String, String> attribute,
        Measm messung
    ) {
        String key = attribute.getKey();
        String value = attribute.getValue();

        if ("MESSUNGS_ID".equals(key)) {
            messung.setExtId(Integer.valueOf(value));
        }
        if ("NEBENPROBENNUMMER".equals(key)) {
            messung.setMinSampleId(value.toString());
        } else if ("MESS_DATUM_UHRZEIT".equals(key)) {
            messung.setMeasmStartDate(getDate(value.toString()));
        } else if ("MESSZEIT_SEKUNDEN".equals(key)) {
            Integer i = Integer.valueOf(value.toString());
            messung.setMeasPd(i);
        } else if ("MESSMETHODE_S".equals(key)) {
            Mmt mmt = repository.entityManager().find(
                Mmt.class, value.toString());
            if (mmt == null) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
            } else {
                messung.setMmtId(value.toString());
            }
        } else if ("MESSMETHODE_C".equals(key)) {
            QueryBuilder<Mmt> builder = repository.queryBuilder(Mmt.class)
                .and(Mmt_.name, value.toString());
            List<Mmt> mm = repository.filter(builder.getQuery());
            if (mm == null || mm.isEmpty()) {
                ReportItem warn = new ReportItem();
                warn.setCode(StatusCodes.IMP_MISSING_VALUE);
                warn.setKey("messmethode");
                warn.setValue(key);
                currentWarnings.add(warn);
            } else {
                messung.setMmtId(mm.get(0).getId());
            }
        } else if ("ERFASSUNG_ABGESCHLOSSEN".equals(key)) {
            if (value.toString().equals("1")) {
                messung.setIsCompleted(true);
            } else if (value.toString().equals("0")) {
                messung.setIsCompleted(false);
            }
        }
        return messung;
    }

    private void validate(BaseModel object, String key) {
        validate(object, key, false, false);
    }

    private void validate(
        BaseModel object,
        String key,
        boolean errorsToWarnings,
        boolean validated
    ) {
        if (!validated) {
            validator.validate(object);
        }
        object.getErrors().forEach((k, v) -> {
                v.forEach((value) -> {
                        ReportItem err = new ReportItem(key, k, value);
                        if (errorsToWarnings) {
                            currentWarnings.add(err);
                        } else {
                            currentErrors.add(err);
                        }
                    });
            });
        object.getWarnings().forEach((k, v) -> {
                v.forEach((value) -> {
                        currentWarnings.add(
                            new ReportItem(key, k, value));
                    });
            });
        object.getNotifications().forEach((k, v) -> {
                v.forEach((value) -> {
                        currentNotifications.add(
                            new ReportItem(key, k, value));
                    });
            });
    }

    /**
     * @return the errors
     */
    public Map<String, List<ReportItem>> getErrors() {
        return errors;
    }

    /**
     * @return the errors
     */
    public Map<String, List<ReportItem>> getWarnings() {
        return warnings;
    }

    /**
     * @return the notifications
     */
    public Map<String, List<ReportItem>> getNotifications() {
        return notifications;
    }

    /**
     * @return Imported probe ids
     */
    public List<Integer> getImportedProbeIds() {
        return importProbeIds;
    }

    /**
     * @param userInfo the userInfo to set
     */
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
        this.authorizer = new Authorization(
            userInfo, this.i18n, this.repository);
    }

    /**
     * @param config the config to set
     */
    public void setConfig(List<ImportConf> config) {
        this.config = config;
        this.configMapper = new ImportConfigMapper(config);
    }

    /**
     * @param measFacilId ID of the default measurement facility
     */
    public void setMeasFacilId(String measFacilId) {
        this.measFacilId = measFacilId;
    }
}
