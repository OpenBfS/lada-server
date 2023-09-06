/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.json.JsonObject;

import de.intevation.lada.util.auth.UserInfo;

/**
 * Interface for Lada data exporter.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public interface Exporter {
    /**
     * Export proben and referenced messung records.
     *
     * Note: This method may not be implemented by the implementing class.
     * The default implementation returns null.
     *
     * @param proben Proben to export
     * @param messungen Messungen to export
     * @param encoding Encoding to use
     * @param userInfo Requesting user info
     * @return Exported data as InputStream or null if not implemented
     */
    default InputStream exportProben(
        List<Integer> proben,
        List<Integer> messungen,
        Charset encoding,
        UserInfo userInfo) {
            return null;
    }

    /**
     * Export Messungen and associated Proben context.
     *
     * Note: This method may not be implemented by the implementing class.
     * The default implementation returns null.
     *
     * @param proben Proben to export
     * @param messungen Messungen to export
     * @param encoding Encoding to use
     * @param userInfo Requesting user info
     * @return Exported data as InputStream or null if not implemented
     */
    default InputStream exportMessungen(
        List<Integer> proben,
        List<Integer> messungen,
        Charset encoding,
        UserInfo userInfo) {
            return null;
    }

    /**
     * Export a query result.
     *
     * Note: This method may not be implemented by the implementing class.
     * The default implementation returns null.
     * @param result Result to export as iterable of maps. Every item
     *               represents a row,
     *               while every map key represents a column
     * @param encoding Encoding to use
     * @param options Export options. Depend on the actual output format
     * @param columnsToInclude List of column names to include in the export.
     *                         If not set, all columns will be exported
     * @param subDataKey Key for subData in JSON format
     * @param qId Query id
     * @param dateFormat DateFormat for timestamp formatting
     * @param locale Locale to use
     * @return Export result as input stream or null if not implemented
     */
    default InputStream export(
        Iterable<Map<String, Object>> result,
        Charset encoding,
        JsonObject options,
        List<String> columnsToInclude,
        String subDataKey,
        Integer qId,
        DateFormat dateFormat,
        Locale locale
    ) {
        return null;
    }
}
