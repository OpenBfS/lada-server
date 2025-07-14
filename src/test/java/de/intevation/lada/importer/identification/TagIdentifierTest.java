/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer.identification;

import jakarta.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.BaseTest;
import de.intevation.lada.model.master.Tag;


/**
 * Unit tests for tag identifier implementation.
 */
@RunWith(Arquillian.class)
public class TagIdentifierTest extends BaseTest {

    private final String globalName = "global";
    private final String networkName = "network";
    private final String measFacilName = "measFacil";
    private final String networkId = "06";
    private final String mstId = "06010";

    @Inject
    private Identifier<Tag> identifier;

    public TagIdentifierTest() {
        testDatasetName = "datasets/dbUnit_identifier_tag.xml";
    }

    /**
     * Existing global tag.
     */
    @Test
    @Transactional
    public final void global() throws IdentificationException {
        Tag global = new Tag();
        global.setName(globalName);

        Tag identified = identifier.getExisting(global);
        assertNotNull(identified);
        assertEquals(globalName, identified.getName());
        assertNull(identified.getNetworkId());
        assertNull(identified.getMeasFacilId());
    }

    /**
     * Non-existent global tag.
     */
    @Test
    @Transactional
    public final void globalNotExistent()
        throws IdentificationException {
        Tag global = new Tag();
        global.setName("");

        assertNull(identifier.getExisting(global));
    }

    /**
     * Non-existent global tag.
     */
    @Test
    @Transactional
    public final void globalNotExistentWrongName()
        throws IdentificationException {
        Tag global = new Tag();
        global.setName(networkName);

        assertNull(identifier.getExisting(global));
    }

    /**
     * Existing network tag.
     */
    @Test
    @Transactional
    public final void network() throws IdentificationException {
        Tag tag = new Tag();
        tag.setName(networkName);
        tag.setNetworkId(networkId);

        Tag identified = identifier.getExisting(tag);
        assertNotNull(identified);
        assertEquals(networkName, identified.getName());
        assertEquals(networkId, identified.getNetworkId());
        assertNull(identified.getMeasFacilId());
    }

    /**
     * Non-existent network tag.
     */
    @Test
    @Transactional
    public final void networkNotExistent()
        throws IdentificationException {
        Tag tag = new Tag();
        tag.setName("");
        tag.setNetworkId(networkId);

        assertNull(identifier.getExisting(tag));
    }

    /**
     * Non-existent network tag.
     */
    @Test
    @Transactional
    public final void networkNotExistentWrongName()
        throws IdentificationException {
        Tag tag = new Tag();
        tag.setName(globalName);
        tag.setNetworkId(networkId);

        assertNull(identifier.getExisting(tag));
    }

    /**
     * Existing measFacil tag.
     */
    @Test
    @Transactional
    public final void measFacil() throws IdentificationException {
        Tag tag = new Tag();
        tag.setName(measFacilName);
        tag.setMeasFacilId(mstId);

        Tag identified = identifier.getExisting(tag);
        assertNotNull(identified);
        assertEquals(measFacilName, identified.getName());
        assertEquals(mstId, identified.getMeasFacilId());
        assertNull(identified.getNetworkId());
    }

    /**
     * Non-existent measFacil tag.
     */
    @Test
    @Transactional
    public final void measFacilNotExistent()
        throws IdentificationException {
        Tag tag = new Tag();
        tag.setName("");
        tag.setMeasFacilId(mstId);

        assertNull(identifier.getExisting(tag));
    }

    /**
     * Non-existent measFacil tag.
     */
    @Test
    @Transactional
    public final void measFacilNotExistentWrongName()
        throws IdentificationException {
        Tag tag = new Tag();
        tag.setName(globalName);
        tag.setMeasFacilId(mstId);

        assertNull(identifier.getExisting(tag));
    }

    /**
     * Impossible combination of identifying attributes.
     * Does not throw error because that is a validation issue.
     */
    @Test
    @Transactional
    public final void impossibleAttributeCombination()
        throws IdentificationException {
        Tag tag = new Tag();
        tag.setName(measFacilName);
        tag.setNetworkId(networkId);
        tag.setMeasFacilId(mstId);

        assertNull(identifier.getExisting(tag));
    }
}
