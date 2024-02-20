/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.exporter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.json.JsonObject;

import de.intevation.lada.exporter.csv.CsvExportJob;
import de.intevation.lada.exporter.json.JsonExportJob;
import de.intevation.lada.exporter.laf.LafExportJob;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.JobManager;

/**
 * Class creating and managing ExportJobs.
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 */
public class ExportJobManager extends JobManager {

    @Inject
    private Provider<CsvExportJob> csvExportJobProvider;

    @Inject
    private Provider<JsonExportJob> jsonExportJobProvider;

    @Inject
    private Provider<LafExportJob> lafExportJobProvider;

    public ExportJobManager() {
        logger.debug("Creating ExportJobManager");
    };

    /**
     * Creates a new export job using the given format and parameters.
     * @param format Export format
     * @param encoding Result encoding
     * @param params Export parameters as JsonObject
     * @param bundle ResourceBundle for export i18n
     * @param userInfo UserInfo
     * @return The new ExportJob's id
     * @throws IllegalArgumentException if an invalid export format is specified
     */
    public String createExportJob(
        String format,
        Charset encoding,
        JsonObject params,
        ResourceBundle bundle,
        UserInfo userInfo
    ) throws IllegalArgumentException {
        ExportJob newJob;
        switch (format) {
            case "csv":
                newJob = csvExportJobProvider.get();
                newJob.setBundle(bundle);
                break;
            case "laf":
                newJob = lafExportJobProvider.get();
                break;
            case "json":
                newJob = jsonExportJobProvider.get();
                break;
            default:
                logger.error(String.format("Unkown export format: %s", format));
                throw new IllegalArgumentException(
                    String.format("%s is not a valid export format", format));
        }

        String downloadFileName =
            params.containsKey("filename")
                ? params.getString("filename")
                : String.format("export.%s", format);

        newJob.setDownloadFileName(downloadFileName);
        newJob.setEncoding(encoding);
        newJob.setExportParameter(params);
        newJob.setUserInfo(userInfo);
        newJob.setFuture(executor.submit(newJob));
        return addJob(newJob);
    }

    /**
     * Get Exportjob by id.
     * @param id Id to look for
     * @throws JobNotFoundException Thrown if a job with the given can not
     *                              be found
     * @return Job instance with given id
     */
    protected ExportJob getJobById(
        String id
    ) throws JobNotFoundException {
        return (ExportJob) super.getJobById(id);
    }

    /**
     * Get the encoding of an export job by id.
     * @param id Id to check
     * @return Encoding as String
     * @throws JobNotFoundException Thrown if a job with the given can not
     *                              be found
     */
    public String getJobEncoding(
        String id
    ) throws JobNotFoundException {
        ExportJob job = getJobById(id);
        return job.getEncoding().name();
    }

    /**
     * Get the filename used for downloading by the given job id.
     * @param id Job id
     * @return Filename as String
     * @throws JobNotFoundException Thrown if a job with the given can not
     *                              be found
     */
    public String getJobDownloadFilename(
        String id
    ) throws JobNotFoundException {
        ExportJob job = getJobById(id);
        return job.getDownloadFileName();
    }

    /**
     * Get the result file of the export job with the given id as stream.
     * @param id ExportJob id
     * @return Result file as stream
     * @throws JobNotFoundException Thrown if a job with the given can not
     *                              be found
     * @throws FileNotFoundException Thrown if the job exists but the result
     *                               was deleted or can not be read
     */
    public ByteArrayInputStream getResultFileAsStream(
        String id
    ) throws JobNotFoundException, FileNotFoundException {
        ExportJob job = getJobById(id);
        Path filePath = job.getOutputFilePath();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Files.copy(filePath, outputStream);
            logger.debug(String.format("Returning result file for job %s", id));
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException ioe) {
            logger.error(String.format(
                "Error on reading result file: %s", ioe.getMessage()));
            throw new FileNotFoundException();
        } finally {
            removeJob(id);
        }
    }
}
