/* Copyright (C) 2019 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.data;

import java.util.List;

import jakarta.inject.Inject;

import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.master.EnvMedium;
import de.intevation.lada.model.master.UnitConvers;


public class MesswertNormalizer {

    private final Repository repository;

    @Inject
    private MesswertNormalizer(Repository repository) {
        this.repository = repository;
    }

    /**
     * Get the list of conversion for the given meh ids.
     * @param mehIdTo MehId to convert to
     * @param mehIdFrom MehId to convert from
     * @return Conversions as list
     */
    private List<UnitConvers> getConversions(
        Integer mehIdTo,
        Integer mehIdFrom
    ) {
        QueryBuilder<UnitConvers> builder = repository
            .queryBuilder(UnitConvers.class)
            .and("toUnitId", mehIdTo)
            .and("fromUnitId", mehIdFrom);
        return repository.filter(builder.getQuery());
    }

    /**
     * Converts the given messwert list into the standard unit of the
     * given UmweltId.
     * @param messwerte Messwerte to convert
     * @param umwId UmweltId to get the standard unit from
     * @return List<Messwert> with converted units.
     */
    public List<MeasVal> normalizeMesswerte(
        List<MeasVal> messwerte,
        String umwId
    ) {
        if (umwId == null || umwId.equals("")) {
            return messwerte;
        }
        EnvMedium umwelt =
            repository.getById(EnvMedium.class, umwId);
        Integer mehIdToConvertTo = umwelt.getUnit1();
        Integer secMehIdToConvertTo = umwelt.getUnit2();

        for (MeasVal messwert: messwerte) {
            if (mehIdToConvertTo != null
                && mehIdToConvertTo.equals(messwert.getMeasUnitId())
                || secMehIdToConvertTo != null
                && secMehIdToConvertTo.equals(messwert.getMeasUnitId())
            ) {
                // no conversion needed
                continue;
            }
            //Get the conversion factors
            List<UnitConvers> primaryMeu = getConversions(
                    mehIdToConvertTo, messwert.getMeasUnitId());
            List<UnitConvers> secondaryMeu = getConversions(
                    secMehIdToConvertTo, messwert.getMeasUnitId());
            if (primaryMeu.size() == 0 && secondaryMeu.size() == 0) {
                //No suitable conversion found: continue
                continue;
            }
            UnitConvers meu = primaryMeu.size() > 0
                    ? primaryMeu.get(0) : secondaryMeu.get(0);
            Double factor = meu.getFactor();

            //Update einheit
            messwert.setMeasUnitId(
                primaryMeu.size() > 0 ? mehIdToConvertTo : secMehIdToConvertTo);
            //Update messwert
            if (messwert.getMeasVal() != null) {
                messwert.setMeasVal(messwert.getMeasVal() * factor);
            }
            //update nwgZuMesswert
            if (messwert.getDetectLim() != null) {
                messwert.setDetectLim(messwert.getDetectLim() * factor);
            }
        }
        return messwerte;
    }
}
