/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter.laf;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.data.Repository;


/**
 * The LAF exporter produces a LAF file.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class Laf8Exporter {

    @Inject
    private Authorization authorization;

    /**
     * The repository used to read data.
     */
    @Inject
    private Repository repository;

    /**
     * Export the {@link Sample} objects.
     *
     * @param proben    List of probe ids.
     * @param messungen    List of messung ids.
     * @param laf sink to write out result
     */
    public void exportProben(
        List<Integer> proben,
        List<Integer> messungen,
        Writer laf
    ) throws IOException {
        try (LafCreator creator = new LafCreator(
                authorization, repository, laf)
        ) {
            for (Integer probeId: proben) {
                creator.createProbe(probeId);
            }
            for (Integer messungId: messungen) {
                Measm m = repository.getById(Measm.class, messungId);
                List<Integer> mList = new ArrayList<>();
                mList.add(messungId);
                creator.createMessung(m.getSample().getId(), mList);
            }
        }
    }
}
