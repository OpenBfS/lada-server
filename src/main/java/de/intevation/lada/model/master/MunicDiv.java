/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

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

@Entity
@Table(schema = SchemaName.NAME)
public class MunicDiv implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String networkId;

    private String adminUnitId;

    private Integer siteId;

    private String name;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    @Transient
    private boolean readonly;

    public MunicDiv() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNetworkId() {
        return this.networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getAdminUnitId() {
        return this.adminUnitId;
    }

    public void setAdminUnitId(String municId) {
        this.adminUnitId = municId;
    }

    public Integer getSiteId() {
        return this.siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

}
