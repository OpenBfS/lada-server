/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.exporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

import de.intevation.lada.data.requests.ExportParameters;
import de.intevation.lada.util.data.Job;


/**
 * Abstract class for an export job.
 *
 * @param <T> Type of parameters supporting an implemented export format
 */
public abstract class ExportJob<T extends ExportParameters> extends Job<File> {

    /**
     * Result encoding.
     */
    protected Charset encoding;

    /**
     * Parameters used for the export.
     */
    protected T exportParameters;

    /**
     * ResourceBundle for export i18n.
     */
    protected ResourceBundle bundle;

    /**
     * Complete path to the output file.
     */
    private Path outputFile;

    public Charset getEncoding() {
        return this.encoding;
    }

    /**
     * Get the export file.
     * @return File
     */
    public Path getOutputFile() {
        return outputFile;
    }

    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }

    /**
     * Set parameters used for the export.
     * @param exportParams Parameters as JsonObject
     */
    public void setExportParameter(T exportParams) {
        this.exportParameters = exportParams;
    }

    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    protected BufferedWriter createTmpFileWriter() throws IOException {
        this.outputFile = Files.createTempFile("export-", "");
        logger.debug(String.format(
                "Writing result to file %s", outputFile));
        return Files.newBufferedWriter(outputFile, encoding);
    }
}
