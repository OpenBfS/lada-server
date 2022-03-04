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
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * The persistent class for the koordinaten_art database table.
 *
 */
@Entity
@Table(name = "koordinaten_art", schema = SchemaName.NAME)
public class KoordinatenArt implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    @Column(name = "idf_geo_key")
    private String idfGeoKey;

    private String koordinatenart;

    @Column(name = "letzte_aenderung", insertable = false)
    private Timestamp letzteAenderung;

    public KoordinatenArt() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdfGeoKey() {
        return this.idfGeoKey;
    }

    public void setIdfGeoKey(String idfGeoKey) {
        this.idfGeoKey = idfGeoKey;
    }

    public String getKoordinatenart() {
        return this.koordinatenart;
    }

    public void setKoordinatenart(String koordinatenart) {
        this.koordinatenart = koordinatenart;
    }

    public Timestamp getLetzteAenderung() {
        return this.letzteAenderung;
    }

    public void setLetzteAenderung(Timestamp letzteAenderung) {
        this.letzteAenderung = letzteAenderung;
    }

}
