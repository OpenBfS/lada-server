/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.rest;

import java.lang.reflect.Type;
import java.sql.Timestamp;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * JSON-B deserializer for java.sql.Timestamp.
 */
public class TimestampDeserializer implements JsonbDeserializer <Timestamp> {
    @Override
    public Timestamp deserialize(JsonParser parser,
            DeserializationContext ctx, Type rtType) {
        while (parser.hasNext()) {
            Event event = parser.next();
            if (event == JsonParser.Event.VALUE_NULL) {
                return null;
            }
            if (event == JsonParser.Event.VALUE_NUMBER) {
                return new Timestamp(parser.getLong());
            }
            if (event == JsonParser.Event.VALUE_STRING) {
                long time = Long.parseLong(parser.getString());
                return new Timestamp(time);
            }
        }
        return null;
    }
}
