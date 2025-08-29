/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.util.Date;

import jakarta.json.bind.annotation.JsonbTransient;
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

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.validation.constraints.CanHaveValUntil;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.NetworkOrMeasFacil;
import de.intevation.lada.validation.constraints.Unique;
import de.intevation.lada.validation.groups.DatabaseConstraints;

@Entity
@Table(schema = Names.SCHEMA_NAME)
@GroupSequence({ Tag.class, DatabaseConstraints.class })
@Unique(groups = DatabaseConstraints.class,
    clazz = Tag.class, fields = { "name", "networkId", "measFacilId" })
@Unique(groups = DatabaseConstraints.class,
    clazz = Tag.class, fields = { "name" },
    predicateFields = { "networkId", "measFacilId" },
    predicateIsNull = { true, true },
    message = "{de.intevation.lada.validation.TagUniqueGlobal.message}")
@Unique(groups = DatabaseConstraints.class,
    clazz = Tag.class, fields = { "name", "networkId" },
    predicateFields = { "measFacilId" }, predicateIsNull = { true })
@Unique(groups = DatabaseConstraints.class,
    clazz = Tag.class, fields = { "name", "measFacilId" },
    predicateFields = { "networkId" }, predicateIsNull = { true })
@CanHaveValUntil
@NetworkOrMeasFacil
public class Tag extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    private String name;

    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = MeasFacil.class)
    private String measFacilId;

    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = Network.class)
    private String networkId;

    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = LadaUser.class)
    private Integer ladaUserId;

    @Temporal(TIMESTAMP)
    private Date valUntil;

    @Column(insertable = false, updatable = false)
    @Temporal(TIMESTAMP)
    private Date createdAt;

    @Column(updatable = false)
    private boolean isAutoTag;

    public Tag() { }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String tag) {
        this.name = tag;
    }

    public String getMeasFacilId() {
        return this.measFacilId;
    }

    public void setMeasFacilId(String measFacilId) {
        this.measFacilId = measFacilId;
    }

    public boolean getIsAutoTag() {
        return this.isAutoTag;
    }

    @JsonbTransient
    public void setIsAutoTag(boolean isAutoTag) {
        this.isAutoTag = isAutoTag;
    }

    public Date getValUntil() {
        return valUntil;
    }

    public void setValUntil(Date valUntil) {
        this.valUntil = valUntil;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * @return ID of Netzbetreiber associated to this tag.
     */
    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public Integer getLadaUserId() {
        return ladaUserId;
    }

    public void setLadaUserId(Integer ladaUserId) {
        this.ladaUserId = ladaUserId;
    }
}
