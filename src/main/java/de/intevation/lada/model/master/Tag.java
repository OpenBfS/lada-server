/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import de.intevation.lada.model.land.TagZuordnung;

import java.sql.Timestamp;
import java.util.Set;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(schema = SchemaName.NAME)
public class Tag {

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

    private String name;

    private String measFacilId;

    private String networkId;

    private Integer ladaUserId;

    private String tagType;

    private Timestamp valUntil;

    @Column(insertable = false, updatable = false)
    private Timestamp createdAt;

    @OneToMany
    @JoinColumn(name = "tag_id", updatable = false)
    @JsonbTransient
    private Set<TagZuordnung> tagZuordnungs;

    private boolean isAutoTag;

    @Transient
    private boolean readonly;

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

    public Set<TagZuordnung> getTagZuordnungs() {
        return this.tagZuordnungs;
    }

    public void setTagZuordnungs(Set<TagZuordnung> tagZuordnungs) {
        this.tagZuordnungs = tagZuordnungs;
    }

    public boolean getIsAutoTag() {
        return this.isAutoTag;
    }

    public void setIsAutoTag(boolean isAutoTag) {
        this.isAutoTag = isAutoTag;
    }


    public Timestamp getValUntil() {
        return valUntil;
    }

    public void setValUntil(Timestamp valUntil) {
        this.valUntil = valUntil;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
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
