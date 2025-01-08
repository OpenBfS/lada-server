/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import java.util.List;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Measm_;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.data.QueryBuilder;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


class ProbeAuthorizer extends Authorizer<Sample> {

    private Authorizer<Measm> messungAuthorizer;

    ProbeAuthorizer(
        UserInfo userInfo,
        Repository repository
    ) {
        super(userInfo, repository);

        this.messungAuthorizer = new MessungAuthorizer(
            this.userInfo, this.repository, this);
    }

    /**
     * Constructor to be used in MessungAuthorizer.
     */
    ProbeAuthorizer(
        UserInfo userInfo,
        Repository repository,
        Authorizer<Measm> messungAuthorizer
    ) {
        super(userInfo, repository);

        this.messungAuthorizer = messungAuthorizer;
    }

    @Override
    void authorize(
        Sample probe,
        RequestMethod method
    ) throws AuthorizationException {
        if (method == RequestMethod.PUT
            || method == RequestMethod.DELETE) {
            if (!anyMeasmReadOnly(probe.getId(), userInfo)
                && getAuthorization(userInfo, probe)) {
                return;
            }
            throw new AuthorizationException(I18N_KEY_FORBIDDEN);
        }
        if (getAuthorization(userInfo, probe)) {
            return;
        }
        throw new AuthorizationException(I18N_KEY_FORBIDDEN);
    }

    @Override
    void setAuthAttrs(Sample sample) {
        // Set readonly flag
        super.setAuthAttrs(sample);

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
            if (!messungAuthorizer.isAuthorized(measm, RequestMethod.PUT)) {
                return true;
            }
        }
        return false;
    }

    private boolean getAuthorization(UserInfo userInfo, Sample sample) {
        return userInfo.getMessstellen().contains(sample.getMeasFacilId());
    }
}
