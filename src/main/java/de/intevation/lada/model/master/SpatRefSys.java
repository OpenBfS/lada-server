/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(schema = SchemaName.NAME)
public class SpatRefSys implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String idfGeoKey;

    private String name;

    @Column(insertable = false)
    private Timestamp lastMod;

    public SpatRefSys() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdfGeoKey() {
        return this.idfGeoKey;
    }

    public void setIdfGeoKey(String idfGeoKey) {
        this.idfGeoKey = idfGeoKey;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Timestamp lastMod) {
        this.lastMod = lastMod;
    }

}
