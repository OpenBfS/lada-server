/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.stammdaten;

import java.io.Serializable;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * The persistent class for the query_messstelle database table.
 *
 */
@Entity
@Table(name = "query_messstelle", schema = SchemaName.LEGACY_NAME)
public class QueryMessstelle implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private QueryMessstellePK id;

    //bi-directional many-to-one association to QueryUser
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "query")
    private QueryUser queryUser;


    public QueryMessstellePK getId() {
        return id;
    }

    public void setId(QueryMessstellePK id) {
        this.id = id;
    }

    public QueryMessstelle() {
    }

    public String getMessStelle() {
        return this.id.getMessStelle();
    }

    public void setMessStelle(String messStelle) {
        this.id.setMessStelle(messStelle);
    }

    @JsonbTransient
    public QueryUser getQueryUser() {
        return this.queryUser;
    }

    public void setQueryUser(QueryUser queryUser) {
        this.queryUser = queryUser;
    }

}
