/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema = SchemaName.NAME)
public class StatusLev implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String lev;

    public StatusLev() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLev() {
        return this.lev;
    }

    public void setLev(String lev) {
        this.lev = lev;
    }
}
