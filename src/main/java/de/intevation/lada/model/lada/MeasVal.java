/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.ws.rs.core.MultivaluedMap;



@Entity
@Table(schema = SchemaName.NAME)
public class MeasVal implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Boolean isThreshold;

    @Column(insertable = false)
    private Timestamp lastMod;

    private Integer measUnitId;

    private Float error;

    private Integer measdId;

    private Integer measmId;

    private Double measVal;

    private String lessThanLOD;

    private Double detectLim;

    @Column(insertable = false, updatable = false)
    private Timestamp treeMod;

    @OneToOne
    @JoinColumn(insertable = false, updatable = false)
    private Measm measm;

    @Transient
    private boolean owner;

    @Transient
    private boolean readonly;

    @Transient
    private Timestamp parentModified;

    @Transient
    private MultivaluedMap<String, Integer> errors;

    @Transient
    private MultivaluedMap<String, Integer> warnings;

    @Transient
    private MultivaluedMap<String, Integer> notifications;

    public MeasVal() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getIsThreshold() {
        return this.isThreshold;
    }

    public void setIsThreshold(Boolean isThreshold) {
        this.isThreshold = isThreshold;
    }

    public Timestamp getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Timestamp lastMod) {
        this.lastMod = lastMod;
    }

    public Integer getMeasUnitId() {
        return this.measUnitId;
    }

    public void setMeasUnitId(Integer unitId) {
        this.measUnitId = unitId;
    }

    public Float getError() {
        return this.error;
    }

    public void setError(Float error) {
        this.error = error;
    }

    public Integer getMeasdId() {
        return this.measdId;
    }

    public void setMeasdId(Integer measdId) {
        this.measdId = measdId;
    }

    @JsonbTransient
    public Measm getMeasm() {
        return this.measm;
    }

    public Integer getMeasmId() {
        return this.measmId;
    }

    public void setMeasmId(Integer measmId) {
        this.measmId = measmId;
    }

    public Double getMeasVal() {
        return this.measVal;
    }

    public void setMeasVal(Double measVal) {
        this.measVal = measVal;
    }

    public String getLessThanLOD() {
        return this.lessThanLOD;
    }

    public void setLessThanLOD(String lessThanLOD) {
        this.lessThanLOD = lessThanLOD;
    }

    public Double getDetectLim() {
        return this.detectLim;
    }

    public void setDetectLim(Double detectLim) {
        this.detectLim = detectLim;
    }

    public Timestamp getTreeMod() {
        return this.treeMod;
    }

    public void setTreeMod(Timestamp treeMod) {
        this.treeMod = treeMod;
    }

    /**
     * @return the owner
     */
    public boolean isOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    /**
     * @return the readonly
     */
    public boolean isReadonly() {
        return readonly;
    }

    /**
     * @param readonly the readonly to set
     */
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    /**
     * Check if a parent object was modified.
     * @return timestamp when the parent was modified
     */
    public Timestamp getParentModified() {
        if (this.parentModified == null && this.measm != null) {
            return this.measm.getTreeMod();
        }
        return this.parentModified;
    }

    public void setParentModified(Timestamp parentModified) {
        this.parentModified = parentModified;
    }

    public MultivaluedMap<String, Integer> getErrors() {
        return this.errors;
    }

    @JsonbTransient
    public void setErrors(MultivaluedMap<String, Integer> errors) {
        this.errors = errors;
    }

    public MultivaluedMap<String, Integer> getWarnings() {
        return this.warnings;
    }

    @JsonbTransient
    public void setWarnings(MultivaluedMap<String, Integer> warnings) {
        this.warnings = warnings;
    }

    public MultivaluedMap<String, Integer> getNotifications() {
        return this.notifications;
    }

    @JsonbTransient
    public void setNotifications(
        MultivaluedMap<String, Integer> notifications
    ) {
        this.notifications = notifications;
    }
}