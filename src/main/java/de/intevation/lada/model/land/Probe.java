/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.land;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.ws.rs.core.MultivaluedMap;

import org.hibernate.annotations.DynamicInsert;

import de.intevation.lada.model.stammdaten.Umwelt;
import de.intevation.lada.util.data.EmptyStringConverter;


/**
 * The persistent class for the probe database table.
 *
 */
@Entity
@DynamicInsert(true)
@Table(schema = SchemaName.NAME)
public class Probe implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer baId;

    private Integer datenbasisId;

    private Integer erzeugerId;

    @Convert(converter = EmptyStringConverter.class)
    private String hauptprobenNr;

    @Column(name = "ext_id")
    @Convert(converter = EmptyStringConverter.class)
    private String externeProbeId;

    private String laborMstId;

    @Column(insertable = false)
    private Timestamp letzteAenderung;

    private String media;

    private String mediaDesk;

    private Long mittelungsdauer;

    private Integer mplId;

    private Integer mprId;

    private String mstId;

    private Integer probeNehmerId;

    private Timestamp probeentnahmeBeginn;

    private Timestamp probeentnahmeEnde;

    private Integer probenartId;

    private Timestamp solldatumBeginn;

    private Timestamp solldatumEnde;

    private Timestamp ursprungszeit;

    private Boolean test;

    @Column(insertable = false, updatable = false)
    private Timestamp treeModified;

    @OneToOne
    @JoinColumn(name = "umw_id", insertable = false, updatable = false)
    private Umwelt umwelt;

    private String umwId;

    private Integer reiProgpunktGrpId;

    private Integer ktaGruppeId;

    @Transient
    private boolean readonly;

    @Transient
    private boolean owner;

    @Transient
    @JsonbTransient
    private boolean found;

    @Transient
    private MultivaluedMap<String, Integer> errors;

    @Transient
    private MultivaluedMap<String, Integer> warnings;

    @Transient
    private MultivaluedMap<String, Integer> notifications;

    public Probe() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBaId() {
        return this.baId;
    }

    public void setBaId(Integer baId) {
        this.baId = baId;
    }

    public Integer getDatenbasisId() {
        return this.datenbasisId;
    }

    public void setDatenbasisId(Integer datenbasisId) {
        this.datenbasisId = datenbasisId;
    }

    public Integer getErzeugerId() {
        return this.erzeugerId;
    }

    public void setErzeugerId(Integer erzeugerId) {
        this.erzeugerId = erzeugerId;
    }

    public String getHauptprobenNr() {
        return this.hauptprobenNr;
    }

    public void setHauptprobenNr(String hauptprobenNr) {
        this.hauptprobenNr = hauptprobenNr;
    }

    public String getExterneProbeId() {
        return this.externeProbeId;
    }

    public void setExterneProbeId(String externeProbeId) {
        this.externeProbeId = externeProbeId;
    }

    public String getLaborMstId() {
        return this.laborMstId;
    }

    public void setLaborMstId(String laborMstId) {
        this.laborMstId = laborMstId;
    }

    public Timestamp getLetzteAenderung() {
        return this.letzteAenderung;
    }

    public void setLetzteAenderung(Timestamp letzteAenderung) {
        this.letzteAenderung = letzteAenderung;
    }

    public String getMedia() {
        return this.media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getMediaDesk() {
        return this.mediaDesk;
    }

    public void setMediaDesk(String mediaDesk) {
        this.mediaDesk = mediaDesk;
    }

    public Long getMittelungsdauer() {
        return this.mittelungsdauer;
    }

    public void setMittelungsdauer(Long mittelungsdauer) {
        this.mittelungsdauer = mittelungsdauer;
    }

    public Integer getMplId() {
        return this.mplId;
    }

    public void setMplId(Integer mplId) {
        this.mplId = mplId;
    }

    public Integer getMprId() {
        return this.mprId;
    }

    public void setMprId(Integer mprId) {
        this.mprId = mprId;
    }

    public String getMstId() {
        return this.mstId;
    }

    public void setMstId(String mstId) {
        this.mstId = mstId;
    }

    public Integer getProbeNehmerId() {
        return this.probeNehmerId;
    }

    public void setProbeNehmerId(Integer probeNehmerId) {
        this.probeNehmerId = probeNehmerId;
    }

    public Timestamp getProbeentnahmeBeginn() {
        return this.probeentnahmeBeginn;
    }

    public void setProbeentnahmeBeginn(Timestamp probeentnahmeBeginn) {
        this.probeentnahmeBeginn = probeentnahmeBeginn;
    }

    public Timestamp getProbeentnahmeEnde() {
        return this.probeentnahmeEnde;
    }

    public void setProbeentnahmeEnde(Timestamp probeentnahmeEnde) {
        this.probeentnahmeEnde = probeentnahmeEnde;
    }

    public Integer getProbenartId() {
        return this.probenartId;
    }

    public void setProbenartId(Integer probenartId) {
        this.probenartId = probenartId;
    }

    public Timestamp getSolldatumBeginn() {
        return this.solldatumBeginn;
    }

    public void setSolldatumBeginn(Timestamp solldatumBeginn) {
        this.solldatumBeginn = solldatumBeginn;
    }

    public Timestamp getSolldatumEnde() {
        return this.solldatumEnde;
    }

    public void setSolldatumEnde(Timestamp solldatumEnde) {
        this.solldatumEnde = solldatumEnde;
    }

    public Timestamp getUrsprungszeit() {
        return this.ursprungszeit;
    }

    public void setUrsprungszeit(Timestamp ursprungszeit) {
        this.ursprungszeit = ursprungszeit;
    }

    public Boolean getTest() {
        return this.test;
    }

    public void setTest(Boolean test) {
        this.test = test;
    }

    public Timestamp getTreeModified() {
        return this.treeModified;
    }

    public void setTreeModified(Timestamp treeModified) {
        this.treeModified = treeModified;
    }

    @JsonbTransient
    public Umwelt getUmwelt() {
        return this.umwelt;
    }

    public String getUmwId() {
        return this.umwId;
    }

    public void setUmwId(String umwId) {
        this.umwId = umwId;
    }

    public Integer getReiProgpunktGrpId() {
        return reiProgpunktGrpId;
    }

    public void setReiProgpunktGrpId(Integer reiProgpunktGrpId) {
        this.reiProgpunktGrpId = reiProgpunktGrpId;
    }

    public Integer getKtaGruppeId() {
        return ktaGruppeId;
    }

    public void setKtaGruppeId(Integer ktaGruppeId) {
        this.ktaGruppeId = ktaGruppeId;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public MultivaluedMap<String, Integer> getErrors() {
        return this.errors;
    }

    @JsonbTransient
    public void setErrors(MultivaluedMap<String, Integer> errors) {
        this.errors = errors;
    }

    public MultivaluedMap<String, Integer> getWarnings() {
        return this.warnings;
    }

    @JsonbTransient
    public void setNotifications(
        MultivaluedMap<String, Integer> notifications
    ) {
      this.notifications = notifications;
    }

    public MultivaluedMap<String, Integer> getNotifications() {
       return this.notifications;
    }

    @JsonbTransient
    public void setWarnings(MultivaluedMap<String, Integer> warnings) {
        this.warnings = warnings;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }
}
