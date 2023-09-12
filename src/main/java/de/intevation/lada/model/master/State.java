/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(schema = SchemaName.NAME)
public class State implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private Boolean isEuCountry;

    private Integer ctryOrigId;

    private Integer spatRefSysId;

    private String coordXExt;

    private String coordYExt;

    private String ctry;

    private String iso3166;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

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

    public String getCoordXExt() {
        return this.coordXExt;
    }

    public void setCoordXExt(String coordXExt) {
        this.coordXExt = coordXExt;
    }

    public String getCoordYExt() {
        return this.coordYExt;
    }

    public void setCoordYExt(String coordYExt) {
        this.coordYExt = coordYExt;
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

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }

}
