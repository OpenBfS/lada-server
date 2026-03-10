/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.exporter.laf;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.List;

import jakarta.inject.Inject;
import de.intevation.lada.data.requests.Laf8ExportParameters;
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
    private Laf8Exporter exporter;

    @Override
    public File callWithTx() throws IOException {
        logger.debug("Starting LAF export");
        List<Integer> pIds = exportParameters.getProben();
        List<Integer> mIds = exportParameters.getMessungen();

        try (Writer writer = createTmpFileWriter()) {
            exporter.exportProben(pIds, mIds, writer);
            logger.debug(String.format("Finished LAF export"));
            return this.getOutputFile().toFile();
        } catch (Exception e) {
            // Prevent orphaned result file of failed export
            Files.deleteIfExists(this.getOutputFile());
            throw e;
        }
    }
}
