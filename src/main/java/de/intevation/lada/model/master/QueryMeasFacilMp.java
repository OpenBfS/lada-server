/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = SchemaName.NAME)
public class QueryMeasFacilMp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String measFacilId;

    //bi-directional many-to-one association to QueryUser
    @ManyToOne(fetch = FetchType.EAGER)
    private QueryUser queryUser;

    public QueryMeasFacilMp() {
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

    public void setMeasFacilId(String measFacilId) {
        this.measFacilId = measFacilId;
    }

    @JsonbTransient
    public QueryUser getQueryUser() {
        return this.queryUser;
    }

    public void setQueryUser(QueryUser queryUser) {
        this.queryUser = queryUser;
    }

}
