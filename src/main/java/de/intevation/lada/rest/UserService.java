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

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.auth.UserInfo;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.Response;

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
@Path("rest/user")
public class UserService extends LadaService {

    @Inject
    private Logger logger = Logger.getLogger(UserService.class);
    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    private Authorization authorization;

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
     * @return Response object containing login data.
     */
    @GET
    @Path("/")
    public Response get() {
        UserInfo userInfo = authorization.getInfo();
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("username", userInfo.getName());
        response.put("roles", userInfo.getAuth().stream()
                .map(a -> a.getLdapGroup()).collect(Collectors.toSet()));
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
            userInfo.getName() + " - " +
            userInfo.getAuth().stream().map(a -> a.getLdapGroup()).collect(Collectors.toSet())
        );
        return new Response(true, StatusCodes.OK, response);
    }
}
