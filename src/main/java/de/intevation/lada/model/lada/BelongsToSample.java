/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import java.util.Date;

import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import de.intevation.lada.model.BaseModel;


@MappedSuperclass
public abstract class BelongsToSample extends BaseModel {

    public static class SampleToId implements JsonbAdapter<Sample, Integer> {
        @PersistenceContext
        EntityManager em;

        @Override
        public Sample adaptFromJson(Integer id) {
            if (em == null) {
                // Mock sample when deserializing in client-side tests
                return new Sample();
            }
            return em.find(Sample.class, id);
        }

        @Override
        public Integer adaptToJson(Sample samp) {
            return samp.getId();
        }
    }

    @JsonbProperty("sampleId")
    @JsonbTypeAdapter(SampleToId.class)
    @Schema(implementation = Integer.class)
    @NotNull
    @ManyToOne
    private Sample sample;

    @Transient
    private boolean owner;

    @Transient
    private Date parentModified;


    public Sample getSample() {
        return this.sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
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
