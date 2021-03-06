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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.intevation.lada.factory.OrtFactory;
import de.intevation.lada.factory.ProbeFactory;
import de.intevation.lada.importer.Identified;
import de.intevation.lada.importer.Identifier;
import de.intevation.lada.importer.IdentifierConfig;
import de.intevation.lada.importer.ObjectMerger;
import de.intevation.lada.importer.ReportItem;
import de.intevation.lada.model.land.KommentarM;
import de.intevation.lada.model.land.KommentarP;
import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Messwert;
import de.intevation.lada.model.land.Ortszuordnung;
import de.intevation.lada.model.land.Probe;
import de.intevation.lada.model.land.StatusProtokoll;
import de.intevation.lada.model.land.ZusatzWert;
import de.intevation.lada.model.stammdaten.Datenbasis;
import de.intevation.lada.model.stammdaten.DatensatzErzeuger;
import de.intevation.lada.model.stammdaten.ImporterConfig;
import de.intevation.lada.model.stammdaten.KoordinatenArt;
import de.intevation.lada.model.stammdaten.KtaGruppe;
import de.intevation.lada.model.stammdaten.MessEinheit;
import de.intevation.lada.model.stammdaten.MessMethode;
import de.intevation.lada.model.stammdaten.MessStelle;
import de.intevation.lada.model.stammdaten.Messgroesse;
import de.intevation.lada.model.stammdaten.MessprogrammKategorie;
import de.intevation.lada.model.stammdaten.MessprogrammTransfer;
import de.intevation.lada.model.stammdaten.Ort;
import de.intevation.lada.model.stammdaten.Ortszusatz;
import de.intevation.lada.model.stammdaten.ProbenZusatz;
import de.intevation.lada.model.stammdaten.Probenart;
import de.intevation.lada.model.stammdaten.Probenehmer;
import de.intevation.lada.model.stammdaten.ReiProgpunktGruppe;
import de.intevation.lada.model.stammdaten.Staat;
import de.intevation.lada.model.stammdaten.StatusErreichbar;
import de.intevation.lada.model.stammdaten.StatusKombi;
import de.intevation.lada.model.stammdaten.Umwelt;
import de.intevation.lada.model.stammdaten.Verwaltungseinheit;
import de.intevation.lada.model.stammdaten.Zeitbasis;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.MesswertNormalizer;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
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

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorizer;

    @Inject
    @ValidationConfig(type = "Probe")
    private Validator probeValidator;

    @Inject
    @ValidationConfig(type = "Messung")
    private Validator messungValidator;

    @Inject
    @ValidationConfig(type = "Ort")
    private Validator ortValidator;

    @Inject
    @IdentifierConfig(type = "Probe")
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

    private List<ImporterConfig> config;

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

    private void create(LafRawData.Probe object) {
        currentWarnings = new ArrayList<>();
        currentErrors = new ArrayList<>();
        currentNotifications = new ArrayList<>();
        Probe probe = new Probe();
        String netzbetreiberId = null;

        Iterator<ImporterConfig> importerConfig = config.iterator();
        while (importerConfig.hasNext()) {
            ImporterConfig current = importerConfig.next();
            if ("ZEITBASIS".equals(current.getName().toUpperCase())) {
                currentZeitbasis = Integer.valueOf(current.getToValue());
            }
            if ("PROBE".equals(current.getName().toUpperCase())
                && "MSTID".equals(current.getAttribute().toUpperCase())
                && "DEFAULT".equals(current.getAction().toUpperCase())) {
                probe.setMstId(current.getToValue());
            }
        }
        if (object.getAttributes().containsKey("MESSSTELLE")) {
            probe.setMstId(object.getAttributes().get("MESSSTELLE"));
        }
        if (probe.getMstId() == null) {
            currentErrors.add(
                new ReportItem(
                    "MESSSTELLE", "", StatusCodes.IMP_MISSING_VALUE));
            errors.put(object.getIdentifier(),
                new ArrayList<ReportItem>(currentErrors));
            return;
        } else {
            MessStelle mst = repository.getByIdPlain(
                MessStelle.class, probe.getMstId());
            if (mst == null) {
                currentErrors.add(
                    new ReportItem(
                        "MESSSTELLE",
                        probe.getMstId(), StatusCodes.IMP_INVALID_VALUE));
                errors.put(
                    object.getIdentifier(),
                    new ArrayList<ReportItem>(currentErrors));
                return;
            }
            netzbetreiberId = mst.getNetzbetreiberId();
        }

        if (object.getAttributes().containsKey("ZEITBASIS")) {
            List<ImporterConfig> cfg =
            getImporterConfigByAttributeUpper("ZEITBASIS");
            String attribute = object.getAttributes().get("ZEITBASIS");
            if (!cfg.isEmpty() && attribute.equals(cfg.get(0).getFromValue())) {
                attribute = cfg.get(0).getToValue();
            }
            QueryBuilder<Zeitbasis> builder =
                repository.queryBuilder(Zeitbasis.class);
            builder.and("bezeichnung", attribute);
            List<Zeitbasis> zb = repository.filterPlain(builder.getQuery());
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
            Zeitbasis zeitbasis = repository.getByIdPlain(
                Zeitbasis.class,
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
        if (probe.getLaborMstId() == null) {
            probe.setLaborMstId(probe.getMstId());
        }
        // Use the deskriptor string to find the medium
        probe = factory.findMediaDesk(probe);
        if (probe.getUmwId() == null) {
            factory.findUmweltId(probe);
        }

        // Check if the user is authorized to create the probe
        boolean isAuthorized =
            authorizer.isAuthorized(userInfo, probe, Probe.class);
        if (!isAuthorized) {
            ReportItem err = new ReportItem();
            err.setCode(StatusCodes.NOT_ALLOWED);
            err.setKey(userInfo.getName());
            err.setValue("Messstelle " + probe.getMstId());
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
        Probe newProbe = null;
        boolean oldProbeIsReadonly = false;
        boolean isAuthorizedOld = false;

        try {
            Identified i = probeIdentifier.find(probe);
            Probe old = (Probe) probeIdentifier.getExisting();
            // Matching probe was found in the db. Update it!
            if (i == Identified.UPDATE) {
                isAuthorizedOld =
                    authorizer.isAuthorized(userInfo, old, Probe.class);
                oldProbeIsReadonly = authorizer.isReadOnly(old.getId());
                if (isAuthorizedOld) {
                    if (oldProbeIsReadonly) {
                        newProbe = old;
                        currentNotifications.add(
                            new ReportItem(
                                "probe",
                                old.getExterneProbeId(),
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
                    err.setValue("Messstelle " + old.getMstId());
                    currentWarnings.clear();
                    currentErrors.add(err);
                    errors.put(
                        object.getIdentifier(),
                        new ArrayList<ReportItem>(currentErrors));
                    return;
                }
            } else if (i == Identified.REJECT) {
                // Probe was found but some data does not match
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
                    newProbe = ((Probe) created.getData());
                } else {
                    for (Entry<String, List<Integer>> err
                        : violation.getErrors().entrySet()
                    ) {
                        for (Integer code : err.getValue()) {
                            currentErrors.add(
                                new ReportItem(
                                    "validation", err.getKey(), code));
                        }
                    }
                    for (Entry<String, List<Integer>> warn
                        :violation.getWarnings().entrySet()
                    ) {
                        for (Integer code : warn.getValue()) {
                            currentWarnings.add(
                                new ReportItem(
                                    "validation", warn.getKey(), code));
                        }
                    }
                    for (Entry<String, List<Integer>> notes
                        : violation.getNotifications().entrySet()
                    ) {
                        for (Integer code :notes.getValue()) {
                            currentNotifications.add(
                                new ReportItem(
                                    "validation", notes.getKey(), code));
                        }
                    }
                }
            }
            if (newProbe != null) {
                importProbeIds.add(newProbe.getId());
            }
        } catch (InvalidTargetObjectTypeException e) {
            ReportItem err = new ReportItem();
            err.setCode(StatusCodes.ERROR_VALIDATION);
            err.setKey("not known");
            err.setValue("No valid Probe Object");
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
                List<KommentarP> kommentare = new ArrayList<>();
                for (int i = 0; i < object.getKommentare().size(); i++) {
                    KommentarP tmp =
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
                if (probe.getReiProgpunktGrpId() != null
                    || Integer.valueOf(3).equals(probe.getDatenbasisId())
                    || Integer.valueOf(4).equals(probe.getDatenbasisId())
                ) {
                    createReiMesspunkt(object, newProbe);
                } else {
                    // Merge entnahmeOrt
                    createEntnahmeOrt(object.getEntnahmeOrt(), newProbe);

                    // Create ursprungsOrte
                    List<Ortszuordnung> uOrte = new ArrayList<>();
                    for (int i = 0; i < object.getUrsprungsOrte().size(); i++) {
                        Ortszuordnung tmp =
                            createUrsprungsOrt(
                                object.getUrsprungsOrte().get(i), newProbe);
                        if (tmp != null) {
                            uOrte.add(tmp);
                        }
                    }
                    // Persist ursprungsOrte
                    merger.mergeUrsprungsOrte(newProbe.getId(), uOrte);
                }
            }

            // Validate probe object
            Violation violation = probeValidator.validate(newProbe);
            for (Entry<String, List<Integer>> err
                : violation.getErrors().entrySet()
            ) {
                for (Integer code : err.getValue()) {
                    currentErrors.add(
                        new ReportItem("validation", err.getKey(), code));
                }
            }
            for (Entry<String, List<Integer>> warn
                : violation.getWarnings().entrySet()
            ) {
                for (Integer code : warn.getValue()) {
                    currentWarnings.add(
                        new ReportItem("validation", warn.getKey(), code));
                }
            }
            for (Entry<String, List<Integer>> notes
                : violation.getNotifications().entrySet()
            ) {
              for (Integer code: notes.getValue()) {
                currentNotifications.add(
                    new ReportItem("validation", notes.getKey(), code));
              }
            }
            // Create messung objects
            for (int i = 0; i < object.getMessungen().size(); i++) {
                create(
                    object.getMessungen().get(i),
                    newProbe,
                    newProbe.getMstId());
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

    private void doDefaults(Probe probe) {
        doDefaults(probe, Probe.class, "probe");
    }

    private void doConverts(Probe probe) {
        doConverts(probe, Probe.class, "probe");
    }

    private void doTransforms(Probe probe) {
        doTransformations(probe, Probe.class, "probe");
    }

    private void doDefaults(Messung messung) {
        doDefaults(messung, Messung.class, "messung");
    }

    private void doConverts(Messung messung) {
        doConverts(messung, Messung.class, "messung");
    }

    private void doTransforms(Messung messung) {
        doTransformations(messung, Messung.class, "messung");
    }

    private void doDefaults(Messwert messwert) {
        doDefaults(messwert, Messwert.class, "messwert");
    }

    private void doConverts(Messwert messwert) {
        doConverts(messwert, Messwert.class, "messwert");
    }

    private void doTransforms(Messwert messwert) {
        doTransformations(messwert, Messwert.class, "messwert");
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

    private void doDefaults(KommentarM kommentar) {
        doDefaults(kommentar, KommentarM.class, "kommentarm");
    }

    private void doConverts(KommentarM kommentar) {
        doConverts(kommentar, KommentarM.class, "kommentarm");
    }

    private void doTransforms(KommentarM kommentar) {
        doTransformations(kommentar, KommentarM.class, "kommentarm");
    }

    private void doDefaults(KommentarP kommentar) {
        doDefaults(kommentar, KommentarP.class, "kommentarp");
    }

    private void doConverts(KommentarP kommentar) {
        doConverts(kommentar, KommentarP.class, "kommentarp");
    }

    private void doTransforms(KommentarP kommentar) {
        doTransformations(kommentar, KommentarP.class, "kommentarp");
    }

    private void doDefaults(Ortszuordnung ort) {
        doDefaults(ort, Ortszuordnung.class, "ortszuordnung");
    }

    private void doConverts(Ortszuordnung ort) {
        doDefaults(ort, Ortszuordnung.class, "ortszuordnung");
    }

    private void doTransforms(Ortszuordnung ort) {
        doTransformations(ort, Ortszuordnung.class, "ortszuordnung");
    }

    private <T> void doDefaults(Object object, Class<T> clazz, String table) {
        Iterator<ImporterConfig> i = config.iterator();
        while (i.hasNext()) {
            ImporterConfig current = i.next();
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
                                    Integer.valueOf(current.getToValue()));
                            } else {
                                // we handle the default as string.
                                // Other parameter types are not implemented!
                                setter.invoke(object, current.getToValue());
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

    private List<ImporterConfig> getImporterConfigByAttributeUpper(
        String attribute
    ) {
        Iterator<ImporterConfig> i = config.iterator();
        List<ImporterConfig> result = new ArrayList<ImporterConfig>();
        while (i.hasNext()) {
            ImporterConfig current = i.next();
            if (current.getAttribute().toUpperCase().equals(attribute)) {
                result.add(current);
            }
        }
        return result;
    }

    private <T> void doConverts(Object object, Class<T> clazz, String table) {
        Iterator<ImporterConfig> i = config.iterator();
        while (i.hasNext()) {
            ImporterConfig current = i.next();
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
                    if (value.equals(current.getFromValue())
                        && setter != null
                    ) {
                        setter.invoke(object, current.getToValue());
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
        Iterator<ImporterConfig> i = config.iterator();
        while (i.hasNext()) {
            ImporterConfig current = i.next();
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
                        current.getFromValue(), 16);
                    char to = (char) Integer.parseInt(
                        current.getToValue(), 16);
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
        Probe probe, String mstId
    ) {
        Messung messung = new Messung();
        messung.setProbeId(probe.getId());

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
        if (!authorizer.isAuthorizedOnNew(userInfo, messung, Messung.class)) {
            ReportItem warn = new ReportItem();
            warn.setCode(StatusCodes.NOT_ALLOWED);
            warn.setKey(userInfo.getName());
            warn.setValue("Messung: " + messung.getNebenprobenNr());
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
            err.setValue("Messung: " + messung.getNebenprobenNr());
            currentErrors.add(err);
            return;
        }
        Messung newMessung;
        boolean oldMessungIsReadonly = false;
        Messung old = (Messung) messungIdentifier.getExisting();
        switch (ident) {
        case UPDATE:
            oldMessungIsReadonly =
                authorizer.isMessungReadOnly(old.getId());
            if (oldMessungIsReadonly) {
                currentNotifications.add(
                    new ReportItem(
                        "messung",
                        old.getExterneMessungsId(),
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
                err2.setValue("Messung: " + messung.getNebenprobenNr());
                currentErrors.add(err2);
                return;
            }

            // Create a new messung and the first status
            Response created = repository.create(messung);
            newMessung = ((Messung) created.getData());
            created =
                repository.getById(
                    Messung.class, newMessung.getId());
            newMessung = ((Messung) created.getData());
            break;
        default:
            throw new IllegalArgumentException(
                "Identified with unexpected enum constant");
        }

        List<KommentarM> kommentare = new ArrayList<KommentarM>();
        for (int i = 0; i < object.getKommentare().size(); i++) {
            KommentarM tmp =
                createMessungKommentar(
                    object.getKommentare().get(i), newMessung.getId(), probe);
            if (tmp != null) {
                kommentare.add(tmp);
            }
        }
        merger.mergeMessungKommentare(newMessung, kommentare);
        List<Messwert> messwerte = new ArrayList<Messwert>();
        List<Integer> messgroessenListe = new ArrayList<Integer>();
        for (int i = 0; i < object.getMesswerte().size(); i++) {
            Messwert tmp =
                createMesswert(
                    object.getMesswerte().get(i), newMessung.getId());
            if (tmp != null) {
                //find duplicates
                if (messgroessenListe.contains(tmp.getMessgroesseId())) {
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
                    messgroessenListe.add(tmp.getMessgroesseId());
                }
            }
        }
        messwerte = messwertNormalizer.normalizeMesswerte(
            messwerte, probe.getUmwId());
        //persist messwerte
        merger.mergeMesswerte(newMessung, messwerte);
        // Check for warnings and errors for messung ...
        Violation violation = messungValidator.validate(newMessung);
        for (Entry<String, List<Integer>> err
            : violation.getErrors().entrySet()
        ) {
            for (Integer code : err.getValue()) {
                currentErrors.add(
                    new ReportItem("validation", err.getKey(), code));
            }
        }
        for (Entry<String, List<Integer>> warn
            : violation.getWarnings().entrySet()
        ) {
            for (Integer code : warn.getValue()) {
                currentWarnings.add(
                    new ReportItem("validation", warn.getKey(), code));
            }
        }
        for (Entry<String, List<Integer>> notes
            : violation.getNotifications().entrySet()
        ) {
            for (Integer code : notes.getValue()) {
                currentNotifications.add(
                    new ReportItem("validation", notes.getKey(), code));
            }
        }
        // ... and messwerte
        QueryBuilder<Messwert> messwBuilder =
            repository.queryBuilder(Messwert.class);
        messwBuilder.and("messungsId", newMessung.getId());
        Response response =
            repository.filter(messwBuilder.getQuery());
        @SuppressWarnings("unchecked")
        List<Messwert> messwerteList = (List<Messwert>) response.getData();
        for (Messwert messwert: messwerte) {
            Violation messwViolation = messwertValidator.validate(messwert);
            if (messwViolation.hasWarnings()) {
                messwViolation.getWarnings().forEach((k, v) -> {
                    v.forEach((value) -> {
                        currentWarnings.add(
                            new ReportItem("validation ", k, value));
                    });
                });
            }

            if (messwViolation.hasErrors()) {
                messwViolation.getErrors().forEach((k, v) -> {
                    v.forEach((value) -> {
                        currentErrors.add(
                            new ReportItem("validation ", k, value));
                    });
                });
            }

            if (messwViolation.hasNotifications()) {
                messwViolation.getNotifications().forEach((k, v) -> {
                    v.forEach((value) -> {
                        currentNotifications.add(
                            new ReportItem("validation ", k, value));
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

    private KommentarP createProbeKommentar(
        Map<String, String> attributes,
        Probe probe
    ) {
        if (attributes.get("TEXT").equals("")) {
            currentWarnings.add(
                new ReportItem(
                    "PROBENKOMMENTAR", "Text", StatusCodes.VALUE_MISSING));
            return null;
        }
        KommentarP kommentar = new KommentarP();
        kommentar.setProbeId(probe.getId());
        kommentar.setText(attributes.get("TEXT"));
        if (attributes.containsKey("MST_ID")) {
            kommentar.setMstId(attributes.get("MST_ID"));
        } else {
            kommentar.setMstId(probe.getMstId());
        }
        if (attributes.containsKey("DATE")) {
            String date = attributes.get("DATE") + " " + attributes.get("TIME");
            kommentar.setDatum(getDate(date));
        } else {
            kommentar.setDatum(
                Timestamp.from(
                    Instant.now().atZone(ZoneOffset.UTC).toInstant()));
        }
        doDefaults(kommentar);
        doConverts(kommentar);
        doTransforms(kommentar);
        if (!userInfo.getMessstellen().contains(kommentar.getMstId())) {
            currentWarnings.add(
                new ReportItem(
                    userInfo.getName(),
                    "Kommentar: " + kommentar.getMstId(),
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
        List<ImporterConfig> cfgs =
            getImporterConfigByAttributeUpper("ZUSATZWERT");
        String attribute = attributes.get("PZS");
        boolean isId = false;
        if (attribute == null) {
            attribute = attributes.get("PZS_ID");
            isId = true;
        }
        for (int i = 0; i < cfgs.size(); i++) {
            ImporterConfig cfg = cfgs.get(i);
            if (cfg.getAction().equals("convert")
                && cfg.getFromValue().equals(attribute)
            ) {
                attribute = cfg.getToValue();
            }
            if (cfg.getAction().equals("transform")) {
                char from = (char) Integer.parseInt(cfg.getFromValue(), 16);
                char to = (char) Integer.parseInt(cfg.getToValue(), 16);
                attribute = attribute.replaceAll(
                    "[" + String.valueOf(from) + "]", String.valueOf(to));
            }
        }
        QueryBuilder<ProbenZusatz> builder =
            repository.queryBuilder(ProbenZusatz.class);
        if (isId) {
            builder.and("id", attribute);
        } else {
            builder.and("zusatzwert", attribute);
        }
        List<ProbenZusatz> zusatz =
            (List<ProbenZusatz>) repository.filterPlain(builder.getQuery());

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

    private Messwert createMesswert(
        Map<String, String> attributes,
        int messungsId
    ) {
        Messwert messwert = new Messwert();
        messwert.setMessungsId(messungsId);
        if (attributes.containsKey("MESSGROESSE_ID")) {
                Messgroesse messgreosse = repository.getByIdPlain(
                    Messgroesse.class,
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
            messwert.setMessgroesseId(
                Integer.valueOf(attributes.get("MESSGROESSE_ID")));
        } else if (attributes.containsKey("MESSGROESSE")) {
            List<ImporterConfig> cfgs =
                getImporterConfigByAttributeUpper("MESSGROESSE");
            String attribute = attributes.get("MESSGROESSE");
            for (int i = 0; i < cfgs.size(); i++) {
                ImporterConfig cfg = cfgs.get(i);
                if (cfg != null
                    && cfg.getAction().equals("convert")
                    && cfg.getFromValue().equals(attribute)
                ) {
                    attribute = cfg.getToValue();
                }
                if (cfg != null && cfg.getAction().equals("transform")) {
                    char from = (char) Integer.parseInt(cfg.getFromValue(), 16);
                    char to = (char) Integer.parseInt(cfg.getToValue(), 16);
                    attribute = attribute.replaceAll(
                        "[" + String.valueOf(from) + "]", String.valueOf(to));
                }
            }
            QueryBuilder<Messgroesse> builder =
                repository.queryBuilder(Messgroesse.class);
            // accept various nuclide notations (e.g.
            // "Cs-134", "CS 134", "Cs134", "SC134", ...)
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
            List<Messgroesse> groesse =
                (List<Messgroesse>) repository.filterPlain(builder.getQuery());
            if (groesse == null || groesse.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        "MESSWERT - MESSGROESSE",
                        attributes.get("MESSGROESSE"),
                        StatusCodes.IMP_INVALID_VALUE));
                return null;
            }
            messwert.setMessgroesseId(groesse.get(0).getId());
        }
        if (attributes.containsKey("MESSEINHEIT_ID")) {
                MessEinheit messEinheit = repository.getByIdPlain(
                    MessEinheit.class,
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
            messwert.setMehId(
                Integer.valueOf(attributes.get("MESSEINHEIT_ID")));
        } else if (attributes.containsKey("MESSEINHEIT")) {
            List<ImporterConfig> cfgs =
                getImporterConfigByAttributeUpper("MESSEINHEIT");
            String attribute = attributes.get("MESSEINHEIT");
            for (int i = 0; i < cfgs.size(); i++) {
                ImporterConfig cfg = cfgs.get(i);
                if (cfg != null
                    && cfg.getAction().equals("convert")
                    && cfg.getFromValue().equals(attribute)
                ) {
                    attribute = cfg.getToValue();
                }
                if (cfg != null && cfg.getAction().equals("transform")) {
                    char from = (char) Integer.parseInt(cfg.getFromValue(), 16);
                    char to = (char) Integer.parseInt(cfg.getToValue(), 16);
                    attribute = attribute.replaceAll(
                        "[" + String.valueOf(from) + "]", String.valueOf(to));
                }
            }
            QueryBuilder<MessEinheit> builder =
                repository.queryBuilder(MessEinheit.class);
            builder.and("einheit", attribute);
            List<MessEinheit> einheit =
                (List<MessEinheit>) repository.filterPlain(builder.getQuery());
            if (einheit == null || einheit.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        "MESSWERT - MESSEINHEIT",
                        attribute,
                        StatusCodes.IMP_INVALID_VALUE));
                return null;
            }
            messwert.setMehId(einheit.get(0).getId());
        }

        String wert = attributes.get("MESSWERT");
        if (wert.startsWith("<")) {
            wert = wert.substring(1);
            messwert.setMesswertNwg("<");
        }
        messwert.setMesswert(Double.valueOf(wert.replaceAll(",", ".")));
        if (attributes.containsKey("MESSFEHLER")) {
            messwert.setMessfehler(
                Double.valueOf(
                    attributes.get("MESSFEHLER")
                        .replaceAll(",", ".")).floatValue());
        }
        if (attributes.containsKey("NWG")) {
            messwert.setNwgZuMesswert(
                Double.valueOf(attributes.get("NWG").replaceAll(",", ".")));
        }
        if (attributes.containsKey("GRENZWERT")) {
            messwert.setGrenzwertueberschreitung(
                attributes.get("GRENZWERT").toUpperCase().equals("J")
                    ? true : false);
        }
        doDefaults(messwert);
        doConverts(messwert);
        doTransforms(messwert);
        if (messwert.getMesswertNwg() != null
            && messwert.getNwgZuMesswert() == null
        ) {
            messwert.setNwgZuMesswert(messwert.getMesswert());
            messwert.setMesswert(null);
        } else if (messwert.getMesswertNwg() != null
            && messwert.getMesswert().equals(messwert.getNwgZuMesswert())
            || messwert.getMesswertNwg() != null
            && messwert.getMesswert() == 0.0
        ) {
            messwert.setMesswert(null);
        }
        if (messwert.getMessfehler() != null) {
            if (messwert.getMesswertNwg() != null
                && messwert.getMessfehler() == 0
            ) {
                messwert.setMessfehler(null);
            }
        }
        return messwert;
    }

    private KommentarM createMessungKommentar(
        Map<String, String> attributes,
        int messungsId,
        Probe probe
    ) {
        if (attributes.get("TEXT").equals("")) {
            currentWarnings.add(
                new ReportItem("KOMMENTAR", "Text", StatusCodes.VALUE_MISSING));
            return null;
        }
        KommentarM kommentar = new KommentarM();
        kommentar.setMessungsId(messungsId);
        if (attributes.containsKey("MST_ID")) {
            kommentar.setMstId(attributes.get("MST_ID"));
        } else {
            kommentar.setMstId(probe.getMstId());
        }
        if (attributes.containsKey("DATE")) {
            String date = attributes.get("DATE") + " " + attributes.get("TIME");
            kommentar.setDatum(getDate(date));
        } else {
            kommentar.setDatum(
                Timestamp.from(
                    Instant.now().atZone(ZoneOffset.UTC).toInstant()));
        }
        kommentar.setText(attributes.get("TEXT"));
        doDefaults(kommentar);
        doConverts(kommentar);
        doTransforms(kommentar);
        if (!userInfo.getMessstellen().contains(kommentar.getMstId())) {
            currentWarnings.add(
                new ReportItem(
                    userInfo.getName(),
                    "Messungs Kommentar: " + kommentar.getMstId(),
                    StatusCodes.NOT_ALLOWED));
            return null;
        }
        return kommentar;
    }

    private void createStatusProtokoll(
        String status,
        Messung messung,
        String mstId
    ) {
        for (int i = 1; i <= 3; i++) {
            if (status.substring(i - 1, i).equals("0")) {
                // no further status settings
                return;
            } else if (currentErrors.isEmpty() && currentWarnings.isEmpty()
                       || status.substring(i - 1, i).equals("7")
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
        Messung messung,
        String mstId
    ) {
        // validation check of new status entries
        int newKombi = 0;
        QueryBuilder<StatusKombi> builder =
            repository.queryBuilder(StatusKombi.class);
        builder.and("statusWert", statusWert);
        builder.and("statusStufe", statusStufe);
        List<StatusKombi> kombi =
            (List<StatusKombi>) repository.filterPlain(builder.getQuery());
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
        StatusProtokoll currentStatus = repository.getByIdPlain(
            StatusProtokoll.class, messung.getStatus());
        StatusKombi currentKombi = repository.getByIdPlain(
            StatusKombi.class, currentStatus.getStatusKombi());
        // check if erreichbar
        QueryBuilder<StatusErreichbar> errFilter =
            repository.queryBuilder(StatusErreichbar.class);
        errFilter.and("stufeId", statusStufe);
        errFilter.and("wertId", statusWert);
        errFilter.and("curStufe", currentKombi.getStatusStufe().getId());
        errFilter.and("curWert", currentKombi.getStatusWert().getId());
        List<StatusErreichbar> erreichbar =
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
            QueryBuilder<Messwert> builderMW =
                repository.queryBuilder(Messwert.class);
            builderMW.and("messungsId", messung.getId());
            Response messwertQry =
                repository.filter(builderMW.getQuery());
            @SuppressWarnings("unchecked")
            List<Messwert> messwerte = (List<Messwert>) messwertQry.getData();
            boolean hasValidMesswerte = false;
            if (!messwerte.isEmpty() && statusWert == 7) {
            for (Messwert messwert: messwerte) {

                boolean hasNoMesswert = false;

                if (messwert.getMesswert() == null
                     && messwert.getMesswertNwg() == null) {
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
        StatusProtokoll tmpStatus = new StatusProtokoll();
        tmpStatus = currentStatus;
        tmpStatus.setStatusKombi(newKombi);
        Violation statusViolation = statusValidator.validate(tmpStatus);

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
        MessStelle messStelle =
            repository.getByIdPlain(MessStelle.class, mstId);
        if ((statusStufe == 1
            && userInfo.getFunktionenForMst(mstId).contains(1))
            || (statusStufe == 2
                && userInfo.getNetzbetreiber().contains(
                    messStelle.getNetzbetreiberId())
                && userInfo.getFunktionenForNetzbetreiber(
                    messStelle.getNetzbetreiberId()).contains(2))
            || (statusStufe == 3
                && userInfo.getFunktionen().contains(3))
        ) {
            StatusProtokoll newStatus = new StatusProtokoll();
            newStatus.setDatum(new Timestamp(new Date().getTime()));
            newStatus.setMessungsId(messung.getId());
            newStatus.setMstId(mstId);
            newStatus.setStatusKombi(newKombi);
            repository.create(newStatus);
            if (newKombi == 0 || newKombi == 9 || newKombi == 13) {
                messung.setFertig(false);
            } else {
                messung.setFertig(true);
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

    private void createReiMesspunkt(LafRawData.Probe object, Probe probe) {

        QueryBuilder<Ortszuordnung> builder =
            repository.queryBuilder(Ortszuordnung.class);
        builder.and("probeId", probe.getId());
        List<Ortszuordnung> zuordnungen =
            repository.filterPlain(builder.getQuery());
        if (!zuordnungen.isEmpty()) {
            // Probe already has an ort.
            return;
        }

        List<Map<String, String>> uort = object.getUrsprungsOrte();
        if (uort.size() > 0
            && uort.get(0).containsKey("U_ORTS_ZUSATZCODE")
        ) {
            // WE HAVE A REI-MESSPUNKT!
            // Search for the ort in db
            Map<String, String> uo = uort.get(0);
            QueryBuilder<Ort> builder1 = repository.queryBuilder(Ort.class);
            builder1.and("ortId", uo.get("U_ORTS_ZUSATZCODE"));
            List<Ort> messpunkte =
                repository.filterPlain(builder1.getQuery());
            if (!messpunkte.isEmpty()) {
                Ortszuordnung ort = new Ortszuordnung();
                ort.setOrtszuordnungTyp("R");
                ort.setProbeId(probe.getId());
                ort.setOrtId(messpunkte.get(0).getId());
                if (uo.containsKey("U_ORTS_ZUSATZTEXT")) {
                    ort.setOrtszusatztext(uo.get("U_ORTS_ZUSATZTEXT"));
                }
                repository.create(ort);
                probe.setKtaGruppeId(messpunkte.get(0).getKtaGruppeId());
                repository.update(probe);
            } else if (uo.get("U_ORTS_ZUSATZCODE").length() == 4) {
                QueryBuilder<KtaGruppe> builderKta =
                    repository.queryBuilder(KtaGruppe.class);
                builderKta.and("ktaGruppe", uo.get("U_ORTS_ZUSATZCODE"));
                List<KtaGruppe> ktaGrp =
                    repository.filterPlain(builderKta.getQuery());
                if (!ktaGrp.isEmpty()) {
                    Ort o = null;
                    o = findOrCreateOrt(uort.get(0), "U_", probe);
                    if (o != null) {
                        o.setOrtTyp(1);
                        o.setKtaGruppeId(ktaGrp.get(0).getId());
                        repository.update(o);

                        Ortszuordnung ort = new Ortszuordnung();
                        ort.setOrtId(o.getId());
                        ort.setOrtszuordnungTyp("R");
                        ort.setProbeId(probe.getId());

                        repository.create(ort);

                        probe.setKtaGruppeId(ktaGrp.get(0).getId());
                        repository.update(probe);
                    } else {
                        ReportItem warn = new ReportItem();
                        warn.setCode(StatusCodes.VALUE_MISSING);
                        warn.setKey("Ort");
                        warn.setValue("Kein Messpunkt angelegt");
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
                ReportItem warn = new ReportItem();
                warn.setCode(StatusCodes.VALUE_NOT_MATCHING);
                warn.setKey("Ort");
                warn.setValue(uo.get("U_ORTS_ZUSATZCODE"));
                currentWarnings.add(warn);
            }
        } else {
            Ort o = null;
            if (uort.size() > 0) {
                o = findOrCreateOrt(uort.get(0), "U_", probe);
            }
            if (o == null) {
                o = findOrCreateOrt(object.getEntnahmeOrt(), "P_", probe);
            }
            if (o == null) {
                return;
            }
            o.setOrtTyp(3);
            repository.update(o);
            Ortszuordnung ort = new Ortszuordnung();
            ort.setOrtId(o.getId());
            ort.setOrtszuordnungTyp("R");
            ort.setProbeId(probe.getId());
            if (uort.size() > 0
                && uort.get(0).containsKey("U_ORTS_ZUSATZCODE")
            ) {
                Map<String, String> uo = uort.get(0);
                o.setOrtId(uo.get("U_ORTS_ZUSATZCODE"));
                if (uo.containsKey("U_ORTS_ZUSATZTEXT")) {
                    ort.setOrtszusatztext(uo.get("U_ORTS_ZUSATZTEXT"));
                }
            }
            repository.create(ort);
        }
        return;
    }

    private Ortszuordnung createUrsprungsOrt(
        Map<String, String> ursprungsOrt,
        Probe probe
    ) {
        if (ursprungsOrt.isEmpty()) {
            return null;
        }
        Ortszuordnung ort = new Ortszuordnung();
        ort.setOrtszuordnungTyp("U");
        ort.setProbeId(probe.getId());

        Ort o = findOrCreateOrt(ursprungsOrt, "U_", probe);
        if (o == null) {
            return null;
        }
        ort.setOrtId(o.getId());
        if (ursprungsOrt.containsKey("U_ORTS_ZUSATZTEXT")) {
            ort.setOrtszusatztext(ursprungsOrt.get("U_ORTS_ZUSATZTEXT"));
        }
        doDefaults(ort);
        doConverts(ort);
        doTransforms(ort);
        return ort;
    }

    private void createEntnahmeOrt(
        Map<String, String> entnahmeOrt,
        Probe probe
    ) {
        if (entnahmeOrt.isEmpty()) {
            return;
        }
        Ortszuordnung ort = new Ortszuordnung();
        ort.setOrtszuordnungTyp("E");
        ort.setProbeId(probe.getId());

        Ort o = findOrCreateOrt(entnahmeOrt, "P_", probe);
        if (o == null) {
            return;
        }
        ort.setOrtId(o.getId());
        if (entnahmeOrt.containsKey("P_ORTS_ZUSATZTEXT")) {
            ort.setOrtszusatztext(entnahmeOrt.get("P_ORTS_ZUSATZTEXT"));
        }
        doDefaults(ort);
        doConverts(ort);
        doTransforms(ort);
        merger.mergeEntnahmeOrt(probe.getId(), ort);
    }

    private Ort findOrCreateOrt(
        Map<String, String> attributes,
        String type,
        Probe probe
    ) {
        Ort o = new Ort();
        // If laf contains coordinates, find a ort with matching coordinates or
        // create one.
        if ((attributes.get(type + "KOORDINATEN_ART") != null
            || attributes.get(type + "KOORDINATEN_ART_S") != null)
            && !attributes.get(type + "KOORDINATEN_X").equals("")
            && attributes.get(type + "KOORDINATEN_X") != null
            && !attributes.get(type + "KOORDINATEN_X").equals("")
            && attributes.get(type + "KOORDINATEN_Y") != null
        ) {
            if (attributes.get(type + "KOORDINATEN_ART_S") != null) {
                o.setKdaId(Integer.valueOf(
                    attributes.get(type + "KOORDINATEN_ART_S")));
                KoordinatenArt koordinatenArt = repository.getByIdPlain(
                    KoordinatenArt.class, o.getKdaId());
                if (koordinatenArt == null) {
                    currentWarnings.add(
                        new ReportItem(
                            type + "KOORDINATEN_ART_S",
                            attributes.get(type + "KOORDINATEN_ART_S"),
                            StatusCodes.IMP_INVALID_VALUE));
                    o.setKdaId(null);
                }
            } else {
                QueryBuilder<KoordinatenArt> kdaBuilder =
                    repository.queryBuilder(KoordinatenArt.class);
                kdaBuilder.and(
                    "koordinatenart", attributes.get(type + "KOORDINATEN_ART"));
                List<KoordinatenArt> arten =
                    repository.filterPlain(
                        kdaBuilder.getQuery());
                if (arten == null || arten.isEmpty()) {
                    currentWarnings.add(
                        new ReportItem(
                            type + "KOORDINATEN_ART",
                            attributes.get(type + "KOORDINATEN_ART"),
                            StatusCodes.IMP_INVALID_VALUE));
                    o.setKdaId(null);
                } else {
                    o.setKdaId(arten.get(0).getId());
                }
            }
            o.setKoordXExtern(attributes.get(type + "KOORDINATEN_X"));
            o.setKoordYExtern(attributes.get(type + "KOORDINATEN_Y"));
        }
        // If laf contains gemeinde attributes, find a ort with matching gemId
        // or create one.
        if (attributes.get(type + "GEMEINDENAME") != null
            && !attributes.get(type + "GEMEINDENAME").equals("")
        ) {
            QueryBuilder<Verwaltungseinheit> builder =
                repository.queryBuilder(Verwaltungseinheit.class);
            builder.and("bezeichnung", attributes.get(type + "GEMEINDENAME"));
            List<Verwaltungseinheit> ves =
                repository.filterPlain(builder.getQuery());
            if (ves == null || ves.size() == 0) {
                currentWarnings.add(
                    new ReportItem(
                        "GEMEINDENAME",
                        attributes.get(type + "GEMEINDENAME"),
                        StatusCodes.IMP_INVALID_VALUE));
            } else {
                o.setGemId(ves.get(0).getId());
            }
        } else if (attributes.get(type + "GEMEINDESCHLUESSEL") != null
            && !attributes.get(type + "GEMEINDESCHLUESSEL").equals("")
        ) {
            o.setGemId(attributes.get(type + "GEMEINDESCHLUESSEL"));
            Verwaltungseinheit v =
                repository.getByIdPlain(
                    Verwaltungseinheit.class, o.getGemId());
            if (v == null) {
                currentWarnings.add(
                    new ReportItem(
                        type + "GEMEINDESCHLUESSEL", o.getGemId(),
                        StatusCodes.IMP_INVALID_VALUE));
                o.setGemId(null);
            }
        }
        String key = "";
        String hLand = "";
        String staatFilter = "";
        if (attributes.get(type + "HERKUNFTSLAND_S") != null
            && !attributes.get(type + "HERKUNFTSLAND_S").equals("")) {
            staatFilter = "id";
            key = "HERKUNFTSLAND_S";
            hLand = attributes.get(type + "HERKUNFTSLAND_S");
        } else if (attributes.get(type + "HERKUNFTSLAND_KURZ") != null
            && !attributes.get(type + "HERKUNFTSLAND_KURZ").equals("")
        ) {
            staatFilter = "staatKurz";
            key = "HERKUNFTSLAND_KURZ";
            hLand = attributes.get(type + "HERKUNFTSLAND_KURZ");
        } else if (attributes.get(type + "HERKUNFTSLAND_LANG") != null
            && !attributes.get(type + "HERKUNFTSLAND_LANG").equals("")) {
            staatFilter = "staat";
            key = "HERKUNFTSLAND_LANG";
            hLand = attributes.get(type + "HERKUNFTSLAND_LANG");
        }

        if (staatFilter.length() > 0) {
            QueryBuilder<Staat> builderStaat =
                repository.queryBuilder(Staat.class);
            builderStaat.and(staatFilter, hLand);
            List<Staat> staat =
                repository.filterPlain(builderStaat.getQuery());
            if (staat == null || staat.size() == 0) {
                currentWarnings.add(
                    new ReportItem(key, hLand, StatusCodes.IMP_INVALID_VALUE));
            } else if (staat.size() > 0) {
                o.setStaatId(staat.get(0).getId());
            }
        }
        if (attributes.containsKey(type + "HOEHE_NN")) {
            o.setHoeheUeberNn(Float.valueOf(attributes.get(type + "HOEHE_NN")));
        }
        if (attributes.containsKey(type + "ORTS_ZUSATZCODE")
            && !attributes.get(type + "ORTS_ZUSATZCODE").equals("")) {
            Ortszusatz zusatz = repository.getByIdPlain(
                Ortszusatz.class,
                attributes.get(type + "ORTS_ZUSATZCODE")
            );
            if (zusatz == null) {
                currentWarnings.add(
                    new ReportItem(
                        type + "ORTS_ZUSATZCODE",
                        attributes.get(type + "ORTS_ZUSATZCODE"),
                        StatusCodes.IMP_INVALID_VALUE));
            } else {
                o.setOzId(zusatz.getOzsId());
            }
        }

        // checkk if all attributes are empty
        if (o.getKdaId() == null
            && o.getGemId() == null
            && o.getStaatId() == null
            && o.getOzId() == null) {
            return null;
        }

        MessStelle mst = repository.getByIdPlain(
            MessStelle.class, probe.getMstId());
        o.setNetzbetreiberId(mst.getNetzbetreiberId());
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
                    // Add to warnings because Probe object might be imported
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

    private void logProbe(Probe probe) {
        logger.debug("%PROBE%");
        logger.debug("datenbasis: " + probe.getDatenbasisId());
        logger.debug("betriebsart: " + probe.getBaId());
        logger.debug("erzeuger: " + probe.getErzeugerId());
        logger.debug("hauptprobennummer: " + probe.getHauptprobenNr());
        logger.debug("externeprobeid: " + probe.getExterneProbeId());
        logger.debug("labor: " + probe.getLaborMstId());
        logger.debug("deskriptoren: " + probe.getMediaDesk());
        logger.debug("media: " + probe.getMedia());
        logger.debug("mittelung: " + probe.getMittelungsdauer());
        logger.debug("mpl: " + probe.getMplId());
        logger.debug("mpr: " + probe.getMprId());
        logger.debug("mst: " + probe.getMstId());
        logger.debug("pnbeginn: " + probe.getProbeentnahmeBeginn());
        logger.debug("pnende: " + probe.getProbeentnahmeEnde());
        logger.debug("probenart: " + probe.getProbenartId());
        logger.debug("probenehmer: " + probe.getProbeNehmerId());
        logger.debug("sbeginn: " + probe.getSolldatumBeginn());
        logger.debug("sende: " + probe.getSolldatumEnde());
        logger.debug("ursprungszeit: " + probe.getUrsprungszeit());
        logger.debug("test: " + probe.getTest());
        logger.debug("umw: " + probe.getUmwId());
    }

    private void addProbeAttribute(
        Entry<String, String> attribute,
        Probe probe,
        String netzbetreiberId
    ) {
        String key = attribute.getKey();
        String value = attribute.getValue();

        if ("DATENBASIS_S".equals(key)
            && !value.equals("")
            && probe.getDatenbasisId() == null
        ) {
            Datenbasis datenbasis = repository.getByIdPlain(
                Datenbasis.class,
                Integer.valueOf(value.toString())
            );
            if (datenbasis == null) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            Integer v = Integer.valueOf(value.toString());
            probe.setDatenbasisId(v);
        } else if ("DATENBASIS_S".equals(key)
            && probe.getDatenbasisId() != null) {
            currentWarnings.add(
                new ReportItem(
                    key, value.toString(), StatusCodes.IMP_DUPLICATE));
        }


        if ("DATENBASIS".equals(key)
            && !value.equals("")
            && probe.getDatenbasisId() == null
        ) {
            List<ImporterConfig> cfgs =
                getImporterConfigByAttributeUpper("DATENBASIS");
            String attr = value.toString();
            for (int i = 0; i < cfgs.size(); i++) {
                ImporterConfig cfg = cfgs.get(i);
                if (cfg != null
                    && cfg.getAction().equals("convert")
                    && cfg.getFromValue().equals(attr)
                ) {
                    attr = cfg.getToValue();
                }
                if (cfg != null && cfg.getAction().equals("transform")) {
                    char from = (char) Integer.parseInt(cfg.getFromValue(), 16);
                    char to = (char) Integer.parseInt(cfg.getToValue(), 16);
                    attr = attr.replaceAll(
                        "[" + String.valueOf(from) + "]", String.valueOf(to));
                }
            }
            QueryBuilder<Datenbasis> builder =
                repository.queryBuilder(Datenbasis.class);
            builder.and("datenbasis", attr);
            List<Datenbasis> datenbasis =
                (List<Datenbasis>) repository.filterPlain(builder.getQuery());
            if (datenbasis == null || datenbasis.isEmpty()) {
                currentErrors.add(
                    new ReportItem(key, attr, StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            Integer v = datenbasis.get(0).getId();
            probe.setDatenbasisId(v);
        } else if ("DATENBASIS".equals(key)
            && !value.equals("")
            && probe.getDatenbasisId() != null
        ) {
            currentWarnings.add(
                new ReportItem(
                    key, value.toString(), StatusCodes.IMP_DUPLICATE));
        }

        if ("PROBE_ID".equals(key)) {
            probe.setExterneProbeId(value);
        }

        if ("HAUPTPROBENNUMMER".equals(key)) {
            probe.setHauptprobenNr(value.toString());
        }

        if ("MPR_ID".equals(key)) {
            Integer v = Integer.valueOf(value.toString());
            probe.setMprId(v);
        }

        if ("MESSLABOR".equals(key) && !value.equals("")) {
            MessStelle mst = repository.getByIdPlain(
                MessStelle.class, value.toString());
            if (mst == null) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setLaborMstId(value.toString());
        }

        if ("MESSPROGRAMM_S".equals(key)
            && !value.equals("")
            && probe.getBaId() == null
        ) {
            QueryBuilder<MessprogrammTransfer> builder =
                repository.queryBuilder(MessprogrammTransfer.class);
            builder.and("messprogrammS", value);
            List<MessprogrammTransfer> transfer =
                (List<MessprogrammTransfer>) repository.filterPlain(
                    builder.getQuery());
            if (transfer == null || transfer.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setBaId(transfer.get(0).getBaId());
            if (probe.getDatenbasisId() == null) {
                probe.setDatenbasisId(transfer.get(0).getDatenbasisId());
            }
        }
        if ("MESSPROGRAMM_C".equals(key) && !value.equals("")) {
            QueryBuilder<MessprogrammTransfer> builder =
                repository.queryBuilder(MessprogrammTransfer.class);
            builder.and("messprogrammC", value);
            List<MessprogrammTransfer> transfer =
                (List<MessprogrammTransfer>) repository.filterPlain(
                    builder.getQuery());
            if (transfer == null || transfer.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setBaId(transfer.get(0).getBaId());
            if (probe.getDatenbasisId() == null) {
                probe.setDatenbasisId(transfer.get(0).getDatenbasisId());
            }
        }

        if ("ERZEUGER".equals(key) && !value.equals("")) {
            QueryBuilder<DatensatzErzeuger> builder =
                repository.queryBuilder(DatensatzErzeuger.class);
            builder.and("netzbetreiberId", netzbetreiberId);
            builder.and("mstId", probe.getMstId());
            builder.and("datensatzErzeugerId", value);
            List<DatensatzErzeuger> datensatzErzeuger =
                    (List<DatensatzErzeuger>) repository.filterPlain(
                            builder.getQuery());
            if (datensatzErzeuger == null || datensatzErzeuger.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setErzeugerId(datensatzErzeuger.get(0).getId());
        }

        if ("MESSPROGRAMM_LAND".equals(key) && !value.equals("")) {
            QueryBuilder<MessprogrammKategorie> builder =
                repository.queryBuilder(MessprogrammKategorie.class);
            builder.and("netzbetreiberId", netzbetreiberId);
            builder.and("code", value);
            List<MessprogrammKategorie> kategorie =
                    (List<MessprogrammKategorie>) repository.filterPlain(
                            builder.getQuery());
            if (kategorie == null || kategorie.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setMplId(kategorie.get(0).getId());
        }

        if ("PROBENAHMEINSTITUTION".equals(key) && !value.equals("")) {
            QueryBuilder<Probenehmer> builder =
                repository.queryBuilder(Probenehmer.class);
            builder.and("netzbetreiberId", netzbetreiberId);
            builder.and("prnId", value);
            List<Probenehmer> prn =
                    (List<Probenehmer>) repository.filterPlain(
                        builder.getQuery());
            if (prn == null || prn.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setProbeNehmerId(prn.get(0).getId());
        }

        if ("SOLL_DATUM_UHRZEIT_A".equals(key) && !value.equals("")) {
            probe.setSolldatumBeginn(getDate(value.toString()));
        }
        if ("SOLL_DATUM_UHRZEIT_E".equals(key) && !value.equals("")) {
            probe.setSolldatumEnde(getDate(value.toString()));
        }
        if ("PROBENAHME_DATUM_UHRZEIT_A".equals(key) && !value.equals("")) {
            probe.setProbeentnahmeBeginn(getDate(value.toString()));
        }
        if ("PROBENAHME_DATUM_UHRZEIT_E".equals(key) && !value.equals("")) {
            probe.setProbeentnahmeEnde(getDate(value.toString()));
        }
        if ("URSPRUNGS_DATUM_UHRZEIT".equals(key) && !value.equals("")) {
            probe.setUrsprungszeit(getDate(value.toString()));
        }

        if ("UMWELTBEREICH_S".equals(key)
            && probe.getUmwId() == null
            && !value.equals("")
        ) {
            Umwelt umw = repository.getByIdPlain(
                Umwelt.class, value.toString());
            if (umw == null) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setUmwId(value.toString());
        } else if ("UMWELTBEREICH_S".equals(key) && probe.getUmwId() != null) {
            currentWarnings.add(
                new ReportItem(
                    key, value.toString(), StatusCodes.IMP_DUPLICATE));
        }
        if ("UMWELTBEREICH_C".equals(key)
            && probe.getUmwId() == null
            && !value.equals("")
        ) {
            QueryBuilder<Umwelt> builder =
                repository.queryBuilder(Umwelt.class);
            int length = value.toString().length() > 80
                ? 80
                : value.toString().length();
            builder.and("umweltBereich", value.toString().substring(0, length));
            List<Umwelt> umwelt =
                (List<Umwelt>) repository.filterPlain(builder.getQuery());
            if (umwelt == null || umwelt.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setUmwId(umwelt.get(0).getId());
        } else if ("UMWELTBEREICH_C".equals(key) && probe.getUmwId() != null) {
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
            probe.setMediaDesk(StringUtils.join(tmp.toArray(), " "));
        }

        if ("TESTDATEN".equals(key)) {
            if (value.toString().equals("1")) {
                probe.setTest(true);
            } else if (value.toString().equals("0")) {
                probe.setTest(false);
            } else if (!value.toString().equals("")) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
            }
        }

        if ("REI_PROGRAMMPUNKTGRUPPE".equals(key) && !value.equals("")
            || "REI_PROGRAMMPUNKT".equals(key) && !value.equals("")) {
            QueryBuilder<ReiProgpunktGruppe> builder =
                repository.queryBuilder(ReiProgpunktGruppe.class);
            builder.and("reiProgPunktGruppe", value.toString());
            List<ReiProgpunktGruppe> list =
                repository.filterPlain(builder.getQuery());
            if (!list.isEmpty()) {
                probe.setReiProgpunktGrpId(list.get(0).getId());
            } else {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.VALUE_NOT_MATCHING));
            }
        }

        if ("MEDIUM".equals(key)) {
            probe.setMedia(value.toString());
        }

        if ("PROBENART".equals(key) && !value.equals("")) {
            List<ImporterConfig> cfgs =
                getImporterConfigByAttributeUpper("PROBENART");
            String attr = value.toString();
            for (int i = 0; i < cfgs.size(); i++) {
                ImporterConfig cfg = cfgs.get(i);
                if (cfg != null
                    && cfg.getAction().equals("convert")
                    && cfg.getFromValue().equals(attr)
                ) {
                    attr = cfg.getToValue();
                }
                if (cfg != null && cfg.getAction().equals("transform")) {
                    char from = (char) Integer.parseInt(cfg.getFromValue(), 16);
                    char to = (char) Integer.parseInt(cfg.getToValue(), 16);
                    attr = attr.replaceAll(
                        "[" + String.valueOf(from) + "]", String.valueOf(to));
                }
            }
            QueryBuilder<Probenart> builder =
                repository.queryBuilder(Probenart.class);
            builder.and("probenart", attr);
            List<Probenart> probenart =
                (List<Probenart>) repository.filterPlain(builder.getQuery());
            if (probenart == null || probenart.isEmpty()) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
                return;
            }
            probe.setProbenartId(probenart.get(0).getId());
        }
    }
    /**
     * Add an attribute to the given LMessung object.
     *
     * @param attribute The attributes to map
     * @param messung   The entity object.
     * @return The updated entity object.
     */
    public Messung addMessungAttribute(
        Entry<String, String> attribute,
        Messung messung
    ) {
        String key = attribute.getKey();
        String value = attribute.getValue();
        if ("MESSUNGS_ID".equals(key) && !value.equals("")) {
            messung.setExterneMessungsId(Integer.valueOf(value));
        }
        if ("NEBENPROBENNUMMER".equals(key) && !value.equals("")) {
            messung.setNebenprobenNr(value.toString());
        } else if ("MESS_DATUM_UHRZEIT".equals(key) && !value.equals("")) {
            messung.setMesszeitpunkt(getDate(value.toString()));
        } else if ("MESSZEIT_SEKUNDEN".equals(key) && !value.equals("")) {
            Integer i = Integer.valueOf(value.toString());
            messung.setMessdauer(i);
        } else if ("MESSMETHODE_S".equals(key) && !value.equals("")) {
            MessMethode mmt = repository.getByIdPlain(
                MessMethode.class, value.toString());
            if (mmt == null) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
            } else {
                messung.setMmtId(value.toString());
            }
        } else if ("MESSMETHODE_C".equals(key) && !value.equals("")) {
            QueryBuilder<MessMethode> builder =
                repository.queryBuilder(MessMethode.class);
            builder.and("messmethode", value.toString());
            List<MessMethode> mm =
                (List<MessMethode>) repository.filterPlain(builder.getQuery());
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
                messung.setFertig(true);
            } else if (value.toString().equals("0")) {
                messung.setFertig(false);
            } else if (!value.toString().equals("")) {
                currentWarnings.add(
                    new ReportItem(
                        key, value.toString(), StatusCodes.IMP_INVALID_VALUE));
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
    }

    /**
     * @return the config
     */
    public List<ImporterConfig> getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(List<ImporterConfig> config) {
        this.config = config;
    }
}
