/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import java.util.Date;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;

import de.intevation.lada.model.BaseModel;

@MappedSuperclass
public abstract class BelongsToMeasm extends BaseModel {

    public static class MeasmToId implements JsonbAdapter<Measm, Integer> {
        @PersistenceContext
        EntityManager em;

        @Override
        public Measm adaptFromJson(Integer id) {
            if (em == null) {
                // Mock measm when deserializing in client-side tests
                return new Measm();
            }
            return em.find(Measm.class, id);
        }

        @Override
        public Integer adaptToJson(Measm measm) {
            return measm.getId();
        }
    }

    @JsonbProperty("measmId")
    @JsonbTypeAdapter(MeasmToId.class)
    @Schema(implementation = Integer.class)
    @NotNull
    @ManyToOne
    protected Measm measm;

    @Transient
    private boolean owner;

    @Transient
    private Date parentModified;


    public Measm getMeasm() {
        return this.measm;
    }

    public void setMeasm(Measm measm) {
        this.measm = measm;
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
