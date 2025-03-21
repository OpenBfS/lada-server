/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.exporter.laf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import de.intevation.lada.data.requests.LafExportParameters;
import de.intevation.lada.exporter.ExportConfig;
import de.intevation.lada.exporter.ExportFormat;
import de.intevation.lada.exporter.Exporter;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.Repository;

/**
* The LAF exporter implements {@link Exporter} to produce a LAF file.
*
* @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
*/
@ExportConfig(format = ExportFormat.LAF)
public class LafExporter implements Exporter<LafExportParameters> {

    /**
     * The creator used to generate content.
     */
    @Inject
    private LafCreator creator;

    /**
     * The repository used to read data.
     */
    @Inject
    private Repository repository;

    /**
     * Export the {@link LProbe} objects.
     *
     * @param proben    List of probe ids.
     * @param messungen    List of messung ids.
     * @param encoding The encoding of the resulting input stream
     * @param userInfo The information about the current user
     * @return InputStream with the LAF data.
     */
    @Override
    public InputStream exportProben(
        List<Integer> proben,
        List<Integer> messungen,
        Charset encoding,
        UserInfo userInfo
    ) {
        String laf = "";
        creator.setUserInfo(userInfo);
        for (Integer probeId: proben) {
            laf += creator.createProbe(probeId);
        }
        for (Integer messungId: messungen) {
            Measm m = repository.getById(
                Measm.class, messungId);
            List<Integer> mList = new ArrayList<>();
            mList.add(messungId);
            laf += creator.createMessung(m.getSampleId(), mList);
        }
        laf += "%ENDE%";
        InputStream in = new ByteArrayInputStream(laf.getBytes(encoding));
        try {
            in.close();
            return in;
        } catch (IOException e) {
            String resp = "Error - Problem while creating the response";
            InputStream is = new ByteArrayInputStream(
                resp.getBytes(encoding));
            return is;
        }
    }
}
