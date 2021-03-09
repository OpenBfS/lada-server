/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.importer;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;

import de.intevation.lada.importer.laf.LafImportJob;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Job;
import de.intevation.lada.util.data.JobManager;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;

/**
 * Class managing import jobs
 */
@ApplicationScoped
public class ImportJobManager extends JobManager {

    /**
     * The importer
     */
    @Inject
    @ImportConfig(format = ImportFormat.LAF)
    private Importer importer;

    /**
     * The data repository
     */
    @Inject
    @RepositoryConfig(type = RepositoryType.RW)
    protected Repository repository;

    public ImportJobManager() {
        activeJobs = new HashMap<String, Job>();
        logger = Logger.getLogger("ImportJobManager");
        logger.debug("Creating ImportJobManager");
    };

    public String createImportJob(UserInfo userInfo, JsonObject params, String mstId) {
        String id = getNextIdentifier();
        logger.debug(String.format("Creating new job: %s", id));

        LafImportJob newJob = new LafImportJob(id);
        newJob.setImporter(importer);
        newJob.setJsonInput(params);
        newJob.setUserInfo(userInfo);
        newJob.start();
        activeJobs.put(id, newJob);
        return id;
    }

    public String getImportResult(String id) throws JobNotFoundException {
        LafImportJob job = (LafImportJob) getJobById(id);
        String jsonString = "";
        if (job == null) {
            throw new JobNotFoundException();
        }
        logger.debug(String.format("Returning result for job %s", id));
        try {
            Map<String, Map<String, Object>> importData = job.getImportData();
            ObjectMapper objectMapper = new ObjectMapper();
            jsonString = objectMapper.writeValueAsString(importData);
        } catch (JsonProcessingException jpe) {
            logger.error(String.format("Error returning result for job %s:", id));
            jpe.printStackTrace();
        } finally {
            removeJob(job);
        }
        return jsonString;
    }
}
