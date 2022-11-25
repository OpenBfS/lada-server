/* Copyright (C) 2019 by Bundesamt fuer Strahlenschutz
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
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(schema = SchemaName.NAME)
public class UnitConvers implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn
    private MeasUnit fromUnit;

    private Integer toUnitId;

    private Double factor;

    @Column(insertable = false)
    private Timestamp lastMod;

    public UnitConvers() {

    }

    public Integer getId() {
        return this.id;
    }

    public MeasUnit getFromUnit() {
        return this.fromUnit;
    }

    public Integer getToUnitId() {
        return this.toUnitId;
    }

    public Double getFactor() {
        return this.factor;
    }

    public Timestamp getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Timestamp lastMod) {
        this.lastMod = lastMod;
    }

}
