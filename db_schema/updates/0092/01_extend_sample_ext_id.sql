DROP VIEW land.probe;

ALTER TABLE lada.sample
ALTER COLUMN ext_id TYPE VARCHAR(19);

CREATE OR REPLACE VIEW land.probe
 AS
 SELECT sample.id,
    sample.ext_id,
    sample.is_test AS test,
    sample.meas_facil_id AS mst_id,
    sample.appr_lab_id AS labor_mst_id,
    sample.main_sample_id AS hauptproben_nr,
    sample.regulation_id AS datenbasis_id,
    sample.opr_mode_id AS ba_id,
    sample.sample_meth_id AS probenart_id,
    sample.env_descrip_display AS media_desk,
    sample.env_descrip_name AS media,
    sample.env_medium_id AS umw_id,
    sample.sample_start_date AS probeentnahme_beginn,
    sample.sample_end_date AS probeentnahme_ende,
    sample.mid_sample_date AS mittelungsdauer,
    sample.last_mod AS letzte_aenderung,
    sample.dataset_creator_id AS erzeuger_id,
    sample.sampler_id AS probe_nehmer_id,
    sample.mpg_categ_id AS mpl_id,
    sample.mpg_id AS mpr_id,
    sample.sched_start_date AS solldatum_beginn,
    sample.sched_end_date AS solldatum_ende,
    sample.tree_mod AS tree_modified,
    sample.rei_ag_gr_id AS rei_progpunkt_grp_id,
    sample.nucl_facil_gr_id AS kta_gruppe_id,
    sample.orig_date AS ursprungszeit,
    sample.mid_coll_pd AS mitte_sammelzeitraum
   FROM lada.sample;