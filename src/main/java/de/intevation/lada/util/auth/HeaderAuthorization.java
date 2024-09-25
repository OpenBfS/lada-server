/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response.Status;

import de.intevation.lada.i18n.I18n;
import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.model.lada.Geolocat;
import de.intevation.lada.model.lada.GeolocatMpg;
import de.intevation.lada.model.lada.MeasVal;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.lada.MpgMmtMp;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.SampleSpecifMeasVal;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.lada.TagLinkMeasm;
import de.intevation.lada.model.lada.TagLinkSample;
import de.intevation.lada.model.master.DatasetCreator;
import de.intevation.lada.model.master.MpgCateg;
import de.intevation.lada.model.master.MunicDiv;
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

    private Map<Class<?>, Authorizer> authorizers;

    private I18n i18n;

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

        ProbeAuthorizer probeAuthorizer =
            new ProbeAuthorizer(repository);
        MessungAuthorizer messungAuthorizer =
            new MessungAuthorizer(repository);
        ProbeIdAuthorizer pIdAuthorizer =
            new ProbeIdAuthorizer(repository);
        MessungIdAuthorizer mIdAuthorizer =
            new MessungIdAuthorizer(repository);
        NetzbetreiberAuthorizer netzAuthorizer =
            new NetzbetreiberAuthorizer(repository);
        MessprogrammAuthorizer messprogrammAuthorizer =
            new MessprogrammAuthorizer(repository);
        MpgIdAuthorizer mpgIdAuthorizer =
            new MpgIdAuthorizer(repository);
        TagAuthorizer tagAuthorizer =
            new TagAuthorizer(repository);
        TagLinkSampleAuthorizer tagZuordnungAuthorizer =
            new TagLinkSampleAuthorizer(repository);
        TagLinkMeasmAuthorizer tagLinkMeasmAuthorizer =
            new TagLinkMeasmAuthorizer(repository);
        SiteAuthorizer siteAuthorizer =
            new SiteAuthorizer(repository);
        SamplerAuthorizer samplerAuthorizer =
            new SamplerAuthorizer(repository);
        StatusProtAuthorizer statusAuthorizer =
            new StatusProtAuthorizer(repository);

        this.authorizers = Map.ofEntries(
            Map.entry(Sample.class, probeAuthorizer),
            Map.entry(Measm.class, messungAuthorizer),
            Map.entry(Geolocat.class, pIdAuthorizer),
            Map.entry(CommSample.class, pIdAuthorizer),
            Map.entry(SampleSpecifMeasVal.class, pIdAuthorizer),
            Map.entry(CommMeasm.class, mIdAuthorizer),
            Map.entry(MeasVal.class, mIdAuthorizer),
            Map.entry(StatusProt.class, statusAuthorizer),
            Map.entry(Sampler.class, samplerAuthorizer),
            Map.entry(DatasetCreator.class, netzAuthorizer),
            Map.entry(MunicDiv.class, netzAuthorizer),
            Map.entry(MpgCateg.class, netzAuthorizer),
            Map.entry(Site.class, siteAuthorizer),
            Map.entry(Mpg.class, messprogrammAuthorizer),
            Map.entry(MpgMmtMp.class, mpgIdAuthorizer),
            Map.entry(GeolocatMpg.class, mpgIdAuthorizer),
            Map.entry(Tag.class, tagAuthorizer),
            Map.entry(TagLinkMeasm.class, tagLinkMeasmAuthorizer),
            Map.entry(TagLinkSample.class, tagZuordnungAuthorizer)
        );
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
        authorizers.get(data.getClass()).setAuthAttrs(data, userInfo);
        return data;
    }

    /**
     * Check whether a user is authorized to operate on the given data.
     *
     * @param data      The data to test.
     * @param method    The Http request type.
     * @param clazz     The data object class.
     * @throws ForbiddenException if the user is not authorized.
     */
    @Override
    public <T> T authorize(
        T data,
        RequestMethod method,
        Class<? extends T> clazz
    ) {
        Authorizer authorizer = authorizers.get(clazz);
        String reason = authorizer.isAuthorizedReason(
            data, method, userInfo);
        if (reason == null) {
            return data;
        }
        throw new ForbiddenException(
            jakarta.ws.rs.core.Response.status(Status.FORBIDDEN)
            .entity(i18n.getString(reason)).build());
     }

    /**
     * Check whether a user is authorized to operate on the given data.
     *
     * @param data      The data to test.
     * @param method    The Http request type.
     * @param clazz     The data object class.
     * @return True if the user is authorized else returns false.
     */
    @Override
    public <T> boolean isAuthorized(
        Object data,
        RequestMethod method,
        Class<T> clazz
    ) {
        Authorizer authorizer = authorizers.get(clazz);
        // Do not authorize anything unknown
        if (authorizer == null || data == null) {
            return false;
        }
        return authorizer.isAuthorized(data, method, userInfo);
    }
}
