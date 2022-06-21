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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import de.intevation.lada.model.stammdaten.Messgroesse;

/**
 * The persistent class for the messprogramm_mmt database table.
 *
 */
@Entity
@Table(name = "messprogramm_mmt", schema = SchemaName.NAME)
public class MessprogrammMmt implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "letzte_aenderung", insertable = false)
    private Timestamp letzteAenderung;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "messprogramm_mmt_messgroesse",
        schema = SchemaName.NAME,
        joinColumns = @JoinColumn(name = "messprogramm_mmt_id"),
        inverseJoinColumns = @JoinColumn(name = "messgroesse_id")
    )
    private Set<Messgroesse> messgroesseObjects;

    @Column(name = "messprogramm_id")
    private Integer messprogrammId;

    @Column(name = "mmt_id")
    private String mmtId;

    @Transient
    private Integer[] messgroessen;

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

    public void setMessgroesseObjects(Set<Messgroesse> messgroesseObjects) {
        this.messgroesseObjects = messgroesseObjects;
    }

    /**
     * @return Integer[] IDs of associated Messgroesse objects.
     */
    public Integer[] getMessgroessen() {
        if (this.messgroessen == null && this.messgroesseObjects != null) {
            Set<Integer> ids = new HashSet<>();
            for (Messgroesse m: this.messgroesseObjects) {
                ids.add(m.getId());
            }
            this.messgroessen = ids.toArray(new Integer[ids.size()]);
        }
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
