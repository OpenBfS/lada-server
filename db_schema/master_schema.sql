\set ON_ERROR_STOP on

BEGIN;

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;


CREATE SCHEMA master;

SET search_path = master, pg_catalog;

CREATE FUNCTION set_site_id() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE value text;
    BEGIN
        value = '#'::text || lpad(NEW.id::text, 9, '0'::text);
        IF NEW.ext_id IS NULL THEN
            NEW.ext_id = value;
        END IF;
        IF NEW.long_text IS NULL OR NEW.long_text = '' THEN
            NEW.long_text = value;
        END IF;
        IF NEW.short_text IS NULL OR NEW.short_text = '' THEN
            NEW.short_text = value;
        END IF;
        RETURN NEW;
    END;
$$;

CREATE FUNCTION update_last_mod() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    BEGIN
        NEW.last_mod = now() AT TIME ZONE 'utc';
        RETURN NEW;
    END;
$$;

CREATE FUNCTION get_media_from_media_desk(media_desk character varying) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
declare
  result character varying(100);
  d00 smallint;
  d01 smallint;
  d02 smallint;
  d03 smallint;
begin
  if media_desk like 'D: %' then
    d00 := substring(media_desk,4,2);
    d01 := substring(media_desk,7,2);
    d02 := substring(media_desk,10,2);
    d03 := substring(media_desk,13,2);
    if d00 = '00' then
      result := null;
    else
      if d01 = '00' then
        select s00.name into result FROM master.env_descrip s00
        where s00.lev = 0 and s00.lev_val = d00::smallint;
      else
        if d02 = '00' or d00 <> '01' then
          select s01.name into result FROM master.env_descrip s01
          where s01.lev = 1 and s01.lev_val = d01::smallint
            and s01.pred_id =
              (select s00.id FROM master.env_descrip s00
               where s00.lev = 0 and s00.lev_val = d00::smallint);
        else
          if d03 = '00' then
            select s02.name into result FROM master.env_descrip s02
            where s02.lev = 2 and s02.lev_val = d02::smallint
              and s02.pred_id =
                (select s01.id FROM master.env_descrip s01
                 where s01.lev = 1 and s01.lev_val = d01::smallint
                   and s01.pred_id =
                     (select s00.id FROM master.env_descrip s00
                      where s00.lev = 0 and s00.lev_val = d00::smallint));
          else
            select s03.name into result FROM master.env_descrip s03
            where s03.lev = 3 and s03.lev_val = d03::smallint
              and s03.pred_id =
              (select s02.id FROM master.env_descrip s02
              where s02.lev = 2 and s02.lev_val = d02::smallint
                and s02.pred_id =
                  (select s01.id FROM master.env_descrip s01
                  where s01.lev = 1 and s01.lev_val = d01::smallint
                    and s01.pred_id =
                      (select s00.id FROM master.env_descrip s00
                      where s00.lev = 0 and s00.lev_val = d00::smallint)));
          end if;
        end if;
      end if;
    end if;
  else
    result := null;
  end if;
  return (result);
end;
$$;


CREATE OR REPLACE FUNCTION get_desk_description(
	media_desk character varying,
	stufe integer)
    RETURNS character varying
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
DECLARE
    d_xx character varying;
  BEGIN
    IF substr(media_desk, 4+stufe*3, 2) = '00' THEN 
      RETURN NULL; 
    END IF;

    IF stufe = 0 THEN
      SELECT d00.name
      INTO d_xx
      FROM master.env_descrip d00
      WHERE d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT);

    ELSEIF stufe = 1 THEN
      SELECT d01.name
      INTO d_xx
      FROM master.env_descrip d01
      JOIN master.env_descrip d00 ON d00.id = d01.pred_id
        AND d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE d01.lev = 1
        AND d01.lev_val = cast(substr(media_desk, 7, 2) AS SMALLINT);

    ELSEIF stufe = 2 THEN
      SELECT d02.name
      INTO d_xx
      FROM master.env_descrip d02
      JOIN master.env_descrip d01 ON d01.id = d02.pred_id
        AND d01.lev = 1
        AND d01.lev_val = cast(substr(media_desk, 7, 2) AS SMALLINT)
      JOIN master.env_descrip d00 ON d00.id = d01.pred_id
        AND d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE d02.lev = 2
        AND d02.lev_val = cast(substr(media_desk, 10, 2) AS SMALLINT);

    ELSEIF stufe = 3 AND substr(media_desk, 4, 2) = '01' THEN
      SELECT d03.name
      INTO d_xx
      FROM master.env_descrip d03
      JOIN master.env_descrip d02 ON d02.id = d03.pred_id
        AND d02.lev = 2
        AND d02.lev_val = cast(substr(media_desk , 10, 2) AS SMALLINT)
      JOIN master.env_descrip d01 ON d01.id = d02.pred_id
        AND d01.lev = 1
        AND d01.lev_val = cast(substr(media_desk, 7, 2) AS SMALLINT)
      JOIN master.env_descrip d00 ON d00.id = d01.pred_id
        AND d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE d03.lev = 3
        AND d03.lev_val = cast(substr(media_desk, 13, 2) AS SMALLINT);

    ELSEIF stufe = 3 AND substr(media_desk, 4, 2) <> '01' OR stufe > 3 THEN
      SELECT dxx.name
      INTO d_xx
      FROM master.env_descrip dxx
      JOIN master.env_descrip d01 ON d01.id = dxx.pred_id
        AND d01.lev = 1
        AND d01.lev_val = cast(substr(media_desk, 7, 2) AS SMALLINT)
      JOIN master.env_descrip d00 ON d00.id = d01.pred_id
        AND d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE dxx.lev = stufe
        AND dxx.lev_val = cast(substr(media_desk, (stufe * 3 + 4), 2) AS SMALLINT);

    ELSE
      d_xx := NULL;
    END IF;
    return d_xx;
  END;
