/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import static javax.persistence.TemporalType.TIMESTAMP;
import javax.persistence.Transient;

import de.intevation.lada.model.BaseModel;


@Entity
@Table(schema = SchemaName.NAME)
public class StatusProt extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(insertable = false, updatable = false)
    @Temporal(TIMESTAMP)
    private Date date;

    private Integer measmId;

    private String measFacilId;

    private Integer statusMpId;

    private String text;

    @Column(insertable = false, updatable = false)
    @Temporal(TIMESTAMP)
    private Date treeMod;

    @Transient
    private boolean owner;

    @Transient
    private boolean readonly;

    @Transient
    private Date parentModified;

    @Transient
    private Integer statusLev;

    @Transient
    private Integer statusVal;

    public StatusProt() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getMeasmId() {
        return this.measmId;
    }

    public void setMeasmId(Integer measmId) {
        this.measmId = measmId;
    }

    public String getMeasFacilId() {
        return this.measFacilId;
    }

    public void setMeasFacilId(String measFacilId) {
        this.measFacilId = measFacilId;
    }

    public Integer getStatusMpId() {
        return this.statusMpId;
    }

    public void setStatusMpId(Integer statusComb) {
        this.statusMpId = statusComb;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTreeMod() {
        return this.treeMod;
    }

    public void setTreeMod(Date treeMod) {
        this.treeMod = treeMod;
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
     * @return the parentModified
     */
    public Date getParentModified() {
        return parentModified;
    }

    /**
     * @param parentModified the parentModified to set
     */
    public void setParentModified(Date parentModified) {
        this.parentModified = parentModified;
    }

    /**
     * @return the status level
     */
    public Integer getStatusLev() {
        return statusLev;
    }

    /**
     * @param statusLev the status level to set
     */
    public void setStatusLev(Integer statusLev) {
        this.statusLev = statusLev;
    }

    /**
     * @return the status value
     */
    public Integer getStatusVal() {
        return statusVal;
    }

    /**
     * @param statusVal the status value to set
     */
    public void setStatusVal(Integer statusVal) {
        this.statusVal = statusVal;
    }

}
