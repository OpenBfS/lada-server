/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data.requests;

import java.util.Map;

import de.intevation.lada.exporter.csv.CsvExporter.CsvOptions;

public class CsvExportParameters extends QueryExportParameters {
    private Map<String, String> subDataColumnNames;
    private CsvExportOptions csvOptions;

    public Map<String, String> getSubDataColumnNames() {
        return subDataColumnNames;
    }
    public void setSubDataColumnNames(Map<String, String> subDataColumnNames) {
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
