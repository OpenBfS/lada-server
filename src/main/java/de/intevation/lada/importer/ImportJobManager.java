/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.importer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import de.intevation.lada.importer.laf.LafImportJob;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.auth.UserInfo;
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
}
