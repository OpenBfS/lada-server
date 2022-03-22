/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.rest;

import java.io.Serializable;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;


/**
* Response object storing information about success, warnings, errors and
* the data object. This class is used as return value in REST services.
*
* @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
*/
public class Response implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean success;
    private String message;
    private Object data;
    private MultivaluedMap<String, Integer> errors;
    private MultivaluedMap<String, Integer> warnings;
    private MultivaluedMap<String, Integer> notifications;
    private int totalCount;

    /**
     * Constructor to create a basic Response object.
     *
     * @param s   Information if the operation was successful.
     * @param code      The return code.
     * @param d      The data object wrapped by the response.
     */
    public Response(boolean s, int code, Object d) {
        this.success = s;
        this.message = Integer.toString(code);
        this.data = d;
        this.errors = new MultivaluedHashMap<String, Integer>();
        this.warnings = new MultivaluedHashMap<String, Integer>();
        this.notifications = new MultivaluedHashMap<String, Integer>();
        this.totalCount = 0;
    }

    /**
     * Constructor to create a basic Response object.
     *
     * @param s         Information if the operation was successful.
     * @param code      The return code.
     * @param d      The data object wrapped by the response.
     */
    public Response(boolean s, int code, Object d, int count) {
        this.success = s;
        this.message = Integer.toString(code);
        this.data = d;
        this.errors = new MultivaluedHashMap<String, Integer>();
        this.warnings = new MultivaluedHashMap<String, Integer>();
        this.notifications = new MultivaluedHashMap<String, Integer>();
        this.totalCount = count;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(int message) {
        this.message = Integer.toString(message);
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public MultivaluedMap<String, Integer> getErrors() {
        return errors;
    }

    public void setErrors(MultivaluedMap<String, Integer> errors) {
        this.errors.putAll(errors);
//        this.errors = this.convertCodes(errors);
    }

    public MultivaluedMap<String, Integer> getWarnings() {
        return warnings;
    }

    public void setWarnings(MultivaluedMap<String, Integer> warnings) {
        this.warnings.putAll(warnings);
        //this.warnings = this.convertCodes(warnings);
    }

    public MultivaluedMap<String, Integer> getNotifications() {
      return notifications;
    }

    public void setNotifications(
        MultivaluedMap<String, Integer> notifications
    ) {
      this.notifications.putAll(notifications);
    }

    /**
     * @return the totalCount
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * @param totalCount the totalCount to set
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
