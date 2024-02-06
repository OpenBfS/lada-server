/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;
import java.util.Date;

import de.intevation.lada.validation.constraints.NotEmptyNorWhitespace;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import jakarta.persistence.Column;

@Entity
@Table(schema = SchemaName.NAME)
public class AuthFunct implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    @NotEmptyNorWhitespace
    private String funct;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

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

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date letzteAenderung) {
        this.lastMod = letzteAenderung;
    }

}
