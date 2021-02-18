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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.annotations.DynamicInsert;

import de.intevation.lada.model.stammdaten.Umwelt;
import de.intevation.lada.util.data.EmptyStringConverter;


/**
 * The persistent class for the probe database table.
 *
 */
@Entity
@DynamicInsert(true)
@Table(name = "probe", schema = "land")
public class Probe implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ba_id")
    private Integer baId;

    @Column(name = "datenbasis_id")
    private Integer datenbasisId;

    @Column(name = "erzeuger_id")
    private Integer erzeugerId;

    @Column(name = "hauptproben_nr")
    @Convert(converter = EmptyStringConverter.class)
    private String hauptprobenNr;

    @Column(name = "ext_id")
    @Convert(converter = EmptyStringConverter.class)
    private String externeProbeId;

    @Column(name = "labor_mst_id")
    private String laborMstId;

    @Column(name = "letzte_aenderung", insertable = false)
    private Timestamp letzteAenderung;

    private String media;

    @Column(name = "media_desk")
    private String mediaDesk;

    private Long mittelungsdauer;

    @Column(name = "mpl_id")
    private Integer mplId;

    @Column(name = "mpr_id")
    private Integer mprId;

    @Column(name = "mst_id")
    private String mstId;

    @Column(name = "probe_nehmer_id")
    private Integer probeNehmerId;

    @Column(name = "probeentnahme_beginn")
    private Timestamp probeentnahmeBeginn;

    @Column(name = "probeentnahme_ende")
    private Timestamp probeentnahmeEnde;

    @Column(name = "probenart_id")
    private Integer probenartId;

    @Column(name = "solldatum_beginn")
    private Timestamp solldatumBeginn;

    @Column(name = "solldatum_ende")
    private Timestamp solldatumEnde;

    @Column(name = "ursprungszeit")
    private Timestamp ursprungszeit;

    private Boolean test;

    @Column(name = "tree_modified", insertable = false, updatable = false)
    private Timestamp treeModified;

    @OneToOne
    @JoinColumn(name = "umw_id", insertable = false, updatable = false)
    private Umwelt umwelt;

    @Column(name = "umw_id")
    private String umwId;

    @Column(name = "rei_progpunkt_grp_id")
    private Integer reiProgpunktGrpId;

    @Column(name = "kta_gruppe_id")
    private Integer ktaGruppeId;

    @Transient
    private boolean readonly;

    @Transient
    private boolean owner;

    @Transient
    @JsonIgnore
    private boolean found;

    @Transient
    @JsonIgnore
    private MultivaluedMap<String, Integer> errors;

    @Transient
    @JsonIgnore
    private MultivaluedMap<String, Integer> warnings;

    @Transient
    @JsonIgnore
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

    @JsonIgnore
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

    @JsonProperty
    public MultivaluedMap<String, Integer> getErrors() {
        return this.errors;
    }

    @JsonIgnore
    public void setErrors(MultivaluedMap<String, Integer> errors) {
        this.errors = errors;
    }

    @JsonProperty
    public MultivaluedMap<String, Integer> getWarnings() {
        return this.warnings;
    }

    @JsonIgnore
    public void setNotifications(
        MultivaluedMap<String, Integer> notifications
    ) {
      this.notifications = notifications;
    }

    @JsonProperty
    public MultivaluedMap<String, Integer> getNotifications() {
       return this.notifications;
    }

    @JsonIgnore
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
