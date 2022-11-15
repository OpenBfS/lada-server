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
import java.sql.Timestamp;
import javax.persistence.Column;

@Entity
@Table(schema = SchemaName.NAME)
public class AuthFunct implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String funct;

    @Column(insertable = false)
    private Timestamp lastMod;

    public AuthFunct() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFunct() {
        return this.funct;
    }

    public void setFunct(String funct) {
        this.funct = funct;
    }

    public Timestamp getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Timestamp letzteAenderung) {
        this.lastMod = letzteAenderung;
    }

}
