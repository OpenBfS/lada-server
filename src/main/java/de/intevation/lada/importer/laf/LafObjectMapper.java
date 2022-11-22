/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.laf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.management.modelmbean.InvalidTargetObjectTypeException;

import org.jboss.logging.Logger;

import de.intevation.lada.factory.OrtFactory;
import de.intevation.lada.factory.ProbeFactory;
import de.intevation.lada.importer.Identified;
import de.intevation.lada.importer.Identifier;
import de.intevation.lada.importer.IdentifierConfig;
import de.intevation.lada.importer.ObjectMerger;
import de.intevation.lada.importer.ReportItem;
import de.intevation.lada.model.land.CommMeasm;
import de.intevation.lada.model.land.CommSample;
import de.intevation.lada.model.land.Measm;
import de.intevation.lada.model.land.MeasVal;
import de.intevation.lada.model.land.Geolocat;
import de.intevation.lada.model.land.Sample;
import de.intevation.lada.model.land.StatusProt;
import de.intevation.lada.model.land.ZusatzWert;
import de.intevation.lada.model.master.AdminUnit;
import de.intevation.lada.model.master.DatasetCreator;
import de.intevation.lada.model.master.EnvMedium;
import de.intevation.lada.model.master.ImportConf;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.MeasUnit;
import de.intevation.lada.model.master.Measd;
import de.intevation.lada.model.master.Mmt;
import de.intevation.lada.model.master.MpgCateg;
import de.intevation.lada.model.master.MpgTransf;
import de.intevation.lada.model.master.NuclFacilGr;
import de.intevation.lada.model.master.Poi;
import de.intevation.lada.model.master.Regulation;
import de.intevation.lada.model.master.ReiAgGr;
import de.intevation.lada.model.master.SampleMeth;
import de.intevation.lada.model.master.SampleSpecif;
import de.intevation.lada.model.master.Sampler;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.SpatRefSys;
import de.intevation.lada.model.master.State;
import de.intevation.lada.model.master.StatusAccessMpView;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.model.master.Tz;
import de.intevation.lada.model.land.TagLink;
import de.intevation.lada.util.auth.HeaderAuthorization;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.MesswertNormalizer;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationConfig;

/**
 * Create database objects and map the attributes from laf raw data.
 */
public class LafObjectMapper {

    @Inject
    private Logger logger;

    private HeaderAuthorization authorizer;

    @Inject
    @ValidationConfig(type = "Sample")
    private Validator probeValidator;

    @Inject
    @ValidationConfig(type = "Messung")
    private Validator messungValidator;

    @Inject
    @ValidationConfig(type = "Ort")
    private Validator ortValidator;

    @Inject
    @IdentifierConfig(type = "Sample")
    private Identifier probeIdentifier;

    @Inject
    @IdentifierConfig(type = "Messung")
    private Identifier messungIdentifier;

    @Inject
    @ValidationConfig(type = "Messwert")
    private Validator messwertValidator;

    @Inject
    @ValidationConfig(type = "Status")
    private Validator statusValidator;

    @Inject
    private ObjectMerger merger;

    @Inject
    private Repository repository;

    @Inject
    private ProbeFactory factory;

    @Inject OrtFactory ortFactory;

    @Inject
    private MesswertNormalizer messwertNormalizer;

    private Map<String, List<ReportItem>> errors;
    private Map<String, List<ReportItem>> warnings;
    private Map<String, List<ReportItem>> notifications;
    private List<ReportItem> currentErrors;
    private List<ReportItem> currentWarnings;
    private List<ReportItem> currentNotifications;
    private List<Integer> importProbeIds;

    private int currentZeitbasis;

    private UserInfo userInfo;

    private List<ImportConf> config;

    /**
     * Map the raw data to database objects.
     * @param data the raw data from laf parser
     */
    public void mapObjects(LafRawData data) {
        errors = new HashMap<>();
        warnings = new HashMap<>();
        notifications = new HashMap<>();
        importProbeIds = new ArrayList<Integer>();
        for (int i = 0; i < data.getProben().size(); i++) {
            create(data.getProben().get(i));
        }
    }

    private void create(LafRawData.Sample object) {
        currentWarnings = new ArrayList<>();
        currentErrors = new ArrayList<>();
        currentNotifications = new ArrayList<>();
        Sample probe = new Sample();
        String netzbetreiberId = null;

        Iterator<ImportConf> importerConfig = config.iterator();
        while (importerConfig.hasNext()) {
            ImportConf current = importerConfig.next();
            if ("ZEITBASIS".equals(current.getName().toUpperCase())) {
                currentZeitbasis = Integer.valueOf(current.getToVal());
            }
            if ("PROBE".equals(current.getName().toUpperCase())
                && "MSTID".equals(current.getAttribute().toUpperCase())
                && "DEFAULT".equals(current.getAction().toUpperCase())) {
                probe.setMeasFacilId(current.getToVal());
            }
        }
        if (object.getAttributes().containsKey("MESSSTELLE")) {
            probe.setMeasFacilId(object.getAttributes().get("MESSSTELLE"));
        }
        if (probe.getMeasFacilId() == null) {
            currentErrors.add(
                new ReportItem(
                    "MESSSTELLE", "", StatusCodes.IMP_MISSING_VALUE));
            errors.put(object.getIdentifier(),
                new ArrayList<ReportItem>(currentErrors));
            return;
        } else {
            MeasFacil mst = repository.getByIdPlain(
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
            List<ImportConf> cfg =
            getImporterConfigByAttributeUpper("ZEITBASIS");
            String attribute = object.getAttributes().get("ZEITBASIS");
            if (!cfg.isEmpty() && attribute.equals(cfg.get(0).getFromVal())) {
                attribute = cfg.get(0).getToVal();
            }
            QueryBuilder<Tz> builder =
                repository.queryBuilder(Tz.class);
            builder.and("name", attribute);
            List<Tz> zb = repository.filterPlain(builder.getQuery());
            if (zb == null || zb.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        "ZEITBASIS",
                        object.getAttributes().get(
                            "ZEITBASIS"), StatusCodes.IMP_INVALID_VALUE));
            } else {
                currentZeitbasis = zb.get(0).getId();
            }
        } else if (object.getAttributes().containsKey("ZEITBASIS_S")) {
            currentZeitbasis =
                Integer.valueOf(object.getAttributes().get("ZEITBASIS_S"));
            Tz zeitbasis = repository.getByIdPlain(
                Tz.class,
                currentZeitbasis
            );
            if (zeitbasis == null) {
                currentWarnings.add(
                    new ReportItem(
                        "ZEITBASIS_S",
                        object.getAttributes().get(
                            "ZEITBASIS_S"), StatusCodes.IMP_INVALID_VALUE));
            }
        }

        // Fill the object with data
        for (Entry<String, String> attribute
            : object.getAttributes().entrySet()
        ) {
            addProbeAttribute(attribute, probe, netzbetreiberId);
        }
        doDefaults(probe);
        doConverts(probe);
        doTransforms(probe);
        if (probe.getApprLabId() == null) {
            probe.setApprLabId(probe.getMeasFacilId());
        }
        // Use the deskriptor string to find the medium
        probe = factory.findMedia(probe);
        if (probe.getEnvMediumId() == null) {
            factory.findUmweltId(probe);
        }

