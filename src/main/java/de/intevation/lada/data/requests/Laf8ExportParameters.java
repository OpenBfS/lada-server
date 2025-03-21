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

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.HasEitherSamplesOrMeasm;


@HasEitherSamplesOrMeasm
public class Laf8ExportParameters extends LafExportParameters {

    private List<@IsValidPrimaryKey(clazz = Measm.class) Integer> messungen;

    public Laf8ExportParameters() {
        proben = new ArrayList<>();
        messungen = new ArrayList<>();
    }

    public List<Integer> getMessungen() {
        return messungen;
    }
    public void setMessungen(List<Integer> messungen) {
        this.messungen = messungen;
    }
}
