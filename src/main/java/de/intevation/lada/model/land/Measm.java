/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.land;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.ws.rs.core.MultivaluedMap;

import org.hibernate.annotations.DynamicInsert;

import de.intevation.lada.util.data.EmptyStringConverter;


// The DynamicInsert Annotation has the effect, that the persisted object still
// has all the "null"-values. There is no reloading after the persistence
// process!
@Entity
@DynamicInsert(true)
@Table(schema = SchemaName.NAME)
public class Measm implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Boolean isCompleted;

    private Boolean isScheduled;

    private Integer extId;

    @Column(insertable = false)
    private Timestamp lastMod;

    private Integer measPd;

    private Timestamp measmStartDate;

    private String mmtId;

    @Convert(converter = EmptyStringConverter.class)
    private String minSampleId;

    private Integer sampleId;

    @OneToOne
    @JoinColumn(name = "sample_id", insertable = false, updatable = false)
    private Sample sample;

    private Integer status;

    @OneToOne
    @JoinColumn(name = "status", insertable = false, updatable = false)
    private StatusProt statusProtocol;

    @Column(insertable = false, updatable = false)
    private Timestamp treeMod;

    @Transient
    private Boolean statusEdit;

    @Transient
    private Boolean statusEditMst;

    @Transient
    private Boolean statusEditLand;

    @Transient
    private Boolean statusEditLst;

    @Transient
    private Timestamp parentModified;

    @Transient
    @JsonbTransient
    private MultivaluedMap<String, Integer> errors;

    @Transient
    private MultivaluedMap<String, Integer> warnings;

    @Transient
    private MultivaluedMap<String, Integer> notifications;

    @Transient
    private boolean owner;

    @Transient
    private boolean readonly;

    public Measm() {
    }

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

    public Timestamp getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Timestamp lastMod) {
        this.lastMod = lastMod;
    }

    public Integer getMeasPd() {
        return this.measPd;
    }

    public void setMeasPd(Integer measPd) {
        this.measPd = measPd;
    }

    public Timestamp getMeasmStartDate() {
        return this.measmStartDate;
    }

    public void setMeasmStartDate(Timestamp measmStartDate) {
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

    @JsonbTransient
    public Sample getSample() {
        return this.sample;
    }

    public Integer getSampleId() {
        return this.sampleId;
    }

    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Timestamp getTreeMod() {
        return this.treeMod;
    }

    public void setTreeMod(Timestamp treeMod) {
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

    /**
     * @return the parentModified
     */
    public Timestamp getParentModified() {
        if (this.parentModified == null && this.sample != null) {
            return this.sample.getTreeMod();
        }
        return parentModified;
    }

    /**
     * @param parentModified the parentModified to set
     */
    public void setParentModified(Timestamp parentModified) {
        this.parentModified = parentModified;
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

    public StatusProt getStatusProtocol() {
        return this.statusProtocol;
    }

    public void setStatusProtocol(StatusProt statusProtocol) {
        this.statusProtocol = statusProtocol;
    }

}
