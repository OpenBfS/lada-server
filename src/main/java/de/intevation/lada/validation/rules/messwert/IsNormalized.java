/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.messwert;

import javax.inject.Inject;

import java.util.List;

import de.intevation.lada.model.land.Messwert;
import de.intevation.lada.model.stammdaten.Umwelt;
import de.intevation.lada.model.stammdaten.UnitConvers;
import de.intevation.lada.model.stammdaten.Measd;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.QueryBuilder;
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
public class IsNormalized implements Rule {

    /**
     * The data repository granting read access.
     */
    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Messwert messwert = (Messwert) object;
        Umwelt umwelt = null;
        Violation violation = new Violation();

        if (messwert.getMessung() != null
                && messwert.getMessung().getProbe() != null) {
            umwelt = messwert.getMessung().getProbe().getUmwelt();
        }

        // If umwelt record is present
        if (umwelt != null) {
            Integer mehId = umwelt.getUnit1();
            Integer secMehId = umwelt.getUnit2();

            //If  meh is not set
            if (mehId == null && secMehId == null){
                violation.addWarning("mehId", StatusCodes.VAL_UNIT_UMW);
                return violation;
            }

            //Check if the messwert mehId can be converted to primary or secondary meh
            Boolean convert = false;

            if (mehId != null && !mehId.equals(messwert.getMehId())) {
                QueryBuilder<UnitConvers> builder =
                repository.queryBuilder(UnitConvers.class);
                builder.and("toUnitId", mehId);
                builder.and("fromUnit", messwert.getMehId());
                List<UnitConvers> result = repository.filterPlain(builder.getQuery());
                convert = result.size() > 0;
            } else if (secMehId != null && !secMehId.equals(messwert.getMehId())) {
                QueryBuilder<UnitConvers> builder =
                repository.queryBuilder(UnitConvers.class);
                builder.and("toUnitId", secMehId);
                builder.and("fromUnit", messwert.getMehId());
                List<UnitConvers> result = repository.filterPlain(builder.getQuery());
                convert = result.size() > 0;
            }

            if (convert) {
                QueryBuilder<Measd> builder_messgr = repository.queryBuilder(Measd.class);
                builder_messgr.and("id", messwert.getMessgroesseId());
                List<Measd> messgroesse = repository.filterPlain(builder_messgr.getQuery());
                violation.addWarning("mehId#"+messgroesse.get(0).getName(), StatusCodes.VAL_UNIT_NORMALIZE);
            } else if ( (mehId != null && mehId.equals(messwert.getMehId())) || (secMehId != null && secMehId.equals(messwert.getMehId())) ) {
                return null;
            } else {
                QueryBuilder<Measd> builder_messgr = repository.queryBuilder(Measd.class);
                builder_messgr.and("id", messwert.getMessgroesseId());
                List<Measd> messgroesse = repository.filterPlain(builder_messgr.getQuery());
                violation.addWarning("mehId#"+messgroesse.get(0).getName(), StatusCodes.VAL_UNIT_UMW);
            }
        }
        return violation;
    }
}
