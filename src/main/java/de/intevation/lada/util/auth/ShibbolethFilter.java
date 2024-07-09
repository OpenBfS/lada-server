/* Copyright (C) 2015 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.auth;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;

import de.intevation.lada.i18n.I18n;

/** ServletFilter used for Shibboleth authentification. */
@WebFilter({"/rest/*", "/data/*"})
public class ShibbolethFilter implements Filter {

    @Inject
    private Logger logger = Logger.getLogger(ShibbolethFilter.class);

    @Inject
    private I18n i18n;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            throw new ServletException("Unsupported request!");
        }
        if (!(response instanceof HttpServletResponse)) {
            throw new ServletException("Unsupported request!");
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String user = httpRequest.getHeader("X-SHIB-user");
        String roles = httpRequest.getHeader("X-SHIB-roles");

        if (user == null || "".equals(user)) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                i18n.getString("no_valid_user_found"));
        }

        Set<String> rolesValue = extractRoles(roles);
        if (rolesValue == null || rolesValue.isEmpty()) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                i18n.getString("no_valid_role_found"));
        }

        httpRequest.setAttribute("lada.user.roles", rolesValue);
        httpRequest.setAttribute("lada.user.name", user);

        chain.doFilter(request, response);
        return;
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
