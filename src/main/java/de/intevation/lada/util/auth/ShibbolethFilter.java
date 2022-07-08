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

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;

/** ServletFilter used for Shibboleth authentification. */
@WebFilter({"/rest/*", "/data/*"})
public class ShibbolethFilter implements Filter {

    @Inject
    private Logger logger = Logger.getLogger(ShibbolethFilter.class);

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
                httpResponse.reset();
                httpResponse.setStatus(401);
                httpResponse.getOutputStream().print(
                    "{\"success\":false,\"message\":\"698\",\"data\":"
                    + "\"No valid user found!\",\"errors\":{},\"warnings\":{},"
                    + "\"readonly\":false,\"totalCount\":0}");
                httpResponse.getOutputStream().flush();
                return;
        }

        Set<String> rolesValue = extractRoles(roles);
        if (rolesValue == null || rolesValue.isEmpty()) {
                httpResponse.reset();
                httpResponse.setStatus(401);
                httpResponse.getOutputStream().print(
                    "{\"success\":false,\"message\":\"698\",\"data\":"
                    + "\"No valid role found!\",\"errors\":{},\"warnings\":{},"
                    + "\"readonly\":false,\"totalCount\":0}");
                httpResponse.getOutputStream().flush();
                return;
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
