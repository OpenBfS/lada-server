/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = SchemaName.NAME)
public class StatusAccessMpView implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private Integer curLevId;

    private Integer curValId;

    private Integer statusLevId;

    private Integer statusValId;

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

    public Integer getStatusLevId() {
        return this.statusLevId;
    }

    public void setStatusLevId(Integer levId) {
        this.statusLevId = levId;
    }

    public Integer getStatusValId() {
        return this.statusValId;
    }

    public void setStatusValId(Integer valId) {
        this.statusValId = valId;
    }

}
