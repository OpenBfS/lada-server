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
public class ObligMeasdMp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private Integer measdId;

    private Integer regulationId;

    private String mmtId;

    private String envMediumId;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    public ObligMeasdMp() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMeasdId() {
        return this.measdId;
    }

    public void setMeasdId(Integer measdId) {
        this.measdId = measdId;
    }

    public Integer getRegulationId() {
        return this.regulationId;
    }

    public void setRegulationId(Integer regulationId) {
        this.regulationId = regulationId;
    }

    public String getMmtId() {
        return this.mmtId;
    }

    public void setMmtId(String mmtId) {
        this.mmtId = mmtId;
    }

    public String getEnvMediumId() {
        return this.envMediumId;
    }

    public void setEnvMediumId(String envMediumId) {
        this.envMediumId = envMediumId;
    }

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }

}
