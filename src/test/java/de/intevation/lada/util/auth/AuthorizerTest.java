/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.auth;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.logging.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.model.lada.GeolocatMpg;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.TagLink;
import de.intevation.lada.model.master.Auth;
import de.intevation.lada.model.master.Sampler;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;
import de.intevation.lada.util.rest.Response;
import jakarta.inject.Inject;


/**
 * Class testing authorizers.
 */
@RunWith(Arquillian.class)
public class AuthorizerTest extends BaseTest {

    private Logger log = Logger.getLogger(AuthorizerTest.class);

    //Error collector to collect nested test errors
    @Rule
    public ErrorCollector collector = new ErrorCollector();

    private static Authorization authorization;

    private static Repository repository;

    /**
     * Test constructor.
     */
    public AuthorizerTest() {
        testDatasetName = "datasets/dbUnit_authorizer.xml";
    }

    /**
     * Init repository.
     * @param repo Repo.
     */
    @Inject
    private void initRepository(Repository repo) {
        repository = repo;
    }

    /**
     * Init authorizer.
     */
    @Before
    public void initAuthorization() {
        final int userId = 2;
        this.authorization = new HeaderAuthorization(
            new UserInfo(
                testUser,
                userId,
                List.of(repository.getByIdPlain(Auth.class, 1))),
            repository);
    }

    /**
     * Test base authorizer functions.
     */
    @Test
    @Transactional
    public void testBaseAuthorizer() {
        BaseAuthorizer baseAuthorizer = new ProbeAuthorizer(repository);
        //Test authorized sample
        collector.checkThat(
            "Authorized sample",
            baseAuthorizer.isProbeReadOnly(
                ParameterizedTests.SAMPLE_ID_AUTHORIZED),
            CoreMatchers.is(false));
        //Test locked sample
        collector.checkThat(
            "Unauthorized sample",
            baseAuthorizer.isProbeReadOnly(
                ParameterizedTests.SAMPLE_ID_LOCKED_BY_STATUS),
            CoreMatchers.is(true));
        //Test unlocked measm
        collector.checkThat(
            "Unlocked measm",
            baseAuthorizer.isMessungReadOnly(
                ParameterizedTests.MEASM_ID_STATUS_EDITABLE),
            CoreMatchers.is(false));
        //Test locked measm
        collector.checkThat(
            "Locked measm",
            baseAuthorizer.isMessungReadOnly(
                ParameterizedTests.MEASM_ID_STATUS_LOCKED),
            CoreMatchers.is(true));
    }

    /**
     * Run parameterized tests.
     * @throws Exception Exception that may occure during run
     */
    @Test
    @Transactional
    public void runHeaderAuthorizationTests() throws Exception {
        Result result = JUnitCore.runClasses(ParameterizedTests.class);
        log.info(String.format(
            "Tests run: %d, failed: %d, ignored: %d",
            result.getRunCount(), result.getFailureCount(),
            result.getIgnoreCount()));
        result.getFailures().forEach(failure -> {
            String descr = failure.getDescription().getDisplayName();
            String msg = failure.getMessage();
            String error = String.format("%s: %s", descr, msg);
            log.error(error);
            collector.addError(new Throwable(error, failure.getException()));
        });
    }

    /**
     * Nested test class running parameterized tests using junit
     * parameterized runnner.
     */
    @RunWith(Parameterized.class)
    public static class ParameterizedTests {

        //Constants
        private static final int TAG_ID_GLOBAL = 103;
        private static final String NETWORK_ID_AUTHORIZED = "06";
        private static final String NETWORK_ID_UNAUTHORIZED = "01";
        private static final int MEASM_ID_LOCKED_BY_SAMPLE = 1201;
        private static final int MEASM_ID_STATUS_LOCKED = 1212;
        private static final int MEASM_ID_STATUS_EDITABLE = 1209;
        private static final int MEASM_ID_NO_STATUS = 1200;
        private static final int SAMPLE_ID_AUTHORIZED = 1099;
        private static final int SAMPLE_ID_UNAUTORIZED = 1001;
        private static final int SAMPLE_ID_LOCKED_BY_STATUS = 1100;
        private static final int MPG_ID_AUTHORIZED = 1000;
        private static final int MPG_ID_UNAUTHORIZED = 1001;
        private static final String MEAS_FACIL_ID_01010 = "01010";
        private static final String MEAS_FACIL_ID_06010 = "06010";

