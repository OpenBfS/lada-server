/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.lada.BelongsToSample;
import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.model.lada.MeasFacilOwned;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


class CommSampleAuthorizer extends Authorizer<CommSample> {

    private Authorizer<BelongsToSample> belongsToSampleAuthorizer;
    private Authorizer<MeasFacilOwned> measFacilAuthorizer;

    CommSampleAuthorizer(
        UserInfo userInfo,
        Repository repository
    ) {
        super(userInfo, repository);

        this.belongsToSampleAuthorizer = new BelongsToSampleAuthorizer(
            this.userInfo, this.repository);
        this.measFacilAuthorizer = new MeasFacilOwnedAuthorizer(
            this.userInfo, this.repository);
    }

    @Override
    void authorizeMethod(
        CommSample data,
        RequestMethod method
    ) throws AuthorizationException {
        belongsToSampleAuthorizer.authorizeMethod(data, method);
        if (!RequestMethod.GET.equals(method)) {
            measFacilAuthorizer.authorizeMethod(data, method);
        }
    }

    @Override
    void setAuthAttrs(CommSample object) {
        // Set owner
        this.belongsToSampleAuthorizer.setAuthAttrs(object);

        // Set readonly flag
        super.setAuthAttrs(object);
    }
}
