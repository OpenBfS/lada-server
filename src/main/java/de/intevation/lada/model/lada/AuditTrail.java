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

import jakarta.json.JsonObject;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(schema = SchemaName.NAME)
public class AuditTrail implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;
    private String action;

    @Temporal(TIMESTAMP)
    private Date actionTstampClk;

    @JdbcTypeCode(SqlTypes.JSON)
    private JsonObject changedFields;

    private Integer objectId;

    @JdbcTypeCode(SqlTypes.JSON)
    private JsonObject rowData;

    private String tableName;

    public AuditTrail() {
    }

    public Long getId() {
        return this.id;
}

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Date getActionTstampClk() {
        return this.actionTstampClk;
    }

    public void setActionTstampClk(Date actionTstampClk) {
        this.actionTstampClk = actionTstampClk;
    }

    public JsonObject getChangedFields() {
        return this.changedFields;
    }

    public void setChangedFields(JsonObject changedFields) {
        this.changedFields = changedFields;
    }

    public Integer getObjectId() {
        return this.objectId;
    }

    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }

    public JsonObject getRowData() {
        return this.rowData;
    }

    public void setRowData(JsonObject rowData) {
        this.rowData = rowData;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

}
