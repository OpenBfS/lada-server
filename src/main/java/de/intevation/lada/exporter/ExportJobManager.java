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
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.apache.log4j.Logger;

import de.intevation.lada.exporter.ExportJob.JobNotFinishedException;
import de.intevation.lada.exporter.ExportJob.Status;
import de.intevation.lada.exporter.csv.CsvExportJob;
import de.intevation.lada.exporter.json.JsonExportJob;
import de.intevation.lada.exporter.laf.LafExportJob;
import de.intevation.lada.query.QueryTools;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;

/**
 * Class creating and managing ExportJobs.
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 */
@ApplicationScoped
public class ExportJobManager {

    private static JobIdentifier identifier =
        new ExportJobManager.JobIdentifier();

    private Logger logger;

    private Map<String, ExportJob> activeJobs =
        new HashMap<String, ExportJob>();

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
    @RepositoryConfig(type = RepositoryType.RO)
    private Repository repository;

    @Inject
    private QueryTools queryTools;

    public ExportJobManager() {
        logger = Logger.getLogger("ExportJobManager");
        logger.debug("Creating ExportJobManager");
    };

    /**
     * Creates a new export job using the given format and parameters.
     * @param format Export format
     * @param encoding Result encoding
     * @param params Export parameters as JsonObject
     * @param userInfo UserInfo
     * @return The new ExportJob's id
     * @throws IllegalArgumentException if an invalid export format is specified
     */
    public String createExportJob(
        String format,
        String encoding,
        JsonObject params,
        UserInfo userInfo
    ) throws IllegalArgumentException {
        String id = getNextIdentifier();
        ExportJob newJob;
        logger.debug(String.format("Creating new job: %s", id));

        switch (format) {
            case "csv":
                newJob = new CsvExportJob(id, queryTools);
                newJob.setExporter(csvExporter);
                break;
            case "laf":
                newJob = new LafExportJob(id);
                newJob.setExporter(lafExporter);
                break;
            case "json":
                newJob = new JsonExportJob(id, queryTools);
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
        newJob.start();
        activeJobs.put(id, newJob);

        return id;
    }

    /**
     * Get Exportjob by id.
     * @param id Id to look for
     * @throws JobNotFoundException Thrown if a job with the given can not
     *                              be found
     */
    private ExportJob getJobById(
        String id
    ) throws JobNotFoundException {
        ExportJob job = activeJobs.get(id);
        if (job == null) {
            throw new JobNotFoundException();
        }
        return job;
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
     * Get the status of a job by identifier.
     *
     * If the job is done with an error, it will be removed after return
     * the failure status.
     * @param id Id to look for
     * @return Job status
     * @throws JobNotFoundException Thrown if a job with the given can not
     *                              be found
     */
    public JobStatus getJobStatus(
        String id
    ) throws JobNotFoundException {
        ExportJob job = getJobById(id);
        String jobStatus = job.getStatusName();
        String message = job.getMessage();
        boolean done = job.isDone();
        JobStatus statusObject = new JobStatus(jobStatus, message, done);
        if (jobStatus.equals(Status.error.name()) && done) {
            removeExportJob(job);
        }
        return statusObject;
    }

    /**
     * Get the user informations for the current job by identifier.
     * @param id Id to look for.
     * @return The user info
     * @throws JobNotFoundException Thrown if a job with the given can not
     *                              be found
     */
    public UserInfo getJobUserInfo(
        String id
    ) throws JobNotFoundException {
        ExportJob job = getJobById(id);
        return job.getUserInfo();
    }

    /**
     * Calculates and returns the next job identifier.
     *
     * The new identifier will be stored in lastIdentifier.
     * @return New identifier as String
     */
    private synchronized String getNextIdentifier() {
        identifier.next();
        return identifier.toString();
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
        ExportJob job = activeJobs.get(id);
        if (job == null) {
            throw new JobNotFoundException();
        }
        Path filePath = job.getOutputFilePath();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Files.copy(filePath, outputStream);
            logger.debug(String.format("Returning result file for job %s", id));
            removeExportJob(job);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException ioe) {
            logger.error(String.format(
                "Error on reading result file: %s", ioe.getMessage()));
            removeExportJob(job);
            throw new FileNotFoundException();
        }
    }

    /**
     * Remove the given job from the active job list and trigger its
     * cleanup function.
     * @param job Job to remove
     */
    private void removeExportJob(ExportJob job) {
        try {
            logger.debug(String.format("Removing job %s", job.getJobId()));
            job.cleanup();
        } catch (JobNotFinishedException jfe) {
            logger.warn(String.format(
                "Tried to remove unfinished job %s", job.getJobId()));
        }
        activeJobs.remove(job.getJobId());
    }

    /**
     * Class modeling a job status.
     * Stores job status and message
     */
    public static class JobStatus {
        private String status;
        private String message;
        private boolean done;

        public JobStatus(String s, String m, boolean d) {
            this.status = s;
            this.message = m;
            this.done = d;
        }

        public boolean isDone() {
            return done;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Thrown if a job with the given can not be found.
     */
    public static class JobNotFoundException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    /**
     * Utility class providing unique identifier values for export jobs.
     *
     * The identifier can be set to the next value by using the next() method
     * and obtained as hex String by using the toString() method.
     *
     * Identifier format:
     * [timestamp]-[sequenceNumber]-[randomPart]
     * timestamp: Timestamp in seconds the identifier was set to the next
     *            value (64 bits)
     * sequenceNumber: Sequence number, will be reset for each
     *                 timestamp (16 bits)
     * randomPart: Random number (32 bits)
     *
     * The hexadecimal representation will contain leading zeroes.
     */
    private static class JobIdentifier {

        /**
         * Format string for the hexadecimal representation.
         */
        private final String hexFormat;

        private static final short INITIAL_SEQ_NO = 1;

        private short seqNo;

        private long timestamp;

        private int randomPart;

        /**
         * Create the identifier with an initial value.
         */
        JobIdentifier() {
            seqNo = INITIAL_SEQ_NO;
            timestamp = System.currentTimeMillis();
            randomPart = 0;
            String longMaxValueHex = Long.toHexString(Long.MAX_VALUE);
            String intMaxValueHex = Integer.toHexString(Integer.MAX_VALUE);
            String shortMaxValueHex = "7fff";
            int longHexWidth = longMaxValueHex.length();
            int intHexWidth = intMaxValueHex.length();
            int shortHexWidth = shortMaxValueHex.length();
            StringBuilder formatBuilder = new StringBuilder("%1$0")
                .append(longHexWidth)
                .append("x-")
                .append("%2$0")
                .append(shortHexWidth)
                .append("x-")
                .append("%3$0")
                .append(intHexWidth)
                .append("x");
            hexFormat = formatBuilder.toString();
        }

        /**
         * Set the identifier to the next value.
         */
        public void next() {
            long currentTime = System.currentTimeMillis();
            if (currentTime == timestamp) {
                seqNo++;
            } else {
                timestamp = currentTime;
                seqNo = INITIAL_SEQ_NO;
            }
            randomPart = ThreadLocalRandom.current().nextInt();
        }

        /**
         * Return the hexadecimal string representation of this identifier.
         * The string will include padding zeroes.
         */
        public String toString() {
            return String.format(hexFormat, timestamp, seqNo, randomPart);
        }
    }
}
