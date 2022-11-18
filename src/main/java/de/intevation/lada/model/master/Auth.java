/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;


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
    private Timestamp lastMod;

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

    public Timestamp getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Timestamp lastMod) {
        this.lastMod = lastMod;
    }

}
