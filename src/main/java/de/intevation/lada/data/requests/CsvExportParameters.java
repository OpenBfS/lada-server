/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data.requests;

import de.intevation.lada.exporter.csv.CsvExporter.CsvOptions;

import jakarta.json.JsonObject;

public class CsvExportParameters extends QueryExportParameters {
    private JsonObject subDataColumnNames;
    private CsvExportOptions csvOptions;

    public JsonObject getSubDataColumnNames() {
        return subDataColumnNames;
    }
    public void setSubDataColumnNames(JsonObject subDataColumnNames) {
        this.subDataColumnNames = subDataColumnNames;
    }
    public CsvExportOptions getCsvOptions() {
        return csvOptions;
    }
    public void setCsvOptions(CsvExportOptions csvOptions) {
        this.csvOptions = csvOptions;
    }

    public CsvOptions getDecimalSeparator() {
        return csvOptions != null ? csvOptions.getDecimalSeparator() : null;
    }
    public CsvOptions getFieldSeparator() {
        return csvOptions != null ? csvOptions.getFieldSeparator() : null;
    }
    public CsvOptions getRowDelimiter() {
        return csvOptions != null ? csvOptions.getRowDelimiter() : null;
    }
    public CsvOptions getQuoteType() {
        return csvOptions != null ? csvOptions.getQuoteType() : null;
    }
}
