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

import jakarta.json.bind.annotation.JsonbTransient;
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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.DynamicInsert;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.master.DatasetCreator;
import de.intevation.lada.model.master.EnvMedium;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.MpgCateg;
import de.intevation.lada.model.master.NuclFacilGr;
import de.intevation.lada.model.master.OprMode;
import de.intevation.lada.model.master.Regulation;
import de.intevation.lada.model.master.ReiAgGr;
import de.intevation.lada.model.master.SampleMeth;
import de.intevation.lada.model.master.Sampler;
import de.intevation.lada.validation.constraints.BeginBeforeEnd;
import de.intevation.lada.validation.constraints.DatesVsSampleMeth;
import de.intevation.lada.validation.constraints.EnvDescripDisplay;
import de.intevation.lada.validation.constraints.EnvDescripDisplayFirstPartsSet;
import de.intevation.lada.validation.constraints.EnvMediumForReiAgGr;
import de.intevation.lada.validation.constraints.HasEndDate;
import de.intevation.lada.validation.constraints.HasOneSiteOfOrigin;
import de.intevation.lada.validation.constraints.HasSamplingLocation;
import de.intevation.lada.validation.constraints.IsReiComplete;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.NotEmptyNorWhitespace;
import de.intevation.lada.validation.constraints.NoUnnecessaryReiAttributes;
import de.intevation.lada.validation.constraints.OrigDateVsStartDate;
import de.intevation.lada.validation.constraints.Unique;
import de.intevation.lada.validation.groups.Notifications;
import de.intevation.lada.validation.groups.Warnings;
import de.intevation.lada.validation.groups.DatabaseConstraints;


@Entity
@DynamicInsert(true)
@Table(schema = SchemaName.NAME)
@GroupSequence({ Sample.class, DatabaseConstraints.class })
@Unique(fields = {"mainSampleId", "isTest", "measFacilId"},
    groups = DatabaseConstraints.class, clazz = Sample.class)
@Unique(fields = {"extId"},
    groups = DatabaseConstraints.class, clazz = Sample.class)
@BeginBeforeEnd(groups = Warnings.class)
@DatesVsSampleMeth(groups = Warnings.class)
@OrigDateVsStartDate(groups = Warnings.class)
@EnvDescripDisplayFirstPartsSet(groups = Warnings.class)
@IsReiComplete(groups = Warnings.class)
@NoUnnecessaryReiAttributes(groups = Warnings.class)
@EnvMediumForReiAgGr(groups = Warnings.class)
@HasEndDate(groups = Warnings.class)
@HasOneSiteOfOrigin(groups = Warnings.class)
@HasSamplingLocation(groups = Warnings.class)
public class Sample extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = OprMode.class)
    private Integer oprModeId;

    @NotNull
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = Regulation.class)
    private Integer regulationId;

    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = DatasetCreator.class)
    private Integer datasetCreatorId;

    @NotEmptyNorWhitespace
    @NotBlank(groups = Notifications.class)
    private String mainSampleId;

    @NotEmptyNorWhitespace
    @Size(max = 16)
    private String extId;

    @NotBlank
    @Size(max = 5)
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = MeasFacil.class)
    private String apprLabId;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    @Size(max = 100)
    @NotEmptyNorWhitespace
    private String envDescripName;

    @Pattern(regexp = "D:( [0-9][0-9]){12}")
    @NotBlank(groups = Warnings.class)
    @EnvDescripDisplay(groups = Warnings.class)
    private String envDescripDisplay;

    private Long midSampleDate;

    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = MpgCateg.class)
    private Integer mpgCategId;

    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = Mpg.class)
    private Integer mpgId;

    @NotBlank
    @Size(max = 5)
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = MeasFacil.class)
    private String measFacilId;

    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = Sampler.class)
    private Integer samplerId;

    @NotNull(groups = Warnings.class)
    @PastOrPresent(groups = Warnings.class)
    @Temporal(TIMESTAMP)
    private Date sampleStartDate;

    @Temporal(TIMESTAMP)
    private Date sampleEndDate;

    @NotNull
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = SampleMeth.class)
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
    @NotBlank(groups = Warnings.class)
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = EnvMedium.class)
    private String envMediumId;

    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = ReiAgGr.class)
    private Integer reiAgGrId;

    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = NuclFacilGr.class)
    private Integer nuclFacilGrId;

    @Transient
    private boolean owner;

    //Transient fields used for Site object generation
    @Transient
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
