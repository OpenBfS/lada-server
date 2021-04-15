/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.util.data;

import javax.ejb.EJBTransactionRolledbackException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.TransactionRequiredException;

import org.apache.log4j.Logger;
import org.hibernate.TransactionException;

import de.intevation.lada.util.annotation.RepositoryConfig;
import de.intevation.lada.util.rest.Response;


/**
 * Repository providing read and write access.
 *
 * @author <a href="mailto:rrenkert@intevation.de">Raimund Renkert</a>
 */
@RepositoryConfig(type = RepositoryType.RW)
@ApplicationScoped
public class DefaultRepository extends ReadOnlyRepository {

    @Inject
    private Logger logger;

    @Inject
    private DataTransaction transaction;

    /**
     * Create and persist a new object in the database.
     *
     * @param object The new object.
     * @param dataSource The datasource.
     *
     * @return Response object containing the new object, potentially
     *         modified by the database.
     */
    @Override
    public Response create(Object object, String dataSource) {
        try {
            transaction.persistInDatabase(object, dataSource);
        } catch (EntityExistsException eee) {
            logger.error("Could not persist " + object.getClass().getName()
                + ". Reason: " + eee.getClass().getName() + " - "
                + eee.getMessage());
            return new Response(false, StatusCodes.PRESENT, object);
        } catch (IllegalArgumentException iae) {
            logger.error("Could not persist " + object.getClass().getName()
                + ". Reason: " + iae.getClass().getName() + " - "
                + iae.getMessage());
            return new Response(false, StatusCodes.NOT_A_PROBE, object);
        } catch (TransactionRequiredException tre) {
            logger.error("Could not persist " + object.getClass().getName()
                + ". Reason: " + tre.getClass().getName() + " - "
                + tre.getMessage());
            return new Response(false, StatusCodes.ERROR_DB_CONNECTION, object);
        } catch (EJBTransactionRolledbackException ete) {
            logger.error("Could not persist " + object.getClass().getName()
                + ". Reason: " + ete.getClass().getName() + " - "
                + ete.getMessage());
            return new Response(false, StatusCodes.ERROR_VALIDATION, object);
        }
        Response response = new Response(true, StatusCodes.OK, object);
        return response;
    }

    /**
     * Update an existing object in the database.
     *
     * @param object The object.
     * @param dataSource The datasource.
     *
     * @return Response object containing the upadted object.
     */
    @Override
    public Response update(Object object, String dataSource) {
        Response response = new Response(true, StatusCodes.OK, object);
        try {
            transaction.updateInDatabase(object, dataSource);
        } catch (EntityExistsException eee) {
            return new Response(false, StatusCodes.PRESENT, object);
        } catch (IllegalArgumentException iae) {
            return new Response(false, StatusCodes.NOT_A_PROBE, object);
        } catch (TransactionRequiredException tre) {
            return new Response(false, StatusCodes.ERROR_DB_CONNECTION, object);
        } catch (EJBTransactionRolledbackException ete) {
            return new Response(false, StatusCodes.ERROR_VALIDATION, object);
        } catch (TransactionException te) {
            return new Response(false, StatusCodes.ERROR_VALIDATION, object);
        }
        return response;
    }

    /**
     * Delete an object from the database.
     *
     * @param object The object.
     * @param dataSource The datasource.
     *
     * @return Response object.
     */
    @Override
    public Response delete(Object object, String dataSource) {
        Response response = new Response(true, StatusCodes.OK, "");
        try {
            transaction.removeFromDatabase(object, dataSource);
        } catch (IllegalArgumentException iae) {
            return new Response(false, StatusCodes.NOT_A_PROBE, object);
        } catch (TransactionRequiredException tre) {
            return new Response(false, StatusCodes.ERROR_DB_CONNECTION, object);
        } catch (EJBTransactionRolledbackException ete) {
            return new Response(false, StatusCodes.OP_NOT_POSSIBLE, object);
        }
        return response;
    }
}
