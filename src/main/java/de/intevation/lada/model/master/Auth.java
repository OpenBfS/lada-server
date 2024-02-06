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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;


@Entity
@Table(schema = SchemaName.NAME)
public class Auth implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private Integer authFunctId;

    private String apprLabId;

    private String ldapGr;

    private String measFacilId;

    private String networkId;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    public Auth() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAuthFunctId() {
        return this.authFunctId;
    }

    public void setAuthFunctId(Integer authFunctId) {
        this.authFunctId = authFunctId;
    }

    public String getApprLabId() {
        return this.apprLabId;
    }

    public void setApprLabId(String apprLabId) {
        this.apprLabId = apprLabId;
    }

    public String getLdapGr() {
        return this.ldapGr;
    }

    public void setLdapGr(String ldapGr) {
        this.ldapGr = ldapGr;
    }

    public String getMeasFacilId() {
        return this.measFacilId;
    }

    public void setMeasFacilId(String measFacilId) {
        this.measFacilId = measFacilId;
    }

    public String getNetworkId() {
        return this.networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }

}
