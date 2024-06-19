/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.importer;

import java.util.Locale;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
     * @param locale Locale to use for message localization
     * @return New job refId
     */
    public String createImportJob(
        UserInfo userInfo, LafImportParameters params, MeasFacil mst,
        Locale locale
    ) {
        LafImportJob newJob = lafImportJobProvider.get();
        newJob.setImportParameters(params);
        newJob.setUserInfo(userInfo);
        newJob.setMst(mst);
        newJob.setLocale(locale);

        newJob.setFuture(executor.submit(newJob));
        return addJob(newJob);
    }

    /**
     * Get the import result for the job with given refId.
     * @param id Refid
     * @return Result as json string
     * @throws JobNotFoundException Thrown if job with given id was not found
     */
    public String getImportResult(String id) throws JobNotFoundException {
        LafImportJob job = (LafImportJob) getJobById(id);
        String jsonString = "";
        logger.debug(String.format("Returning result for job %s", id));
        try {
            Map<String, Map<String, Object>> importData = job.getImportData();
            ObjectMapper objectMapper = new ObjectMapper();
            jsonString = objectMapper.writeValueAsString(importData);
        } catch (JsonProcessingException jpe) {
            logger.error(
                String.format("Error returning result for job %s:", id));
            jpe.printStackTrace();
        } finally {
            removeJob(id);
        }
        return jsonString;
    }
}
