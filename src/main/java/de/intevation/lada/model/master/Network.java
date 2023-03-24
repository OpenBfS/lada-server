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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(schema = SchemaName.NAME)
@NamedQuery(
    name = "Network.findAll",
    query = "SELECT n FROM Network n")
public class Network implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Column(insertable = false, updatable = false)
    private Boolean isActive;

    private String idfNetworkId;

    private Boolean isFmn;

    private String mailList;

    private String name;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    public Network() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getIdfNetworkId() {
        return this.idfNetworkId;
    }

    public void setIdfNetworkId(String idfNetworkId) {
        this.idfNetworkId = idfNetworkId;
    }

    public Boolean getIsFmn() {
        return this.isFmn;
    }

    public void setIsFmn(Boolean isFmn) {
        this.isFmn = isFmn;
    }

    public String getMailList() {
        return this.mailList;
    }

    public void setMailList(String mailList) {
        this.mailList = mailList;
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

}
