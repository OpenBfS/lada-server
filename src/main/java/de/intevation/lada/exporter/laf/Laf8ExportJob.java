/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.exporter.laf;

import java.util.List;

import jakarta.inject.Inject;
import de.intevation.lada.data.requests.Laf8ExportParameters;
import de.intevation.lada.exporter.Exporter;
import de.intevation.lada.exporter.ExportJob;

/**
 * Job class for exporting records to a laf file.
 *
 * @author <a href="mailto:awoestmann@intevation.de">Alexander Woestmann</a>
 */
public class Laf8ExportJob extends ExportJob<Laf8ExportParameters> {

    /**
     * The laf exporter.
     */
    @Inject
    private Exporter<Laf8ExportParameters> exporter;

    public Laf8ExportJob() {
        this.format = "laf";
        this.downloadFileName = "export.laf";
    }

    /**
     * Start the export.
     */
    @Override
    public void runWithTx() {
        logger.debug("Starting LAF export");
        List<Integer> pIds = exportParameters.getProben();
        List<Integer> mIds = exportParameters.getMessungen();

        //Export and write to file
        writeResultToFile(
            exporter.exportProben(pIds, mIds, encoding, userInfo));

        logger.debug(String.format("Finished LAF export"));
    }
}
