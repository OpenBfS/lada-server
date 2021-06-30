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
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    protected String encoding;

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
     * Filename set by the users request.
     */
    protected String downloadFileName;

    /**
     * Temporary output file's name.
     */
    protected String outputFileName;

    /**
     * Output file's location.
     */
    protected String outputFileLocation;

    /**
     * Complete path to the output file.
     */
    protected Path outputFilePath;

    /**
     * Repository used for loading data.
     */
    protected Repository repository;

    /**
     * Create a new job with the given id.
     * @param jobId Job identifier
     */
    public ExportJob(String jId) {
        this.jobId = jId;
        this.currentStatus = new JobStatus(Status.WAITING, "", false);
        // TODO: Use e.g. Files.createTempFile() to make it more portable
        this.outputFileLocation = "/tmp/lada-server/";
        if (!outputFileLocation.endsWith("/")) {
            outputFileLocation += "/";
        }
        this.outputFileName = jobId;
        this.outputFilePath = Paths.get(outputFileLocation + outputFileName);
    }

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
     * Set this job to a running state.
     */
    protected void runnning() {
        try {
            this.setCurrentStatus(Status.RUNNING);
        } catch (IllegalStatusTransitionException iste) {
            this.currentStatus.setStatus(Status.ERROR);
            this.currentStatus.setMessage("Internal server errror");
            this.currentStatus.setDone(true);
        }
    }

    /**
     * Get the filename used for downloading.
     * @return Filename as String
     */
    public String getDownloadFileName() {
        return downloadFileName;
    }

    /**
     * Get the encoding.
     * @return Encoding as String
     */
    public String getEncoding() {
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
     * Get the output file name.
     * @return Output file name String
     */
    public String getOutputFileName() {
        return outputFileName;
    }

    /**
     * Get the export file's path.
     * @return File path
     */
    public Path getOutputFilePath() {
        return outputFilePath;
    }

    /**
     * Checks if given charset is valid.
     *
     * Note that charset names are not case sensitive.
     * See https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/charset/Charset.html
     * for further information.
     * @return True if charset is valid, else false
     */
    protected boolean isEncodingValid() {
        return Charset.isSupported(this.encoding);
    }

    /**
     * Set the filename used for downloading the result file.
     * @param downloadFileName File name
     */
    public void setDownloadFileName(String downloadFileName) {
        this.downloadFileName = downloadFileName;
    }

    /**
     * Set the export encoding.
     * @param encoding Encoding as String
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Set the exporter instance.
     * @param exporter The exporter instance
     */
    public void setExporter(Exporter exporter) {
        this.exporter = exporter;
    }

    /**
     * Set parameters used for the export.
     * @param exportParams Parameters as JsonObject
     */
    public void setExportParameter(JsonObject exportParams) {
        this.exportParameters = exportParams;
    }

    /**
     * Set the repository.
     * @param repository Repository instance
     */
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    /**
     * Remove the export's result file if present.
     */
    protected void removeResultFile() {
        try {
            Files.delete(outputFilePath);
        } catch (NoSuchFileException nsfe) {
            logger.debug("Can not remove result file: File not found");
        } catch (IOException ioe) {
            logger.error(String.format(
                "Cannot delete result file. IOException: %s",
                ioe.getMessage()));
        }
    }

    /**
     * Write the export result to a file.
     * @param result Result string to export
     * @return True if written successfully, else false
     */
    protected boolean writeResultToFile(String result) {
        Path tmpPath = Paths.get(outputFileLocation);
        logger.debug(String.format(
            "Writing result to file %s", outputFilePath));

        //Create dir
        if (!Files.exists(tmpPath)) {
            try {
                Files.createDirectories(tmpPath);
            } catch (IOException ioe) {
                logger.error(String.format(
                    "Cannot create export folder. IOException: %s",
                    ioe.getMessage()));
                return false;
            } catch (SecurityException se) {
                logger.error(String.format(
                    "Security Exception during directory creation %s",
                    se.getMessage()));
                return false;
            }
        }

        //Create file
        try {
            Files.createFile(outputFilePath);
        } catch (FileAlreadyExistsException faee) {
            logger.error("Cannot create export file. File already exists");
            return false;
        } catch (IOException ioe) {
            logger.error(String.format(
                "Cannot create export file. IOException: %s",
                ioe.getMessage()));
            return false;
        } catch (SecurityException se) {
            logger.error(String.format(
                "Security Exception during file creation %s",
                se.getMessage()));
            return false;
        }

        //Write to file
        try (BufferedWriter writer =
            Files.newBufferedWriter(
                outputFilePath, Charset.forName(encoding))) {
            writer.write(result);
        } catch (IOException ioe) {
            logger.error(String.format(
                "Cannot write to export file. IOException: %s",
                ioe.getMessage()));
            return false;
        }

        return true;
    }
}
