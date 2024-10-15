/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import java.util.Date;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.groups.DatabaseConstraints;


@MappedSuperclass
public abstract class BelongsToMeasm extends BaseModel {

    @NotNull
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = Measm.class)
    private Integer measmId;

    @OneToOne
    @JoinColumn(insertable = false, updatable = false)
    private Measm measm;

    @Transient
    private boolean owner;

    @Transient
    private Date parentModified;


    public Integer getMeasmId() {
        return this.measmId;
    }

    public void setMeasmId(Integer measmId) {
        this.measmId = measmId;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    /**
     * Check if a parent object was modified.
     * @return timestamp when the parent was modified
     */
    public Date getParentModified() {
        if (this.parentModified == null && this.measm != null) {
            return this.measm.getTreeMod();
        }
        return this.parentModified;
    }

    public void setParentModified(Date parentModified) {
        this.parentModified = parentModified;
    }
}
