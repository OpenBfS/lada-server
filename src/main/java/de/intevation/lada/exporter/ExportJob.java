/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.exporter;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
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

    private static final int LENGTH = 1024;

    /**
     * Result encoding.
     */
    protected Charset encoding;

    /**
     * Exporter instance.
     */
    protected Exporter<T> exporter;

    /**
     * Parameters used for the export.
     */
    protected T exportParameters;

    /**
     * The export format.
     */
    protected String format;

    /**
     * ResourceBundle for export i18n.
     */
    protected ResourceBundle bundle;

    /**
     * Filename set by the users request.
     */
    protected String downloadFileName;

    /**
     * Complete path to the output file.
     */
    protected Path outputFilePath;

    /**
     * Clean up after the export has finished.
     *
     * Removes the result file
     * @throws JobNotFinishedException Thrown if job is still running
     */
    public void cleanup() throws JobNotFinishedException {
        if (!currentStatus.isDone()) {
            throw new JobNotFinishedException();
        }
        removeResultFile();
    }

    /**
     * Get the filename used for downloading.
     * @return Filename as String
     */
    public String getDownloadFileName() {
        return downloadFileName;
    }

    public Charset getEncoding() {
        return this.encoding;
    }

    /**
     * Get the export format as String.
     * @return Export format as String
     */
    public String getFormat() {
        return format;
    }

    /**
     * Get the export file's path.
     * @return File path
     */
    public Path getOutputFilePath() {
        return outputFilePath;
    }

    /**
     * Set the filename used for downloading the result file.
     * @param downloadFileName File name
     */
    public void setDownloadFileName(String downloadFileName) {
        this.downloadFileName = downloadFileName;
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
        if (this.outputFilePath != null) {
            try {
                Files.delete(this.outputFilePath);
            } catch (NoSuchFileException nsfe) {
                logger.debug("Can not remove result file: File not found");
            } catch (IOException ioe) {
                logger.error(String.format(
                        "Cannot delete result file. IOException: %s",
                        ioe.getMessage()));
            }
        }
    }

    /**
     * Write the export result to a file.
     * @param exported Result InputStream to export
     * @throws RuntimeException if temp file cannot be created
     * or writing it fails.
    */
    protected void writeResultToFile(InputStream exported) {
        try {
            //Create file
            this.outputFilePath =
                File.createTempFile("export-", "." + this.format).toPath();
            logger.debug(String.format(
                    "Writing result to file %s", outputFilePath));

            //Write to file
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[LENGTH];
            int length;

            while ((length = exported.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            try (BufferedWriter writer =
                Files.newBufferedWriter(outputFilePath, encoding)) {
                writer.write(new String(result.toByteArray(), encoding));
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
