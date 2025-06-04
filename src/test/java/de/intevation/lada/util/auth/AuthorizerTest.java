/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

package de.intevation.lada.util.auth;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.junit.Arquillian;
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
import de.intevation.lada.i18n.I18n;
import de.intevation.lada.model.BaseModel;
import de.intevation.lada.model.lada.CommMeasm;
import de.intevation.lada.model.lada.CommSample;
import de.intevation.lada.model.lada.GeolocatMpg;
import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.model.lada.Mpg;
import de.intevation.lada.model.lada.Sample;
import de.intevation.lada.model.lada.StatusProt;
import de.intevation.lada.model.lada.TagLinkMeasm;
import de.intevation.lada.model.lada.TagLinkSample;
import de.intevation.lada.model.master.Auth;
import de.intevation.lada.model.master.DatasetCreator;
import de.intevation.lada.model.master.Sampler;
import de.intevation.lada.model.master.Site;
import de.intevation.lada.model.master.Tag;
import de.intevation.lada.util.data.Repository;
import de.intevation.lada.util.rest.RequestMethod;

import jakarta.inject.Inject;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;


/**
 * Class testing authorizers.
 */
@RunWith(Arquillian.class)
public class AuthorizerTest extends BaseTest {

    private Logger log = Logger.getLogger(AuthorizerTest.class);

    private static Authorization authorization;

    private static I18n i18n;

    private static Repository repository;

    private static UserTransaction transaction;

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
    public record TestConfig(
        boolean getResult, boolean postResult,
        boolean putResult, boolean deleteResult,
        String testDescription) { }

    /**
     * Test constructor.
     */
    public AuthorizerTest() {
        testDatasetName = "datasets/dbUnit_authorizer.xml";
    }

    /**
     * Initialize static instance variables.
     *
     * @param repo Repository
     * @param localizer I18n
     * @param tx transaction
     */
    @Inject
    private void init(Repository repo, I18n localizer, UserTransaction tx) {
        repository = repo;
        i18n = localizer;
        transaction = tx;
    }

    /**
     * Init authorizer.
     */
    @Before
    public void initAuthorization() throws
        NotSupportedException, SystemException {
        try {
            final int userId = 2;
            transaction.begin();
            authorization = new Authorization(
                new UserInfo(
                    testUser,
                    userId,
                    repository.getAll(Auth.class)),
                i18n,
                repository);
        } finally {
            transaction.rollback();
        }
    }

    /**
     * Run parameterized isAuthorized tests.
     * @throws Exception Exception that may occure during run
     */
    @Test
    public void runIsAuthorizedTests() throws Exception {
        processResult(JUnitCore.runClasses(IsAuthorizedTests.class),
            "isAuthorizedTests");
    }

    /**
     * Run parameterized filter tests.
     * @throws Exception Exception that may occure during run
     */
    @Test
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
            // Print stack trace to server log for debugging
            failure.getException().printStackTrace();
            errors.add(new Throwable(error, failure.getException()));
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
        public BaseModel testObject;
        @Parameter(1)
        public RequestMethod method;
        @Parameter(2)
        public Boolean expectedResult;
        @Parameter(3)
        public String testDescription;

        /**
         * Get the test data list.
         * @return List of test data rows.
         */
        @Parameters(name =
            "Test: {3}, Method: {1}, TestObject: {0}")
        public static List<Object[]> getParameters() {
            List<Object[]> data = new ArrayList<Object[]>();
            createTestData().forEach((object, testConfig) -> {
                String testDescr = testConfig.testDescription();
                List<Object[]> newEntry = List.of(
                    new Object[]{object, RequestMethod.GET,
                        testConfig.getResult(), testDescr},
                    new Object[]{object, RequestMethod.POST,
                        testConfig.postResult(), testDescr},
                    new Object[]{object, RequestMethod.PUT,
                        testConfig.putResult(), testDescr},
                    new Object[]{object, RequestMethod.DELETE,
                        testConfig.deleteResult(), testDescr}
                );
                data.addAll(newEntry);
            });
            return data;
        }