        // Check if the user is authorized to create the probe
        if (
            !authorizer.isAuthorized(probe, RequestMethod.POST, Sample.class)
        ) {
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
        // logProbe(probe);

        // Check for errors and warnings

        // Compare the probe with objects in the db
        Sample newProbe = null;
        boolean oldProbeIsReadonly = false;
        try {
            Identified i = probeIdentifier.find(probe);
            Sample old = (Sample) probeIdentifier.getExisting();
            // Matching probe was found in the db. Update it!
            if (i == Identified.UPDATE) {
                oldProbeIsReadonly = authorizer.isProbeReadOnly(old.getId());
                if (
                    // TODO: Should use RequestMethod.PUT?
                    authorizer.isAuthorized(old, RequestMethod.GET, Sample.class)
                ) {
                    if (oldProbeIsReadonly) {
                        newProbe = old;
                        currentNotifications.add(
                            new ReportItem(
                                "probe",
                                old.getExtId(),
                                StatusCodes.IMP_UNCHANGABLE));
                    } else {
                        if (merger.merge(old, probe)) {
                            newProbe = old;
                        } else {
                            ReportItem err = new ReportItem();
                            err.setCode(StatusCodes.ERROR_MERGING);
                            err.setKey("Database error");
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
                                new ArrayList<ReportItem>(
                                    currentNotifications));
                            }
                            return;
                        }
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
            } else if (i == Identified.REJECT) {
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
            } else if (i == Identified.NEW) {
                // It is a brand new probe!
                Violation violation = probeValidator.validate(probe);
                if (!violation.hasErrors()) {
                    Response created = repository.create(probe);
                    newProbe = ((Sample) created.getData());
                } else {
                    for (Entry<String, List<Integer>> err
                        : violation.getErrors().entrySet()
                    ) {
                        for (Integer code : err.getValue()) {
                            currentErrors.add(
                                new ReportItem(
                                    "validation#probe", err.getKey(), code));
                        }
                    }
                    for (Entry<String, List<Integer>> warn
                        :violation.getWarnings().entrySet()
                    ) {
                        for (Integer code : warn.getValue()) {
                            currentWarnings.add(
                                new ReportItem(
                                    "validation#probe", warn.getKey(), code));
                        }
                    }
                    for (Entry<String, List<Integer>> notes
                        : violation.getNotifications().entrySet()
                    ) {
                        for (Integer code :notes.getValue()) {
                            currentNotifications.add(
                                new ReportItem(
                                    "validation#probe", notes.getKey(), code));
                        }
                    }
                }
            }
            if (newProbe != null) {
                importProbeIds.add(newProbe.getId());
            }  else if (probe != null) {
                importProbeIds.add(probe.getId());
            }
        } catch (InvalidTargetObjectTypeException e) {
            ReportItem err = new ReportItem();
            err.setCode(StatusCodes.ERROR_VALIDATION);
            err.setKey("not known");
            err.setValue("No valid Sample Object");
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
            if (!oldProbeIsReadonly) {
                // Create kommentar objects
                List<CommSample> kommentare = new ArrayList<>();
                for (int i = 0; i < object.getKommentare().size(); i++) {
                    CommSample tmp =
                        createProbeKommentar(
                            object.getKommentare().get(i), newProbe);
                    if (tmp != null) {
                        kommentare.add(tmp);
                    }
                }
                // Persist kommentar objects
                merger.mergeKommentare(newProbe, kommentare);

                // Create zusatzwert objects
                List<ZusatzWert> zusatzwerte = new ArrayList<>();
                for (int i = 0; i < object.getZusatzwerte().size(); i++) {
                    ZusatzWert tmp =
                        createZusatzwert(
                            object.getZusatzwerte().get(i), newProbe.getId());
                    if (tmp != null) {
                        zusatzwerte.add(tmp);
                    }
                }
                // Persist zusatzwert objects
                merger.mergeZusatzwerte(newProbe, zusatzwerte);

                // Special things for REI-Messpunkt
                if (probe.getReiAgGrId() != null
                    || Integer.valueOf(3).equals(probe.getRegulationId())
                    || Integer.valueOf(4).equals(probe.getRegulationId())
                ) {
                    createReiMesspunkt(object, newProbe);
                } else {
                    // Check if we have EOrte present
                    QueryBuilder<Geolocat> builderPresentEOrte =
                        repository.queryBuilder(Geolocat.class);
                        builderPresentEOrte.and("sampleId", newProbe.getId());
                        builderPresentEOrte.and("typeRegulation", "E");
                    List<Geolocat> presentEOrte = repository.filterPlain(builderPresentEOrte.getQuery());

                    // Check if we have UOrte present
                    QueryBuilder<Geolocat> builderPresentUOrte =
                        repository.queryBuilder(Geolocat.class);
                        builderPresentUOrte.and("sampleId", newProbe.getId());
                        builderPresentUOrte.and("typeRegulation", "U");
                    List<Geolocat> presentUOrte = repository.filterPlain(builderPresentUOrte.getQuery());

                    // Check if we have ROrte present
                    QueryBuilder<Geolocat> builderPresentROrte =
                        repository.queryBuilder(Geolocat.class);
                        builderPresentROrte.and("sampleId", newProbe.getId());
                        builderPresentROrte.and("typeRegulation", "R");
                    List<Geolocat> presentROrte = repository.filterPlain(builderPresentROrte.getQuery());

                    //Switch if we need to create an R-Ort
                    Boolean rOrt = false;
                    // First create or find entnahmeOrte and ursprungsOrte
                    // Create the new entnahmeOrt but do not persist
                    Geolocat eOrt = createOrtszuordnung(object.getEntnahmeOrt(), "E" , newProbe);

                    //Create/Find Ursprungsort(e) from LAF
                    List<Geolocat> uOrte = new ArrayList<>();
                    //If object.getUrsprungsOrte().size() > 1
                    for (int i = 0; i < object.getUrsprungsOrte().size(); i++) {
                        Geolocat tmp =
                            createOrtszuordnung(
                                object.getUrsprungsOrte().get(i), "U", newProbe);
                        if (tmp != null) {
                            uOrte.add(tmp);
                        }
                    }

                    //If the LAF delivers eOrt and uOrt and those are a match by created/found - Id -- we need to create an R-Ort
                    if (uOrte.size() > 0 && eOrt!= null && uOrte.stream().anyMatch(uOrt -> uOrt.getSiteId().equals(eOrt.getSiteId()))){
                        rOrt = true;
                    }

                    //further conditionals for eOrt
                    if (eOrt != null) {
                        //Check if the new Ort matches an U-Ort if exists
                        if (presentUOrte.size() > 0 && eOrt!= null && presentUOrte.stream().anyMatch(uOrt -> uOrt.getSiteId().equals(eOrt.getSiteId()))){
                            rOrt = true;
                        }
                        else if (presentROrte.size() > 0 && eOrt!= null && presentROrte.stream().anyMatch(rtypeOrt -> rtypeOrt.getSiteId().equals(eOrt.getSiteId()))){
                            rOrt = true;
                        } else if (presentROrte.size() > 0 && eOrt!= null){
                            for (int i = 0; i < presentROrte.size(); i++) {
                                presentROrte.get(i).setTypeRegulation("U");
                                repository.update(presentROrte.get(i));
                            }
                            rOrt = false;
                        }
                    }

                    //conditionals for the ursprungsOrte
                    if (uOrte.size()==1) {
                        //Check if the new Ort matches the U-Ort if exists, this only works if we have 1 Ursprungsort in the LAF, if we have more we must assume multiple
                        //ursprungsorte
                            if (presentEOrte.size() > 0 && uOrte.size() > 0 && presentEOrte.stream().anyMatch(etypeOrt -> etypeOrt.getSiteId().equals(uOrte.get(0).getSiteId()))){
                                rOrt = true;
                            }
                            else if (presentROrte.size() > 0 && uOrte.size() > 0 && presentROrte.stream().anyMatch(rtypeOrt -> rtypeOrt.getSiteId().equals(uOrte.get(0).getSiteId()))){
                                //ToDo: We need to handle R-Orte!
                                rOrt = true;
                            } else if (presentROrte.size() > 0 && uOrte.size() > 0){
                                for (int i = 0; i < presentROrte.size(); i++) {
                                    presentROrte.get(i).setTypeRegulation("E");
                                    repository.update(presentROrte.get(i));
                                }
                                rOrt = false;
                            }
                    }

                    if (!rOrt) {
                        //persist general entnahmeOrt
                        if(eOrt != null){
                            merger.mergeEntnahmeOrt(newProbe.getId(), eOrt);
                        }
                        if(uOrte.size()>0){
                            merger.mergeUrsprungsOrte(newProbe.getId(), uOrte);
                        }
                    } else {
                        if (rOrt && presentROrte.size() == 1){
                            //we may have additional information for the ortszuordnung such as an ortszusatz, we make an update.
                            QueryBuilder<Geolocat> builderUOrt =
                            repository.queryBuilder(Geolocat.class);
                            builderUOrt.and("sampleId", newProbe.getId());
                            builderUOrt.and("typeRegulation", "R");
                            Response uOrtQuery =
                                repository.filter(builderUOrt.getQuery());
                            @SuppressWarnings("unchecked")
                            List<Geolocat> uOrteProbe = (List<Geolocat>) uOrtQuery.getData();
                            if (!uOrteProbe.isEmpty()){
                                for (Geolocat elemOrt : uOrteProbe){
                                    repository.delete(elemOrt);
                                }
                            }

                            if ((eOrt != null)) {
                                eOrt.setTypeRegulation(("R"));
                                merger.mergeEntnahmeOrt(newProbe.getId(), eOrt);
                            }
                            if (uOrte.size()==1 && eOrt == null){
                                uOrte.get(0).setTypeRegulation("R");
                                merger.mergeUrsprungsOrte(newProbe.getId(), uOrte);
                            }
                        }
                        // clean up ursprungsorte before!
                        if (object.getUrsprungsOrte().size() > 0 || presentUOrte.size() > 0){
                            QueryBuilder<Geolocat> builderUOrt =
                                repository.queryBuilder(Geolocat.class);
                                builderUOrt.and("sampleId", newProbe.getId());
                                builderUOrt.and("typeRegulation", "U");
                            Response uOrtQuery =
                                repository.filter(builderUOrt.getQuery());
                            @SuppressWarnings("unchecked")
                            List<Geolocat> uOrteProbe = (List<Geolocat>) uOrtQuery.getData();
                            if (!uOrteProbe.isEmpty()){
                                for (Geolocat elemOrt : uOrteProbe){
                                    repository.delete(elemOrt);
                                }
                            }
                        }
                        if (eOrt != null){
                            eOrt.setTypeRegulation("R");
                            //Merging the entnahmeOrt cleans it up!
                            merger.mergeEntnahmeOrt(newProbe.getId(), eOrt);
                        } else {
                            if (uOrte.size()==1){

                                //clean up entnahmeOrte before merge

                                QueryBuilder<Geolocat> builderEOrt =
                                    repository.queryBuilder(Geolocat.class);
                                    builderEOrt.and("sampleId", newProbe.getId());
                                    builderEOrt.and("typeRegulation", "E");
                                Response eOrtQuery =
                                    repository.filter(builderEOrt.getQuery());
                                @SuppressWarnings("unchecked")
                                List<Geolocat> eOrteProbe = (List<Geolocat>) eOrtQuery.getData();
                                if (!eOrteProbe.isEmpty()){
                                    for (Geolocat elemOrt : eOrteProbe){
                                        repository.delete(elemOrt);
                                    }
                                }

                                uOrte.get(0).setTypeRegulation("R");
                                merger.mergeUrsprungsOrte(newProbe.getId(), uOrte);
                            }
                        }
                    }
                }
            }

            // Validate probe object
            Violation violation = probeValidator.validate(newProbe);
            for (Entry<String, List<Integer>> err
                : violation.getErrors().entrySet()
            ) {
                for (Integer code : err.getValue()) {
                    currentErrors.add(
                        new ReportItem("validation#probe", err.getKey(), code));
                }
            }
            for (Entry<String, List<Integer>> warn
                : violation.getWarnings().entrySet()
            ) {
                for (Integer code : warn.getValue()) {
                    currentWarnings.add(
                        new ReportItem("validation#probe", warn.getKey(), code));
                }
            }
            for (Entry<String, List<Integer>> notes
                : violation.getNotifications().entrySet()
            ) {
              for (Integer code: notes.getValue()) {
                currentNotifications.add(
                    new ReportItem("validation#probe", notes.getKey(), code));
              }
            }
            // Create messung objects
            for (int i = 0; i < object.getMessungen().size(); i++) {
                create(
                    object.getMessungen().get(i),
                    newProbe,
                    newProbe.getMeasFacilId());
            }
            //if key SZENARIO is present in imported file, assign global tag to probe and its messung objects
            if (object.getAttributes().containsKey("SZENARIO")) {
                //assign to probe object
                assignGlobalTag(object.getAttributes().get("SZENARIO"), newProbe);
                //assign to messung objects
                QueryBuilder<Measm> builderMessung =
                    repository.queryBuilder(Measm.class);
                builderMessung.and("sampleId", newProbe.getId());
                List<Measm> messungen =  repository.filterPlain(builderMessung.getQuery());
                for (Measm messung: messungen) {
                    assignGlobalTag(object.getAttributes().get("SZENARIO"), messung);
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

    private void doDefaults(Sample probe) {
        doDefaults(probe, Sample.class, "probe");
    }

    private void doConverts(Sample probe) {
        doConverts(probe, Sample.class, "probe");
    }

    private void doTransforms(Sample probe) {
        doTransformations(probe, Sample.class, "probe");
    }

    private void doDefaults(Measm messung) {
        doDefaults(messung, Measm.class, "messung");
    }

    private void doConverts(Measm messung) {
        doConverts(messung, Measm.class, "messung");
    }

    private void doTransforms(Measm messung) {
        doTransformations(messung, Measm.class, "messung");
    }

    private void doDefaults(MeasVal messwert) {
        doDefaults(messwert, MeasVal.class, "messwert");
    }

    private void doConverts(MeasVal messwert) {
        doConverts(messwert, MeasVal.class, "messwert");
    }

    private void doTransforms(MeasVal messwert) {
        doTransformations(messwert, MeasVal.class, "messwert");
    }

    private void doDefaults(ZusatzWert zusatzwert) {
        doDefaults(zusatzwert, ZusatzWert.class, "zusatwert");
    }

    private void doConverts(ZusatzWert zusatzwert) {
        doConverts(zusatzwert, ZusatzWert.class, "zusatzwert");
    }

    private void doTransforms(ZusatzWert zusatzwert) {
        doTransformations(zusatzwert, ZusatzWert.class, "zusatwert");
    }

    private void doDefaults(CommMeasm kommentar) {
        doDefaults(kommentar, CommMeasm.class, "kommentarm");
    }

    private void doConverts(CommMeasm kommentar) {
        doConverts(kommentar, CommMeasm.class, "kommentarm");
    }

    private void doTransforms(CommMeasm kommentar) {
        doTransformations(kommentar, CommMeasm.class, "kommentarm");
    }

    private void doDefaults(CommSample kommentar) {
        doDefaults(kommentar, CommSample.class, "kommentarp");
    }

    private void doConverts(CommSample kommentar) {
        doConverts(kommentar, CommSample.class, "kommentarp");
    }

    private void doTransforms(CommSample kommentar) {
        doTransformations(kommentar, CommSample.class, "kommentarp");
    }

    private void doDefaults(Geolocat ort) {
        doDefaults(ort, Geolocat.class, "ortszuordnung");
    }

    private void doConverts(Geolocat ort) {
        doConverts(ort, Geolocat.class, "ortszuordnung");
    }

    private void doTransforms(Geolocat ort) {
        doTransformations(ort, Geolocat.class, "ortszuordnung");
    }

    private void doDefaults(Site o) {
        doDefaults(o, Site.class, "ort");
    }

    private <T> void doDefaults(Object object, Class<T> clazz, String table) {
        Iterator<ImportConf> i = config.iterator();
        while (i.hasNext()) {
            ImportConf current = i.next();
            if (table.equals(current.getName())
                && "default".equals(current.getAction())
            ) {
                String attribute = current.getAttribute();
                Method getter;
                Method setter = null;
                try {
                    getter = clazz.getMethod("get"
                        + attribute.substring(0, 1).toUpperCase()
                        + attribute.substring(1));
                    String methodName = "set"
                        + attribute.substring(0, 1).toUpperCase()
                        + attribute.substring(1);
                    for (Method method : clazz.getMethods()) {
                        String name = method.getName();
                        if (!methodName.equals(name)) {
                            continue;
                        }
                        setter = method;
                        break;
                    }
                } catch (NoSuchMethodException | SecurityException e) {
                    logger.debug("attribute " + attribute + " does not exist");
                    return;
                }
                try {
                    Object value = getter.invoke(object);
                    if (value == null && setter != null) {
                        Class<?>[] types = setter.getParameterTypes();
                        if (types.length == 1) {
                            // we have exactly one parameter, thats fine.
                            if (types[0].isAssignableFrom(Integer.class)) {
                                // the parameter is of type Integer!
                                // Cast to integer
                                setter.invoke(
                                    object,
                                    Integer.valueOf(current.getToVal()));
                            } else {
                                // we handle the default as string.
                                // Other parameter types are not implemented!
                                setter.invoke(object, current.getToVal());
                            }
                        }
                    }
                } catch (IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException e
                ) {
                    logger.debug("Could not set attribute " + attribute);
                    return;
                }
            }
        }
    }

    private List<ImportConf> getImporterConfigByAttributeUpper(
        String attribute
    ) {
        Iterator<ImportConf> i = config.iterator();
        List<ImportConf> result = new ArrayList<ImportConf>();
        while (i.hasNext()) {
            ImportConf current = i.next();
            if (current.getAttribute().toUpperCase().equals(attribute)) {
                result.add(current);
            }
        }
        return result;
    }

    private <T> void doConverts(Object object, Class<T> clazz, String table) {
        Iterator<ImportConf> i = config.iterator();
        while (i.hasNext()) {
            ImportConf current = i.next();
            if (table.equals(current.getName())
                && "convert".equals(current.getAction())
            ) {
                String attribute = current.getAttribute();
                Method getter;
                Method setter = null;
                try {
                    getter = clazz.getMethod("get"
                        + attribute.substring(0, 1).toUpperCase()
                        + attribute.substring(1));
                    String methodName = "set"
                        + attribute.substring(0, 1).toUpperCase()
                        + attribute.substring(1);
                    for (Method method : clazz.getMethods()) {
                        String name = method.getName();
                        if (!methodName.equals(name)) {
                            continue;
                        }
                        setter = method;
                        break;
                    }
                } catch (NoSuchMethodException | SecurityException e) {
                    logger.warn("attribute " + attribute + " does not exist");
                    return;
                }
                try {
                    Object value = getter.invoke(object);
                    if (value.equals(current.getFromVal())
                        && setter != null
                    ) {
                        setter.invoke(object, current.getToVal());
                    }
                } catch (IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException e
                ) {
                    logger.warn("Could not convert attribute " + attribute);
                    return;
                }
            }
        }
    }

    private <T> void doTransformations(
        Object object,
        Class<T> clazz,
        String table
    ) {
        Iterator<ImportConf> i = config.iterator();
        while (i.hasNext()) {
            ImportConf current = i.next();
            if (table.equals(current.getName())
                && "transform".equals(current.getAction())
            ) {
                String attribute = current.getAttribute();
                Method getter;
                Method setter = null;
                try {
                    getter = clazz.getMethod("get"
                        + attribute.substring(0, 1).toUpperCase()
                        + attribute.substring(1));
                    String methodName = "set"
                        + attribute.substring(0, 1).toUpperCase()
                        + attribute.substring(1);
                    for (Method method : clazz.getMethods()) {
                        String name = method.getName();
                        if (methodName.equals(name)) {
                            setter = method;
                            break;
                        }
                    }
                    if (setter == null) {
                        logger.warn(
                            "Could not transform attribute " + attribute);
                        return;
                    }
                } catch (NoSuchMethodException | SecurityException e) {
                    logger.warn("attribute " + attribute + " does not exist");
                    return;
                }
                try {
                    Object value = getter.invoke(object);
                    if (value == null) {
                        logger.warn("Attribute " + attribute + " is not set");
                        return;
                    }
                    char from = (char) Integer.parseInt(
                        current.getFromVal(), 16);
                    char to = (char) Integer.parseInt(
                        current.getToVal(), 16);
                    value = value.toString().replaceAll(
                        "[" + String.valueOf(from) + "]", String.valueOf(to));
                    setter.invoke(object, value);
                } catch (IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException e
                ) {
                    logger.warn("Could not transform attribute " + attribute);
                    return;
                }
            }
        }
    }

    private void create(
        LafRawData.Messung object,
        Sample probe, String mstId
    ) {
        Measm messung = new Measm();
        messung.setSampleId(probe.getId());

        // Fill the new messung with data
        for (Entry<String, String> attribute
            : object.getAttributes().entrySet()
        ) {
            addMessungAttribute(attribute, messung);
        }
        doDefaults(messung);
        doConverts(messung);
        doTransforms(messung);
        // Check if the user is authorized to create the object
        if (
            !authorizer.isAuthorized(messung, RequestMethod.POST, Measm.class)
        ) {
            ReportItem warn = new ReportItem();
            warn.setCode(StatusCodes.NOT_ALLOWED);
            warn.setKey(userInfo.getName());
            warn.setValue("Messung: " + messung.getMinSampleId());
            currentErrors.add(warn);
            return;
        }

        // Compare with messung objects in the db
        Identified ident;
        try {
            ident = messungIdentifier.find(messung);
        } catch (InvalidTargetObjectTypeException e) {
            ReportItem err = new ReportItem();
            err.setCode(StatusCodes.ERROR_VALIDATION);
            err.setKey("not valid");
            err.setValue("Messung: " + messung.getMinSampleId());
            currentErrors.add(err);
            return;
        }
        Measm newMessung;
        boolean oldMessungIsReadonly = false;
        Measm old = (Measm) messungIdentifier.getExisting();
        switch (ident) {
        case UPDATE:
            oldMessungIsReadonly =
                authorizer.isMessungReadOnly(old.getId());
            if (oldMessungIsReadonly) {
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
            break;
        case REJECT:
            ReportItem err = new ReportItem();
            err.setCode(StatusCodes.VALUE_MISSING);
            err.setKey("identification");
            err.setValue("Messung");
            currentErrors.add(err);
            return;
        case NEW:
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
            newMessung = (Measm) repository.create(messung).getData();
            break;
        default:
            throw new IllegalArgumentException(
                "Identified with unexpected enum constant");
        }

        List<CommMeasm> kommentare = new ArrayList<CommMeasm>();
        for (int i = 0; i < object.getKommentare().size(); i++) {
            CommMeasm tmp =
                createMessungKommentar(
                    object.getKommentare().get(i), newMessung.getId(), probe);
            if (tmp != null) {
                kommentare.add(tmp);
            }
        }
        merger.mergeMessungKommentare(newMessung, kommentare);
        List<MeasVal> messwerte = new ArrayList<MeasVal>();
        List<Integer> messgroessenListe = new ArrayList<Integer>();
        for (int i = 0; i < object.getMesswerte().size(); i++) {
            MeasVal tmp =
                createMesswert(
                    object.getMesswerte().get(i), newMessung.getId());
            if (tmp != null) {
                //find duplicates
                if (messgroessenListe.contains(tmp.getMeasdId())) {
                    currentWarnings.add(new ReportItem(
                        (object.getMesswerte().get(i).get("MESSGROESSE_ID")
                            == null)
                        ? "MESSWERT - MESSGROESSE"
                        : "MESSWERT - MESSGROESSE_ID",
                        (object.getMesswerte().get(i).get("MESSGROESSE_ID")
                            == null)
                        ? object.getMesswerte().get(i).get(
                            "MESSGROESSE").toString()
                        : object.getMesswerte().get(i).get(
                            "MESSGROESSE_ID").toString(),
                            StatusCodes.IMP_DUPLICATE));
                } else {
                   //temporary messwertobjects
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
        Violation violation = messungValidator.validate(newMessung);
        for (Entry<String, List<Integer>> err
            : violation.getErrors().entrySet()
        ) {
            for (Integer code : err.getValue()) {
                currentErrors.add(
                    new ReportItem("validation#messung", err.getKey(), code));
            }
        }
        for (Entry<String, List<Integer>> warn
            : violation.getWarnings().entrySet()
        ) {
            for (Integer code : warn.getValue()) {
                currentWarnings.add(
                    new ReportItem("validation#messung", warn.getKey(), code));
            }
        }
        for (Entry<String, List<Integer>> notes
            : violation.getNotifications().entrySet()
        ) {
            for (Integer code : notes.getValue()) {
                currentNotifications.add(
                    new ReportItem("validation#messung", notes.getKey(), code));
            }
        }
        // ... and messwerte
        QueryBuilder<MeasVal> messwBuilder =
            repository.queryBuilder(MeasVal.class);
        messwBuilder.and("measmId", newMessung.getId());
        for (MeasVal messwert: messwerte) {
            Violation messwViolation = messwertValidator.validate(messwert);
            if (messwViolation.hasWarnings()) {
                messwViolation.getWarnings().forEach((k, v) -> {
                    v.forEach((value) -> {
                        currentWarnings.add(
                            new ReportItem("validation#messwert", k, value));
                    });
                });
            }

            if (messwViolation.hasErrors()) {
                messwViolation.getErrors().forEach((k, v) -> {
                    v.forEach((value) -> {
                        currentErrors.add(
                            new ReportItem("validation#messwert", k, value));
                    });
                });
            }

            if (messwViolation.hasNotifications()) {
                messwViolation.getNotifications().forEach((k, v) -> {
                    v.forEach((value) -> {
                        currentNotifications.add(
                            new ReportItem("validation#messwert", k, value));
                    });
                });
            }
        }

        // Validate / Create Status
        if (!object.hasErrors()) {
            if (object.getAttributes().containsKey("BEARBEITUNGSSTATUS")) {
                createStatusProtokoll(
                    object.getAttributes().get(
                        "BEARBEITUNGSSTATUS"), newMessung, mstId);
            }
        }
    }

    private CommSample createProbeKommentar(
        Map<String, String> attributes,
        Sample probe
    ) {
        if (attributes.get("TEXT").equals("")) {
            currentWarnings.add(
                new ReportItem(
                    "PROBENKOMMENTAR", "Text", StatusCodes.VALUE_MISSING));
            return null;
        }

        // TODO: Why does the following duplicate a validation rule?
        QueryBuilder<CommSample> kommentarBuilder = repository
            .queryBuilder(CommSample.class)
            .and("sampleId", probe.getId());
        List<CommSample> kommentarExist = repository.filterPlain(
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
            return null;
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
        doDefaults(kommentar);
        doConverts(kommentar);
        doTransforms(kommentar);
        if (!userInfo.getMessstellen().contains(kommentar.getMeasFacilId())) {
            currentWarnings.add(
                new ReportItem(
                    userInfo.getName(),
                    "Kommentar: " + kommentar.getMeasFacilId(),
                    StatusCodes.NOT_ALLOWED));
            return null;
        }
        return kommentar;
    }

    private ZusatzWert createZusatzwert(
        Map<String, String> attributes,
        int probeId
    ) {
        ZusatzWert zusatzwert = new ZusatzWert();
        zusatzwert.setProbeId(probeId);
        if (attributes.containsKey("MESSFEHLER")) {
            zusatzwert.setMessfehler(
                Float.valueOf(
                    attributes.get("MESSFEHLER").replaceAll(",", ".")));
        }
        String wert = attributes.get("MESSWERT_PZS");
        if (wert.startsWith("<")) {
            wert = wert.substring(1);
            zusatzwert.setKleinerAls("<");
        }
        zusatzwert.setMesswertPzs(Double.valueOf(wert.replaceAll(",", ".")));
        List<ImportConf> cfgs =
            getImporterConfigByAttributeUpper("ZUSATZWERT");
        String attribute = attributes.get("PZS");
        boolean isId = false;
        if (attribute == null) {
            attribute = attributes.get("PZS_ID");
            isId = true;
        }
        for (int i = 0; i < cfgs.size(); i++) {
            ImportConf cfg = cfgs.get(i);
            if (cfg.getAction().equals("convert")
                && cfg.getFromVal().equals(attribute)
            ) {
                attribute = cfg.getToVal();
            }
            if (cfg.getAction().equals("transform")) {
                char from = (char) Integer.parseInt(cfg.getFromVal(), 16);
                char to = (char) Integer.parseInt(cfg.getToVal(), 16);
                attribute = attribute.replaceAll(
                    "[" + String.valueOf(from) + "]", String.valueOf(to));
            }
        }
        QueryBuilder<SampleSpecif> builder =
            repository.queryBuilder(SampleSpecif.class);
        if (isId) {
            builder.and("id", attribute);
        } else {
            builder.and("extId", attribute);
        }
        List<SampleSpecif> zusatz =
            (List<SampleSpecif>) repository.filterPlain(builder.getQuery());

        doDefaults(zusatzwert);
        doConverts(zusatzwert);
        doTransforms(zusatzwert);
        if (zusatz == null || zusatz.isEmpty()) {
            currentWarnings.add(new ReportItem(
                (isId) ? "PROBENZUSATZBESCHREIBUNG" : "PZB_S",
                attribute,
                StatusCodes.IMP_INVALID_VALUE));
            return null;
        }
        zusatzwert.setPzsId(zusatz.get(0).getId());
        return zusatzwert;
    }

    private MeasVal createMesswert(
        Map<String, String> attributes,
        int messungsId
    ) {
        MeasVal messwert = new MeasVal();
        messwert.setMeasmId(messungsId);
        if (attributes.containsKey("MESSGROESSE_ID")) {
                Measd messgreosse = repository.getByIdPlain(
                    Measd.class,
                    Integer.valueOf(attributes.get("MESSGROESSE_ID"))
                );
            if (messgreosse == null) {
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
            List<ImportConf> cfgs =
                getImporterConfigByAttributeUpper("MESSGROESSE");
            String attribute = attributes.get("MESSGROESSE");
            for (int i = 0; i < cfgs.size(); i++) {
                ImportConf cfg = cfgs.get(i);
                if (cfg != null
                    && cfg.getAction().equals("convert")
                    && cfg.getFromVal().equals(attribute)
                ) {
                    attribute = cfg.getToVal();
                }
                if (cfg != null && cfg.getAction().equals("transform")) {
                    char from = (char) Integer.parseInt(cfg.getFromVal(), 16);
                    char to = (char) Integer.parseInt(cfg.getToVal(), 16);
                    attribute = attribute.replaceAll(
                        "[" + String.valueOf(from) + "]", String.valueOf(to));
                }
            }
            QueryBuilder<Measd> builder =
                repository.queryBuilder(Measd.class);
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

            builder.and("messgroesse", messgroesseString);
            List<Measd> groesse =
                (List<Measd>) repository.filterPlain(builder.getQuery());
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
                MeasUnit messEinheit = repository.getByIdPlain(
                    MeasUnit.class,
                    Integer.valueOf(attributes.get("MESSEINHEIT_ID"))
                );
            if (messEinheit == null) {
                currentWarnings.add(
                    new ReportItem(
                        "MESSWERT - MESSEINHEIT_ID",
                        attributes.get("MESSEINHEIT_ID"),
                        StatusCodes.IMP_INVALID_VALUE));
                return null;
            }
            messwert.setUnitId(
                Integer.valueOf(attributes.get("MESSEINHEIT_ID")));
        } else if (attributes.containsKey("MESSEINHEIT")) {
            List<ImportConf> cfgs =
                getImporterConfigByAttributeUpper("MESSEINHEIT");
            String attribute = attributes.get("MESSEINHEIT");
            for (int i = 0; i < cfgs.size(); i++) {
                ImportConf cfg = cfgs.get(i);
                if (cfg != null
                    && cfg.getAction().equals("convert")
                    && cfg.getFromVal().equals(attribute)
                ) {
                    attribute = cfg.getToVal();
                }
                if (cfg != null && cfg.getAction().equals("transform")) {
                    char from = (char) Integer.parseInt(cfg.getFromVal(), 16);
                    char to = (char) Integer.parseInt(cfg.getToVal(), 16);
                    attribute = attribute.replaceAll(
                        "[" + String.valueOf(from) + "]", String.valueOf(to));
                }
            }
            QueryBuilder<MeasUnit> builder =
                repository.queryBuilder(MeasUnit.class);
            builder.and("unitSymbol", attribute);
            List<MeasUnit> einheit =
                (List<MeasUnit>) repository.filterPlain(builder.getQuery());
            if (einheit == null || einheit.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        "MESSWERT - MESSEINHEIT",
                        attribute,
                        StatusCodes.IMP_INVALID_VALUE));
                return null;
            }
            messwert.setUnitId(einheit.get(0).getId());
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
        doDefaults(messwert);
        doConverts(messwert);
        doTransforms(messwert);
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

    private CommMeasm createMessungKommentar(
        Map<String, String> attributes,
        int messungsId,
        Sample probe
    ) {
        if (attributes.get("TEXT").equals("")) {
            currentWarnings.add(
                new ReportItem("KOMMENTAR", "Text", StatusCodes.VALUE_MISSING));
            return null;
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

        // TODO: Why does the following duplicate a validation rule?
        QueryBuilder<CommMeasm> kommentarBuilder = repository
            .queryBuilder(CommMeasm.class)
            .and("measmId", messungsId);
        List<CommMeasm> kommentarExist = repository.filterPlain(
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
            return null;
        }
        kommentar.setText(attributes.get("TEXT"));
        doDefaults(kommentar);
        doConverts(kommentar);
        doTransforms(kommentar);
        if (!userInfo.getMessstellen().contains(kommentar.getMeasFacilId())) {
            currentWarnings.add(
                new ReportItem(
                    userInfo.getName(),
                    "Messungs Kommentar: " + kommentar.getMeasFacilId(),
                    StatusCodes.NOT_ALLOWED));
            return null;
        }
        return kommentar;
    }

    private void createStatusProtokoll(
        String status,
        Measm messung,
        String mstId
    ) {
        //check for warnings in Probeobject - if true prevent status 7
        Boolean probeWarnings = true;
        probeWarnings = currentWarnings.stream().anyMatch(elem -> (elem.getKey().equals("validation#probe")));

        for (int i = 1; i <= 3; i++) {
            if (status.substring(i - 1, i).equals("0")) {
                // no further status settings
                return;
            } else if (currentErrors.isEmpty() && currentWarnings.isEmpty()
              ) {
                if (!addStatusProtokollEntry(
                        i,
                        Integer.valueOf(status.substring(i - 1, i)),
                        messung,
                        mstId)
                ) {
                    return;
                }
                } else if (status.substring(i - 1, i).equals("7") && !probeWarnings) {
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
        QueryBuilder<StatusMp> builder =
            repository.queryBuilder(StatusMp.class);
        builder.and("statusVal", statusWert);
        builder.and("statusLev", statusStufe);
        List<StatusMp> kombi =
            (List<StatusMp>) repository.filterPlain(builder.getQuery());
        if (kombi != null && !kombi.isEmpty()) {
            newKombi = kombi.get(0).getId();
        } else {
            currentWarnings.add(
                new ReportItem(
                    "status#" + statusStufe,
                    statusWert,
                    StatusCodes.IMP_INVALID_VALUE));
            return false;
        }
        // get current status kombi
        StatusProt currentStatus = repository.getByIdPlain(
            StatusProt.class, messung.getStatus());
        StatusMp currentKombi = repository.getByIdPlain(
            StatusMp.class, currentStatus.getStatusComb());
        // check if erreichbar
        QueryBuilder<StatusAccessMpView> errFilter =
            repository.queryBuilder(StatusAccessMpView.class);
        errFilter.and("levId", statusStufe);
        errFilter.and("valId", statusWert);
        errFilter.and("curLev", currentKombi.getStatusLev().getId());
        errFilter.and("curVal", currentKombi.getStatusVal().getId());
        List<StatusAccessMpView> erreichbar =
            repository.filterPlain(errFilter.getQuery());
        if (erreichbar.isEmpty()) {
            currentWarnings.add(
                new ReportItem(
                    "status#" + statusStufe,
                    statusWert,
                    StatusCodes.IMP_INVALID_VALUE));
            return false;
        }
        //Cleanup Messwerte for Status 7
            QueryBuilder<MeasVal> builderMW =
                repository.queryBuilder(MeasVal.class);
            builderMW.and("measmId", messung.getId());
            Response messwertQry =
                repository.filter(builderMW.getQuery());
            @SuppressWarnings("unchecked")
            List<MeasVal> messwerte = (List<MeasVal>) messwertQry.getData();
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
                for (int i = 0; i < messwerte.size(); i++) {
                    repository.delete(messwerte.get(i));
                }
            }
        }

        // Validator: StatusAssignment
        StatusProt newStatus = new StatusProt();
        newStatus.setDate(new Timestamp(new Date().getTime()));
        newStatus.setMeasmId(messung.getId());
        newStatus.setMeasFacilId(mstId);
        newStatus.setStatusComb(newKombi);
        Violation statusViolation = statusValidator.validate(newStatus);

        if (statusViolation.hasWarnings()) {
            statusViolation.getWarnings().forEach((k, v) -> {
                v.forEach((value) -> {
                    currentErrors.add(new ReportItem("Status ", k, value));
                });
            });
        }

        if (statusViolation.hasNotifications()) {
            statusViolation.getNotifications().forEach((k, v) -> {
                v.forEach((value) -> {
                    currentNotifications.add(
                        new ReportItem("Status ", k, value));
                });
            });
        }

        if (statusViolation.hasErrors()) {
            statusViolation.getErrors().forEach((k, v) -> {
                v.forEach((value) -> {
                    currentErrors.add(new ReportItem("Status ", k, value));
                });
            });
        }

        if (statusViolation.hasErrors() || statusViolation.hasWarnings()) {
          return false;
        }

        // check auth
        MeasFacil messStelle =
            repository.getByIdPlain(MeasFacil.class, mstId);
        if ((statusStufe == 1
            && userInfo.getFunktionenForMst(mstId).contains(1))
            || (statusStufe == 2
                && userInfo.getNetzbetreiber().contains(
                    messStelle.getNetworkId())
                && userInfo.getFunktionenForNetzbetreiber(
                    messStelle.getNetworkId()).contains(2))
            || (statusStufe == 3
                && userInfo.getFunktionen().contains(3))
        ) {
            //persist newStatus if authorized to do so
            repository.create(newStatus);
            if (newKombi == 0 || newKombi == 9 || newKombi == 13) {
                messung.setIsCompleted(false);
            } else {
                messung.setIsCompleted(true);
            }
            messung.setStatus(newStatus.getId());
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

        QueryBuilder<Geolocat> builder =
            repository.queryBuilder(Geolocat.class);
        builder.and("sampleId", probe.getId());
        List<Geolocat> zuordnungen =
            repository.filterPlain(builder.getQuery());
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
            QueryBuilder<Site> builder1 = repository.queryBuilder(Site.class);
            builder1.and("extId", uo.get("U_ORTS_ZUSATZCODE"));
            List<Site> messpunkte =
                repository.filterPlain(builder1.getQuery());
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
                probe.setNuclFacilGrId(messpunkte.get(0).getReiNuclFacilGrId());
                repository.update(probe);
            } else if (uo.get("U_ORTS_ZUSATZCODE").length() == 4) {
                QueryBuilder<NuclFacilGr> builderKta =
                    repository.queryBuilder(NuclFacilGr.class);
                builderKta.and("extId", uo.get("U_ORTS_ZUSATZCODE"));
                List<NuclFacilGr> ktaGrp =
                    repository.filterPlain(builderKta.getQuery());
                if (!ktaGrp.isEmpty()) {
                    Site o = null;
                    //check for Koordinates U_Ort (primary): If none are present, assume Koordinates
                    //in P_Ort. If P_Ort is not valid - this import must fail.
                    if (uort.get(0).get("U_KOORDINATEN_ART_S") != null
                    && uort.get(0).get("U_KOORDINATEN_ART_S").equals("")
                    && uort.get(0).get("U_KOORDINATEN_X") != null
                    && uort.get(0).get("U_KOORDINATEN_X").equals("")
                    && uort.get(0).get("U_KOORDINATEN_Y") != null
                    && uort.get(0).get("U_KOORDINATEN_Y").equals("")
                    ) {
                        o = findOrCreateOrt(uort.get(0), "U_", probe);
                    }

                    if (o == null) {
                        Site oE = findOrCreateOrt(object.getEntnahmeOrt(), "P_", probe);
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
                        o.setSiteClassId(1);
                        o.setReiNuclFacilGrId(ktaGrp.get(0).getId());
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
            o.setSiteClassId(3);
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
        if (type.equals("E")) {type = "P";}
        Site o = findOrCreateOrt(rawOrt, type+"_", probe);
        if (o == null) {
            return null;
        }
        ort.setSiteId(o.getId());
        ort.setPoiId(o.getPoiId());
        if (rawOrt.containsKey(type+"_ORTS_ZUSATZCODE")) {
            Poi zusatz = repository.getByIdPlain(
                Poi.class,
                rawOrt.get(type+"_ORTS_ZUSATZCODE")
            );
            if (zusatz == null) {
                currentWarnings.add(
                    new ReportItem(
                        type+"_ORTS_ZUSATZCODE",
                        rawOrt.get(type+"_ORTS_ZUSATZCODE"),
                        StatusCodes.IMP_INVALID_VALUE));
            } else {
                ort.setPoiId(zusatz.getId());
            }
        }
        if (rawOrt.containsKey(type+"_ORTS_ZUSATZTEXT")) {
            ort.setAddSiteText(rawOrt.get(type+"_ORTS_ZUSATZTEXT"));
        }
        doDefaults(ort);
        doConverts(ort);
        doTransforms(ort);
        return ort;
    }

    private Site findOrCreateOrt(
        Map<String, String> attributes,
        String type,
        Sample probe
    ) {
        Site o = new Site();
        doDefaults(o);
        // If laf contains coordinates, find a ort with matching coordinates or
        // create one.
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
                SpatRefSys koordinatenArt = repository.getByIdPlain(
                    SpatRefSys.class, o.getSpatRefSysId());
                if (koordinatenArt == null) {
                    currentWarnings.add(
                        new ReportItem(
                            type + "KOORDINATEN_ART_S",
                            attributes.get(type + "KOORDINATEN_ART_S"),
                            StatusCodes.IMP_INVALID_VALUE));
                    o.setSpatRefSysId(null);
                }
            } else {
                QueryBuilder<SpatRefSys> kdaBuilder =
                    repository.queryBuilder(SpatRefSys.class);
                kdaBuilder.and(
                    "koordinatenart", attributes.get(type + "KOORDINATEN_ART"));
                List<SpatRefSys> arten =
                    repository.filterPlain(
                        kdaBuilder.getQuery());
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
        // If laf contains gemeinde attributes, find a ort with matching gemId
        // or create one.
        if (attributes.get(type + "GEMEINDENAME") != null) {
            QueryBuilder<AdminUnit> builder =
                repository.queryBuilder(AdminUnit.class);
            builder.and("name", attributes.get(type + "GEMEINDENAME"));
            List<AdminUnit> ves =
                repository.filterPlain(builder.getQuery());
            if (ves == null || ves.size() == 0) {
                currentWarnings.add(
                    new ReportItem(
                        "GEMEINDENAME",
                        attributes.get(type + "GEMEINDENAME"),
                        StatusCodes.IMP_INVALID_VALUE));
            } else {
                o.setMunicId(ves.get(0).getId());
            }
        } else if (attributes.get(type + "GEMEINDESCHLUESSEL") != null) {
            o.setMunicId(attributes.get(type + "GEMEINDESCHLUESSEL"));
            AdminUnit v =
                repository.getByIdPlain(
                    AdminUnit.class, o.getMunicId());
            if (v == null) {
                currentWarnings.add(
                    new ReportItem(
                        type + "GEMEINDESCHLUESSEL", o.getMunicId(),
                        StatusCodes.IMP_INVALID_VALUE));
                o.setMunicId(null);
            }
        }
        String key = "";
        String hLand = "";
        String staatFilter = "";
        if (attributes.get(type + "HERKUNFTSLAND_S") != null) {
            staatFilter = "id";
            key = "HERKUNFTSLAND_S";
            hLand = attributes.get(type + "HERKUNFTSLAND_S");
        } else if (attributes.get(type + "HERKUNFTSLAND_KURZ") != null) {
            staatFilter = "intVehRegCode";
            key = "HERKUNFTSLAND_KURZ";
            hLand = attributes.get(type + "HERKUNFTSLAND_KURZ");
        } else if (attributes.get(type + "HERKUNFTSLAND_LANG") != null) {
            staatFilter = "ctry";
            key = "HERKUNFTSLAND_LANG";
            hLand = attributes.get(type + "HERKUNFTSLAND_LANG");
        }

        if (staatFilter.length() > 0) {
            QueryBuilder<State> builderStaat =
                repository.queryBuilder(State.class);
            builderStaat.and(staatFilter, hLand);
            List<State> staat =
                repository.filterPlain(builderStaat.getQuery());
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

        // checkk if all attributes are empty
        if (o.getSpatRefSysId() == null
            && o.getMunicId() == null
            && o.getStateId() == null) {
            return null;
        }

        MeasFacil mst = repository.getByIdPlain(
            MeasFacil.class, probe.getMeasFacilId());
        o.setNetworkId(mst.getNetworkId());
        o = ortFactory.completeOrt(o);
        if (o == null || o.getGeom() == null) {
            currentWarnings.addAll(ortFactory.getErrors());
            return null;
        }
        Violation violation = ortValidator.validate(o);
        for (Entry<String, List<Integer>> warn
            : violation.getWarnings().entrySet()
        ) {
            for (Integer code : warn.getValue()) {
                currentWarnings.add(
                    new ReportItem("validation", warn.getKey(), code));
            }
        }
        if (violation.hasErrors()) {
            for (Entry<String, List<Integer>> err
                : violation.getErrors().entrySet()) {
                for (Integer code : err.getValue()) {
                    // Add to warnings because Sample object might be imported
                    currentWarnings.add(
                        new ReportItem("validation", err.getKey(), code));
                }
            }
            return null;
        }
        if (o.getId() != null) {
            return o;
        }
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
        QueryBuilder<Tag> builderTag =
                repository.queryBuilder(Tag.class);
            builderTag.and("name", szenario);
            builderTag.and("tagType", "global");
            List<Tag> globalTag =
                repository.filterPlain(builderTag.getQuery());

        if (globalTag.isEmpty()){
            ReportItem note = new ReportItem();
            note.setCode(StatusCodes.VALUE_NOT_MATCHING);
            note.setKey("globalTag");
            note.setValue(szenario);
            currentWarnings.add(note);
        } else {
                TagLink tagZuord = new TagLink();

            if (object instanceof Sample) {
                Sample probe = (Sample) object;
                QueryBuilder<TagLink> builderZuord =
                        repository.queryBuilder(TagLink.class);
                    builderZuord.and("sampleId", probe.getId());
                    List<TagLink> globalTagZuord =
                        repository.filterPlain(builderZuord.getQuery());
                 if (globalTagZuord.stream().anyMatch(z -> z.getTagId().equals(globalTag.get(0).getId()))) {
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
                Measm messung = (Measm) object;
                QueryBuilder<TagLink> builderZuord =
                    repository.queryBuilder(TagLink.class);
                builderZuord.and("measmId", messung.getId());
                List<TagLink> globalTagZuord =
                    repository.filterPlain(builderZuord.getQuery());

                if (globalTagZuord.stream().anyMatch(z -> z.getTagId().equals(globalTag.get(0).getId()))) {
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

    private void logProbe(Sample probe) {
        logger.debug("%PROBE%");
        logger.debug("datenbasis: " + probe.getRegulationId());
        logger.debug("betriebsart: " + probe.getOprModeId());
        logger.debug("erzeuger: " + probe.getDatasetCreatorId());
        logger.debug("hauptprobennummer: " + probe.getMainSampleId());
        logger.debug("externeprobeid: " + probe.getExtId());
        logger.debug("labor: " + probe.getApprLabId());
        logger.debug("deskriptoren: " + probe.getEnvDescripDisplay());
        logger.debug("media: " + probe.getEnvDescripName());
        logger.debug("mittelung: " + probe.getMidSampleDate());
        logger.debug("mpl: " + probe.getStateMpgId());
        logger.debug("mpr: " + probe.getMpgId());
        logger.debug("mst: " + probe.getMeasFacilId());
        logger.debug("pnbeginn: " + probe.getSampleStartDate());
        logger.debug("pnende: " + probe.getSampleEndDate());
        logger.debug("probenart: " + probe.getSampleMethId());
        logger.debug("probenehmer: " + probe.getSamplerId());
        logger.debug("sbeginn: " + probe.getSchedStartDate());
        logger.debug("sende: " + probe.getSchedEndDate());
        logger.debug("ursprungszeit: " + probe.getOrigDate());
        logger.debug("test: " + probe.getIsTest());
        logger.debug("umw: " + probe.getEnvMediumId());
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
            Regulation datenbasis = repository.getByIdPlain(
                Regulation.class,
                Integer.valueOf(value.toString())
            );
            if (datenbasis == null) {
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
            List<ImportConf> cfgs =
                getImporterConfigByAttributeUpper("DATENBASIS");
            String attr = value.toString();
            for (int i = 0; i < cfgs.size(); i++) {
                ImportConf cfg = cfgs.get(i);
                if (cfg != null
                    && cfg.getAction().equals("convert")
                    && cfg.getFromVal().equals(attr)
                ) {
                    attr = cfg.getToVal();
                }
                if (cfg != null && cfg.getAction().equals("transform")) {
                    char from = (char) Integer.parseInt(cfg.getFromVal(), 16);
                    char to = (char) Integer.parseInt(cfg.getToVal(), 16);
                    attr = attr.replaceAll(
                        "[" + String.valueOf(from) + "]", String.valueOf(to));
                }
            }
            QueryBuilder<Regulation> builder =
                repository.queryBuilder(Regulation.class);
            builder.and("regulation", attr);
            List<Regulation> datenbasis =
                (List<Regulation>) repository.filterPlain(builder.getQuery());
            if (datenbasis == null || datenbasis.isEmpty()) {
                currentErrors.add(
                    new ReportItem(key, attr, StatusCodes.IMP_INVALID_VALUE));
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
            MeasFacil mst = repository.getByIdPlain(
                MeasFacil.class, value.toString());
            if (mst == null) {
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
            QueryBuilder<MpgTransf> builder =
                repository.queryBuilder(MpgTransf.class);
            builder.and("extId", value);
            List<MpgTransf> transfer =
                (List<MpgTransf>) repository.filterPlain(
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
            QueryBuilder<MpgTransf> builder =
                repository.queryBuilder(MpgTransf.class);
            builder.and("name", value);
            List<MpgTransf> transfer =
                (List<MpgTransf>) repository.filterPlain(
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

        if ("ERZEUGER".equals(key)) {
            QueryBuilder<DatasetCreator> builder =
                repository.queryBuilder(DatasetCreator.class);
            builder.and("networkId", netzbetreiberId);
            builder.and("measFacilId", probe.getMeasFacilId());
            builder.and("extId", value);
            List<DatasetCreator> datensatzErzeuger =
                    (List<DatasetCreator>) repository.filterPlain(
                            builder.getQuery());
            if (datensatzErzeuger == null || datensatzErzeuger.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setDatasetCreatorId(datensatzErzeuger.get(0).getId());
        }

        if ("MESSPROGRAMM_LAND".equals(key)) {
            QueryBuilder<MpgCateg> builder =
                repository.queryBuilder(MpgCateg.class);
            builder.and("networkId", netzbetreiberId);
            builder.and("extId", value);
            List<MpgCateg> kategorie =
                    (List<MpgCateg>) repository.filterPlain(
                            builder.getQuery());
            if (kategorie == null || kategorie.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setStateMpgId(kategorie.get(0).getId());
        }

        if ("PROBENAHMEINSTITUTION".equals(key)) {
            QueryBuilder<Sampler> builder =
                repository.queryBuilder(Sampler.class);
            builder.and("networkId", netzbetreiberId);
            builder.and("exitId", value);
            List<Sampler> prn =
                    (List<Sampler>) repository.filterPlain(
                        builder.getQuery());
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
            EnvMedium umw = repository.getByIdPlain(
                EnvMedium.class, value.toString());
            if (umw == null) {
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
            QueryBuilder<EnvMedium> builder =
                repository.queryBuilder(EnvMedium.class);
            int length = value.toString().length() > 80
                ? 80
                : value.toString().length();
            builder.and("name", value.toString().substring(0, length));
            List<EnvMedium> umwelt =
                (List<EnvMedium>) repository.filterPlain(builder.getQuery());
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
            QueryBuilder<ReiAgGr> builder =
                repository.queryBuilder(ReiAgGr.class);
            builder.and("name", value.toString());
            List<ReiAgGr> list =
                repository.filterPlain(builder.getQuery());
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
            List<ImportConf> cfgs =
                getImporterConfigByAttributeUpper("PROBENART");
            String attr = value.toString();
            for (int i = 0; i < cfgs.size(); i++) {
                ImportConf cfg = cfgs.get(i);
                if (cfg != null
                    && cfg.getAction().equals("convert")
                    && cfg.getFromVal().equals(attr)
                ) {
                    attr = cfg.getToVal();
                }
                if (cfg != null && cfg.getAction().equals("transform")) {
                    char from = (char) Integer.parseInt(cfg.getFromVal(), 16);
                    char to = (char) Integer.parseInt(cfg.getToVal(), 16);
                    attr = attr.replaceAll(
                        "[" + String.valueOf(from) + "]", String.valueOf(to));
                }
            }
            QueryBuilder<SampleMeth> builder =
                repository.queryBuilder(SampleMeth.class);
            builder.and("extId", attr);
            List<SampleMeth> probenart =
                (List<SampleMeth>) repository.filterPlain(builder.getQuery());
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
    public Measm addMessungAttribute(
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
            Mmt mmt = repository.getByIdPlain(
                Mmt.class, value.toString());
            if (mmt == null) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
            } else {
                messung.setMmtId(value.toString());
            }
        } else if ("MESSMETHODE_C".equals(key)) {
            QueryBuilder<Mmt> builder =
                repository.queryBuilder(Mmt.class);
            builder.and("name", value.toString());
            List<Mmt> mm =
                (List<Mmt>) repository.filterPlain(builder.getQuery());
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
     * @return the userInfo
     */
    public UserInfo getUserInfo() {
        return userInfo;
    }

    /**
     * @param userInfo the userInfo to set
     */
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
        this.authorizer = new HeaderAuthorization(userInfo, this.repository);
    }

    /**
     * @return the config
     */
    public List<ImportConf> getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(List<ImportConf> config) {
        this.config = config;
    }
}
