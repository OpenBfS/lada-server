/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.ort;

import javax.inject.Inject;

import de.intevation.lada.model.stammdaten.Ort;
import de.intevation.lada.model.stammdaten.Kta;
//import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

@ValidationRule("Ort")
public class validREIMesspunkt implements Rule {

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        Ort ort = (Ort) object;
        String kta = null;

        //check ortType
        if (ort.getOrtTyp()!=3){
            return null;
        }

        Violation violation = new Violation();
        //getkta
        if (ort.getKtaGruppeId()!= null){
            kta = repository.getByIdPlain(Kta.class, ort.getKtaGruppeId()).getCode();
        } else {
            violation.addWarning("kta#kta_missing", StatusCodes.VALUE_MISSING);
        }

        //check ortId
        if ( ort.getOrtId() == null) {
            violation.addWarning("ortId", StatusCodes.VALUE_MISSING);
        } else {
            String mpId = ort.getOrtId();
            if (mpId.length()>12){
                violation.addWarning("ortId#tooLong", StatusCodes.VALUE_OUTSIDE_RANGE);
            }
            if (kta!=null && !mpId.substring(0, 3).equals(kta) ) {
                violation.addWarning("kta#kta_notMatching", StatusCodes.VALUE_NOT_MATCHING);
            }
            if (mpId.substring(0, 4).equals(kta) && mpId.length() == 4) {
                violation.addWarning("ortId#MP_missing", StatusCodes.VALUE_OUTSIDE_RANGE);
            }
            if (mpId.substring(0, 4).equals(kta) && mpId.substring(4, mpId.length()).length() > 4) {
                violation.addNotification("ortId#MP_tooLong", StatusCodes.VALUE_OUTSIDE_RANGE);
            }
        }

    return violation;
    }
}
