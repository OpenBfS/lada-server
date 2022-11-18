/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.stammdaten;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema = SchemaName.NAME)
public class StatusAccessMpView implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private Integer curLevId;

    private Integer curValId;

    private Integer levId;

    private Integer valId;

    public StatusAccessMpView() {
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCurLevId() {
        return this.curLevId;
    }

    public void setCurLevId(Integer curLev) {
        this.curLevId = curLev;
    }

    public Integer getCurValId() {
        return this.curValId;
    }

    public void setCurValId(Integer curVal) {
        this.curValId = curVal;
    }

    public Integer getLevId() {
        return this.levId;
    }

    public void setLevId(Integer levId) {
        this.levId = levId;
    }

    public Integer getValId() {
        return this.valId;
    }

    public void setValId(Integer valId) {
        this.valId = valId;
    }

}
