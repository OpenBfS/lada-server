/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.land;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import de.intevation.lada.model.stammdaten.Tag;

/**
 * The persistent class for the tagzuordnung database table.
 */
@Entity
@Table(name = "tagzuordnung", schema = SchemaName.NAME)
public class TagZuordnung {
    public static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "probe_id")
    private Integer probeId;

    @Column(name = "messung_id")
    private Integer messungId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Transient
    private Integer tagId;

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMessungId() {
        return this.messungId;
    }

    public void setMessungId(Integer messungId) {
        this.messungId = messungId;
    }

    public Integer getProbeId() {
        return this.probeId;
    }

    public void setProbeId(Integer probe) {
        this.probeId = probe;
    }

    public Tag getTag() {
        return this.tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    /**
     * @return ID of the referenced tag
     */
    public Integer getTagId() {
        if (this.tagId == null && this.tag != null) {
            this.tagId = this.tag.getId();
        }
        return this.tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    /**
     * Create json object representation of this object.
     * @return JSON object
     */
    public JsonObject toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        JsonObjectBuilder tagBuilder = Json.createObjectBuilder();
        builder.add("id", id.toString());
        if (tag.getId() != null) {
            tagBuilder.add("id", tag.getId().toString());
        }
        if (tag.getTag() != null) {
            tagBuilder.add("tag", tag.getTag());
        }
        if (messungId != null) {
            builder.add("messungId", messungId);
        }
        if (probeId != null) {
            builder.add("probeId", probeId);
        }
        if (tagId != null) {
            builder.add("tagId", tagId);
        }
        builder.add("tag", tagBuilder.build());
        return builder.build();
    }
}
