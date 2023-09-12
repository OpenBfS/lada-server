/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.exporter.csv;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Collection;

import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.persistence.NoResultException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jboss.logging.Logger;

import de.intevation.lada.exporter.ExportConfig;
import de.intevation.lada.exporter.ExportFormat;
import de.intevation.lada.exporter.Exporter;
import de.intevation.lada.model.master.GridColMp;
import de.intevation.lada.model.master.StatusLev;
import de.intevation.lada.model.master.StatusMp;
import de.intevation.lada.model.master.StatusVal;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;

/**
 * Exporter class for writing query results to CSV.
 *
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 */
@ExportConfig(format = ExportFormat.CSV)
public class CsvExporter implements Exporter {

    private static final String BUNDLE_FILE = "lada_server";

    @Inject Logger logger;

    @Inject
    private Repository repository;

    /**
     * Enum storing all possible csv options.
     */
    private enum CsvOptions {
        comma(","), period("."), semicolon(";"), space(" "),
        singlequote("'"), doublequote("\""),
        linux("\n"), windows("\r\n");

        private final String value;

        CsvOptions(String v) {
            this.value = v;
        }

        public char getChar() {
            return this.value.charAt(0);
        }

        public String getValue() {
            return this.value;
        }
    }

    private String getStatusStringByid(Integer id) {
        StatusMp kombi =
            repository.getByIdPlain(StatusMp.class, id);
        StatusLev stufe = kombi.getStatusLev();
        StatusVal wert = kombi.getStatusVal();

        return String.format("%s - %s", stufe.getLev(), wert.getVal());
    }

    /**
     * Return an array of readable column names.
     *
     * The names are either fetched from the database or used from the given
     * sub data column name object
     * @param keys Array of GridColMp.dataIndex keys to get names for
     * @param subDataColumnNames Object containing sub data column names
     * @param qId GridColMp.baseQueryId for filtering GridColMp objects
     * @return Name array
     */
    private String[] getReadableColumnNames(
        Collection<String> keys,
        JsonObject subDataColumnNames,
        Integer qId
    ) {
        String[] names = new String[keys.size()];
        int index = 0;
        for (String key : keys) {
            String name = key;

            QueryBuilder<GridColMp> builder =
                repository.queryBuilder(GridColMp.class)
                .and("dataIndex", key)
                .and("baseQueryId", qId);
            try {
                GridColMp column =
                    repository.getSinglePlain(builder.getQuery());
                name = column.getGridCol();
            } catch (NoResultException e) {
                name = subDataColumnNames != null
                    && subDataColumnNames.containsKey(key)
                    ? subDataColumnNames.getString(key)
                    : key;
            }
            names[index] = name;
            index++;
        }
        return names;
    }