$BODY$;


CREATE OR REPLACE FUNCTION get_desk_sxx(
	media_desk character varying,
	stufe integer)
    RETURNS integer
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
DECLARE
    s_xx INTEGER;
  BEGIN
    IF substr(media_desk, 4+stufe*3, 2) = '00' THEN 
      RETURN NULL; 
    END IF;
    IF stufe = 0 THEN
      SELECT d00.s_xx
      INTO s_xx
      FROM master.env_descrip d00
      WHERE d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT);

    ELSEIF stufe = 1 THEN
      SELECT d01.s_xx
      INTO s_xx
      FROM master.env_descrip d01
      JOIN master.env_descrip d00 ON d00.id = d01.pred_id
        AND d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE d01.lev = 1
        AND d01.lev_val = cast(substr(media_desk, 7, 2) AS SMALLINT);

    ELSEIF stufe = 2 THEN
      SELECT d02.s_xx
      INTO s_xx
      FROM master.env_descrip d02
      JOIN master.env_descrip d01 ON d01.id = d02.pred_id
        AND d01.lev = 1
        AND d01.lev_val = cast(substr(media_desk, 7, 2) AS SMALLINT)
      JOIN master.env_descrip d00 ON d00.id = d01.pred_id
        AND d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE d02.lev = 2
        AND d02.lev_val = cast(substr(media_desk, 10, 2) AS SMALLINT);

    ELSEIF stufe = 3 AND substr(media_desk, 4, 2) = '01' THEN
      SELECT d03.s_xx
      INTO s_xx
      FROM master.env_descrip d03
      JOIN master.env_descrip d02 ON d02.id = d03.pred_id
        AND d02.lev = 2
        AND d02.lev_val = cast(substr(media_desk , 10, 2) AS SMALLINT)
      JOIN master.env_descrip d01 ON d01.id = d02.pred_id
        AND d01.lev = 1
        AND d01.lev_val = cast(substr(media_desk, 7, 2) AS SMALLINT)
      JOIN master.env_descrip d00 ON d00.id = d01.pred_id
        AND d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE d03.lev = 3
        AND d03.lev_val = cast(substr(media_desk, 13, 2) AS SMALLINT);

    ELSEIF stufe = 3 AND substr(media_desk, 4, 2) <> '01' OR stufe > 3 THEN
      SELECT dxx.s_xx
      INTO s_xx
      FROM master.env_descrip dxx
      JOIN master.env_descrip d01 ON d01.id = dxx.pred_id
        AND d01.lev = 1
        AND d01.lev_val = cast(substr(media_desk, 7, 2) AS SMALLINT)
      JOIN master.env_descrip d00 ON d00.id = d01.pred_id
        AND d00.lev = 0
        AND d00.lev_val = cast(substr(media_desk, 4, 2) AS SMALLINT)
      WHERE dxx.lev = stufe
        AND dxx.lev_val = cast(substr(media_desk, (stufe * 3 + 4), 2) AS SMALLINT);

    ELSE
      s_xx := NULL;
    END IF;
    return s_xx;
  END;
$BODY$;

/* Check whether a given SQL statement can be executed in this database
 *
 * To be used as a check constraint for SQL statements stored in the database.
 * Be careful to not use it with any DML or DDL statements!
 */
CREATE OR REPLACE FUNCTION check_sql(stmt text)
    RETURNS boolean
    LANGUAGE 'plpgsql'
    VOLATILE PARALLEL UNSAFE
AS $BODY$
  BEGIN
    EXECUTE stmt || ' LIMIT 0';
    RETURN true;
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE USING MESSAGE = SQLERRM, ERRCODE = SQLSTATE;
      RETURN false;
  END;
$BODY$;


