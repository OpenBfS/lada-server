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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import jakarta.persistence.Transient;

import de.intevation.lada.model.BaseModel;


@Entity
@Table(schema = SchemaName.NAME)
public class GeolocatMpg extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    private Integer mpgId;

    private Integer siteId;

    private String typeRegulation;

    private String addSiteText;

    private String poiId;

    @Column(insertable = false, updatable = false)
    @Temporal(TIMESTAMP)
    private Date treeMod;

    @Transient
    private boolean owner;

    @Transient
    private boolean readonly;

    public GeolocatMpg() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }

    public Integer getMpgId() {
        return this.mpgId;
    }

    public void setMpgId(Integer mpgId) {
        this.mpgId = mpgId;
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

    public String getPoiId() {
        return this.poiId;
    }

    public void setPoiId(String poiId) {
        this.poiId = poiId;
    }

    public Date getTreeMod() {
        return this.treeMod;
    }

    public void setTreeMod(Date treeModified) {
        this.treeMod = treeModified;
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
}
