/* Copyright (C) 2017 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.land;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.JsonNode;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import de.intevation.lada.util.data.JsonObjectType;

@Entity
@Table(name = "audit_trail_sample_view", schema = SchemaName.NAME)
@TypeDefs({ @TypeDef(name = "JsonObject", typeClass = JsonObjectType.class) })
public class AuditTrailProbe implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Type(type = "JsonObject")
    private JsonNode changedFields;

    @Type(type = "JsonObject")
    private JsonNode rowData;

    private Timestamp tstamp;

    private String action;

    private Integer measmId;

    private Integer objectId;

    private Integer siteId;

    private Integer sampleId;

    private String tableName;

    public AuditTrailProbe() {
    }

    public JsonNode getChangedFields() {
        return this.changedFields;
    }

    public void setChangedFields(JsonNode changedFields) {
        this.changedFields = changedFields;
    }

    public JsonNode getRowData() {
        return rowData;
    }

    public void setRowData(JsonNode rowData) {
        this.rowData = rowData;
    }

    public Timestamp getTstamp() {
        return tstamp;
    }

    public void setTstamp(Timestamp tstamp) {
        this.tstamp = tstamp;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getMeasmId() {
        return this.measmId;
    }

    public void setMeasmId(Integer measmId) {
        this.measmId = measmId;
    }

    public Integer getObjectId() {
        return this.objectId;
    }

    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }

    public Integer getSiteId() {
        return this.siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    public Integer getSampleId() {
        return this.sampleId;
    }

    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

}
