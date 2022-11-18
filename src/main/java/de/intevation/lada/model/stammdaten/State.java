/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.stammdaten;

import java.io.Serializable;

import javax.json.bind.annotation.JsonbProperty;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(schema = SchemaName.NAME)
public class State implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private Boolean isEuCountry;

    private Integer ctryOrigId;

    private Integer spatRefSysId;

    private String xCoordExt;

    private String yCoordExt;

    private String ctry;

    private String iso3166;

    @Column(insertable = false)
    private Timestamp lastMod;

    private String intVehRegCode;

    public State() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getIsEuCountry() {
        return this.isEuCountry;
    }

    public void setIsEuCountry(Boolean isEuCountry) {
        this.isEuCountry = isEuCountry;
    }

    public Integer getCtryOrigId() {
        return this.ctryOrigId;
    }

    public void setCtryOrigId(Integer ctryOrigId) {
        this.ctryOrigId = ctryOrigId;
    }

    public Integer getSpatRefSysId() {
        return this.spatRefSysId;
    }

    public void setSpatRefSysId(Integer spatRefSysId) {
        this.spatRefSysId = spatRefSysId;
    }

    @JsonbProperty("xCoordExt")
    public String getXCoordExt() {
        return this.xCoordExt;
    }

    @JsonbProperty("xCoordExt")
    public void setXCoordExt(String xCoordExt) {
        this.xCoordExt = xCoordExt;
    }

    @JsonbProperty("yCoordExt")
    public String getYCoordExt() {
        return this.yCoordExt;
    }

    @JsonbProperty("yCoordExt")
    public void setYCoordExt(String yCoordExt) {
        this.yCoordExt = yCoordExt;
    }

    public String getCtry() {
        return this.ctry;
    }

    public void setCtry(String ctry) {
        this.ctry = ctry;
    }

    public String getIso3166() {
        return this.iso3166;
    }

    public void setIso3166(String iso3166) {
        this.iso3166 = iso3166;
    }

    public String getIntVehRegCode() {
        return this.intVehRegCode;
        }

    public void setIntVehRegCode(String intVehRegCode) {
        this.intVehRegCode = intVehRegCode;
    }

    public Timestamp getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Timestamp lastMod) {
        this.lastMod = lastMod;
    }

}
