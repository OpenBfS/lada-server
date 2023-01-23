/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.stammdaten;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class QueryMessstellePK implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "mess_stelle")
    private String messStelle;

    @Column(name = "query_id")
    private Integer queryId;

    public Integer getQueryId() {
        return queryId;
    }

    public void setQueryId(Integer queryId) {
        this.queryId = queryId;
    }

    public QueryMessstellePK() {
    }

    public String getMessStelle() {
        return this.messStelle;
    }

    public void setMessStelle(String messStelle) {
        this.messStelle = messStelle;
    }

    /**
     * equals operator.
     * @param other the other object
     * @return true if the objects are equal
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof QueryMessstellePK)) {
            return false;
        }
        QueryMessstellePK castOther = (QueryMessstellePK) other;
        return
            this.messStelle.equals(castOther.messStelle)
            && this.queryId.equals(castOther.queryId);
    }

    /**
     * get the objects hash code.
     * @return the hash
     */
    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.messStelle.hashCode();
        hash = hash * prime + this.queryId.hashCode();
        return hash;
    }

}
