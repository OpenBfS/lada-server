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
public class AuditTrailSampleView implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    private String changedFields;

    private String rowData;

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

    public JsonObject getChangedFieldsJson() {
        return Json.createReader(new StringReader(this.changedFields))
            .readObject();
    }

    public void setChangedFields(String changedFields) {
        this.changedFields = changedFields;
    }

    public JsonObject getRowDataJson() {
        return Json.createReader(new StringReader(this.rowData))
            .readObject();
    }

    public void setRowData(String rowData) {
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
