/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.lada;

import java.io.Serializable;
import java.time.Instant;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.NoCompleteMeasValsOnUndeliverable;
import de.intevation.lada.validation.constraints.NotEmptyNorWhitespace;
import de.intevation.lada.validation.constraints.StatusOrder;
import de.intevation.lada.validation.constraints.HaveDependenciesNotifications;
import de.intevation.lada.validation.constraints.ValidDependenciesFinalStatus;
import de.intevation.lada.validation.groups.DatabaseConstraints;
import de.intevation.lada.validation.groups.Notifications;
import de.intevation.lada.validation.groups.PostAuthorization;


@Entity
@Table(schema = Names.SCHEMA_NAME)
@GroupSequence({ StatusProt.class, DatabaseConstraints.class })
@NoCompleteMeasValsOnUndeliverable
@ValidDependenciesFinalStatus(groups = PostAuthorization.class)
@HaveDependenciesNotifications(groups = Notifications.class)
@StatusOrder(groups = DatabaseConstraints.class)
public class StatusProt extends BelongsToMeasm implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(insertable = false, updatable = false)
    private int seqNo;

    @Schema(readOnly = true)
    @Column(insertable = false, updatable = false)
    private Instant date;

    @NotBlank
    @Size(max = 5)
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = MeasFacil.class)
    private String measFacilId;

    @NotNull
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = StatusMp.class)
    private Integer statusMpId;

    @JsonbTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(insertable = false, updatable = false)
    private StatusMp statusMp;

    @Size(max = 1024)
    @NotEmptyNorWhitespace
    private String text;

    @Column(insertable = false, updatable = false)
    private Instant treeMod;


    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Instant getDate() {
        return this.date;
    }

    public String getMeasFacilId() {
        return this.measFacilId;
    }

    public void setMeasFacilId(String measFacilId) {
        this.measFacilId = measFacilId;
    }

    public Integer getStatusMpId() {
        return this.statusMpId;
    }

    public void setStatusMpId(Integer statusComb) {
        this.statusMpId = statusComb;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getTreeMod() {
        return this.treeMod;
    }

    public void setTreeMod(Instant treeMod) {
        this.treeMod = treeMod;
    }
}
