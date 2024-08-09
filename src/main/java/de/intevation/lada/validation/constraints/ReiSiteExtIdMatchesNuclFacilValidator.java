/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.master.NuclFacilGrMp;
import de.intevation.lada.model.master.NuclFacilGrMp_;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.Site_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


/**
 * Validates if extId is valid for REI site.
 */
public class ReiSiteExtIdMatchesNuclFacilValidator
    implements ConstraintValidator<ReiSiteExtIdMatchesNuclFacil, Site> {

    private static final int NUCL_FACIL_EXT_ID_LENGTH = 4;

    private String message;

    @Override
    public void initialize(ReiSiteExtIdMatchesNuclFacil constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Transactional
    @Override
    public boolean isValid(Site ort, ConstraintValidatorContext ctx) {
        if (ort == null
            || !Site.SiteClassId.REI.equals(ort.getSiteClassId())
            || ort.getNuclFacilGrId() == null
            // Leave validation of extId up to other rule
            || ort.getExtId() == null
            || ort.getExtId().length() < NUCL_FACIL_EXT_ID_LENGTH
        ) {
            return true;
        }

        // First 4 characters of extId should match a nuclear facility
        // in given group
        Repository repository = CDI.current().getBeanContainer()
            .createInstance().select(Repository.class).get();
        QueryBuilder<NuclFacilGrMp> builder = repository
            .queryBuilder(NuclFacilGrMp.class)
            .and(NuclFacilGrMp_.nuclFacilExtId,
                ort.getExtId().substring(0, NUCL_FACIL_EXT_ID_LENGTH))
            .and(NuclFacilGrMp_.nuclFacilGrId,
                ort.getNuclFacilGrId());
        if (repository.filter(builder.getQuery()).isEmpty()) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(this.message)
                .addPropertyNode(Site_.EXT_ID)
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
