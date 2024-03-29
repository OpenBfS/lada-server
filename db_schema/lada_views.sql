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
