/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = Names.SCHEMA_NAME)
public class StatusMpView implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(insertable = false, updatable = false)
    private Integer statusMpId;

    @Column(insertable = false, updatable = false)
    private String statusComb;

    public Integer getStatusMpId() {
        return statusMpId;
    }

    public void setStatusMpId(Integer statusMpId) {
        this.statusMpId = statusMpId;
    }

    public String getStatusComb() {
        return statusComb;
    }

    public void setStatusComb(String statusComb) {
        this.statusComb = statusComb;
    }
}
