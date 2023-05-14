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
import java.util.List;

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
import javax.persistence.Temporal;
import static javax.persistence.TemporalType.TIMESTAMP;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.DynamicInsert;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.master.EnvMedium;
import de.intevation.lada.util.data.EmptyStringConverter;


@Entity
@DynamicInsert(true)
@Table(schema = SchemaName.NAME)
public class Sample extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private Integer oprModeId;

    @NotNull
    private Integer regulationId;

    private Integer datasetCreatorId;

    @Convert(converter = EmptyStringConverter.class)
    private String mainSampleId;

    @Convert(converter = EmptyStringConverter.class)
    @Size(max = 16)
    private String extId;

    @NotBlank
    @Size(max = 5)
    private String apprLabId;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    @Size(max = 100)
    private String envDescripName;

    @Pattern(regexp = "D:( [0-9][0-9]){12}")
    private String envDescripDisplay;

    private Long midSampleDate;

    private Integer mpgCategId;

    private Integer mpgId;

    @NotBlank
    @Size(max = 5)
    private String measFacilId;

    private Integer samplerId;

    @Temporal(TIMESTAMP)
    private Date sampleStartDate;

    @Temporal(TIMESTAMP)
    private Date sampleEndDate;

    @NotNull
    private Integer sampleMethId;

    @Temporal(TIMESTAMP)
    private Date schedStartDate;

    @Temporal(TIMESTAMP)
    private Date schedEndDate;

    @Temporal(TIMESTAMP)
    private Date origDate;

    @NotNull
    private Boolean isTest;

    @Column(insertable = false, updatable = false)
    @Temporal(TIMESTAMP)
    private Date treeMod;

    @OneToOne
    @JoinColumn(insertable = false, updatable = false)
    private EnvMedium envMedium;

    @Size(max = 3)
    private String envMediumId;

    private Integer reiAgGrId;

    private Integer nuclFacilGrId;

    @Transient
    private boolean readonly;

    @Transient
    private boolean owner;

    //Transient fields used for Site object generation
    @Transient
    @JsonbTransient
    private boolean found;

    @Transient
    private List<String> mmt;

    @Transient
    private String gemId;


    public Sample() {
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

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
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

    public Integer getMpgCategId() {
        return this.mpgCategId;
    }

    public void setMpgCategId(Integer stateMpgId) {
        this.mpgCategId = stateMpgId;
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

    public Date getSampleStartDate() {
        return this.sampleStartDate;
    }

    public void setSampleStartDate(Date sampleStartDate) {
        this.sampleStartDate = sampleStartDate;
    }

    public Date getSampleEndDate() {
        return this.sampleEndDate;
    }

    public void setSampleEndDate(Date sampleEndDate) {
        this.sampleEndDate = sampleEndDate;
    }

    public Integer getSampleMethId() {
        return this.sampleMethId;
    }

    public void setSampleMethId(Integer sampleMethId) {
        this.sampleMethId = sampleMethId;
    }

    public Date getSchedStartDate() {
        return this.schedStartDate;
    }

    public void setSchedStartDate(Date schedStartDate) {
        this.schedStartDate = schedStartDate;
    }

    public Date getSchedEndDate() {
        return this.schedEndDate;
    }

    public void setSchedEndDate(Date schedEndDate) {
        this.schedEndDate = schedEndDate;
    }

    public Date getOrigDate() {
        return this.origDate;
    }

    public void setOrigDate(Date origDate) {
        this.origDate = origDate;
    }

    public Boolean getIsTest() {
        return this.isTest;
    }

    public void setIsTest(Boolean isTest) {
        this.isTest = isTest;
    }

    public Date getTreeMod() {
        return this.treeMod;
    }

    public void setTreeMod(Date treeMod) {
        this.treeMod = treeMod;
    }

    @JsonbTransient
    public EnvMedium getEnvMedium() {
        return this.envMedium;
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

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public List<String> getMmt() {
        return mmt;
    }

    public void setMmt(List<String> mmt) {
        this.mmt = mmt;
    }

    public String getGemId() {
        return gemId;
    }

    public void setGemId(String gemId) {
        this.gemId = gemId;
    }
}
