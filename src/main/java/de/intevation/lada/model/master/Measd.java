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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(schema = SchemaName.NAME)
public class Measd implements Serializable {
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
    @Temporal(TIMESTAMP)
    private Date lastMod;

    public Measd() {
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

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }
}
