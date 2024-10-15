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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import jakarta.persistence.Transient;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.DynamicInsert;

import de.intevation.lada.model.master.Mmt;
import de.intevation.lada.validation.constraints.HasMeasmStartDateRegulation1;
import de.intevation.lada.validation.constraints.HasMeasmStartDateRegulationNot1;
import de.intevation.lada.validation.constraints.HasMeasPdNotSampleMeth9OrRegulation1;
import de.intevation.lada.validation.constraints.HasMeasPdSampleMeth9OrRegulation1;
import de.intevation.lada.validation.constraints.HasObligMeasds;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.MeasuringAfterSampling;
import de.intevation.lada.validation.constraints.NotEmptyNorWhitespace;
import de.intevation.lada.validation.constraints.Unique;
import de.intevation.lada.validation.groups.DatabaseConstraints;
import de.intevation.lada.validation.groups.Notifications;
import de.intevation.lada.validation.groups.Warnings;


// The DynamicInsert Annotation has the effect, that the persisted object still
// has all the "null"-values. There is no reloading after the persistence
// process!
@Entity
@DynamicInsert(true)
@Table(schema = SchemaName.NAME)
@GroupSequence({ Measm.class, DatabaseConstraints.class })
@Unique(groups = DatabaseConstraints.class,
    clazz = Measm.class, fields = { "minSampleId", "sampleId" })
@Unique(groups = DatabaseConstraints.class,
    clazz = Measm.class, fields = { "extId", "sampleId" })
@MeasuringAfterSampling(groups = Warnings.class)
@HasMeasPdNotSampleMeth9OrRegulation1(groups = Warnings.class)
@HasMeasPdSampleMeth9OrRegulation1(groups = Notifications.class)
@HasMeasmStartDateRegulation1(groups = Warnings.class)
@HasMeasmStartDateRegulationNot1(groups = Notifications.class)
@HasObligMeasds(groups = Notifications.class)
public class Measm extends BelongsToSample implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Boolean isCompleted;

    private Boolean isScheduled;

    private Integer extId;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    private Integer measPd;

    @Temporal(TIMESTAMP)
    @PastOrPresent(groups = Warnings.class)
    private Date measmStartDate;

    @NotBlank
    @Size(max = 2)
    @IsValidPrimaryKey(groups = DatabaseConstraints.class, clazz = Mmt.class)
    private String mmtId;

    @Size(max = 4)
    @NotEmptyNorWhitespace
    @NotBlank(groups = Notifications.class)
    private String minSampleId;

    @OneToOne
    @JoinColumn(name = "status", insertable = false, updatable = false)
    private StatusProt statusProt;

    @Column(insertable = false, updatable = false)
    @Temporal(TIMESTAMP)
    private Date treeMod;

    @Transient
    private Boolean statusEdit;

    @Transient
    private Boolean statusEditMst;

    @Transient
    private Boolean statusEditLand;

    @Transient
    private Boolean statusEditLst;


    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getIsCompleted() {
        return this.isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public Boolean getIsScheduled() {
        return this.isScheduled;
    }

    public void setIsScheduled(Boolean isScheduled) {
        this.isScheduled = isScheduled;
    }

    public Integer getExtId() {
        return this.extId;
    }

    public void setExtId(Integer extId) {
        this.extId = extId;
    }

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }

    public Integer getMeasPd() {
        return this.measPd;
    }

    public void setMeasPd(Integer measPd) {
        this.measPd = measPd;
    }

    public Date getMeasmStartDate() {
        return this.measmStartDate;
    }

    public void setMeasmStartDate(Date measmStartDate) {
        this.measmStartDate = measmStartDate;
    }

    public String getMmtId() {
        return this.mmtId;
    }

    public void setMmtId(String mmtId) {
        this.mmtId = mmtId;
    }

    public String getMinSampleId() {
        return this.minSampleId;
    }

    public void setMinSampleId(String minSampleId) {
        this.minSampleId = minSampleId;
    }

    public Date getTreeMod() {
        return this.treeMod;
    }

    public void setTreeMod(Date treeMod) {
        this.treeMod = treeMod;
    }

    /**
     * @return the statusEdit
     */
    public Boolean getStatusEdit() {
        return statusEdit;
    }
    public Boolean getStatusEditMst() {
        return statusEditMst;
    }
    public Boolean getStatusEditLand() {
        return statusEditLand;
    }
    public Boolean getStatusEditLst() {
        return statusEditLst;
    }

    /**
     * @param statusEdit the statusEdit to set
     */
    public void setStatusEdit(Boolean statusEdit) {
        this.statusEdit = statusEdit;
    }
    public void setStatusEditMst(Boolean statusEditMst) {
        this.statusEditMst = statusEditMst;
    }
    public void setStatusEditLand(Boolean statusEditLand) {
        this.statusEditLand = statusEditLand;
    }
    public void setStatusEditLst(Boolean statusEditLst) {
        this.statusEditLst = statusEditLst;
    }

    public StatusProt getStatusProt() {
        return this.statusProt;
    }
}
