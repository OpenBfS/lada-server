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


/**
 * The persistent class for the staat database table.
 *
 */
@Entity
@Table(name = "nuts", schema = SchemaName.LEGACY_NAME)
public class Nuts implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "nuts_code")
    private String id;

    @Column(name = "staat_id")
    private Integer staatId;

    private String bezeichnung;

    @Column(name = "kda_id")
    private Integer kdaId;

    @Column(name = "koord_x_extern")
    private String koordXExtern;

    @Column(name = "koord_y_extern")
    private String koordYExtern;

    public Nuts() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getStaatId() {
        return this.staatId;
    }

    public void setStaatId(Integer staatId) {
        this.staatId = staatId;
    }

    public String getBezeichnungd() {
        return this.bezeichnung;
    }

    public void setBezeichnung(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public Integer getKdaId() {
        return this.kdaId;
    }

    public void setKdaId(Integer kdaId) {
        this.kdaId = kdaId;
    }

    public String getKoordXExtern() {
        return this.koordXExtern;
    }

    public void setKoordXExtern(String koordXExtern) {
        this.koordXExtern = koordXExtern;
    }

    public String getKoordYExtern() {
        return this.koordYExtern;
    }

    public void setKoordYExtern(String koordYExtern) {
        this.koordYExtern = koordYExtern;
    }

}
