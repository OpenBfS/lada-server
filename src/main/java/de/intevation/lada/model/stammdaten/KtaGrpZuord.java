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
import java.sql.Timestamp;

@Entity
@Table(name = "nucl_facil_gr_mp", schema = SchemaName.NAME)
public class KtaGrpZuord implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private Integer nuclFacilGrId;

    private Integer nuclFacilId;

    private Timestamp lastMod;

    public KtaGrpZuord() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNuclFacilGrId() {
        return this.nuclFacilGrId;
    }

    public void setNuclFacilGrId(Integer nuclFacilGrId) {
        this.nuclFacilGrId = nuclFacilGrId;
    }

    public Integer getNuclFacilId() {
        return this.nuclFacilId;
    }

    public void setNuclFacilId(Integer nuclFacilId) {
        this.nuclFacilId = nuclFacilId;
    }

    public Timestamp getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Timestamp lastMod) {
        this.lastMod = lastMod;
    }

}