        //Test parameters
        @Parameter(0)
        public Object testObject;
        @Parameter(1)
        public RequestMethod method;
        @Parameter(2)
        public Boolean expectedResult;
        @Parameter(3)
        public Boolean expectedReadonly;
        @Parameter(4)
        public String authorizer;
        @Parameter(5)
        public Object testObjectId;

        /**
         * Create test data list.
         * @return List of test data rows.
         */
        @Parameters(name =
            "[#{index} {4}] TestObjectId: {5}, Method: {1}, "
            + "exptectedAuthResult: {2}, expectedReadonly: {3}")
        public static List<Object[]> getParameters() {
            List<Object[]> paramList = new ArrayList<>();
            paramList.addAll(createMpgTestData());
            paramList.addAll(createMpgIdTestData());
            paramList.addAll(createMeasmTestData());
            paramList.addAll(createMeasmIdTestData());
            paramList.addAll(createNetworkTestData());
            paramList.addAll(createSampleTestData());
            paramList.addAll(createSampleIdTestData());
            paramList.addAll(createTagTestData());
            paramList.addAll(createTagLinkTestData());

            return paramList;
        }

        /**
         * Test the authorizers isAuthorized method.
         */
        @Test
        public void testIsAuthorized() {
            Class<?> testClass = testObject.getClass();
            boolean authorized = authorization.isAuthorized(
                    testObject, method, testClass);
            assertEquals(expectedResult, authorized);
        }

        /**
         * Test the authorizer filter method.
         * Will be skipped if no expected readonly status is given.
         */
        @Test
        public void testFilter()
            throws NoSuchMethodException, SecurityException,
            IllegalAccessException, InvocationTargetException {
            //Skip if method is not GET or no expected result is given
            if (!method.equals(RequestMethod.GET)
                    || expectedReadonly == null) {
                return;
            }
            Response response = new Response(true, 0, testObject);
            Object filtered = authorization
                .filter(response, testObject.getClass())
                .getData();
            assertEquals(
                expectedReadonly,
                filtered.getClass().getMethod("isReadonly").invoke(filtered));
        }

        /**
         * Create a test data row.
         * @param testObject Object to test
         * @param requestMethod Request method
         * @param expectedResult Expected authorization result
         * @param authorizerClass Authorizer class
         * @return Test data row as Object[]
         * @throws IllegalArgumentException Thrown if no testobject is given.
         */
        private static Object[] createTestDataRow(
            Object testObject, RequestMethod requestMethod,
            Boolean expectedResult,
            Class<?> authorizerClass) {
            return createTestDataRow(
                testObject, requestMethod,
                expectedResult, null, authorizerClass);
        }

