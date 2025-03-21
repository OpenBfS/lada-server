/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data.requests;

import java.util.ArrayList;
import java.util.List;

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;


public class LafExportParameters extends ExportParameters {
    protected List<@IsValidPrimaryKey(clazz = Sample.class) Integer> proben;

    public LafExportParameters() {
        proben = new ArrayList<>();
    }

    public List<Integer> getProben() {
        return proben;
    }
    public void setProben(List<Integer> proben) {
        this.proben = proben;
    }
}
