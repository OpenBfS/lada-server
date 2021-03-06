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
import de.intevation.lada.model.stammdaten.MassEinheitUmrechnung;
import de.intevation.lada.model.stammdaten.Messgroesse;
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
            Integer mehId = umwelt.getMehId();
            Integer secMehId = umwelt.getSecMehId();

            //If  meh is not set
            if (mehId == null && secMehId == null){
                violation.addWarning("mehId", StatusCodes.VAL_UNIT_UMW);
                return violation;
            }

            //Check if the messwert mehId can be converted to primary or secondary meh
            Boolean convert = false;

            if (mehId != null && !mehId.equals(messwert.getMehId())) {
                QueryBuilder<MassEinheitUmrechnung> builder =
                repository.queryBuilder(MassEinheitUmrechnung.class);
                builder.and("mehIdZu", mehId);
                builder.and("mehVon", messwert.getMehId());
                List<MassEinheitUmrechnung> result = repository.filterPlain(builder.getQuery());
                convert = result.size()>0 ? true : false;
            } else if (secMehId != null && !secMehId.equals(messwert.getMehId())) {
                QueryBuilder<MassEinheitUmrechnung> builder =
                repository.queryBuilder(MassEinheitUmrechnung.class);
                builder.and("mehIdZu", secMehId);
                builder.and("mehVon", messwert.getMehId());
                List<MassEinheitUmrechnung> result = repository.filterPlain(builder.getQuery());
                convert = result.size()>0 ? true : false;
            }

            if (convert) {
                QueryBuilder<Messgroesse> builder_messgr = repository.queryBuilder(Messgroesse.class);
                builder_messgr.and("id", messwert.getMessgroesseId());
                List<Messgroesse> messgroesse = repository.filterPlain(builder_messgr.getQuery());
                violation.addWarning("mehId#"+messgroesse.get(0).getMessgroesse(), StatusCodes.VAL_UNIT_NORMALIZE);
            } else if ( (mehId != null && mehId.equals(messwert.getMehId())) || (secMehId != null && secMehId.equals(messwert.getMehId())) ) {
                return null;
            } else {
                QueryBuilder<Messgroesse> builder_messgr = repository.queryBuilder(Messgroesse.class);
                builder_messgr.and("id", messwert.getMessgroesseId());
                List<Messgroesse> messgroesse = repository.filterPlain(builder_messgr.getQuery());
                violation.addWarning("mehId#"+messgroesse.get(0).getMessgroesse(), StatusCodes.VAL_UNIT_UMW);
            }
        }
        return violation;
    }
}