        /**
         * Test the authorizers isAuthorized method.
         */
        @Test
        public void testIsAuthorized() throws
            NotSupportedException, SystemException {
            try {
                transaction.begin();
                assertEquals(expectedResult,
                    authorization.isAuthorized(testObject, method));
            } finally {
                transaction.rollback();
            }
        }
    }

    /**
     * Nested test class testing all authorizers filter function.
     */
    @RunWith(Parameterized.class)
    public static class FilterTests {

        //Test parameters
        @Parameter(0)
        public BaseModel testObject;
        @Parameter(1)
        public Boolean expectedReadonly;
        @Parameter(2)
        public String testDescr;

        /**
         * Get the test data list for the filter test.
         *
         * The tests are using the inverted put authorization result as
         * expected readonly value.
         * @return List of test data rows.
         */
        @Parameters(name = "Test: {2}")
        public static List<Object[]> getParameters() {
            return createTestData()
                .entrySet()
                .stream()
                //Expected readonly is the inverted put authorization result
                .map(entry -> {
                    return new Object[]{
                        entry.getKey(), !entry.getValue().putResult(),
                        entry.getValue().testDescription()};
                })
                .toList();
        }

        /**
         * Test the authorizer filter method.
         * Will be skipped if no expected readonly status is given.
         */
        @Test
        public void testFilter() throws
            NotSupportedException, SystemException {
            try {
                transaction.begin();
                assertEquals(expectedReadonly,
                    authorization.filter(testObject).isReadonly());
            } finally {
                transaction.rollback();
            }
        }
    }

    /**
     * Create test data for parameterized tests.
     * @return Test data map
     */
    public static Map<Object, TestConfig> createTestData() {
        Map<Object, TestConfig> testData
            = new HashMap<Object, TestConfig>();
        testData.putAll(createMpgTestData());
        testData.putAll(createMpgIdTestData());
        testData.putAll(createMeasmTestData());
        testData.putAll(createMeasmIdTestData());
        testData.putAll(createStatusTestData());
        testData.putAll(createNetworkTestData());
        testData.putAll(createSampleTestData());
        testData.putAll(createSampleIdTestData());
        testData.putAll(createTagTestData());
        testData.putAll(createTagLinkTestData());

        return testData;
    }

    private static Map<Object, TestConfig> createMpgTestData() {
        //Test mpg that should be editable
        Mpg authorized = new Mpg();
        authorized.setMeasFacilId(MEAS_FACIL_ID_06010);
        //Test mpg that should not be editable
        Mpg unauth = new Mpg();
        unauth.setMeasFacilId(MEAS_FACIL_ID_01010);

        return Map.of(
            authorized, new TestConfig(true, true, true, true, "mpgAuthorized"),
            unauth, new TestConfig(true, false, false, false, "mpgUnauthorzed")
        );
    }

    private static Map<Object, TestConfig> createMpgIdTestData() {
        //Test geolocatMpg attached to editable mpg
        GeolocatMpg authorized = new GeolocatMpg();
        authorized.setMpgId(MPG_ID_AUTHORIZED);
        //Test geolocatMpg attached to non editable mpg
        GeolocatMpg unauth = new GeolocatMpg();
        unauth.setMpgId(MPG_ID_UNAUTHORIZED);

        return Map.of(
            authorized, new TestConfig(true, true, true, true,
                "mpgIdAuthorized"),
            unauth, new TestConfig(false, false, false, false,
                "mpgIdUnauthorized")
        );
    }

    private static Map<Object, TestConfig> createMeasmTestData() {
        //Test editable measm without status
        Measm noStatus = repository.entityManager().find(
            Measm.class, MEASM_ID_NO_STATUS);
        //Test measm with editable status
        Measm editableStatus = repository.entityManager().find(
            Measm.class, MEASM_ID_STATUS_EDITABLE);
        //Test measm locked by status
        Measm lockedByStatus = repository.entityManager().find(
            Measm.class, MEASM_ID_STATUS_LOCKED);
        //Test measm locked by connected sample
        Measm lockedBySample = repository.entityManager().find(
            Measm.class, MEASM_ID_LOCKED_BY_SAMPLE);

        return Map.of(
            noStatus, new TestConfig(true, true, true, true,
                "measmNoStatus"),
            editableStatus, new TestConfig(true, true, true, true,
                "measmEditableStatus"),
            lockedByStatus, new TestConfig(true, true, false, false,
                "measmLockedByStatus"),
            lockedBySample, new TestConfig(false, false, false, false,
                "measmLockedBySample")
        );
    }

    private static Map<Object, TestConfig> createMeasmIdTestData() {
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
            noStatus, new TestConfig(true, true, true, true,
                "measmIdNoStatus"),
            editableStatus, new TestConfig(true, true, true, true,
                "measmIdEditableStatus"),
            lockedByStatus, new TestConfig(true, false, false, false,
                "measmIdLockedByStatus"),
            lockedBySample, new TestConfig(false, false, false, false,
                "measmIDLockedBySample")
        );
    }

    private static Map<Object, TestConfig> createStatusTestData() {
        return Map.of(
            // Status of associated Measm instance can be set
            newStatusProt(MEASM_ID_STATUS_EDITABLE),
            new TestConfig(true, true, false, false, "statusEditableStatus"),

            // Associated Measm instance is read-only due to status
            newStatusProt(MEASM_ID_STATUS_LOCKED),
            new TestConfig(true, false, false, false, "statusReadonlyByStatus"),

            // Associated Measm instance belongs to foreign network and cannot
            // be read due to status
            newStatusProt(MEASM_ID_LOCKED_BY_SAMPLE),
            new TestConfig(false, false, false, false, "statusLockedBySample")
        );
    }

    private static StatusProt newStatusProt(Integer measmId) {
        StatusProt status = new StatusProt();
        status.setMeasmId(measmId);
        status.setStatusMpId(2);
        status.setMeasFacilId("06010");
        return status;
    }

    private static Map<Object, TestConfig> createNetworkTestData() {
        DatasetCreator authorized = new DatasetCreator();
        authorized.setNetworkId(NETWORK_ID_AUTHORIZED);
        DatasetCreator unauth = new DatasetCreator();
        unauth.setNetworkId(NETWORK_ID_UNAUTHORIZED);

        //Test authorized sampler
        Sampler authorizedSampler = new Sampler();
        authorizedSampler.setNetworkId(NETWORK_ID_AUTHORIZED);
        //Test unauthorized sampler
        Sampler unauthSampler = new Sampler();
        unauthSampler.setNetworkId(NETWORK_ID_UNAUTHORIZED);

        //Test Site special handling
        //Test authorized site
        Site authorizedSite = new Site();
        authorizedSite.setNetworkId(NETWORK_ID_AUTHORIZED);
        //Test unauthorized site
        Site unauthSite = new Site();
        unauthSite.setNetworkId(NETWORK_ID_UNAUTHORIZED);

        return Map.of(
            authorized, new TestConfig(true, true, true, true,
                 "networkAuthorizer"),
            unauth, new TestConfig(false, false, false, false,
                "networkUnauthorized"),
            authorizedSampler, new TestConfig(true, true, true, true,
                 "authorizedSampler"),
            unauthSampler, new TestConfig(false, false, false, false,
                "unauthorizedSampler"),
            authorizedSite, new TestConfig(true, true, true, true,
                "authorizedSite"),
            unauthSite, new TestConfig(true, false, false, false,
                "unauthorizedSite")
        );
    }

    private static Map<Object, TestConfig> createSampleTestData() {
        //Test authorized sample
        Sample authorized = repository.entityManager().find(
            Sample.class, SAMPLE_ID_AUTHORIZED);
        //Test unauthorized sample
        Sample unauthorized = repository.entityManager().find(
            Sample.class, SAMPLE_ID_UNAUTORIZED);
        //Test sample locked by measm status
        Sample statusLocked = repository.entityManager().find(
            Sample.class, SAMPLE_ID_LOCKED_BY_STATUS);

        return Map.of(
            authorized, new TestConfig(true, true, true, true,
                "sampleAuthorized"),
            unauthorized, new TestConfig(false, false, false, false,
                "sampleUnauthorized"),
            statusLocked, new TestConfig(true, true, false, false,
                "sampleStatusLocked")
        );
    }

    private static Map<Object, TestConfig> createSampleIdTestData() {
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
            authorized, new TestConfig(true, true, true, true,
                "sampleIdAuthorized"),
            unauthorized, new TestConfig(false, false, false, false,
                "sampleIdUnauthorized"),
            statusLocked, new TestConfig(false, false, false, false,
                "sampleIdStatusLocked")
        );
    }

    private static Map<Object, TestConfig> createTagTestData() {
        //Test that global tags are never authorized
        Tag global = new Tag();
        //Test meas facil tag of own meas facil
        Tag authorizedMeasFacil = new Tag();
        authorizedMeasFacil.setMeasFacilId(MEAS_FACIL_ID_06010);
        //Test meas facil tag of other meas facil
        Tag unauthorizedMeasFacil = new Tag();
        unauthorizedMeasFacil.setMeasFacilId(MEAS_FACIL_ID_01010);
        //Test authorized network tag
        Tag authorizedNetwork = new Tag();
        authorizedNetwork.setNetworkId(NETWORK_ID_AUTHORIZED);
        //Test unauthorized network tag
        Tag unauthorizedNetwork = new Tag();
        unauthorizedNetwork.setNetworkId(NETWORK_ID_UNAUTHORIZED);

        return Map.of(
            global, new TestConfig(false, false, false, false,
                "tabGlobasl"),
            authorizedMeasFacil, new TestConfig(true, true, true, true,
                "tabAuthorizedMeasFacil"),
            unauthorizedMeasFacil,
                new TestConfig(false, false, false, false,
                "tagUnauthorizedMeasFacil"),
            authorizedNetwork, new TestConfig(true, true, true, true,
                "tagAuthorizedNetwork"),
            unauthorizedNetwork, new TestConfig(false, false, false, false,
                "tagUnauthorizedNetwork")
        );
    }

    private static Map<Object, TestConfig> createTagLinkTestData() {
        //Test global tag and authorized sample
        TagLinkSample authorizedSample = new TagLinkSample();
        authorizedSample.setTagId(TAG_ID_GLOBAL);
        authorizedSample.setSampleId(SAMPLE_ID_AUTHORIZED);
        //Test global tag and authorized sample
        TagLinkSample unauthorizedSample = new TagLinkSample();
        unauthorizedSample.setTagId(TAG_ID_GLOBAL);
        unauthorizedSample.setSampleId(SAMPLE_ID_UNAUTORIZED);
        //Test global tag and authorized measm
        TagLinkMeasm authorizedMeasm = new TagLinkMeasm();
        authorizedMeasm.setTagId(TAG_ID_GLOBAL);
        authorizedMeasm.setMeasmId(MEASM_ID_NO_STATUS);
        //Test global tag and authorized measm
        TagLinkMeasm unauthorizedMeasm = new TagLinkMeasm();
        unauthorizedMeasm.setTagId(TAG_ID_GLOBAL);
        unauthorizedMeasm.setMeasmId(MEASM_ID_STATUS_LOCKED);

        return Map.of(
            authorizedSample, new TestConfig(false, true, false, true,
                "tagLinkAuthorizedSample"),
            unauthorizedSample, new TestConfig(false, false, false, false,
                "tagLinkUnauthorizedSample"),
            authorizedMeasm, new TestConfig(false, true, false, true,
                "tagLinkAuthorizedMeasm"),
            unauthorizedMeasm, new TestConfig(false, false, false, false,
                "tagLinkUnauthorizedMeasm")
        );
    }
}