        /**
         * Create a test data row.
         * @param testObject Object to test
         * @param requestMethod Request method
         * @param expectedResult Expected authorization result
         * @param expectedReadonly Expected readonly status.
         *                         May be null if not applicable.
         * @param authorizerClass Authorizer class
         * @return Test data row as Object[]
         * @throws IllegalArgumentException Thrown if no testobject is given.
         */
        private static Object[] createTestDataRow(
            Object testObject, RequestMethod requestMethod,
            Boolean expectedResult, Boolean expectedReadonly,
            Class<?> authorizerClass
        ) {
            //Get test object id
            Object id = null;
            try {
                Method m = testObject.getClass()
                    .getMethod("getId");
                id = m.invoke(testObject);
            } catch (NoSuchMethodException | SecurityException
                | IllegalAccessException
                | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return new Object[]{
                testObject, requestMethod,
                expectedResult, expectedReadonly,
                authorizerClass.getSimpleName(), id};
        }

        /**
         * Create test data for mpg authorizer tests.
         * @return List of test data rows
         */
        private static List<Object[]> createMpgTestData() {
            Class<?> authorizer = MessprogrammAuthorizer.class;
            List<Object[]> data = new ArrayList<>();

            //Test mpg that should be editable
            Mpg authorized = new Mpg();
            authorized.setMeasFacilId(MEAS_FACIL_ID_06010);
            data.add(createTestDataRow(
                authorized, RequestMethod.GET, true, false, authorizer));
            data.add(createTestDataRow(
                authorized, RequestMethod.POST, true, authorizer));
            data.add(createTestDataRow(
                authorized, RequestMethod.PUT, true, authorizer));
            data.add(createTestDataRow(
                authorized, RequestMethod.DELETE, true, authorizer));

            //Test mpg that should not be editable
            Mpg unauth = new Mpg();
            unauth.setMeasFacilId(MEAS_FACIL_ID_01010);
            data.add(createTestDataRow(
                unauth, RequestMethod.GET, true, true, authorizer));
            data.add(createTestDataRow(
                unauth, RequestMethod.POST, false, authorizer));
            data.add(createTestDataRow(
                unauth, RequestMethod.PUT, false, authorizer));
            data.add(createTestDataRow(
                unauth, RequestMethod.DELETE, false, authorizer));
            return data;
        }

        /**
         * Create test data for mpg id authorizer tests.
         * @return List of test data rows
         */
        private static List<Object[]> createMpgIdTestData() {
            Class<?> authorizer = MessprogrammIdAuthorizer.class;
            List<Object[]> data = new ArrayList<>();

            //Test geolocatMpg attached to editable mpg
            GeolocatMpg authorized = new GeolocatMpg();
            authorized.setMpgId(MPG_ID_AUTHORIZED);
            data.add(createTestDataRow(authorized,
                RequestMethod.GET, true, false, authorizer));
            data.add(createTestDataRow(authorized,
                RequestMethod.POST, true, authorizer));
            data.add(createTestDataRow(authorized,
                RequestMethod.PUT, true, authorizer));
            data.add(createTestDataRow(authorized,
                RequestMethod.DELETE, true, authorizer));

            //Test geolocatMpg attached to non editable mpg
            GeolocatMpg unauth = new GeolocatMpg();
            unauth.setMpgId(MPG_ID_UNAUTHORIZED);
            data.add(createTestDataRow(unauth,
                RequestMethod.GET, false, true, authorizer));
            data.add(createTestDataRow(unauth,
                RequestMethod.POST, false, authorizer));
            data.add(createTestDataRow(unauth,
                RequestMethod.PUT, false, authorizer));
            data.add(createTestDataRow(unauth,
                RequestMethod.DELETE, false, authorizer));
            return data;
        }

        private static List<Object[]> createMeasmTestData() {
            Class<?> authorizer = MessungAuthorizer.class;
            List<Object[]> data = new ArrayList<>();

            //Test editable measm without status
            Measm noStatus = repository.getByIdPlain(
                Measm.class, MEASM_ID_NO_STATUS);
            data.add(createTestDataRow(
                noStatus, RequestMethod.GET, true, false, authorizer));
            data.add(createTestDataRow(
                noStatus, RequestMethod.POST, true, authorizer));
            data.add(createTestDataRow(
                noStatus, RequestMethod.PUT, true, authorizer));
            data.add(createTestDataRow(
                noStatus, RequestMethod.DELETE, true, authorizer));

            //Test measm with editable status
            Measm editableStatus = repository.getByIdPlain(
                Measm.class, MEASM_ID_STATUS_EDITABLE);
            data.add(createTestDataRow(
                editableStatus, RequestMethod.GET, true, false, authorizer));
            data.add(createTestDataRow(
                editableStatus, RequestMethod.POST, true, authorizer));
            data.add(createTestDataRow(
                editableStatus, RequestMethod.PUT, true, authorizer));
            data.add(createTestDataRow(
                editableStatus, RequestMethod.DELETE, true, authorizer));

            //Test measm locked by status
            Measm lockedByStatus = repository.getByIdPlain(
                Measm.class, MEASM_ID_STATUS_LOCKED);
            data.add(createTestDataRow(
                lockedByStatus, RequestMethod.GET, true, false, authorizer));
            data.add(createTestDataRow(
                lockedByStatus, RequestMethod.POST, true, authorizer));
            data.add(createTestDataRow(
                lockedByStatus, RequestMethod.PUT, false, authorizer));
            data.add(createTestDataRow(
                lockedByStatus, RequestMethod.DELETE, false, authorizer));

            //Test measm locked by connected sample
            Measm lockedBySample = repository.getByIdPlain(
                Measm.class, MEASM_ID_LOCKED_BY_SAMPLE);
            data.add(createTestDataRow(
                lockedBySample, RequestMethod.GET, false, true, authorizer));
            data.add(createTestDataRow(
                lockedBySample, RequestMethod.POST, false, authorizer));
            data.add(createTestDataRow(
                lockedBySample, RequestMethod.PUT, false, authorizer));
            data.add(createTestDataRow(
                lockedBySample, RequestMethod.DELETE, false, authorizer));
            return data;
        }

        private static List<Object[]> createMeasmIdTestData() {
            Class<?> authorizer = MessungIdAuthorizer.class;
            List<Object[]> data = new ArrayList<>();

            //Test editable measm without status
            CommMeasm noStatus = new CommMeasm();
            noStatus.setMeasmId(MEASM_ID_NO_STATUS);
            data.add(createTestDataRow(
                noStatus, RequestMethod.GET, false, false, authorizer));
            data.add(createTestDataRow(
                noStatus, RequestMethod.POST, true, authorizer));
            data.add(createTestDataRow(
                noStatus, RequestMethod.PUT, true, authorizer));
            data.add(createTestDataRow(
                noStatus, RequestMethod.DELETE, true, authorizer));

            //Test measm with editable status
            CommMeasm editableStatus = new CommMeasm();
            editableStatus.setMeasmId(MEASM_ID_STATUS_EDITABLE);
            data.add(createTestDataRow(
                editableStatus, RequestMethod.GET, false, authorizer));
            data.add(createTestDataRow(
                editableStatus, RequestMethod.POST, true, authorizer));
            data.add(createTestDataRow(
                editableStatus, RequestMethod.PUT, true, authorizer));
            data.add(createTestDataRow(
                editableStatus, RequestMethod.DELETE, true, authorizer));

            //Test measm locked by status
            CommMeasm lockedByStatus = new CommMeasm();
            lockedByStatus.setMeasmId(MEASM_ID_STATUS_LOCKED);
            data.add(createTestDataRow(
                lockedByStatus, RequestMethod.GET, false, authorizer));
            data.add(createTestDataRow(
                lockedByStatus, RequestMethod.POST, true, authorizer));
            data.add(createTestDataRow(
                lockedByStatus, RequestMethod.PUT, true, authorizer));
            data.add(createTestDataRow(
                lockedByStatus, RequestMethod.DELETE, true, authorizer));

            //Test measm locked by connected sample
            CommMeasm lockedBySample = new CommMeasm();
            lockedBySample.setMeasmId(MEASM_ID_LOCKED_BY_SAMPLE);
            data.add(createTestDataRow(
                lockedBySample, RequestMethod.GET, false, true, authorizer));
            data.add(createTestDataRow(
                lockedBySample, RequestMethod.POST, false, authorizer));
            data.add(createTestDataRow(
                lockedBySample, RequestMethod.PUT, false, authorizer));
            data.add(createTestDataRow(
                lockedBySample, RequestMethod.DELETE, false, authorizer));
            return data;
        }

        private static List<Object[]> createNetworkTestData() {
            Class<?> authorizer = NetzbetreiberAuthorizer.class;
            List<Object[]> data = new ArrayList<>();

            //Test authorized sampler
            Sampler authorized = new Sampler();
            authorized.setNetworkId(NETWORK_ID_AUTHORIZED);
            data.add(createTestDataRow(authorized,
                RequestMethod.GET, false, authorizer));
            data.add(createTestDataRow(authorized,
                RequestMethod.POST, true, authorizer));
            data.add(createTestDataRow(authorized,
                RequestMethod.PUT, true, authorizer));
            data.add(createTestDataRow(authorized,
                RequestMethod.DELETE, true, authorizer));

            //Test unauthorized sampler
            Sampler unauth = new Sampler();
            unauth.setNetworkId(NETWORK_ID_UNAUTHORIZED);
            data.add(createTestDataRow(unauth,
                RequestMethod.GET, false, authorizer));
            data.add(createTestDataRow(unauth,
                RequestMethod.POST, false, authorizer));
            data.add(createTestDataRow(unauth,
                RequestMethod.PUT, false, authorizer));
            data.add(createTestDataRow(unauth,
                RequestMethod.DELETE, false, authorizer));

            //Test Site special handling
            //Test authorized site
            Site authorizedSite = new Site();
            authorizedSite.setNetworkId(NETWORK_ID_AUTHORIZED);
            data.add(createTestDataRow(authorizedSite,
                RequestMethod.GET, false, authorizer));
            data.add(createTestDataRow(authorizedSite,
                RequestMethod.POST, true, authorizer));
            data.add(createTestDataRow(authorizedSite,
                RequestMethod.PUT, true, authorizer));
            data.add(createTestDataRow(authorizedSite,
                RequestMethod.DELETE, true, authorizer));

            //Test unauthorized site
            Site unauthSite = new Site();
            unauthSite.setNetworkId(NETWORK_ID_UNAUTHORIZED);
            data.add(createTestDataRow(unauthSite,
                RequestMethod.GET, false, authorizer));
            data.add(createTestDataRow(unauthSite,
                RequestMethod.POST, false, authorizer));
            data.add(createTestDataRow(unauthSite,
                RequestMethod.PUT, false, authorizer));
            data.add(createTestDataRow(unauthSite,
                RequestMethod.DELETE, false, authorizer));

            return data;
        }

        private static List<Object[]> createSampleTestData() {
            Class<?> authorizer = ProbeAuthorizer.class;
            List<Object[]> data = new ArrayList<>();

            //Test authorized sample
            Sample authorized = new Sample();
            authorized.setMeasFacilId(MEAS_FACIL_ID_06010);
            data.add(createTestDataRow(
                authorized, RequestMethod.GET, true, false, authorizer));
            data.add(createTestDataRow(
                authorized, RequestMethod.POST, true, authorizer));
            data.add(createTestDataRow(
                authorized, RequestMethod.PUT, true, authorizer));
            data.add(createTestDataRow(
                authorized, RequestMethod.DELETE, true, authorizer));

            //Test unauthorized sample
            Sample unauthorized = new Sample();
            unauthorized.setMeasFacilId(MEAS_FACIL_ID_01010);
            data.add(createTestDataRow(
                unauthorized, RequestMethod.GET, false, true, authorizer));
            data.add(createTestDataRow(
                unauthorized, RequestMethod.POST, false, authorizer));
            data.add(createTestDataRow(
                unauthorized, RequestMethod.PUT, false, authorizer));
            data.add(createTestDataRow(
                unauthorized, RequestMethod.DELETE, false, authorizer));

            //Test sample locked by measm status
            Sample statusLocked = repository.getByIdPlain(
                Sample.class, SAMPLE_ID_LOCKED_BY_STATUS);
            data.add(createTestDataRow(
                statusLocked, RequestMethod.GET, true, authorizer));
            data.add(createTestDataRow(
                statusLocked, RequestMethod.POST, true, authorizer));
            data.add(createTestDataRow(
                statusLocked, RequestMethod.PUT, false, authorizer));
            data.add(createTestDataRow(
                statusLocked, RequestMethod.DELETE, false, authorizer));
            return data;
        }

        private static List<Object[]> createSampleIdTestData() {
            Class<?> authorizer = ProbeIdAuthorizer.class;
            List<Object[]> data = new ArrayList<>();

            //Test authorized sample id
            CommSample authorized = new CommSample();
            authorized.setSampleId(SAMPLE_ID_AUTHORIZED);
            data.add(createTestDataRow(
                authorized, RequestMethod.GET, true, false, authorizer));
            data.add(createTestDataRow(
                authorized, RequestMethod.POST, true, authorizer));
            data.add(createTestDataRow(
                authorized, RequestMethod.PUT, true, authorizer));
            data.add(createTestDataRow(
                authorized, RequestMethod.DELETE, true, authorizer));

            //Test unauthorized sample id
            CommSample unauthorized = new CommSample();
            unauthorized.setSampleId(SAMPLE_ID_UNAUTORIZED);
            data.add(createTestDataRow(
                unauthorized, RequestMethod.GET, false, true, authorizer));
            data.add(createTestDataRow(
                unauthorized, RequestMethod.POST, false, authorizer));
            data.add(createTestDataRow(
                unauthorized, RequestMethod.PUT, false, authorizer));
            data.add(createTestDataRow(
                unauthorized, RequestMethod.DELETE, false, authorizer));

            //Test sample id locked by measm status
            CommSample statusLocked = new CommSample();
                statusLocked.setSampleId(SAMPLE_ID_LOCKED_BY_STATUS);
            data.add(createTestDataRow(
                statusLocked, RequestMethod.GET, false, authorizer));
            data.add(createTestDataRow(
                statusLocked, RequestMethod.POST, false, authorizer));
            data.add(createTestDataRow(
                statusLocked, RequestMethod.PUT, false, authorizer));
            data.add(createTestDataRow(
                statusLocked, RequestMethod.DELETE, false, authorizer));
            return data;
        }

        private static List<Object[]> createTagTestData() {
            Class<?> authorizer = TagAuthorizer.class;
            List<Object[]> data = new ArrayList<>();

            //Test that global tags are never authorized
            Tag global = new Tag();
            global.setTagType(Tag.TAG_TYPE_GLOBAL);
            data.add(createTestDataRow(
                global, RequestMethod.GET, false, true, authorizer));
            data.add(createTestDataRow(
                global, RequestMethod.POST, false, authorizer));
            data.add(createTestDataRow(
                global, RequestMethod.PUT, false, authorizer));
            data.add(createTestDataRow(
                global, RequestMethod.DELETE, false, authorizer));

            //Test meas facil tag of own meas facil
            Tag authorizedMeasFacil = new Tag();
            authorizedMeasFacil.setTagType(Tag.TAG_TYPE_MST);
            authorizedMeasFacil.setMeasFacilId(MEAS_FACIL_ID_06010);
            data.add(createTestDataRow(
                authorizedMeasFacil, RequestMethod.GET,
                true, false, authorizer));
            data.add(createTestDataRow(
                authorizedMeasFacil, RequestMethod.POST, true, authorizer));
            data.add(createTestDataRow(
                authorizedMeasFacil, RequestMethod.PUT, true, authorizer));
            data.add(createTestDataRow(
                authorizedMeasFacil, RequestMethod.DELETE, true, authorizer));

            //Test meas facil tag of other meas facil
            Tag unauthorizedMeasFacil = new Tag();
            unauthorizedMeasFacil.setTagType(Tag.TAG_TYPE_MST);
            unauthorizedMeasFacil.setMeasFacilId(MEAS_FACIL_ID_01010);
            data.add(createTestDataRow(
                unauthorizedMeasFacil, RequestMethod.GET,
                false, true, authorizer));
            data.add(createTestDataRow(
                unauthorizedMeasFacil, RequestMethod.POST, false, authorizer));
            data.add(createTestDataRow(
                unauthorizedMeasFacil, RequestMethod.PUT, false, authorizer));
            data.add(createTestDataRow(
                unauthorizedMeasFacil, RequestMethod.DELETE,
                false, authorizer));

            //Test authorized network tag
            Tag authorizedNetwork = new Tag();
            authorizedNetwork.setTagType(Tag.TAG_TYPE_NETZBETREIBER);
            authorizedNetwork.setNetworkId(NETWORK_ID_AUTHORIZED);
            data.add(createTestDataRow(
                authorizedNetwork, RequestMethod.GET, true, false, authorizer));
            data.add(createTestDataRow(
                authorizedNetwork, RequestMethod.POST, true, authorizer));
            data.add(createTestDataRow(
                authorizedNetwork, RequestMethod.PUT, true, authorizer));
            data.add(createTestDataRow(
                authorizedNetwork, RequestMethod.DELETE, true, authorizer));

            //Test unauthorized network tag
            Tag unauthorizedNetwork = new Tag();
            unauthorizedNetwork.setTagType(Tag.TAG_TYPE_NETZBETREIBER);
            unauthorizedNetwork.setNetworkId(NETWORK_ID_UNAUTHORIZED);
            data.add(createTestDataRow(
                unauthorizedNetwork, RequestMethod.GET,
                false, true, authorizer));
            data.add(createTestDataRow(
                unauthorizedNetwork, RequestMethod.POST, false, authorizer));
            data.add(createTestDataRow(
                unauthorizedNetwork, RequestMethod.PUT, false, authorizer));
            data.add(createTestDataRow(
                unauthorizedNetwork, RequestMethod.DELETE, false, authorizer));
            return data;
        }

        private static List<Object[]> createTagLinkTestData() {
            Class<?> authorizer = TagZuordnungAuthorizer.class;
            List<Object[]> data = new ArrayList<>();

            //Test global tag and authorized sample
            TagLink authorizedSample = new TagLink();
            authorizedSample.setTagId(TAG_ID_GLOBAL);
            authorizedSample.setSampleId(SAMPLE_ID_AUTHORIZED);
            data.add(createTestDataRow(
                authorizedSample, RequestMethod.GET, false, authorizer));
            data.add(createTestDataRow(
                authorizedSample, RequestMethod.POST, true, authorizer));
            data.add(createTestDataRow(
                authorizedSample, RequestMethod.PUT, false, authorizer));
            data.add(createTestDataRow(
                authorizedSample, RequestMethod.DELETE, true, authorizer));

            //Test global tag and authorized sample
            TagLink unauthorizedSample = new TagLink();
            unauthorizedSample.setTagId(TAG_ID_GLOBAL);
            unauthorizedSample.setSampleId(SAMPLE_ID_UNAUTORIZED);
            data.add(createTestDataRow(
                unauthorizedSample, RequestMethod.GET, false, authorizer));
            data.add(createTestDataRow(
                unauthorizedSample, RequestMethod.POST, false, authorizer));
            data.add(createTestDataRow(
                unauthorizedSample, RequestMethod.PUT, false, authorizer));
            data.add(createTestDataRow(
                unauthorizedSample, RequestMethod.DELETE, false, authorizer));

            //Test global tag and authorized measm
            TagLink authorizedMeasm = new TagLink();
            authorizedMeasm.setTagId(TAG_ID_GLOBAL);
            authorizedMeasm.setMeasmId(MEASM_ID_NO_STATUS);
            data.add(createTestDataRow(
                authorizedMeasm, RequestMethod.GET, false, authorizer));
            data.add(createTestDataRow(
                authorizedMeasm, RequestMethod.POST, true, authorizer));
            data.add(createTestDataRow(
                authorizedMeasm, RequestMethod.PUT, false, authorizer));
            data.add(createTestDataRow(
                authorizedMeasm, RequestMethod.DELETE, true, authorizer));

            //Test global tag and authorized measm
            TagLink unauthorizedMeasm = new TagLink();
            unauthorizedMeasm.setTagId(TAG_ID_GLOBAL);
            unauthorizedMeasm.setMeasmId(MEASM_ID_STATUS_LOCKED);
            data.add(createTestDataRow(
                unauthorizedMeasm, RequestMethod.GET, false, authorizer));
            data.add(createTestDataRow(
                unauthorizedMeasm, RequestMethod.POST, false, authorizer));
            data.add(createTestDataRow(
                unauthorizedMeasm, RequestMethod.PUT, false, authorizer));
            data.add(createTestDataRow(
                unauthorizedMeasm, RequestMethod.DELETE, false, authorizer));
            return data;
        }
    }
}
