/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = SchemaName.NAME)
public class GridColMp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String dataIndex;

    @ManyToOne(fetch = FetchType.EAGER)
    private Disp disp;

    @ManyToOne(fetch = FetchType.EAGER)
    private Filter filter;

    private String gridCol;

    private Integer position;

    private Integer baseQueryId;

    public GridColMp() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDataIndex() {
        return this.dataIndex;
    }

    public void setDataIndex(String dataIndex) {
        this.dataIndex = dataIndex;
    }

    public Disp getDisp() {
        return this.disp;
    }

    public void setDisp(Disp dataType) {
        this.disp = dataType;
    }

    public Filter getFilter() {
        return this.filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public String getGridCol() {
        return this.gridCol;
    }

    public void setGridCol(String gridCol) {
        this.gridCol = gridCol;
    }

    public Integer getPosition() {
        return this.position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getBaseQueryId() {
        return this.baseQueryId;
    }

    public void setBaseQueryId(Integer query) {
        this.baseQueryId = query;
    }
}
