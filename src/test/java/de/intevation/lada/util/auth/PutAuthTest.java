/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.auth;

import static org.junit.Assert.assertFalse;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.BaseTest;
import de.intevation.lada.i18n.I18n;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.master.Auth;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;

import jakarta.inject.Inject;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;


/**
 * Ensure {@link RequestMethod.PUT} is not authorized if persistent state
 * of given entity does not allow it, regardless of state of given entity
 * in relation to persistence context.
 */
@RunWith(Arquillian.class)
public class PutAuthTest extends BaseTest {

    private static final int SAMPLE_ID_UNAUTHORIZED = 1;
    private static final String MEAS_FACIL_AUTHORIZED = "06010";

    @Inject
    private UserTransaction tx;

    @Inject
    private I18n i18n;

    @Inject
    private Repository repository;

    private Authorization authorization;

    /**
     * Test constructor.
     */
    public PutAuthTest() {
        testDatasetName = "datasets/dbUnit_put_auth.xml";
    }

    /**
     * Init authorizer.
     */
    @Before
    public void init()
        throws NotSupportedException, SystemException {
        try {
            tx.begin();
            this.authorization = new Authorization(
                new UserInfo(
                    BaseTest.testUser, 1, repository.getAll(Auth.class)),
                this.i18n,
                this.repository);
        } catch (RuntimeException e) {
            tx.rollback();
            throw e;
        }
    }

    @After
    public void cleanup() throws SystemException {
        tx.rollback();
    }

    @Test
    public void putNew() {
        Sample newSample = new Sample();
        newSample.setId(SAMPLE_ID_UNAUTHORIZED);
        newSample.setMeasFacilId(MEAS_FACIL_AUTHORIZED);
        assertPutNotAuthorized(newSample);
    }

    @Test
    public void putManaged() {
        Sample persistent = repository.getById(
            Sample.class, SAMPLE_ID_UNAUTHORIZED);
        persistent.setMeasFacilId(MEAS_FACIL_AUTHORIZED);
        assertPutNotAuthorized(persistent);
    }

    @Test
    public void putDetached() {
        Sample persistent = repository.getById(
            Sample.class, SAMPLE_ID_UNAUTHORIZED);
        persistent.setMeasFacilId(MEAS_FACIL_AUTHORIZED);
        repository.entityManager().detach(persistent);
        assertPutNotAuthorized(persistent);
    }

    private void assertPutNotAuthorized(Sample sample) {
        assertFalse(
            "PUT should not have been authorized",
            authorization.isAuthorized(sample, RequestMethod.PUT));
    }
}
