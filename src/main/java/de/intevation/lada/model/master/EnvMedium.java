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
public class EnvMedium implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String descr;

    private Integer unit1;

    private Integer unit2;

    private String name;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    public EnvMedium() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescr() {
        return this.descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public Integer getUnit1() {
        return this.unit1;
    }

    public void setUnit1(Integer unit1) {
        this.unit1 = unit1;
    }

    public Integer getUnit2() {
        return this.unit2;
    }

    public void setUnit2(Integer unit2) {
        this.unit2 = unit2;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }

}
