/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;

import static jakarta.persistence.TemporalType.TIMESTAMP;


@Entity
@Table(schema = SchemaName.NAME)
public class EnvDescripEnvMediumMp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    @Column(name = "s00")
    private Integer s00;

    @Column(name = "s01")
    private Integer s01;

    @Column(name = "s02")
    private Integer s02;

    @Column(name = "s03")
    private Integer s03;

    @Column(name = "s04")
    private Integer s04;

    @Column(name = "s05")
    private Integer s05;

    @Column(name = "s06")
    private Integer s06;

    @Column(name = "s07")
    private Integer s07;

    @Column(name = "s08")
    private Integer s08;

    @Column(name = "s09")
    private Integer s09;

    @Column(name = "s10")
    private Integer s10;

    @Column(name = "s11")
    private Integer s11;

    private String envMediumId;

    @Column(insertable = false)
    @Temporal(TIMESTAMP)
    private Date lastMod;

    public EnvDescripEnvMediumMp() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getS00() {
        return this.s00;
    }

    public void setS00(Integer s00) {
        this.s00 = s00;
    }

    public Integer getS01() {
        return this.s01;
    }

    public void setS01(Integer s01) {
        this.s01 = s01;
    }

    public Integer getS02() {
        return this.s02;
    }

    public void setS02(Integer s02) {
        this.s02 = s02;
    }

    public Integer getS03() {
        return this.s03;
    }

    public void setS03(Integer s03) {
        this.s03 = s03;
    }

    public Integer getS04() {
        return this.s04;
    }

    public void setS04(Integer s04) {
        this.s04 = s04;
    }

    public Integer getS05() {
        return this.s05;
    }

    public void setS05(Integer s05) {
        this.s05 = s05;
    }

    public Integer getS06() {
        return this.s06;
    }

    public void setS06(Integer s06) {
        this.s06 = s06;
    }

    public Integer getS07() {
        return this.s07;
    }

    public void setS07(Integer s07) {
        this.s07 = s07;
    }

    public Integer getS08() {
        return this.s08;
    }

    public void setS08(Integer s08) {
        this.s08 = s08;
    }

    public Integer getS09() {
        return this.s09;
    }

    public void setS09(Integer s09) {
        this.s09 = s09;
    }

    public Integer getS10() {
        return this.s10;
    }

    public void setS10(Integer s10) {
        this.s10 = s10;
    }

    public Integer getS11() {
        return this.s11;
    }

    public void setS11(Integer s11) {
        this.s11 = s11;
    }

    public String getEnvMediumId() {
        return this.envMediumId;
    }

    public void setEnvMediumId(String envMediumId) {
        this.envMediumId = envMediumId;
    }

    public Date getLastMod() {
        return this.lastMod;
    }

    public void setLastMod(Date lastMod) {
        this.lastMod = lastMod;
    }
}
