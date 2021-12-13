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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.apache.log4j.Logger;

import de.intevation.lada.exporter.csv.CsvExportJob;
import de.intevation.lada.exporter.json.JsonExportJob;
import de.intevation.lada.exporter.laf.LafExportJob;
import de.intevation.lada.query.QueryTools;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Job;
import de.intevation.lada.util.data.JobManager;
import de.intevation.lada.util.data.Repository;

/**
 * Class creating and managing ExportJobs.
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 */
@ApplicationScoped
public class ExportJobManager extends JobManager {

    /**
     * The csv exporter.
     */
    @Inject
    @ExportConfig(format = ExportFormat.CSV)
    private Exporter csvExporter;

    /**
     * The laf exporter.
     */
    @Inject
    @ExportConfig(format = ExportFormat.LAF)
    private Exporter lafExporter;

    /**
     * The Json exporter.
     */
    @Inject
    @ExportConfig(format = ExportFormat.JSON)
    private Exporter jsonExporter;

    /**
     * The data repository granting read-only access.
     */
    @Inject
    protected Repository repository;

    @Inject
    private QueryTools queryTools;

    public ExportJobManager() {
        activeJobs = new HashMap<String, Job>();
        logger = Logger.getLogger("ExportJobManager");
        logger.debug("Creating ExportJobManager");
    };

    /**
     * Creates a new export job using the given format and parameters.
     * @param format Export format
     * @param encoding Result encoding
     * @param params Export parameters as JsonObject
     * @param locale Locale to use
     * @param userInfo UserInfo
     * @return The new ExportJob's id
     * @throws IllegalArgumentException if an invalid export format is specified
     */
    public String createExportJob(
        String format,
        String encoding,
        JsonObject params,
        Locale locale,
        UserInfo userInfo
    ) throws IllegalArgumentException {
        String id = getNextIdentifier();
        ExportJob newJob;
        logger.debug(String.format("Creating new job: %s", id));

        switch (format) {
            case "csv":
                newJob = new CsvExportJob(queryTools);
                newJob.setExporter(csvExporter);
                newJob.setLocale(locale);
                break;
            case "laf":
                newJob = new LafExportJob();
                newJob.setExporter(lafExporter);
                break;
            case "json":
                newJob = new JsonExportJob(queryTools);
                newJob.setExporter(jsonExporter);
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

        newJob.setRepository(repository);
        newJob.setDownloadFileName(downloadFileName);
        newJob.setEncoding(encoding);
        newJob.setExportParameter(params);
        newJob.setUserInfo(userInfo);
        newJob.setUncaughtExceptionHandler(new JobExceptionHandler());
        newJob.start();
        activeJobs.put(id, newJob);

        return id;
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
        return job.getEncoding();
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
        if (job == null) {
            throw new JobNotFoundException();
        }
        Path filePath = job.getOutputFilePath();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Files.copy(filePath, outputStream);
            logger.debug(String.format("Returning result file for job %s", id));
            removeJob(id);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException ioe) {
            logger.error(String.format(
                "Error on reading result file: %s", ioe.getMessage()));
            removeJob(id);
            throw new FileNotFoundException();
        }
    }
}
