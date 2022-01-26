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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The persistent class for the tag database table.
 */
@Entity
@Table(name = "tag", schema = SchemaName.NAME)
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tag")
    private String tag;

    @Column(name = "mst_id")
    private String mstId;

    @OneToOne
    @JoinColumn(name = "netzbetreiber", referencedColumnName = "id")
    private NetzBetreiber netzbetreiber;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private LadaUser user;

    @OneToOne
    @JoinColumn(name = "typ", referencedColumnName = "id")
    private TagTyp typ;

    @Column(name = "gueltig_bis")
    private Timestamp gueltigBis;

    @Column(name = "generated_at")
    private Timestamp generatedAt;

    @OneToMany(mappedBy = "tag", fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<TagZuordnung> tagZuordnungs;

    private boolean generated;


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

    public TagTyp getTyp() {
        return typ;
    }

    public void setTyp(TagTyp typ) {
        this.typ = typ;
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
}