CREATE TABLE spat_ref_sys (
    id serial PRIMARY KEY,
    name character varying(50),
    idf_geo_key character varying(1),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_spat_ref_sys BEFORE UPDATE ON spat_ref_sys FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE meas_unit (
    id serial PRIMARY KEY,
    name character varying(50),
    unit_symbol character varying(12),
    eudf_unit_id character varying(8),
    eudf_convers_factor bigint,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_meas_unit BEFORE UPDATE ON master.meas_unit FOR EACH ROW EXECUTE PROCEDURE master.update_last_mod();

CREATE TABLE unit_convers
(
    id serial PRIMARY KEY,
    from_unit_id integer NOT NULL REFERENCES meas_unit,
    to_unit_id  integer NOT NULL REFERENCES meas_unit,
    factor float NOT NULL,
    UNIQUE( from_unit_id, to_unit_id ),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_unit_convers BEFORE UPDATE ON master.unit_convers FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE opr_mode (
    id smallint PRIMARY KEY,
    name character varying(30) NOT NULL,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_opr_mode BEFORE UPDATE ON master.opr_mode FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE state (
    id serial PRIMARY KEY,
    ctry character varying(50) NOT NULL UNIQUE,
    ctry_orig_id smallint NOT NULL UNIQUE,
    iso_3166 character varying(2) UNIQUE,
    int_veh_reg_code character varying(5) UNIQUE,
    is_eu_country boolean NOT NULL DEFAULT false,
    x_coord_ext character varying(22),
    y_coord_ext character varying(22),
    spat_ref_sys_id integer REFERENCES spat_ref_sys,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_state BEFORE UPDATE ON master.state FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE admin_unit (
    id character varying(8) NOT NULL PRIMARY KEY,
    name character varying(80) NOT NULL,
    gov_dist_id character varying(8),
    rural_dist_id character varying(8),
    state_id character varying(8) NOT NULL,
    is_munic boolean DEFAULT false NOT NULL,
    is_rural_dist boolean DEFAULT false NOT NULL,
    is_gov_dist boolean DEFAULT false NOT NULL,
    is_state boolean DEFAULT false NOT NULL,
    zip character varying(6),
    nuts character varying(10),
    geom_center public.geometry(Point)
);

CREATE TABLE network (
    id character varying(2) PRIMARY KEY,
    name character varying(50),
    idf_network_id character varying(1),
    is_fmn boolean NOT NULL DEFAULT false,
    mail_list character varying(512),
    is_active boolean NOT NULL DEFAULT false,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_network BEFORE UPDATE ON master.network FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE meas_facil (
    id character varying(5) PRIMARY KEY,
    network_id character varying(2) NOT NULL REFERENCES network,
    address character varying(300),
    name character varying(60),
    meas_facil_type character varying(1),
    trunk_code character varying(6),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_meas_facil BEFORE UPDATE ON meas_facil FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE env_medium (
    id character varying(3) PRIMARY KEY,
    descr character varying(300),
    name character varying(80) NOT NULL,
    unit_1 integer REFERENCES meas_unit,
    unit_2 integer REFERENCES meas_unit,
    coord_ofc character varying(5) REFERENCES meas_facil,
    UNIQUE (name),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_env_medium BEFORE UPDATE ON master.env_medium FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE auth_funct (
    id smallint PRIMARY KEY,
    funct character varying(40) UNIQUE NOT NULL,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
INSERT INTO auth_funct VALUES (0, 'Erfasser');
INSERT INTO auth_funct VALUES (1, 'Status-Erfasser');
INSERT INTO auth_funct VALUES (2, 'Status-Land');
INSERT INTO auth_funct VALUES (3, 'Status-Leitstelle');
INSERT INTO auth_funct VALUES (4, 'Stammdatenpflege-Land');
CREATE TRIGGER last_mod_auth_funct BEFORE UPDATE ON master.auth_funct FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE auth (
    id serial PRIMARY KEY,
    ldap_gr character varying(40) NOT NULL,
    network_id character varying(2) REFERENCES network,
    meas_facil_id character varying(5) REFERENCES meas_facil,
    appr_lab_id character varying(5) REFERENCES meas_facil,
    auth_funct_id smallint REFERENCES auth_funct,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
ALTER TABLE auth
    ADD CONSTRAINT auth_unique UNIQUE (ldap_gr, network_id, meas_facil_id, appr_lab_id, auth_funct_id);
CREATE TRIGGER last_mod_auth BEFORE UPDATE ON auth FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE auth_coord_ofc_env_medium_mp (
    id serial PRIMARY KEY,
    meas_facil_id character varying(5) REFERENCES meas_facil,
    env_medium_id character varying(3) REFERENCES env_medium,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_auth_coord_ofc_env_medium_mp BEFORE UPDATE ON master.auth_coord_ofc_env_medium_mp FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE regulation (
    id serial PRIMARY KEY,
    descr character varying(30),
    regulation character varying(12),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_regulation BEFORE UPDATE ON master.regulation FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE dataset_creator (
    id serial PRIMARY KEY,
    network_id character varying(2) NOT NULL REFERENCES network,
    ext_id character varying(2) NOT NULL,
    meas_facil_id character varying(5) NOT NULL REFERENCES meas_facil,
    descr character varying(120) NOT NULL,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    UNIQUE(ext_id, network_id, meas_facil_id)

);
CREATE TRIGGER last_mod_dataset_creator BEFORE UPDATE ON dataset_creator FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE env_descrip_env_medium_mp (
    id serial PRIMARY KEY,
    s00 integer NOT NULL,
    s01 integer NOT NULL,
    s02 integer,
    s03 integer,
    s04 integer,
    s05 integer,
    s06 integer,
    s07 integer,
    s08 integer,
    s09 integer,
    s10 integer,
    s11 integer,
    env_medium_id character varying(3) NOT NULL REFERENCES env_medium,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_env_descrip_env_medium_mp BEFORE UPDATE ON master.env_descrip_env_medium_mp FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE env_descrip (
    id serial PRIMARY KEY,
    pred_id integer REFERENCES env_descrip,
    lev smallint,
    s_xx serial,
    lev_val smallint,
    name character varying(100),
    implication character varying(300),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_env_descrip BEFORE UPDATE ON env_descrip FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE lada_user (
    id serial PRIMARY KEY,
    name character varying(80) NOT NULL,
    UNIQUE (name)
);


CREATE TABLE base_query (
    id serial PRIMARY KEY,
    sql text NOT NULL CHECK(check_sql(sql))
);

CREATE TABLE query_user (
    id serial PRIMARY KEY,
    name character varying(80) NOT NULL,
    lada_user_id integer NOT NULL REFERENCES lada_user,
    base_query_id integer NOT NULL REFERENCES base_query,
    descr text NOT NULL
);

CREATE TABLE query_meas_facil_mp (
    id serial PRIMARY KEY,
    query integer NOT NULL REFERENCES query_user ON DELETE CASCADE,
    meas_facil_id character varying(5) NOT NULL REFERENCES meas_facil
);


CREATE TABLE filter_type (
    id serial PRIMARY KEY,
    type character varying(12) NOT NULL,
    is_multiselect boolean NOT NULL DEFAULT false,
    UNIQUE(type)
);
INSERT INTO filter_type VALUES(0, 'text', false);
INSERT INTO filter_type VALUES(1, 'number', false);
INSERT INTO filter_type VALUES(2, 'bool', false);
INSERT INTO filter_type VALUES(3, 'datetime', false);
INSERT INTO filter_type VALUES(4, 'listtext', true);
INSERT INTO filter_type VALUES(5, 'listnumber', true);
INSERT INTO filter_type VALUES(6, 'listdatetime', true);
INSERT INTO filter_type VALUES(7, 'generictext', false);
INSERT INTO filter_type VALUES(8, 'name', true);
/* Used to filter result returned from base_query.sql by any unique identifier,
 * e.g. for export of selected entries (see QueryExportJob.createIdListFilter): */
INSERT INTO filter_type VALUES(9, 'genericid', true);

CREATE TABLE filter (
    id serial PRIMARY KEY,
    sql text NOT NULL,
    param text NOT NULL,
    type integer NOT NULL REFERENCES filter_type,
    name text
);



CREATE TABLE mmt (
    id character varying(2) PRIMARY KEY,
    descr character varying(300),
    name character varying(50),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_mmt BEFORE UPDATE ON master.mmt FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE measd (
    id serial PRIMARY KEY,
    descr character varying(300),
    name character varying(50) NOT NULL,
    def_color character varying(9),
    idf_ext_id character varying(6),
    is_ref_nucl boolean NOT NULL DEFAULT false,
    eudf_nucl_id bigint,
    bvl_format_id character varying(7),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_measd BEFORE UPDATE ON master.measd FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE measd_gr (
    id serial PRIMARY KEY,
    name character varying(80),
    ref_nucl_gr character(1) DEFAULT NULL::bpchar,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_measd_gr BEFORE UPDATE ON master.measd_gr FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE mpg_categ (
    id serial PRIMARY KEY,
    network_id character varying(2) NOT NULL REFERENCES network,
    ext_id character varying(3) NOT NULL,
    name character varying(120) NOT NULL,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    UNIQUE(ext_id, network_id)
);
CREATE TRIGGER last_mod_mpg_categ BEFORE UPDATE ON mpg_categ FOR EACH ROW EXECUTE PROCEDURE update_last_mod();


CREATE TABLE measd_gr_mp (
    measd_gr_id integer NOT NULL REFERENCES measd_gr,
    measd_id integer NOT NULL REFERENCES measd,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
ALTER TABLE ONLY measd_gr_mp
    ADD CONSTRAINT mg_grp_pkey PRIMARY KEY (measd_gr_id, measd_id);
CREATE TRIGGER last_mod_measd_gr_mp BEFORE UPDATE ON master.measd_gr_mp FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE mmt_measd_gr_mp (
    measd_gr_id integer NOT NULL REFERENCES measd_gr,
    mmt_id character varying(2) NOT NULL REFERENCES mmt,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
ALTER TABLE ONLY mmt_measd_gr_mp
    ADD CONSTRAINT mmt_messgroesse_grp_pkey PRIMARY KEY (measd_gr_id, mmt_id);
CREATE TRIGGER last_mod_mmt_measd_gr_mp BEFORE UPDATE ON master.mmt_measd_gr_mp FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE VIEW mmt_measd_view AS
 SELECT mmt_measd_gr_mp.mmt_id,
    measd_gr_mp.measd_id
   FROM mmt_measd_gr_mp,
    measd_gr_mp
  WHERE (measd_gr_mp.measd_gr_id = mmt_measd_gr_mp.measd_gr_id);


-- Mappings for REI extension

CREATE TABLE rei_ag
(
    id serial PRIMARY KEY,
    name character varying(10) NOT NULL,
    descr character varying(120),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_rei_ag BEFORE UPDATE ON master.rei_ag FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE rei_ag_gr
(
    id serial PRIMARY KEY,
    name character varying(30),
    descr character varying(120),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_rei_ag_gr BEFORE UPDATE ON master.rei_ag_gr FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE rei_ag_gr_mp
(
    id serial PRIMARY KEY,
    rei_ag_gr_id integer REFERENCES rei_ag_gr,
    rei_ag_id integer REFERENCES rei_ag,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_rei_ag_gr_mp BEFORE UPDATE ON master.rei_ag_gr_mp FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE rei_ag_gr_env_medium_mp
(
    id serial PRIMARY KEY,
    rei_ag_gr_id integer REFERENCES rei_ag_gr,
    env_medium_id character varying(3) REFERENCES env_medium,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_rei_ag_gr_env_medium_mp BEFORE UPDATE ON master.rei_ag_gr_env_medium_mp FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE nucl_facil (
  id serial NOT NULL,
  ext_id character varying(7),
  name character varying(80),
  CONSTRAINT kta_pkey PRIMARY KEY (id),
  last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
COMMENT ON TABLE nucl_facil
  IS 'kernteschnische Anlagen';
CREATE TRIGGER last_mod_nucl_facil BEFORE UPDATE ON master.nucl_facil FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE nucl_facil_gr
(
    id serial PRIMARY KEY,
    ext_id character varying(7) NOT NULL,
    name character varying(120),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_nucl_facil_gr BEFORE UPDATE ON master.nucl_facil_gr FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE nucl_facil_gr_mp
(
    id serial PRIMARY KEY,
    nucl_facil_gr_id integer REFERENCES nucl_facil_gr,
    nucl_facil_id integer REFERENCES nucl_facil,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_nucl_facil_gr_mp BEFORE UPDATE ON master.nucl_facil_gr_mp FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

-- Mappings for site

CREATE TABLE site_class (
    id smallint PRIMARY KEY,
    name character varying(60),
    ext_id character varying(3),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_site_class BEFORE UPDATE ON master.site_class FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE poi (
    id character varying(7) PRIMARY KEY,
    name character varying(80) NOT NULL,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_poi BEFORE UPDATE ON poi FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE munic_div (
    id serial PRIMARY KEY,
    network_id character varying(2) NOT NULL REFERENCES network,
    munic_id character varying(8) NOT NULL REFERENCES admin_unit,
    site_id integer NOT NULL,
    name character varying(180),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_munic_div BEFORE UPDATE ON munic_div FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE site (
    id serial PRIMARY KEY,
    network_id character varying(2) NOT NULL REFERENCES network,
    ext_id character varying(20) NOT NULL,
    long_text character varying(100) NOT NULL,
    state_id smallint REFERENCES state,
    munic_id character varying(8) REFERENCES admin_unit,
    is_fuzzy boolean NOT NULL DEFAULT false,
    nuts_id character varying(10),
    spat_ref_sys_id integer NOT NULL REFERENCES spat_ref_sys,
    x_coord_ext character varying(22) NOT NULL,
    y_coord_ext character varying(22) NOT NULL,
    alt real,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    geom public.geometry(Point,4326) NOT NULL,
    shape public.geometry(MultiPolygon,4326),
    site_class_id smallint REFERENCES site_class,
    short_text character varying(15) NOT NULL,
    rei_report_text character varying(70),
    rei_zone character varying(1),
    rei_sector character varying(2),
    rei_competence character varying(10),
    rei_opr_mode character varying(10),
    is_rei_active boolean,
    rei_nucl_facil_gr_id integer REFERENCES nucl_facil_gr,
    poi_id character varying(7) REFERENCES poi(id),
    height_asl real,
    rei_ag_gr_id integer REFERENCES rei_ag_gr,
    munic_div_id integer REFERENCES munic_div,
    UNIQUE(ext_id, network_id)
);

CREATE INDEX site_network_id_idx ON master.site USING btree (network_id);

CREATE TRIGGER last_mod_site BEFORE UPDATE ON site FOR EACH ROW EXECUTE PROCEDURE update_last_mod();
CREATE TRIGGER set_site_id_site BEFORE INSERT ON site FOR EACH ROW EXECUTE PROCEDURE set_site_id();

CREATE TABLE type_regulation (
    id character(1) PRIMARY KEY,
    name character varying(60),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_type_regulation BEFORE UPDATE ON master.type_regulation FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE oblig_measd_mp (
    id serial PRIMARY KEY,
    measd_id integer NOT NULL REFERENCES measd,
    mmt_id character varying(2) NOT NULL REFERENCES mmt,
    env_medium_id character varying(3) NOT NULL REFERENCES env_medium,
    regulation_id smallint NOT NULL REFERENCES regulation,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
ALTER TABLE IF EXISTS oblig_measd_mp
    ADD CONSTRAINT pflicht_messgroesse_unique UNIQUE (measd_id, mmt_id, env_medium_id, regulation_id);
CREATE TRIGGER last_mod_oblig_measd_mp BEFORE UPDATE ON master.oblig_measd_mp FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE sample_specif (
    id character varying(3) PRIMARY KEY,
    unit_id integer REFERENCES meas_unit,
    name character varying(50) NOT NULL,
    ext_id character varying(7) NOT NULL,
    eudf_keyword character varying(40),
    UNIQUE (eudf_keyword),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_sample_specif BEFORE UPDATE ON master.sample_specif FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE env_specif_mp (
    id serial PRIMARY KEY,
    sample_specif_id character varying(3) REFERENCES sample_specif,
    env_medium_id character varying(3) REFERENCES env_medium,
    UNIQUE (sample_specif_id, env_medium_id),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_env_specif_mp BEFORE UPDATE ON master.env_specif_mp FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE sample_meth (
    id serial PRIMARY KEY,
    name character varying(30),
    ext_id character varying(5) NOT NULL,
    eudf_sample_meth_id character varying(1) NOT NULL,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_sample_meth BEFORE UPDATE ON master.sample_meth FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE sampler (
    id serial PRIMARY KEY,
    network_id character varying(2) NOT NULL REFERENCES network,
    ext_id character varying(9) NOT NULL,
    editor character varying(25),
    comm character varying(60),
    inst character varying(80),
    descr character varying(80) NOT NULL,
    short_text character varying(10) NOT NULL,
    city character varying(20),
    zip character varying(5),
    street character varying(30),
    phone character varying(20),
    route_planning character varying(3),
    type character(1),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    UNIQUE(ext_id, network_id)
);
CREATE TRIGGER last_mod_sampler BEFORE UPDATE ON sampler FOR EACH ROW EXECUTE PROCEDURE update_last_mod();


CREATE TABLE disp (
    id  serial PRIMARY KEY,
    name character varying(12) NOT NULL,
    format character varying(30)
);


-- Status workflow
CREATE TABLE status_lev (
    id integer PRIMARY KEY,
    lev character varying(50) UNIQUE NOT NULL
);
INSERT INTO status_lev VALUES (1, 'MST');
INSERT INTO status_lev VALUES (2, 'LAND');
INSERT INTO status_lev VALUES (3, 'LST');


CREATE TABLE status_val (
    id integer PRIMARY KEY,
    val character varying(50) UNIQUE NOT NULL
);
INSERT INTO status_val VALUES (0, 'nicht vergeben');
INSERT INTO status_val VALUES (1, 'plausibel');
INSERT INTO status_val VALUES (2, 'nicht repräsentativ');
INSERT INTO status_val VALUES (3, 'nicht plausibel');
INSERT INTO status_val VALUES (4, 'Rückfrage');
INSERT INTO status_val VALUES (5, 'ungeprüft');
INSERT INTO status_val VALUES (7, 'nicht lieferbar');
INSERT INTO status_val VALUES (8, 'zurückgesetzt');


CREATE TABLE status_mp (
    id integer PRIMARY KEY,
    status_lev_id integer REFERENCES status_lev NOT NULL,
    status_val_id integer REFERENCES status_val NOT NULL,
    UNIQUE(status_lev_id, status_val_id)
);
INSERT INTO status_mp VALUES (1, 1, 0);
INSERT INTO status_mp VALUES (2, 1, 1);
INSERT INTO status_mp VALUES (3, 1, 2);
INSERT INTO status_mp VALUES (4, 1, 3);
INSERT INTO status_mp VALUES (5, 1, 7);
INSERT INTO status_mp VALUES (6, 2, 1);
INSERT INTO status_mp VALUES (7, 2, 2);
INSERT INTO status_mp VALUES (8, 2, 3);
INSERT INTO status_mp VALUES (9, 2, 4);
INSERT INTO status_mp VALUES (10, 3, 1);
INSERT INTO status_mp VALUES (11, 3, 2);
INSERT INTO status_mp VALUES (12, 3, 3);
INSERT INTO status_mp VALUES (13, 3, 4);
INSERT INTO status_mp VALUES (14, 1, 8);
INSERT INTO status_mp VALUES (15, 2, 8);
INSERT INTO status_mp VALUES (16, 3, 8);
INSERT INTO status_mp VALUES (17, 1, 5);


CREATE TABLE status_ord_mp (
    id serial PRIMARY KEY,
    from_id integer REFERENCES status_mp NOT NULL,
    to_id integer REFERENCES status_mp NOT NULL,
    UNIQUE(from_id, to_id)
);

CREATE TABLE master.tag_type (id text PRIMARY KEY, tag_type TEXT);
INSERT INTO master.tag_type VALUES('global', 'Global');
INSERT INTO master.tag_type VALUES('netz', 'Netzbetreiber');
INSERT INTO master.tag_type VALUES('mst', 'Messstelle');

/*
CREATE FUNCTION populate_status_ord_mp() RETURNS void AS $$
DECLARE kombi_from RECORD;
DECLARE s_from integer;
DECLARE w_from integer;
DECLARE kombi_to RECORD;
DECLARE s_to integer;
DECLARE w_to integer;

BEGIN
FOR kombi_from IN SELECT * FROM status_mp LOOP
    s_from := kombi_from.stufe_id;
    w_from := kombi_from.wert_id;

    FOR kombi_to IN SELECT * FROM status_mp LOOP
        s_to := kombi_to.stufe_id;
        w_to := kombi_to.wert_id;

        IF s_from = s_to AND w_to <> 0 THEN
           -- At the same 'stufe', all permutations occur,
           -- but 'nicht vergeben' is only allowed for von_id
           INSERT INTO status_ord_mp (from_id, to_id)
                  VALUES (kombi_from.id, kombi_to.id);

        ELSEIF s_to = s_from + 1
               AND w_from <> 0 AND w_from <> 4
               AND w_from <> 8 AND w_to <> 8 THEN
           -- Going to the next 'stufe' all available status_mp are allowed
           -- in case current wert is not 'nicht vergeben', 'Rückfrage' or
           -- 'zurückgesetzt' and we are not trying to set 'zurückgesetzt'
           INSERT INTO status_ord_mp (from_id, to_id)
                  VALUES (kombi_from.id, kombi_to.id);

        ELSEIF w_from = 4 AND s_to = 1 AND w_to >= 1 AND w_to <= 3 THEN
           -- After 'Rückfrage' follows 'MST' with
           -- 'plausibel', 'nicht plausibel' or 'nicht repräsentativ'
           INSERT INTO status_ord_mp (from_id, to_id)
                  VALUES (kombi_from.id, kombi_to.id);

        ELSEIF w_to = 8 AND s_from = s_to THEN
           -- 'zurückgesetzt' can only be set on the same 'stufe'
           INSERT INTO status_ord_mp (from_id, to_id)
                  VALUES (kombi_from.id, kombi_to.id);

        ELSEIF w_from = 8 AND s_to = s_from - 1 THEN
           -- after 'zurückgesetzt' always follows the next lower 'stufe'
           INSERT INTO status_ord_mp (from_id, to_id)
                  VALUES (kombi_from.id, kombi_to.id);

        END IF;
    END LOOP;
END LOOP;
END;
$$ LANGUAGE plpgsql;

SELECT populate_status_reihenfolge();
DROP FUNCTION populate_status_reihenfolge();
ALTER TABLE status_ord_mp ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE status_reihenfolge_id_seq;
*/

CREATE VIEW status_access_mp_view AS (
    SELECT r.id,
           zu.status_val_id,
           zu.status_lev_id,
           von.status_val_id AS cur_val_id,
           von.status_lev_id AS cur_lev_id
    FROM master.status_ord_mp r
        JOIN master.status_mp von
            ON von.id = r.from_id
        JOIN master.status_mp zu
            ON zu.id = r.to_id
);

-- Mappings for import

CREATE TABLE mpg_transf (
    id serial PRIMARY KEY,
    ext_id character varying(1) NOT NULL,
    name character varying(100) NOT NULL,
    opr_mode_id integer NOT NULL REFERENCES opr_mode,
    regulation_id integer NOT NULL REFERENCES regulation,
    UNIQUE (ext_id)
);

-- Mappings for import

CREATE INDEX fts_status_kooin10001 ON state USING btree (spat_ref_sys_id);

CREATE TABLE tz (
    id  integer PRIMARY KEY,
    name character varying(20) NOT NULL,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_tz BEFORE UPDATE ON master.tz FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE import_conf (
    id serial PRIMARY KEY,
    name character varying(30) NOT NULL,
    attribute character varying(30) NOT NULL,
    meas_facil_id character varying(5) NOT NULL REFERENCES meas_facil,
    from_val character varying(100),
    to_val character varying(100),
    action character varying(10),
    CHECK (action = 'default' OR
        action = 'convert' OR
        action = 'transform'),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_import_conf BEFORE UPDATE ON master.import_conf FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

-- Tables for queryui

CREATE TABLE grid_col_mp (
    id serial PRIMARY KEY,
    base_query integer NOT NULL REFERENCES base_query ON DELETE CASCADE,
    grid_col character varying(80) NOT NULL,
    data_index character varying(80) NOT NULL,
    position integer NOT NULL CHECK(position > 0),
    filter integer REFERENCES filter,
    data_type integer NOT NULL REFERENCES disp,
    UNIQUE(base_query, grid_col),
    UNIQUE(base_query, data_index),
    UNIQUE(base_query, position)
);

CREATE TABLE grid_col_conf (
    id serial PRIMARY KEY,
    user_id integer NOT NULL REFERENCES lada_user,
    grid_col_mp_id integer NOT NULL REFERENCES grid_col_mp,
    query_user_id integer NOT NULL REFERENCES query_user ON DELETE CASCADE,
    sort character varying(4),
    sort_index integer,
    filter_val text,
    is_filter_active boolean NOT NULL DEFAULT false,
    is_filter_negate boolean NOT NULL DEFAULT false,
    is_filter_regex boolean NOT NULL DEFAULT false,
    is_filter_null boolean NOT NULL DEFAULT false,
    is_visible boolean NOT NULL DEFAULT false,
    col_index integer,
    width integer
);

CREATE TABLE tag (
    id serial PRIMARY KEY,
    name text NOT NULL,
    meas_facil_id character varying REFERENCES meas_facil(id),
    is_auto_tag boolean NOT NULL DEFAULT false,
    network_id varchar(2) REFERENCES network,
    lada_user_id INTEGER REFERENCES lada_user,
    tag_type TEXT REFERENCES tag_type NOT NULL,
    val_until TIMESTAMP without time zone,
    created_at TIMESTAMP without time zone NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    UNIQUE(name, network_id, meas_facil_id)
);
CREATE UNIQUE INDEX is_auto_tag_unique_idx ON master.tag (name) WHERE is_auto_tag = true;

CREATE TABLE master.convers_dm_fm(
  id serial NOT NULL PRIMARY KEY,	
  unit_id smallint NOT NULL REFERENCES meas_unit(id),
  to_unit_id  smallint NOT NULL REFERENCES meas_unit(id),
  env_medium_id character varying(3) NOT NULL REFERENCES env_medium(id),
  env_descrip_pattern character varying(100),	
  conv_factor numeric(25,12) NOT NULL,
  last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_convers_dm_fm BEFORE UPDATE ON master.convers_dm_fm FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE IF NOT EXISTS master.ref_val_measure
(
    id serial NOT NULL PRIMARY KEY,
    measure character varying(80) COLLATE pg_catalog."default" NOT NULL,
    descr character varying(512) COLLATE pg_catalog."default",
    --CONSTRAINT richtwert_massnahme_pkey PRIMARY KEY (id),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_ref_val_measure BEFORE UPDATE ON master.ref_val_measure FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE IF NOT EXISTS master.ref_val
(
    id serial NOT NULL PRIMARY KEY,
    env_medium_id character varying(3) COLLATE pg_catalog."default" NOT NULL,
    ref_val_meas_id integer NOT NULL,
    measd_gr_id integer NOT NULL,
    specif character varying(80) COLLATE pg_catalog."default",
    ref_val double precision NOT NULL,
    --CONSTRAINT name_pkey PRIMARY KEY (id),
    CONSTRAINT name_massnahme_id_fkey FOREIGN KEY (ref_val_meas_id)
        REFERENCES ref_val_measure (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT name_messgroessengruppe_id_fkey FOREIGN KEY (measd_gr_id)
        REFERENCES measd_gr (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT name_umw_id_fkey FOREIGN KEY (env_medium_id)
        REFERENCES env_medium (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_ref_val BEFORE UPDATE ON master.ref_val FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE IF NOT EXISTS master.targ_act_mmt_gr
(
    id serial NOT NULL PRIMARY KEY,
    name character varying(20) COLLATE pg_catalog."default",
    descr character varying(120) COLLATE pg_catalog."default",
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_targ_act_mmt_gr BEFORE UPDATE ON master.targ_act_mmt_gr FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE IF NOT EXISTS master.targ_act_mmt_gr_mp
(
    id serial NOT NULL PRIMARY KEY,
    mmt_id character varying(2) COLLATE pg_catalog."default" NOT NULL,
    targ_act_mmt_gr_id integer NOT NULL,
    CONSTRAINT sollist_mmtgrp_zuord_mmt_id_fkey FOREIGN KEY (mmt_id)
        REFERENCES mmt (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT sollist_mmtgrp_zuord_sollist_mmtgrp_id_fkey FOREIGN KEY (targ_act_mmt_gr_id)
        REFERENCES targ_act_mmt_gr (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_targ_act_mmt_gr_mp BEFORE UPDATE ON master.targ_act_mmt_gr_mp FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE IF NOT EXISTS master.targ_env_gr
(
    id serial NOT NULL PRIMARY KEY,
    name character varying(20) COLLATE pg_catalog."default",
    targ_env__gr_displ character varying(120) COLLATE pg_catalog."default",
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_targ_env_gr BEFORE UPDATE ON master.targ_env_gr FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE IF NOT EXISTS master.targ_env_gr_mp
(
    id serial NOT NULL PRIMARY KEY,
    targ_env__gr_id integer NOT NULL,
    env_medium_id character varying(3) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT sollist_umwgrp_zuord_sollist_umwgrp_id_fkey FOREIGN KEY (targ_env__gr_id)
        REFERENCES targ_env_gr (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT sollist_umwgrp_zuord_umw_id_fkey FOREIGN KEY (env_medium_id)
        REFERENCES env_medium (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_targ_env_gr_mp BEFORE UPDATE ON master.targ_env_gr_mp FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE IF NOT EXISTS master.targ_act_targ
(
    id serial NOT NULL PRIMARY KEY,
    network_id character varying(2) COLLATE pg_catalog."default" NOT NULL,
    targ_act_mmt_gr_id integer NOT NULL,
    targ_env_medium_gr_id integer NOT NULL,
    is_imp boolean NOT NULL,
    targ integer NOT NULL,
    CONSTRAINT sollist_soll_netzbetreiber_id_fkey FOREIGN KEY (network_id)
        REFERENCES network (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT sollist_soll_sollist_mmtgrp_id_fkey FOREIGN KEY (targ_act_mmt_gr_id)
        REFERENCES targ_act_mmt_gr (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT sollist_soll_sollist_umwgrp_id_fkey FOREIGN KEY (targ_env_medium_gr_id)
        REFERENCES targ_env_gr (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_targ_act_targ BEFORE UPDATE ON master.targ_act_targ FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

COMMIT;
