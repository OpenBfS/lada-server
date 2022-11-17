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
import de.intevation.lada.model.land.KommentarM;
import de.intevation.lada.model.land.KommentarP;
import de.intevation.lada.model.land.Messung;
import de.intevation.lada.model.land.Messwert;
import de.intevation.lada.model.land.Ortszuordnung;
import de.intevation.lada.model.land.Sample;
import de.intevation.lada.model.land.StatusProtokoll;
import de.intevation.lada.model.land.ZusatzWert;
import de.intevation.lada.model.stammdaten.MeasUnit;
import de.intevation.lada.model.stammdaten.MeasFacil;
import de.intevation.lada.model.stammdaten.Measd;
import de.intevation.lada.model.stammdaten.Site;
import de.intevation.lada.model.stammdaten.MunicDiv;
import de.intevation.lada.model.stammdaten.ProbenZusatz;
import de.intevation.lada.model.stammdaten.Probenart;
import de.intevation.lada.model.stammdaten.DatasetCreator;
import de.intevation.lada.model.stammdaten.MpgCateg;
import de.intevation.lada.model.stammdaten.Probenehmer;
import de.intevation.lada.model.stammdaten.ReiProgpunktGruppe;
import de.intevation.lada.model.stammdaten.StatusKombi;
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
        QueryBuilder<KommentarP> kommBuilder =
            repository.queryBuilder(KommentarP.class);
        kommBuilder.and("probeId", probe.getId());
        Response kommentar =
            repository.filter(kommBuilder.getQuery());
        List<KommentarP> kommentare = (List<KommentarP>) kommentar.getData();

        String probenart = null;
        if (probe.getSampleMethId() != null) {
            QueryBuilder<Probenart> builder =
                repository.queryBuilder(Probenart.class);
            builder.and("id", probe.getSampleMethId());
            List<Probenart> probenarten =
                (List<Probenart>) repository.filter(
                    builder.getQuery()
                ).getData();
            probenart = probenarten.get(0).getProbenart();
        }

        MeasFacil messstelle =
            repository.getByIdPlain(
                MeasFacil.class, probe.getMeasFacilId());

        QueryBuilder<ZusatzWert> zusatzBuilder =
            repository.queryBuilder(ZusatzWert.class);
        zusatzBuilder.and("probeId", probe.getId());
        Response zusatz =
            repository.filter(zusatzBuilder.getQuery());
        List<ZusatzWert> zusatzwerte = (List<ZusatzWert>) zusatz.getData();

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
        if (probe.getStateMpgId() != null) {
            MpgCateg mpkat = repository.getByIdPlain(
                MpgCateg.class, probe.getStateMpgId());
            laf += lafLine("MESSPROGRAMM_LAND", mpkat.getExtId(), CN);
        }
        if (probe.getSamplerId() != null) {
            Probenehmer prn = repository.getByIdPlain(
                Probenehmer.class, probe.getSamplerId());
            laf += lafLine("PROBENAHMEINSTITUTION", prn.getPrnId(), CN);
        }
        if (probe.getReiAgGrId() != null) {
            ReiProgpunktGruppe rpg = repository.getByIdPlain(
                ReiProgpunktGruppe.class,
                probe.getReiAgGrId());
            laf += lafLine(
                "REI_PROGRAMMPUNKTGRUPPE",
                rpg.getReiProgPunktGruppe(), CN);
        }
        for (ZusatzWert zw : zusatzwerte) {
            laf += writeZusatzwert(zw);
        }
        for (KommentarP kp : kommentare) {
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
    private String writeZusatzwert(ZusatzWert zw) {
        QueryBuilder<ProbenZusatz> builder =
            repository.queryBuilder(ProbenZusatz.class);
        builder.and("id", zw.getPzsId());
        List<ProbenZusatz> zusatz =
            (List<ProbenZusatz>) repository.filter(
                builder.getQuery()
            ).getData();

        String value = "\"" + zusatz.get(0).getId() + "\"";
        value += ((zw.getKleinerAls() == null)
            ? " "
            : " " + zw.getKleinerAls());
        value += zw.getMesswertPzs();
        value += " " + ((zusatz.get(0).getUnitId() == null)
            ? "\"\"" : zusatz.get(0).getUnitId());
        value += " " + ((zw.getMessfehler() == null) ? "" : zw.getMessfehler());
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
        QueryBuilder<Ortszuordnung> builder =
            repository.queryBuilder(Ortszuordnung.class);
        builder.and("probeId", probe.getId());
        Response objects = repository.filter(builder.getQuery());
        List<Ortszuordnung> orte =
            (List<Ortszuordnung>) objects.getData();

        String laf = "";
        for (Ortszuordnung o : orte) {
            if ("E".equals(o.getOrtszuordnungTyp())
                || "R".equals(o.getOrtszuordnungTyp())) {
                laf += writeOrtData(o, "P_");
            }
        }
        for (Ortszuordnung o : orte) {
            if ("U".equals(o.getOrtszuordnungTyp())
                || "R".equals(o.getOrtszuordnungTyp())) {
                laf += "%URSPRUNGSORT%\n";
                laf += writeOrtData(o, "U_");
            }
        }
        return laf;
    }

    /**
     * Write {@link LOrt} attributes.
     *
     * @param Ortszuordnung
     * @return LAF conform string
     */
    @SuppressWarnings("unchecked")
    private String writeOrtData(Ortszuordnung o, String typePrefix) {
        String laf = "";
        if (o.getOrtszusatztext() != null
            && o.getOrtszusatztext().length() > 0
        ) {
            laf += lafLine(typePrefix + "ORTS_ZUSATZTEXT",
                o.getOrtszusatztext(), CN);
        }
        QueryBuilder<Site> oBuilder = repository.queryBuilder(Site.class);
        oBuilder.and("id", o.getOrtId());
        List<Site> sOrte =
            (List<Site>) repository.filter(
                oBuilder.getQuery()
            ).getData();

        if (sOrte.get(0).getStateId() != null) {
            laf += lafLine(typePrefix + "HERKUNFTSLAND_S",
                String.format("%08d", sOrte.get(0).getStateId()));
        }

        if (sOrte.get(0).getMunicId() != null
            && sOrte.get(0).getMunicId().length() > 0
        ) {
            laf += lafLine(typePrefix + "GEMEINDESCHLUESSEL",
                sOrte.get(0).getMunicId());
        }

        if (sOrte.get(0).getMunicDivId() != null) {
            MunicDiv gu = repository.getByIdPlain(
                MunicDiv.class, sOrte.get(0).getMunicDivId());
            laf += lafLine(typePrefix + "ORTS_ZUSATZKENNZAHL",
                gu.getSiteId(), CN);
        }

        String koord = String.format("%02d", sOrte.get(0).getSpatRefSysId());
        koord += " \"";
        koord += sOrte.get(0).getXCoordExt() + "\" \"";
        koord += sOrte.get(0).getYCoordExt() + "\"";
        laf += lafLine(typePrefix + "KOORDINATEN_S", koord);

        if ("P_".equals(typePrefix) && o.getOzId() != null) {
            laf += lafLine(
                typePrefix + "ORTS_ZUSATZCODE",
                o.getOzId(),
                CN);
        } else if ("U_".equals(typePrefix)
            && "R".equals(o.getOrtszuordnungTyp())
        ) {
            laf += lafLine(
                typePrefix + "ORTS_ZUSATZCODE",
                sOrte.get(0).getExtId(),
                CN);
        } else if ("U_".equals(typePrefix)
            && o.getOzId() != null
        ) {
            laf += lafLine(
                typePrefix + "ORTS_ZUSATZCODE",
                o.getOzId(),
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
    private String writeKommentar(KommentarP kp) {
        String value = "\"" + kp.getMstId()
            + "\" "
            + toUTCString(kp.getDatum()) + " "
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
        QueryBuilder<Messung> builder = repository.queryBuilder(Messung.class);
        if (messungen.isEmpty()) {
            // Get all messungen
            builder.and("probeId", probe.getId());
        } else {
            builder.andIn("id", messungen);
        }
        List<Messung> mess = repository.filterPlain(
            builder.getQuery());

        String laf = "";
        for (Messung m : mess) {
            laf += "%MESSUNG%\n";
            QueryBuilder<Messwert> wertBuilder =
                repository.queryBuilder(Messwert.class);
            wertBuilder.and("messungsId", m.getId());
            Response messw =
                repository.filter(wertBuilder.getQuery());
            List<Messwert> werte = (List<Messwert>) messw.getData();
            QueryBuilder<KommentarM> kommBuilder =
                repository.queryBuilder(KommentarM.class);
            kommBuilder.and("messungsId", m.getId());
            Response kommentar =
                repository.filter(kommBuilder.getQuery());
            List<KommentarM> kommentare =
                (List<KommentarM>) kommentar.getData();
            laf += lafLine("MESSUNGS_ID", m.getExterneMessungsId().toString());
            laf += m.getNebenprobenNr() == null
                ? ""
                : lafLine("NEBENPROBENNUMMER", m.getNebenprobenNr(), CN);
            laf += m.getMesszeitpunkt() == null
                ? ""
                : lafLine(
                    "MESS_DATUM_UHRZEIT", toUTCString(m.getMesszeitpunkt()));
            laf += m.getMessdauer() == null
                ? ""
                : lafLine("MESSZEIT_SEKUNDEN", m.getMessdauer().toString());
            laf += m.getMmtId() == null
                ? ""
                : lafLine("MESSMETHODE_S", m.getMmtId(), CN);
            laf += lafLine(
                "ERFASSUNG_ABGESCHLOSSEN",
                (m.getFertig() ? "1" : "0"));
            laf += lafLine("BEARBEITUNGSSTATUS", writeStatus(m));
            if (this.userInfo != null
                // TODO: GET is correct RequestMethod here?
                && authorization.isAuthorized(
                    m, RequestMethod.GET, Messung.class)
            ) {
                for (Messwert mw : werte) {
                    laf += writeMesswert(mw);
                }
            }
            for (KommentarM mk: kommentare) {
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
    private String writeStatus(Messung messung) {
        Integer[] status = {0, 0, 0};
        QueryBuilder<StatusProtokoll> builder =
        repository.queryBuilder(StatusProtokoll.class);
            builder.and("messungsId", messung.getId());
        builder.orderBy("id", false);
        List<StatusProtokoll> statusHistory = repository.filterPlain(
                builder.getQuery());
        Integer stufe = 4;
        Integer st, w;
        for (StatusProtokoll statusEntry : statusHistory) {
            StatusKombi stKombi = repository.getByIdPlain(
                StatusKombi.class,
                statusEntry.getStatusKombi()
            );
            st = stKombi.getStatusStufe().getId();
            w = stKombi.getStatusWert().getId();
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
    private String writeKommentar(KommentarM mk) {
        String value = "\"" + mk.getMstId() + "\" "
            + toUTCString(mk.getDatum()) + " "
            + "\"" + mk.getText() + "\"";
        return lafLine("KOMMENTAR", value);
    }

    /**
     * Write {@link LMesswert} attributes.
     * @param mw    The {@link LMesswert} object.
     * @return Single LAF line.
     */
    @SuppressWarnings("unchecked")
    private String writeMesswert(Messwert mw) {
        QueryBuilder<Measd> builder =
            repository.queryBuilder(Measd.class);
        builder.and("id", mw.getMessgroesseId());
        List<Measd> groessen =
            (List<Measd>) repository.filter(
                builder.getQuery()).getData();

        QueryBuilder<MeasUnit> eBuilder =
            repository.queryBuilder(MeasUnit.class);
        eBuilder.and("id", mw.getMehId());
        List<MeasUnit> einheiten =
            (List<MeasUnit>) repository.filter(
                eBuilder.getQuery()).getData();

        String tag = "MESSWERT";
        String value = "\"" + groessen.get(0).getName() + "\"";
        value += " ";
        value += mw.getMesswertNwg() == null ? " " : mw.getMesswertNwg();
        value += mw.getMesswertNwg() == null
            ? mw.getMesswert() : mw.getNwgZuMesswert();
        value += " \"" + einheiten.get(0).getUnitSymbol() + "\"";
        value += mw.getMessfehler() == null ? " 0.0" : " " + mw.getMessfehler();
        if (mw.getGrenzwertueberschreitung() == null
            || !mw.getGrenzwertueberschreitung()
        ) {
            if (mw.getNwgZuMesswert() != null) {
                tag += "_NWG";
                value += " " + mw.getNwgZuMesswert();
            }
        } else {
            tag += "_NWG_G";
            value += " "
                + (mw.getNwgZuMesswert() == null
                    ? "0.0" : mw.getNwgZuMesswert());
            value += " " + (mw.getGrenzwertueberschreitung() == null
                ? " N" : mw.getGrenzwertueberschreitung() ? " J" : " N");
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
