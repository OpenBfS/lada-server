\set ON_ERROR_STOP on

BEGIN;


SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: lada; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA lada;

SET search_path = lada, pg_catalog;

CREATE FUNCTION set_measm_ext_id() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    BEGIN
        IF NEW.ext_id IS NULL THEN
            NEW.ext_id = (
                SELECT coalesce(max(ext_id),0)
                   FROM lada.measm
                   WHERE sample_id = NEW.sample_id) + 1;
        END IF;
        RETURN NEW;
    END;
$$;

CREATE FUNCTION set_measm_status() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE status_id integer;
    BEGIN
        INSERT INTO lada.status_prot
            (meas_facil_id, date, text, measm_id, status_mp_id)
        VALUES ((SELECT meas_facil_id
                     FROM lada.sample
                     WHERE id = NEW.sample_id),
                now() AT TIME ZONE 'utc', NULL, NEW.id, 1)
        RETURNING id into status_id;
        UPDATE lada.measm SET status = status_id where id = NEW.id;
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


--
-- Name: update_time_status(); Type: FUNCTION; Schema: lada; Owner: -
--

CREATE FUNCTION update_tree_mod() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    BEGIN
        NEW.tree_mod = now() AT TIME ZONE 'utc';
        RETURN NEW;
    END;
$$;


--
-- Name: update_time_messung(); Type: FUNCTION; Schema: lada; Owner: -
--

CREATE FUNCTION update_tree_mod_measm() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    BEGIN
        NEW.tree_mod = now() AT TIME ZONE 'utc';
        UPDATE lada.meas_val SET tree_mod = now() AT TIME ZONE 'utc' WHERE measm_id = NEW.id;
        UPDATE lada.status_prot SET tree_mod = now() AT TIME ZONE 'utc' WHERE measm_id = NEW.id;
        RETURN NEW;
    END;
$$;


--
-- Name: update_time_probe(); Type: FUNCTION; Schema: lada; Owner: -
--

CREATE FUNCTION update_tree_mod_sample() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    BEGIN
        NEW.tree_mod = now() AT TIME ZONE 'utc';
        UPDATE lada.measm SET tree_mod = now() AT TIME ZONE 'utc' WHERE sample_id = NEW.id;
        UPDATE lada.geolocat SET tree_mod = now() AT TIME ZONE 'utc' WHERE sample_id = NEW.id;
        UPDATE lada.sample_specif_meas_val SET tree_mod = now() AT TIME ZONE 'utc' WHERE sample_id = NEW.id;
        RETURN NEW;
    END;
$$;

--
-- Name: update_status_messung(); Type: FUNCTION; Schema: lada; Owner: -
--

CREATE OR REPLACE FUNCTION update_status_measm() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    BEGIN
        CASE
            WHEN new.status_mp_id in (2, 3, 4, 5, 6, 7, 8, 10, 11, 12)
            THEN
                UPDATE lada.measm SET is_completed = true, status = NEW.id
                    WHERE id = NEW.measm_id;
            WHEN new.status_mp_id in (1, 9, 13)
            THEN
                UPDATE lada.measm SET is_completed = false, status = NEW.id
                    WHERE id = NEW.measm_id;
            ELSE
                UPDATE lada.measm SET status = NEW.id WHERE id = NEW.measm_id;
        END CASE;
        RETURN NEW;
    END
$$;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: measm_measm_ext_id_seq; Type: SEQUENCE; Schema: lada; Owner: -
--

CREATE SEQUENCE measm_measm_ext_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sample_sample_id_seq; Type: SEQUENCE; Schema: lada; Owner: -
--

CREATE SEQUENCE sample_sample_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: mpg; Type: TABLE; Schema: lada; Owner: -; Tablespace:
--

