/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.stammdaten;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * The persistent class for the mess_einheit database table.
 *
 */
@Entity
@Table(name = "mess_einheit", schema = SchemaName.LEGACY_NAME)
public class MessEinheit implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String beschreibung;

    private String einheit;

    /**
     * Get all MasseinheitUmrechnungs for units that can be converted into
     * this one.
     */
    @OneToMany(mappedBy = "toUnitId", fetch = FetchType.EAGER)
    @JsonbTransient
    private List<UnitConvers> massEinheitUmrechnungZus;

    @Column(name = "eudf_messeinheit_id")
    private String eudfMesseinheitId;

    @Column(name = "umrechnungs_faktor_eudf")
    private Long umrechnungsFaktorEudf;

    /**
     * Attribute used to distinguish between primary and secondary messeinheit
     * records.
     */
    @Transient
    private Boolean primary;

    @Column(name = "letzte_aenderung", insertable = false)
    private Timestamp letzteAenderung;

    public MessEinheit() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBeschreibung() {
        return this.beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public String getEinheit() {
        return this.einheit;
    }

    public void setEinheit(String einheit) {
        this.einheit = einheit;
    }

    public String getEudfMesseinheitId() {
        return this.eudfMesseinheitId;
    }

    public void setEudfMesseinheitId(String eudfMesseinheitId) {
        this.eudfMesseinheitId = eudfMesseinheitId;
    }

    public Long getUmrechnungsFaktorEudf() {
        return this.umrechnungsFaktorEudf;
    }

    public void setUmrechnungsFaktorEudf(Long umrechnungsFaktorEudf) {
        this.umrechnungsFaktorEudf = umrechnungsFaktorEudf;
    }

    public List<UnitConvers> getMassEinheitUmrechnungZus() {
        return this.massEinheitUmrechnungZus;
    }

    public Boolean getPrimary() {
        return this.primary;
    }

    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }

    public Timestamp getLetzteAenderung() {
        return this.letzteAenderung;
    }

    public void setLetzteAenderung(Timestamp letzteAenderung) {
        this.letzteAenderung = letzteAenderung;
    }

}
