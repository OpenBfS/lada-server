/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.rest;

import java.sql.Timestamp;

import javax.json.bind.serializer.SerializationContext;
import javax.json.bind.serializer.JsonbSerializer;
import javax.json.stream.JsonGenerator;


/**
 * JSON-B serializer for java.sql.Timestamp.
 */
public class TimestampSerializer implements JsonbSerializer<Timestamp> {
    @Override
    public void serialize(
        Timestamp time,
        JsonGenerator generator,
        SerializationContext ctx
    ) {
        generator.write(time.getTime());
    }
}
