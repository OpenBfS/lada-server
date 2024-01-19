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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.model.MultipleFailureException;

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

    private static Authorization authorization;

    private static Repository repository;

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

    //Record used to store expected test results in.
    public record ExpectedResults(
        boolean getResult, boolean postResult,
        boolean putResult, boolean deleteResult) { }

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
        authorization = new HeaderAuthorization(
            new UserInfo(
                testUser,
                userId,
                List.of(repository.getByIdPlain(Auth.class, 1))),
            repository);
    }

    /**
     * Test base authorizer with authorized sample.
     */
    @Test
    @Transactional
    public void testBaseAuthorizerAuthorizedSample() {
        BaseAuthorizer baseAuthorizer = new ProbeAuthorizer(repository);
        //Test authorized sample
        assertEquals(
            baseAuthorizer.isProbeReadOnly(
                SAMPLE_ID_AUTHORIZED),
            false);
    }

    /**
     * Test base authorizer with unauthorized sample.
     */
    @Test
    @Transactional
    public void testBaseAuthorizerUnauthorizedSample() {
        BaseAuthorizer baseAuthorizer = new ProbeAuthorizer(repository);
        //Test locked sample
        assertEquals(
            "Unauthorized sample",
            baseAuthorizer.isProbeReadOnly(
                SAMPLE_ID_LOCKED_BY_STATUS),
            true);
    }

    /**
     * Test base authorizer with authorized measm.
     */
    @Test
    @Transactional
    public void testBaseAuthorizerAuthorizedMeasm() {
        BaseAuthorizer baseAuthorizer = new ProbeAuthorizer(repository);
        //Test unlocked measm
        assertEquals(
            "Unlocked measm",
            baseAuthorizer.isMessungReadOnly(
                MEASM_ID_STATUS_EDITABLE),
            false);
    }

    /**
     * Test base authorizer with locked sample.
     */
    @Test
    @Transactional
    public void testBaseAuthorizerUnauthorizedMeasm() {
        BaseAuthorizer baseAuthorizer = new ProbeAuthorizer(repository);
        //Test locked measm
        assertEquals(
            "Locked measm",
            baseAuthorizer.isMessungReadOnly(
                MEASM_ID_STATUS_LOCKED),
            true);
    }

    /**
     * Run parameterized isAuthorized tests.
     * @throws Exception Exception that may occure during run
     */
    @Test
    @Transactional
    public void runIsAuthorizedTests() throws Exception {
        processResult(JUnitCore.runClasses(IsAuthorizedTests.class),
            "isAuthorizedTests");
    }

    /**
     * Run parameterized filter tests.
     * @throws Exception Exception that may occure during run
     */
    @Test
    @Transactional
    public void runFilterTests() throws Exception {
        processResult(JUnitCore.runClasses(FilterTests.class),
            "FilterTests");
    }

    private void processResult(Result result, String testName)
        throws Exception {
        log.info(String.format(
            "%s: Tests run: %d, failed: %d, ignored: %d",
            testName,
            result.getRunCount(), result.getFailureCount(),
            result.getIgnoreCount()));
        List<Throwable> errors = new ArrayList<Throwable>();
        result.getFailures().forEach(failure -> {
            String descr = failure.getDescription().getDisplayName();
            String msg = failure.getMessage();
            String error = String.format("%s: %s", descr, msg);
            log.error(error);
            errors.add(failure.getException());
        });
        //Throw exception manually to ensure all errors are printed
        MultipleFailureException.assertEmpty(errors);
    }

    /**
     * Nested test class testing all authorizers isAuthorized function.
     */
    @RunWith(Parameterized.class)
    public static class IsAuthorizedTests {

        //Test parameters
        @Parameter(0)
        public Object testObject;
        @Parameter(1)
        public RequestMethod method;
        @Parameter(2)
        public Boolean expectedResult;

        /**
         * Get the test data list.
         * @return List of test data rows.
         */
        @Parameters(name = "[#{index}] TestObject: {0}")
        public static List<Object[]> getParameters() {
            List<Object[]> data = new ArrayList<Object[]>();
            createTestData().forEach((object, results) -> {
                List<Object[]> newEntry = List.of(
                    new Object[]{object, RequestMethod.GET,
                        results.getResult()},
                    new Object[]{object, RequestMethod.POST,
                        results.postResult()},
                    new Object[]{object, RequestMethod.PUT,
                        results.putResult()},
                    new Object[]{object, RequestMethod.DELETE,
                        results.deleteResult()}
                );
                data.addAll(newEntry);
            });
            return data;
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
    }

    /**
     * Nested test class testing all authorizers filter function.
     */
    @RunWith(Parameterized.class)
    public static class FilterTests {

        //Test parameters
        @Parameter(0)
        public Object testObject;
        @Parameter(1)
        public Boolean expectedReadonly;

        /**
         * Get the test data list for the filter test.
         *
         * The tests are using the inverted put authorization result as
         * expected readonly value.
         * @return List of test data rows.
         */
        @Parameters(name = "[#{index}] TestObject: {0}")
        public static List<Object[]> getParameters() {
            return createTestData()
                .entrySet()
                .stream()
                //Remove testObjects without readonly field
                .filter(entry -> {
                    try {
                        entry.getKey().getClass().getMethod("isReadonly");
                        return true;
                    } catch (NoSuchMethodException nsme) {
                        return false;
                    }
                })
                //Expected readonly is the inverted put authorization result
                .map(entry -> {
                    return new Object[]{
                        entry.getKey(), !entry.getValue().putResult()};
                })
                .toList();
        }

        /**
         * Test the authorizer filter method.
         * Will be skipped if no expected readonly status is given.
         * @throws SecurityException
         * @throws NoSuchMethodException
         * @throws InvocationTargetException
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         */
        @Test
        public void testFilter() throws
                IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException,
                SecurityException {
            Response response = new Response(true, 0, testObject);
            Object filtered = authorization
                .filter(response, testObject.getClass())
                .getData();
            assertEquals(expectedReadonly,
                filtered.getClass()
                .getMethod("isReadonly").invoke(filtered));
        }
    }

    /**
     * Create test data for parameterized tests.
     * @return Test data map
     */
    public static Map<Object, ExpectedResults> createTestData() {
        Map<Object, ExpectedResults> testData
            = new HashMap<Object, ExpectedResults>();
        testData.putAll(createMpgTestData());
        testData.putAll(createMpgIdTestData());
        testData.putAll(createMeasmTestData());
        testData.putAll(createMeasmIdTestData());
        testData.putAll(createNetworkTestData());
        testData.putAll(createSampleTestData());
        testData.putAll(createSampleIdTestData());
        testData.putAll(createTagTestData());
        testData.putAll(createTagLinkTestData());

        return testData;
    }

    private static Map<Object, ExpectedResults> createMpgTestData() {
        //Test mpg that should be editable
        Mpg authorized = new Mpg();
        authorized.setMeasFacilId(MEAS_FACIL_ID_06010);
        //Test mpg that should not be editable
        Mpg unauth = new Mpg();
        unauth.setMeasFacilId(MEAS_FACIL_ID_01010);

        return Map.of(
            authorized, new ExpectedResults(true, true, true, true),
            unauth, new ExpectedResults(true, false, false, false)
        );
    }

    private static Map<Object, ExpectedResults> createMpgIdTestData() {
        //Test geolocatMpg attached to editable mpg
        GeolocatMpg authorized = new GeolocatMpg();
        authorized.setMpgId(MPG_ID_AUTHORIZED);
        //Test geolocatMpg attached to non editable mpg
        GeolocatMpg unauth = new GeolocatMpg();
        unauth.setMpgId(MPG_ID_UNAUTHORIZED);

        return Map.of(
            authorized, new ExpectedResults(true, true, true, true),
            unauth, new ExpectedResults(false, false, false, false)
        );
    }

    private static Map<Object, ExpectedResults> createMeasmTestData() {
        //Test editable measm without status
        Measm noStatus = repository.getByIdPlain(
            Measm.class, MEASM_ID_NO_STATUS);
        //Test measm with editable status
        Measm editableStatus = repository.getByIdPlain(
            Measm.class, MEASM_ID_STATUS_EDITABLE);
        //Test measm locked by status
        Measm lockedByStatus = repository.getByIdPlain(
            Measm.class, MEASM_ID_STATUS_LOCKED);
        //Test measm locked by connected sample
        Measm lockedBySample = repository.getByIdPlain(
            Measm.class, MEASM_ID_LOCKED_BY_SAMPLE);

        return Map.of(
            noStatus, new ExpectedResults(true, true, true, true),
            editableStatus, new ExpectedResults(true, true, true, true),
            lockedByStatus, new ExpectedResults(true, true, false, false),
            lockedBySample, new ExpectedResults(false, false, false, false)
        );
    }

    private static Map<Object, ExpectedResults> createMeasmIdTestData() {
        //Test editable measm without status
        CommMeasm noStatus = new CommMeasm();
        noStatus.setMeasmId(MEASM_ID_NO_STATUS);
        //Test measm with editable status
        CommMeasm editableStatus = new CommMeasm();
        editableStatus.setMeasmId(MEASM_ID_STATUS_EDITABLE);
        //Test measm locked by status
        CommMeasm lockedByStatus = new CommMeasm();
        lockedByStatus.setMeasmId(MEASM_ID_STATUS_LOCKED);
        //Test measm locked by connected sample
        CommMeasm lockedBySample = new CommMeasm();
        lockedBySample.setMeasmId(MEASM_ID_LOCKED_BY_SAMPLE);

        return Map.of(
            noStatus, new ExpectedResults(false, true, true, true),
            editableStatus, new ExpectedResults(false, true, true, true),
            lockedByStatus, new ExpectedResults(false, true, true, true),
            lockedBySample, new ExpectedResults(false, false, false, false)
        );
    }

    private static Map<Object, ExpectedResults> createNetworkTestData() {
        //Test authorized sampler
        Sampler authorized = new Sampler();
        authorized.setNetworkId(NETWORK_ID_AUTHORIZED);
        //Test unauthorized sampler
        Sampler unauth = new Sampler();
        unauth.setNetworkId(NETWORK_ID_UNAUTHORIZED);
        //Test Site special handling
        //Test authorized site
        Site authorizedSite = new Site();
        authorizedSite.setNetworkId(NETWORK_ID_AUTHORIZED);
        //Test unauthorized site
        Site unauthSite = new Site();
        unauthSite.setNetworkId(NETWORK_ID_UNAUTHORIZED);

        return Map.of(
            authorized, new ExpectedResults(false, true, true, true),
            unauth, new ExpectedResults(false, false, false, false),
            authorizedSite, new ExpectedResults(false, true, true, true),
            unauthSite, new ExpectedResults(false, false, false, false)
        );
    }

    private static Map<Object, ExpectedResults> createSampleTestData() {
        //Test authorized sample
        Sample authorized = new Sample();
        authorized.setMeasFacilId(MEAS_FACIL_ID_06010);
        //Test unauthorized sample
        Sample unauthorized = new Sample();
        unauthorized.setMeasFacilId(MEAS_FACIL_ID_01010);
        //Test sample locked by measm status
        Sample statusLocked = repository.getByIdPlain(
            Sample.class, SAMPLE_ID_LOCKED_BY_STATUS);

        return Map.of(
            authorized, new ExpectedResults(true, true, true, true),
            unauthorized, new ExpectedResults(false, false, false, false),
            statusLocked, new ExpectedResults(true, true, false, false)
        );
    }

    private static Map<Object, ExpectedResults> createSampleIdTestData() {
        //Test authorized sample id
        CommSample authorized = new CommSample();
        authorized.setSampleId(SAMPLE_ID_AUTHORIZED);
        //Test unauthorized sample id
        CommSample unauthorized = new CommSample();
        unauthorized.setSampleId(SAMPLE_ID_UNAUTORIZED);
        //Test sample id locked by measm status
        CommSample statusLocked = new CommSample();
            statusLocked.setSampleId(SAMPLE_ID_LOCKED_BY_STATUS);

        return Map.of(
            authorized, new ExpectedResults(true, true, true, true),
            unauthorized, new ExpectedResults(false, false, false, false),
            statusLocked, new ExpectedResults(false, false, false, false)
        );
    }

    private static Map<Object, ExpectedResults> createTagTestData() {
        //Test that global tags are never authorized
        Tag global = new Tag();
        global.setTagType(Tag.TAG_TYPE_GLOBAL);
        //Test meas facil tag of own meas facil
        Tag authorizedMeasFacil = new Tag();
        authorizedMeasFacil.setTagType(Tag.TAG_TYPE_MST);
        authorizedMeasFacil.setMeasFacilId(MEAS_FACIL_ID_06010);
        //Test meas facil tag of other meas facil
        Tag unauthorizedMeasFacil = new Tag();
        unauthorizedMeasFacil.setTagType(Tag.TAG_TYPE_MST);
        unauthorizedMeasFacil.setMeasFacilId(MEAS_FACIL_ID_01010);
        //Test authorized network tag
        Tag authorizedNetwork = new Tag();
        authorizedNetwork.setTagType(Tag.TAG_TYPE_NETZBETREIBER);
        authorizedNetwork.setNetworkId(NETWORK_ID_AUTHORIZED);
        //Test unauthorized network tag
        Tag unauthorizedNetwork = new Tag();
        unauthorizedNetwork.setTagType(Tag.TAG_TYPE_NETZBETREIBER);
        unauthorizedNetwork.setNetworkId(NETWORK_ID_UNAUTHORIZED);

        return Map.of(
            global, new ExpectedResults(false, false, false, false),
            authorizedMeasFacil, new ExpectedResults(true, true, true, true),
            unauthorizedMeasFacil,
                new ExpectedResults(false, false, false, false),
            authorizedNetwork, new ExpectedResults(true, true, true, true),
            unauthorizedNetwork, new ExpectedResults(false, false, false, false)
        );
    }

    private static Map<Object, ExpectedResults> createTagLinkTestData() {
        //Test global tag and authorized sample
        TagLink authorizedSample = new TagLink();
        authorizedSample.setTagId(TAG_ID_GLOBAL);
        authorizedSample.setSampleId(SAMPLE_ID_AUTHORIZED);
        //Test global tag and authorized sample
        TagLink unauthorizedSample = new TagLink();
        unauthorizedSample.setTagId(TAG_ID_GLOBAL);
        unauthorizedSample.setSampleId(SAMPLE_ID_UNAUTORIZED);
        //Test global tag and authorized measm
        TagLink authorizedMeasm = new TagLink();
        authorizedMeasm.setTagId(TAG_ID_GLOBAL);
        authorizedMeasm.setMeasmId(MEASM_ID_NO_STATUS);
        //Test global tag and authorized measm
        TagLink unauthorizedMeasm = new TagLink();
        unauthorizedMeasm.setTagId(TAG_ID_GLOBAL);
        unauthorizedMeasm.setMeasmId(MEASM_ID_STATUS_LOCKED);

        return Map.of(
            authorizedSample, new ExpectedResults(false, true, false, true),
            unauthorizedSample, new ExpectedResults(false, false, false, false),
            authorizedMeasm, new ExpectedResults(false, true, false, true),
            unauthorizedMeasm, new ExpectedResults(false, false, false, false)
        );
    }
}
