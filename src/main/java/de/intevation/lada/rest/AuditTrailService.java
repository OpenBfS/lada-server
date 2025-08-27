/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import de.intevation.lada.model.NamingStrategy;
import de.intevation.lada.model.lada.AuditTrailMeasmView;
import de.intevation.lada.model.lada.AuditTrailMeasmView_;
import de.intevation.lada.model.lada.AuditTrailMpgView;
import de.intevation.lada.model.lada.AuditTrailMpgView_;
import de.intevation.lada.model.lada.AuditTrailSampleView;
import de.intevation.lada.model.lada.AuditTrailSampleView_;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.Geolocat_;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.lada.Mpg_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.Sample_;
import de.intevation.lada.model.master.MeasUnit;
import de.intevation.lada.model.master.MeasUnit_;
import de.intevation.lada.model.master.MpgCateg;
import de.intevation.lada.model.master.MpgCateg_;
import de.intevation.lada.model.master.NuclFacilGr;
import de.intevation.lada.model.master.NuclFacilGr_;
import de.intevation.lada.model.master.OprMode;
import de.intevation.lada.model.master.OprMode_;
import de.intevation.lada.model.master.Regulation;
import de.intevation.lada.model.master.Regulation_;
import de.intevation.lada.model.master.ReiAgGr;
import de.intevation.lada.model.master.ReiAgGr_;
import de.intevation.lada.model.master.SampleMeth;
import de.intevation.lada.model.master.SampleMeth_;
import de.intevation.lada.model.master.Sampler;
import de.intevation.lada.model.master.Sampler_;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.Site_;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


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
@Path(LadaService.PATH_REST + "audit")
public class AuditTrailService extends LadaService {

    private static final String SITE_ID = "siteId";

    /**
     * Class to store tablename and value field for foreign key mappings.
     */
    private class TableMapper<T> {
        private Class<T> mappingTable;
        private SingularAttribute<T, ?> valueField;

        TableMapper(
            Class<T> mTable,
            SingularAttribute<T, ?> vField
        ) {
            this.mappingTable = mTable;
            this.valueField = vField;
        }

        public Class<T> getMappingTable() {
            return mappingTable;
        }

        public SingularAttribute<T, ?> getValueField() {
            return valueField;
        }
    }

    /**
     * The data repository granting read/write access.
     */
    @Inject
    private Repository repository;

    /**
     * Map foreign key to their associated table and the display value.
     */
    private Map<String, TableMapper<?>> mappings = Map.ofEntries(
        Map.entry(MeasVal_.MEAS_UNIT_ID,
            new TableMapper<MeasUnit>(MeasUnit.class, MeasUnit_.unitSymbol)),
        Map.entry(SITE_ID,
            new TableMapper<Site>(Site.class, Site_.extId)),
        Map.entry(Sample_.REGULATION_ID,
            new TableMapper<Regulation>(Regulation.class, Regulation_.name)),
        Map.entry(Sample_.OPR_MODE_ID,
            new TableMapper<OprMode>(OprMode.class, OprMode_.name)),
        Map.entry(Mpg_.MPG_CATEG_ID,
            new TableMapper<MpgCateg>(MpgCateg.class, MpgCateg_.extId)),
        Map.entry(Sample_.SAMPLE_METH_ID,
            new TableMapper<SampleMeth>(SampleMeth.class, SampleMeth_.extId)),
        Map.entry(Sample_.SAMPLER_ID,
            new TableMapper<Sampler>(Sampler.class, Sampler_.extId)),
        Map.entry(Sample_.NUCL_FACIL_GR_ID,
            new TableMapper<NuclFacilGr>(NuclFacilGr.class, NuclFacilGr_.extId)),
        Map.entry(Sample_.REI_AG_GR_ID,
            new TableMapper<ReiAgGr>(ReiAgGr.class, ReiAgGr_.name))
    );

    private Map<String, String> mappedAttrs = mappings.keySet().stream()
        .collect(Collectors.toMap(NamingStrategy::camelToSnake, k -> k));

    private List<String> dateAttrs = Set.of(
        Sample_.SAMPLE_START_DATE,
        Sample_.SAMPLE_END_DATE,
        Sample_.SCHED_START_DATE,
        Sample_.SCHED_END_DATE,
        "mid_coll_pd",
        Measm_.MEASM_START_DATE
    ).stream().map(NamingStrategy::camelToSnake).toList();

