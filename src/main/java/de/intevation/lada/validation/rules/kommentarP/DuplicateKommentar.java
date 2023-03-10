/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.rules.kommentarP;

import javax.inject.Inject;
import javax.persistence.Query;

import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Violation;
import de.intevation.lada.validation.annotation.ValidationRule;
import de.intevation.lada.validation.rules.Rule;

/**
 * Validation rule for probe.
 * Validates if the probe has an existing kommentarP".
 *
 */
@ValidationRule("KommentarP")
public class DuplicateKommentar implements Rule {

    private static final String EXISTS_QUERY_TEMPLATE =
    "SELECT EXISTS("
    + "SELECT 1 FROM lada.comm_sample "
    + "WHERE lower(replace(text,' ',''))=lower(replace(:%s,' ',''))"
    + " AND %s=:%s)";
    /*TEXT, Probe_id = id */

    @Inject
    private Repository repository;

    @Override
    public Violation execute(Object object) {
        CommSample kommentar = (CommSample) object;
        Violation violation = new Violation();

        if (kommentar.getSampleId() == null) {
            violation.addError("sample_id", StatusCodes.VALUE_MISSING);
            return violation;
        }

        if (isExisting(kommentar)) {
            violation.addWarning("Comment", StatusCodes.VAL_EXISTS);
            return violation;
        }
        return null;
    }

    private Boolean isExisting(CommSample kommentar) {
        // Check if tag is already assigned
        final String textParam = "TEXT",
            probeIdParam = "sample_id";
        String idField = "sample_id";
        Query isAssigned = repository.queryFromString(
            String.format(EXISTS_QUERY_TEMPLATE,
                textParam, idField, probeIdParam));
        isAssigned.setParameter(textParam, kommentar.getText());
        isAssigned.setParameter(probeIdParam, kommentar.getSampleId());
        return (Boolean) isAssigned.getSingleResult();
    }
}

