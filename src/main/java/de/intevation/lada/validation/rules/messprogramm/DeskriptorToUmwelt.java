/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messprogramm;

import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.DeskriptorToUmweltImpl;


/**
 * Validation rule for Mpg.
 * Validates if the umwelt id fits the deskriptor string.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Messprogramm")
public class DeskriptorToUmwelt extends DeskriptorToUmweltImpl {

    @Override
    public Violation execute(Object object) {
        Mpg messprogramm = (Mpg) object;
        String envDescripDisplay = messprogramm.getEnvDescripDisplay();
        String envMediumId = messprogramm.getEnvMediumId();
        if (envDescripDisplay == null
            || envDescripDisplay.isEmpty()
            || envMediumId == null
        ) {
            return null;
        }
        return doExecute(
            envDescripDisplay, envMediumId, messprogramm.getRegulationId());
    }
}
