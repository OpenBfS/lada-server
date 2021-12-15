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
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Locale;

import javax.inject.Inject;
import javax.json.JsonObject;

import de.intevation.lada.util.data.Job;
import de.intevation.lada.util.data.Repository;

/**
 * Abstract class for an export job.
 */
public abstract class ExportJob extends Job {

    /**
     * Result encoding.
     */
    protected Charset encoding;

    /**
     * Exporter instance.
     */
    protected Exporter exporter;

    /**
     * Parameters used for the export.
     */
    protected JsonObject exportParameters;

    /**
     * The export format.
     */
    protected String format;

    /**
     * Export locale.
     */
    protected Locale locale;

    /**
     * Filename set by the users request.
     */
    protected String downloadFileName;

    /**
     * Complete path to the output file.
     */
    protected Path outputFilePath;

    /**
     * Repository used for loading data.
     */
    @Inject
    protected Repository repository;

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

    /*
     * Get the locale.
     * @return Locale object
     */
    public Locale getLocale() {
        return this.locale;
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
    public void setExportParameter(JsonObject exportParams) {
        this.exportParameters = exportParams;
    }

    /**
     * Set the locale used for the export.
     * @param locale Locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
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
     * @param result Result string to export
     * @throws IOException if temp file cannot be created or writing it fails.
     */
    protected void writeResultToFile(String result) throws IOException {
        //Create file
        this.outputFilePath =
            File.createTempFile("export-", "." + this.format).toPath();
        logger.debug(String.format(
                "Writing result to file %s", outputFilePath));

        //Write to file
        try (BufferedWriter writer =
            Files.newBufferedWriter(outputFilePath, encoding)) {
            writer.write(result);
        }
    }
}
