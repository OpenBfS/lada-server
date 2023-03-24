/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import static javax.persistence.TemporalType.TIMESTAMP;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;
import javax.ws.rs.core.MultivaluedMap;

import de.intevation.lada.model.master.SampleSpecif;

@Entity
@Table(schema = SchemaName.NAME)
public class Mpg implements Serializable {
    private static final long serialVersionUID = 1L;

    // Has to be kept in sync with database schema
    @PrePersist
    void setDefaults() {
        if (oprModeId == null) {
            oprModeId = 1;
        }
        if (samplePdOffset == null) {
            samplePdOffset = 0;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer oprModeId;

    private Integer regulationId;

    private String adminUnitId;

    private Integer validEndDate;

    private Integer validStartDate;

    private Integer samplePdOffset;

    private String commMpg;

    private String apprLabId;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    @Pattern(regexp = "D:( [0-9][0-9]){12}")
    private String envDescripDisplay;

    private String measFacilId;

    private Integer mpgCategId;

    private String commSample;

    private Integer samplerId;

    private Integer sampleMethId;

    private String samplePd;

    private Integer samplePdEndDate;

    private Integer samplePdStartDate;

    private Boolean isTest;

    private String envMediumId;

    private Integer reiAgGrId;

    private Integer nuclFacilGrId;

    private Boolean isActive;

    private Integer measUnitId;

    private String sampleQuant;

    @OneToMany(mappedBy = "mpgId", fetch = FetchType.EAGER)
    private Set<Sample> samples;

    @ManyToMany (fetch = FetchType.EAGER)
    @JoinTable(
        schema = SchemaName.NAME,
        inverseJoinColumns = @JoinColumn(name = "sample_specif_id")
    )
    private Set<SampleSpecif> sampleSpecifs;

    @Transient
    private int referenceCount;

    @Transient
    private MultivaluedMap<String, Integer> errors;

    @Transient
    private MultivaluedMap<String, Integer> warnings;

    @Transient
    private boolean readonly;

    public Mpg() {
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

    public String getAdminUnitId() {
        return this.adminUnitId;
    }

    public void setAdminUnitId(String municId) {
        this.adminUnitId = municId;
    }

    public Integer getValidEndDate() {
        return this.validEndDate;
    }

    public void setValidEndDate(Integer validEndDate) {
        this.validEndDate = validEndDate;
    }

    public Integer getValidStartDate() {
        return this.validStartDate;
    }

    public void setValidStartDate(Integer validStartDate) {
        this.validStartDate = validStartDate;
    }

    public Integer getSamplePdOffset() {
        return this.samplePdOffset;
    }

    public void setSamplePdOffset(Integer samplePdOffset) {
        this.samplePdOffset = samplePdOffset;
    }

    public String getCommMpg() {
        return this.commMpg;
    }

    public void setCommMpg(String commMpg) {
        this.commMpg = commMpg;
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

    public String getEnvDescripDisplay() {
        return this.envDescripDisplay;
    }

    public void setEnvDescripDisplay(String envDescripDisplay) {
        this.envDescripDisplay = envDescripDisplay;
    }

    public String getMeasFacilId() {
        return this.measFacilId;
    }

    public void setMeasFacilId(String measFacilId) {
        this.measFacilId = measFacilId;
    }

    public Integer getMpgCategId() {
        return this.mpgCategId;
    }

    public void setMpgCategId(Integer stateMpgId) {
        this.mpgCategId = stateMpgId;
    }

    public String getCommSample() {
        return this.commSample;
    }

    public void setCommSample(String commSample) {
        this.commSample = commSample;
    }

    public Integer getSamplerId() {
        return this.samplerId;
    }

    public void setSamplerId(Integer sampleId) {
        this.samplerId = sampleId;
    }

    public Integer getSampleMethId() {
        return this.sampleMethId;
    }

    public void setSampleMethId(Integer sampleMethId) {
        this.sampleMethId = sampleMethId;
    }

    public String getSamplePd() {
        return this.samplePd;
    }

    public void setSamplePd(String samplePd) {
        this.samplePd = samplePd;
    }

    public Integer getSamplePdEndDate() {
        return this.samplePdEndDate;
    }

    public void setSamplePdEndDate(Integer samplePdEndDate) {
        this.samplePdEndDate = samplePdEndDate;
    }

    public Integer getSamplePdStartDate() {
        return this.samplePdStartDate;
    }

    public void setSamplePdStartDate(Integer samplePdStartDate) {
        this.samplePdStartDate = samplePdStartDate;
    }

    public Boolean getIsTest() {
        return this.isTest;
    }

    public void setIsTest(Boolean isTest) {
        this.isTest = isTest;
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

    public void setAgGrId(Integer reiAgGrId) {
        this.reiAgGrId = reiAgGrId;
    }

    public Integer getNuclFacilGrId() {
        return nuclFacilGrId;
    }

    public void setNuclFacilGrId(Integer nuclFacilGrId) {
        this.nuclFacilGrId = nuclFacilGrId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getMeasUnitId() {
        return this.measUnitId;
    }

    public void setMeasUnitId(Integer unitId) {
        this.measUnitId = unitId;
    }

    public String getSampleQuant() {
        return this.sampleQuant;
    }

    public void setSampleQuant(String sampleQuant) {
        this.sampleQuant = sampleQuant;
    }

    public Set<SampleSpecif> getSampleSpecifs() {
        return sampleSpecifs;
    }

    public void setSampleSpecifs(Set<SampleSpecif> sampleSpecifs) {
        this.sampleSpecifs = sampleSpecifs;
    }

    /**
     * @return The number of Sample objects referencing this Messprogramm.
     */
    public int getReferenceCount() {
        if (this.samples != null) {
            return this.samples.size();
        }
        return 0;
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

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

}
