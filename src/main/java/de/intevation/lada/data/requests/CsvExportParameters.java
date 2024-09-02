/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data.requests;

import java.util.Map;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public class CsvExportParameters extends QueryExportParameters {

    private Map<String, String> subDataColumnNames;

    // Commons CSV does not allow the delimiter to be a line break
    @Pattern(regexp = "[^\r\n]*")
    private String fieldSeparator;

    private String rowDelimiter;

    // Commons CSV does not allow the quote character to be a line break
    @Pattern(regexp = "[^\r\n]")
    private String quote;

    // Serializing into Character just ignores anything but the first char.
    // Thus use String and validate in order to have a more clear behavior.
    @Size(max = 1)
    private String decimalSeparator;


    public Map<String, String> getSubDataColumnNames() {
        return subDataColumnNames;
    }

    public void setSubDataColumnNames(Map<String, String> subDataColumnNames) {
        this.subDataColumnNames = subDataColumnNames;
    }

    public String getFieldSeparator() {
        return this.fieldSeparator;
    }

    public void setFieldSeparator(String fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    public String getRowDelimiter() {
        return this.rowDelimiter;
    }

    public void setRowDelimiter(String rowDelimiter) {
        this.rowDelimiter = rowDelimiter;
    }

    public Character getQuote() {
        return this.quote != null ? this.quote.charAt(0) : null;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public Character getDecimalSeparator() {
        return this.decimalSeparator != null
            ? this.decimalSeparator.charAt(0)
            : null;
    }

    public void setDecimalSeparator(String decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }
}
