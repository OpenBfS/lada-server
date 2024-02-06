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
import java.util.List;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import jakarta.persistence.Transient;

@Entity
@Table(schema = SchemaName.NAME)
public class MeasUnit implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String name;

    private String unitSymbol;

    /**
     * Get all MasseinheitUmrechnungs for units that can be converted into
     * this one.
     */
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_unit_id")
    @JsonbTransient
    private List<UnitConvers> unitConversTo;

    private String eudfUnitId;

    private Long eudfConversFactor;

    /**
     * Attribute used to distinguish between primary and secondary messeinheit
     * records.
     */
    @Transient
    private Boolean primary;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    public MeasUnit() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnitSymbol() {
        return this.unitSymbol;
    }

    public void setUnitSymbol(String unitSymbol) {
        this.unitSymbol = unitSymbol;
    }

    public String getEudfUnitId() {
        return this.eudfUnitId;
    }

    public void setEudfUnitId(String eudfUnitId) {
        this.eudfUnitId = eudfUnitId;
    }

    public Long getEudfConversFactor() {
        return this.eudfConversFactor;
    }

    public void setEudfConversFactor(Long eudfConversFactor) {
        this.eudfConversFactor = eudfConversFactor;
    }

    public List<UnitConvers> getUnitConversTo() {
        return this.unitConversTo;
    }

    public Boolean getPrimary() {
        return this.primary;
    }

    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }

}