CREATE TABLE mpg (
    id serial PRIMARY KEY,
    comm_mpg character varying(1000) CHECK (trim(both ' ' from comm_mpg) <> ''),
    is_test boolean DEFAULT false NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    meas_facil_id character varying(5) NOT NULL REFERENCES master.meas_facil,
    appr_lab_id character varying(5) NOT NULL REFERENCES master.meas_facil,
    regulation_id integer NOT NULL REFERENCES master.regulation,
    opr_mode_id integer DEFAULT 1 REFERENCES master.opr_mode,
    admin_unit_id character varying(8) REFERENCES master.admin_unit,
    env_descrip_display character varying(100) CHECK
        (env_descrip_display ~ '^D:( [0-9][0-9]){12}$'),
    env_medium_id character varying(3) REFERENCES master.env_medium,
    sample_meth_id integer NOT NULL REFERENCES master.sample_meth,
    sample_pd character varying(2) NOT NULL CHECK (trim(both ' ' from sample_pd) <> ''),
    sample_pd_start_date integer NOT NULL,
    sample_pd_end_date integer NOT NULL,
    sample_pd_offset integer NOT NULL DEFAULT 0,
    valid_start_date integer NOT NULL CHECK(valid_start_date BETWEEN 1 AND 365),
    valid_end_date integer NOT NULL CHECK(valid_end_date BETWEEN 1 AND 365),
    sampler_id integer REFERENCES master.sampler,
    mpg_categ_id integer REFERENCES master.mpg_categ,
    comm_sample character varying(80) CHECK (trim(both ' ' from comm_sample) <> ''),
    rei_ag_gr_id integer REFERENCES master.rei_ag_gr,
    nucl_facil_gr_id integer REFERENCES master.nucl_facil_gr,
    meas_unit_id smallint REFERENCES master.meas_unit,
    sample_quant character varying(90) CHECK (trim(both ' ' from sample_quant) <> ''),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc') NOT NULL,
    CHECK (sample_pd = 'J'
               AND sample_pd_start_date BETWEEN valid_start_date AND valid_end_date
               AND sample_pd_end_date BETWEEN valid_start_date AND valid_end_date
               AND sample_pd_offset BETWEEN 0 AND 364
           OR sample_pd = 'H'
               AND sample_pd_start_date BETWEEN 1 AND 184
               AND sample_pd_end_date BETWEEN 1 AND 184
               AND sample_pd_offset BETWEEN 0 AND 183
           OR sample_pd = 'Q'
               AND sample_pd_start_date BETWEEN 1 AND 92
               AND sample_pd_end_date BETWEEN 1 AND 92
               AND sample_pd_offset BETWEEN 0 AND 91
           OR sample_pd = 'M'
               AND sample_pd_start_date BETWEEN 1 AND 31
               AND sample_pd_end_date BETWEEN 1 AND 31
               AND sample_pd_offset BETWEEN 0 AND 30
           OR sample_pd = 'W4'
               AND sample_pd_start_date BETWEEN 1 AND 28
               AND sample_pd_end_date BETWEEN 1 AND 28
               AND sample_pd_offset BETWEEN 0 AND 27
           OR sample_pd = 'W2'
               AND sample_pd_start_date BETWEEN 1 AND 14
               AND sample_pd_end_date BETWEEN 1 AND 14
               AND sample_pd_offset BETWEEN 0 AND 13
           OR sample_pd = 'W'
               AND sample_pd_start_date BETWEEN 1 AND 7
               AND sample_pd_end_date BETWEEN 1 AND 7
               AND sample_pd_offset BETWEEN 0 AND 6
           OR sample_pd = 'T'
               AND sample_pd_start_date = 1
               AND sample_pd_end_date = 1
               AND sample_pd_offset = 0
           ),
    CHECK (sample_pd_start_date <= sample_pd_end_date)
);
CREATE TRIGGER last_mod_mpg BEFORE UPDATE ON mpg FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE mpg_sample_specif (
    id SERIAL PRIMARY KEY,
    sample_specif_id character varying(3) REFERENCES master.sample_specif NOT NULL,
    mpg_id INTEGER REFERENCES mpg ON DELETE CASCADE NOT NULL,
    UNIQUE (sample_specif_id, mpg_id)
);

--
-- Name: mpg_mmt_mp; Type: TABLE; Schema: lada; Owner: -; Tablespace:
--

