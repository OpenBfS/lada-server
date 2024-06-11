/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.probe;

import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.DeskriptorToUmweltImpl;


/**
 * Validation rule for probe.
 * Validates if the umwelt id fits the deskriptor string.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@ValidationRule("Sample")
public class DeskriptorToUmwelt extends DeskriptorToUmweltImpl {

    @Override
    public Violation execute(Object object) {
        Sample probe = (Sample) object;
        String envDescripDisplay = probe.getEnvDescripDisplay();
        String envMediumId = probe.getEnvMediumId();
        if (envDescripDisplay == null
            || envDescripDisplay.isEmpty()
            || envMediumId == null
        ) {
            return null;
        }
        return doExecute(
            envDescripDisplay, envMediumId, probe.getRegulationId());
    }
}
