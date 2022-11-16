/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.stammdaten;

import java.io.Serializable;

import javax.persistence.Embeddable;

/**
 * The MmtMessgroesse primary key.
 */
@Embeddable
public class MmtMeasdViewPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer measdId;

    private String mmtId;

    public MmtMeasdViewPK() {
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

    /**
     * equals operator.
     * @param other the other object
     * @return true if the objects are equal
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MmtMeasdViewPK)) {
            return false;
        }
        MmtMeasdViewPK castOther = (MmtMeasdViewPK) other;
        return
            this.measdId.equals(castOther.measdId)
            && this.mmtId.equals(castOther.mmtId);
    }

    /**
     * get the objects hash code.
     * @return the hash
     */
    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.measdId.hashCode();
        hash = hash * prime + this.mmtId.hashCode();
        return hash;
    }
}
