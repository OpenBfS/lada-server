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
import jakarta.persistence.EntityManager;
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

    private static EntityManager em;

    private static UserTransaction transaction;

    //Constants
    private static final String NETWORK_ID_AUTHORIZED = "06";
    private static final int MEASM_ID_LOCKED_BY_SAMPLE = 1201;
    private static final int MEASM_ID_STATUS_LOCKED = 1212;
    private static final int MEASM_ID_STATUS_EDITABLE = 1209;
    private static final int MEASM_ID_NO_STATUS = 1200;
    private static final int SAMPLE_ID_AUTHORIZED = 1099;
    private static final int SAMPLE_ID_UNAUTORIZED = 1001;
    private static final int SAMPLE_ID_LOCKED_BY_STATUS = 1100;
    private static final int MPG_ID_AUTHORIZED = 1000;
    private static final String MEAS_FACIL_AUTHORIZED = "06010";

    //Record used to store expected test results in.
    public record TestConfig(
        boolean getResult, boolean postResult,
        boolean putResult, boolean deleteResult,
        // Whether BaseModel.readonly can be derived from putResult
        boolean readOnlyViaPut,
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
        em = repo.entityManager();
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
                .filter(entry -> entry.getValue().readOnlyViaPut())
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
        testData.putAll(createSiteTestData());
        testData.putAll(createSampleTestData());
        testData.putAll(createSampleIdTestData());
        testData.putAll(createTagTestData());
        testData.putAll(createTagLinkTestData());

        return testData;
    }

    private static Map<Object, TestConfig> createMpgTestData() {
        //Test mpg that should be editable
        final int authorizedId = 1000;
        Mpg authorized = em.find(Mpg.class, authorizedId);
        //Test mpg that should not be editable
        final int unauthId = 1001;
        Mpg unauth = em.find(Mpg.class, unauthId);
        Mpg hijacked = em.find(Mpg.class, unauthId);
        hijacked.setMeasFacilId(MEAS_FACIL_AUTHORIZED);

        return Map.of(
            authorized, new TestConfig(true, true, true, true, true,
                "mpgAuthorized"),
            unauth, new TestConfig(true, false, false, false, true,
                "mpgUnauthorzed"),
            hijacked, new TestConfig(true, true, false, true, false,
                "mpgHijacked")
        );
    }

    private static Map<Object, TestConfig> createMpgIdTestData() {
        //Test geolocatMpg attached to editable mpg
        final int authId = 1000;
        GeolocatMpg authorized = em.find(GeolocatMpg.class, authId);
        //Test geolocatMpg attached to non editable mpg
        final int unauthId = 1001;
        GeolocatMpg unauth = em.find(GeolocatMpg.class, unauthId);
        GeolocatMpg hijacked = em.find(GeolocatMpg.class, unauthId);
        hijacked.setMpgId(MPG_ID_AUTHORIZED);

        return Map.of(
            authorized, new TestConfig(true, true, true, true, true,
                "mpgIdAuthorized"),
            unauth, new TestConfig(false, false, false, false, true,
                "mpgIdUnauthorized"),
            hijacked, new TestConfig(true, true, false, true, false,
                "belongsToMpgHijacked")
        );
    }

    private static Map<Object, TestConfig> createMeasmTestData() {
        //Test editable measm without status
        Measm noStatus = em.find(
            Measm.class, MEASM_ID_NO_STATUS);
        //Test measm with editable status
        Measm editableStatus = em.find(
            Measm.class, MEASM_ID_STATUS_EDITABLE);
        //Test measm locked by status
        Measm lockedByStatus = em.find(
            Measm.class, MEASM_ID_STATUS_LOCKED);
        //Test measm locked by connected sample
        Measm lockedBySample = em.find(
            Measm.class, MEASM_ID_LOCKED_BY_SAMPLE);
        Measm hijackedBySample = em.find(
            Measm.class, MEASM_ID_LOCKED_BY_SAMPLE);
        hijackedBySample.setSample(
            em.find(Sample.class, SAMPLE_ID_AUTHORIZED));

        return Map.of(
            noStatus, new TestConfig(true, true, true, true, true,
                "measmNoStatus"),
            editableStatus, new TestConfig(true, true, true, true, true,
                "measmEditableStatus"),
            lockedByStatus, new TestConfig(true, true, false, false, true,
                "measmLockedByStatus"),
            lockedBySample, new TestConfig(false, false, false, false, true,
                "measmLockedBySample"),
            hijackedBySample, new TestConfig(true, true, false, true, false,
                "measmHijackedBySample")
        );
    }

    private static Map<Object, TestConfig> createMeasmIdTestData() {
        //Test editable measm without status
        final int noStatusId = 1000;
        CommMeasm noStatus = em.find(CommMeasm.class, noStatusId);
        //Test measm with editable status
        final int editableId = 1001;
        CommMeasm editableStatus = em.find(CommMeasm.class, editableId);
        //Test measm locked by status
        final int lockedByStatusId = 1002;
        CommMeasm lockedByStatus = em.find(CommMeasm.class, lockedByStatusId);
        //Test measm locked by connected sample
        final int lockedBySampleId = 1003;
        CommMeasm lockedBySample = em.find(CommMeasm.class, lockedBySampleId);
        CommMeasm hijacked = em.find(CommMeasm.class, lockedBySampleId);
        final int authorizedMeasmId = 1200;
        hijacked.setMeasm(em.find(Measm.class, authorizedMeasmId));

        return Map.of(
            noStatus, new TestConfig(true, true, true, true, true,
                "measmIdNoStatus"),
            editableStatus, new TestConfig(true, true, true, true, true,
                "measmIdEditableStatus"),
            lockedByStatus, new TestConfig(true, false, false, false, true,
                "measmIdLockedByStatus"),
            lockedBySample, new TestConfig(false, false, false, false, true,
                "measmIDLockedBySample"),
            hijacked, new TestConfig(true, true, false, true, false,
                "belongsToMeasmHijacked")
        );
    }

    private static Map<Object, TestConfig> createStatusTestData() {
        return Map.of(
            // Status of associated Measm instance can be set
            newStatusProt(MEASM_ID_STATUS_EDITABLE),
            new TestConfig(true, true, false, false, true,
                "statusEditableStatus"),

            // Associated Measm instance is read-only due to status
            newStatusProt(MEASM_ID_STATUS_LOCKED),
            new TestConfig(true, false, false, false, true,
                "statusReadonlyByStatus"),

            // Associated Measm instance belongs to foreign network and cannot
            // be read due to status
            newStatusProt(MEASM_ID_LOCKED_BY_SAMPLE),
            new TestConfig(false, false, false, false, true,
                "statusLockedBySample")
        );
    }

    private static StatusProt newStatusProt(Integer measmId) {
        final int statusProtId = 1000;
        StatusProt status = em.find(StatusProt.class, statusProtId);
        status.setMeasm(em.find(Measm.class, measmId));
        status.setStatusMpId(2);
        status.setMeasFacilId(MEAS_FACIL_AUTHORIZED);
        return status;
    }

    private static Map<Object, TestConfig> createNetworkTestData() {
        final int authorizedId = 1000;
        DatasetCreator authorized = em.find(DatasetCreator.class, authorizedId);
        final int unauthId = 1001;
        DatasetCreator unauth = em.find(DatasetCreator.class, unauthId);
        DatasetCreator hijackedCreator =
            em.find(DatasetCreator.class, unauthId);
        hijackedCreator.setNetworkId(NETWORK_ID_AUTHORIZED);

        //Test authorized sampler
        final int linkedSamplerId = 726;
        Sampler authorizedLinkedSampler = em.find(
            Sampler.class, linkedSamplerId);
        final int samplerId = 727;
        Sampler authorizedSampler = em.find(Sampler.class, samplerId);
        //Test unauthorized sampler
        final int unauthSamplerId = 728;
        Sampler unauthSampler = em.find(Sampler.class, unauthSamplerId);
        Sampler hijackedSampler = em.find(Sampler.class, unauthSamplerId);
        hijackedSampler.setNetworkId(NETWORK_ID_AUTHORIZED);

        return Map.of(
            authorized, new TestConfig(true, true, true, true, true,
                 "networkAuthorizer"),
            unauth, new TestConfig(false, false, false, false, true,
                "networkUnauthorized"),
            hijackedCreator, new TestConfig(true, true, false, true, false,
                "belongsToNetworkHijacked"),
            authorizedLinkedSampler, new TestConfig(true, true, true, false, true,
                 "authorizedLinkedSampler"),
            authorizedSampler, new TestConfig(true, true, true, true, true,
                 "authorizedSampler"),
            unauthSampler, new TestConfig(false, false, false, false, true,
                "unauthorizedSampler"),
            hijackedSampler, new TestConfig(true, true, false, true, false,
                 "hijackedSampler")
        );
    }

    private static Map<Object, TestConfig> createSiteTestData() {
        //Test authorized site
        final int linkedSiteId = 1000;
        Site authorizedLinkedSite = em.find(Site.class, linkedSiteId);
        final int siteId = 1001;
        Site authorizedSite = em.find(Site.class, siteId);
        //Test unauthorized site
        final int unauthSiteId = 1002;
        Site unauthSite = em.find(Site.class, unauthSiteId);
        Site hijackedSite = em.find(Site.class, unauthSiteId);
        hijackedSite.setNetworkId(NETWORK_ID_AUTHORIZED);

        return Map.of(
            authorizedLinkedSite, new TestConfig(true, true, true, false, true,
                "authorizedLinkedSite"),
            authorizedSite, new TestConfig(true, true, true, true, true,
                "authorizedSite"),
            unauthSite, new TestConfig(true, false, false, false, true,
                "unauthorizedSite"),
            hijackedSite, new TestConfig(true, true, false, true, false,
                "hijackedSite")
        );
    }

    private static Map<Object, TestConfig> createSampleTestData() {
        //Test authorized sample
        Sample authorized = em.find(Sample.class, SAMPLE_ID_AUTHORIZED);
        //Test unauthorized sample
        Sample unauthorized = em.find(Sample.class, SAMPLE_ID_UNAUTORIZED);
        final int unauthNoMeasmsId = 1002;
        Sample hijacked = em.find(Sample.class, unauthNoMeasmsId);
        hijacked.setMeasFacilId(MEAS_FACIL_AUTHORIZED);
        //Test sample locked by measm status
        Sample statusLocked = em.find(
            Sample.class, SAMPLE_ID_LOCKED_BY_STATUS);

        return Map.of(
            authorized, new TestConfig(true, true, true, true, true,
                "sampleAuthorized"),
            unauthorized, new TestConfig(false, false, false, false, true,
                "sampleUnauthorized"),
            hijacked, new TestConfig(true, true, false, true, false,
                "sampleHijacked"),
            statusLocked, new TestConfig(true, true, false, false, true,
                "sampleStatusLocked")
        );
    }

    private static Map<Object, TestConfig> createSampleIdTestData() {
        //Test authorized sample id
        final int commSampleId = 1000;
        CommSample authorized = em.find(CommSample.class, commSampleId);
        //Test unauthorized sample id
        final int unauthId = 1001;
        CommSample unauthorized = em.find(CommSample.class, unauthId);
        CommSample hijacked = em.find(CommSample.class, unauthId);
        hijacked.setSample(em.find(Sample.class, SAMPLE_ID_AUTHORIZED));
        //Test sample id locked by measm status
        CommSample statusLocked = em.find(CommSample.class, commSampleId);
        statusLocked.setSample(em.find(Sample.class, SAMPLE_ID_LOCKED_BY_STATUS));

        return Map.of(
            authorized, new TestConfig(true, true, true, true, true,
                "sampleIdAuthorized"),
            unauthorized, new TestConfig(false, false, false, false, true,
                "sampleIdUnauthorized"),
            hijacked, new TestConfig(true, true, false, true, false,
                "belongsToSampleHijacked"),
            statusLocked, new TestConfig(false, false, false, false, true,
                "sampleIdStatusLocked")
        );
    }

    private static Map<Object, TestConfig> createTagTestData() {
        //Test that global tags are never authorized
        final int globalTagId = 103;
        Tag global = em.find(Tag.class, globalTagId);
        //Test meas facil tag of own meas facil
        final int mstTagId = 101;
        Tag authorizedMeasFacil = em.find(Tag.class, mstTagId);
        //Test meas facil tag of other meas facil
        final int mstTagUnauthId = 111;
        Tag unauthorizedMeasFacil = em.find(Tag.class, mstTagUnauthId);
        Tag hijackedByMeasFacil = em.find(Tag.class, mstTagUnauthId);
        hijackedByMeasFacil.setMeasFacilId(MEAS_FACIL_AUTHORIZED);
        //Test authorized network tag
        final int networkTagId = 102;
        Tag authorizedNetwork = em.find(Tag.class, networkTagId);
        //Test unauthorized network tag
        final int networkTagUnauthId = 112;
        Tag unauthorizedNetwork = em.find(Tag.class, networkTagUnauthId);
        Tag hijackedByNetwork = em.find(Tag.class, networkTagUnauthId);
        hijackedByNetwork.setNetworkId(NETWORK_ID_AUTHORIZED);

        return Map.of(
            global, new TestConfig(false, false, false, false, true,
                "tagGlobal"),
            authorizedMeasFacil, new TestConfig(true, true, true, true, true,
                "tagAuthorizedMeasFacil"),
            unauthorizedMeasFacil,
                new TestConfig(false, false, false, false, true,
                "tagUnauthorizedMeasFacil"),
            hijackedByMeasFacil, new TestConfig(true, true, false, true, false,
                "tagHijackedByMeasFacil"),
            authorizedNetwork, new TestConfig(true, true, true, true, true,
                "tagAuthorizedNetwork"),
            unauthorizedNetwork, new TestConfig(false, false, false, false, true,
                "tagUnauthorizedNetwork"),
            hijackedByNetwork, new TestConfig(true, true, false, true, false,
                "tagHijackedByNetwork")
        );
    }

    private static Map<Object, TestConfig> createTagLinkTestData() {
        final int authorizedTagLinkId = 1;
        //Test global tag and authorized sample
        TagLinkSample authorizedSample = em.find(
            TagLinkSample.class, authorizedTagLinkId);
        //Test global tag and authorized sample
        TagLinkSample unauthorizedSample = em.find(
            TagLinkSample.class, authorizedTagLinkId);
        unauthorizedSample.setSampleId(SAMPLE_ID_UNAUTORIZED);
        //Test global tag and authorized measm
        TagLinkMeasm authorizedMeasm = em.find(
            TagLinkMeasm.class, authorizedTagLinkId);
        authorizedMeasm.setMeasmId(MEASM_ID_NO_STATUS);
        //Test global tag and authorized measm
        TagLinkMeasm unauthorizedMeasm = em.find(
            TagLinkMeasm.class, authorizedTagLinkId);
        unauthorizedMeasm.setMeasmId(MEASM_ID_STATUS_LOCKED);

        return Map.of(
            authorizedSample, new TestConfig(false, true, false, true, true,
                "tagLinkAuthorizedSample"),
            unauthorizedSample, new TestConfig(false, false, false, false, true,
                "tagLinkUnauthorizedSample"),
            authorizedMeasm, new TestConfig(false, true, false, true, true,
                "tagLinkAuthorizedMeasm"),
            unauthorizedMeasm, new TestConfig(false, false, false, false, true,
                "tagLinkUnauthorizedMeasm")
        );
    }
}
