/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.sql.Timestamp;

@Entity
@Table(schema = SchemaName.NAME)
public class SampleSpecif implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Size(max = 50)
    private String name;

    @Size(max = 40)
    private String eudfKeyword;

    @NotNull
    @Size(max = 7)
    private String extId;

    private Integer measUnitId;

    @Column(insertable = false)
    private Timestamp lastMod;

    public SampleSpecif() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEudfKeyword() {
        return this.eudfKeyword;
    }

    public void setEudfKeyword(String eudfKeyword) {
        this.eudfKeyword = eudfKeyword;
    }

    public String getExtId() {
        return this.extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public Integer getMeasUnitId() {
        return this.measUnitId;
    }

    public void setMeasUnitId(Integer unitId) {
        this.measUnitId = unitId;
    }

    public Timestamp getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Timestamp lastMod) {
        this.lastMod = lastMod;
    }

}
