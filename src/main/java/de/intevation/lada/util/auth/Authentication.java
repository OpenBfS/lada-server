/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.auth;

import static jakarta.security.enterprise.AuthenticationStatus.SEND_FAILURE;
import static jakarta.security.enterprise.AuthenticationStatus.SUCCESS;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import de.intevation.lada.i18n.I18n;

/**
 * Authenticate HTTP requests based on username and roles provided
 * in HTTP headers.
 */
public class Authentication implements HttpAuthenticationMechanism {

    private static final String HEADER_X_SHIB_USER = "X-SHIB-user";
    private static final String HEADER_X_SHIB_ROLES = "X-SHIB-roles";

    static final String ROLES = "lada.user.roles";
    static final String USER = "lada.user.name";

    @Inject
    private I18n i18n;

    @Override
    public AuthenticationStatus validateRequest(
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse,
        HttpMessageContext ctx
    ) throws AuthenticationException {
        String user = httpRequest.getHeader(HEADER_X_SHIB_USER);
        String roles = httpRequest.getHeader(HEADER_X_SHIB_ROLES);

        Set<String> rolesValue;
        try {
            if (user == null || "".equals(user)) {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    i18n.getString("no_valid_user_found"));
                return SEND_FAILURE;
            }

            rolesValue = extractRoles(roles);
            if (rolesValue.isEmpty()) {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    i18n.getString("no_valid_role_found"));
                return SEND_FAILURE;
            }
        } catch (IOException e) {
            throw new AuthenticationException(e);
        }

        httpRequest.setAttribute(ROLES, rolesValue);
        httpRequest.setAttribute(USER, user);

        return SUCCESS;
    }

    private Set<String> extractRoles(String roles) {
        Set<String> groups = new HashSet<>();
        if (roles == null || "".equals(roles) || "(null)".equals(roles)) {
            return groups;
        } else {
            String[] groupStrings = roles.split(";");
            for (int i = 0; i < groupStrings.length; i++) {
                String[] items = groupStrings[i].trim().split(",");
                for (int j = 0; j < items.length; j++) {
                    groups.add(items[j].replace("cn=", "").trim());
                }
            }
            return groups;
        }
    }
}
