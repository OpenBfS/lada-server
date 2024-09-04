/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data.requests;

import java.util.List;
import java.util.TimeZone;

import de.intevation.lada.model.master.GridColConf;

import jakarta.validation.Valid;

public class QueryExportParameters extends ExportParameters {
    private String[] subDataColumns;
    private String idField;
    private List<String> idFilter;
    private TimeZone timezone;

    private List<@Valid GridColConf> columns;

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

    public TimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }

    public List<GridColConf> getColumns() {
        return columns;
    }

    public void setColumns(List<GridColConf> columns) {
        this.columns = columns;
    }
}
