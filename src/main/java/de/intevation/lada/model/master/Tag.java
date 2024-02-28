/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.util.Date;
import java.util.Set;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.TagLink;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.NetworkMatchingMeasFacil;
import de.intevation.lada.validation.constraints.Unique;
import de.intevation.lada.validation.groups.DatabaseConstraints;

@Entity
@Table(schema = SchemaName.NAME)
@GroupSequence({ Tag.class, DatabaseConstraints.class })
@Unique(groups = DatabaseConstraints.class,
    clazz = Tag.class, fields = { "name", "networkId", "measFacilId" })
@NetworkMatchingMeasFacil(groups = DatabaseConstraints.class)
public class Tag extends BaseModel {

    // Default time after which mst tags expire in days
    public static final int MST_TAG_EXPIRATION_TIME = 365;

    // Default time after which auto tags expire in days
    public static final int GENERATED_EXPIRATION_TIME = 584;

    // Tag type ids
    public static final String TAG_TYPE_GLOBAL = "global";
    public static final String TAG_TYPE_NETZBETREIBER = "netz";
    public static final String TAG_TYPE_MST = "mst";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    private String name;

    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = MeasFacil.class)
    private String measFacilId;


    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = Network.class)
    private String networkId;


    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = LadaUser.class)
    private Integer ladaUserId;

    @NotBlank
    private String tagType;

    @Temporal(TIMESTAMP)
    private Date valUntil;

    @Column(insertable = false, updatable = false)
    @Temporal(TIMESTAMP)
    private Date createdAt;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "tag_id", updatable = false)
    @JsonbTransient
    private Set<TagLink> tagZuordnungs;

    private boolean isAutoTag;

    public Tag() { }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String tag) {
        this.name = tag;
    }

    public String getMeasFacilId() {
        return this.measFacilId;
    }

    public void setMeasFacilId(String measFacilId) {
        this.measFacilId = measFacilId;
    }

    public Set<TagLink> getTagZuordnungs() {
        return this.tagZuordnungs;
    }

    public void setTagZuordnungs(Set<TagLink> tagZuordnungs) {
        this.tagZuordnungs = tagZuordnungs;
    }

    public boolean getIsAutoTag() {
        return this.isAutoTag;
    }

    public void setIsAutoTag(boolean isAutoTag) {
        this.isAutoTag = isAutoTag;
    }


    public Date getValUntil() {
        return valUntil;
    }

    public void setValUntil(Date valUntil) {
        this.valUntil = valUntil;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * @return ID of Netzbetreiber associated to this tag.
     */
    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public Integer getLadaUserId() {
        return ladaUserId;
    }

    public void setLadaUserId(Integer ladaUserId) {
        this.ladaUserId = ladaUserId;
    }

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }
}
