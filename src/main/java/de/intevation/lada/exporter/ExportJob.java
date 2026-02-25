/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.exporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ResourceBundle;

import de.intevation.lada.data.requests.ExportParameters;
import de.intevation.lada.util.data.Job;


/**
 * Abstract class for an export job.
 *
 * @param <T> Type of parameters supporting an implemented export format
 */
public abstract class ExportJob<T extends ExportParameters> extends Job {

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

    /**
     * Clean up after the export has finished.
     *
     * Removes the result file
     * @throws JobNotFinishedException Thrown if job is still running
     */
    public void cleanup() throws JobNotFinishedException {
        if (!this.future.isDone()) {
            throw new JobNotFinishedException();
        }
        removeResultFile();
    }

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

    /**
     * Remove the export's result file if present.
     */
    protected void removeResultFile() {
        if (this.outputFile != null) {
            try {
                Files.delete(this.outputFile);
            } catch (NoSuchFileException nsfe) {
                logger.debug("Can not remove result file: File not found");
            } catch (IOException ioe) {
                logger.error(String.format(
                        "Cannot delete result file. IOException: %s",
                        ioe.getMessage()));
            }
        }
    }

    protected BufferedWriter createTmpFileWriter() {
        try {
            this.outputFile = Files.createTempFile("export-", "");
            logger.debug(String.format(
                    "Writing result to file %s", outputFile));
            return Files.newBufferedWriter(outputFile, encoding);
        } catch (IOException e) {
            throw new RuntimeException("Could not create tmp file", e);
        }
    }
}
