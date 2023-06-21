/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import de.intevation.lada.model.lada.AuditTrailMeasmView;
import de.intevation.lada.model.lada.AuditTrailSampleView;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;

/**
 * REST service for AuditTrail.
 * <p>
 * The services produce data in the application/json media type.
 * All HTTP methods use the authorization module to determine if the user is
 * allowed to perform the requested action.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean];
 *  "message": [string],
 *  "data":[{
 *      "id": [number],
 *      "identifier: [string]
 *      "audit": [array]
 *  }],
 * }
 * </code>
 * </pre>
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Path("audit")
public class AuditTrailService extends LadaService {

    /**
     * Class to store tablename and value field for foreign key mappings.
     */
    private class TableMapper {
        private String mappingTable;
        private String valueField;

        TableMapper(
            String mTable,
            String vField
        ) {
            this.mappingTable = mTable;
            this.valueField = vField;
        }

        public String getMappingTable() {
            return mappingTable;
        }

        public String getValueField() {
            return valueField;
        }
    }

    /**
     * The data repository granting read/write access.
     */
    @Inject
    private Repository repository;

    /**
     * The authorization module.
     */
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

    /**
     * Map foreign key to their associated table and the display value.
     */
    private Map<String, TableMapper> mappings;

    /**
     * Initialize the object with key <-> table mappings.
     */
    @PostConstruct
    public void initialize() {
        mappings = new HashMap<String, TableMapper>();
        mappings.put("messgroesse_id",
            new TableMapper("messgroesse", "messgroesse"));
        mappings.put("meh_id",
            new TableMapper("mess_einheit", "einheit"));
        mappings.put("ort_id",
            new TableMapper("ort", "ort_id"));
        mappings.put("datenbasis_id",
            new TableMapper("datenbasis", "datenbasis"));
        mappings.put("ba_id",
            new TableMapper("betriebsart", "name"));
        mappings.put("mpl_id",
            new TableMapper("messprogramm_kategorie", "code"));
        mappings.put("probenart_id",
            new TableMapper("probenart", "probenart"));
        mappings.put("probe_nehmer_id",
            new TableMapper("probenehmer", "prn_id"));
        mappings.put("probeentnahme_beginn",
            new TableMapper("date", "dd.MM.yy HH:mm"));
        mappings.put("probeentnahme_ende",
            new TableMapper("date", "dd.MM.yy HH:mm"));
        mappings.put("solldatum_beginn",
                new TableMapper("date", "dd.MM.yy HH:mm"));
        mappings.put("solldatum_ende",
                new TableMapper("date", "dd.MM.yy HH:mm"));
        mappings.put("mitte_sammelzeitraum",
                new TableMapper("date", "dd.MM.yy HH:mm"));
        mappings.put("messzeitpunkt",
                new TableMapper("date", "dd.MM.yy HH:mm"));
        mappings.put("kta_gruppe_id",
            new TableMapper("kta_gruppe", "kta_gruppe"));
        mappings.put("rei_progpunkt_grp_id",
            new TableMapper("rei_progpunkt_gruppe", "rei_prog_punkt_gruppe"));
    }

    /**
     * Service to generate audit trail for sample objects.
     *
     * @param pId ID of sample given in URL path.
     * @return Response with audit trail data for requested sample.
     */
    @GET
    @Path("probe/{id}")
    public Response getProbe(
        @PathParam("id") Integer pId
    ) {
        // Get the plain probe object to have the hauptproben_nr.
        Sample probe = repository.getByIdPlain(Sample.class, pId);
        if (probe == null) {
            return new Response(false, StatusCodes.NOT_EXISTING, null);
        }

        UserInfo userInfo = authorization.getInfo();

        //Get ort ids connected to this probe
        QueryBuilder<Geolocat> refBuilder =
            repository.queryBuilder(Geolocat.class);
        refBuilder.and("sampleId", pId);
        List<Integer> ortIds = new LinkedList<Integer>();
        for (Geolocat zuordnung
            : repository.filterPlain(refBuilder.getQuery())
        ) {
            ortIds.add(zuordnung.getSiteId());
        }

        // Get all entries for the probe and its sub objects.
        QueryBuilder<AuditTrailSampleView> builder =
            repository.queryBuilder(AuditTrailSampleView.class);
        builder.and("objectId", pId);
        builder.and("tableName", "sample");
        builder.or("sampleId", pId);
        if (ortIds.size() > 0) {
            builder.orIn("siteId", ortIds);
        }
        builder.orderBy("tstamp", true);
        List<AuditTrailSampleView> audit =
            repository.filterPlain(builder.getQuery());

        AuditResponseData auditResponseData = new AuditResponseData();
        List<AuditEntry> entries = new ArrayList<>();
        auditResponseData.setId(probe.getId());
        auditResponseData.setIdentifier(
            (probe.getMainSampleId() == null)
            ? probe.getExtId()
            : probe.getMainSampleId()
        );
        for (AuditTrailSampleView a : audit) {
            //If audit entry shows a messwert, do not show if:
            // - StatusKombi is 1 (MST - nicht vergeben)
            // - User is not owner of the messung
            if (a.getTableName().equals("messwert")) {
                Measm messung =
                    repository.getByIdPlain(
                        Measm.class, a.getMeasmId());
                if (messung != null) {
                    StatusProt status =
                    repository.getByIdPlain(
                        StatusProt.class, messung.getStatus());
                    if (status.getStatusMpId() == 1
                        && !userInfo.getMessstellen().contains(
                            probe.getMeasFacilId())
                    ) {
                        continue;
                    }
                } else {
                    continue;
                }
            }
            entries.add(createEntry(a));
        }
        auditResponseData.setAudit(entries);
        return new Response(
            true,
            StatusCodes.OK,
            auditResponseData);
    }

    /**
     * Convert AuditTrailSampleView to representation for response.
     *
     * @param audit The table entry
     * @return AuditEntry for response
     */
    private AuditEntry createEntry(
        AuditTrailSampleView audit
    ) {
        AuditEntry node = new AuditEntry();
        node.setTimestamp(audit.getTstamp().getTime());
        node.setType(audit.getTableName());
        node.setAction(audit.getAction());
        JsonStructure changedFields = Json.createReader(
            new StringReader(audit.getChangedFields().toString())).read();
        JsonStructure data = translateValues((JsonObject) changedFields);
        node.setChangedFields(data);
        if ("site".equals(audit.getTableName())) {
            node.setIdentifier(audit.getRowData().get("ext_id").toString());
        }
        if ("comm_sample".equals(audit.getTableName())) {
            node.setIdentifier(audit.getRowData().get("date").toString());
        }
        if ("sample_specif_meas_val".equals(audit.getTableName())) {
            node.setIdentifier(
                audit.getRowData().get("sample_specif_id").toString());
        }
        if ("geolocat".equals(audit.getTableName())) {
            String value = translateId(
                "site",
                "ext_id",
                audit.getRowData().get("site_id").toString(),
                "id",
                de.intevation.lada.model.master.SchemaName.NAME);
            node.setIdentifier(value);
        }
        if ("measm".equals(audit.getTableName())) {
            Measm m = repository.getByIdPlain(
                Measm.class, audit.getObjectId());
            node.setIdentifier(
                (m == null)
                ? "(deleted)"
                : (m.getMinSampleId() == null)
                    ? m.getExtId().toString()
                    : m.getMinSampleId()
                );
        }
        if (audit.getMeasmId() != null) {
            Measm m = repository.getByIdPlain(
                Measm.class, audit.getMeasmId());
            AuditEntryIdentifier identifier = new AuditEntryIdentifier();
            identifier.setMeasm(
                (m.getMinSampleId() == null)
                ? m.getExtId().toString() : m.getMinSampleId());
            if ("comm_measm".equals(audit.getTableName())) {
                identifier.setIdentifier(
                    audit.getRowData().get("date").toString());
            }
            if ("meas_val".equals(audit.getTableName())) {
                String value = translateId(
                    "measd",
                    "name",
                    audit.getRowData().get("measd_id").toString(),
                    "id",
                    de.intevation.lada.model.master.SchemaName.NAME);
                identifier.setIdentifier(value);
            }
            node.setIdentifier(identifier);
        }
        return node;
    }

    /**
     * Service to generate audit trail for measm objects.
     *
     * @param mId ID of measm given in URL path.
     * @return Response with audit trail data for requested measm.
     */
    @GET
    @Path("messung/{id}")
    public Response getMessung(
        @PathParam("id") Integer mId
    ) {
        Measm messung = repository.getByIdPlain(Measm.class, mId);
        if (messung == null) {
            return new Response(false, StatusCodes.NOT_EXISTING, null);
        }
        StatusProt status =
            repository.getByIdPlain(StatusProt.class, messung.getStatus());
        Sample probe =
            repository.getByIdPlain(Sample.class, messung.getSampleId());
        UserInfo userInfo = authorization.getInfo();
        QueryBuilder<AuditTrailMeasmView> builder =
            repository.queryBuilder(AuditTrailMeasmView.class);
        builder.and("objectId", mId);
        builder.and("tableName", "measm");
        builder.or("measmId", mId);
        builder.orderBy("tstamp", true);
        List<AuditTrailMeasmView> audit =
            repository.filterPlain(builder.getQuery());

        // Create an empty JsonObject
        AuditResponseData auditData = new AuditResponseData();
        List<AuditEntry> entries = new ArrayList<>();
        auditData.setId(messung.getId());
        auditData.setIdentifier(
            (messung.getMinSampleId() == null)
            ? messung.getExtId().toString()
            : messung.getMinSampleId()
        );
        for (AuditTrailMeasmView a : audit) {
            //If audit entry shows a messwert, do not show if:
            // - StatusKombi is 1 (MST - nicht vergeben)
            // - User is not owner of the messung
            if (a.getTableName().equals("messwert")
                    && status.getStatusMpId() == 1
                    && !userInfo.getMessstellen().contains(
                        probe.getMeasFacilId())) {
                continue;
            }
            entries.add(createEntry(a));

        }
        auditData.setAudit(entries);
        return new Response(
            true,
            StatusCodes.OK,
            auditData);
    }

    /**
     * Convert AuditTrailMeasmView to representation for response.
     *
     * @param audit The table entry
     * @return AuditEntry for response
     */
    private AuditEntry createEntry(
        AuditTrailMeasmView audit
    ) {
        AuditEntry node = new AuditEntry();
        node.setTimestamp(audit.getTstamp().getTime());
        node.setType(audit.getTableName());
        node.setAction(audit.getAction());
        node.setChangedFields(Json.createReader(new StringReader(
                    audit.getChangedFields().toString())).read());
        if ("comm_measm".equals(audit.getTableName())) {
            node.setIdentifier(audit.getRowData().get("date").toString());
        }
        if ("meas_val".equals(audit.getTableName())) {
            String value = translateId(
                "measd",
                "name",
                audit.getRowData().get("measd_id").toString(),
                "id",
                de.intevation.lada.model.master.SchemaName.NAME);
            node.setIdentifier(value);
        }
        return node;
    }

    /**
     * Translate a foreign key into the associated value.
     */
    private String translateId(
        String table,
        String field,
        String id,
        String idField,
        String schema
    ) {
        String sql = "SELECT "
            + field
            + " FROM "
            + schema + "." + table
            + " WHERE "
            + idField
            + " = :id ;";
        javax.persistence.Query query = repository.queryFromString(sql);
        if (id == null) {
            return "";
        }
        try {
            int value = Integer.parseInt(id);
            query.setParameter("id", value);
        } catch (NumberFormatException nfe) {
            query.setParameter("id", id);
        }
        List<?> result = query.getResultList();
        if (!result.isEmpty()) {
            return result.get(0).toString();
        } else {
            return "(Object wurde gelöscht)";
        }
    }

    private Long formatDate(String format, String date) {
        DateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            return inFormat.parse(date).getTime();
        } catch (ParseException e) {
            return 0L;
        }
    }

    /**
     * Translate all known foreign keys.
     */
    private JsonStructure translateValues(JsonObject node) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        node.forEach((key, val) -> {
            if (mappings.containsKey(key)) {
                TableMapper m = mappings.get(key);
                if (m.getMappingTable().equals("date")) {
                    Long value =
                        formatDate(m.getValueField(), node.getString(key));
                    builder.add(key, value);
                } else {
                    String value = translateId(
                        m.getMappingTable(),
                        m.getValueField(),
                        node.get(key) != null ? node.getString(key) : null,
                        "id",
                        de.intevation.lada.model.master.SchemaName.NAME);
                    builder.add(key, value);
                }
            } else {
                builder.add(key, val);
            }
        });
        return builder.build();
    }

    /**
     * Class modeling audit service response data.
     */
    public class AuditResponseData {
        Integer id;
        String identifier;
        List<AuditEntry> audit;
        public Integer getId() {
            return id;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        public String getIdentifier() {
            return identifier;
        }
        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }
        public List<AuditEntry> getAudit() {
            return audit;
        }
        public void setAudit(List<AuditEntry> audit) {
            this.audit = audit;
        }
    }

    /**
     * Class modeling an audit trail entry.
     */
    public class AuditEntry {
        Long timestamp;
        String type;
        String action;
        JsonStructure changedFields;
        Object identifier;
        public Long getTimestamp() {
            return timestamp;
        }
        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public String getAction() {
            return action;
        }
        public void setAction(String action) {
            this.action = action;
        }
        public JsonStructure getChangedFields() {
            return changedFields;
        }
        public void setChangedFields(JsonStructure changedFields) {
            this.changedFields = changedFields;
        }
        public Object getIdentifier() {
            return identifier;
        }
        public void setIdentifier(Object identifier) {
            this.identifier = identifier;
        }
    }

    /**
     * Class modeling an audit identifier object.
     */
    public class AuditEntryIdentifier {
        String measm;
        String identifier;
        public String getMeasm() {
            return measm;
        }
        public void setMeasm(String measm) {
            this.measm = measm;
        }
        public String getIdentifier() {
            return identifier;
        }
        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }
    }
}
