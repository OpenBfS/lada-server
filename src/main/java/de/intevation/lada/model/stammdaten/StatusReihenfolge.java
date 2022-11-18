/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.stammdaten;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "status_ord_mp", schema = SchemaName.NAME)
public class StatusReihenfolge implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private Integer fromId;

    private Integer toId;

    public StatusReihenfolge() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFromId() {
        return this.fromId;
    }

    public void setFromId(Integer fromId) {
        this.fromId = fromId;
    }

    public Integer getToId() {
        return this.toId;
    }

    public void setToId(Integer toId) {
        this.toId = toId;
    }

}
