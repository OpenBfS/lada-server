/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data.requests;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import de.intevation.lada.model.master.MeasFacil;
import de.intevation.lada.validation.constraints.IsValidPrimaryKey;
import de.intevation.lada.validation.constraints.NotEmptyNorWhitespace;


public abstract class LafImportParameters<T> {

    private Map<@NotBlank String, @NotNull T> files;

    @NotNull
    @NotEmptyNorWhitespace
    @Size(max = 5)
    @IsValidPrimaryKey(clazz = MeasFacil.class)
    private String measFacilId;

    public Map<String, T> getFiles() {
        return files;
    }

    public void setFiles(Map<String, T> files) {
        this.files = files;
    }

    public String getMeasFacilId() {
        return measFacilId;
    }

    public void setMeasFacilId(String measFacilId) {
        this.measFacilId = measFacilId;
    }
}
