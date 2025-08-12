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
import jakarta.persistence.NoResultException;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.master.EnvMedium;
import de.intevation.lada.model.master.UnitConvers;
import de.intevation.lada.model.master.UnitConvers_;


public class MesswertNormalizer {

    private final Repository repository;

    @Inject
    private MesswertNormalizer(Repository repository) {
        this.repository = repository;
    }

    /**
     * Get conversion for the given MeasUnits.
     * @param mehIdTo MeasUnit to convert to
     * @param mehIdFrom MeasUnit to convert from
     * @return Conversion
     * @throws NoResultException if no such conversion exists
     */
    private UnitConvers getConversion(
        Integer mehIdTo,
        Integer mehIdFrom
    ) throws NoResultException {
        QueryBuilder<UnitConvers> builder = repository
            .queryBuilder(UnitConvers.class)
            .and(UnitConvers_.toUnitId, mehIdTo)
            .and(UnitConvers_.fromUnitId, mehIdFrom);
        return repository.getSingle(builder.getQuery());
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

            //Get the conversion factor
            UnitConvers meu;
            try {
                meu = getConversion(mehIdToConvertTo, messwert.getMeasUnitId());
            } catch (NoResultException e) {
                try {
                    meu = getConversion(
                        secMehIdToConvertTo, messwert.getMeasUnitId());
                } catch (NoResultException e2) {
                    //No suitable conversion found: continue
                    continue;
                }
            }
            Double factor = meu.getFactor();

            //Update einheit
            messwert.setMeasUnitId(meu.getToUnitId());
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
