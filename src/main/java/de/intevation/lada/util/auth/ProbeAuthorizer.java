/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import java.util.List;

import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


public class ProbeAuthorizer extends BaseAuthorizer {

    private MessungAuthorizer messungAuthorizer;

    public ProbeAuthorizer(Repository repository) {
        super(repository);

        this.messungAuthorizer = new MessungAuthorizer(repository, this);
    }

    public ProbeAuthorizer(
        Repository repository, MessungAuthorizer messungAuthorizer
    ) {
        super(repository);

        this.messungAuthorizer = messungAuthorizer;
    }

    @Override
    public String isAuthorizedReason(
        Object data,
        RequestMethod method,
        UserInfo userInfo
    ) {
        Sample probe = (Sample) data;
        if (method == RequestMethod.PUT
            || method == RequestMethod.DELETE) {
            return !anyMeasmReadOnly(probe.getId(), userInfo)
                && getAuthorization(userInfo, probe)
                ? null : I18N_KEY_FORBIDDEN;
        }
        return getAuthorization(userInfo, probe)
            ? null : I18N_KEY_FORBIDDEN;
    }

    @Override
    public void setAuthAttrs(
        BaseModel data,
        UserInfo userInfo
    ) {
        // Set readonly flag
        super.setAuthAttrs(data, userInfo);

        Sample sample = (Sample) data;
        String mstId = sample.getMeasFacilId();
        MeasFacil mst = repository.getById(MeasFacil.class, mstId);
        sample.setOwner(
            userInfo.getNetzbetreiber().contains(mst.getNetworkId())
            && userInfo.belongsTo(mstId, sample.getApprLabId()));
    }

    private boolean anyMeasmReadOnly(Integer sampleId, UserInfo userInfo) {
        QueryBuilder<Measm> builder = repository
            .queryBuilder(Measm.class)
            .and(Measm_.sampleId, sampleId);
        List<Measm> measms = repository.filter(builder.getQuery());
        for (Measm measm: measms) {
            if (!messungAuthorizer.isAuthorized(
                    measm, RequestMethod.PUT, userInfo)
            ) {
                return true;
            }
        }
        return false;
    }

    private boolean getAuthorization(UserInfo userInfo, Sample sample) {
        return userInfo.getMessstellen().contains(sample.getMeasFacilId());
    }
}
