/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter.laf;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
//import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import de.intevation.lada.exporter.Creator;
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
import de.intevation.lada.util.auth.HeaderAuthorization;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

/**
 * This creator produces a LAF conform String containing all information about
 * a single {@link LProbe} object including subobjects like
 * {@link LMessung}, {@link LMesswert}, {@link LKommentarP}...
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Named("lafcreator")
public class LafCreator
implements Creator {

    private static final int LAND_RESET = 15;
    private static final int MST_RESET = 14;
    private static final int LAND_QUERY = 9;
    private static final int LAND_UNPLAUS = 8;
    private static final int LAND_NOT_REPR = 7;
    private static final int LAND_PLAUS = 6;
    private static final int MST_NOT_DELIV = 5;
    private static final int MST_NOT_PLAUS = 4;
    private static final int MST_NOT_REPR = 3;
    private static final int MST_PLAUS = 2;
    private static final int NOT_SET = 1;
    private static final int MP6 = 6;
    private static final int BAID3 = 3;
    private static final int MP5 = 5;
    private static final int MP4 = 4;
    private static final int DATENBASIS4 = 4;
    // Some format strings corresponding to LAF notation
    private static final String KEY_FORMAT = "%-30s";
    private static final String DEFAULT_FORMAT = "%s";
    private static final String CN = "\"%s\""; // cn, mcn, scn

    private HeaderAuthorization authorization;

    /**
     * The repository used to read data.
     */
    @Inject
    private Repository repository;

    private UserInfo userInfo;

    /**
     * Create the LAF conform String.
     *
     * @param probeId   The {@link LProbe} id.
     */
    @Override
    public String createProbe(String probeId) {
        String lafProbe = "%PROBE%\n";
        lafProbe += lafLine("UEBERTRAGUNGSFORMAT", "7", CN);
        lafProbe += lafLine("VERSION", "0084", CN);
        lafProbe += probeToLAF(probeId, new ArrayList<Integer>());
        return lafProbe;
    }

    /**
     * Create the LAF conform String.
     *
     * @param probeId   The {@link LProbe} id.
     */
    @Override
    public String createMessung(String probeId, List<Integer> messungen) {
        String lafProbe = "%PROBE%\n";
        lafProbe += lafLine("UEBERTRAGUNGSFORMAT", "7", CN);
        lafProbe += lafLine("VERSION", "0084", CN);
        lafProbe += probeToLAF(probeId, messungen);
        return lafProbe;
    }

    /**
     * Find the {@link LProbe} object and produce the LAF conform string.
     * @param probeId The {@link LProbe} id.
     * @return LAF conform string.
     */
    private String probeToLAF(String probeId, List<Integer> messungen) {
        Response found = repository.getById(
            Sample.class, Integer.valueOf(probeId));
        if (found.getData() == null) {
            return null;
        }
        Sample aProbe = (Sample) found.getData();
        String lafProbe = writeAttributes(aProbe, messungen);
        return lafProbe;
    }

    /**
     * Write the attributes and subobjects.
     *
     * @param probe The {@link LProbeInfo} object.
     * @return LAF conform string.
     */
    @SuppressWarnings("unchecked")
    private String writeAttributes(Sample probe, List<Integer> messungen) {
        QueryBuilder<CommSample> kommBuilder =
            repository.queryBuilder(CommSample.class);
        kommBuilder.and("sampleId", probe.getId());
        Response kommentar =
            repository.filter(kommBuilder.getQuery());
        List<CommSample> kommentare = (List<CommSample>) kommentar.getData();

        String probenart = null;
        if (probe.getSampleMethId() != null) {
            QueryBuilder<SampleMeth> builder =
                repository.queryBuilder(SampleMeth.class);
            builder.and("id", probe.getSampleMethId());
            List<SampleMeth> probenarten =
                (List<SampleMeth>) repository.filter(
                    builder.getQuery()
                ).getData();
            probenart = probenarten.get(0).getExtId();
        }

        MeasFacil messstelle =
            repository.getByIdPlain(
                MeasFacil.class, probe.getMeasFacilId());

        QueryBuilder<SampleSpecifMeasVal> zusatzBuilder =
            repository.queryBuilder(SampleSpecifMeasVal.class);
        zusatzBuilder.and("sampleId", probe.getId());
        Response zusatz =
            repository.filter(zusatzBuilder.getQuery());
        List<SampleSpecifMeasVal> zusatzwerte = (List<SampleSpecifMeasVal>) zusatz.getData();

        String laf = "";
        laf += lafLine("PROBE_ID", probe.getExtId(), CN);
        laf += probe.getRegulationId() == null
            ? ""
            : lafLine("DATENBASIS_S",
                String.format("%02d", probe.getRegulationId()));
        laf += messstelle == null
            ? ""
            : lafLine("NETZKENNUNG", messstelle.getNetworkId(), CN);
        laf += probe.getMeasFacilId() == null
            ? ""
            : lafLine("MESSSTELLE", probe.getMeasFacilId(), CN);
        laf += probe.getApprLabId() == null
            ? ""
            : lafLine("MESSLABOR", probe.getApprLabId(), CN);
        laf += probe.getMainSampleId() == null
            ? ""
            : lafLine("HAUPTPROBENNUMMER", probe.getMainSampleId(), CN);
        if (probe.getOprModeId() != null && probe.getRegulationId() != null) {
            if (probe.getRegulationId() == DATENBASIS4) {
                if (probe.getOprModeId() == 1) {
                    laf += lafLine("MESSPROGRAMM_S", MP4, CN);
                } else if (probe.getOprModeId() == 2) {
                    laf += lafLine("MESSPROGRAMM_S", MP5, CN);
                } else if (probe.getOprModeId() == BAID3) {
                    laf += lafLine("MESSPROGRAMM_S", MP6, CN);
                } else {
                    laf += lafLine("MESSPROGRAMM_S",
                        "\"" + (char) probe.getOprModeId().intValue() + "\"");
                }
            } else {
                if (probe.getOprModeId() > BAID3) {
                    laf +=
                        lafLine("MESSPROGRAMM_S", "\""
                            + (char) probe.getOprModeId().intValue() + "\"");
                } else if (probe.getOprModeId() == BAID3) {
                    laf += lafLine("MESSPROGRAMM_S", 2, CN);
                } else {
                    laf += lafLine("MESSPROGRAMM_S", probe.getOprModeId(), CN);
                }
            }
        }
        laf += probe.getSampleMethId() == null
            ? ""
            : lafLine("PROBENART", probenart, CN);
        laf += lafLine("ZEITBASIS_S", "2");
        laf += probe.getSchedStartDate() == null
            ? ""
            : lafLine("SOLL_DATUM_UHRZEIT_A",
                toUTCString(probe.getSchedStartDate()));
        laf += probe.getSchedEndDate() == null
            ? ""
            : lafLine("SOLL_DATUM_UHRZEIT_E",
                toUTCString(probe.getSchedEndDate()));
        laf += probe.getSampleStartDate() == null
            ? ""
            : lafLine("PROBENAHME_DATUM_UHRZEIT_A",
                toUTCString(probe.getSampleStartDate()));
        laf += probe.getSampleEndDate() == null
            ? ""
            : lafLine("PROBENAHME_DATUM_UHRZEIT_E",
                toUTCString(probe.getSampleEndDate()));
        laf += probe.getOrigDate() == null
            ? ""
            : lafLine("URSPRUNGS_DATUM_UHRZEIT",
                toUTCString(probe.getOrigDate()));
        laf += probe.getEnvMediumId() == null
            ? ""
            : lafLine("UMWELTBEREICH_S", probe.getEnvMediumId(), CN);
        laf += probe.getEnvDescripDisplay() == null
            ? ""
            : lafLine("DESKRIPTOREN",
                probe.getEnvDescripDisplay().replaceAll(" ", "")
                    .substring(2), CN);
        laf += probe.getIsTest()
            ? lafLine("TESTDATEN", "1")
            : lafLine("TESTDATEN", "0");
        if (probe.getDatasetCreatorId() != null) {
            DatasetCreator erz = repository.getByIdPlain(
                DatasetCreator.class, probe.getDatasetCreatorId());
            laf += lafLine("ERZEUGER", erz.getExtId(), CN);
        }
        if (probe.getMpgCategId() != null) {
            MpgCateg mpkat = repository.getByIdPlain(
                MpgCateg.class, probe.getMpgCategId());
            laf += lafLine("MESSPROGRAMM_LAND", mpkat.getExtId(), CN);
        }
        if (probe.getSamplerId() != null) {
            Sampler prn = repository.getByIdPlain(
                Sampler.class, probe.getSamplerId());
            laf += lafLine("PROBENAHMEINSTITUTION", prn.getExtId(), CN);
        }
        if (probe.getReiAgGrId() != null) {
            ReiAgGr rpg = repository.getByIdPlain(
                ReiAgGr.class,
                probe.getReiAgGrId());
            laf += lafLine(
                "REI_PROGRAMMPUNKTGRUPPE",
                rpg.getName(), CN);
        }
        for (SampleSpecifMeasVal zw : zusatzwerte) {
            laf += writeZusatzwert(zw);
        }
        for (CommSample kp : kommentare) {
            laf += writeKommentar(kp);
        }
        laf += writeOrt(probe);
        laf += writeMessung(probe, messungen);
        return laf;
    }

    /**
     * Write {@link LZusatzWert} attributes.
     *
     * @param zw    The {@link LZusatzWert}.
     * @return Single LAF line.
     */
    @SuppressWarnings("unchecked")
    private String writeZusatzwert(SampleSpecifMeasVal zw) {
        QueryBuilder<SampleSpecif> builder =
            repository.queryBuilder(SampleSpecif.class);
        builder.and("id", zw.getSampleSpecifId());
        List<SampleSpecif> zusatz =
            (List<SampleSpecif>) repository.filter(
                builder.getQuery()
            ).getData();

        String value = "\"" + zusatz.get(0).getId() + "\"";
        value += ((zw.getSmallerThan() == null)
            ? " "
            : " " + zw.getSmallerThan());
        value += zw.getMeasVal();
        value += " " + ((zusatz.get(0).getMeasUnitId() == null)
            ? "\"\"" : zusatz.get(0).getMeasUnitId());
        value += " " + ((zw.getError() == null) ? "" : zw.getError());
        return lafLine("PZB_S", value);
    }

    /**
     * Write {@link LOrt} attributes.
     *
     * @param probe The {@link LProbeInfo} object.
     * @return LAF conform string
     */
    @SuppressWarnings("unchecked")
    private String writeOrt(Sample probe) {
        QueryBuilder<Geolocat> builder =
            repository.queryBuilder(Geolocat.class);
        builder.and("sampleId", probe.getId());
        Response objects = repository.filter(builder.getQuery());
        List<Geolocat> orte =
            (List<Geolocat>) objects.getData();

        String laf = "";
        for (Geolocat o : orte) {
            if ("E".equals(o.getTypeRegulation())
                || "R".equals(o.getTypeRegulation())) {
                laf += writeOrtData(o, "P_");
            }
        }
        for (Geolocat o : orte) {
            if ("U".equals(o.getTypeRegulation())
                || "R".equals(o.getTypeRegulation())) {
                laf += "%URSPRUNGSORT%\n";
                laf += writeOrtData(o, "U_");
            }
        }
        return laf;
    }

    /**
     * Write {@link LOrt} attributes.
     *
     * @param Geolocat
     * @return LAF conform string
     */
    @SuppressWarnings("unchecked")
    private String writeOrtData(Geolocat o, String typePrefix) {
        String laf = "";
        if (o.getAddSiteText() != null
            && o.getAddSiteText().length() > 0
        ) {
            laf += lafLine(typePrefix + "ORTS_ZUSATZTEXT",
                o.getAddSiteText(), CN);
        }
        QueryBuilder<Site> oBuilder = repository.queryBuilder(Site.class);
        oBuilder.and("id", o.getSiteId());
        List<Site> sOrte =
            (List<Site>) repository.filter(
                oBuilder.getQuery()
            ).getData();

        if (sOrte.get(0).getStateId() != null) {
            laf += lafLine(typePrefix + "HERKUNFTSLAND_S",
                String.format("%08d", sOrte.get(0).getStateId()));
        }

        if (sOrte.get(0).getAdminUnitId() != null
            && sOrte.get(0).getAdminUnitId().length() > 0
        ) {
            laf += lafLine(typePrefix + "GEMEINDESCHLUESSEL",
                sOrte.get(0).getAdminUnitId());
        }

        if (sOrte.get(0).getMunicDivId() != null) {
            MunicDiv gu = repository.getByIdPlain(
                MunicDiv.class, sOrte.get(0).getMunicDivId());
            laf += lafLine(typePrefix + "ORTS_ZUSATZKENNZAHL",
                gu.getSiteId(), CN);
        }

        String koord = String.format("%02d", sOrte.get(0).getSpatRefSysId());
        koord += " \"";
        koord += sOrte.get(0).getCoordXExt() + "\" \"";
        koord += sOrte.get(0).getCoordYExt() + "\"";
        laf += lafLine(typePrefix + "KOORDINATEN_S", koord);

        if ("P_".equals(typePrefix) && o.getPoiId() != null) {
            laf += lafLine(
                typePrefix + "ORTS_ZUSATZCODE",
                o.getPoiId(),
                CN);
        } else if ("U_".equals(typePrefix)
            && "R".equals(o.getTypeRegulation())
        ) {
            laf += lafLine(
                typePrefix + "ORTS_ZUSATZCODE",
                sOrte.get(0).getExtId(),
                CN);
        } else if ("U_".equals(typePrefix)
            && o.getPoiId() != null
        ) {
            laf += lafLine(
                typePrefix + "ORTS_ZUSATZCODE",
                o.getPoiId(),
                CN);
        }
//        if (sOrte.get(0).getHoeheUeberNn() != null) {
//            laf += lafLine(typePrefix + "HOEHE_NN",
//                String.format("%f", sOrte.get(0).getHoeheUeberNn()));
//        }
        return laf;
    }

    /**
     * Write {@link LKommentarP} attributes.
     *
     * @param kp    The {@link LKommentarP} object.
     * @return Single LAF line.
     */
    private String writeKommentar(CommSample kp) {
        String value = "\"" + kp.getMeasFacilId()
            + "\" "
            + toUTCString(kp.getDate()) + " "
            + "\"" + kp.getText() + "\"";
        return lafLine("PROBENKOMMENTAR", value);
    }

    /**
     * Write {@link LMessung} attributes.
     *
     * @param probe The {@link LProbeInfo} object.
     * @return LAF conform string.
     */
    @SuppressWarnings("unchecked")
    private String writeMessung(Sample probe, List<Integer> messungen) {
        QueryBuilder<Measm> builder = repository.queryBuilder(Measm.class);
        if (messungen.isEmpty()) {
            // Get all messungen
            builder.and("sampleId", probe.getId());
        } else {
            builder.andIn("id", messungen);
        }
        List<Measm> mess = repository.filterPlain(
            builder.getQuery());

        String laf = "";
        for (Measm m : mess) {
            laf += "%MESSUNG%\n";
            QueryBuilder<MeasVal> wertBuilder =
                repository.queryBuilder(MeasVal.class);
            wertBuilder.and("measmId", m.getId());
            Response messw =
                repository.filter(wertBuilder.getQuery());
            List<MeasVal> werte = (List<MeasVal>) messw.getData();
            QueryBuilder<CommMeasm> kommBuilder =
                repository.queryBuilder(CommMeasm.class);
            kommBuilder.and("measmId", m.getId());
            Response kommentar =
                repository.filter(kommBuilder.getQuery());
            List<CommMeasm> kommentare =
                (List<CommMeasm>) kommentar.getData();
            laf += lafLine("MESSUNGS_ID", m.getExtId().toString());
            laf += m.getMinSampleId() == null
                ? ""
                : lafLine("NEBENPROBENNUMMER", m.getMinSampleId(), CN);
            laf += m.getMeasmStartDate() == null
                ? ""
                : lafLine(
                    "MESS_DATUM_UHRZEIT", toUTCString(m.getMeasmStartDate()));
            laf += m.getMeasPd() == null
                ? ""
                : lafLine("MESSZEIT_SEKUNDEN", m.getMeasPd().toString());
            laf += m.getMmtId() == null
                ? ""
                : lafLine("MESSMETHODE_S", m.getMmtId(), CN);
            laf += lafLine(
                "ERFASSUNG_ABGESCHLOSSEN",
                (m.getIsCompleted() ? "1" : "0"));
            laf += lafLine("BEARBEITUNGSSTATUS", writeStatus(m));
            if (this.userInfo != null
                // TODO: GET is correct RequestMethod here?
                && authorization.isAuthorized(
                    m, RequestMethod.GET, Measm.class)
            ) {
                for (MeasVal mw : werte) {
                    laf += writeMesswert(mw);
                }
            }
            for (CommMeasm mk: kommentare) {
                laf += writeKommentar(mk);
            }
        }
        return laf;
    }

    /**
     * Write out the status protocol as string with 4 character:
     *  1. status level mst
     *  2. status level land
     *  3. status level lst
     *  4. set to 0 (unused)
     *
     * @param messung the messung containing the status
     * @return 4 character string
     */
    private String writeStatus(Measm messung) {
        Integer[] status = {0, 0, 0};
        QueryBuilder<StatusProt> builder =
        repository.queryBuilder(StatusProt.class);
            builder.and("measmId", messung.getId());
        builder.orderBy("id", false);
        List<StatusProt> statusHistory = repository.filterPlain(
                builder.getQuery());
        Integer stufe = 4;
        Integer st, w;
        for (StatusProt statusEntry : statusHistory) {
            StatusMp stKombi = repository.getByIdPlain(
                StatusMp.class,
                statusEntry.getStatusMpId()
            );
            st = stKombi.getStatusLev().getId();
            w = stKombi.getStatusVal().getId();
            if (st < stufe) {
                stufe = st;
                status[stufe-1] = w;
            };
            if (stufe == 1) {
                break;
            };
        }
        if ( status[2] != 0 && status[1] == 0) {status[1] = status[2];}
        if ( status[1] != 0 && status[0] == 0) {status[0] = status[1];}
        return "" + status[0] + status[1] + status[2] + "0";
    }

    /**
     * Write {@link LKommentarM} attributes.
     * @param mk    The {@link LKommentarM} object.
     * @return Single LAF line.
     */
    private String writeKommentar(CommMeasm mk) {
        String value = "\"" + mk.getMeasFacilId() + "\" "
            + toUTCString(mk.getDate()) + " "
            + "\"" + mk.getText() + "\"";
        return lafLine("KOMMENTAR", value);
    }

    /**
     * Write {@link LMesswert} attributes.
     * @param mw    The {@link LMesswert} object.
     * @return Single LAF line.
     */
    @SuppressWarnings("unchecked")
    private String writeMesswert(MeasVal mw) {
        QueryBuilder<Measd> builder =
            repository.queryBuilder(Measd.class);
        builder.and("id", mw.getMeasdId());
        List<Measd> groessen =
            (List<Measd>) repository.filter(
                builder.getQuery()).getData();

        QueryBuilder<MeasUnit> eBuilder =
            repository.queryBuilder(MeasUnit.class);
        eBuilder.and("id", mw.getMeasUnitId());
        List<MeasUnit> einheiten =
            (List<MeasUnit>) repository.filter(
                eBuilder.getQuery()).getData();

        String tag = "MESSWERT";
        String value = "\"" + groessen.get(0).getName() + "\"";
        value += " ";
        value += mw.getLessThanLOD() == null ? " " : mw.getLessThanLOD();
        value += mw.getLessThanLOD() == null
            ? mw.getMeasVal() : mw.getDetectLim();
        value += " \"" + einheiten.get(0).getUnitSymbol() + "\"";
        value += mw.getError() == null ? " 0.0" : " " + mw.getError();
        if (mw.getIsThreshold() == null
            || !mw.getIsThreshold()
        ) {
            if (mw.getDetectLim() != null) {
                tag += "_NWG";
                value += " " + mw.getDetectLim();
            }
        } else {
            tag += "_NWG_G";
            value += " "
                + (mw.getDetectLim() == null
                    ? "0.0" : mw.getDetectLim());
            value += " " + (mw.getIsThreshold() == null
                ? " N" : mw.getIsThreshold() ? " J" : " N");
        }
        return lafLine(tag, value);
    }

    /**
     * Write a single LAF conform line from key and value.
     *
     * @param key   The key.
     * @param value The value.
     * @return LAF conform line.
     */
    private String lafLine(String key, String value) {
        return lafLine(key, value, DEFAULT_FORMAT);
    }

    /**
     * Write a single LAF conform line from key and value.
     *
     * @param key    The key.
     * @param value  The value.
     * @param format A format string for the value
     * @return LAF conform line.
     */
    private String lafLine(String key, Object value, String format) {
        return String.format(KEY_FORMAT, key)
            + String.format(format, value)
            + "\n";
    }

    private String toUTCString(Timestamp timestamp) {
        DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyyMMdd HHmm");
        return formatter.format(timestamp.toInstant().atZone(ZoneOffset.UTC));
    }

    @Override
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
        this.authorization = new HeaderAuthorization(userInfo, this.repository);
    }
}
