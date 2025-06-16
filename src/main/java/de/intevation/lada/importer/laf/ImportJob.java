/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.importer.laf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import de.intevation.lada.importer.Report;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.data.Job;
import de.intevation.lada.util.data.TagUtil;


/**
 * Asynchronous import job.
 */
public abstract class ImportJob<T> extends Job {

    protected Map<String, Report> importData = new HashMap<>();

    protected Map<String, T> files;

    protected MeasFacil mst;

    @Inject
    protected TagUtil tagUtil;

    public void cleanup() {
        //Intentionally left blank
    }

    public Map<String, Report> getImportData() {
        return importData;
    }

    public void setFiles(Map<String, T> files) {
        this.files = files;
    }

    public void setMst(MeasFacil mst) {
        this.mst = mst;
    }

    protected void tagImportedData(
        List<Integer> importedSampleIds, MeasFacil mst
    ) {
        // If import created at least a new record
        if (!importedSampleIds.isEmpty()) {
            //Generate a tag for the imported probe records
            Tag newTag = tagUtil.generateTag("IMP", mst.getNetworkId());
            tagUtil.setTagsByProbeIds(
                importedSampleIds, newTag.getId());

            //Put new tag in import response
            importData.forEach((file, responseData) -> {
                    responseData.setTag(newTag.getName());
            });
        }
    }
}
