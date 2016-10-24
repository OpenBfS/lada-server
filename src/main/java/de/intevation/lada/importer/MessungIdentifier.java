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
import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.RepositoryType;

@IdentifierConfig(type="Messung")
public class MessungIdentifier implements Identifier {

    @Inject
    @RepositoryConfig(type=RepositoryType.RO)
    private Repository repository;

    @Override
    public Identified find(Object object)
    throws InvalidTargetObjectTypeException
    {
        if (!(object instanceof Messung)) {
            throw new InvalidTargetObjectTypeException(
                "Object is not of type Messung");
        }
        Messung messung = (Messung)object;
        QueryBuilder<Messung> builder = new QueryBuilder<Messung>(
            repository.entityManager("land"),
            Messung.class
        );

        // idAlt null and hauptprobenNr not null and mstId not null.
        if (messung.getIdAlt() == null &&
            messung.getNebenprobenNr() != null
        ) {
            builder.and("probeId", messung.getProbeId());
            builder.and("nebenprobenNr", messung.getNebenprobenNr());
            List<Messung> messungen =
                repository.filterPlain(builder.getQuery(), "land");
            if (messungen.size() > 1) {
                // Should never happen. DB has unique constraint for
                // "nebenprobenNr"
                return Identified.REJECT;
            }
            if (messungen.isEmpty()) {
                return Identified.NEW;
            }
            return Identified.UPDATE;
        }
        else if (messung.getIdAlt() != null &&
            messung.getNebenprobenNr() == null
        ) {
            builder.and("probeId", messung.getProbeId());
            builder.and("idAlt", messung.getIdAlt());
            List<Messung> messungen =
                repository.filterPlain(builder.getQuery(), "land");
            if (messungen.size() > 1) {
                // Should never happen. DB has unique constraint for "idAlt"
                return Identified.REJECT;
            }
            if (messungen.isEmpty()) {
                return Identified.NEW;
            }
            return Identified.UPDATE;
        }
        else {
            builder.and("probeId", messung.getProbeId());
            builder.and("idAlt", messung.getIdAlt());
            List<Messung> messungen =
                repository.filterPlain(builder.getQuery(), "land");
            if (messungen.size() > 1) {
                // Should never happen. DB has unique constraint for "idAlt"
                return Identified.REJECT;
            }
            if (messungen.isEmpty()) {
                return Identified.NEW;
            }
            if (messungen.get(0).getNebenprobenNr().equals(
                    messung.getNebenprobenNr()) ||
                messung.getNebenprobenNr().isEmpty() ||
                messungen.get(0).getNebenprobenNr().isEmpty()
            ) {
                return Identified.UPDATE;
            }
            else {
                return Identified.REJECT;
            }
        }
    }
}