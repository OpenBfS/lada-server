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
public class EnvSpecifMp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String sampleSpecifId;

    private String envMediumId;

    public EnvSpecifMp() {
    }

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSampleSpecifId() {
        return this.sampleSpecifId;
    }

    public void setSampleSpecifId(String sampleSpecifId) {
        this.sampleSpecifId = sampleSpecifId;
    }

    public String getEnvMediumId() {
        return this.envMediumId;
    }

    public void setEnvMediumId(String envMediumId) {
        this.sampleSpecifId = envMediumId;
    }

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }

}
