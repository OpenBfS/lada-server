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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.jboss.logging.Logger;

import de.intevation.lada.exporter.ExportConfig;
import de.intevation.lada.exporter.ExportFormat;
import de.intevation.lada.exporter.Exporter;
import de.intevation.lada.model.land.CommMeasm;
import de.intevation.lada.model.land.CommSample;
import de.intevation.lada.model.land.Measm;
import de.intevation.lada.model.land.MeasVal;
import de.intevation.lada.model.land.Geolocat;
import de.intevation.lada.model.land.Sample;
import de.intevation.lada.model.land.StatusProtokoll;
import de.intevation.lada.model.land.ZusatzWert;
import de.intevation.lada.model.master.AdminUnit;
import de.intevation.lada.model.master.EnvDescrip;
import de.intevation.lada.model.master.EnvMedium;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.MeasUnit;
import de.intevation.lada.model.master.Measd;
import de.intevation.lada.model.master.Mmt;
import de.intevation.lada.model.master.MpgCateg;
import de.intevation.lada.model.master.OprMode;
import de.intevation.lada.model.master.Regulation;
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
     *          <li> id: Name of the id column, mandatory </li>
     *          <li> subData: key of the subData json object, optional </li>
     *          <li> timezone: Target timezone for timestamp conversion </li>
     *        </ul>
     *
     * @param columnsToInclude List of column names to include in the export.
     *                         If not set, all columns will be exported
     * @return Export result as input stream or null if the export failed
     */
    @Override
    @SuppressWarnings("unchecked")
    public InputStream export(
        List<Map<String, Object>> queryResult,
        Charset encoding,
        JsonObject options,
        ArrayList<String> columnsToInclude,
        Integer qId
    ) {
        String subDataKey = options.getString("subData", "");

        final JsonObjectBuilder builder = Json.createObjectBuilder();
        final String timezone =
            options.containsKey("timezone")
            ? options.getString("timezone") : "UTC";
        String idColumn = options.getString("id");

        //For each result
        queryResult.forEach(item -> {
            JsonObjectBuilder rowBuilder = Json.createObjectBuilder();
            //Add value for each column
            columnsToInclude.forEach(key -> {
                Object value = item.getOrDefault(key, null);
                if (value == null) {
                    rowBuilder.add(key, JsonValue.NULL);
                    return;
                }
                if (value instanceof Integer) {
                    rowBuilder.add(key, (Integer) value);
                } else if (value instanceof Double) {
                    rowBuilder.add(key, (Double) value);
                } else if (value instanceof Timestamp) {
                    //Convert to target timezone
                    Timestamp time = (Timestamp) value;
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date(time.getTime()));
                    SimpleDateFormat sdf =
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    sdf.setTimeZone(TimeZone.getTimeZone(timezone));
                    rowBuilder.add(key, sdf.format(calendar.getTime()));
                } else {
                    rowBuilder.add(key, value.toString());
                }
            });
            //Append id
            if (!subDataKey.isEmpty()
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
     * @param proben List of Sample IDs to export.
     * @param messungen Ignored. All associated Messung objects are exported.
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
            builder.or("id", id);
        }
        List<Measm> messungen =
            repository.filterPlain(builder.getQuery());
        if (messungen.isEmpty()) {
            return null;
        }
        final ObjectMapper mapper = new ObjectMapper();
        ArrayNode json = mapper.createArrayNode();
        for (Measm m : messungen) {
            Sample p = repository.getByIdPlain(
                Sample.class,
                m.getSampleId()
            );
            try {
                String tmp = mapper.writeValueAsString(p);
                JsonNode jsProbe = mapper.readTree(tmp);
                addProbeninfo(jsProbe);
                tmp = mapper.writeValueAsString(m);
                JsonNode jsMessung = mapper.readTree(tmp);
                Mmt mmt = repository.getByIdPlain(
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
                ((ObjectNode) jsProbe).set("messungen", mArry);
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
            builder.or("id", id);
        }
        List<Sample> proben =
            repository.filterPlain(builder.getQuery());
        final ObjectMapper mapper = new ObjectMapper();
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
        SampleMeth art = repository.getByIdPlain(
            SampleMeth.class,
            probe.get("probenartId").asInt()
        );
        Regulation datenbasis = repository.getByIdPlain(
            Regulation.class,
            probe.get("datenbasisId").asInt()
        );
        EnvMedium umw = repository.getByIdPlain(
            EnvMedium.class,
            probe.get("umwId").asText()
        );
        probe.put("probenart",
            art == null ? "" : art.getExtId());
        probe.put("datenbasis",
            datenbasis == null ? "" : datenbasis.getRegulation());
        probe.put("umw", umw == null ? "" : umw.getName());
        if (probe.get("baId").asInt() != 0) {
            OprMode ba = repository.getByIdPlain(
                OprMode.class,
                probe.get("baId").asInt()
            );
            probe.put("messRegime", ba.getName());
        }
        if (probe.get("mplId").asInt() != 0) {
            MpgCateg mpl = repository.getByIdPlain(
                MpgCateg.class,
                probe.get("mplId").asInt()
            );
            probe.put("mplCode", mpl.getExtId());
            probe.put("mpl", mpl.getName());
        }
        if (probe.get("probeNehmerId").asInt() != 0) {
            Sampler probenehmer = repository.getByIdPlain(
                Sampler.class,
                probe.get("probeNehmerId").asInt()
            );
            probe.put("prnId", probenehmer.getExtId());
            probe.put("prnBezeichnung", probenehmer.getDescr());
            probe.put(
                "prnKurzBezeichnung", probenehmer.getShortText());
        }
    }

    private void addMessstelle(JsonNode node) {
        MeasFacil messstelle = repository.getByIdPlain(
            MeasFacil.class,
            node.get("mstId").asText()
        );
        MeasFacil laborMessstelle = repository.getByIdPlain(
            MeasFacil.class,
            node.get("laborMstId").asText()
        );
        final ObjectMapper mapper = new ObjectMapper();
        try {
            String tmp = mapper.writeValueAsString(messstelle);
            String tmp2 = mapper.writeValueAsString(laborMessstelle);
            JsonNode nodes = mapper.readTree(tmp);
            JsonNode nodes2 = mapper.readTree(tmp2);
            ((ObjectNode) node).set("messstelle", nodes);
            ((ObjectNode) node).set("labormessstelle", nodes2);
        } catch (IOException e) {
            logger.debug("Could not export Messstelle for Sample "
                + node.get("externeProbeId").asText());
        }
    }

    private void addMessungen(JsonNode probe) {
        QueryBuilder<Measm> builder = repository.queryBuilder(Measm.class);
        builder.and("sampleId", probe.get("id").asInt());
        List<Measm> messungen =
            repository.filterPlain(builder.getQuery());
        final ObjectMapper mapper = new ObjectMapper();
        try {
            String tmp = mapper.writeValueAsString(messungen);
            JsonNode nodes = mapper.readTree(tmp);
            for (int i = 0; i < nodes.size(); i++) {
                Mmt mmt = repository.getByIdPlain(
                    Mmt.class,
                    nodes.get(i).get("mmtId").asText()
                );
                ((ObjectNode) nodes.get(i)).put("mmt",
                    mmt == null ? "" : mmt.getName());
                addMesswerte(nodes.get(i));
                addMessungsKommentare(nodes.get(i));
                addStatusProtokoll(nodes.get(i));
            }
            ((ObjectNode) probe).set("messungen", nodes);
        } catch (IOException e) {
            logger.debug("Could not export Messungen for Sample "
                + probe.get("externeProbeId").asText());
        }
    }

    private void addKommentare(JsonNode probe) {
        QueryBuilder<CommSample> builder =
            repository.queryBuilder(CommSample.class);
        builder.and("sampleId", probe.get("id").asInt());
        List<CommSample> kommentare =
            repository.filterPlain(builder.getQuery());
        final ObjectMapper mapper = new ObjectMapper();
        try {
            String tmp = mapper.writeValueAsString(kommentare);
            JsonNode nodes = mapper.readTree(tmp);
            for (int i = 0; i < nodes.size(); i++) {
                MeasFacil mst = repository.getByIdPlain(
                    MeasFacil.class,
                    nodes.get(i).get("mstId").asText()
                );
                ((ObjectNode) nodes.get(i)).put(
                    "mst",
                    mst.getName());
            }
            ((ObjectNode) probe).set("kommentare", nodes);
        } catch (IOException e) {
            logger.debug("Could not export Kommentare for Sample "
                + probe.get("externeProbeId").asText());
        }
    }

    private void addZusatzwerte(JsonNode probe) {
        QueryBuilder<ZusatzWert> builder =
            repository.queryBuilder(ZusatzWert.class);
        builder.and("probeId", probe.get("id").asInt());
        List<ZusatzWert> zusatzwerte =
            repository.filterPlain(builder.getQuery());
        final ObjectMapper mapper = new ObjectMapper();
        try {
            String tmp = mapper.writeValueAsString(zusatzwerte);
            JsonNode nodes = mapper.readTree(tmp);
            for (int i = 0; i < nodes.size(); i++) {
                SampleSpecif pz = repository.getByIdPlain(
                    SampleSpecif.class,
                    nodes.get(i).get("pzsId").asText()
                );
                ((ObjectNode) nodes.get(i)).put(
                    "pzwGroesse", pz.getName());
                Integer mehId = pz.getUnitId();
                if (mehId != null) {
                MeasUnit meh = repository.getByIdPlain(
                    MeasUnit.class, mehId);
                ((ObjectNode) nodes.get(i)).put(
                    "meh", meh.getUnitSymbol());
                } else {
                    continue;
                }
            }
            ((ObjectNode) probe).set("zusatzwerte", nodes);
        } catch (IOException e) {
            logger.debug("Could not export Zusatzwerte for Sample "
                + probe.get("externeProbeId").asText());
        }
    }

    private void addDeskriptoren(JsonNode probe) {
        String desk = probe.get("mediaDesk").asText();
        String[] parts = desk.split(" ");
        if (parts.length <= 1) {
            return;
        }

        QueryBuilder<EnvDescrip> builder =
            repository.queryBuilder(EnvDescrip.class);
        int vorgaenger = 0;
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
        boolean isZebs = Integer.parseInt(parts[1]) == 1;
        int hdV = 0;
        int ndV = 0;
        for (int i = 0; i < parts.length - 1; i++) {
            String beschreibung = "";
            if (Integer.parseInt(parts[i + 1]) != 0) {
                builder.and("lev", i);
                builder.and("levVal", Integer.parseInt(parts[i + 1]));
                if (i != 0) {
                    builder.and("predId", vorgaenger);
                }
                List<EnvDescrip> found =
                    repository.filterPlain(builder.getQuery());
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
        ((ObjectNode) probe).set("deskriptoren", node);
    }

    private void addMesswerte(JsonNode node) {
        QueryBuilder<MeasVal> builder =
            repository.queryBuilder(MeasVal.class);
        builder.and("measmId", node.get("id").asInt());
        List<MeasVal> messwerte =
            repository.filterPlain(builder.getQuery());
        final ObjectMapper mapper = new ObjectMapper();
        try {
            String tmp = mapper.writeValueAsString(messwerte);
            JsonNode nodes = mapper.readTree(tmp);
            for (int i = 0; i < nodes.size(); i++) {
                MeasUnit meh = repository.getByIdPlain(
                    MeasUnit.class,
                    nodes.get(i).get("mehId").asInt()
                );
                ((ObjectNode) nodes.get(i)).put("meh",
                    meh == null ? "" : meh.getUnitSymbol());
                Measd mg = repository.getByIdPlain(
                    Measd.class,
                    nodes.get(i).get("messgroesseId").asInt()
                );
                ((ObjectNode) nodes.get(i)).put("messgroesse",
                    mg == null ? "" : mg.getName());
            }
            ((ObjectNode) node).set("messwerte", nodes);
        } catch (IOException e) {
            logger.debug("Could not export Messwerte for Messung "
                + node.get("nebenprobenNr").asText());
        }
    }

    private void addMessungsKommentare(JsonNode node) {
        QueryBuilder<CommMeasm> builder =
            repository.queryBuilder(CommMeasm.class);
        builder.and("measmId", node.get("id").asInt());
        List<CommMeasm> kommentare =
            repository.filterPlain(builder.getQuery());
        final ObjectMapper mapper = new ObjectMapper();
        try {
            String tmp = mapper.writeValueAsString(kommentare);
            JsonNode nodes = mapper.readTree(tmp);
            for (int i = 0; i < nodes.size(); i++) {
                MeasFacil mst = repository.getByIdPlain(
                    MeasFacil.class,
                    nodes.get(i).get("mstId").asText()
                );
                ((ObjectNode) nodes.get(i)).put(
                    "mst",
                    mst.getName());
            }
            ((ObjectNode) node).set("kommentare", nodes);
        } catch (IOException e) {
            logger.debug("Could not export Kommentare for Messung "
                + node.get("nebenprobenNr").asText());
        }
    }

    private void addStatusProtokoll(JsonNode node) {
        QueryBuilder<StatusProtokoll> builder =
            repository.queryBuilder(StatusProtokoll.class);
        builder.and("measmId", node.get("id").asInt());
        List<StatusProtokoll> status =
            repository.filterPlain(builder.getQuery());
        final ObjectMapper mapper = new ObjectMapper();
        try {
            String tmp = mapper.writeValueAsString(status);
            JsonNode nodes = mapper.readTree(tmp);
            for (int i = 0; i < nodes.size(); i++) {
                StatusMp kombi = repository.getByIdPlain(
                    StatusMp.class,
                    nodes.get(i).get("statusKombi").asInt()
                );
                ((ObjectNode) nodes.get(i)).put(
                    "statusStufe",
                    kombi.getStatusLev().getLev());
                ((ObjectNode) nodes.get(i)).put(
                    "statusWert",
                    kombi.getStatusVal().getVal());
                MeasFacil mst = repository.getByIdPlain(
                    MeasFacil.class,
                    nodes.get(i).get("mstId").asText()
                );
                ((ObjectNode) nodes.get(i)).put(
                    "mst",
                    mst.getName());
            }
            ((ObjectNode) node).set("statusprotokoll", nodes);
        } catch (IOException e) {
            logger.debug("Could not export Statusprotokoll for Messung "
                + node.get("nebenprobenNr").asText());
        }
    }

    private void addOrtszuordung(JsonNode node) {
        QueryBuilder<Geolocat> builder =
            repository.queryBuilder(Geolocat.class);
        builder.and("sampleId", node.get("id").asInt());
        List<Geolocat> ortszuordnung =
            repository.filterPlain(builder.getQuery());
        final ObjectMapper mapper = new ObjectMapper();
        try {
            String tmp = mapper.writeValueAsString(ortszuordnung);
            JsonNode nodes = mapper.readTree(tmp);
            for (int i = 0; i < nodes.size(); i++) {
                addOrt(nodes.get(i));
            }
            ((ObjectNode) node).set("ortszuordnung", nodes);
        } catch (IOException e) {
            logger.debug("Could not export Ortszuordnugen for Sample "
                + node.get("externeProbeId").asText());
        }
    }

    private void addOrt(JsonNode node) {
        Site ort = repository.getByIdPlain(
                Site.class, node.get("ortId").asInt());
        Jsonb ortJsonb = JsonbBuilder.create();
        String tmp = ortJsonb.toJson(ort);
        final ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode oNode = mapper.readTree(tmp);

            final String gemIdKey = "gemId";
            if (oNode.hasNonNull(gemIdKey)) {
                AdminUnit ve = repository.getByIdPlain(
                    AdminUnit.class,
                    oNode.get(gemIdKey).asText()
                );
                ((ObjectNode) oNode).put("gem",
                    ve == null ? "" : ve.getName());
            }

            final String staatIdKey = "staatId";
            if (oNode.hasNonNull(staatIdKey)) {
                State staat = repository.getByIdPlain(
                    State.class,
                    oNode.get(staatIdKey).asInt()
                );
                ((ObjectNode) oNode).put("staat",
                    staat == null ? "" : staat.getCtry());
            }

            ((ObjectNode) node).set("ort", oNode);
        } catch (IOException e) {
            logger.debug("Could not export Ort for Ortszuordnung "
                + node.get("id").asText());
            logger.debug(e);
        }
    }
}
