/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer;

import java.util.List;

import javax.inject.Inject;
import javax.management.modelmbean.InvalidTargetObjectTypeException;

import de.intevation.lada.model.land.Messung;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;

/**
 * Identifier for messung objects.
 */
@IdentifierConfig(type = "Messung")
public class MessungIdentifier implements Identifier {

    @Inject
    private Repository repository;

    private Messung found;

    @Override
    public Identified find(Object object)
    throws InvalidTargetObjectTypeException {
        found = null;
        if (!(object instanceof Messung)) {
            throw new InvalidTargetObjectTypeException(
                "Object is not of type Messung");
        }
        Messung messung = (Messung) object;
        QueryBuilder<Messung> builder = repository.queryBuilder(Messung.class);

        // extermeMessungsId null and hauptprobenNr not null and mstId not null.
        if (messung.getExterneMessungsId() == null
            && messung.getNebenprobenNr() != null
        ) {
            builder.and("probeId", messung.getProbeId());
            builder.and("nebenprobenNr", messung.getNebenprobenNr());
            List<Messung> messungen =
                repository.filterPlain(builder.getQuery());
            if (messungen.size() > 1) {
                // Should never happen. DB has unique constraint for
                // "nebenprobenNr"
                return Identified.REJECT;
            }
            if (messungen.isEmpty()) {
                builder = builder.getEmptyBuilder();
                builder.and("probeId", messung.getProbeId());
                builder.and("mmtId", messung.getMmtId());
                messungen =
                    repository.filterPlain(builder.getQuery());
                if (messungen.isEmpty()) {
                    return Identified.NEW;
                }
                if (messungen.size() > 1) {
                    return Identified.NEW;
                }
                if (messungen.get(0).getNebenprobenNr() == null) {
                    found = messungen.get(0);
                    return Identified.UPDATE;
                } else {
                    return Identified.NEW;
                }
            }
            found = messungen.get(0);
            return Identified.UPDATE;
        } else if (messung.getExterneMessungsId() != null) {
            builder.and("probeId", messung.getProbeId());
            builder.and("externeMessungsId", messung.getExterneMessungsId());
            List<Messung> messungen =
                repository.filterPlain(builder.getQuery());
            if (messungen.size() > 1) {
                // Should never happen. DB has unique constraint for
                // "externeMessungsId"
                return Identified.REJECT;
            }
            if (messungen.isEmpty()) {
                return Identified.NEW;
            }
            found = messungen.get(0);
            return Identified.UPDATE;
        } else if (messung.getMmtId() != null) {
            builder.and("probeId", messung.getProbeId());
            builder.and("mmtId", messung.getMmtId());
            List<Messung> messungen =
                repository.filterPlain(builder.getQuery());
            if (messungen.isEmpty()) {
                return Identified.NEW;
            }
            if (messungen.size() > 1) {
                return Identified.NEW;
            }
            found = messungen.get(0);
            return Identified.UPDATE;
        } else {
            return Identified.REJECT;
        }
    }

    @Override
    public Object getExisting() {
        return found;
    }
}
