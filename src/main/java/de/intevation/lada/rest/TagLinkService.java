/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.rest;

import jakarta.inject.Inject;
import jakarta.persistence.Query;
import de.intevation.lada.util.annotation.AuthorizationConfig;
import de.intevation.lada.util.auth.Authorization;
import de.intevation.lada.util.auth.AuthorizationType;
import de.intevation.lada.util.data.Repository;

public abstract class TagLinkService extends LadaService {

    @Inject
    protected Repository repository;

    @Inject
    @AuthorizationConfig(type = AuthorizationType.HEADER)
    protected Authorization authorization;

    private static final String EXISTS_QUERY_TEMPLATE =
        "SELECT EXISTS("
        + "SELECT 1 FROM lada.%s "
        + "WHERE tag_id=:tagId"
        + " AND %s=:taggedId)";

    public static class Response<T> {
        private boolean success;
        private String message;
        private T data;

        public Response(boolean success, int code, T data) {
            this.success = success;
            this.message = Integer.toString(code);
            this.data = data;
        }

        public boolean getSuccess() {
            return this.success;
        }

        public String getMessage() {
            return this.message;
        }

        public T getData() {
            return this.data;
        }
    }

    /**
     * Check if a tag link already exists.
     * @param tagId Tag id
     * @param taggedId Id of the tagged object
     * @param idField Tagged object id field
     * @param linkTable Tag link table name
     * @return True if link already exists
     */
    protected Boolean isExisting(Integer tagId, Integer taggedId,
            String idField, String linkTable) {
        // Check if tag is already assigned
        final String tagIdParam = "tagId";
        final String taggedIdParam = "taggedId";
        Query isAssigned = repository.queryFromString(
            String.format(EXISTS_QUERY_TEMPLATE, linkTable, idField));
        isAssigned.setParameter(tagIdParam, tagId);
        isAssigned.setParameter(taggedIdParam, taggedId);
        return (Boolean) isAssigned.getSingleResult();
    }
}
