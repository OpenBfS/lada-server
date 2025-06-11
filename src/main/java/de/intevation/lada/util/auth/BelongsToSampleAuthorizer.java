/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.lada.BelongsToSample;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


class BelongsToSampleAuthorizer extends Authorizer<BelongsToSample> {

    Authorizer<Sample> probeAuthorizer;

    BelongsToSampleAuthorizer(
        UserInfo userInfo,
        Repository repository
    ) {
        super(userInfo, repository);

        this.probeAuthorizer = new SampleAuthorizer(
            this.userInfo, this.repository);
    }

    @Override
    void authorizeMethod(
        BelongsToSample data,
        RequestMethod method
    ) throws AuthorizationException {
        // Authorized if editing associated sample is authorized
        probeAuthorizer.authorizeMethod(
            repository.getById(Sample.class, data.getSampleId()),
            RequestMethod.PUT);
    }

    @Override
    void setAuthAttrs(BelongsToSample object) {
        // Set readonly flag
        super.setAuthAttrs(object);

        Sample sample = repository.getById(Sample.class, object.getSampleId());

        String mfId = sample.getMeasFacilId();
        MeasFacil mst = repository.getById(MeasFacil.class, mfId);

        object.setOwner(
            userInfo.getNetzbetreiber().contains(mst.getNetworkId())
            && userInfo.belongsTo(mfId, sample.getApprLabId()));
    }
}
