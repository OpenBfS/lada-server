/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;

import de.intevation.lada.model.master.Tag;
import de.intevation.lada.validation.Validator;


/**
 * Test validation rules for Tag objects.
 */
@Transactional
public class TagTest extends ValidatorBaseTest {

    private static final String MEAS_FACIL = "06010";

    @Inject
    private Validator<Tag> validator;

    /**
     * Test tag with matching Network and MeasFacil.
     */
    public void matchingNetworkMeasFacil() {
        Tag tag = createMinimumValidTag();
        tag.setMeasFacilId(MEAS_FACIL);
        tag.setNetworkId("06");

        validator.validate(tag);
        assertNoWarningsOrErrors(tag);
    }

    /**
     * Test tag with not matching Network and MeasFacil.
     */
    public void notMatchingNetworkMeasFacil() {
        Tag tag = createMinimumValidTag();
        tag.setMeasFacilId(MEAS_FACIL);
        tag.setNetworkId("07");

        validator.validate(tag);
        assertHasErrors(tag);
        final String measFacilIdKey = "measFacilId",
            networkIdKey = "networkId",
            expectedError = "Values not matching";
        MatcherAssert.assertThat(tag.getErrors().keySet(),
            CoreMatchers.hasItems(measFacilIdKey, networkIdKey));
        MatcherAssert.assertThat(tag.getErrors().get(measFacilIdKey),
            CoreMatchers.hasItem(expectedError));
        MatcherAssert.assertThat(tag.getErrors().get(networkIdKey),
            CoreMatchers.hasItem(expectedError));
    }

    /**
     * Create tag with a minimum set of fields to be validated.
     * @return Tag.
     */
    private Tag createMinimumValidTag() {
        Tag tag = new Tag();
        tag.setName("test");
        tag.setTagType("mst");
        return tag;
    }
}
