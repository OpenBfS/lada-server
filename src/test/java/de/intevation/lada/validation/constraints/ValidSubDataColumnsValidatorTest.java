/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.validation.constraints;

import static de.intevation.lada.exporter.QueryExportJob.ID_TYPE_MEASM;
import static de.intevation.lada.exporter.QueryExportJob.ID_TYPE_SAMPLE;
import static de.intevation.lada.exporter.QueryExportJob.SUBDATA_MEASM_MEASVAL_COUNT;
import static de.intevation.lada.exporter.QueryExportJob.SUBDATA_MEASM_STATUS_MP;
import static de.intevation.lada.exporter.QueryExportJob.SUBDATA_MEASVAL_UNIT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.intevation.lada.BaseTest;
import de.intevation.lada.data.requests.QueryExportParameters;
import de.intevation.lada.model.lada.MeasVal_;
import de.intevation.lada.model.lada.Measm_;
import jakarta.persistence.metamodel.Attribute;
import jakarta.validation.ConstraintValidatorContext;

@RunWith(Arquillian.class)
public class ValidSubDataColumnsValidatorTest extends BaseTest {

    private ValidSubDataColumnsValidator validator =
        new ValidSubDataColumnsValidator();

    // Unused in this validator
    private ConstraintValidatorContext ctx = null;

    @Test
    public void irrelevantIdField() {
        QueryExportParameters params = new QueryExportParameters();
        params.setIdField("xxx");
        assertTrue("idField without possible sub-data: Check should be skipped",
            validator.isValid(params, ctx));
    }

    @Test
    public void validSampleSubDataColumnNames() {
        QueryExportParameters params = new QueryExportParameters();
        params.setIdField(ID_TYPE_SAMPLE);
        params.setSubDataColumns(Measm_.class_.getAttributes().stream()
            .<String>map(Attribute::getName).toArray(String[]::new));
        assertTrue("Given subDataColumnNames should be considered valid",
            validator.isValid(params, ctx));
    }

    @Test
    public void validSampleNonColumnNames() {
        QueryExportParameters params = new QueryExportParameters();
        params.setIdField(ID_TYPE_SAMPLE);
        String[] validNames = {
            SUBDATA_MEASM_STATUS_MP, SUBDATA_MEASM_MEASVAL_COUNT };
        params.setSubDataColumns(validNames);
        assertTrue("Given subDataColumnNames should be considered valid",
            validator.isValid(params, ctx));
    }

    @Test
    public void invalidSampleSubDataColumnNames() {
        QueryExportParameters params = new QueryExportParameters();
        params.setIdField(ID_TYPE_SAMPLE);
        String[] invalidNames = {
            Measm_.EXT_ID, // valid
            MeasVal_.DETECT_LIM, // invalid
        };
        params.setSubDataColumns(invalidNames);
        assertFalse("Given subDataColumnNames should be considered invalid",
            validator.isValid(params, ctx));
    }

    @Test
    public void validMeasmSubDataColumnNames() {
        QueryExportParameters params = new QueryExportParameters();
        params.setIdField(ID_TYPE_MEASM);
        params.setSubDataColumns(MeasVal_.class_.getAttributes().stream()
            .<String>map(Attribute::getName).toArray(String[]::new));
        assertTrue("Given subDataColumnNames should be considered valid",
            validator.isValid(params, ctx));
    }

    @Test
    public void validMeasmNonColumnNames() {
        QueryExportParameters params = new QueryExportParameters();
        params.setIdField(ID_TYPE_MEASM);
        String[] validNames = { SUBDATA_MEASVAL_UNIT };
        params.setSubDataColumns(validNames);
        assertTrue("Given subDataColumnNames should be considered valid",
            validator.isValid(params, ctx));
    }

    @Test
    public void invalidMeasmSubDataColumnNames() {
        QueryExportParameters params = new QueryExportParameters();
        params.setIdField(ID_TYPE_MEASM);
        String[] invalidNames = {
            Measm_.EXT_ID, // invalid
            MeasVal_.DETECT_LIM, // valid
        };
        params.setSubDataColumns(invalidNames);
        assertFalse("Given subDataColumnNames should be considered invalid",
            validator.isValid(params, ctx));
    }
}
