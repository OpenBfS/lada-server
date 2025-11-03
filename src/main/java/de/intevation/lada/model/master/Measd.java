/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;

import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.NotEmptyNorWhitespace;
import de.intevation.lada.validation.groups.DatabaseConstraints;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Entity
@Table(schema = Names.SCHEMA_NAME)
@GroupSequence({ Measd.class, DatabaseConstraints.class })
public class Measd implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank
    @Size(max = 50)
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = Measd.class)
    private String id;

    private Integer idOld;

    @Size(max = 300)
    @NotEmptyNorWhitespace
    private String descr;

    @Size(max = 9)
    @NotEmptyNorWhitespace
    private String defColor;

    private Long eudfNuclId;

    @Size(max = 6)
    @NotEmptyNorWhitespace
    private String idfExtId;

    @NotNull
    private Boolean isRefNucl;

    @Size(max = 7)
    @NotEmptyNorWhitespace
    private String bvlFormatId;

    public Measd() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getIdOld() {
        return idOld;
    }

    public void setIdOld(Integer idOld) {
        this.idOld = idOld;
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
}
