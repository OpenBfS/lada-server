/* Copyright (C) 2017 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Date;

import jakarta.json.Json;
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

    private String changedFields;

    private Integer objectId;

    private String rowData;

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

    public JsonObject getChangedFieldsJson() {
        return Json.createReader(new StringReader(this.changedFields))
            .readObject();
    }

    public void setChangedFields(String changedFields) {
        this.changedFields = changedFields;
    }

    public Integer getObjectId() {
        return this.objectId;
    }

    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }

    public JsonObject getRowDataJson() {
        return Json.createReader(new StringReader(this.rowData))
            .readObject();
    }

    public void setRowData(String rowData) {
        this.rowData = rowData;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

}
