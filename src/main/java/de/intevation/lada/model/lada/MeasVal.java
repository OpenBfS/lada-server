/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import de.intevation.lada.model.master.MeasUnit;
import de.intevation.lada.model.master.Measd;
import de.intevation.lada.validation.constraints.HasDetectLim;
import de.intevation.lada.validation.constraints.HasErrorOrLessThanLOD;
import de.intevation.lada.validation.constraints.HasMeasValOrLessThanLOD;
import de.intevation.lada.validation.constraints.HasNotErrorAndLessThanLOD;
import de.intevation.lada.validation.constraints.HasNotMeasValAndLessThanLOD;
import de.intevation.lada.validation.constraints.IsMeasdPrimaryOrConvertibleTo;
import de.intevation.lada.validation.constraints.IsNormalized;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.MeasdMatchesMmt;
import de.intevation.lada.validation.constraints.NotEmptyNorWhitespace;
import de.intevation.lada.validation.constraints.Unique;
import de.intevation.lada.validation.groups.DatabaseConstraints;
import de.intevation.lada.validation.groups.Notifications;
import de.intevation.lada.validation.groups.Warnings;


@Entity
@Table(schema = SchemaName.NAME)
@GroupSequence({ MeasVal.class, DatabaseConstraints.class })
@Unique(fields = {"measdId", "measmId"},
    groups = DatabaseConstraints.class, clazz = MeasVal.class)
@HasDetectLim
@HasMeasValOrLessThanLOD(groups = Warnings.class)
@HasNotMeasValAndLessThanLOD(groups = Warnings.class)
@HasErrorOrLessThanLOD(groups = Warnings.class)
@HasNotErrorAndLessThanLOD(groups = Warnings.class)
@MeasdMatchesMmt(groups = Warnings.class)
@IsNormalized(groups = Warnings.class)
@IsMeasdPrimaryOrConvertibleTo(groups = Notifications.class)
public class MeasVal extends BelongsToMeasm implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Boolean isThreshold;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    @NotNull
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = MeasUnit.class)
    private Integer measUnitId;

    private Float error;

    @NotNull
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = Measd.class)
    private Integer measdId;

    @Positive(groups = Notifications.class)
    private Double measVal;

    @Size(max = 1)
    @NotEmptyNorWhitespace
    private String lessThanLOD;

    private Double detectLim;

    @Column(insertable = false, updatable = false)
    @Temporal(TIMESTAMP)
    private Date treeMod;


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

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
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

    public Date getTreeMod() {
        return this.treeMod;
    }

    public void setTreeMod(Date treeMod) {
        this.treeMod = treeMod;
    }
}
