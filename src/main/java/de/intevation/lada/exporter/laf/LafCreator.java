/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter.laf;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import de.intevation.lada.exporter.Creator;
import de.intevation.lada.i18n.I18n;
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
import de.intevation.lada.model.lada.SampleSpecifMeasVal_;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.lada.StatusProt_;
import de.intevation.lada.model.master.DatasetCreator;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.MeasUnit;
import de.intevation.lada.model.master.MpgCateg;
import de.intevation.lada.model.master.MunicDiv;
import de.intevation.lada.model.master.ReiAgGr;
import de.intevation.lada.model.master.SampleMeth;
import de.intevation.lada.model.master.SampleSpecif;
import de.intevation.lada.model.master.Sampler;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


/**
 * This creator produces a LAF conform String containing all information about
 * a single {@link Sample} object including subobjects like
 * {@link Measm}, {@link MeasVal}, {@link CommSample}...
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Named("lafcreator")
public class LafCreator implements Creator {

    private static final int MP6 = 6;
    private static final int BAID3 = 3;
    private static final int MP5 = 5;
    private static final int MP4 = 4;
    private static final int DATENBASIS4 = 4;
    // Some format strings corresponding to LAF notation
    private static final String KEY_FORMAT = "%-30s";
    private static final String DEFAULT_FORMAT = "%s";
    private static final String CN = "\"%s\""; // cn, mcn, scn

    private Authorization authorization;

    /**
     * The repository used to read data.
     */
    @Inject
    private Repository repository;

    @Inject
    private I18n i18n;

    private UserInfo userInfo;

    @Override
    public String createProbe(Integer probeId) {
        String lafProbe = "%PROBE%\n";
        lafProbe += lafLine("UEBERTRAGUNGSFORMAT", "7", CN);
        lafProbe += lafLine("VERSION", "0084", CN);
        lafProbe += probeToLAF(probeId, new ArrayList<Integer>());
        return lafProbe;
    }

    @Override
    public String createMessung(Integer probeId, List<Integer> messungen) {
        String lafProbe = "%PROBE%\n";
        lafProbe += lafLine("UEBERTRAGUNGSFORMAT", "7", CN);
        lafProbe += lafLine("VERSION", "0084", CN);
        lafProbe += probeToLAF(probeId, messungen);
        return lafProbe;
    }

    private String probeToLAF(Integer probeId, List<Integer> messungen) {
        Sample aProbe = repository.getById(Sample.class, probeId);
        String lafProbe = writeAttributes(aProbe, messungen);
        return lafProbe;
    }

    private String writeAttributes(Sample probe, List<Integer> messungen) {
        QueryBuilder<CommSample> kommBuilder = repository
            .queryBuilder(CommSample.class)
            .and(CommSample_.sample, probe);
        List<CommSample> kommentare = repository.filter(
            kommBuilder.getQuery());

        String probenart = null;
        if (probe.getSampleMethId() != null) {
            probenart = repository.getById(
                SampleMeth.class, probe.getSampleMethId()).getExtId();
        }

        MeasFacil messstelle =
            repository.getById(
                MeasFacil.class, probe.getMeasFacilId());

        QueryBuilder<SampleSpecifMeasVal> zusatzBuilder = repository
            .queryBuilder(SampleSpecifMeasVal.class)
            .and(SampleSpecifMeasVal_.sample, probe);
        List<SampleSpecifMeasVal> zusatzwerte =
            repository.filter(zusatzBuilder.getQuery());

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
            DatasetCreator erz = repository.getById(
                DatasetCreator.class, probe.getDatasetCreatorId());
            laf += lafLine("ERZEUGER", erz.getExtId(), CN);
        }
        if (probe.getMpgCategId() != null) {
            MpgCateg mpkat = repository.getById(
                MpgCateg.class, probe.getMpgCategId());
            laf += lafLine("MESSPROGRAMM_LAND", mpkat.getExtId(), CN);
        }
        if (probe.getSamplerId() != null) {
            Sampler prn = repository.getById(
                Sampler.class, probe.getSamplerId());
            laf += lafLine("PROBENAHMEINSTITUTION", prn.getExtId(), CN);
        }
        if (probe.getReiAgGrId() != null) {
            ReiAgGr rpg = repository.getById(
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

    private String writeZusatzwert(SampleSpecifMeasVal zw) {
        SampleSpecif zusatz = repository.getById(
            SampleSpecif.class, zw.getSampleSpecifId());

        String value = "\"" + zusatz.getId() + "\"";
        value += ((zw.getSmallerThan() == null)
            ? " "
            : " " + zw.getSmallerThan());
        value += zw.getMeasVal();
        value += " " + ((zusatz.getMeasUnitId() == null)
            ? "\"\"" : zusatz.getMeasUnitId());
        value += " " + ((zw.getError() == null) ? "" : zw.getError());
        return lafLine("PZB_S", value);
    }

    private String writeOrt(Sample probe) {
        QueryBuilder<Geolocat> builder = repository
            .queryBuilder(Geolocat.class)
            .and(Geolocat_.sample, probe);
        List<Geolocat> orte = repository.filter(builder.getQuery());

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

    private String writeOrtData(Geolocat o, String typePrefix) {
        String laf = "";
        if (o.getAddSiteText() != null
            && o.getAddSiteText().length() > 0
        ) {
            laf += lafLine(typePrefix + "ORTS_ZUSATZTEXT",
                o.getAddSiteText(), CN);
        }

        Site sOrt = o.getSite();

        if (sOrt.getStateId() != null) {
            laf += lafLine(typePrefix + "HERKUNFTSLAND_S",
                String.format("%08d", sOrt.getStateId()));
        }

        if (sOrt.getAdminUnitId() != null
            && sOrt.getAdminUnitId().length() > 0
        ) {
            laf += lafLine(typePrefix + "GEMEINDESCHLUESSEL",
                sOrt.getAdminUnitId());
        }

        if (sOrt.getMunicDivId() != null) {
            MunicDiv gu = repository.getById(
                MunicDiv.class, sOrt.getMunicDivId());
            laf += lafLine(typePrefix + "ORTS_ZUSATZKENNZAHL",
                gu.getSiteId(), CN);
        }

        String koord = String.format("%02d", sOrt.getSpatRefSysId());
        koord += " \"";
        koord += sOrt.getCoordXExt() + "\" \"";
        koord += sOrt.getCoordYExt() + "\"";
        laf += lafLine(typePrefix + "KOORDINATEN_S", koord);

        if ("P_".equals(typePrefix) && o.getPoiId() != null) {
            laf += lafLine(
                typePrefix + "ORTS_ZUSATZCODE",
                o.getPoiId(),
                CN);
        } else if ("U_".equals(typePrefix)
            && "R".equals(o.getTypeRegulation())
        ) {
            Sample s = o.getSample();
            if (s.getRegulationId() == DATENBASIS4) {
                laf += lafLine(
                    typePrefix + "ORTS_ZUSATZCODE",
                    sOrt.getExtId(),
                    CN);
            } else {
                if (o.getPoiId() != null) {
                    laf += lafLine(
                        typePrefix + "ORTS_ZUSATZCODE",
                        o.getPoiId(),
                        CN);
                }
            }
        } else if ("U_".equals(typePrefix) && o.getPoiId() != null) {
            laf += lafLine(
                typePrefix + "ORTS_ZUSATZCODE",
                o.getPoiId(),
                CN);
        }
        return laf;
    }

    private String writeKommentar(CommSample kp) {
        String value = "\"" + kp.getMeasFacilId()
            + "\" "
            + toUTCString(kp.getDate()) + " "
            + "\"" + kp.getText() + "\"";
        return lafLine("PROBENKOMMENTAR", value);
    }

    private String writeMessung(Sample probe, List<Integer> messungen) {
        QueryBuilder<Measm> builder = repository.queryBuilder(Measm.class);
        if (messungen.isEmpty()) {
            // Get all messungen
            builder.and(Measm_.sample, probe);
        } else {
            builder.andIn(Measm_.id, messungen);
        }
        List<Measm> mess = repository.filter(
            builder.getQuery());

        String laf = "";
        for (Measm m : mess) {
            laf += "%MESSUNG%\n";
            QueryBuilder<MeasVal> wertBuilder = repository
                .queryBuilder(MeasVal.class)
                .and(MeasVal_.measm, m);
            List<MeasVal> werte = repository.filter(
                wertBuilder.getQuery());
            QueryBuilder<CommMeasm> kommBuilder = repository
                .queryBuilder(CommMeasm.class)
                .and(CommMeasm_.measm, m);
            List<CommMeasm> kommentare = repository.filter(
                kommBuilder.getQuery());
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
                && authorization.isAuthorized(m, RequestMethod.GET)
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

    private String writeStatus(Measm messung) {
        Integer[] status = {0, 0, 0};
        QueryBuilder<StatusProt> builder =
        repository.queryBuilder(StatusProt.class)
        .and(StatusProt_.measm, messung)
        .orderBy(StatusProt_.id, false);
        List<StatusProt> statusHistory = repository.filter(
                builder.getQuery());
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

    private String writeKommentar(CommMeasm mk) {
        String value = "\"" + mk.getMeasFacilId() + "\" "
            + toUTCString(mk.getDate()) + " "
            + "\"" + mk.getText() + "\"";
        return lafLine("KOMMENTAR", value);
    }

    private String writeMesswert(MeasVal mw) {
        String tag = "MESSWERT";
        String value = "\"" + mw.getMeasdId() + "\"";
        value += " ";
        value += mw.getLessThanLOD() == null ? " " : mw.getLessThanLOD();
        value += mw.getLessThanLOD() == null
            ? mw.getMeasVal() : mw.getDetectLim();

        value += " \"" + repository.getById(
            MeasUnit.class, mw.getMeasUnitId()).getUnitSymbol() + "\"";
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

    private String lafLine(String key, String value) {
        return lafLine(key, value, DEFAULT_FORMAT);
    }

    private String lafLine(String key, Object value, String format) {
        return String.format(KEY_FORMAT, key)
            + String.format(format, value)
            + "\n";
    }

    private String toUTCString(Date timestamp) {
        DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyyMMdd HHmm");
        return formatter.format(timestamp.toInstant().atZone(ZoneOffset.UTC));
    }

    @Override
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
        this.authorization = new Authorization(
            userInfo, this.i18n, this.repository);
    }
}
