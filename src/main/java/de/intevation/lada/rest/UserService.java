/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import de.intevation.lada.util.auth.UserInfo;

import org.jboss.logging.Logger;

/**
 * REST service to get login data for the Lada application.
 * <p>
 * This service produces data in the application/json media type.
 * A typical response holds information about the action performed and the data.
 * <pre>
 * <code>
 * {
 *  "success": [boolean],
 *  "message": [string],
 *  "data":{
 *      "username": [string],
 *      "servertime": [timestamp],
 *      "roles": [string]
 *  },
 *  "errors": [object],
 *  "warnings": [object],
 *  "readonly": [boolean],
 *  "totalCount": [number]
 * }
 * </code>
 * </pre>
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
//@Path(LadaService.PATH_REST + "user")
@Path("rest/user")
public class UserService extends LadaService {

    @Inject
    private Logger logger = Logger.getLogger(UserService.class);

    /**
     * Get login data.
     * <pre>
     * <code>
     * {
     *  "success": [boolean],
     *  "message": [string],
     *  "data": {
     *      "username": [string],
     *      "servertime": [timestamp],
     *      "roles": [string]
     *  },
     *  "errors": [object],
     *  "warnings": [object],
     *  "readonly": [boolean],
     *  "totalCount": [number]
     * }
     * </code>
     * </pre>
     *
     * @return login data.
     */
    @GET
    public Map<String, Object> get() {
        UserInfo userInfo = authorization.getInfo();
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("username", userInfo.getName());
        response.put("roles", userInfo.getAuth().stream()
                .map(a -> a.getLdapGr()).collect(Collectors.toSet()));
        response.put("servertime", new Date().getTime());
        response.put("messstelleLabor", userInfo.getMessLaborId());
        response.put("netzbetreiber", userInfo.getNetzbetreiber());
        response.put("funktionen", userInfo.getFunktionen());
        response.put("netzbetreiberFunktionen", userInfo.getNetzbetreiber()
            .stream().collect(Collectors.toMap(
                    nb -> nb,
                    nb -> userInfo.getFunktionenForNetzbetreiber(nb))));
        response.put("userId", userInfo.getUserId());
        logger.debug(
            userInfo.getName() + " - "
            + userInfo.getAuth().stream().map(
                a -> a.getLdapGr()).collect(Collectors.toSet())
        );
        return response;
    }
}
