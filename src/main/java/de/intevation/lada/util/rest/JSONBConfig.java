/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.rest;

import java.nio.charset.Charset;

import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;


/**
 * JSON binding configuration for REST services.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JSONBConfig implements ContextResolver<Jsonb> {

    public static final String DATE_FORMAT =
        "yyyy'-'MM'-'dd'T'HH':'mm[':'ss['.'SSS]]XXX";

    @Override
    public Jsonb getContext(Class<?> type) {
        // Return regardless of type in order to use the same config for
        // de-/serializing any object
        return JsonbBuilder.create(new JsonbConfig()
            .withNullValues(true)

            // The API-doc says "Custom date format as specified in
            // DateTimeFormatter", but at least with Yasson the special
            // JsonbDateFormat.TIME_IN_MILLIS can be used here, too.
            .withDateFormat(DATE_FORMAT, null)

            .withAdapters(
                // De-/serialize Charset from/to String
                new JsonbAdapter<Charset, String>() {
                    @Override
                    public Charset adaptFromJson(
                        String serialized
                    ) throws Exception {
                        return Charset.forName(serialized);
                    }

                    @Override
                    public String adaptToJson(
                        Charset charset
                    ) throws Exception {
                        return charset.name();
                    }
                })
        );
    }
}
