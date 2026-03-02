/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter.json;

import java.io.StringReader;
import java.io.Writer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonGenerator;

import de.intevation.lada.data.requests.QueryExportParameters;
import de.intevation.lada.exporter.QueryExporter;
import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.Geolocat_;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.model.lada.Sample_;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.master.AdminUnit;
import de.intevation.lada.model.master.EnvDescrip;
import de.intevation.lada.model.master.EnvDescrip_;
import de.intevation.lada.model.master.EnvMedium;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.MeasUnit;
import de.intevation.lada.model.master.Measd;
import de.intevation.lada.model.master.Mmt;
import de.intevation.lada.model.master.MpgCateg;
import de.intevation.lada.model.master.OprMode;
import de.intevation.lada.model.master.Regulation;
import de.intevation.lada.model.master.ReiAgGr;
import de.intevation.lada.model.master.SampleMeth;
import de.intevation.lada.model.master.SampleSpecif;
import de.intevation.lada.model.master.Sampler;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.State;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.JSONBConfig;

/**
 * Exporter class for writing query results to JSON.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class JsonExporter implements QueryExporter<QueryExportParameters> {

    private static final int ZEBS_COUNTER = 3;

    @Inject
    private Repository repository;

    @Override
    @SuppressWarnings("unchecked")
    public void export(
        Stream<Map<String, Object>> queryResult,
        Writer sink,
        QueryExportParameters options,
        List<String> columnsToInclude,
        String subDataKey,
        Integer qId,
        DateFormat dateFormat,
        ResourceBundle i18n
    ) {
        String idColumn = options.getIdField();

        try (JsonGenerator generator = Json.createGenerator(sink)) {
            generator.writeStartObject();

            Iterator<Map<String, Object>> resultIterator
                = queryResult.iterator();
            while (resultIterator.hasNext()) {
                Map<String, Object> item = resultIterator.next();
                generator.writeStartObject(item.get(idColumn).toString());
                //Add value for each column
                columnsToInclude.forEach(key -> {
                        Object value = item.get(key);
                        if (value == null) {
                            generator.writeNull(key);
                        } else if (value instanceof Integer) {
                            generator.write(key, (Integer) value);
                        } else if (value instanceof Double) {
                            generator.write(key, (Double) value);
                        } else if (value instanceof Date) {
                            //Convert to target timezone
                            Date time = (Date) value;
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(time);
                            generator.write(
                                key, dateFormat.format(calendar.getTime()));
                        } else {
                            generator.write(key, value.toString());
                        }
                    });

                if (subDataKey != null
                    && item.containsKey(subDataKey)
                    && item.get(subDataKey) instanceof List<?>
                ) {
                    generator.writeStartArray(subDataKey);
                    ((List<Map<String, Object>>) item.get(subDataKey)).forEach(
                        map -> {
                            generator.writeStartObject();
                            map.forEach((key, value) -> {
                                    if (value == null) {
                                        generator.writeNull(key);
                                    } else if (value instanceof Integer) {
                                        generator.write(key, (Integer) value);
                                    } else if (value instanceof Double) {
                                        generator.write(key, (Double) value);
                                    } else {
                                        generator.write(key, value.toString());
                                    }
                                });
                            generator.writeEnd();
                        });
                    generator.writeEnd();
                }
                generator.writeEnd();
            };
            generator.writeEnd();
        }
    }

    /**
     * Export Sample objects as JSON.
     * @param probeIds List of Sample IDs to export.
     * @return Export result as JSON
     */
    public JsonArray exportProben(List<Integer> probeIds) {
        return generateProbenObjectBuilder(probeIds).build();
    }

    /**
     * Export Messungen and associated Proben context.
     * @param messungsIds List of Messungs IDs to export.
     * @return Export result as JSON
     */
    public JsonArray exportMessungen(List<Integer> messungsIds) {
        QueryBuilder<Measm> builder = repository.queryBuilder(Measm.class)
            .andIn(Measm_.id, messungsIds);
        List<Measm> messungen = repository.filter(builder.getQuery());
        List<JsonObjectBuilder> messungenObjectBuilders =
            new ArrayList<>(messungen.size());
        for (Measm m : messungen) {
            Sample p = m.getSample();
            JsonObject sampleJsonObject = convertToJsonObject(p);
            JsonObjectBuilder sampleJsonObjectBuilder =
                Json.createObjectBuilder(sampleJsonObject);
            addProbeninfo(sampleJsonObjectBuilder, p);
            JsonObject measmJsonObject = convertToJsonObject(m);
            JsonObjectBuilder measmJsonObjectBuilder =
                Json.createObjectBuilder(measmJsonObject);
            Mmt mmt = repository.getById(
                Mmt.class,
                m.getMmtId()
            );
            measmJsonObjectBuilder.add("mmt", mmt == null ?
                nullableString("") : nullableString(mmt.getName()));
            addMesswerte(measmJsonObjectBuilder, m);
            addMessungsKommentare(measmJsonObjectBuilder, m);
            addStatusProtokoll(measmJsonObjectBuilder, m);
            JsonArrayBuilder mArry = Json.createArrayBuilder();
            mArry.add(measmJsonObjectBuilder);
            sampleJsonObjectBuilder.add(Sample_.MEASMS, mArry);
            addKommentare(sampleJsonObjectBuilder, p);
            addZusatzwerte(sampleJsonObjectBuilder, p);
            addDeskriptoren(sampleJsonObjectBuilder, p);
            addOrtszuordung(sampleJsonObjectBuilder, p);
            addMessstelle(sampleJsonObjectBuilder, p);
            messungenObjectBuilders.add(sampleJsonObjectBuilder);
        }
        return Json.createArrayBuilder(messungenObjectBuilders).build();
    }

    private JsonArrayBuilder generateProbenObjectBuilder(
        List<Integer> probeIds
    ) {
        JsonArrayBuilder jsonBuilder = Json.createArrayBuilder();
        List<Sample> proben = repository.filter(repository
            .queryBuilder(Sample.class)
            .andIn(Sample_.id, probeIds)
            .getQuery());
        for (Sample s : proben) {
            JsonObject probeJson = convertToJsonObject(s);
            JsonObjectBuilder builder = Json.createObjectBuilder(probeJson);
            addProbeninfo(builder, s);
            addMessungen(builder, s);
            addKommentare(builder, s);
            addZusatzwerte(builder, s);
            addDeskriptoren(builder, s);
            addOrtszuordung(builder, s);
            addMessstelle(builder, s);
            jsonBuilder.add(builder);
        }
        return jsonBuilder;
    }

    private void addProbeninfo(JsonObjectBuilder probe, Sample sample) {
        SampleMeth art = repository.getById(
            SampleMeth.class,
            sample.getSampleMethId()
        );
        Regulation datenbasis = repository.getById(
            Regulation.class,
            sample.getRegulationId()
        );
        EnvMedium umw = sample.getEnvMedium();
        probe.add("sampleMethExtId",
            art == null
            ? nullableString("")
            : nullableString(art.getExtId()));
        probe.add("regulation",
            datenbasis == null
            ? nullableString("")
            : nullableString(datenbasis.getName()));
        probe.add("envMediumName",
            umw == null
            ? nullableString("")
            : nullableString(umw.getName()));
        if (sample.getOprModeId() != null) {
            OprMode ba = repository.getById(
                OprMode.class,
                sample.getOprModeId()
            );
            probe.add("oprModeName", nullableString(ba.getName()));
        }
        if (sample.getMpgCategId() != null) {
            MpgCateg mpl = repository.getById(
                MpgCateg.class,
                sample.getMpgCategId()
            );
            probe.add("mpgCategExtId", nullableString(mpl.getExtId()));
            probe.add("mpgCategName", nullableString(mpl.getName()));
        }
        if (sample.getSamplerId() != null) {
            Sampler probenehmer = repository.getById(
                Sampler.class,
                sample.getSamplerId()
            );
            probe.add("samplerExtId", nullableString(probenehmer.getExtId()));
            probe.add("samplerDescr", nullableString(probenehmer.getDescr()));
            probe.add("samplerNetworkId", nullableString(probenehmer.getNetworkId()));
            probe.add(
                "samplerShortText", nullableString(probenehmer.getShortText()));
        }
        if (sample.getReiAgGrId() != null) {
            ReiAgGr reiAgGr = repository.getById(
                ReiAgGr.class,
                sample.getReiAgGrId()
            );
            probe.add("reiAgGrDescr", nullableString(reiAgGr.getDescr()));
            probe.add("reiAgGrName", nullableString(reiAgGr.getName()));
        }
    }

    private void addMessstelle(JsonObjectBuilder probe, Sample sample) {
        MeasFacil messstelle = repository.getById(
            MeasFacil.class,
            sample.getMeasFacilId()
        );
        MeasFacil laborMessstelle = repository.getById(
            MeasFacil.class,
            sample.getApprLabId()
        );
        JsonObjectBuilder measFacilBuilder = Json.createObjectBuilder(
            convertToJsonObject(messstelle)
        );
        JsonObjectBuilder apprLabBuilder = Json.createObjectBuilder(
            convertToJsonObject(laborMessstelle)
        );
        probe.add("measFacil", measFacilBuilder);
        probe.add("apprLab", apprLabBuilder);
    }

    private void addMessungen(JsonObjectBuilder probe, Sample sample) {
        Collection<Measm> messungen = sample.getMeasms();
        List<JsonObjectBuilder> messungenBuilders =
            new ArrayList<>(messungen.size());
        for (Measm messung: messungen) {
            Mmt mmt = repository.getById(
                Mmt.class,
                messung.getMmtId()
            );
            JsonObject messungJsonObject = convertToJsonObject(messung);
            JsonObjectBuilder messungJsonObjectBuilder =
                Json.createObjectBuilder(messungJsonObject);
            messungJsonObjectBuilder.add("mmt",
                mmt == null
                ? nullableString("")
                : nullableString(mmt.getName()));
            addMesswerte(messungJsonObjectBuilder, messung);
            addMessungsKommentare(messungJsonObjectBuilder, messung);
            addStatusProtokoll(messungJsonObjectBuilder, messung);
            messungenBuilders.add(messungJsonObjectBuilder);
        }
        probe.add(Sample_.MEASMS, Json.createArrayBuilder(messungenBuilders));
    }

    private void addKommentare(JsonObjectBuilder probe, Sample sample) {
        Collection<CommSample> kommentare = sample.getCommSamples();
        List<JsonObjectBuilder> kommentareJsonObjectBuilders =
            new ArrayList<>(kommentare.size());
        for (CommSample kommentar: kommentare) {
            JsonObject kommentarJsonObject = convertToJsonObject(kommentar);
            JsonObjectBuilder kommentarJsonObjectBuilder =
                Json.createObjectBuilder(kommentarJsonObject);
            MeasFacil mst = repository.getById(
                MeasFacil.class,
                kommentar.getMeasFacilId()
            );
            kommentarJsonObjectBuilder.add(
                "measFacil",
                nullableString(mst.getName()));
            kommentareJsonObjectBuilders.add(kommentarJsonObjectBuilder);
        }
        probe.add(Sample_.COMM_SAMPLES, Json.createArrayBuilder(
                kommentareJsonObjectBuilders));
    }

    private void addZusatzwerte(JsonObjectBuilder probe, Sample sample) {
        Collection<SampleSpecifMeasVal> zusatzwerte =
            sample.getSampleSpecifMeasVals();
        List<JsonObjectBuilder> zusatzwerteJsonObjectBuilders =
            new ArrayList<>(zusatzwerte.size());
        for (SampleSpecifMeasVal zusatzwert:zusatzwerte) {
            SampleSpecif pz = repository.getById(
                SampleSpecif.class,
                zusatzwert.getSampleSpecifId()
            );
            JsonObject zusatzwertJsonObject = convertToJsonObject(zusatzwert);
            JsonObjectBuilder zusatzwertJsonObjectBuilder =
                Json.createObjectBuilder(zusatzwertJsonObject);
            zusatzwertJsonObjectBuilder.add(
                "sampleSpecifName", nullableString(pz.getName()));
            Integer mehId = pz.getMeasUnitId();
            if (mehId != null) {
                MeasUnit meh = repository.getById(
                    MeasUnit.class, mehId);
                zusatzwertJsonObjectBuilder.add(
                    "unit", nullableString(meh.getUnitSymbol()));
            } else {
                continue;
            }
            zusatzwerteJsonObjectBuilders.add(zusatzwertJsonObjectBuilder);
        }
        probe.add(Sample_.SAMPLE_SPECIF_MEAS_VALS,
            Json.createArrayBuilder(zusatzwerteJsonObjectBuilders));
    }

    private void addDeskriptoren(JsonObjectBuilder probe, Sample sample) {
        String desk = sample.getEnvDescripDisplay();
        if (desk == null) {
            return;
        }
        String[] parts = desk.split(" ");
        if (parts.length <= 1) {
            return;
        }

        int vorgaenger = 0;
        JsonObjectBuilder node = Json.createObjectBuilder();

        boolean isZebs = Integer.parseInt(parts[1]) == 1;
        int hdV = 0;
        int ndV = 0;
        for (int i = 0; i < parts.length - 1; i++) {
            QueryBuilder<EnvDescrip> builder =
                repository.queryBuilder(EnvDescrip.class);
            String beschreibung = "";
            if (Integer.parseInt(parts[i + 1]) != 0) {
                builder.and(EnvDescrip_.lev, i)
                .and(EnvDescrip_.levVal, Integer.parseInt(parts[i + 1]));
                if (i != 0) {
                    builder.and(EnvDescrip_.predId, vorgaenger);
                }
                List<EnvDescrip> found =
                    repository.filter(builder.getQuery());
                if (!found.isEmpty()) {
                    beschreibung = found.get(0).getName();
                    if ((isZebs && i < ZEBS_COUNTER)
                        || (!isZebs && i < 1)
                    ) {
                        if (i < ZEBS_COUNTER) {
                            hdV = found.get(0).getId();
                        }
                        if (isZebs && i == 1) {
                            ndV = found.get(0).getId();
                        }
                        vorgaenger = hdV;
                    } else {
                        if (!isZebs && i == 1) {
                            ndV = found.get(0).getId();
                        }
                        vorgaenger = ndV;
                    }
                }
            }
            node.add("S" + i, beschreibung);
            builder = builder.getEmptyBuilder();
        }
        probe.add("envDescrip", node);
    }

    private void addMesswerte(
        JsonObjectBuilder messungJsonObjectBuilder, Measm measm
    ) {
        Collection<MeasVal> messwerte = measm.getMeasVals();
        List<JsonObjectBuilder> messwerteJsonObjectBuilders =
            new ArrayList<>(messwerte.size());
        for (MeasVal messwert: messwerte) {
            JsonObject messwertJsonObject = convertToJsonObject(messwert);
            JsonObjectBuilder messwertJsonObjectBuilder =
                Json.createObjectBuilder(messwertJsonObject);
            MeasUnit meh = repository.getById(
                MeasUnit.class,
                messwert.getMeasUnitId()
            );
            messwertJsonObjectBuilder.add("unit",
                meh == null
                ? nullableString("")
                : nullableString(meh.getUnitSymbol()));
            Measd mg = repository.getById(
                Measd.class,
                messwert.getMeasdId()
            );
            messwertJsonObjectBuilder.add("measd",
                mg == null ? nullableString("") : nullableString(mg.getName()));
            messwerteJsonObjectBuilders.add(messwertJsonObjectBuilder);
        }
        messungJsonObjectBuilder.add(Measm_.MEAS_VALS,
            Json.createArrayBuilder(messwerteJsonObjectBuilders));
    }

    private void addMessungsKommentare(
        JsonObjectBuilder messungJsonObjectBuilder, Measm measm
    ) {
        Collection<CommMeasm> kommentare = measm.getCommMeasms();
        List<JsonObjectBuilder> kommJsonObjectBuilders =
            new ArrayList<>(kommentare.size());
        for (CommMeasm kommentar: kommentare) {
            JsonObject kommentarJsonObject = convertToJsonObject(kommentar);
            JsonObjectBuilder kommentarJsonObjectBuilder =
                Json.createObjectBuilder(kommentarJsonObject);
            MeasFacil mst = repository.getById(
                MeasFacil.class,
                kommentar.getMeasFacilId()
            );
            kommentarJsonObjectBuilder.add(
                "measFacil",
                nullableString(mst.getName()));
            kommJsonObjectBuilders.add(kommentarJsonObjectBuilder);
        }
        messungJsonObjectBuilder.add(Measm_.COMM_MEASMS,
            Json.createArrayBuilder(kommJsonObjectBuilders));
    }

    private void addStatusProtokoll(
        JsonObjectBuilder messungJsonObjectBuilder,
        Measm measm
    ) {
        Collection<StatusProt> status = measm.getStatusProts();
        List<JsonObjectBuilder> statusObjectBuilders = new ArrayList<>();
        for (StatusProt statusProt: status) {
            StatusMp kombi = repository.getById(
                StatusMp.class,
                statusProt.getStatusMpId()
            );
            JsonObject statusJsonObject = convertToJsonObject(statusProt);
            JsonObjectBuilder statusBuilder =
                Json.createObjectBuilder(statusJsonObject);
            statusBuilder.add(
                "statusLev",
                nullableString(kombi.getStatusLev().getLev()));
            statusBuilder.add(
                "statusVal",
                nullableString(kombi.getStatusVal().getVal()));
            MeasFacil mst = repository.getById(
                MeasFacil.class,
                statusProt.getMeasFacilId()
            );
            statusBuilder.add(
                "measFacil",
                nullableString(mst.getName()));
            statusObjectBuilders.add(statusBuilder);
        }
        messungJsonObjectBuilder.add("statusProtocol",
            Json.createArrayBuilder(statusObjectBuilders));
    }

    private void addOrtszuordung(JsonObjectBuilder probe, Sample sample) {
        Collection<Geolocat> ortszuordnung = sample.getGeolocats();
        List<JsonObjectBuilder> geolocatBuilders = new ArrayList<>();
        for (Geolocat g: ortszuordnung) {
            JsonObject ortszuordnungJsonObject = convertToJsonObject(g);
            JsonObjectBuilder geolocatBuilder =
                Json.createObjectBuilder(ortszuordnungJsonObject);
            addOrt(geolocatBuilder, g);
            geolocatBuilders.add(geolocatBuilder);
        }
        probe.add("geolocat", Json.createArrayBuilder(geolocatBuilders));
    }

    private void addOrt(
        JsonObjectBuilder ortszuordnungJson, Geolocat geolocat
    ) {
        Site ort = geolocat.getSite();
        JsonObject siteJsonObject = convertToJsonObject(ort);
        JsonObjectBuilder siteJsonObjectBuilder =
            Json.createObjectBuilder(siteJsonObject);

        AdminUnit ve = ort.getAdminUnit();
        if (ve != null) {
            siteJsonObjectBuilder.add("adminUnit", ve.getName());
        }

        Integer stateId = ort.getStateId();
        if (stateId != null) {
            State staat = repository.getById(State.class, stateId);
            siteJsonObjectBuilder.add("state", staat.getCtry());
        }

        ortszuordnungJson.add(Geolocat_.SITE, siteJsonObjectBuilder);
    }

    private JsonObject convertToJsonObject(Object o) {
        String jsonString = JSONBConfig.JSONB.toJson(o);
        JsonReader reader = Json.createReader(new StringReader(jsonString));
        JsonObject jsonObject = reader.readObject();
        reader.close();
        return jsonObject;
    }

    private JsonValue nullableString (String value){
        if(value == null) {
            return JsonValue.NULL;
        }
        return Json.createValue(value);
    }
}
