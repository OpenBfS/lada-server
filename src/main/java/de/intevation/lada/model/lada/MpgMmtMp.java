/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import de.intevation.lada.model.master.Measd;

@Entity
@Table(schema = SchemaName.NAME)
public class MpgMmtMp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(insertable = false)
    private Timestamp lastMod;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        schema = SchemaName.NAME,
        inverseJoinColumns = @JoinColumn(name = "measd_id")
    )
    private Set<Measd> measdObjects;

    @NotNull
    private Integer mpgId;

    @NotBlank
    private String mmtId;

    @Transient
    private Integer[] measds;

    public MpgMmtMp() {
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

    public void setMeasdObjects(Set<Measd> measdObjects) {
        this.measdObjects = measdObjects;
    }

    /**
     * @return Integer[] IDs of associated Messgroesse objects.
     */
    public Integer[] getMeasds() {
        if (this.measds == null && this.measdObjects != null) {
            Set<Integer> ids = new HashSet<>();
            for (Measd m: this.measdObjects) {
                ids.add(m.getId());
            }
            this.measds = ids.toArray(new Integer[ids.size()]);
        }
        return this.measds;
    }

    public void setMeasds(Integer[] measds) {
        this.measds = measds;
    }

    public Integer getMpgId() {
        return this.mpgId;
    }

    public void setMpgId(Integer mpgId) {
        this.mpgId = mpgId;
    }

    public String getMmtId() {
        return this.mmtId;
    }

    public void setMmtId(String mmtId) {
        this.mmtId = mmtId;
    }

}
