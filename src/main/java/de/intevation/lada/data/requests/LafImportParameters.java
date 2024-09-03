/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data.requests;

import java.nio.charset.Charset;
import java.util.Map;

import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.NotEmptyNorWhitespace;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LafImportParameters {

    @NotNull
    private Charset encoding;

    @NotNull
    private Map<String, String> files;

    @NotNull
    @NotEmptyNorWhitespace
    @Size(max = 5)
    @IsValidPrimaryKey(clazz = MeasFacil.class)
    private String measFacilId;

    public Charset getEncoding() {
        return encoding;
    }

    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }

    public Map<String, String> getFiles() {
        return files;
    }

    public void setFiles(Map<String, String> files) {
        this.files = files;
    }

    public String getMeasFacilId() {
        return measFacilId;
    }

    public void setMeasFacilId(String measFacilId) {
        this.measFacilId = measFacilId;
    }
}
