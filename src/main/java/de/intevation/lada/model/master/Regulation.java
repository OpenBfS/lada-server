/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import javax.persistence.Column;

/**
 * The persistent class for the regulation database table.
 *
 */
@Entity
@Table(name = "regulation", schema = SchemaName.NAME)
public class Regulation implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String descr;

    private String regulation;

    @Column(insertable = false)
    private Timestamp lastMod;

    public Regulation() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescr() {
        return this.descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getRegulation() {
        return this.regulation;
    }

    public void setRegulation(String regulation) {
        this.regulation = regulation;
    }

    public Timestamp getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Timestamp lastMod) {
        this.lastMod = lastMod;
    }

}
