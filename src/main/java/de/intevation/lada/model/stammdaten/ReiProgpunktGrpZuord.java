/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
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
import java.sql.Timestamp;

/**
 * The persistent class for the rei_progpunkt_grp_zuord database table.
 *
 */
@Entity
@Table(name = "rei_progpunkt_grp_zuord", schema = SchemaName.LEGACY_NAME)
public class ReiProgpunktGrpZuord implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private ReiProgpunktGrpZuordPK id;

    @Column(name = "letzte_aenderung", insertable = false)
    private Timestamp letzteAenderung;

    public ReiProgpunktGrpZuord() {
    }

    public ReiProgpunktGrpZuordPK getId() {
        return this.id;
    }

    public void setId(ReiProgpunktGrpZuordPK id) {
        this.id = id;
    }

    public Integer getReiProgpunktGrpId() {
        return this.id.getReiProgpunktGrpId();
    }

    public void setReiProgpunktGrpId(Integer reiProgpunktGrpId) {
        this.id.setReiProgpunktGrpId(reiProgpunktGrpId);
    }

    public Integer getReiProgpunktId() {
        return this.id.getReiProgpunktId();
    }

    public void setReiProgpunktId(Integer reiProgpunktId) {
        this.setReiProgpunktId(reiProgpunktId);
    }

    public Timestamp getLetzteAenderung() {
        return this.letzteAenderung;
    }

    public void setLetzteAenderung(Timestamp letzteAenderung) {
        this.letzteAenderung = letzteAenderung;
    }

}
