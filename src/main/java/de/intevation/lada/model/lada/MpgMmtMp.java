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
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import jakarta.persistence.Transient;
import jakarta.validation.GroupSequence;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import de.intevation.lada.model.master.Measd;
import de.intevation.lada.model.master.Mmt;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.groups.DatabaseConstraints;

@Entity
@Table(schema = SchemaName.NAME)
@GroupSequence({ MpgMmtMp.class, DatabaseConstraints.class })
public class MpgMmtMp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        schema = SchemaName.NAME,
        inverseJoinColumns = @JoinColumn(name = "measd_id")
    )
    @Valid
    private Set<Measd> measdObjects;

    @NotNull
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = Mpg.class)
    private Integer mpgId;

    @NotBlank
    @Size(max = 2)
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = Mmt.class)
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

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
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
