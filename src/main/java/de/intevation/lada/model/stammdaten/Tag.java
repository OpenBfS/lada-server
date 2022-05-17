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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
    public static final int AUTO_TAG_EXPIRATION_TIME = 584;

    // Tag type ids
    public static final String TAG_TYPE_GLOBAL = "global";
    public static final String TAG_TYPE_NETZBETREIBER = "netzbetreiber";
    public static final String TAG_TYPE_MST = "mst";
    public static final String TAG_TYPE_AUTO = "auto";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tag")
    private String tag;

    @Column(name = "mst_id")
    private String mstId;

    @Column(name = "netzbetreiber")
    private String netzbetreiberId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "netzbetreiber", insertable = false, updatable = false)
    @JsonIgnore
    private NetzBetreiber netzbetreiber;

    @Column(name = "user_id")
    private Integer userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnore
    private LadaUser user;

    @Column(name = "typ")
    private String typId;

    @Column(name = "gueltig_bis")
    private Timestamp gueltigBis;

    @Column(name = "generated_at", insertable = false, updatable = false)
    private Timestamp generatedAt;

    @OneToMany
    @JoinColumn(name = "tag_id")
    @JsonIgnore
    private Set<TagZuordnung> tagZuordnungs;

    private boolean generated;

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

    public boolean getGenerated() {
        return this.generated;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
    }


    public NetzBetreiber getNetzbetreiber() {
        return netzbetreiber;
    }

    public void setNetzbetreiber(NetzBetreiber netzbetreiber) {
        this.netzbetreiber = netzbetreiber;
    }

    public LadaUser getUser() {
        return user;
    }

    public void setUser(LadaUser user) {
        this.user = user;
    }

    public Timestamp getGueltigBis() {
        return gueltigBis;
    }

    public void setGueltigBis(Timestamp gueltigBis) {
        this.gueltigBis = gueltigBis;
    }

    public Timestamp getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Timestamp generatedAt) {
        this.generatedAt = generatedAt;
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
        if (this.netzbetreiberId == null && this.netzbetreiber != null) {
            this.netzbetreiberId = this.netzbetreiber.getId();
        }
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
