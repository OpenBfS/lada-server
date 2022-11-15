/* Copyright (C) 2017 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.stammdaten;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "import_conf", schema = SchemaName.NAME)
public class ImporterConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String action;

    private String attribute;

    private String fromVal;

    private String measFacilId;

    private String name;

    private String toVal;

    @Column(insertable = false)
    private Timestamp lastMod;

    public ImporterConfig() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAttribute() {
        return this.attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getFromVal() {
        return this.fromVal;
    }

    public void setFromVal(String fromVal) {
        this.fromVal = fromVal;
    }

    public String getMeasFacilId() {
        return this.measFacilId;
    }

    public void setMeasFacilId(String measFacilId) {
        this.measFacilId = measFacilId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToVal() {
        return this.toVal;
    }

    public void setToVal(String toVal) {
        this.toVal = toVal;
    }

    public Timestamp getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Timestamp lastMod) {
        this.lastMod = lastMod;
    }

}
