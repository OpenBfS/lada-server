/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.test.validator;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.junit.Assert;

import de.intevation.lada.model.lada.Measm;
import de.intevation.lada.util.data.StatusCodes;
import de.intevation.lada.validation.Validator;
import de.intevation.lada.validation.Violation;

/**
 * Test messung entities.
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@Transactional
public class MessungTest {

    private static final int ID990 = 990;
    private static final int ID776 = 776;

    private final String minSampleIdKey = "minSampleId";

    // Values correspond with dataset dbUnit_probe.json
    private final int existingSampleId = 1000;
    private final int existingMeasmId = 1200;
    private final String existingMinSampleId = "T100";

    @Inject
    private Validator<Measm> validator;

    /**
     * Test nebenproben nr.
     * @param protocol the test protocol.
     */
    public void hasNebenprobenNr() {
        Measm messung = new Measm();
        messung.setMinSampleId("10R1");
        messung.setSampleId(existingSampleId);
        Violation violation = validator.validate(messung);
        if (violation.hasWarnings()) {
            Assert.assertFalse(
                violation.getWarnings().containsKey(minSampleIdKey));
        }
    }

    /**
     * Test without nebenproben nr.
     * @param protocol the test protocol.
     */
    public void hasNoNebenprobenNr() {
        Measm messung = new Measm();
        messung.setSampleId(existingSampleId);
        Violation violation = validator.validate(messung);
        Assert.assertTrue(violation.hasWarnings());
        Assert.assertTrue(violation.getWarnings().containsKey(minSampleIdKey));
        Assert.assertTrue(
            violation.getWarnings().get(minSampleIdKey).contains(
                StatusCodes.VALUE_MISSING));
    }

    /**
     * Test empty nebenproben nr.
     * @param protocol the test protocol.
     */
    public void hasEmptyNebenprobenNr() {
        Measm messung = new Measm();
        messung.setMinSampleId("");
        messung.setSampleId(existingSampleId);
        Violation violation = validator.validate(messung);
        Assert.assertTrue(violation.hasWarnings());
        Assert.assertTrue(violation.getWarnings().containsKey(minSampleIdKey));
        Assert.assertTrue(
            violation.getWarnings().get(minSampleIdKey).contains(
                StatusCodes.VALUE_MISSING));
    }

    /**
     * Test new existing nebenproben nr.
     * @param protocol the test protocol.
     */
    public void existingNebenprobenNrNew() {
        Measm messung = new Measm();
        messung.setMinSampleId(existingMinSampleId);
        messung.setSampleId(existingSampleId);
        Violation violation = validator.validate(messung);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors().containsKey(minSampleIdKey));
        Assert.assertTrue(
            violation.getErrors().get(minSampleIdKey).contains(
            StatusCodes.VALUE_AMBIGOUS));
    }

    /**
     * Test new unique nebenproben nr.
     * @param protocol the test protocol.
     */
    public void uniqueNebenprobenNrNew() {
        Measm messung = new Measm();
        messung.setMinSampleId("00G2");
        messung.setSampleId(existingSampleId);
        Violation violation = validator.validate(messung);
        if (violation.hasErrors()) {
            Assert.assertFalse(
                violation.getErrors().containsKey(minSampleIdKey));
        }
    }

    /**
     * Test update unique nebenproben nr.
     * @param protocol the test protocol.
     */
    public void uniqueNebenprobenNrUpdate() {
        Measm messung = new Measm();
        messung.setId(existingMeasmId);
        messung.setSampleId(existingSampleId);
        messung.setMinSampleId("00G2");
        Violation violation = validator.validate(messung);
        if (violation.hasErrors()) {
            Assert.assertFalse(
                violation.getErrors().containsKey(minSampleIdKey));
            return;
        }
    }

    /**
     * Test update existing nebenproben nr.
     * @param protocol the test protocol.
     */
    public void existingNebenprobenNrUpdate() {
        Measm messung = new Measm();
        messung.setId(ID776);
        messung.setSampleId(1);
        messung.setMinSampleId("0003");
        Violation violation = validator.validate(messung);
        Assert.assertTrue(violation.hasErrors());
        Assert.assertTrue(violation.getErrors().containsKey(minSampleIdKey));
        Assert.assertTrue(
            violation.getErrors().get(minSampleIdKey).contains(
                StatusCodes.VALUE_AMBIGOUS));
    }

    /**
     * Test messwert.
     * @param protocol the test protocol.
     */
    public void hasMesswert() {
        Measm messung = new Measm();
        messung.setId(1);
        messung.setSampleId(existingSampleId);
        Violation violation = validator.validate(messung);
        if (violation.hasWarnings()) {
            Assert.assertFalse(violation.getWarnings().containsKey("messwert"));
        }
    }

    /**
     * Test no messwert.
     * @param protocol the test protocol.
     */
    public void hasNoMesswert() {
        Measm messung = new Measm();
        messung.setId(ID990);
        messung.setSampleId(existingSampleId);
        Violation violation = validator.validate(messung);
        Assert.assertTrue(violation.hasWarnings());
        Assert.assertTrue(violation.getWarnings().containsKey("messwert"));
        Assert.assertTrue(
            violation.getWarnings().get("messwert").contains(
                StatusCodes.VALUE_MISSING));
    }
}
