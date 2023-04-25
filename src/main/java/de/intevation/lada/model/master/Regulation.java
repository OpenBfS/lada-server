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

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import jakarta.persistence.Column;

/**
 * The persistent class for the regulation database table.
 *
 */
@Entity
@Table(schema = SchemaName.NAME)
public class Regulation implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String descr;

    private String regulation;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

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

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }

}
