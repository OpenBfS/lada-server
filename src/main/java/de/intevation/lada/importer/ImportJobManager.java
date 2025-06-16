/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.importer;

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import de.intevation.lada.data.requests.Laf8ImportParameters;
import de.intevation.lada.importer.laf.ImportJob;
import de.intevation.lada.importer.laf.Laf8ImportJob;
import de.intevation.lada.importer.laf.Laf9ImportJob;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.JobManager;


/**
 * Class managing import jobs.
 */
public class ImportJobManager extends JobManager {

    @Inject
    private Provider<Laf8ImportJob> laf8ImportJobProvider;

    @Inject
    private Provider<Laf9ImportJob> laf9ImportJobProvider;

    public ImportJobManager() {
        logger.debug("Creating ImportJobManager");
    };

    /**
     * Create a new import job.
     * @param userInfo User info
     * @param params Parameters
     * @param mst MessStelle
     * @param files Decoded files
     * @return New job jobId
     */
    public String createLaf8ImportJob(
        UserInfo userInfo,
        Laf8ImportParameters params,
        MeasFacil mst,
        Map<String, String> files
    ) {
        Laf8ImportJob newJob = laf8ImportJobProvider.get();
        newJob.setImportParameters(params);
        newJob.setUserInfo(userInfo);
        newJob.setMst(mst);
        newJob.setFiles(files);
        return addJob(newJob);
    }

    /**
     * Create a new import job.
     * @param userInfo User info
     * @param files Decoded files
     * @return New job jobId
     */
    public String createLaf9ImportJob(
        UserInfo userInfo,
        MeasFacil mst,
        Map<String, List<Sample>> files
    ) {
        Laf9ImportJob newJob = laf9ImportJobProvider.get();
        newJob.setUserInfo(userInfo);
        newJob.setMst(mst);
        newJob.setFiles(files);
        return addJob(newJob);
    }

    /**
     * Get the import result for the job with given jobId.
     * @param id jobId
     * @param userInfo for authorization
     * @return Import result report data for requested job
     */
    public Map<String, Report> getImportResult(
        String id, UserInfo userInfo
    ) {
        ImportJob<?> job = (ImportJob<?>) getJobById(id, userInfo);
        logger.debug(String.format("Returning result for job %s", id));
        try {
             return job.getImportData();
        } finally {
            removeJob(id);
        }
    }
}
