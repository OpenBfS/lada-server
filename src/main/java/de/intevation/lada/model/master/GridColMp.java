/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(schema = SchemaName.NAME)
public class GridColMp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String dataIndex;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "data_type")
    private Disp dataType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "filter")
    private Filter filter;

    private String gridCol;

    private Integer position;

    private Integer baseQuery;

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

    public Disp getDataType() {
        return this.dataType;
    }

    public void setDataType(Disp dataType) {
        this.dataType = dataType;
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

    public Integer getBaseQuery() {
        return this.baseQuery;
    }

    public void setBaseQuery(Integer query) {
        this.baseQuery = query;
    }
}
