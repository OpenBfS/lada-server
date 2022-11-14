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
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;

/**
 * Class to identify a probe object.
 */
@IdentifierConfig(type = "Probe")
public class ProbeIdentifier implements Identifier {

    @Inject
    private Repository repository;

    private Probe found;

    @Override
    public Identified find(Object object)
    throws InvalidTargetObjectTypeException {
        found = null;
        if (!(object instanceof Probe)) {
            throw new InvalidTargetObjectTypeException(
                "Object is not of type Probe");
        }
        Probe probe = (Probe) object;
        QueryBuilder<Probe> builder = repository.queryBuilder(Probe.class);

        // sampleExtId null and mainSampleId not null and mstId not null.
        if (probe.getSampleExtId() == null
            && probe.getMainSampleId() != null
            && probe.getMeasFacilId() != null
        ) {
            builder.and("measFacilId", probe.getMeasFacilId());
            builder.and("mainSampleId", probe.getMainSampleId());
            List<Probe> proben =
                repository.filterPlain(builder.getQuery());
            if (proben.size() > 1) {
                // Should never happen. DB has unique constraint for
                // "mainSampleId"
                return Identified.REJECT;
            }
            if (proben.isEmpty()) {
                return Identified.NEW;
            }
            found = proben.get(0);
            return Identified.UPDATE;
        } else if (probe.getSampleExtId() != null
            && (probe.getMainSampleId() == null
                || probe.getMeasFacilId() == null)
        ) {
            builder.and("sampleExtId", probe.getSampleExtId());
            List<Probe> proben =
                repository.filterPlain(builder.getQuery());
            if (proben.size() > 1) {
                // Should never happen. DB has unique constraint for
                // "sampleExtId"
                return Identified.REJECT;
            }
            if (proben.isEmpty()) {
                return Identified.NEW;
            }
            found = proben.get(0);
            return Identified.UPDATE;
        } else {
            builder.and("sampleExtId", probe.getSampleExtId());
            List<Probe> proben =
                repository.filterPlain(builder.getQuery());
            if (proben.size() > 1) {
                // Should never happen. DB has unique constraint for
                // "sampleExtId"
                return Identified.REJECT;
            }
            if (proben.isEmpty()) {
                return Identified.NEW;
            }
            if (proben.get(0).getMainSampleId() == null
                || proben.get(0).getMainSampleId().equals(
                    probe.getMainSampleId())
                || probe.getMainSampleId().isEmpty()
                || proben.get(0).getMainSampleId().isEmpty()
            ) {
                found = proben.get(0);
                return Identified.UPDATE;
            } else {
                return Identified.REJECT;
            }
        }
    }

    /**
     * @return the found probe
     */
    public Object getExisting() {
        return found;
    }
}