    /**
     * Service to generate audit trail for sample objects.
     *
     * @param pId ID of sample given in URL path.
     * @return Audit trail data for requested sample.
     */
    @GET
    @Path("probe/{id}")
    public AuditResponseData getProbe(
        @PathParam("id") Integer pId
    ) {
        Sample probe = repository.getById(Sample.class, pId);

        UserInfo userInfo = authorization.getInfo();

        //Get ort ids connected to this probe
        QueryBuilder<Geolocat> refBuilder = repository
            .queryBuilder(Geolocat.class)
            .and(Geolocat_.sample, probe);
        List<Integer> ortIds = new LinkedList<Integer>();
        for (Geolocat zuordnung
            : repository.filter(refBuilder.getQuery())
        ) {
            ortIds.add(zuordnung.getSite().getId());
        }

        // Get all entries for the probe and its sub objects.
        QueryBuilder<AuditTrailSampleView> builder = repository
            .queryBuilder(AuditTrailSampleView.class)
            .and(AuditTrailSampleView_.objectId, pId)
            .and(AuditTrailSampleView_.tableName, "sample")
            .or(AuditTrailSampleView_.sampleId, pId);
        if (ortIds.size() > 0) {
            builder.orIn(AuditTrailSampleView_.siteId, ortIds);
        }
        builder.orderBy(AuditTrailSampleView_.tstamp, true);
        List<AuditTrailSampleView> audit =
            repository.filter(builder.getQuery());

        AuditResponseData auditResponseData = new AuditResponseData();
        List<AuditEntry> entries = new ArrayList<>();
        auditResponseData.setId(probe.getId());
        auditResponseData.setIdentifier(
            probe.getMainSampleId() == null
            ? probe.getExtId()
            : probe.getMainSampleId()
        );
        for (AuditTrailSampleView a : audit) {
            //If audit entry shows a messwert, do not show if:
            // - StatusKombi is 1 (MST - nicht vergeben)
            // - User is not owner of the messung
            if (a.getTableName().equals("meas_val")) {
                Measm messung = repository.entityManager().find(
                    Measm.class, a.getMeasmId());
                if (messung != null) {
                    if (messung.getStatusProt().getStatusMpId() == 1
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
        return auditResponseData;
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
        node.setChangedFields(translateValues(audit.getChangedFieldsJson()));

        switch (audit.getTableName()) {
        case "site":
            node.setIdentifier(audit.getRowDataJson().get("ext_id").toString());
            break;
        case "comm_sample":
            node.setIdentifier(audit.getRowDataJson().get("date").toString());
            break;
        case "sample_specif_meas_val":
            node.setIdentifier(
                audit.getRowDataJson().get("sample_specif_id").toString());
            break;
        case "geolocat":
            String value = translateId(
                SITE_ID,
                audit.getRowDataJson().get("site_id").toString());
            node.setIdentifier(value);
            break;
        case "measm":
            Measm m = repository.getById(
                Measm.class, audit.getObjectId());
            node.setIdentifier(getIdentifier(m));
            break;
        default:
            // Do nothing
        }

        if (audit.getMeasmId() != null) {
            Measm m = repository.getById(
                Measm.class, audit.getMeasmId());
            AuditEntryIdentifier identifier = new AuditEntryIdentifier();
            identifier.setMeasm(getIdentifier(m));

            switch (audit.getTableName()) {
            case "comm_measm":
                identifier.setIdentifier(
                    audit.getRowDataJson().get("date").toString());
                break;
            case "meas_val":
                identifier.setIdentifier(
                    audit.getRowDataJson().get("measd_id").toString());
                break;
            default:
                // Do nothing
            }
            node.setIdentifier(identifier);
        }
        return node;
    }

    /**
     * Service to generate audit trail for measm objects.
     *
     * @param mId ID of measm given in URL path.
     * @return Audit trail data for requested measm.
     */
    @GET
    @Path("messung/{id}")
    public AuditResponseData getMessung(
        @PathParam("id") Integer mId
    ) {
        Measm messung = repository.getById(Measm.class, mId);

        Sample probe = messung.getSample();
        UserInfo userInfo = authorization.getInfo();
        QueryBuilder<AuditTrailMeasmView> builder = repository
            .queryBuilder(AuditTrailMeasmView.class)
            .and(AuditTrailMeasmView_.objectId, mId)
            .and(AuditTrailMeasmView_.tableName, "measm")
            .or(AuditTrailMeasmView_.measmId, mId);
        builder.orderBy(AuditTrailMeasmView_.tstamp, true);
        List<AuditTrailMeasmView> audit =
            repository.filter(builder.getQuery());

        AuditResponseData auditData = new AuditResponseData();
        List<AuditEntry> entries = new ArrayList<>();
        auditData.setId(messung.getId());
        auditData.setIdentifier(getIdentifier(messung));
        for (AuditTrailMeasmView a : audit) {
            //If audit entry shows a messwert, do not show if:
            // - StatusKombi is 1 (MST - nicht vergeben)
            // - User is not owner of the messung
            if (a.getTableName().equals("meas_val")
                && messung.getStatusProt().getStatusMpId() == 1
                && !userInfo.getMessstellen().contains(
                    probe.getMeasFacilId())) {
                continue;
            }
            entries.add(createEntry(a));
        }
        auditData.setAudit(entries);
        return auditData;
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
        node.setChangedFields(audit.getChangedFieldsJson());

        switch (audit.getTableName()) {
        case "comm_measm":
            node.setIdentifier(audit.getRowDataJson().get("date").toString());
            break;
        case "meas_val":
            node.setIdentifier(
                audit.getRowDataJson().get("measd_id").toString());
            break;
        default:
            // Do nothing
        }
        return node;
    }

    /**
     * Service to generate audit trail for mpg objects.
     */
    @GET
    @Path("/messprogramm/{id}")
    public AuditResponseData getMessprogramm(
        @PathParam("id") Integer mpgId
    ) {
        Mpg messprogramm = repository.getById(Mpg.class, mpgId);

        QueryBuilder<AuditTrailMpgView> builder =
            repository.queryBuilder(AuditTrailMpgView.class);
        builder.and(AuditTrailMpgView_.objectId, mpgId);
        builder.and(AuditTrailMpgView_.tableName, "mpg");
        builder.or(AuditTrailMpgView_.mpgId, mpgId);
        builder.orderBy(AuditTrailMpgView_.tstamp, true);
        List<AuditTrailMpgView> audit =
            repository.filter(builder.getQuery());

        // Create an empty JsonObject
        AuditResponseData auditData = new AuditResponseData();
        List<AuditEntry> entries = new ArrayList<>();
        auditData.setId(messprogramm.getId());
        auditData.setIdentifier(getIdentifier(messprogramm));
        for (AuditTrailMpgView a : audit) {
            entries.add(createEntry(a));
        }
        auditData.setAudit(entries);
        return auditData;
    }

    private AuditEntry createEntry(AuditTrailMpgView audit) {
        AuditEntry node = new AuditEntry();
        node.setTimestamp(audit.getTstamp().getTime());
        node.setType(audit.getTableName());
        node.setAction(audit.getAction());
        node.setChangedFields(audit.getChangedFieldsJson());

        if ("mpg_mmt_mp_measd".equals(audit.getTableName())) {
            node.setIdentifier(
                audit.getRowDataJson().get("measd_id").toString());
        }
        if ("mpg_sample_specif".equals(audit.getTableName())) {
            node.setIdentifier(audit.getRowDataJson().get("sample_specif_id"));
        }
        if ("mpg_mmt_mp".equals(audit.getTableName())) {
            node.setIdentifier(audit.getRowDataJson().get("mmt_id").toString());
        }
        if ("geolocat_mpg".equals(audit.getTableName())) {
            String value = translateId(
                SITE_ID,
                audit.getRowDataJson().get("site_id").toString());
            node.setIdentifier(value);
        }
        return node;
    }


    /**
     * Translate a foreign key value into the associated value.
     */
    private String translateId(String key, String value) {
        if (value == null) {
            return "";
        }

        Object id;
        try {
            id = Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            id = value;
        }

        EntityType<?> table = repository.entityManager().getMetamodel().entity(
            mappings.get(key).getMappingTable());
        String sql = String.format("select %s from %s where %s = :id",
            mappings.get(key).getValueField().getName(),
            table.getName(),
            table.getId(table.getIdType().getJavaType()).getName());
        try {
            return repository.entityManager().createQuery(sql)
                .setParameter("id", id)
                .getSingleResult().toString();
        } catch (NoResultException e) {
            return "(Object wurde gelöscht)";
        }
    }

    private Long formatDate(String date) {
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
    private JsonObject translateValues(JsonObject node) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        node.forEach((key, val) -> {
            if (dateAttrs.contains(key)) {
                if (node.isNull(key)) {
                    builder.add(key, JsonValue.NULL);
                } else {
                    Long value = formatDate(node.getString(key));
                    builder.add(key, value);
                }
            } else if (mappedAttrs.containsKey(key)) {
                String id;
                if (node.containsKey(key) && node.get(key)
                        .getValueType() == ValueType.NUMBER) {
                    id = "" + node.getInt(key);
                } else {
                    id = node.getString(key, null);
                }
                String value = translateId(mappedAttrs.get(key), id);
                builder.add(key, value);
            } else {
                builder.add(key, val);
            }
        });
        return builder.build();
    }

    private String getIdentifier(Measm measm) {
        return measm == null
            ? "(deleted)"
            : measm.getMinSampleId() == null
                ? measm.getExtId().toString()
                : measm.getMinSampleId();
    }

    private String getIdentifier(Mpg mpg) {
        return mpg == null
            ? "(deleted)"
            : mpg.getId().toString();
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
        JsonObject changedFields;
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
        public JsonObject getChangedFields() {
            return changedFields;
        }
        public void setChangedFields(JsonObject changedFields) {
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
