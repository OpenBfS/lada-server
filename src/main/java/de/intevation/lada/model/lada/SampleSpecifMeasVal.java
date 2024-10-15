/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import de.intevation.lada.model.master.SampleSpecif;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.NotEmptyNorWhitespace;
import de.intevation.lada.validation.constraints.SampleSpecifMatchesEnvMedium;
import de.intevation.lada.validation.constraints.Unique;
import de.intevation.lada.validation.groups.DatabaseConstraints;
import de.intevation.lada.validation.groups.Warnings;


@Entity
@Table(schema = SchemaName.NAME)
@GroupSequence({ SampleSpecifMeasVal.class, DatabaseConstraints.class })
@Unique(fields = {"sampleSpecifId", "sampleId"},
    groups = DatabaseConstraints.class, clazz = SampleSpecifMeasVal.class)
@SampleSpecifMatchesEnvMedium(groups = Warnings.class)
public class SampleSpecifMeasVal extends BelongsToSample
    implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    private Float error;

    private Double measVal;

    @Size(max = 1)
    @NotEmptyNorWhitespace
    private String smallerThan;

    @NotBlank
    @Size(max = 3)
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = SampleSpecif.class)
    private String sampleSpecifId;

    @Column(insertable = false, updatable = false)
    @Temporal(TIMESTAMP)
    private Date treeMod;


    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }

    public Float getError() {
        return this.error;
    }

    public void setError(Float error) {
        this.error = error;
    }

    public Double getMeasVal() {
        return this.measVal;
    }

    public void setMeasVal(Double measVal) {
        this.measVal = measVal;
    }

    public String getSampleSpecifId() {
        return this.sampleSpecifId;
    }

    public void setSampleSpecifId(String sampleSpecifId) {
        this.sampleSpecifId = sampleSpecifId;
    }

    public String getSmallerThan() {
        return this.smallerThan;
    }

    public void setSmallerThan(String smallerThan) {
        this.smallerThan = smallerThan;
    }

    public Date getTreeMod() {
        return this.treeMod;
    }

    public void setTreeMod(Date treeMod) {
        this.treeMod = treeMod;
    }
}
