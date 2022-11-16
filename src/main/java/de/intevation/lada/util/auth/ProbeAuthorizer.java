/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import java.util.ArrayList;
import java.util.List;

import de.intevation.lada.model.land.Sample;
import de.intevation.lada.model.stammdaten.MeasFacil;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;

public class ProbeAuthorizer extends BaseAuthorizer {

    public ProbeAuthorizer(Repository repository) {
        super(repository);
    }

    @Override
    public <T> boolean isAuthorized(
        Object data,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        Sample probe = (Sample) data;
        if (method == RequestMethod.PUT
            || method == RequestMethod.DELETE) {
            return !isProbeReadOnly(probe.getId())
                && getAuthorization(userInfo, probe);
        }
        return getAuthorization(userInfo, probe);
    }

    @Override
    public <T> boolean isAuthorizedById(
        Object id,
        RequestMethod method,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        Sample probe = repository.getByIdPlain(Sample.class, id);
        return isAuthorized(probe, method, userInfo, clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Response filter(
        Response data,
        UserInfo userInfo,
        Class<T> clazz
    ) {
        if (data.getData() instanceof List<?>) {
            List<Sample> proben = new ArrayList<Sample>();
            for (Sample probe :(List<Sample>) data.getData()) {
                proben.add(setAuthData(userInfo, probe));
            }
            data.setData(proben);
        } else if (data.getData() instanceof Sample) {
            Sample probe = (Sample) data.getData();
            data.setData(setAuthData(userInfo, probe));
        }
        return data;
    }

    /**
     * Set authorization data for the current probe object.
     *
     * @param userInfo  The user information.
     * @param probe     The probe object.
     * @return The probe.
     */
    private Sample setAuthData(UserInfo userInfo, Sample probe) {
        MeasFacil mst =
            repository.getByIdPlain(
                MeasFacil.class, probe.getMeasFacilId());
        if (!userInfo.getNetzbetreiber().contains(mst.getNetworkId())) {
            probe.setOwner(false);
            probe.setReadonly(true);
            return probe;
        }
        if (userInfo.belongsTo(probe.getMeasFacilId(), probe.getApprLabId())) {
            probe.setOwner(true);
        } else {
            probe.setOwner(false);
        }
        probe.setReadonly(this.isProbeReadOnly(probe.getId()));
        return probe;
    }
}
