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

import de.intevation.lada.model.land.Probe;
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;

@IdentifierConfig(type="Probe")
public class ProbeIdentifier implements Identifier {

    @Inject
    @RepositoryConfig(type=RepositoryType.RO)
    private Repository repository;

    @Override
    public Identified find(Object object)
    throws InvalidTargetObjectTypeException
    {
        if (!(object instanceof Probe)) {
            throw new InvalidTargetObjectTypeException(
                "Object is not of type Probe");
        }
        Probe probe = (Probe)object;
        QueryBuilder<Probe> builder = new QueryBuilder<Probe>(
            repository.entityManager("land"),
            Probe.class
        );

        // idAlt null and hauptprobenNr not null and mstId not null.
        if (probe.getIdAlt() == null &&
            probe.getHauptprobenNr() != null &&
            probe.getMstId() != null
        ) {
            builder.and("mstId", probe.getMstId());
            builder.and("hauptprobenNr", probe.getHauptprobenNr());
            List<Probe> proben = repository.filterPlain(builder.getQuery(), "land");
            if (proben.size() > 1) {
                // Should never happen. DB has unique constraint for
                // "hauptprobenNr"
                return Identified.REJECT;
            }
            if (proben.isEmpty()) {
                return Identified.NEW;
            }
            return Identified.UPDATE;
        }
        else if (probe.getIdAlt() != null &&
            (probe.getHauptprobenNr() == null ||
            probe.getMstId() == null)
        ) {
            builder.and("idAlt", probe.getIdAlt());
            List<Probe> proben =
                repository.filterPlain(builder.getQuery(), "land");
            if (proben.size() > 1) {
                // Should never happen. DB has unique constraint for "idAlt"
                return Identified.REJECT;
            }
            if (proben.isEmpty()) {
                return Identified.NEW;
            }
            return Identified.UPDATE;
        }
        else {
            builder.and("idAlt", probe.getIdAlt());
            List<Probe> proben =
                repository.filterPlain(builder.getQuery(), "land");
            if (proben.size() > 1) {
                // Should never happen. DB has unique constraint for "idAlt"
                return Identified.REJECT;
            }
            if (proben.isEmpty()) {
                return Identified.NEW;
            }
            if (proben.get(0).getHauptprobenNr().equals(
                    probe.getHauptprobenNr()) ||
                probe.getHauptprobenNr().isEmpty() ||
                proben.get(0).getHauptprobenNr().isEmpty()
            ) {
                return Identified.UPDATE;
            }
            else {
                return Identified.REJECT;
            }
        }
    }
}