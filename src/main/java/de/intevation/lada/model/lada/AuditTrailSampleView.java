/* Copyright (C) 2017 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;

import com.fasterxml.jackson.databind.JsonNode;
@Entity
@Table(schema = SchemaName.NAME)
public class AuditTrailSampleView implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode changedFields;

    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode rowData;

    @Temporal(TIMESTAMP)
    private Date tstamp;

    private String action;

    private Integer measmId;

    private Integer objectId;

    private Integer siteId;

    private Integer sampleId;

    private String tableName;

    public AuditTrailSampleView() {
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

    public Date getTstamp() {
        return tstamp;
    }

    public void setTstamp(Date tstamp) {
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
