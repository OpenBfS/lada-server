/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
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

/**
 * The persistent class for the env_descrip database table.
 *
 */
@Entity
@Table(name = "env_descrip", schema = SchemaName.NAME)
public class EnvDescrip implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String implication;

    private String name;

    private Integer lev;

    @Column(name = "s_xx")
    private Integer sXx;

    private Integer levVal;

    private Integer predId;

    @Column(insertable = false)
    private Timestamp lastMod;

    public EnvDescrip() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getImplication() {
        return this.implication;
    }

    public void setImplication(String implication) {
        this.implication = implication;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLev() {
        return this.lev;
    }

    public void setLev(Integer lev) {
        this.lev = lev;
    }

    public Integer getSXx() {
        return this.sXx;
    }

    public void setSXx(Integer s) {
        this.sXx = s;
    }

    public Integer getLevVal() {
        return this.levVal;
    }

    public void setLevVal(Integer levVal) {
        this.levVal = levVal;
    }

    public Integer getPredId() {
        return this.predId;
    }

    public void setPredId(Integer predId) {
        this.predId = predId;
    }

    public Timestamp getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Timestamp lastMod) {
        this.lastMod = lastMod;
    }

}