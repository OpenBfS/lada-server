/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.rest;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;


/**
 * JSON binding configuration for REST services.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JSONBConfig implements ContextResolver<Jsonb> {
    @Override
    public Jsonb getContext(Class type) {
        return JsonbBuilder.create(new JsonbConfig()
            .withNullValues(true)
            .withDeserializers(new TimestampDeserializer())
            .withSerializers(new TimestampSerializer())
        );
    }
}
