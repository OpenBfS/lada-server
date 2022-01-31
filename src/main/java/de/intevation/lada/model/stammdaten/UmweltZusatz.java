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
 * The persistent class for the umwelt_zusatz database table.
 *
 */
@Entity
@Table(name = "umwelt_zusatz", schema = SchemaName.NAME)
public class UmweltZusatz implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    @Column(name = "pzs_id")
    private String pzsId;

    @Column(name = "UMW_id")
    private String umwId;
    public UmweltZusatz() {
    }

    @Column(name = "letzte_aenderung", insertable = false)
    private Timestamp letzteAenderung;

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPzsId() {
        return this.pzsId;
    }

    public void setPzsId(String pzsId) {
        this.pzsId = pzsId;
    }

    public String getUmwId() {
        return this.umwId;
    }

    public void setUmwId(String umwId) {
        this.pzsId = umwId;
    }

    public Timestamp getLetzteAenderung() {
        return this.letzteAenderung;
    }

    public void setLetzteAenderung(Timestamp letzteAenderung) {
        this.letzteAenderung = letzteAenderung;
    }

}
