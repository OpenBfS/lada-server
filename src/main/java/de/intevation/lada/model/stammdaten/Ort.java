package de.intevation.lada.model.stammdaten;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vividsolutions.jts.geom.Point;



/**
 * The persistent class for the ort database table.
 * 
 */
@Entity
@Table(name="ort")
public class Ort implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    private String aktiv;

    @Column(name="anlage_id")
    private Integer anlageId;

    private String berichtstext;

    @Column(name="gem_id")
    private String gemId;

    @Column(name="hoehe_ueber_nn")
    private Float hoeheUeberNn;

    @Column(name="hoehe_land")
    private Float hoeheLand;

    @Column(name="koord_x_extern")
    private String koordXExtern;

    @Column(name="koord_y_extern")
    private String koordYExtern;

    private String kurztext;

    private String langtext;

    @Column(name="letzte_aenderung")
    private Timestamp letzteAenderung;

    @Column(name="mp_art")
    private String mpArt;

    @Column(name="netzbetreiber_id")
    private String netzbetreiberId;

    @Column(name="nuts_code")
    private String nutsCode;

    @Column(name="ort_id")
    private String ortId;

    @Column(name="ort_typ")
    private Integer ortTyp;

    @Column(name="oz_id")
    private String ozId;

    private String sektor;

    @Column(name="staat_id")
    private Integer staatId;

    private boolean unscharf;

    private String zone;

    private String zustaendigkeit;

    @Column(name="kda_id")
    private Integer kdaId;

    @Column(columnDefinition="geometry(Point, 4326)")
    @Type(type = "org.hibernate.spatial.GeometryType")
    private Point geom;

    @Transient
    private boolean readonly;

    @Transient
    private Double longitude;

    @Transient
    private Double latitude;

    public Ort() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAktiv() {
        return this.aktiv;
    }

    public void setAktiv(String aktiv) {
        this.aktiv = aktiv;
    }

    public Integer getAnlageId() {
        return this.anlageId;
    }

    public void setAnlageId(Integer anlageId) {
        this.anlageId = anlageId;
    }

    public String getBerichtstext() {
        return this.berichtstext;
    }

    public void setBerichtstext(String berichtstext) {
        this.berichtstext = berichtstext;
    }

    public String getGemId() {
        return this.gemId;
    }

    public void setGemId(String gemId) {
        this.gemId = gemId;
    }

    public Float getHoeheUeberNn() {
        return this.hoeheUeberNn;
    }

    public void setHoeheUeberNn(Float hoeheUeberNn) {
        this.hoeheUeberNn = hoeheUeberNn;
    }

    public Float getHoeheLand() {
        return this.hoeheLand;
    }

    public void setHoeheLand(Float hoeheLand) {
        this.hoeheLand = hoeheLand;
    }

    public String getKoordXExtern() {
        return this.koordXExtern;
    }

    public void setKoordXExtern(String koordXExtern) {
        this.koordXExtern = koordXExtern;
    }

    public String getKoordYExtern() {
        return this.koordYExtern;
    }

    public void setKoordYExtern(String koordYExtern) {
        this.koordYExtern = koordYExtern;
    }

    public String getKurztext() {
        return this.kurztext;
    }

    public void setKurztext(String kurztext) {
        this.kurztext = kurztext;
    }

    public String getLangtext() {
        return this.langtext;
    }

    public void setLangtext(String langtext) {
        this.langtext = langtext;
    }

    public Double getLatitude() {
        // We might want to serialize an object without geom
        return this.geom != null
            ? this.geom.getY()
            : null;
    }

    public Timestamp getLetzteAenderung() {
        return this.letzteAenderung;
    }

    public void setLetzteAenderung(Timestamp letzteAenderung) {
        this.letzteAenderung = letzteAenderung;
    }

    public Double getLongitude() {
        // We might want to serialize an object without geom
        return this.geom != null
            ? this.geom.getX()
            : null;
    }

    public String getMpArt() {
        return this.mpArt;
    }

    public void setMpArt(String mpArt) {
        this.mpArt = mpArt;
    }

    public String getNetzbetreiberId() {
        return this.netzbetreiberId;
    }

    public void setNetzbetreiberId(String netzbetreiberId) {
        this.netzbetreiberId = netzbetreiberId;
    }

    public String getNutsCode() {
        return this.nutsCode;
    }

    public void setNutsCode(String nutsCode) {
        this.nutsCode = nutsCode;
    }

    public String getOrtId() {
        return this.ortId;
    }

    public void setOrtId(String ortId) {
        this.ortId = ortId;
    }

    public Integer getOrtTyp() {
        return this.ortTyp;
    }

    public void setOrtTyp(Integer ortTyp) {
        this.ortTyp = ortTyp;
    }

    public String getOzId() {
        return this.ozId;
    }

    public void setOzId(String ozId) {
        this.ozId = ozId;
    }

    public String getSektor() {
        return this.sektor;
    }

    public void setSektor(String sektor) {
        this.sektor = sektor;
    }

    public Integer getStaatId() {
        return this.staatId;
    }

    public void setStaatId(Integer staatId) {
        this.staatId = staatId;
    }

    public boolean getUnscharf() {
        return this.unscharf;
    }

    public void setUnscharf(boolean unscharf) {
        this.unscharf = unscharf;
    }

    public String getZone() {
        return this.zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getZustaendigkeit() {
        return this.zustaendigkeit;
    }

    public void setZustaendigkeit(String zustaendigkeit) {
        this.zustaendigkeit = zustaendigkeit;
    }

    public Integer getKdaId() {
        return this.kdaId;
    }

    public void setKdaId(Integer kdaId) {
        this.kdaId = kdaId;
    }

    @JsonIgnore
    public Point getGeom() {
        return geom;
    }

    @JsonIgnore
    public void setGeom(Point geom) {
        this.geom = geom;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

}
