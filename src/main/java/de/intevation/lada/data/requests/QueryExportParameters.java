/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data.requests;

import java.util.List;

import de.intevation.lada.model.master.GridColConf;

public class QueryExportParameters extends ExportParameters {
    private boolean exportSubData;
    private String[] subDataColumns;
    private String idField;
    private List<String> idFilter;
    private String timezone;

    private List<GridColConf> columns;

    public boolean isExportSubData() {
        return exportSubData;
    }

    public void setExportSubData(boolean exportSubData) {
        this.exportSubData = exportSubData;
    }

    public String[] getSubDataColumns() {
        return subDataColumns;
    }

    public void setSubDataColumns(String[] subDataColumns) {
        this.subDataColumns = subDataColumns;
    }

    public String getIdField() {
        return idField;
    }

    public void setIdField(String idField) {
        this.idField = idField;
    }

    public List<String> getIdFilter() {
        return idFilter;
    }

    public void setIdFilter(List<String> idFilter) {
        this.idFilter = idFilter;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public List<GridColConf> getColumns() {
        return columns;
    }

    public void setColumns(List<GridColConf> columns) {
        this.columns = columns;
    }
}
