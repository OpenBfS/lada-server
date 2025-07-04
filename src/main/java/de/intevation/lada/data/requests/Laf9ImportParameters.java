/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data.requests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import jakarta.json.JsonObject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import de.intevation.lada.model.lada.Sample;


/**
 * A LAF9 import file is expected to contain a JSON represented list of
 * {@link Sample} objects. In order to be able to distinguish between
 * attributes not present in the input (to be left untouched in an update)
 * and {@code null}-valued attributes (to be set to {@code null} in an update),
 * input files are deserialized into a {@link Collection} of
 * {@link JsonObject}s.
 */
public class Laf9ImportParameters
    extends LafImportParameters<Collection<JsonObject>> {

    // Just here to provide a class literal for the Schema annotation
    private static class SampleCollection extends ArrayList<Sample> {
        private static final long serialVersionUID = 1L;
    }

    // Shadow field in order to add container element validation constraints
    @Schema(additionalProperties = SampleCollection.class)
    @NotEmpty
    private Map<@NotBlank String, @NotNull Collection<@NotNull JsonObject>> files;

    @Override
    public Map<String, Collection<JsonObject>> getFiles() {
        return this.files;
    }

    @Override
    public void setFiles(Map<String, Collection<JsonObject>> files) {
        this.files = files;
    }
}
