/* Copyright (C) 2018 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.master;

import java.io.Serializable;

import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.NotEmptyNorWhitespace;
import de.intevation.lada.validation.groups.DatabaseConstraints;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(schema = SchemaName.NAME)
@GroupSequence({ GridColConf.class, DatabaseConstraints.class })
public class GridColConf implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer colIndex;

    private boolean isFilterActive;

    private String filterVal;

    private boolean isFilterNegate;

    private boolean isFilterRegex;

    private boolean isFilterNull;

    @NotEmptyNorWhitespace
    @Size(max = 4)
    private String sort;

    private Integer sortIndex;

    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = LadaUser.class)
    private Integer ladaUserId;

    private boolean isVisible;

    private Integer width;

    @Transient
    private boolean export;

    //bi-directional one-to-one association to GridColumn
    @ManyToOne(fetch = FetchType.EAGER)
    private GridColMp gridColMp;

    @ManyToOne(fetch = FetchType.EAGER)
    private QueryUser queryUser;

    //Connected grid column's id, used for creating/updating grid_column_values
    @Transient
    @NotNull
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = GridColMp.class)
    private Integer gridColMpId;

    @Transient
    @NotNull
    @IsValidPrimaryKey(
        groups = DatabaseConstraints.class, clazz = QueryUser.class)
    private Integer queryUserId;

    public GridColConf() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getColIndex() {
        return this.colIndex;
    }

    public void setColIndex(Integer colIndex) {
        this.colIndex = colIndex;
    }

    public boolean getIsFilterActive() {
        return this.isFilterActive;
    }

    public void setIsFilterActive(boolean isFilterActive) {
        this.isFilterActive = isFilterActive;
    }

    public String getFilterVal() {
        return this.filterVal;
    }

    public void setFilterVal(String filterVal) {
        this.filterVal = filterVal;
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

    public Integer getLadaUserId() {
        return this.ladaUserId;
    }

    public void setLadaUserId(Integer userId) {
        this.ladaUserId = userId;
    }

    public boolean getIsVisible() {
        return this.isVisible;
    }

    public void setIsVisible(boolean isVisible) {
        this.isVisible = isVisible;
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
    public Integer getGridColMpId() {
        if (this.gridColMpId == null && this.gridColMp != null) {
            this.gridColMpId = this.gridColMp.getId();
        }
        return this.gridColMpId;
    }

    /**
     * Set the grid column id.
     * @param gridColMpId the id
     */
    public void setGridColMpId(int gridColMpId) {
        this.gridColMpId = gridColMpId;
    }

    @JsonbTransient
    public GridColMp getGridColMp() {
        return this.gridColMp;
    }

    public void setGridColMp(GridColMp gridColMp) {
        this.gridColMp = gridColMp;
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

    public void setQueryUserId(Integer queryUserId) {
        this.queryUserId = queryUserId;
    }

    public boolean getIsFilterNegate() {
        return isFilterNegate;
    }

    public void setIsFilterNegate(boolean isFilterNegate) {
        this.isFilterNegate = isFilterNegate;
    }

    public boolean getIsFilterRegex() {
        return isFilterRegex;
    }

    public void setIsFilterRegex(boolean isFilterRegex) {
        this.isFilterRegex = isFilterRegex;
    }

    public boolean getIsFilterNull() {
        return isFilterNull;
    }

    public void setIsFilterNull(boolean isFilterNull) {
        this.isFilterNull = isFilterNull;
    }

    public boolean isExport() {
        return export;
    }

    public void setExport(boolean export) {
        this.export = export;
    }
}
