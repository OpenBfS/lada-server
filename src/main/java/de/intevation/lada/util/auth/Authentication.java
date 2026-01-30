/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.auth;

import static de.intevation.lada.model.master.Names.QUERY_INSERT_USER_NAME;
import static de.intevation.lada.model.master.Names.QUERY_LADA_USER_ID;
import static de.intevation.lada.model.master.Names.QUERY_PARAM_USER_NAME;
import static jakarta.security.enterprise.AuthenticationStatus.SEND_FAILURE;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import de.intevation.lada.i18n.I18n;
import de.intevation.lada.model.master.Auth;
import de.intevation.lada.model.master.Auth_;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;

/**
 * Authenticate HTTP requests based on username and roles provided
 * in HTTP headers.
 */
public class Authentication implements HttpAuthenticationMechanism {

    public static final String HEADER_X_SHIB_USER = "X-SHIB-user";
    public static final String HEADER_X_SHIB_ROLES = "X-SHIB-roles";

    @Inject
    private I18n i18n;

    @Inject
    private Repository repository;

    @Transactional
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

        // Query the user's roles
        QueryBuilder<Auth> authBuilder = repository.queryBuilder(Auth.class)
            .andIn(Auth_.ldapGr, rolesValue);
        List<Auth> auth = repository.filter(authBuilder.getQuery());

        // Query the user's ID or create a new one
        repository.entityManager()
            .createNamedQuery(QUERY_INSERT_USER_NAME)
            .setParameter(QUERY_PARAM_USER_NAME, user)
            .executeUpdate();
        Integer userId = repository.entityManager()
            .createNamedQuery(QUERY_LADA_USER_ID, Integer.class)
            .setParameter(QUERY_PARAM_USER_NAME, user)
            .getSingleResult();

        return ctx.notifyContainerAboutLogin(
            new UserInfo(user, userId, auth), rolesValue);
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
