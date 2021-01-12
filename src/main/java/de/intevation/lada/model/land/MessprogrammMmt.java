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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import de.intevation.lada.util.data.IntegerArrayType;
/**
 * The persistent class for the messprogramm_mmt database table.
 *
 */
@Entity
@Table(name = "messprogramm_mmt", schema = "land")
@TypeDefs({@TypeDef(name = "IntegerArray", typeClass = IntegerArrayType.class)})
public class MessprogrammMmt implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "letzte_aenderung", insertable = false)
    private Timestamp letzteAenderung;

    @Type(type = "IntegerArray")
    private Integer[] messgroessen;

    @Column(name = "messprogramm_id")
    private Integer messprogrammId;

    @Column(name = "mmt_id")
    private String mmtId;

    public MessprogrammMmt() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Timestamp getLetzteAenderung() {
        return this.letzteAenderung;
    }

    public void setLetzteAenderung(Timestamp letzteAenderung) {
        this.letzteAenderung = letzteAenderung;
    }

    public Integer[] getMessgroessen() {
        return this.messgroessen;
    }

    public void setMessgroessen(Integer[] messgroessen) {
        this.messgroessen = messgroessen;
    }

    public Integer getMessprogrammId() {
        return this.messprogrammId;
    }

    public void setMessprogrammId(Integer messprogrammId) {
        this.messprogrammId = messprogrammId;
    }

    public String getMmtId() {
        return this.mmtId;
    }

    public void setMmtId(String mmtId) {
        this.mmtId = mmtId;
    }

}
