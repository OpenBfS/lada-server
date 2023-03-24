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
import javax.persistence.Table;
import javax.persistence.Temporal;
import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(schema = SchemaName.NAME)
public class MeasFacil implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String trunkCode;

    private String address;

    private String name;

    private String measFacilType;

    private String networkId;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    public MeasFacil() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrunkCode() {
        return this.trunkCode;
    }

    public void setTrunkCode(String trunkCode) {
        this.trunkCode = trunkCode;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMeasFacilType() {
        return this.measFacilType;
    }

    public void setMeasFacilType(String measFacilType) {
        this.measFacilType = measFacilType;
    }

    public String getNetworkId() {
        return this.networkId;
    }

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }

}
