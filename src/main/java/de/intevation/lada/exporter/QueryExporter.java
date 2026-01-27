/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter;

import java.io.Writer;
import java.text.DateFormat;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import de.intevation.lada.data.requests.QueryExportParameters;

/**
 * Interface for LADA data exports of query results.
 *
 * @param <T> Type of parameters supporting an implemented export format
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public interface QueryExporter<T extends QueryExportParameters> {

    /**
     * Export a query result.
     *
     * @param result Result to export as {@link Stream} of maps. Every item
     *               represents a row,
     *               while every map key represents a column
     * @param sink to write results to.
     * @param options Export options. Depend on the actual output format
     * @param columnsToInclude List of column names to include in the export.
     *                         If not set, all columns will be exported
     * @param subDataKey Key for subData in JSON format
     * @param qId Query id
     * @param dateFormat DateFormat for timestamp formatting
     * @param i18n ResourceBundle for i18n
     */
    void export(
        Stream<Map<String, Object>> result,
        Writer sink,
        T options,
        List<String> columnsToInclude,
        String subDataKey,
        Integer qId,
        DateFormat dateFormat,
        ResourceBundle i18n
    );
}
