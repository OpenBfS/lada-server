/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;
import java.util.Date;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.Unique;
import de.intevation.lada.validation.groups.DatabaseConstraints;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


/**
 * The persistent class for the messprogramm_kategorie database table.
 *
 */
@Entity
@Table(schema = SchemaName.NAME)
@GroupSequence({ MpgCateg.class, DatabaseConstraints.class })
@Unique(groups = DatabaseConstraints.class,
    clazz = MpgCateg.class, fields = { "extId", "networkId" })
public class MpgCateg extends BaseModel
    implements BelongsToNetwork, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    private String name;

    @NotBlank
    @Size(max = 3)
    private String extId;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    @NotBlank
    @Size(max = 2)
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = Network.class)
    private String networkId;

    public MpgCateg() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtId() {
        return this.extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }

    @Override
    public String getNetworkId() {
        return this.networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }
}
