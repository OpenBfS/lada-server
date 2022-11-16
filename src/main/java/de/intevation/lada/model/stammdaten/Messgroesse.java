/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.stammdaten;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "measd", schema = SchemaName.NAME)
public class Messgroesse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String descr;

    private String defColor;

    private Long eudfNuclId;

    private String idfExtId;

    private Boolean isRefNucl;

    private String bvlFormatId;

    private String name;

    @Column(insertable = false)
    private Timestamp lastMod;

    public Messgroesse() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescr() {
        return this.descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getDefColor() {
        return this.defColor;
    }

    public void setDefColor(String defColor) {
        this.defColor = defColor;
    }

    public Long getEudfNuclId() {
        return this.eudfNuclId;
    }

    public void setEudfNuclId(Long eudfNuclId) {
        this.eudfNuclId = eudfNuclId;
    }

    public String getIdfExtId() {
        return this.idfExtId;
    }

    public void setIdfExtId(String idfExtId) {
        this.idfExtId = idfExtId;
    }

    public Boolean getIsRefNucl() {
        return this.isRefNucl;
    }

    public void setIsRefNucl(Boolean istRefNucl) {
        this.isRefNucl = istRefNucl;
    }

    public String getBvlFormatId() {
        return this.bvlFormatId;
    }

    public void setBvlFormatId(String bvlFormatId) {
        this.bvlFormatId = bvlFormatId;
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
