CREATE TABLE IF NOT EXISTS stamm.richtwert_massnahme
(
    id serial NOT NULL PRIMARY KEY,
    massnahme character varying(80) COLLATE pg_catalog."default" NOT NULL,
    beschreibung character varying(512) COLLATE pg_catalog."default",
    --CONSTRAINT richtwert_massnahme_pkey PRIMARY KEY (id),
    letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER letzte_aenderung_richtwert_massnahme BEFORE UPDATE ON stamm.richtwert_massnahme FOR EACH ROW EXECUTE PROCEDURE update_letzte_aenderung();

CREATE TABLE IF NOT EXISTS stamm.richtwert
(
    id serial NOT NULL PRIMARY KEY,
    umw_id character varying(3) COLLATE pg_catalog."default" NOT NULL,
    massnahme_id integer NOT NULL,
    messgroessengruppe_id integer NOT NULL,
    zusatztext character varying(80) COLLATE pg_catalog."default",
    richtwert double precision NOT NULL,
    --CONSTRAINT richtwert_pkey PRIMARY KEY (id),
    CONSTRAINT richtwert_massnahme_id_fkey FOREIGN KEY (massnahme_id)
        REFERENCES stamm.richtwert_massnahme (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT richtwert_messgroessengruppe_id_fkey FOREIGN KEY (messgroessengruppe_id)
        REFERENCES stamm.messgroessen_gruppe (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT richtwert_umw_id_fkey FOREIGN KEY (umw_id)
        REFERENCES stamm.umwelt (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER letzte_aenderung_richtwert BEFORE UPDATE ON stamm.richtwert FOR EACH ROW EXECUTE PROCEDURE update_letzte_aenderung();

CREATE TABLE IF NOT EXISTS stamm.sollist_mmtgrp
(
    id serial NOT NULL PRIMARY KEY,
    bezeichnung character varying(20) COLLATE pg_catalog."default",
    beschreibung character varying(120) COLLATE pg_catalog."default",
    letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER letzte_aenderung_sollist_mmtgrp BEFORE UPDATE ON stamm.sollist_mmtgrp FOR EACH ROW EXECUTE PROCEDURE update_letzte_aenderung();

CREATE TABLE IF NOT EXISTS stamm.sollist_mmtgrp_zuord
(
    id serial NOT NULL PRIMARY KEY,
    mmt_id character varying(2) COLLATE pg_catalog."default" NOT NULL,
    sollist_mmtgrp_id integer NOT NULL,
    CONSTRAINT sollist_mmtgrp_zuord_mmt_id_fkey FOREIGN KEY (mmt_id)
        REFERENCES stamm.mess_methode (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT sollist_mmtgrp_zuord_sollist_mmtgrp_id_fkey FOREIGN KEY (sollist_mmtgrp_id)
        REFERENCES stamm.sollist_mmtgrp (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER letzte_aenderung_sollist_mmtgrp_zuord BEFORE UPDATE ON stamm.sollist_mmtgrp_zuord FOR EACH ROW EXECUTE PROCEDURE update_letzte_aenderung();

CREATE TABLE IF NOT EXISTS stamm.sollist_umwgrp
(
    id serial NOT NULL PRIMARY KEY,
    bezeichnung character varying(20) COLLATE pg_catalog."default",
    beschreibung character varying(120) COLLATE pg_catalog."default",
    letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER letzte_aenderung_sollist_umwgrp BEFORE UPDATE ON stamm.sollist_umwgrp FOR EACH ROW EXECUTE PROCEDURE update_letzte_aenderung();

CREATE TABLE IF NOT EXISTS stamm.sollist_umwgrp_zuord
(
    id serial NOT NULL PRIMARY KEY,
    sollist_umwgrp_id integer NOT NULL,
    umw_id character varying(3) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT sollist_umwgrp_zuord_sollist_umwgrp_id_fkey FOREIGN KEY (sollist_umwgrp_id)
        REFERENCES stamm.sollist_umwgrp (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT sollist_umwgrp_zuord_umw_id_fkey FOREIGN KEY (umw_id)
        REFERENCES stamm.umwelt (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER letzte_aenderung_sollist_umwgrp_zuord BEFORE UPDATE ON stamm.sollist_umwgrp_zuord FOR EACH ROW EXECUTE PROCEDURE update_letzte_aenderung();

CREATE TABLE IF NOT EXISTS stamm.sollist_soll
(
    id serial NOT NULL PRIMARY KEY,
    netzbetreiber_id character varying(2) COLLATE pg_catalog."default" NOT NULL,
    sollist_mmtgrp_id integer NOT NULL,
    sollist_umwgrp_id integer NOT NULL,
    imp boolean NOT NULL,
    soll integer NOT NULL,
    CONSTRAINT sollist_soll_netzbetreiber_id_fkey FOREIGN KEY (netzbetreiber_id)
        REFERENCES stamm.netz_betreiber (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT sollist_soll_sollist_mmtgrp_id_fkey FOREIGN KEY (sollist_mmtgrp_id)
        REFERENCES stamm.sollist_mmtgrp (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT sollist_soll_sollist_umwgrp_id_fkey FOREIGN KEY (sollist_umwgrp_id)
        REFERENCES stamm.sollist_umwgrp (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    letzte_aenderung timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER letzte_aenderung_sollist_soll BEFORE UPDATE ON stamm.sollist_soll FOR EACH ROW EXECUTE PROCEDURE update_letzte_aenderung();
