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
import java.util.Set;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import jakarta.persistence.Transient;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.GeolocatMpg;
import de.intevation.lada.validation.constraints.CanChangeCoordinates;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.NotEmptyNorWhitespace;
import de.intevation.lada.validation.constraints.Unique;
import de.intevation.lada.validation.groups.DatabaseConstraints;

@Entity
@EntityListeners(SiteListener.class)
@Table(schema = SchemaName.NAME)
@GroupSequence({ Site.class, DatabaseConstraints.class })
@Unique(fields = {"extId", "networkId"},
    groups = DatabaseConstraints.class, clazz = Site.class)
@CanChangeCoordinates(groups = DatabaseConstraints.class)
public class Site extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Boolean isReiActive;

    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = NuclFacilGr.class)
    private Integer nuclFacilGrId;

    @Size(max = 70)
    @NotEmptyNorWhitespace
    private String reiReportText;

    @ManyToOne
    @JoinColumn(updatable = false, insertable = false)
    private AdminUnit adminUnit;


    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = AdminUnit.class)
    private String adminUnitId;


    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = MunicDiv.class)
    private Integer municDivId;

    private Float heightAsl;

    private Float alt;

    @Size(max = 22)
    @NotEmptyNorWhitespace
    private String coordXExt;

    @Size(max = 22)
    @NotEmptyNorWhitespace
    private String coordYExt;

    @Size(max = 22)
    @NotEmptyNorWhitespace
    private String shortText;

    @Size(max = 100)
    @NotEmptyNorWhitespace
    private String longText;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    @Size(max = 10)
    @NotEmptyNorWhitespace
    private String reiOprMode;

    @NotBlank
    @Size(max = 2)
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = Network.class)
    private String networkId;

    @Size(max = 20)
    @NotEmptyNorWhitespace
    private String extId;


    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = SiteClass.class)
    private Integer siteClassId;


    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = Poi.class)
    private String poiId;

    @Size(max = 2)
    @NotEmptyNorWhitespace
    private String reiSector;


    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = State.class)
    private Integer stateId;

    private Boolean isFuzzy;

    @Size(max = 1)
    @NotEmptyNorWhitespace
    private String reiZone;

    @Size(max = 10)
    @NotEmptyNorWhitespace
    private String reiCompetence;


    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = SpatRefSys.class)
    private Integer spatRefSysId;


    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = ReiAgGr.class)
    private Integer reiAgGrId;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point geom;

    @Lob
    @JdbcTypeCode(value = SqlTypes.BINARY)
    @JsonbTransient
    private byte[] img;

    @Lob
    @JdbcTypeCode(value = SqlTypes.BINARY)
    @JsonbTransient
    private byte[] map;

    @NotEmptyNorWhitespace
    private String route;

    @Transient
    private Double longitude;

    @Transient
    private Double latitude;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "site_id", insertable = false, updatable = false)
    @JsonbTransient
    private Set<Geolocat> geolocats;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "site_id", insertable = false, updatable = false)
    @JsonbTransient
    private Set<GeolocatMpg> geolocatMpgs;

    @Transient
    private Integer referenceCount;

    @Transient
    //Number of plausible sample references
    //Updated in the entity listener
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

    public Integer getReferenceCount() {
        return this.geolocats != null
        ? this.geolocats.size() : 0;
    }

    public Integer getPlausibleReferenceCount() {
        return this.plausibleReferenceCount;
    }

    public void setPlausibleReferenceCount(Integer plausibleReferenceCount) {
        this.plausibleReferenceCount = plausibleReferenceCount;
    }

    public Integer getReferenceCountMp() {
        return this.geolocatMpgs != null
        ? this.geolocatMpgs.size() : 0;
    }

    public byte[] getImg() {
        return img;
    }

    public void setImg(byte[] img) {
        this.img = img;
    }

    public byte[] getMap() {
        return map;
    }

    public void setMap(byte[] map) {
        this.map = map;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public Set<Geolocat> getGeolocats() {
        return geolocats;
    }

    public void setGeolocats(Set<Geolocat> geolocats) {
        this.geolocats = geolocats;
    }

    public Set<GeolocatMpg> getGeolocatMpgs() {
        return geolocatMpgs;
    }

    public void setGeolocatMpgs(Set<GeolocatMpg> geolocatMpgs) {
        this.geolocatMpgs = geolocatMpgs;
    }
}
