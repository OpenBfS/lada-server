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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import jakarta.persistence.Transient;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.NotEmptyNorWhitespace;
import de.intevation.lada.validation.constraints.StatusOrder;
import de.intevation.lada.validation.constraints.HaveDependenciesNotifications;
import de.intevation.lada.validation.constraints.ValidDependenciesFinalStatus;
import de.intevation.lada.validation.groups.DatabaseConstraints;
import de.intevation.lada.validation.groups.Notifications;


@Entity
@Table(schema = SchemaName.NAME)
@GroupSequence({ StatusProt.class, DatabaseConstraints.class })
@ValidDependenciesFinalStatus(groups = DatabaseConstraints.class)
@HaveDependenciesNotifications(groups = Notifications.class)
@StatusOrder(groups = DatabaseConstraints.class)
public class StatusProt extends BelongsToMeasm implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(insertable = false, updatable = false)
    @Temporal(TIMESTAMP)
    private Date date;

    @NotBlank
    @Size(max = 5)
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = MeasFacil.class)
    private String measFacilId;

    @NotNull
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = StatusMp.class)
    private Integer statusMpId;

    @Size(max = 1024)
    @NotEmptyNorWhitespace
    private String text;

    @Column(insertable = false, updatable = false)
    @Temporal(TIMESTAMP)
    private Date treeMod;

    @Transient
    private Date parentModified;

    @Transient
    private Integer statusLev;

    @Transient
    private Integer statusVal;


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
