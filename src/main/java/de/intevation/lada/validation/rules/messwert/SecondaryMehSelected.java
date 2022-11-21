/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messwert;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import de.intevation.lada.model.land.Messwert;
import de.intevation.lada.model.master.EnvMedium;
import de.intevation.lada.model.master.MeasUnit;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for messwert.
 * Validates if the "messeinheit" is the secondary "messeinheit" of to
 * umweltbereich connected to this messwert
 */
@ValidationRule("Messwert")
public class SecondaryMehSelected implements Rule {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Messwert messwert = (Messwert) object;
        EnvMedium umwelt = null;
        Violation violation = new Violation();

        if (messwert.getMessung() != null
                && messwert.getMessung().getSample() != null) {
            umwelt = messwert.getMessung().getSample().getUmwelt();
        }

        // If umwelt record is present
        if (umwelt != null) {
            Integer mehId = umwelt.getUnit1();
            Integer secMehId = umwelt.getUnit2();
            //If secondary meh is set
            if (secMehId == null) {
                return null;
            }
            //Check if the messwert is the secondary mehId
            if (secMehId.equals(messwert.getMehId())) {
                violation.addNotification("mehId", StatusCodes.VAL_SEC_UNIT);
                return violation;
            }
            /*Check if the messwert is convertable into the secondary unit but
            not into the primary */
            MeasUnit meh =
                repository.getByIdPlain(
                    MeasUnit.class, mehId);
            MeasUnit secMeh =
                repository.getByIdPlain(
                    MeasUnit.class, secMehId);
            AtomicBoolean primary = new AtomicBoolean(false);
            meh.getUnitConversTo().forEach(umrechnung -> {
                if (umrechnung.getFromUnit().getId()
                    .equals(messwert.getMehId())
                ) {
                    primary.set(true);
                }
            });
            if (primary.get()) {
                return null;
            }
            secMeh.getUnitConversTo().forEach(secUmrechnung -> {
                if (secUmrechnung.getFromUnit().getId()
                    .equals(messwert.getMehId())
                ) {
                    violation.addNotification(
                        "mehId", StatusCodes.VAL_SEC_UNIT);
                }
            });
        }
        return violation;
    }
}
