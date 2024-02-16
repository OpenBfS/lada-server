/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Context;

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
import de.intevation.lada.model.lada.TagLink;
import de.intevation.lada.model.master.Auth;
import de.intevation.lada.model.master.DatasetCreator;
import de.intevation.lada.model.master.LadaUser;
import de.intevation.lada.model.master.MpgCateg;
import de.intevation.lada.model.master.MunicDiv;
import de.intevation.lada.model.master.Sampler;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

/**
 * Authorize a user via HttpServletRequest attributes.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@RequestScoped
@AuthorizationConfig(type = AuthorizationType.HEADER)
public class HeaderAuthorization implements Authorization {

    private UserInfo userInfo;

    private Map<Class, Authorizer> authorizers;

    /**
     * Injectable constructor to be used in request context.
     */
    @Inject
    public HeaderAuthorization(
        @Context HttpServletRequest request,
        Repository repository
    ) {
        // The username
        String name = request.getAttribute("lada.user.name").toString();

        // The user's roles
        String[] mst = request.getAttribute("lada.user.roles").toString()
            .replace("[", "").replace("]", "").replace(" ", "").split(",");
        QueryBuilder<Auth> authBuilder = repository.queryBuilder(Auth.class);
        authBuilder.andIn("ldapGr", Arrays.asList(mst));
        List<Auth> auth = repository.filterPlain(authBuilder.getQuery());

        // The user's ID
        QueryBuilder<LadaUser> uIdBuilder =
            repository.queryBuilder(LadaUser.class);
        uIdBuilder.and("name", name);
        LadaUser user;
        try {
            user = repository.getSinglePlain(uIdBuilder.getQuery());
        } catch (NoResultException e) {
            LadaUser newUser = new LadaUser();
            newUser.setName(name);
            user = (LadaUser) repository.create(newUser).getData();
        }

        this.userInfo = new UserInfo(name, user.getId(), auth);
        initAuthorizers(repository);
    }

    /**
     * Constructor to be used outside request context.
     */
    public HeaderAuthorization(
        UserInfo userInfo,
        Repository repository
    ) {
        this.userInfo = userInfo;
        initAuthorizers(repository);
    }

    private void initAuthorizers(Repository repository) {
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
        MessprogrammIdAuthorizer mpIdAuthorizer =
            new MessprogrammIdAuthorizer(repository);
        TagAuthorizer tagAuthorizer =
            new TagAuthorizer(repository);
        TagZuordnungAuthorizer tagZuordnungAuthorizer =
            new TagZuordnungAuthorizer(repository);

        this.authorizers = Map.ofEntries(
            Map.entry(Sample.class, probeAuthorizer),
            Map.entry(Measm.class, messungAuthorizer),
            Map.entry(Geolocat.class, pIdAuthorizer),
            Map.entry(CommSample.class, pIdAuthorizer),
            Map.entry(SampleSpecifMeasVal.class, pIdAuthorizer),
            Map.entry(CommMeasm.class, mIdAuthorizer),
            Map.entry(MeasVal.class, mIdAuthorizer),
            Map.entry(StatusProt.class, mIdAuthorizer),
            Map.entry(Sampler.class, netzAuthorizer),
            Map.entry(DatasetCreator.class, netzAuthorizer),
            Map.entry(MunicDiv.class, netzAuthorizer),
            Map.entry(MpgCateg.class, netzAuthorizer),
            Map.entry(Site.class, netzAuthorizer),
            Map.entry(Mpg.class, messprogrammAuthorizer),
            Map.entry(MpgMmtMp.class, messprogrammAuthorizer),
            Map.entry(GeolocatMpg.class, mpIdAuthorizer),
            Map.entry(Tag.class, tagAuthorizer),
            Map.entry(TagLink.class, tagZuordnungAuthorizer)
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
     * @param clazz     The data object class.
     * @return The Response object containing the filtered data.
     */
    @Override
    public <T extends BaseModel> Response filter(
        Response data,
        Class<T> clazz
    ) {
        Authorizer authorizer = authorizers.get(clazz);
        if (authorizer == null) {
            return new Response(false, StatusCodes.NOT_ALLOWED, null);
        }
        return authorizer.filter(data, userInfo, clazz);
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
    public <T> void authorize(
        Object data,
        RequestMethod method,
        Class<T> clazz
    ) {
        if (isAuthorized(data, method, clazz)) {
            return;
        }
        throw new ForbiddenException();
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
        return authorizer.isAuthorized(data, method, userInfo, clazz);
    }

    /**
     * Check whether a user is authorized to operate on the given data
     * by the given object id.
     *
     * @param id        The data's id to test.
     * @param method    The Http request type.
     * @param clazz     The data object class.
     * @return True if the user is authorized else returns false.
     */
    @Override
    public <T> boolean isAuthorizedById(
        Object id,
        RequestMethod method,
        Class<T> clazz
    ) {
        Authorizer authorizer = authorizers.get(clazz);
        // Do not authorize anything unknown
        if (authorizer == null) {
            return false;
        }
        return authorizer.isAuthorizedById(id, method, userInfo, clazz);
    }

    /**
     * Test whether a probe is readonly.
     *
     * @param probeId   The probe Id.
     * @return True if the probe is readonly.
     */
    @Override
    public boolean isProbeReadOnly(Integer probeId) {
        Authorizer a = authorizers.get(Sample.class);
        return a.isProbeReadOnly(probeId);
    }

    /**
     * Test whether a Messung object is readonly.
     *
     * @param messungId   The ID of the Messung object.
     * @return True if the Messung object is readonly.
     */
    @Override
    public boolean isMessungReadOnly(Integer messungId) {
        Authorizer a = authorizers.get(Measm.class);
        return a.isMessungReadOnly(messungId);
    }
}
