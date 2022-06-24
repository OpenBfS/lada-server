/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.stammdaten;

import de.intevation.lada.model.land.TagZuordnung;

import java.sql.Timestamp;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The persistent class for the tag database table.
 */
@Entity
@Table(name = "tag", schema = SchemaName.NAME)
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

    @Column(name = "tag")
    private String tag;

    @Column(name = "mst_id")
    private String mstId;

    @Column(name = "netzbetreiber_id")
    private String netzbetreiberId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "tag_typ")
    private String typId;

    @Column(name = "gueltig_bis")
    private Timestamp gueltigBis;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;

    @OneToMany
    @JoinColumn(name = "tag_id", updatable = false)
    @JsonIgnore
    private Set<TagZuordnung> tagZuordnungs;

    @Column(name = "auto_tag")
    private boolean autoTag;

    @Transient
    private boolean readonly;

    public Tag() { }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMstId() {
        return this.mstId;
    }

    public void setMstId(String mstId) {
        this.mstId = mstId;
    }

    public Set<TagZuordnung> getTagZuordnungs() {
        return this.tagZuordnungs;
    }

    public void setTagZuordnungs(Set<TagZuordnung> tagZuordnungs) {
        this.tagZuordnungs = tagZuordnungs;
    }

    public boolean getAutoTag() {
        return this.autoTag;
    }

    public void setAutoTag(boolean autoTag) {
        this.autoTag = autoTag;
    }


    public Timestamp getGueltigBis() {
        return gueltigBis;
    }

    public void setGueltigBis(Timestamp gueltigBis) {
        this.gueltigBis = gueltigBis;
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
    public String getNetzbetreiberId() {
        return netzbetreiberId;
    }

    public void setNetzbetreiberId(String netzbetreiberId) {
        this.netzbetreiberId = netzbetreiberId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTypId() {
        return typId;
    }

    public void setTypId(String typId) {
        this.typId = typId;
    }
}
