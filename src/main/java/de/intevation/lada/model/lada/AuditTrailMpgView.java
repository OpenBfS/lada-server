/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import java.io.Serializable;
import java.io.StringReader;
import java.sql.Timestamp;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The persistent class for the audit_trail_messprogramm database table.
 *
 */
@Entity
@Table(schema = SchemaName.NAME)
public class AuditTrailMpgView implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    private String action;

    private Timestamp tstamp;

    private String changedFields;

    private Integer mpgId;

    private Integer objectId;

    private String rowData;

    private String tableName;

    /**
     * Constructor.
     */
    public AuditTrailMpgView() { }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Timestamp getTstamp() {
        return this.tstamp;
    }

    public void setTstamp(Timestamp tstamp) {
        this.tstamp = tstamp;
    }

    public JsonObject getChangedFieldsJson() {
        return Json.createReader(new StringReader(this.changedFields))
            .readObject();
    }

    public void setChangedFields(String changedFields) {
        this.changedFields = changedFields;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMpgId() {
        return this.mpgId;
    }

    public void setMpgId(Integer mpgId) {
        this.mpgId = mpgId;
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
