/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import java.util.Date;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.groups.DatabaseConstraints;


@MappedSuperclass
public abstract class BelongsToSample extends BaseModel {

    @NotNull
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = Sample.class)
    private Integer sampleId;

    @OneToOne
    @JoinColumn(insertable = false, updatable = false)
    private Sample sample;

    @Transient
    private boolean owner;

    @Transient
    private Date parentModified;


    public Integer getSampleId() {
        return this.sampleId;
    }

    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    @JsonbTransient
    public Sample getSample() {
        return this.sample;
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
        if (this.parentModified == null && this.sample != null) {
            return this.sample.getTreeMod();
        }
        return this.parentModified;
    }

    public void setParentModified(Date parentModified) {
        this.parentModified = parentModified;
    }
}
