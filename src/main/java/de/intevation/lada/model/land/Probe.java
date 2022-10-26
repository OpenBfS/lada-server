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

import de.intevation.lada.model.stammdaten.Umwelt;
import de.intevation.lada.util.data.EmptyStringConverter;


/**
 * The persistent class for the probe database table.
 *
 */
@Entity
@DynamicInsert(true)
@Table(name = "sample", schema = SchemaName.NAME)
public class Probe implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer oprModeId;

    private Integer regulationId;

    private Integer datasetCreatorId;

    @Convert(converter = EmptyStringConverter.class)
    private String mainSampleId;

    @Convert(converter = EmptyStringConverter.class)
    private String extId;

    private String apprLabId;

    @Column(insertable = false)
    private Timestamp lastMod;

    private String envDescripName;

    private String envDescripDisplay;

    private Long midSampleDate;

    private Integer stateMpgId;

    private Integer mpgId;

    private String measFacilId;

    private Integer samplerId;

    private Timestamp sampleStartDate;

    private Timestamp sampleEndDate;

    private Integer sampleMethId;

    private Timestamp schedStartDate;

    private Timestamp schedEndDate;

    private Timestamp origDate;

    private Boolean isTest;

    @Column(insertable = false, updatable = false)
    private Timestamp treeMod;

    @OneToOne
    @JoinColumn(name = "env_medium_id", insertable = false, updatable = false)
    private Umwelt umwelt;

    private String envMediumId;

    private Integer reiAgGrId;

    private Integer nuclFacilGrId;

    @Transient
    private boolean readonly;

    @Transient
    private boolean owner;

    @Transient
    @JsonbTransient
    private boolean found;

    @Transient
    private MultivaluedMap<String, Integer> errors;

    @Transient
    private MultivaluedMap<String, Integer> warnings;

    @Transient
    private MultivaluedMap<String, Integer> notifications;

    public Probe() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOprModeId() {
        return this.oprModeId;
    }

    public void setOprModeId(Integer oprModeId) {
        this.oprModeId = oprModeId;
    }

    public Integer getRegulationId() {
        return this.regulationId;
    }

    public void setRegulationId(Integer regulationId) {
        this.regulationId = regulationId;
    }

    public Integer getDatasetCreatorId() {
        return this.datasetCreatorId;
    }

    public void setDatasetCreatorId(Integer datasetCreatorId) {
        this.datasetCreatorId = datasetCreatorId;
    }

    public String getMainSampleId() {
        return this.mainSampleId;
    }

    public void setMainSampleId(String mainSampleId) {
        this.mainSampleId = mainSampleId;
    }

    public String getExtId() {
        return this.extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public String getApprLabId() {
        return this.apprLabId;
    }

    public void setApprLabId(String apprLabId) {
        this.apprLabId = apprLabId;
    }

    public Timestamp getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Timestamp lastMod) {
        this.lastMod = lastMod;
    }

    public String getEnvDescripName() {
        return this.envDescripName;
    }

    public void setEnvDescripName(String envDescripName) {
        this.envDescripName = envDescripName;
    }

    public String getEnvDescripDisplay() {
        return this.envDescripDisplay;
    }

    public void setEnvDescripDisplay(String envDescripDisplay) {
        this.envDescripDisplay = envDescripDisplay;
    }

    public Long getMidSampleDate() {
        return this.midSampleDate;
    }

    public void setMidSampleDate(Long midSampleDate) {
        this.midSampleDate = midSampleDate;
    }

    public Integer getStateMpgId() {
        return this.stateMpgId;
    }

    public void setStateMpgId(Integer stateMpgId) {
        this.stateMpgId = stateMpgId;
    }

    public Integer getMpgId() {
        return this.mpgId;
    }

    public void setMpgId(Integer mpgId) {
        this.mpgId = mpgId;
    }

    public String getMeasFacilId() {
        return this.measFacilId;
    }

    public void setMeasFacilId(String measFacilId) {
        this.measFacilId = measFacilId;
    }

    public Integer getSamplerId() {
        return this.samplerId;
    }

    public void setSamplerId(Integer samplerId) {
        this.samplerId = samplerId;
    }

    public Timestamp getSampleStartDate() {
        return this.sampleStartDate;
    }

    public void setSampleStartDate(Timestamp sampleStartDate) {
        this.sampleStartDate = sampleStartDate;
    }

    public Timestamp getSampleEndDate() {
        return this.sampleEndDate;
    }

    public void setSampleEndDate(Timestamp sampleEndDate) {
        this.sampleEndDate = sampleEndDate;
    }

    public Integer getSampleMethId() {
        return this.sampleMethId;
    }

    public void setSampleMethId(Integer sampleMethId) {
        this.sampleMethId = sampleMethId;
    }

    public Timestamp getSchedStartDate() {
        return this.schedStartDate;
    }

    public void setSchedStartDate(Timestamp schedStartDate) {
        this.schedStartDate = schedStartDate;
    }

    public Timestamp getSchedEndDate() {
        return this.schedEndDate;
    }

    public void setSchedEndDate(Timestamp schedEndDate) {
        this.schedEndDate = schedEndDate;
    }

    public Timestamp getOrigDate() {
        return this.origDate;
    }

    public void setOrigDate(Timestamp origDate) {
        this.origDate = origDate;
    }

    public Boolean getIsTest() {
        return this.isTest;
    }

    public void setIsTest(Boolean isTest) {
        this.isTest = isTest;
    }

    public Timestamp getTreeMod() {
        return this.treeMod;
    }

    public void setTreeMod(Timestamp treeMod) {
        this.treeMod = treeMod;
    }

    @JsonbTransient
    public Umwelt getUmwelt() {
        return this.umwelt;
    }

    public String getEnvMediumId() {
        return this.envMediumId;
    }

    public void setEnvMediumId(String envMediumId) {
        this.envMediumId = envMediumId;
    }

    public Integer getReiAgGrId() {
        return reiAgGrId;
    }

    public void setReiAgGrId(Integer reiAgGrId) {
        this.reiAgGrId = reiAgGrId;
    }

    public Integer getNuclFacilGrId() {
        return nuclFacilGrId;
    }

    public void setNuclFacilGrId(Integer nuclFacilGrId) {
        this.nuclFacilGrId = nuclFacilGrId;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isOwner() {
        return owner;
    }

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
    public void setNotifications(
        MultivaluedMap<String, Integer> notifications
    ) {
      this.notifications = notifications;
    }

    public MultivaluedMap<String, Integer> getNotifications() {
       return this.notifications;
    }

    @JsonbTransient
    public void setWarnings(MultivaluedMap<String, Integer> warnings) {
        this.warnings = warnings;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }
}
