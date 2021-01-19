/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.importer;

/**
 * Container for error or warning messages send to the client.
 *
 * Errors and warnings are specified by the key-value pair that caused
 * the problem and a code.
 * The code can be
 * 670: Parser error
 * 671: existing
 * 672: duplicated entry
 * 673: missing
 * 674: date error
 * or any validation code.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
public class ReportItem {
    private String key;
    private Object value;
    private Integer code;

    /**
     * Default constructor.
     */
    public ReportItem() {
    }

    /**
     * Constructor to create a {@link ReportItem} object with data.
     * @param k The key caused the error/warning.
     * @param v The value caused the error/warning.
     * @param c The code specifying the error/warning.
     */
    public ReportItem(String k, Object v, Integer c) {
        this.key = k;
        this.value = v;
        this.code = c;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the code
     */
    public Integer getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(Integer code) {
        this.code = code;
    }
}
