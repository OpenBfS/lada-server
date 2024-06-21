--
-- PostgreSQL database dump
--

-- Dumped from database version 10.6
-- Dumped by pg_dump version 10.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: lada_messwert; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.lada_meas_val AS
 SELECT meas_val.id,
    meas_val.measm_id,
    meas_val.measd_id,
    meas_val.less_than_lod,
    meas_val.meas_val,
    meas_val.error,
    meas_val.detect_lim,
    meas_val.meas_unit_id,
    meas_val.is_threshold,
    status_prot.status_mp_id,
    meas_val.last_mod
   FROM ((lada.meas_val
     JOIN lada.measm ON ((meas_val.measm_id = measm.id)))
     JOIN lada.status_prot ON (((measm.status = status_prot.id) AND (status_prot.status_mp_id <> 1))));

CREATE OR REPLACE VIEW lada.meas_val_view
 AS
 SELECT meas_val.id,
    meas_val.measm_id,
    meas_val.measd_id,
    meas_val.less_than_lod,
    meas_val.meas_val,
    meas_val.error,
    meas_val.detect_lim,
    meas_val.meas_unit_id,
    meas_val.is_threshold,
    status_prot.status_mp_id,
    meas_val.last_mod
   FROM lada.meas_val
     JOIN lada.measm ON meas_val.measm_id = measm.id
     JOIN lada.status_prot ON measm.status = status_prot.id AND status_prot.status_mp_id <> 1;

--
-- Name: query_measm_view; Type: VIEW; Schema: lada; Owner: postgres
--

CREATE OR REPLACE VIEW lada.query_measm_view
 AS
 SELECT DISTINCT status_prot.measm_id
   FROM lada.status_prot
  WHERE (status_prot.status_mp_id = ANY (ARRAY[9, 13]));

--
-- Name: mv_tags_array; Type: MATERIALIZED VIEW; Schema: lada; Owner: postgres
-- Prebuild tag array for sid and mid (sample and measurement) to avoid timeout error for selections
--

CREATE MATERIALIZED VIEW lada.mv_tags_array AS
 SELECT sample.id AS pid, measm.id as mid
 , array_agg(tag.name) AS tags
 FROM lada.sample
 INNER JOIN lada.measm ON sample.id = measm.sample_id
 LEFT OUTER JOIN lada.tag_link_measm ON measm.id = tag_link_measm.measm_id
 LEFT OUTER JOIN lada.tag_link_sample ON sample.id = tag_link_sample.sample_id 
 JOIN master.tag ON (tag_link_sample.tag_id = tag.id OR tag_link_measm.tag_id = tag.id)
 GROUP BY sample.id, measm.id;
 
CREATE UNIQUE INDEX mv_tags_array_idx ON lada.mv_tags_array (pid, mid);
ALTER MATERIALIZED VIEW lada.mv_tags_array OWNER TO lada;

CREATE OR REPLACE FUNCTION lada.refresh_mv_tags_array()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY lada.mv_tags_array;
    RETURN NULL;
END;
$$;

CREATE TRIGGER mv_tags_array_update_sample
AFTER INSERT OR UPDATE OR DELETE
ON lada.tag_link_sample
FOR EACH STATEMENT EXECUTE PROCEDURE lada.refresh_mv_tags_array();

CREATE TRIGGER mv_tags_array_update_measm
AFTER INSERT OR UPDATE OR DELETE
ON lada.tag_link_measm
FOR EACH STATEMENT EXECUTE PROCEDURE lada.refresh_mv_tags_array();
