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

import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.groups.DatabaseConstraints;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import jakarta.persistence.Transient;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import de.intevation.lada.model.BaseModel;

@Entity
@Table(name = "comm_measm", schema = SchemaName.NAME)
@GroupSequence({ CommMeasm.class, DatabaseConstraints.class })
public class CommMeasm extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Temporal(TIMESTAMP)
    private Date date;

    @NotNull
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = Measm.class)
    private Integer measmId;

    @NotBlank
    @Size(max = 5)
        @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = MeasFacil.class)
    private String measFacilId;

    @NotBlank
    @Size(max = 1024)
    private String text;

    @Transient
    private boolean owner;

    public CommMeasm() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date datum) {
        this.date = datum;
    }

    public Integer getMeasmId() {
        return this.measmId;
    }

    public void setMeasmId(Integer measmId) {
        this.measmId = measmId;
    }

    public String getMeasFacilId() {
        return this.measFacilId;
    }

    public void setMeasFacilId(String measFacil) {
        this.measFacilId = measFacil;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the owner
     */
    public boolean isOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(boolean owner) {
        this.owner = owner;
    }
}
