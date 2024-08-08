/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import java.sql.SQLException;

import jakarta.inject.Inject;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.BaseModel;
import de.intevation.lada.validation.Validator;


/**
 * Baseclass for ValidatorTests.
 */
@RunWith(Arquillian.class)
@Transactional
public abstract class ValidatorBaseTest extends BaseTest {

    @Inject
    protected Validator validator;

    /**
     * Constructor.
     * Sets test dataset.
     */
    public ValidatorBaseTest() {
        this.testDatasetName = "datasets/dbUnit_validator.xml";
    }

    /**
     * Set up validator tests.
     * @throws SQLException
     */
    @Before
    public void setupValidatorTests() throws SQLException {
        //Refresh materialized views
        String sql = "REFRESH MATERIALIZED VIEW master.admin_border_view;";
        getConnection().getConnection().prepareStatement(sql).execute();
    }

    /**
     * Assert that the given entity has no validation messages attached.
     * @param entity Entity to check
     */
    protected void assertNoMessages(BaseModel entity) {
        Assert.assertFalse(
            "Unexpected validation errors: "
                + entity.getErrors(),
            entity.hasErrors());
        Assert.assertFalse(
            "Unexpected validation warnings: "
                + entity.getWarnings(),
            entity.hasWarnings());
        Assert.assertFalse(
            "Unexpected validation notifications: "
                + entity.getNotifications(),
            entity.hasNotifications());
    }

    /**
     * Assert that the given entity has errors attached.
     * @param entity Entity to check
     */
    protected void assertHasErrors(BaseModel entity) {
        Assert.assertTrue("Expected errors missing", entity.hasErrors());
    }

    /**
     * Assert that the given entity has the error with given key and value
     * attached.
     * @param entity Entity to check
     * @param key Expected error key
     * @param val Expected error values
     */
    protected void assertHasErrors(
        BaseModel entity, String key, String... val
    ) {
        assertHasErrors(entity);
        MatcherAssert.assertThat(entity.getErrors().keySet(),
            CoreMatchers.hasItem(key));
        MatcherAssert.assertThat(entity.getErrors().get(key),
            CoreMatchers.hasItems(val));
    }

    /**
     * Assert that the given entity has the warning with given key and value
     * attached.
     * @param entity Entity to check
     * @param key Expected warning key
     * @param val Expected warning values
     */
    protected void assertHasWarnings(
        BaseModel entity, String key, String... val
    ) {
        Assert.assertTrue("Expected warnings missing", entity.hasWarnings());
        MatcherAssert.assertThat(entity.getWarnings().keySet(),
            CoreMatchers.hasItem(key));
        MatcherAssert.assertThat(entity.getWarnings().get(key),
            CoreMatchers.hasItems(val));
    }

    /**
     * Assert that the given entity has the notification with given key and
     * value attached.
     * @param entity Entity to check
     * @param key Expected notification key
     * @param val Expected notification values
     */
    protected void assertHasNotifications(
        BaseModel entity, String key, String... val
    ) {
        Assert.assertTrue(
            "Expected notifications missing", entity.hasNotifications());
        MatcherAssert.assertThat(entity.getNotifications().keySet(),
            CoreMatchers.hasItem(key));
        MatcherAssert.assertThat(entity.getNotifications().get(key),
            CoreMatchers.hasItems(val));
    }
}
