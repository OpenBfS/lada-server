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
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * The persistent class for the auth_lst_umw database table.
 *
 */
@Entity
@Table(name = "auth_coord_ofc_env_medium_mp", schema = SchemaName.NAME)
public class AuthCoordOfcEnvMediumMp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String measFacilId;

    private String envMediumId;

    @Column(insertable = false)
    private Timestamp lastMod;

    public AuthCoordOfcEnvMediumMp() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMeasFacilId() {
        return this.measFacilId;
    }

    public void setMeasFacilId(String mstId) {
        this.measFacilId = mstId;
    }

    public String getEnvMediumId() {
        return this.envMediumId;
    }

    public void setEnvMediumId(String envMediumId) {
        this.envMediumId = envMediumId;
    }

    public Timestamp getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Timestamp lastMod) {
        this.lastMod = lastMod;
    }

}
