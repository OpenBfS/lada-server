/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import jakarta.persistence.metamodel.EntityType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static de.intevation.lada.exporter.QueryExportJob.ID_TYPE_MEASM;
import static de.intevation.lada.exporter.QueryExportJob.ID_TYPE_SAMPLE;
import static de.intevation.lada.exporter.QueryExportJob.ID_TYPE_TO_SUBDATA_KEY;
import static de.intevation.lada.exporter.QueryExportJob.SUBDATA_MEASM_STATUS_MP;
import static de.intevation.lada.exporter.QueryExportJob.SUBDATA_MEASVAL_UNIT;

import java.util.List;

import de.intevation.lada.data.requests.QueryExportParameters;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.model.lada.Measm_;


/**
 * Checks if the given "sub-data" columns in an export request are valid.
 */
public class ValidSubDataColumnsValidator
    implements ConstraintValidator<ValidSubDataColumns, QueryExportParameters> {
    @Override
    public boolean isValid(
        QueryExportParameters params, ConstraintValidatorContext ctx
    ) {
        if (params == null
            || params.getIdField() == null
            || !ID_TYPE_TO_SUBDATA_KEY.keySet().contains(params.getIdField())
            || params.getSubDataColumns() == null
        ) {
            return true;
        }

        List<String> nonColumnNames;
        EntityType<?> subDataType;
        switch (params.getIdField()) {
        case ID_TYPE_SAMPLE:
            nonColumnNames = List.of(SUBDATA_MEASM_STATUS_MP);
            subDataType = Measm_.class_;
            break;
        case ID_TYPE_MEASM:
            nonColumnNames = List.of(SUBDATA_MEASVAL_UNIT);
            subDataType = MeasVal_.class_;
            break;
        default:
            // Should not happen
            throw new RuntimeException("Unimplemented ID type");
        }
        try {
            for (String subDataCol : params.getSubDataColumns()) {
                if (nonColumnNames.contains(subDataCol)) {
                    continue;
                }
                subDataType.getAttribute(subDataCol);
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
