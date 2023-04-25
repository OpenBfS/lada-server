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
public class AuditTrailMeasmView implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    private String action;

    @Temporal(TIMESTAMP)
    private Date tstamp;

    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode changedFields;

    private String measmId;

    private Integer objectId;

    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode rowData;

    private String tableName;

    public AuditTrailMeasmView() {
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Date getTstamp() {
        return this.tstamp;
    }

    public void setTstamp(Date tstamp) {
        this.tstamp = tstamp;
    }

    public JsonNode getChangedFields() {
        return this.changedFields;
    }

    public void setChangedFields(JsonNode changedFields) {
        this.changedFields = changedFields;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMeasmId() {
        return this.measmId;
    }

    public void setMeasmId(String measmId) {
        this.measmId = measmId;
    }

    public Integer getObjectId() {
        return this.objectId;
    }

    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }

    public JsonNode getRowData() {
        return this.rowData;
    }

    public void setRowData(JsonNode rowData) {
        this.rowData = rowData;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

}
