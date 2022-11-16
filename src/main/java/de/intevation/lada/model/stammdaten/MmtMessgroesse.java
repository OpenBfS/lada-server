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
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "mmt_measd_view", schema = SchemaName.NAME)
public class MmtMessgroesse implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private MmtMessgroessePK id;

    @Column(insertable = false, updatable = false)
    private Integer measdId;

    @Column(insertable = false, updatable = false)
    private String mmtId;

    public MmtMessgroesse() {
    }

    public MmtMessgroessePK getMmtMessgroessePK() {
        return this.id;
    }

    public void setMmtMessgroessePK(MmtMessgroessePK i) {
        this.id = i;
    }

    public Integer getMeasdId() {
        return this.measdId;
    }

    public void setMeasdId(Integer measdId) {
        this.measdId = measdId;
    }

    public String getMmtId() {
        return this.mmtId;
    }

    public void setMmtId(String mmtId) {
        this.mmtId = mmtId;
    }

}
