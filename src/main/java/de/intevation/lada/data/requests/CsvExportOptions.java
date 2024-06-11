/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data.requests;

import de.intevation.lada.exporter.csv.CsvExporter.CsvOptions;

public class CsvExportOptions {
    private CsvOptions decimalSeparator;
    private CsvOptions fieldSeparator;
    private CsvOptions rowDelimiter;
    private CsvOptions quoteType;
    public CsvExportOptions() {};
    public CsvOptions getDecimalSeparator() {
        return decimalSeparator;
    }
    public void setDecimalSeparator(CsvOptions decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }
    public CsvOptions getFieldSeparator() {
        return fieldSeparator;
    }
    public void setFieldSeparator(CsvOptions fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }
    public CsvOptions getRowDelimiter() {
        return rowDelimiter;
    }
    public void setRowDelimiter(CsvOptions rowDelimiter) {
        this.rowDelimiter = rowDelimiter;
    }
    public CsvOptions getQuoteType() {
        return quoteType;
    }
    public void setQuoteType(CsvOptions quoteType) {
        this.quoteType = quoteType;
    }
}
