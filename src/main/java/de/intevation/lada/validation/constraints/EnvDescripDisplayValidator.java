/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import java.util.List;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import de.intevation.lada.model.master.EnvDescrip;
import de.intevation.lada.model.master.EnvDescrip_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;


/**
 * Validates if the given string contains valid parts.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class EnvDescripDisplayValidator
    implements ConstraintValidator<EnvDescripDisplay, String> {

    private String message;

    @Override
    public void initialize(EnvDescripDisplay constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    @Transactional
    public boolean isValid(
        String envDescripDisplay, ConstraintValidatorContext ctx
    ) {
        if (envDescripDisplay == null) {
            return true;
        }

        String[] mediaDesk = envDescripDisplay.split(" ");
        // leave it up to Pattern constraint to ensure a valid string.
        // Just avoid IndexOutOfBoundsException here
        if (mediaDesk.length < 2) {
            return true;
        }

        Repository repository = CDI.current().getBeanContainer()
            .createInstance().select(Repository.class).get();

        boolean zebs = false;
        Integer parent = null;
        Integer hdParent = null;
        Integer ndParent = null;
        if ("01".equals(mediaDesk[1])) {
            zebs = true;
        }
        for (int i = 1; i < mediaDesk.length; i++) {
            if ("00".equals(mediaDesk[i])) {
                continue;
            }
            if (zebs && i < 5) {
                parent = hdParent;
            } else if (!zebs && i < 3) {
                parent = hdParent;
            } else {
                parent = ndParent;
            }
            QueryBuilder<EnvDescrip> builder =
                repository.queryBuilder(EnvDescrip.class);
            if (parent != null) {
                builder.and(EnvDescrip_.predId, parent);
            }
            builder.and(EnvDescrip_.levVal, Integer.parseInt(mediaDesk[i]))
                .and(EnvDescrip_.lev, i - 1);
            List<EnvDescrip> data = repository.filter(builder.getQuery());
            if (data.isEmpty()) {
                return false;
            }
            hdParent = data.get(0).getId();
            if (i == 2) {
                ndParent = data.get(0).getId();
            }
        }
        return true;
    }
}
