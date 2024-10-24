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

import de.intevation.lada.data.requests.LafImportParameters;
import de.intevation.lada.importer.laf.LafImportJob;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.JobManager;


/**
 * Class managing import jobs.
 */
public class ImportJobManager extends JobManager {

    @Inject
    private Provider<LafImportJob> lafImportJobProvider;

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
    public String createImportJob(
        UserInfo userInfo,
        LafImportParameters params,
        MeasFacil mst,
        Map<String, String> files
    ) {
        LafImportJob newJob = lafImportJobProvider.get();
        newJob.setImportParameters(params);
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
    public Map<String, Map<String, Object>> getImportResult(
        String id, UserInfo userInfo
    ) {
        LafImportJob job = (LafImportJob) getJobById(id, userInfo);
        logger.debug(String.format("Returning result for job %s", id));
        try {
             return job.getImportData();
        } finally {
            removeJob(id);
        }
    }
}
