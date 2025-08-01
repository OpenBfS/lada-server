/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.laf;

import java.util.ArrayList;
import java.util.List;

import de.intevation.lada.importer.Report;
import de.intevation.lada.model.lada.Sample;


/**
 * Container for result of importing a JSON file.
 */
public class Laf9Report extends Report {

    private List<Sample> samples = new ArrayList<>();

    @Override
    public boolean isSuccess() {
        return this.samples.stream().noneMatch(Sample::hasErrorsWithChilds);
    }

    public List<Sample> getSamples() {
        return this.samples;
    }
}
