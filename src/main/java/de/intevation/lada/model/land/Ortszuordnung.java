/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.land;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.ws.rs.core.MultivaluedMap;



@Entity
@Table(name = "geolocat", schema = SchemaName.NAME)
public class Ortszuordnung implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(insertable = false)
    private Timestamp lastMod;

    private Integer siteId;

    private String typeRegulation;

    private String addSiteText;

    private Integer sampleId;

    private String poiId;

    @Column(insertable = false, updatable = false)
    private Timestamp treeMod;

    @OneToOne
    @JoinColumn(name = "sample_id", insertable = false, updatable = false)
    private Sample sample;

    @Transient
    private MultivaluedMap<String, Integer> errors;

    @Transient
    private MultivaluedMap<String, Integer> warnings;

    @Transient
    private boolean owner;

    @Transient
    private boolean readonly;

    @Transient
    private Timestamp parentModified;

    public Ortszuordnung() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Timestamp getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Timestamp lastMod) {
        this.lastMod = lastMod;
    }

    public Integer getSiteId() {
        return this.siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    public String getTypeRegulation() {
        return this.typeRegulation;
    }

    public void setTypeRegulation(String typeRegulation) {
        this.typeRegulation = typeRegulation;
    }

    public String getAddSiteText() {
        return this.addSiteText;
    }

    public void setAddSiteText(String addSiteText) {
        this.addSiteText = addSiteText;
    }

    public Integer getSampleId() {
        return this.sampleId;
    }

    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    public String getPoiId() {
        return this.poiId;
    }

    public void setPoiId(String poiId) {
        this.poiId = poiId;
    }

    public Timestamp getTreeMod() {
        return this.treeMod;
    }

    public void setTreeMod(Timestamp treeMod) {
        this.treeMod = treeMod;
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

    /**
     * @return the owner
     */
    public boolean isOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    /**
     * @return the readonly
     */
    public boolean isReadonly() {
        return readonly;
    }

    /**
     * @param readonly the readonly to set
     */
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    /**
     * Check if a parent object was modified.
     * @return timestamp when the parent was modified
     */
    public Timestamp getParentModified() {
        if (this.parentModified == null && this.sample != null) {
            return this.sample.getTreeMod();
        }
        return this.parentModified;
    }

    public void setParentModified(Timestamp parentModified) {
        this.parentModified = parentModified;
    }
}
