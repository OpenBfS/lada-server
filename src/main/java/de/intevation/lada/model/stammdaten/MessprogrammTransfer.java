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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "mpg_transf", schema = SchemaName.NAME)
public class MessprogrammTransfer implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer oprModeId;

    private Integer regulationId;

    private String name;

    private String extId;

    public MessprogrammTransfer() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOprModeId() {
        return this.oprModeId;
    }

    public void setOprModeId(Integer oprModeId) {
        this.oprModeId = oprModeId;
    }

    public Integer getRegulationId() {
        return this.regulationId;
    }

    public void setRegulationId(Integer regulationId) {
        this.regulationId = regulationId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtId() {
        return this.extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

}
