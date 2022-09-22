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
    meas_val.less_than_LOD,
    meas_val.name,
    meas_val.meas_err,
    meas_val.detect_lim,
    meas_val.unit_id,
    meas_val.is_threshold,
    status_prot.status_comb,
    meas_val.last_mod
   FROM ((lada.meas_val
     JOIN lada.measm ON ((meas_val.measm_id = measm.id)))
     JOIN lada.status_prot ON (((measm.status = status_prot.id) AND (status_prot.status_comb <> 1))));
ALTER TABLE public.lada_meas_val OWNER TO postgres;
GRANT SELECT ON TABLE public.lada_meas_val TO lada;

--
-- Name: query_measm_view; Type: VIEW; Schema: lada; Owner: postgres
--

CREATE OR REPLACE VIEW lada.query_measm_view
 AS
 SELECT DISTINCT status_prot.measm_id
   FROM lada.status_prot
  WHERE (status_prot.status_comb = ANY (ARRAY[9, 13]));
ALTER TABLE lada.query_measm_view
    OWNER TO postgres;
GRANT SELECT ON TABLE lada.query_measm_view TO lada;
