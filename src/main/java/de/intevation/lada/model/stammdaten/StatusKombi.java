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
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "status_mp", schema = SchemaName.NAME)
public class StatusKombi implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    //bi-directional many-to-one association to StatusStufe
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_lev_id")
    private StatusStufe statusLev;

    //bi-directional many-to-one association to StatusWert
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_val_id")
    private StatusWert statusVal;

    public StatusKombi() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public StatusStufe getStatusLev() {
        return this.statusLev;
    }

    public void setStatusLev(StatusStufe statusLev) {
        this.statusLev = statusLev;
    }

    public StatusWert getStatusVal() {
        return this.statusVal;
    }

    public void setStatusVal(StatusWert statusVal) {
        this.statusVal = statusVal;
    }

}
