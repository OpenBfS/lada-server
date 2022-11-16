/* Copyright (C) 2019 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.data;

import java.util.List;

import javax.inject.Inject;

import de.intevation.lada.model.land.Messwert;
import de.intevation.lada.model.stammdaten.MassEinheitUmrechnung;
import de.intevation.lada.model.stammdaten.Umwelt;


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
    private List<MassEinheitUmrechnung> getConversions(
        Integer mehIdTo,
        Integer mehIdFrom
    ) {
        QueryBuilder<MassEinheitUmrechnung> builder =
            repository.queryBuilder(MassEinheitUmrechnung.class);
        builder.and("toUnitId", mehIdTo);
        builder.and("fromUnit", mehIdFrom);
        return repository.filterPlain(builder.getQuery());
    }

    /**
     * Converts the given messwert list into the standard unit of the
     * given UmweltId.
     * @param messwerte Messwerte to convert
     * @param umwId UmweltId to get the standard unit from
     * @return List<Messwert> with converted units.
     */
    public List<Messwert> normalizeMesswerte(
        List<Messwert> messwerte,
        String umwId
    ) {
        if (umwId == null || umwId.equals("")) {
            return messwerte;
        }
        Umwelt umwelt =
            repository.getByIdPlain(Umwelt.class, umwId);
        Integer mehIdToConvertTo = umwelt.getMehId();
        Integer secMehIdToConvertTo = umwelt.getSecMehId();

        for (Messwert messwert: messwerte) {
            if (mehIdToConvertTo != null
                && mehIdToConvertTo.equals(messwert.getMehId())
                || secMehIdToConvertTo != null
                && secMehIdToConvertTo.equals(messwert.getMehId())
            ) {
                // no conversion needed
                continue;
            }
            //Get the conversion factors
            List<MassEinheitUmrechnung> primaryMeu = getConversions(
                    mehIdToConvertTo, messwert.getMehId());
            List<MassEinheitUmrechnung> secondaryMeu = getConversions(
                    secMehIdToConvertTo, messwert.getMehId());
            if (primaryMeu.size() == 0 && secondaryMeu.size() == 0) {
                //No suitable conversion found: continue
                continue;
            }
            MassEinheitUmrechnung meu = primaryMeu.size() > 0
                    ? primaryMeu.get(0) : secondaryMeu.get(0);
            Double factor = meu.getFactor();

            //Update einheit
            messwert.setMehId(
                primaryMeu.size() > 0 ? mehIdToConvertTo : secMehIdToConvertTo);
            //Update messwert
            if (messwert.getMesswert() != null) {
                messwert.setMesswert(messwert.getMesswert() * factor);
            }
            //update nwgZuMesswert
            if (messwert.getNwgZuMesswert() != null) {
                messwert.setNwgZuMesswert(messwert.getNwgZuMesswert() * factor);
            }
        }
        return messwerte;
    }
}
