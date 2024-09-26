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

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Inject;

import de.intevation.lada.data.requests.CsvExportParameters;
import de.intevation.lada.data.requests.ExportParameters;
import de.intevation.lada.data.requests.QueryExportParameters;
import de.intevation.lada.data.requests.LafExportParameters;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.JobManager;


/**
 * Class creating and managing ExportJobs.
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 */
public class ExportJobManager extends JobManager {

    @Inject
    private Instance<ExportJob<? extends ExportParameters>> exportJobProvider;

    public ExportJobManager() {
        logger.debug("Creating ExportJobManager");
    };

    /**
     * Creates a new export job using the given format and parameters.
     * @param <F> Type of export parameters bound to format
     * @param encoding Result encoding
     * @param params Export parameters as JsonObject
     * @param bundle ResourceBundle for export i18n
     * @param userInfo UserInfo
     * @return The new ExportJob's id
     * @throws IllegalArgumentException if an invalid export format is specified
     */
    public <F extends ExportParameters> String createExportJob(
        Charset encoding,
        F params,
        ResourceBundle bundle,
        UserInfo userInfo
    ) throws IllegalArgumentException {
        ExportJob<?> newJob;
        String format;
        if (params instanceof CsvExportParameters p) {
            TypeLiteral<ExportJob<CsvExportParameters>> type
                = new TypeLiteral<>() { };
            ExportJob<CsvExportParameters> job
                = exportJobProvider.select(type).get();
            job.setExportParameter(p);
            job.setBundle(bundle);
            format = "csv";
            newJob = job;
        } else if (params instanceof LafExportParameters p) {
            TypeLiteral<ExportJob<LafExportParameters>> type
                = new TypeLiteral<>() { };
            ExportJob<LafExportParameters> job
                = exportJobProvider.select(type).get();
            job.setExportParameter(p);
            format = "laf";
            newJob = job;
        } else if (params instanceof QueryExportParameters p) {
            TypeLiteral<ExportJob<QueryExportParameters>> type
                = new TypeLiteral<>() { };
            ExportJob<QueryExportParameters> job
                = exportJobProvider.select(type).get();
            job.setExportParameter(p);
            format = "json";
            newJob = job;
        } else {
            throw new IllegalArgumentException("Unkown export format");
        }

        String downloadFileName =
            params.getFilename() != null && !params.getFilename().isBlank()
                ? params.getFilename()
                : String.format("export.%s", format);
        newJob.setDownloadFileName(downloadFileName);
        newJob.setEncoding(encoding);
        newJob.setUserInfo(userInfo);
        return addJob(newJob);
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
        ExportJob<?> job = (ExportJob) getJobById(id);
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
        ExportJob<?> job = (ExportJob) getJobById(id);
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
