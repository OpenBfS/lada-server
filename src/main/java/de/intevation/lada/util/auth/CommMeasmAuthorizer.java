/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.auth;

import de.intevation.lada.model.lada.BelongsToMeasm;
import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.model.lada.MeasFacilOwned;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;


class CommMeasmAuthorizer extends Authorizer<CommMeasm> {

    private Authorizer<BelongsToMeasm> belongsToMeasmAuthorizer;
    private Authorizer<MeasFacilOwned> measFacilAuthorizer;

    CommMeasmAuthorizer(
        UserInfo userInfo,
        Repository repository
    ) {
        super(userInfo, repository);

        this.belongsToMeasmAuthorizer = new BelongsToMeasmAuthorizer(
            this.userInfo, this.repository);
        this.measFacilAuthorizer = new MeasFacilOwnedAuthorizer(
            this.userInfo, this.repository);
    }

    @Override
    void authorizeMethod(
        CommMeasm data,
        RequestMethod method
    ) throws AuthorizationException {
        belongsToMeasmAuthorizer.authorizeMethod(data, method);
        if (!RequestMethod.GET.equals(method)) {
            measFacilAuthorizer.authorizeMethod(data, method);
        }
    }

    @Override
    void setAuthAttrs(CommMeasm object) {
        // Set owner
        this.belongsToMeasmAuthorizer.setAuthAttrs(object);

        // Set readonly flag
        super.setAuthAttrs(object);
    }
}
