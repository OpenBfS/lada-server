/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.stammdaten;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ReiProgpunktGrpZuordPK implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "rei_progpunkt_grp_id")
    private Integer reiProgpunktGrpId;

    @Column(name = "rei_progpunkt_id")
    private Integer reiProgpunktId;


    public Integer getReiProgpunktGrpId() {
        return this.reiProgpunktGrpId;
    }

    public void setReiProgpunktGrpId(Integer reiProgpunktGrpId) {
        this.reiProgpunktGrpId = reiProgpunktGrpId;
    }

    public Integer getReiProgpunktId() {
        return this.reiProgpunktId;
    }

    public void setReiProgpunktId(Integer reiProgpunktId) {
        this.reiProgpunktId = reiProgpunktId;
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
        if (!(other instanceof ReiProgpunktGrpZuordPK)) {
            return false;
        }
        ReiProgpunktGrpZuordPK castOther = (ReiProgpunktGrpZuordPK) other;
        return
            this.reiProgpunktGrpId.equals(castOther.reiProgpunktGrpId)
            && this.reiProgpunktId.equals(castOther.reiProgpunktId);
    }

    /**
     * Get the objects hash code.
     * @return the hash
     */
    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.reiProgpunktGrpId.hashCode();
        hash = hash * prime + this.reiProgpunktId.hashCode();
        return hash;
    }
}
