/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The persistent class for the auth_coord_ofc_env_medium_mp database table.
 *
 */
@Entity
@Table(schema = Names.SCHEMA_NAME)
public class AuthCoordOfcEnvMediumMp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String measFacilId;

    private String envMediumId;

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
}
