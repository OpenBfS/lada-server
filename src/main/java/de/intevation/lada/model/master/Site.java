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

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import static javax.persistence.TemporalType.TIMESTAMP;
import javax.persistence.Transient;

import org.locationtech.jts.geom.Point;

import org.hibernate.annotations.Type;

import de.intevation.lada.model.BaseModel;

@Entity
@Table(schema = SchemaName.NAME)
public class Site extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Boolean isReiActive;

    private Integer nuclFacilGrId;

    private String reiReportText;

    @ManyToOne
    @JoinColumn(updatable = false, insertable = false)
    private AdminUnit adminUnit;

    private String adminUnitId;

    private Integer municDivId;

    private Float heightAsl;

    private Float alt;

    private String coordXExt;

    private String coordYExt;

    private String shortText;

    private String longText;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    private String reiOprMode;

    private String networkId;

    private String extId;

    private Integer siteClassId;

    private String poiId;

    private String reiSector;

    private Integer stateId;

    private Boolean isFuzzy;

    private String reiZone;

    private String reiCompetence;

    private Integer spatRefSysId;

    private Integer reiAgGrId;

    @Type(type = "jts_geometry")
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point geom;

    @Transient
    private boolean readonly;

    @Transient
    private Double longitude;

    @Transient
    private Double latitude;

    @Transient
    private Integer referenceCount;

    @Transient
    private Integer plausibleReferenceCount;

    @Transient
    private Integer referenceCountMp;

    public Site() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getIsReiActive() {
        return this.isReiActive;
    }

    public void setIsReiActive(Boolean isReiActive) {
        this.isReiActive = isReiActive;
    }

    public Integer getNuclFacilGrId() {
        return this.nuclFacilGrId;
    }

    public void setNuclFacilGrId(Integer reiNuclFacilGrId) {
        this.nuclFacilGrId = reiNuclFacilGrId;
    }

    public String getReiReportText() {
        return this.reiReportText;
    }

    public void setReiReportText(String reiReportText) {
        this.reiReportText = reiReportText;
    }

    @JsonbTransient
    public AdminUnit getAdminUnit() {
        return this.adminUnit;
    }

    public void setAdminUnit(AdminUnit munic) {
        this.adminUnit = munic;
    }

    public String getAdminUnitId() {
        return this.adminUnitId;
    }

    public void setAdminUnitId(String municId) {
        this.adminUnitId = municId;
    }

    public Integer getMunicDivId() {
        return this.municDivId;
    }

    public void setMunicDivId(Integer municDivId) {
        this.municDivId = municDivId;
    }

    public Integer getReiAgGrId() {
        return this.reiAgGrId;
    }

    public void setReiAgGrId(Integer reiAgGrId) {
        this.reiAgGrId = reiAgGrId;
    }

    public Float getHeightAsl() {
        return this.heightAsl;
    }

    public void setHeightAsl(Float heightAsl) {
        this.heightAsl = heightAsl;
    }

    public Float getAlt() {
        return this.alt;
    }

    public void setAlt(Float alt) {
        this.alt = alt;
    }

    public String getCoordXExt() {
        return this.coordXExt;
    }

    public void setCoordXExt(String coordXExt) {
        this.coordXExt = coordXExt;
    }

    public String getCoordYExt() {
        return this.coordYExt;
    }

    public void setCoordYExt(String coordYExt) {
        this.coordYExt = coordYExt;
    }

    public String getShortText() {
        return this.shortText;
    }

    public void setShortText(String shortText) {
        this.shortText = shortText;
    }

    public String getLongText() {
        return this.longText;
    }

    public void setLongText(String longText) {
        this.longText = longText;
    }

    /**
     * Get the lat.
     * @return the lat
     */
    public Double getLatitude() {
        // We might want to serialize an object without geom
        return this.geom != null
            ? this.geom.getY()
            : null;
    }

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }

    /**
     * Get the lon.
     * @return the lon
     */
    public Double getLongitude() {
        // We might want to serialize an object without geom
        return this.geom != null
            ? this.geom.getX()
            : null;
    }

    public String getReiOprMode() {
        return this.reiOprMode;
    }

    public void setReiOprMode(String reiOprMode) {
        this.reiOprMode = reiOprMode;
    }

    public String getNetworkId() {
        return this.networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getExtId() {
        return this.extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public Integer getSiteClassId() {
        return this.siteClassId;
    }

    public void setSiteClassId(Integer siteClassId) {
        this.siteClassId = siteClassId;
    }

    public String getPoiId() {
        return this.poiId;
    }

    public void setPoiId(String poiId) {
        this.poiId = poiId;
    }

    public String getReiSector() {
        return this.reiSector;
    }

    public void setReiSector(String reiSector) {
        this.reiSector = reiSector;
    }

    public Integer getStateId() {
        return this.stateId;
    }

    public void setStateId(Integer stateId) {
        this.stateId = stateId;
    }

    public Boolean getIsFuzzy() {
        return this.isFuzzy;
    }

    public void setIsFuzzy(Boolean isFuzzy) {
        this.isFuzzy = isFuzzy;
    }

    public String getReiZone() {
        return this.reiZone;
    }

    public void setReiZone(String reiZone) {
        this.reiZone = reiZone;
    }

    public String getReiCompetence() {
        return this.reiCompetence;
    }

    public void setReiCompetence(String reiCompetence) {
        this.reiCompetence = reiCompetence;
    }

    public Integer getSpatRefSysId() {
        return this.spatRefSysId;
    }

    public void setSpatRefSysId(Integer spatRefSysId) {
        this.spatRefSysId = spatRefSysId;
    }

    @JsonbTransient
    public Point getGeom() {
        return geom;
    }

    @JsonbTransient
    public void setGeom(Point geom) {
        this.geom = geom;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public Integer getReferenceCount() {
        return this.referenceCount;
    }

    public void setReferenceCount(Integer referenceCount) {
        this.referenceCount = referenceCount;
    }

    public Integer getPlausibleReferenceCount() {
        return this.plausibleReferenceCount;
    }

    public void setPlausibleReferenceCount(Integer plausibleReferenceCount) {
        this.plausibleReferenceCount = plausibleReferenceCount;
    }

    public Integer getReferenceCountMp() {
        return this.referenceCountMp;
    }

    public void setReferenceCountMp(Integer referenceCountMp) {
        this.referenceCountMp = referenceCountMp;
    }

}