    /**
     * Export a query result.
     * @param queryResult Result to export as list of maps.
     *                    Every list item represents a row,
     *                    while every map key represents a column
     * @param encoding Encoding to use
     * @param options Optional export options as JSON Object.
     *                Valid options are "csvOptions" with: <p>
     *   <ul>
     *     <li> decimalSeparator: "comma" | "period", defaults to "period" </li>
     *     <li> fieldSeparator: "comma" | "semicolon" | "period" |
     *          "space", defaults to "comma" </li>
     *     <li> rowDelimiter: "windows" | "linux", defaults to "windows" </li>
     *     <li> quoteType: "singlequote" |
     *          "doublequote", defaults to "doublequote" </li>
     *   </ul>
     * and "subDataColumnNames": JsonObject containing dataIndex:
     * ColumnName key-value-pairs used to get readable column names
     * Invalid options will cause the export to fail.
     *
     * @param columnsToInclude List of column names to include in the export.
     *                         If not set, all columns will be exported
     * @param qId query id
     * @param locale Locale to use
     * @return Export result as input stream or null if the export failed
     */
    @Override
    public InputStream export(
        Iterable<Map<String, Object>> queryResult,
        Charset encoding,
        JsonObject options,
        List<String> columnsToInclude,
        String subDataKey,
        Integer qId,
        DateFormat dateFormat,
        Locale locale
    ) {
        ResourceBundle i18n = ResourceBundle.getBundle(BUNDLE_FILE, locale);

        char decimalSeparator = CsvOptions.valueOf("period").getChar();
        char fieldSeparator = CsvOptions.valueOf("comma").getChar();
        String rowDelimiter = CsvOptions.valueOf("windows").getValue();
        char quoteType = CsvOptions.valueOf("doublequote").getChar();
        JsonObject subDataColumnNames = null;
        //Parse options
        if (options != null) {
            try {
                if (options.containsKey("csvOptions")) {
                    JsonObject csvOptions = options.getJsonObject("csvOptions");
                    decimalSeparator = CsvOptions.valueOf(
                        csvOptions.containsKey("decimalSeparator")
                        ? csvOptions.getString("decimalSeparator")
                        : "period").getChar();
                    fieldSeparator = CsvOptions.valueOf(
                        csvOptions.containsKey("fieldSeparator")
                        ? csvOptions.getString(
                            "fieldSeparator") : "comma").getChar();
                    rowDelimiter = CsvOptions.valueOf(
                        csvOptions.containsKey("rowDelimiter")
                        ? csvOptions.getString(
                            "rowDelimiter") : "windows").getValue();
                    quoteType = CsvOptions.valueOf(
                        csvOptions.containsKey("quoteType")
                        ? csvOptions.getString(
                            "quoteType") : "doublequote").getChar();
                }
                final String subDataColumnNamesKey = "subDataColumnNames";
                subDataColumnNames =
                    options.containsKey(subDataColumnNamesKey)
                    && !options.isNull(subDataColumnNamesKey)
                    ? options.getJsonObject(subDataColumnNamesKey) : null;
            } catch (IllegalArgumentException iae) {
                logger.error(
                    String.format(
                        "Invalid CSV options: %s", options.toString()));
                return null;
            }
        }

        DecimalFormat decimalFormat = new DecimalFormat("0.###E00");
        DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
        symbols.setDecimalSeparator(decimalSeparator);
        decimalFormat.setDecimalFormatSymbols(symbols);
        decimalFormat.setGroupingUsed(false);

        //Get header fields
        String[] header = getReadableColumnNames(
            columnsToInclude, subDataColumnNames, qId);

        //Create CSV format
        CSVFormat format = CSVFormat.DEFAULT
            .withDelimiter(fieldSeparator)
            .withQuote(quoteType)
            .withRecordSeparator(rowDelimiter)
            .withHeader(header);

        StringBuffer result = new StringBuffer();

        try {
            final CSVPrinter printer = new CSVPrinter(result, format);
            //For every queryResult row
            queryResult.forEach(row -> {
                ArrayList<String> rowItems = new ArrayList<String>();
                for (String key: columnsToInclude) {
                    Object value = row.get(key);

                    //Value is a status kombi
                    if (key.equals("statusK")) {
                        rowItems.add(getStatusStringByid((Integer) value));
                    } else if (key.equals("latitude")
                        | key.equals("longitude")) {
                        rowItems.add(value.toString());
                    } else if (value instanceof Double) {
                        decimalFormat.applyPattern("0.###E00");
                        rowItems.add(decimalFormat.format((Double) value));
                    } else if (value instanceof Float) {
                        decimalFormat.applyPattern("###0.0#");
                        rowItems.add(decimalFormat.format((Float) value));
                    } else if (value instanceof Timestamp) {
                        //Convert to target timezone
                        Timestamp time = (Timestamp) value;
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date(time.getTime()));
                        rowItems.add(dateFormat.format(calendar.getTime()));
                    } else if (value instanceof Boolean) {
                        rowItems.add(value != null
                            ? i18n.getString(value.toString()) : null);
                    } else {
                        rowItems.add(value != null ? value.toString() : null);
                    }
                }
                try {
                    printer.printRecord(rowItems);
                } catch (IOException ioe) {
                    logger.error(
                        String.format(
                            "Error on printing records: %s", ioe.toString()));
                }
            });

            printer.close();
            return new ByteArrayInputStream(
                result.toString().getBytes(encoding));
        } catch (UnsupportedEncodingException uee) {
            logger.error(
                String.format("Unsupported encoding: %s", encoding.name()));
            return null;
        } catch (IOException ioe) {
            logger.error(ioe.toString());
            return null;
        }
    }
}
