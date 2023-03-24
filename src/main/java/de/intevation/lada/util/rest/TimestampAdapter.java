/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.rest;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.json.bind.adapter.JsonbAdapter;
import javax.ws.rs.ext.Provider;

@Provider
public class TimestampAdapter implements JsonbAdapter<Timestamp, String> {

    @Override
    public String adaptToJson(Timestamp obj) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter
                    .ofPattern(JSONBConfig.DATE_FORMAT)
                    .withZone(ZoneId.of("UTC"));
        return formatter.format(obj.toInstant());
    }

    @Override
    public Timestamp adaptFromJson(String obj) throws Exception {
        ZonedDateTime time = ZonedDateTime.of(
            LocalDateTime.parse(
                obj, DateTimeFormatter.ofPattern(JSONBConfig.DATE_FORMAT)),
            ZoneId.of("UTC")
        );
        return new Timestamp(time.toInstant().toEpochMilli());
    }
}
