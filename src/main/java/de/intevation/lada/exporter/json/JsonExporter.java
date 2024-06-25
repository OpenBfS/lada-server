/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.jboss.logging.Logger;

import de.intevation.lada.exporter.ExportConfig;
import de.intevation.lada.exporter.ExportFormat;
import de.intevation.lada.exporter.Exporter;
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
import de.intevation.lada.model.lada.Sample_;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.lada.StatusProt_;
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
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;

/**
 * Exporter class for writing query results to JSON.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
@ExportConfig(format = ExportFormat.JSON)
public class JsonExporter implements Exporter {

    private static final int ZEBS_COUNTER = 3;
    private static final String JSON_DATE_FORMAT
        = "yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSSXXX";

    @Inject private Logger logger;

    @Inject
    private Repository repository;

    /**
     * Export a query result.
     * @param queryResult Result to export as list of maps. Every list item
     *                    represents a row,
     *                    while every map key represents a column
     * @param encoding Ignored. Result is always UTF_8.
     * @param options Export options as JSON Object. Options are: <p>
     *        <ul>
     *          <li> idField: Name of the id column, mandatory </li>
     *          <li> subData: key of the subData json object, optional </li>
     *        </ul>
     *
     * @param columnsToInclude List of column names to include in the export.
     *                         If not set, all columns will be exported
     * @return Export result as input stream or null if the export failed
     */
    @Override
    @SuppressWarnings("unchecked")
    public InputStream export(
        Iterable<Map<String, Object>> queryResult,
        Charset encoding,
        JsonObject options,
        List<String> columnsToInclude,
        String subDataKey,
        Integer qId,
        DateFormat dateFormat,
        ResourceBundle i18n
    ) {
        final JsonObjectBuilder builder = Json.createObjectBuilder();
        String idColumn = options.getString("idField");

        //For each result
        queryResult.forEach(item -> {
            JsonObjectBuilder rowBuilder = Json.createObjectBuilder();
            //Add value for each column
            columnsToInclude.forEach(key -> {
                Object value = item.get(key);
                if (value == null) {
                    rowBuilder.addNull(key);
                } else if (value instanceof Integer) {
                    rowBuilder.add(key, (Integer) value);
                } else if (value instanceof Double) {
                    rowBuilder.add(key, (Double) value);
                } else if (value instanceof Date) {
                    //Convert to target timezone
                    Date time = (Date) value;
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(time);
                    rowBuilder.add(
                        key, dateFormat.format(calendar.getTime()));
                } else {
                    rowBuilder.add(key, value.toString());
                }
            });
            //Append id
            if (subDataKey != null
                && item.containsKey(subDataKey)
                && item.get(subDataKey) instanceof List<?>
            ) {
                List<Map<String, Object>> subData =
                    (List<Map<String, Object>>) item.get(subDataKey);
                rowBuilder.add(subDataKey, createSubdataArray(subData));
            }
            builder.add(item.get(idColumn).toString(), rowBuilder);
        });
        return new ByteArrayInputStream(
            builder.build().toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Create a json array from a list of sub data maps.
     * @param subData Sub data as list of maps
     * @return Json array
     */
    private JsonArray createSubdataArray(List<Map<String, Object>> subData) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        subData.forEach(map -> {
            JsonObjectBuilder itemBuilder = Json.createObjectBuilder();
            map.forEach((key, value) -> {
                if (value == null) {
                    itemBuilder.add(key, JsonValue.NULL);
                    return;
                }
                if (value instanceof Integer) {
                    itemBuilder.add(key, (Integer) value);
                } else if (value instanceof Double) {
                    itemBuilder.add(key, (Double) value);
                } else {
                    itemBuilder.add(key, value.toString());
                }
            });
            arrayBuilder.add(itemBuilder.build());
        });
        return arrayBuilder.build();
    }

    /**
     * Export Sample objects as JSON.
     * @param probeIds List of Sample IDs to export.
     * @param messungsIds Ignored. All associated Messung objects are exported.
     * @param encoding Ignored. Result is always UTF_8.
     * @param userInfo UserInfo
     * @return Export result as InputStream or null if the export failed
     */
    @Override
    public InputStream exportProben(
        List<Integer> probeIds,
        List<Integer> messungsIds,
        Charset encoding,
        UserInfo userInfo
    ) {
        //Create json.
        String json = createJsonString(probeIds, userInfo);
        if (json == null) {
            return null;
        }

        InputStream in = new ByteArrayInputStream(
            json.getBytes(StandardCharsets.UTF_8));
        try {
            in.close();
        } catch (IOException e) {
            logger.debug("Error while closing Stream.", e);
            return null;
        }
        return in;
    }

    /**
     * Export Messungen and associated Proben context.
     * @param probeIds ignored.
     * @param messungsIds List of Messungs IDs to export.
     * @param encoding Ignored. Result is always UTF_8.
     * @param userInfo UserInfo
     * @return Export result as InputStream or null if the export failed
     */
    @Override
    public InputStream exportMessungen(
        List<Integer> probeIds,
        List<Integer> messungsIds,
        Charset encoding,
        UserInfo userInfo
    ) {
        QueryBuilder<Measm> builder = repository.queryBuilder(Measm.class);
        for (Integer id : messungsIds) {
            builder.or(Measm_.id, id);
        }
        List<Measm> messungen =
            repository.filter(builder.getQuery());
        if (messungen.isEmpty()) {
            return null;
        }
        final ObjectMapper mapper = createObjectMapper();
        ArrayNode json = mapper.createArrayNode();
        for (Measm m : messungen) {
            Sample p = repository.getById(
                Sample.class,
                m.getSampleId()
            );
            try {
                String tmp = mapper.writeValueAsString(p);
                JsonNode jsProbe = mapper.readTree(tmp);
                addProbeninfo(jsProbe);
                tmp = mapper.writeValueAsString(m);
                JsonNode jsMessung = mapper.readTree(tmp);
                Mmt mmt = repository.getById(
                    Mmt.class,
                    m.getMmtId()
                );
                ((ObjectNode) jsMessung).put("mmt", mmt == null ?
                    "" : mmt.getName());
                addMesswerte(jsMessung);
                addMessungsKommentare(jsMessung);
                addStatusProtokoll(jsMessung);
                ArrayNode mArry = mapper.createArrayNode();
                mArry.add(jsMessung);
                ((ObjectNode) jsProbe).set("measms", mArry);
                addKommentare(jsProbe);
                addZusatzwerte(jsProbe);
                addDeskriptoren(jsProbe);
                addOrtszuordung(jsProbe);
                addMessstelle(jsProbe);
                json.add(jsProbe);
            } catch (IOException e) {
                logger.debug("Error parsing object structure.", e);
                return null;
            }
        }
        InputStream in = new ByteArrayInputStream(
            json.toString().getBytes(StandardCharsets.UTF_8));
        try {
            in.close();
        } catch (IOException e) {
            logger.debug("Error while closing Stream.", e);
            return null;
        }
        return in;
    }

    private String createJsonString(List<Integer> probeIds, UserInfo userInfo) {
        QueryBuilder<Sample> builder = repository.queryBuilder(Sample.class);
        for (Integer id : probeIds) {
            builder.or(Sample_.id, id);
        }
        List<Sample> proben =
            repository.filter(builder.getQuery());
        final ObjectMapper mapper = createObjectMapper();
        try {
            String tmp = mapper.writeValueAsString(proben);
            JsonNode nodes = mapper.readTree(tmp);
            for (int i = 0; i < nodes.size(); i++) {
                addProbeninfo(nodes.get(i));
                addMessungen(nodes.get(i));
                addKommentare(nodes.get(i));
                addZusatzwerte(nodes.get(i));
                addDeskriptoren(nodes.get(i));
                addOrtszuordung(nodes.get(i));
                addMessstelle(nodes.get(i));
            }
            return mapper.writeValueAsString(nodes);
        } catch (IOException e) {
            logger.debug("Error parsing object structure.", e);
            return null;
        }
    }

    private void addProbeninfo(JsonNode node) {
        ObjectNode probe = (ObjectNode) node;
        SampleMeth art = repository.getById(
            SampleMeth.class,
            probe.get("sampleMethId").asInt()
        );
        Regulation datenbasis = repository.getById(
            Regulation.class,
            probe.get("regulationId").asInt()
        );
        EnvMedium umw = repository.getById(
            EnvMedium.class,
            probe.get("envMediumId").asText()
        );
        probe.put("sampleMethExtId",
            art == null ? "" : art.getExtId());
        probe.put("regulation",
            datenbasis == null ? "" : datenbasis.getName());
        probe.put("envMediumName", umw == null ? "" : umw.getName());
        if (probe.get("oprModeId").asInt() != 0) {
            OprMode ba = repository.getById(
                OprMode.class,
                probe.get("oprModeId").asInt()
            );
            probe.put("oprModeName", ba.getName());
        }
        if (probe.get("mpgCategId").asInt() != 0) {
            MpgCateg mpl = repository.getById(
                MpgCateg.class,
                probe.get("mpgCategId").asInt()
            );
            probe.put("mpgCategExtId", mpl.getExtId());
            probe.put("mpgCategName", mpl.getName());
        }
        if (probe.get("samplerId").asInt() != 0) {
            Sampler probenehmer = repository.getById(
                Sampler.class,
                probe.get("samplerId").asInt()
            );
            probe.put("samplerExtId", probenehmer.getExtId());
            probe.put("samplerDescr", probenehmer.getDescr());
            probe.put("samplerNetworkId", probenehmer.getNetworkId());
            probe.put(
                "samplerShortText", probenehmer.getShortText());
        }
        if (probe.get("reiAgGrId").asInt() != 0) {
            ReiAgGr reiAgGr = repository.getById(
                ReiAgGr.class,
                probe.get("reiAgGrId").asInt()
            );
            probe.put("reiAgGrDescr", reiAgGr.getDescr());
            probe.put("reiAgGrName", reiAgGr.getName());
        }
    }

    private void addMessstelle(JsonNode node) {
        MeasFacil messstelle = repository.getById(
            MeasFacil.class,
            node.get("measFacilId").asText()
        );
        MeasFacil laborMessstelle = repository.getById(
            MeasFacil.class,
            node.get("apprLabId").asText()
        );
        final ObjectMapper mapper = createObjectMapper();
        try {
            String tmp = mapper.writeValueAsString(messstelle);
            String tmp2 = mapper.writeValueAsString(laborMessstelle);
            JsonNode nodes = mapper.readTree(tmp);
            JsonNode nodes2 = mapper.readTree(tmp2);
            ((ObjectNode) node).set("measFacil", nodes);
            ((ObjectNode) node).set("apprLab", nodes2);
        } catch (IOException e) {
            logger.debug("Could not export Messstelle for Sample "
                + node.get("externeProbeId").asText());
        }
    }

    private void addMessungen(JsonNode probe) {
        QueryBuilder<Measm> builder = repository.queryBuilder(Measm.class);
        builder.and(Measm_.id, probe.get("id").asInt());
        List<Measm> messungen =
            repository.filter(builder.getQuery());
        final ObjectMapper mapper = createObjectMapper();
        try {
            String tmp = mapper.writeValueAsString(messungen);
            JsonNode nodes = mapper.readTree(tmp);
            for (int i = 0; i < nodes.size(); i++) {
                Mmt mmt = repository.getById(
                    Mmt.class,
                    nodes.get(i).get("mmtId").asText()
                );
                ((ObjectNode) nodes.get(i)).put("mmt",
                    mmt == null ? "" : mmt.getName());
                addMesswerte(nodes.get(i));
                addMessungsKommentare(nodes.get(i));
                addStatusProtokoll(nodes.get(i));
            }
            ((ObjectNode) probe).set("measms", nodes);
        } catch (IOException e) {
            logger.debug("Could not export Messungen for Sample "
                + probe.get("externeProbeId").asText());
        }
    }

    private void addKommentare(JsonNode probe) {
        QueryBuilder<CommSample> builder =
            repository.queryBuilder(CommSample.class);
        builder.and(CommSample_.id, probe.get("id").asInt());
        List<CommSample> kommentare =
            repository.filter(builder.getQuery());
        final ObjectMapper mapper = createObjectMapper();
        try {
            String tmp = mapper.writeValueAsString(kommentare);
            JsonNode nodes = mapper.readTree(tmp);
            for (int i = 0; i < nodes.size(); i++) {
                MeasFacil mst = repository.getById(
                    MeasFacil.class,
                    nodes.get(i).get("measFacilId").asText()
                );
                ((ObjectNode) nodes.get(i)).put(
                    "measFacil",
                    mst.getName());
            }
            ((ObjectNode) probe).set("commSamples", nodes);
        } catch (IOException e) {
            logger.debug("Could not export Kommentare for Sample "
                + probe.get("externeProbeId").asText());
        }
    }

    private void addZusatzwerte(JsonNode probe) {
        QueryBuilder<SampleSpecifMeasVal> builder =
            repository.queryBuilder(SampleSpecifMeasVal.class);
        builder.and(SampleSpecifMeasVal_.sampleId, probe.get("id").asInt());
        List<SampleSpecifMeasVal> zusatzwerte =
            repository.filter(builder.getQuery());
        final ObjectMapper mapper = createObjectMapper();
        try {
            String tmp = mapper.writeValueAsString(zusatzwerte);
            JsonNode nodes = mapper.readTree(tmp);
            for (int i = 0; i < nodes.size(); i++) {
                SampleSpecif pz = repository.getById(
                    SampleSpecif.class,
                    nodes.get(i).get("sampleSpecifId").asText()
                );
                ((ObjectNode) nodes.get(i)).put(
                    "sampleSpecifName", pz.getName());
                Integer mehId = pz.getMeasUnitId();
                if (mehId != null) {
                MeasUnit meh = repository.getById(
                    MeasUnit.class, mehId);
                ((ObjectNode) nodes.get(i)).put(
                    "unit", meh.getUnitSymbol());
                } else {
                    continue;
                }
            }
            ((ObjectNode) probe).set("sampleSpecifMeasVals", nodes);
        } catch (IOException e) {
            logger.debug("Could not export Zusatzwerte for Sample "
                + probe.get("externeProbeId").asText());
        }
    }

    private void addDeskriptoren(JsonNode probe) {
        String desk = probe.get("envDescripDisplay").asText();
        String[] parts = desk.split(" ");
        if (parts.length <= 1) {
            return;
        }

        int vorgaenger = 0;
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
        boolean isZebs = Integer.parseInt(parts[1]) == 1;
        int hdV = 0;
        int ndV = 0;
        for (int i = 0; i < parts.length - 1; i++) {
            QueryBuilder<EnvDescrip> builder =
                repository.queryBuilder(EnvDescrip.class);
            String beschreibung = "";
            if (Integer.parseInt(parts[i + 1]) != 0) {
                builder.and(EnvDescrip_.lev, i);
                builder.and(EnvDescrip_.levVal, Integer.parseInt(parts[i + 1]));
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
            node.put("S" + i, beschreibung);
            builder = builder.getEmptyBuilder();
        }
        ((ObjectNode) probe).set("envDescrip", node);
    }

    private void addMesswerte(JsonNode node) {
        QueryBuilder<MeasVal> builder =
            repository.queryBuilder(MeasVal.class);
        builder.and(MeasVal_.measmId, node.get("id").asInt());
        List<MeasVal> messwerte =
            repository.filter(builder.getQuery());
        final ObjectMapper mapper = createObjectMapper();
        try {
            String tmp = mapper.writeValueAsString(messwerte);
            JsonNode nodes = mapper.readTree(tmp);
            for (int i = 0; i < nodes.size(); i++) {
                MeasUnit meh = repository.getById(
                    MeasUnit.class,
                    nodes.get(i).get("measUnitId").asInt()
                );
                ((ObjectNode) nodes.get(i)).put("unit",
                    meh == null ? "" : meh.getUnitSymbol());
                Measd mg = repository.getById(
                    Measd.class,
                    nodes.get(i).get("measdId").asInt()
                );
                ((ObjectNode) nodes.get(i)).put("measd",
                    mg == null ? "" : mg.getName());
            }
            ((ObjectNode) node).set("measVals", nodes);
        } catch (IOException e) {
            logger.debug("Could not export Messwerte for Messung "
                + node.get("nebenprobenNr").asText());
        }
    }

    private void addMessungsKommentare(JsonNode node) {
        QueryBuilder<CommMeasm> builder =
            repository.queryBuilder(CommMeasm.class);
        builder.and(CommMeasm_.measmId, node.get("id").asInt());
        List<CommMeasm> kommentare =
            repository.filter(builder.getQuery());
        final ObjectMapper mapper = createObjectMapper();
        try {
            String tmp = mapper.writeValueAsString(kommentare);
            JsonNode nodes = mapper.readTree(tmp);
            for (int i = 0; i < nodes.size(); i++) {
                MeasFacil mst = repository.getById(
                    MeasFacil.class,
                    nodes.get(i).get("measFacilId").asText()
                );
                ((ObjectNode) nodes.get(i)).put(
                    "measFacil",
                    mst.getName());
            }
            ((ObjectNode) node).set("commMeasms", nodes);
        } catch (IOException e) {
            logger.debug("Could not export Kommentare for Messung "
                + node.get("nebenprobenNr").asText());
        }
    }

    private void addStatusProtokoll(JsonNode node) {
        QueryBuilder<StatusProt> builder =
            repository.queryBuilder(StatusProt.class);
        builder.and(StatusProt_.measmId, node.get("id").asInt());
        List<StatusProt> status =
            repository.filter(builder.getQuery());
        final ObjectMapper mapper = createObjectMapper();
        try {
            String tmp = mapper.writeValueAsString(status);
            JsonNode nodes = mapper.readTree(tmp);
            for (int i = 0; i < nodes.size(); i++) {
                StatusMp kombi = repository.getById(
                    StatusMp.class,
                    nodes.get(i).get("statusMpId").asInt()
                );
                ((ObjectNode) nodes.get(i)).put(
                    "statusLev",
                    kombi.getStatusLev().getLev());
                ((ObjectNode) nodes.get(i)).put(
                    "statusVal",
                    kombi.getStatusVal().getVal());
                MeasFacil mst = repository.getById(
                    MeasFacil.class,
                    nodes.get(i).get("measFacilId").asText()
                );
                ((ObjectNode) nodes.get(i)).put(
                    "measFacil",
                    mst.getName());
            }
            ((ObjectNode) node).set("statusProtocol", nodes);
        } catch (IOException e) {
            logger.debug("Could not export Statusprotokoll for Messung "
                + node.get("nebenprobenNr").asText());
        }
    }

    private void addOrtszuordung(JsonNode node) {
        QueryBuilder<Geolocat> builder =
            repository.queryBuilder(Geolocat.class);
        builder.and(Geolocat_.sampleId, node.get("id").asInt());
        List<Geolocat> ortszuordnung =
            repository.filter(builder.getQuery());
        final ObjectMapper mapper = createObjectMapper();
        try {
            String tmp = mapper.writeValueAsString(ortszuordnung);
            JsonNode nodes = mapper.readTree(tmp);
            for (int i = 0; i < nodes.size(); i++) {
                addOrt(nodes.get(i));
            }
            ((ObjectNode) node).set("geolocat", nodes);
        } catch (IOException e) {
            logger.debug("Could not export Ortszuordnugen for Sample "
                + node.get("externeProbeId").asText());
        }
    }

    private void addOrt(JsonNode node) {
        Site ort = repository.getById(
                Site.class, node.get("siteId").asInt());
        Jsonb ortJsonb = JsonbBuilder.create();
        String tmp = ortJsonb.toJson(ort);
        final ObjectMapper mapper = createObjectMapper();
        try {
            JsonNode oNode = mapper.readTree(tmp);

            final String gemIdKey = "adminUnitId";
            if (oNode.hasNonNull(gemIdKey)) {
                AdminUnit ve = repository.getById(
                    AdminUnit.class,
                    oNode.get(gemIdKey).asText()
                );
                ((ObjectNode) oNode).put("adminUnit",
                    ve == null ? "" : ve.getName());
            }

            final String staatIdKey = "stateId";
            if (oNode.hasNonNull(staatIdKey)) {
                State staat = repository.getById(
                    State.class,
                    oNode.get(staatIdKey).asInt()
                );
                ((ObjectNode) oNode).put("state",
                    staat == null ? "" : staat.getCtry());
            }

            ((ObjectNode) node).set("site", oNode);
        } catch (IOException e) {
            logger.debug("Could not export Ort for Ortszuordnung "
                + node.get("id").asText());
            logger.debug(e);
        }
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat(JSON_DATE_FORMAT));
        return mapper;
    }
}
