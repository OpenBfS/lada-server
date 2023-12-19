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
public class EnvDescrip implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String implication;

    private String name;

    private Integer lev;

    @Column(name = "imis2_id")
    private Integer imis2Id;

    private Integer levVal;

    private Integer predId;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

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

    public Integer getImis2Id() {
        return this.imis2Id;
    }

    public void setImis2Id(Integer imis2Id) {
        this.imis2Id = imis2Id;
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

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }

}
