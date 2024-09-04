/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.data.requests;

import java.nio.charset.Charset;


public abstract class ExportParameters {

    private Charset encoding = Charset.forName("iso-8859-15");

    private String filename;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Charset getEncoding() {
        return encoding;
    }

    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }
}
