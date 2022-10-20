/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.stammdaten;

import java.io.Serializable;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * The persistent class for the grid_column_values database table.
 *
 */
@Entity
@Table(name = "grid_column_values", schema = SchemaName.NAME)
public class GridColumnValue implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "column_index")
    private Integer columnIndex;

    @Column(name = "filter_active")
    private boolean filterActive;

    @Column(name = "filter_value")
    private String filterValue;

    @Column(name = "filter_negate")
    private boolean filterNegate;

    @Column(name = "filter_regex")
    private boolean filterRegex;

    @Column(name = "filter_is_null")
    private boolean filterIsNull;

    private String sort;

    @Column(name = "sort_index")
    private Integer sortIndex;

    @Column(name = "user_id")
    private Integer userId;

    private boolean visible;

    private Integer width;

    //bi-directional one-to-one association to GridColumn
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "grid_column")
    private GridColumn gridColumn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "query_user")
    private QueryUser queryUser;

    //Connected grid column's id, used for creating/updating grid_column_values
    @Transient
    private Integer gridColumnId;

    @Transient
    private Integer queryUserId;

    public GridColumnValue() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getColumnIndex() {
        return this.columnIndex;
    }

    public void setColumnIndex(Integer columnIndex) {
        this.columnIndex = columnIndex;
    }

    public boolean getFilterActive() {
        return this.filterActive;
    }

    public void setFilterActive(boolean filterActive) {
        this.filterActive = filterActive;
    }

    public String getFilterValue() {
        return this.filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public String getSort() {
        return this.sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Integer getSortIndex() {
        return this.sortIndex;
    }

    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }

    public Integer getUserId() {
        return this.userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public boolean getVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Integer getWidth() {
        return this.width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    /**
     * @return the grid column ID
     */
    public Integer getGridColumnId() {
        if (this.gridColumnId == null && this.gridColumn != null) {
            this.gridColumnId = this.gridColumn.getId();
        }
        return this.gridColumnId;
    }

    /**
     * Set the grid column id.
     * @param gid the id
     */
    public void setgridColumnId(int gid) {
        this.gridColumnId = gid;
    }

    @JsonbTransient
    public GridColumn getGridColumn() {
        return this.gridColumn;
    }

    public void setGridColumn(GridColumn gridColumn) {
        this.gridColumn = gridColumn;
    }

    @JsonbTransient
    public QueryUser getQueryUser() {
        return this.queryUser;
    }

    public void setQueryUser(QueryUser queryUser) {
        this.queryUser = queryUser;
    }

    /**
     * @return the QueryUser ID
     */
    public Integer getQueryUserId() {
        if (this.queryUserId == null && this.queryUser != null) {
            this.queryUserId = this.queryUser.getId();
        }
        return this.queryUserId;
    }

    public void setQueryUserId(int queryUserId) {
        this.queryUserId = queryUserId;
    }

    public boolean getFilterNegate() {
        return filterNegate;
    }

    public void setFilterNegate(boolean filterNegate) {
        this.filterNegate = filterNegate;
    }

    public boolean getFilterRegex() {
        return filterRegex;
    }

    public void setFilterRegex(boolean filterRegex) {
        this.filterRegex = filterRegex;
    }

    public boolean getFilterIsNull() {
        return filterIsNull;
    }

    public void setFilterIsNull(boolean filterIsNull) {
        this.filterIsNull = filterIsNull;
    }
}