CREATE TABLE mpg_mmt_mp (
    id serial PRIMARY KEY,
    mpg_id integer NOT NULL REFERENCES mpg ON DELETE CASCADE,
    mmt_id character varying(2) NOT NULL REFERENCES master.mmt,
    UNIQUE(mpg_id, mmt_id),
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER last_mod_mpg_mmt_mp BEFORE UPDATE ON mpg_mmt_mp FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

CREATE TABLE mpg_mmt_mp_measd (
    id serial PRIMARY KEY,
    mpg_mmt_mp_id integer NOT NULL REFERENCES mpg_mmt_mp ON DELETE CASCADE,
    measd_id integer NOT NULL REFERENCES master.measd,
    UNIQUE (mpg_mmt_mp_id, measd_id)
);

--
-- Name: sample; Type: TABLE; Schema: lada; Owner: -; Tablespace:
--

CREATE TABLE sample (
    id serial PRIMARY KEY,
    ext_id character varying(16) UNIQUE NOT NULL CHECK (trim(both ' ' from ext_id) <> '')
        DEFAULT 'sss'
            || lpad(nextval('lada.sample_sample_id_seq')::varchar, 12, '0')
            || 'Y',
    is_test boolean DEFAULT false NOT NULL,
    meas_facil_id character varying(5) NOT NULL REFERENCES master.meas_facil,
    appr_lab_id character varying(5) NOT NULL REFERENCES master.meas_facil,
    main_sample_id character varying(20) CHECK (trim(both ' ' from main_sample_id) <> ''),
    regulation_id smallint REFERENCES master.regulation NOT NULL,
    opr_mode_id integer REFERENCES master.opr_mode NOT NULL,
    sample_meth_id smallint REFERENCES master.sample_meth NOT NULL,
    env_descrip_display character varying(100) CHECK(env_descrip_display ~ '^D:( [0-9][0-9]){12}$'),
    env_descrip_name character varying(100) CHECK (trim(both ' ' from env_descrip_name) <> ''),
    env_medium_id character varying(3) REFERENCES master.env_medium,
    sample_start_date timestamp without time zone,
    sample_end_date timestamp without time zone,
    mid_sample_date bigint,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    dataset_creator_id integer REFERENCES master.dataset_creator,
    sampler_id integer REFERENCES master.sampler,
    mpg_categ_id integer REFERENCES master.mpg_categ,
    mpg_id integer REFERENCES mpg,
    sched_start_date timestamp without time zone,
    sched_end_date timestamp without time zone,
    orig_date timestamp without time zone,
    tree_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    rei_ag_gr_id integer REFERENCES master.rei_ag_gr,
    nucl_facil_gr_id integer REFERENCES master.nucl_facil_gr,
    mid_coll_pd timestamp without time zone GENERATED ALWAYS AS (
        CASE
            WHEN (sample_start_date IS NULL) THEN NULL::timestamp without time zone
            WHEN ((sample_start_date IS NOT NULL) AND (sample_end_date IS NULL)) THEN sample_start_date
            ELSE (sample_start_date + ((sample_end_date - sample_start_date) / (2)::double precision))
        END) STORED,
    UNIQUE (is_test, meas_facil_id, main_sample_id),
    CHECK(sched_start_date <= sched_end_date)
);
CREATE TRIGGER last_mod_sample BEFORE UPDATE ON sample FOR EACH ROW EXECUTE PROCEDURE update_last_mod();
CREATE TRIGGER tree_mod_sample BEFORE UPDATE ON sample FOR EACH ROW EXECUTE PROCEDURE update_tree_mod_sample();


--
-- Name: comm_sample; Type: TABLE; Schema: lada; Owner: -; Tablespace:
--

CREATE TABLE comm_sample (
    id serial PRIMARY KEY,
    meas_facil_id character varying(5) NOT NULL REFERENCES master.meas_facil,
    date timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    text character varying(1024) NOT NULL CHECK (trim(both ' ' from text) <> ''),
    sample_id integer NOT NULL REFERENCES sample ON DELETE CASCADE
);


--
-- Name: geolocat; Type: TABLE; Schema: lada; Owner: -; Tablespace:
--

CREATE TABLE geolocat (
    id serial PRIMARY KEY,
    sample_id integer NOT NULL REFERENCES sample ON DELETE CASCADE,
    site_id integer NOT NULL REFERENCES master.site,
    type_regulation character varying(1) NOT NULL REFERENCES master.type_regulation,
    add_site_text character varying(100) CHECK (trim(both ' ' from add_site_text) <> ''),
    poi_id character varying(7) REFERENCES master.poi,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    tree_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    UNIQUE(sample_id, site_id, type_regulation),
    EXCLUDE (sample_id WITH =) WHERE (type_regulation = 'E')
);
CREATE TRIGGER last_mod_geolocat BEFORE UPDATE ON geolocat FOR EACH ROW EXECUTE PROCEDURE update_last_mod();
CREATE TRIGGER tree_mod_geolocat BEFORE UPDATE ON geolocat FOR EACH ROW EXECUTE PROCEDURE update_tree_mod();

--
-- Name: geolocat_mpg; Type: TABLE; Schema: lada; Owner: -; Tablespace:
--

CREATE TABLE geolocat_mpg (
    id serial PRIMARY KEY,
    mpg_id integer NOT NULL REFERENCES mpg ON DELETE CASCADE,
    site_id integer NOT NULL REFERENCES master.site,
    type_regulation character varying(1) NOT NULL REFERENCES master.type_regulation,
    add_site_text character varying(100) CHECK (trim(both ' ' from add_site_text) <> ''),
    poi_id character varying(7) REFERENCES master.poi,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    tree_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    EXCLUDE (mpg_id WITH =) WHERE (type_regulation = 'E')
);
CREATE TRIGGER last_mod_geolocat_mpg BEFORE UPDATE ON geolocat_mpg FOR EACH ROW EXECUTE PROCEDURE update_last_mod();

--
-- Name: sample_specif_meas_val; Type: TABLE; Schema: lada; Owner: -; Tablespace:
--

CREATE TABLE sample_specif_meas_val (
    id serial PRIMARY KEY,
    sample_id integer NOT NULL REFERENCES sample ON DELETE CASCADE,
    sample_specif_id character varying(3) NOT NULL REFERENCES master.sample_specif,
    meas_val double precision,
    error real,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    smaller_than character varying(1) CHECK (trim(both ' ' from smaller_than) <> ''),
    tree_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    UNIQUE (sample_id, sample_specif_id)
);
CREATE TRIGGER last_mod_zusatzwert BEFORE UPDATE ON sample_specif_meas_val FOR EACH ROW EXECUTE PROCEDURE update_last_mod();
CREATE TRIGGER tree_mod_zusatzwert BEFORE UPDATE ON sample_specif_meas_val FOR EACH ROW EXECUTE PROCEDURE update_tree_mod();


--
-- Name: measm; Type: TABLE; Schema: lada; Owner: -; Tablespace:
--

CREATE TABLE measm (
    id serial PRIMARY KEY,
    ext_id integer NOT NULL,
    sample_id integer NOT NULL REFERENCES sample ON DELETE CASCADE,
    min_sample_id character varying(4) CHECK (trim(both ' ' from min_sample_id) <> ''),
    mmt_id character varying(2) NOT NULL REFERENCES master.mmt ON DELETE NO ACTION,
    meas_pd integer,
    measm_start_date timestamp without time zone,
    is_completed boolean DEFAULT false NOT NULL,
    status integer,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    is_scheduled boolean DEFAULT false NOT NULL,
    tree_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    UNIQUE (sample_id, ext_id),
    UNIQUE (sample_id, min_sample_id)
);
CREATE TRIGGER last_mod_measm BEFORE UPDATE ON measm FOR EACH ROW EXECUTE PROCEDURE update_last_mod();
CREATE TRIGGER tree_mod_measm BEFORE UPDATE ON measm FOR EACH ROW EXECUTE PROCEDURE update_tree_mod_measm();
CREATE TRIGGER ext_id BEFORE INSERT ON lada.measm FOR EACH ROW EXECUTE PROCEDURE set_measm_ext_id();
CREATE TRIGGER status_measm AFTER INSERT ON lada.measm FOR EACH ROW EXECUTE PROCEDURE set_measm_status();

--
-- Name: comm_measm; Type: TABLE; Schema: lada; Owner: -; Tablespace:
--

CREATE TABLE comm_measm (
    id serial PRIMARY KEY,
    meas_facil_id character varying(5) NOT NULL REFERENCES master.meas_facil,
    date timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    text character varying(1024) NOT NULL CHECK (trim(both ' ' from text) <> ''),
    measm_id integer NOT NULL REFERENCES measm ON DELETE CASCADE
);


--
-- Name: meas_val; Type: TABLE; Schema: lada; Owner: -; Tablespace:
--

CREATE TABLE meas_val (
    id serial PRIMARY KEY,
    measm_id integer NOT NULL REFERENCES measm ON DELETE CASCADE,
    measd_id integer NOT NULL REFERENCES master.measd,
    less_than_lod character varying(1) CHECK (trim(both ' ' from less_than_lod) <> ''),
    meas_val double precision,
    error real,
    detect_lim double precision,
    meas_unit_id smallint NOT NULL REFERENCES master.meas_unit,
    is_threshold boolean DEFAULT false,
    last_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    tree_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    UNIQUE (measm_id, measd_id)
);
CREATE TRIGGER last_mod_meas_val BEFORE UPDATE ON meas_val FOR EACH ROW EXECUTE PROCEDURE update_last_mod();
CREATE TRIGGER tree_mod_meas_val BEFORE UPDATE ON meas_val FOR EACH ROW EXECUTE PROCEDURE update_tree_mod();

--
-- Name: status_prot; Type: TABLE; Schema: lada; Owner: -; Tablespace:
--

CREATE TABLE status_prot (
    id serial PRIMARY KEY,
    meas_facil_id character varying(5) NOT NULL REFERENCES master.meas_facil,
    date timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc'),
    text character varying(1024) CHECK (trim(both ' ' from text) <> ''),
    measm_id integer NOT NULL REFERENCES measm ON DELETE CASCADE,
    status_mp_id integer NOT NULL REFERENCES master.status_mp,
    tree_mod timestamp without time zone DEFAULT (now() AT TIME ZONE 'utc')
);
CREATE TRIGGER tree_mod_status_prot BEFORE UPDATE ON status_prot FOR EACH ROW EXECUTE PROCEDURE update_tree_mod();
CREATE TRIGGER update_measm_after_status_prot_created AFTER INSERT ON status_prot FOR EACH ROW EXECUTE PROCEDURE update_status_measm();

ALTER TABLE ONLY measm
    ADD CONSTRAINT messung_status_protokoll_id_fkey FOREIGN KEY (status) REFERENCES status_prot(id);

CREATE TABLE tag_link
(
    id serial PRIMARY KEY,
    sample_id integer REFERENCES sample ON DELETE CASCADE,
    measm_id integer REFERENCES measm ON DELETE CASCADE,
    tag_id integer NOT NULL REFERENCES master.tag ON DELETE CASCADE,
    date timestamp without time zone
        NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    CHECK(sample_id IS NOT NULL OR measm_id IS NOT NULL),
    UNIQUE (sample_id, tag_id),
    UNIQUE (measm_id, tag_id)
);
--
-- Name: measm_sample_id_idx; Type: INDEX; Schema: lada; Owner: -; Tablespace:
--

CREATE INDEX measm_sample_id_idx ON measm USING btree (sample_id);


--
-- Name: site_sample_id_idx; Type: INDEX; Schema: lada; Owner: -; Tablespace:
--

CREATE INDEX site_sample_id_idx ON geolocat USING btree (sample_id);


--
-- Name: sample_specif_meas_val_sample_id_idx; Type: INDEX; Schema: lada; Owner: -; Tablespace:
--

CREATE INDEX sample_specif_meas_val_sample_id_idx ON sample_specif_meas_val USING btree (sample_id);


--
-- Name: comm_sample_id_idx; Type: INDEX; Schema: lada; Owner: -; Tablespace:
--

CREATE INDEX comm_sample_id_idx ON comm_sample USING btree (sample_id);


--
-- Name: meas_val_measm_id_idx; Type: INDEX; Schema: lada; Owner: -; Tablespace:
--

CREATE INDEX meas_val_measm_id_idx ON meas_val USING btree (measm_id);


--
-- Name: status_prot_measm_id_idx; Type: INDEX; Schema: lada; Owner: -; Tablespace:
--

CREATE INDEX status_prot_measm_id_idx ON status_prot USING btree (measm_id);


--
-- Name: comm_measm_measm_id_idx; Type: INDEX; Schema: lada; Owner: -; Tablespace:
--

CREATE INDEX comm_measm_id_idx ON comm_measm USING btree (measm_id);

--NEW 221220

--Indices that are in production environment
CREATE INDEX geolocat_mpg_mpg_id_idx ON lada.geolocat_mpg USING btree (mpg_id ASC NULLS LAST);
CREATE INDEX sample_env_medium_id_idx ON lada.sample USING btree (env_medium_id);
CREATE INDEX sample_meas_facil_id_idx ON lada.sample USING btree (meas_facil_id);
CREATE INDEX sample_mid_collect_period_idx ON lada.sample USING btree (COALESCE(sample_start_date + (sample_end_date - sample_start_date) / 2::double precision, sample_start_date));
CREATE INDEX sample_nucl_facil_gr_id_idx ON lada.sample USING btree (nucl_facil_gr_id);
CREATE INDEX sample_opr_mode_id_idx ON lada.sample USING btree (opr_mode_id);
CREATE INDEX sample_regulation_id_idx ON lada.sample USING btree (regulation_id);
CREATE INDEX sample_rei_ag_gr_id_idx ON lada.sample USING btree (rei_ag_gr_id);
CREATE INDEX sample_sample_start_date_idx ON lada.sample USING btree (sample_start_date);
CREATE INDEX state_sample_mid_collect_period_id_ndx ON lada.sample USING btree (mid_coll_pd);
CREATE INDEX tag_link_measm_id_idx ON lada.tag_link USING btree (measm_id ASC NULLS LAST) TABLESPACE pg_default;
CREATE INDEX tag_link_sample_id_idx ON lada.tag_link USING btree (sample_id ASC NULLS LAST) TABLESPACE pg_default;
CREATE INDEX measm_status_idx ON lada.measm USING btree (status ASC NULLS LAST) TABLESPACE pg_default;
--CREATE INDEX audit_trail_object_id_idx ON lada.audit_trail USING btree (object_id ASC NULLS LAST) TABLESPACE pg_default;
--CREATE INDEX audit_trail_table_name_idx ON lada.audit_trail USING btree (table_name COLLATE pg_catalog."default" ASC NULLS LAST) TABLESPACE pg_default;
CREATE INDEX mpg_meas_facil_id_idx ON lada.mpg USING btree (meas_facil_id COLLATE pg_catalog."default" ASC NULLS LAST) TABLESPACE pg_default;
CREATE INDEX mpg_mmt_mp_mpg_id_idx ON lada.mpg_mmt_mp USING btree (mpg_id ASC NULLS LAST) TABLESPACE pg_default;
CREATE INDEX status_prot_status_mp_id_idx ON lada.status_prot USING btree (status_mp_id ASC NULLS LAST) TABLESPACE pg_default;
CREATE INDEX meas_val_measd_id_idx ON lada.meas_val USING btree (measd_id ASC NULLS LAST) TABLESPACE pg_default;

--
-- Name: COLUMN geolocat.type_regulation; Type: COMMENT; Schema: lada; Owner: -
--

COMMENT ON COLUMN geolocat.type_regulation IS 'E = Entnahmeort, U = Ursprungsort, Z = Ortszusatz';


--
-- Name: COLUMN sample.id; Type: COMMENT; Schema: lada; Owner: -
--

COMMENT ON COLUMN sample.id IS 'internal sample_id';


--
-- Name: COLUMN sample.test; Type: COMMENT; Schema: lada; Owner: -
--

COMMENT ON COLUMN sample.is_test IS 'is test data?';


--
-- Name: COLUMN sample.mst_id; Type: COMMENT; Schema: lada; Owner: -
--

COMMENT ON COLUMN sample.meas_facil_id IS 'ID for measuring facility';


--
-- Name: COLUMN sample.labor_mst_id; Type: COMMENT; Schema: lada; Owner: -
--

COMMENT ON COLUMN sample.appr_lab_id IS 'ID for approved laboratory';


--
-- Name: COLUMN sample.hauptproben_nr; Type: COMMENT; Schema: lada; Owner: -
--

COMMENT ON COLUMN sample.main_sample_id IS 'external sample id';


--
-- Name: COLUMN sample.ba_id; Type: COMMENT; Schema: lada; Owner: -
--

COMMENT ON COLUMN sample.opr_mode_id IS 'ID of operation mode (normal/Routine oder Störfall/intensiv)';


--
-- Name: COLUMN sample.probenart_id; Type: COMMENT; Schema: lada; Owner: -
--

COMMENT ON COLUMN sample.sample_meth_id IS 'ID of sample method (Einzel-, Sammel-, Misch- ...Probe)';


--
-- Name: COLUMN sample.env_descrip_display; Type: COMMENT; Schema: lada; Owner: -
--

COMMENT ON COLUMN sample.env_descrip_display IS 'Mediencodierung (Deskriptoren oder ADV-Codierung)';


--
-- Name: COLUMN sample.media; Type: COMMENT; Schema: lada; Owner: -
--

COMMENT ON COLUMN sample.env_descrip_name IS 'dekodierte Medienbezeichnung (aus env_descrip_display abgeleitet)';


--
-- Name: COLUMN sample.umw_id; Type: COMMENT; Schema: lada; Owner: -
--

COMMENT ON COLUMN sample.env_medium_id IS 'ID for environmental medium';


--
-- Name: COLUMN mpg.media_desk; Type: COMMENT; Schema: lada; Owner: -
--

COMMENT ON COLUMN mpg.env_descrip_display IS 'dekodierte Medienbezeichnung (aus env_descrip_display abgeleitet)';


COMMIT;
