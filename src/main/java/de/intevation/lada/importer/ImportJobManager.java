/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.importer;

import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import de.intevation.lada.data.requests.Laf8ImportParameters;
import de.intevation.lada.data.requests.Laf9ImportParameters;
import de.intevation.lada.data.requests.LafImportParameters;
import de.intevation.lada.importer.laf.ImportJob;
import de.intevation.lada.importer.laf.Laf8ImportJob;
import de.intevation.lada.importer.laf.Laf9ImportJob;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.JobManager;
import de.intevation.lada.util.data.Repository;


/**
 * Class managing import jobs.
 */
public class ImportJobManager extends JobManager {

    @Inject
    private Repository repository;

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
     * @return New job jobId
     */
    @SuppressWarnings("unchecked")
    public <T> String createLafImportJob(
        UserInfo userInfo,
        LafImportParameters<T> params
    ) {
        MeasFacil mst = repository.getById(
            MeasFacil.class, params.getMeasFacilId());

        ImportJob<T> newJob;
        if (params instanceof Laf8ImportParameters laf8params) {
            Laf8ImportJob laf8ImportJob = laf8ImportJobProvider.get();
            laf8ImportJob.setImportParameters(laf8params);
            newJob = (ImportJob<T>) laf8ImportJob;
        } else if (params instanceof Laf9ImportParameters) {
            newJob = (ImportJob<T>) laf9ImportJobProvider.get();
        } else {
            throw new IllegalArgumentException();
        }
        newJob.setUserInfo(userInfo);
        newJob.setMst(mst);
        newJob.setFiles(params.getFiles());
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
