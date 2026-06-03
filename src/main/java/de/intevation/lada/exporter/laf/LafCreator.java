/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter.laf;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.master.DatasetCreator;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.MeasUnit;
import de.intevation.lada.model.master.Measd;
import de.intevation.lada.model.master.MpgCateg;
import de.intevation.lada.model.master.MunicDiv;
import de.intevation.lada.model.master.ReiAgGr;
import de.intevation.lada.model.master.SampleMeth;
import de.intevation.lada.model.master.SampleSpecif;
import de.intevation.lada.model.master.Sampler;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


/**
 * This creator produces a LAF conform String containing all information about
 * a single {@link Sample} object including subobjects like
 * {@link Measm}, {@link MeasVal}, {@link CommSample}...
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class LafCreator implements Closeable {

    private static final int MP6 = 6;
    private static final int BAID3 = 3;
    private static final int MP5 = 5;
    private static final int MP4 = 4;
    private static final int DATENBASIS4 = 4;
    // Some format strings corresponding to LAF notation
    private static final String KEY_FORMAT = "%-30s";
    private static final String DEFAULT_FORMAT = "%s";
    private static final String CN = "\"%s\""; // cn, mcn, scn

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMdd HHmm");

    private static final int MEAS_VAL_BUILDER_CAPACITY = 30;

    private Authorization authorization;

    private Repository repository;

    private Writer sink;

    private boolean closed = true;

    public LafCreator(
        Authorization authorization,
        Repository repository,
        Writer sink
    ) throws IOException {
        this.authorization = authorization;
        this.repository = repository;
        this.sink = sink;

        this.closed = false;
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
        sink.write("%ENDE%");
        sink.close();
    }

    /**
     * Write LAF8 string representation of a {@link Sample} object
     * with given {@link Measm} objects to {@code sink}.
     * @param aProbe the requested {@link Sample} object
     * @param measms the {@link Measm} objects to be included in the LAF8
     * representation of the {@link Sample}
     * @throws IOException if underlying {@link Writer} throws
     * {@link IOException}
     */
    public void sampleToLAF(
        Sample aProbe, List<Measm> measms
    ) throws IOException {
        if (this.closed) {
            throw new IllegalStateException(
                this.getClass().getName() + " closed");
        }

        try {
            sink.write("%PROBE%\n");
            lafLine("UEBERTRAGUNGSFORMAT", "7", CN);
            lafLine("VERSION", "0084", CN);
            writeAttributes(aProbe, measms);
        } catch (IOException e) {
            this.close();
            throw e;
        }
    }

    private void writeAttributes(
        Sample probe, List<Measm> measms
    ) throws IOException {
        lafLine("PROBE_ID", probe.getExtId(), CN);
        lafLine("DATENBASIS_S", probe.getRegulationId(), "%02d");
        lafLine("NETZKENNUNG", repository.getById(
                MeasFacil.class, probe.getMeasFacilId()).getNetworkId(), CN);
        lafLine("MESSSTELLE", probe.getMeasFacilId(), CN);
        lafLine("MESSLABOR", probe.getApprLabId(), CN);
        if (probe.getMainSampleId() != null) {
            lafLine("HAUPTPROBENNUMMER", probe.getMainSampleId(), CN);
        }
        if (probe.getRegulationId() == DATENBASIS4) {
            if (probe.getOprModeId() == 1) {
                lafLine("MESSPROGRAMM_S", MP4, CN);
            } else if (probe.getOprModeId() == 2) {
                lafLine("MESSPROGRAMM_S", MP5, CN);
            } else if (probe.getOprModeId() == BAID3) {
                lafLine("MESSPROGRAMM_S", MP6, CN);
            } else {
                lafLine("MESSPROGRAMM_S", probe.getOprModeId(), CN);
            }
        } else {
            if (probe.getOprModeId() > BAID3) {
                lafLine("MESSPROGRAMM_S", probe.getOprModeId(), CN);
            } else if (probe.getOprModeId() == BAID3) {
                lafLine("MESSPROGRAMM_S", 2, CN);
            } else {
                lafLine("MESSPROGRAMM_S", probe.getOprModeId(), CN);
            }
        }
        lafLine("PROBENART", repository.getById(
                SampleMeth.class, probe.getSampleMethId()).getExtId(), CN);
        lafLine("ZEITBASIS_S", "2");
        if (probe.getSchedStartDate() != null) {
            lafLine("SOLL_DATUM_UHRZEIT_A",
                toUTCString(probe.getSchedStartDate()));
        }
        if (probe.getSchedEndDate() != null) {
            lafLine("SOLL_DATUM_UHRZEIT_E",
                toUTCString(probe.getSchedEndDate()));
        }
        if (probe.getSampleStartDate() != null) {
            lafLine("PROBENAHME_DATUM_UHRZEIT_A",
                toUTCString(probe.getSampleStartDate()));
        }
        if (probe.getSampleEndDate() != null) {
            lafLine("PROBENAHME_DATUM_UHRZEIT_E",
                toUTCString(probe.getSampleEndDate()));
        }
        if (probe.getOrigDate() != null) {
            lafLine("URSPRUNGS_DATUM_UHRZEIT",
                toUTCString(probe.getOrigDate()));
        }
        if (probe.getEnvMediumId() != null) {
            lafLine("UMWELTBEREICH_S", probe.getEnvMediumId(), CN);
        }
        if (probe.getEnvDescripDisplay() != null) {
            lafLine("DESKRIPTOREN",
                probe.getEnvDescripDisplay().replaceAll(" ", "")
                    .substring(2), CN);
        }
        lafLine("TESTDATEN", probe.getIsTest() ? "1" : "0");
        if (probe.getDatasetCreatorId() != null) {
            DatasetCreator erz = repository.getById(
                DatasetCreator.class, probe.getDatasetCreatorId());
            lafLine("ERZEUGER", erz.getExtId(), CN);
        }
        if (probe.getMpgCategId() != null) {
            MpgCateg mpkat = repository.getById(
                MpgCateg.class, probe.getMpgCategId());
            lafLine("MESSPROGRAMM_LAND", mpkat.getExtId(), CN);
        }
        if (probe.getSamplerId() != null) {
            Sampler prn = repository.getById(
                Sampler.class, probe.getSamplerId());
            lafLine("PROBENAHMEINSTITUTION", prn.getExtId(), CN);
        }
        if (probe.getReiAgGrId() != null) {
            ReiAgGr rpg = repository.getById(
                ReiAgGr.class,
                probe.getReiAgGrId());
            lafLine("REI_PROGRAMMPUNKTGRUPPE", rpg.getName(), CN);
        }
        for (SampleSpecifMeasVal zw : probe.getSampleSpecifMeasVals()) {
            writeZusatzwert(zw);
        }
        for (CommSample kp : probe.getCommSamples()) {
            writeKommentar(kp);
        }
        writeOrt(probe);
        writeMessung(probe, measms);
    }

    private void writeZusatzwert(SampleSpecifMeasVal zw) throws IOException {
        SampleSpecif zusatz = repository.getById(
            SampleSpecif.class, zw.getSampleSpecifId());

        StringBuilder value = new StringBuilder("\"")
            .append(zusatz.getId())
            .append("\" ");
        if (zw.getSmallerThan() != null) {
            value.append(zw.getSmallerThan());
        }
        value.append(zw.getMeasVal())
            .append(" ")
            .append(zusatz.getMeasUnitId() == null
                ? "\"\""
                : zusatz.getMeasUnitId());
        if (zw.getError() != null) {
            value.append(" ")
                .append(zw.getError());
        }
        lafLine("PZB_S", value);
    }

    private void writeOrt(Sample probe) throws IOException {
        List<Geolocat> orte = probe.getGeolocats();

        for (Geolocat o : orte) {
            if ("E".equals(o.getTypeRegulation())
                || "R".equals(o.getTypeRegulation())) {
                writeOrtData(o, "P_");
            }
        }
        for (Geolocat o : orte) {
            if ("U".equals(o.getTypeRegulation())
                || "R".equals(o.getTypeRegulation())) {
                sink.write("%URSPRUNGSORT%\n");
                writeOrtData(o, "U_");
            }
        }
    }

    private void writeOrtData(Geolocat o, String typePrefix)
        throws IOException {
        if (o.getAddSiteText() != null) {
            lafLine(typePrefix + "ORTS_ZUSATZTEXT",
                o.getAddSiteText(), CN);
        }

        Site sOrt = o.getSite();

        if (sOrt.getStateId() != null) {
            lafLine(typePrefix + "HERKUNFTSLAND_S", sOrt.getStateId(), "%08d");
        }

        if (sOrt.getAdminUnitId() != null
            && sOrt.getAdminUnitId().length() > 0
        ) {
            lafLine(typePrefix + "GEMEINDESCHLUESSEL",
                sOrt.getAdminUnitId());
        }

        if (sOrt.getMunicDivId() != null) {
            MunicDiv gu = repository.getById(
                MunicDiv.class, sOrt.getMunicDivId());
            lafLine(typePrefix + "ORTS_ZUSATZKENNZAHL",
                gu.getDivCode(), CN);
        }

        String koord = String.format("%02d", sOrt.getSpatRefSysId())
            + " \"" + sOrt.getCoordXExt() + "\" \""
            + sOrt.getCoordYExt() + "\"";
        lafLine(typePrefix + "KOORDINATEN_S", koord);

        if ("P_".equals(typePrefix) && o.getPoiId() != null) {
            lafLine(
                typePrefix + "ORTS_ZUSATZCODE",
                o.getPoiId(),
                CN);
        } else if ("U_".equals(typePrefix)
            && "R".equals(o.getTypeRegulation())
        ) {
            Sample s = o.getSample();
            if (s.getRegulationId() == DATENBASIS4) {
                lafLine(
                    typePrefix + "ORTS_ZUSATZCODE",
                    sOrt.getExtId(),
                    CN);
            } else {
                if (o.getPoiId() != null) {
                    lafLine(
                        typePrefix + "ORTS_ZUSATZCODE",
                        o.getPoiId(),
                        CN);
                }
            }
        } else if ("U_".equals(typePrefix) && o.getPoiId() != null) {
            lafLine(
                typePrefix + "ORTS_ZUSATZCODE",
                o.getPoiId(),
                CN);
        }
    }

    private void writeKommentar(CommSample kp) throws IOException {
        String value = "\"" + kp.getMeasFacilId()
            + "\" "
            + toUTCString(kp.getDate()) + " "
            + "\"" + kp.getText() + "\"";
        lafLine("PROBENKOMMENTAR", value);
    }

    private void writeMessung(Sample probe, List<Measm> mess)
        throws IOException {
        for (Measm m : mess) {
            sink.write("%MESSUNG%\n");
            lafLine("MESSUNGS_ID", m.getExtId());
            if (m.getMinSampleId() != null) {
                lafLine("NEBENPROBENNUMMER", m.getMinSampleId(), CN);
            }
            if (m.getMeasmStartDate() != null) {
                lafLine(
                    "MESS_DATUM_UHRZEIT", toUTCString(m.getMeasmStartDate()));
            }
            if (m.getMeasPd() != null) {
                lafLine("MESSZEIT_SEKUNDEN", m.getMeasPd());
            }
            lafLine("MESSMETHODE_S", m.getMmtId(), CN);
            lafLine(
                "ERFASSUNG_ABGESCHLOSSEN",
                (m.getIsCompleted() ? "1" : "0"));
            lafLine("BEARBEITUNGSSTATUS", writeStatus(m));
            if (authorization.isAuthorized(m, RequestMethod.GET)) {
                for (MeasVal mw : m.getMeasVals()) {
                    writeMesswert(mw);
                }
            }
            for (CommMeasm mk: m.getCommMeasms()) {
                writeKommentar(mk);
            }
        }
    }

    private String writeStatus(Measm messung) {
        Integer[] status = {0, 0, 0};
        // Status protocoll in descending order (newest first)
        List<StatusProt> statusHistory =
            messung.getStatusProts().reversed();
        Integer stufe = 4;
        Integer st, w;
        for (StatusProt statusEntry : statusHistory) {
            StatusMp stKombi = repository.getById(
                StatusMp.class,
                statusEntry.getStatusMpId()
            );
            st = stKombi.getStatusLev().getId();
            w = stKombi.getStatusVal().getId();
            if (st < stufe) {
                stufe = st;
                status[stufe - 1] = w;
            }
            if (stufe == 1) {
                break;
            }
        }
        if (status[2] != 0 && status[1] == 0) {
            status[1] = status[2];
        }
        if (status[1] != 0 && status[0] == 0) {
            status[0] = status[1];
        }
        return "" + status[0] + status[1] + status[2] + "0";
    }

    private void writeKommentar(CommMeasm mk) throws IOException {
        String value = "\"" + mk.getMeasFacilId() + "\" "
            + toUTCString(mk.getDate()) + " "
            + "\"" + mk.getText() + "\"";
        lafLine("KOMMENTAR", value);
    }

    private void writeMesswert(MeasVal mw) throws IOException {
        StringBuilder tag = new StringBuilder("MESSWERT");
        StringBuilder value = new StringBuilder(MEAS_VAL_BUILDER_CAPACITY)
            .append("\"")
            .append(repository.getById(
                    Measd.class, mw.getMeasdId()).getName())
            .append("\" ")
            .append(mw.getLessThanLOD() == null ? " " : mw.getLessThanLOD())
            .append(mw.getLessThanLOD() == null
                ? mw.getMeasVal() : mw.getDetectLim())
            .append(" \"")
            .append(repository.getById(
                    MeasUnit.class, mw.getMeasUnitId()).getUnitSymbol())
            .append("\" ")
            .append(mw.getError() == null ? "0.0" : mw.getError());
        if (mw.getIsThreshold() == null
            || !mw.getIsThreshold()
        ) {
            if (mw.getDetectLim() != null) {
                tag.append("_NWG");
                value.append(" ")
                    .append(mw.getDetectLim());
            }
        } else {
            tag.append("_NWG_G");
            value.append(" ")
                .append(mw.getDetectLim() == null
                    ? "0.0" : mw.getDetectLim())
                .append(" ")
                .append(mw.getIsThreshold() == null || !mw.getIsThreshold()
                    ? " N" : " J");
        }
        lafLine(tag.toString(), value);
    }

    private void lafLine(String key, Object value) throws IOException {
        lafLine(key, value, DEFAULT_FORMAT);
    }

    private void lafLine(String key, Object value, String format)
        throws IOException {
        sink.write(String.format(KEY_FORMAT, key)
            + String.format(format, value)
            + "\n");
    }

    private String toUTCString(Date timestamp) {
        return DATE_FORMATTER.format(
            timestamp.toInstant().atZone(ZoneOffset.UTC));
    }
}
