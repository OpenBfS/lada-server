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
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Collection;
import java.util.Date;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jboss.logging.Logger;

import de.intevation.lada.data.requests.CsvExportParameters;
import de.intevation.lada.exporter.ExportConfig;
import de.intevation.lada.exporter.ExportFormat;
import de.intevation.lada.exporter.Exporter;
import de.intevation.lada.model.master.GridColMp;
import de.intevation.lada.model.master.GridColMp_;
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
public class CsvExporter implements Exporter<CsvExportParameters> {

    @Inject Logger logger;

    @Inject
    private Repository repository;


    private String getStatusStringByid(Integer id) {
        StatusMp kombi =
            repository.getById(StatusMp.class, id);
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
        Map<String, String> subDataColumnNames,
        Integer qId
    ) {
        String[] names = new String[keys.size()];
        int index = 0;
        for (String key : keys) {
            String name = key;

            QueryBuilder<GridColMp> builder =
                repository.queryBuilder(GridColMp.class)
                .and(GridColMp_.dataIndex, key)
                .and(GridColMp_.baseQueryId, qId);
            try {
                GridColMp column =
                    repository.getSingle(builder.getQuery());
                name = column.getGridCol();
            } catch (NoResultException e) {
                name = subDataColumnNames != null
                    && subDataColumnNames.containsKey(key)
                    ? subDataColumnNames.get(key)
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
     * @param options CsvExportParameters
     * @param columnsToInclude List of column names to include in the export.
     *                         If not set, all columns will be exported
     * @param qId query id
     * @param i18n ResourceBundle for i18n
     * @return Export result as input stream or null if the export failed
     */
    @Override
    public InputStream export(
        Iterable<Map<String, Object>> queryResult,
        Charset encoding,
        CsvExportParameters options,
        List<String> columnsToInclude,
        String subDataKey,
        Integer qId,
        DateFormat dateFormat,
        ResourceBundle i18n
    ) {
        Map<String, String> subDataColumnNames = null;
        if (options != null) {
            subDataColumnNames = options.getSubDataColumnNames();
        }

        DecimalFormat decimalFormat = new DecimalFormat("0.###E00");
        if (options.getDecimalSeparator() != null) {
            DecimalFormatSymbols symbols = decimalFormat
                .getDecimalFormatSymbols();
            symbols.setDecimalSeparator(options.getDecimalSeparator());
            decimalFormat.setDecimalFormatSymbols(symbols);
        }
        decimalFormat.setGroupingUsed(false);

        //Get header fields
        String[] header = getReadableColumnNames(
            columnsToInclude, subDataColumnNames, qId);

        //Create CSV format
        CSVFormat.Builder format = CSVFormat.DEFAULT.builder()
            .setHeader(header);
        if (options.getFieldSeparator() != null) {
            format.setDelimiter(options.getFieldSeparator());
        }
        if (options.getRowDelimiter() != null) {
            format.setRecordSeparator(options.getRowDelimiter());
        }
        if (options.getQuote() != null) {
            format.setQuote(options.getQuote());
        }

        StringBuffer result = new StringBuffer();

        try {
            final CSVPrinter printer = new CSVPrinter(result, format.build());
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
                    } else if (value instanceof Date) {
                        //Convert to target timezone
                        Date time = (Date) value;
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(time);
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
        } catch (IOException ioe) {
            logger.error(ioe.toString());
            return null;
        }
    }
}
