/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */

@SuppressWarnings("requires-automatic")
module lada.server {
    requires java.desktop;
    requires java.sql;

    requires jakarta.concurrency;
    requires jakarta.json;
    requires jakarta.json.bind;
    requires jakarta.persistence;
    requires jakarta.servlet;
    requires jakarta.transaction;
    requires jakarta.validation;
    requires jakarta.ws.rs;

    requires org.antlr.antlr4.runtime;
    requires org.apache.commons.csv;
    requires org.apache.commons.lang3;
    requires org.eclipse.microprofile.openapi;
    requires org.geotools.main;
    requires org.geotools.opengis;
    requires org.geotools.referencing;
    requires org.hibernate.orm.core;
    requires org.hibernate.validator;
    requires org.jboss.logging;
    requires org.locationtech.jts;

    requires resteasy.core.spi;
    requires resteasy.validator.provider;
}
