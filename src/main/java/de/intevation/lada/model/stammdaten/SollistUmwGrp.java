/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
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
@Table(name = "targ_env_gr", schema = SchemaName.NAME)
public class SollistUmwGrp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String targEnvGrDispl;

    private String name;

    public SollistUmwGrp() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTargEnvGrDispl() {
        return this.targEnvGrDispl;
    }

    public void setTargEnvGrDispl(String tarEnvGrDispl) {
        this.targEnvGrDispl = tarEnvGrDispl;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
