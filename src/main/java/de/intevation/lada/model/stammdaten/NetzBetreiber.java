/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.stammdaten;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "network", schema = SchemaName.NAME)
@NamedQuery(
    name = "NetzBetreiber.findAll",
    query = "SELECT n FROM NetzBetreiber n")
public class NetzBetreiber implements Serializable {
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
    private Timestamp lastMod;

    public NetzBetreiber() {
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

    public Timestamp getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Timestamp lastMod) {
        this.lastMod = lastMod;
    }

}
