\set ON_ERROR_STOP on

BEGIN;

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;


SET search_path = master, pg_catalog;

CREATE MATERIALIZED VIEW admin_border_view AS
 SELECT vg250_gem.gid AS id,
    vg250_gem.ags AS munic_id,
    true AS is_munic,
    vg250_gem.geom AS shape
   FROM geo.vg250_gem
UNION
 SELECT vg250_kr.gid AS id,
    vg250_kr.ags || '000' AS munic_id,
    false AS is_munic,
    vg250_kr.geom AS shape
   FROM geo.vg250_kr
UNION
 SELECT vg250_rb.gid AS id,
    vg250_rb.ags || '00000' AS munic_id,
    false AS is_munic,
    vg250_rb.geom AS shape
   FROM geo.vg250_rb
UNION
 SELECT vg250_bl.gid AS id,
    vg250_bl.ags || '000000' AS munic_id,
    false AS is_munic,
    vg250_bl.geom AS shape
   FROM geo.vg250_bl;

CREATE INDEX verwaltungsgrenze_sp_idx ON admin_border_view USING gist (shape);
CREATE INDEX verwaltungsgrenze_gem_id_idx ON admin_border_view (munic_id);

GRANT ALL ON TABLE master.admin_border_view TO lada;

COMMIT;
