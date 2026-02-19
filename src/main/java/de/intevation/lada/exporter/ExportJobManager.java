/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.exporter;

import java.nio.charset.Charset;
import java.util.ResourceBundle;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Inject;

import de.intevation.lada.data.requests.CsvExportParameters;
import de.intevation.lada.data.requests.ExportParameters;
import de.intevation.lada.data.requests.Laf8ExportParameters;
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
        } else if (params instanceof Laf8ExportParameters p) {
            TypeLiteral<ExportJob<Laf8ExportParameters>> type
                = new TypeLiteral<>() { };
            ExportJob<Laf8ExportParameters> job
                = exportJobProvider.select(type).get();
            job.setExportParameter(p);
            format = "laf";
            newJob = job;
        } else if (params instanceof LafExportParameters p) {
            TypeLiteral<ExportJob<LafExportParameters>> type
                = new TypeLiteral<>() { };
            ExportJob<LafExportParameters> job
                = exportJobProvider.select(type).get();
            job.setExportParameter(p);
            format = "json";
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
}
