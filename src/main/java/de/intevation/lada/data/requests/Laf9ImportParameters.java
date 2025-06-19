/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data.requests;

import java.util.List;
import java.util.Map;

import de.intevation.lada.model.lada.Sample;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;


public class Laf9ImportParameters extends LafImportParameters<List<Sample>> {
    // Shadow field in order to add container element validation constraints
    @NotEmpty
    private Map<@NotBlank String, @NotNull List<@NotNull Sample>> files;

    @Override
    public Map<String, List<Sample>> getFiles() {
        return this.files;
    }

    @Override
    public void setFiles(Map<String, List<Sample>> files) {
        this.files = files;
    }
}
