/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;

import de.intevation.lada.i18n.I18n;
import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.BelongsToMeasm;
import de.intevation.lada.model.lada.BelongsToMpg;
import de.intevation.lada.model.lada.BelongsToSample;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.lada.TagLinkMeasm;
import de.intevation.lada.model.lada.TagLinkSample;
import de.intevation.lada.model.master.BelongsToNetwork;
import de.intevation.lada.model.master.Sampler;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


/**
 * Authorize a user via HttpServletRequest attributes.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@RequestScoped
@AuthorizationConfig(type = AuthorizationType.HEADER)
public class HeaderAuthorization implements Authorization {

    private UserInfo userInfo;

    private I18n i18n;

    private Authorizer<Sample> probeAuthorizer;
    private Authorizer<Measm> messungAuthorizer;
    private Authorizer<Mpg> messprogrammAuthorizer;
    private Authorizer<Tag> tagAuthorizer;
    private Authorizer<TagLinkSample> tagZuordnungAuthorizer;
    private Authorizer<TagLinkMeasm> tagLinkMeasmAuthorizer;
    private Authorizer<Site> siteAuthorizer;
    private Authorizer<Sampler> samplerAuthorizer;
    private Authorizer<StatusProt> statusAuthorizer;
    private Authorizer<BelongsToSample> pIdAuthorizer;
    private Authorizer<BelongsToMeasm> mIdAuthorizer;
    private Authorizer<BelongsToMpg> mpgIdAuthorizer;
    private Authorizer<BelongsToNetwork> netzAuthorizer;

    @Inject
    private Instance<Authorizer<?>> authorizer;

    /**
     * Sets information about requesting user and initializes authorizers.
     */
    @Inject
    public HeaderAuthorization(
        UserInfo userInfo,
        I18n i18n,
        Repository repository
    ) {
        this.userInfo = userInfo;
        this.i18n = i18n;

        this.probeAuthorizer =
            new ProbeAuthorizer(userInfo, repository);
        this.messungAuthorizer =
            new MessungAuthorizer(userInfo, repository);
        this.messprogrammAuthorizer =
            new MessprogrammAuthorizer(userInfo, repository);
        this.tagAuthorizer =
            new TagAuthorizer(userInfo, repository);
        this.tagZuordnungAuthorizer =
            new TagLinkSampleAuthorizer(userInfo, repository);
        this.tagLinkMeasmAuthorizer =
            new TagLinkMeasmAuthorizer(userInfo, repository);
        this.siteAuthorizer =
            new SiteAuthorizer(userInfo, repository);
        this.samplerAuthorizer =
            new SamplerAuthorizer(userInfo, repository);
        this.statusAuthorizer =
            new StatusProtAuthorizer(userInfo, repository);
        this.pIdAuthorizer =
            new ProbeIdAuthorizer(userInfo, repository);
        this.mIdAuthorizer =
            new MessungIdAuthorizer(userInfo, repository);
        this.mpgIdAuthorizer =
            new MpgIdAuthorizer(userInfo, repository);
        this.netzAuthorizer =
            new NetzbetreiberAuthorizer(userInfo, repository);
    }

    /**
     * Request user informations.
     *
     * @return The UserInfo object containing username and groups.
     */
    @Override
    public UserInfo getInfo() {
        return this.userInfo;
    }

    /**
     * Filter a list of data objects using the user informations contained in
     * the HttpServletRequest.
     *
     * @param data      The Response object containing the data.
     * @return The Response object containing the filtered data.
     */
    @Override
    public <T extends BaseModel> T filter(T data) {
        try {
            return doAuthorize(data, null);
        } catch (AuthorizationException ae) {
            // Should not happen
            throw new RuntimeException(
                "Error during setting of authorization hints", ae);
        }
    }

    /**
     * Check whether a user is authorized to operate on the given data.
     *
     * @param data      The data to test.
     * @param method    The Http request type.
     * @throws ForbiddenException if the user is not authorized.
     */
    @Override
    public <T extends BaseModel> T authorize(
        T data,
        RequestMethod method
    ) {
        try {
            return doAuthorize(data, method);
        } catch (AuthorizationException ae) {
            throw new ForbiddenException(
                Response.status(Response.Status.FORBIDDEN)
                .entity(Json.createValue(i18n.getString(ae.getMessage())))
                    .build());
        }
     }

    /**
     * Check whether a user is authorized to operate on the given data.
     *
     * @param data      The data to test.
     * @param method    The Http request type.
     * @return True if the user is authorized else returns false.
     */
    @Override
    public <T extends BaseModel> boolean isAuthorized(
        T data,
        RequestMethod method
    ) {
        try {
            doAuthorize(data, method);
            return true;
        } catch (AuthorizationException ae) {
            return false;
        }
    }

    private <T extends BaseModel> T doAuthorize(
        T data,
        RequestMethod method
    ) throws AuthorizationException {
        if (data instanceof Sample o) {
            if (method == null) {
                this.probeAuthorizer.setAuthAttrs(o);
            } else {
                this.probeAuthorizer.authorize(o, method);
            }
        } else if (data instanceof Measm o) {
            if (method == null) {
                this.messungAuthorizer.setAuthAttrs(o);
            } else {
                this.messungAuthorizer.authorize(o, method);
            }
        } else if (data instanceof Mpg o) {
            if (method == null) {
                this.messprogrammAuthorizer.setAuthAttrs(o);
            } else {
                this.messprogrammAuthorizer.authorize(o, method);
            }
        } else if (data instanceof Tag o) {
            if (method == null) {
                this.tagAuthorizer.setAuthAttrs(o);
            } else {
                this.tagAuthorizer.authorize(o, method);
            }
        } else if (data instanceof TagLinkSample o) {
            if (method == null) {
                this.tagZuordnungAuthorizer.setAuthAttrs(o);
            } else {
                this.tagZuordnungAuthorizer.authorize(o, method);
            }
        } else if (data instanceof TagLinkMeasm o) {
            if (method == null) {
                this.tagLinkMeasmAuthorizer.setAuthAttrs(o);
            } else {
                this.tagLinkMeasmAuthorizer.authorize(o, method);
            }
        } else if (data instanceof Site o) {
            if (method == null) {
                this.siteAuthorizer.setAuthAttrs(o);
            } else {
                this.siteAuthorizer.authorize(o, method);
            }
        } else if (data instanceof Sampler o) {
            if (method == null) {
                this.samplerAuthorizer.setAuthAttrs(o);
            } else {
                this.samplerAuthorizer.authorize(o, method);
            }
        } else if (data instanceof StatusProt o) {
            if (method == null) {
                this.statusAuthorizer.setAuthAttrs(o);
            } else {
                this.statusAuthorizer.authorize(o, method);
            }
        } else if (data instanceof BelongsToSample o) {
            if (method == null) {
                this.pIdAuthorizer.setAuthAttrs(o);
            } else {
                this.pIdAuthorizer.authorize(o, method);
            }
        } else if (data instanceof BelongsToMeasm o) {
            if (method == null) {
                this.mIdAuthorizer.setAuthAttrs(o);
            } else {
                this.mIdAuthorizer.authorize(o, method);
            }
        } else if (data instanceof BelongsToMpg o) {
            if (method == null) {
                this.mpgIdAuthorizer.setAuthAttrs(o);
            } else {
                this.mpgIdAuthorizer.authorize(o, method);
            }
        } else if (data instanceof BelongsToNetwork o) {
            if (method == null) {
                this.netzAuthorizer.setAuthAttrs(o);
            } else {
                this.netzAuthorizer.authorize(o, method);
            }
        } else {
            throw new IllegalArgumentException(String.format(
                    "%s not authorizable", data.getClass()));
        }
        return data;
    }
}
