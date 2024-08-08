/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import org.junit.Test;

import de.intevation.lada.model.master.Tag;


/**
 * Test validation rules for Tag objects.
 */
public class TagTest extends ValidatorBaseTest {

    private static final String MEAS_FACIL = "06010";
    private static final String NETWORK_ID = "06";

    /**
     * Test tag with Network and MeasFacil.
     */
    @Test
    public void networkAndMeasFacil() {
        Tag tag = createMinimumValidTag();
        tag.setMeasFacilId(MEAS_FACIL);
        tag.setNetworkId(NETWORK_ID);

        validator.validate(tag);
        final String msg = "Either networkId or measFacilId can be given";
        assertHasErrors(tag, "networkId", msg);
        assertHasErrors(tag, "measFacilId", msg);
    }

    /**
     * New unique measFacil tag.
     */
    @Test
    public void uniqueMeasFacilTag() {
        Tag tag = createMinimumValidTag();
        tag.setMeasFacilId(MEAS_FACIL);
        tag.setName("new");
        assertNoMessages(validator.validate(tag));
    }

    /**
     * Non-unique measFacil tag.
     */
    @Test
    public void nonUniqueMeasFacilTag() {
        Tag tag = createMinimumValidTag();
        tag.setMeasFacilId(MEAS_FACIL);
        tag.setName("mst");
        assertHasErrors(
            validator.validate(tag),
            "name",
            "Non-unique value combination for [name, measFacilId]");
    }

    /**
     * Non-unique network tag.
     */
    @Test
    public void nonUniqueNetworkTag() {
        Tag tag = createMinimumValidTag();
        tag.setNetworkId(NETWORK_ID);
        assertHasErrors(
            validator.validate(tag),
            "name",
            "Non-unique value combination for [name, networkId]");
    }

    /**
     * New unique global tag.
     */
    @Test
    public void uniqueGlobalTag() {
        Tag tag = createMinimumValidTag();
        tag.setName("new");
        assertNoMessages(validator.validate(tag));
    }

    /**
     * Non-unique global tag.
     */
    @Test
    public void nonUniqueGlobalTag() {
        Tag tag = createMinimumValidTag();
        assertHasErrors(
            validator.validate(tag),
            "name",
            "Non-unique name for global tag");
    }

    /**
     * Create tag with a minimum set of fields to be validated.
     * @return Tag.
     */
    private Tag createMinimumValidTag() {
        Tag tag = new Tag();
        tag.setName("test");
        return tag;
    }
}
