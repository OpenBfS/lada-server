SET role lada;
--change audit trail to reflect english dm changes

SET search_path TO lada;

ALTER TABLE lada.audit_trail 
  ADD COLUMN sample_id INTEGER,
  ADD COLUMN measm_id INTEGER GENERATED always AS 
    (cast(row_data ->> 'measm_id' AS integer)) STORED;

CREATE OR REPLACE FUNCTION if_modified_func() RETURNS TRIGGER AS $body$
DECLARE
    audit_row lada.audit_trail;
    include_values boolean;
    log_diffs boolean;
    h_old jsonb;
    h_new jsonb;
    excluded_cols text[] = ARRAY[]::text[];
    item_id integer;
BEGIN
    IF TG_WHEN <> 'AFTER' THEN
        RAISE EXCEPTION 'lada.if_modified_func() may only run as an AFTER trigger';
    END IF;

    IF (TG_OP = 'DELETE') THEN
        item_id = OLD.id;
    ELSE
        item_id = NEW.id;
    END IF;

    audit_row = ROW(
        nextval('audit_trail_id_seq'),   -- id
        TG_TABLE_NAME::varchar,               -- table_name
        current_timestamp AT TIME ZONE 'utc', -- tstamp
        substring(TG_OP,1,1),                 -- action
        item_id,                              -- object_id
        NULL, NULL                            -- row_data, changed_fields
        );

    IF TG_ARGV[1] IS NOT NULL THEN
        excluded_cols = TG_ARGV[1]::text[];
    END IF;

    IF (TG_OP = 'UPDATE' AND TG_LEVEL = 'ROW') THEN
        audit_row.row_data = row_to_json(OLD)::JSONB;
        audit_row.changed_fields = row_to_json(NEW)::JSONB - audit_row.row_data - excluded_cols;
        IF audit_row.changed_fields = '{}'::jsonb THEN
            -- All changed fields are ignored. Skip this update.
            RETURN NULL;
        END IF;
    ELSIF (TG_OP = 'INSERT' AND TG_LEVEL = 'ROW') THEN
        audit_row.row_data = row_to_json(NEW)::JSONB;
        audit_row.changed_fields = jsonb_strip_nulls(row_to_json(NEW)::JSONB - excluded_cols);
    ELSIF (TG_OP = 'DELETE' AND TG_LEVEL = 'ROW') THEN
        audit_row.row_data = row_to_json(OLD)::JSONB;
        audit_row.changed_fields = jsonb_strip_nulls(row_to_json(OLD)::JSONB - excluded_cols);
    ELSE
        RAISE EXCEPTION '[lada.if_modified_func] - Trigger func added as trigger for unhandled case: %, %',TG_OP, TG_LEVEL;
        RETURN NULL;
    END IF;
    INSERT INTO audit_trail(id, table_name, tstamp, action, object_id, row_data, changed_fields)
    VALUES (
        audit_row.id, 
        audit_row.table_name,
        audit_row.tstamp,
        audit_row.action,
        audit_row.object_id,
        audit_row.row_data,
        audit_row.changed_fields);
    RETURN NULL;
END;
$body$
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = lada, public;

UPDATE lada.audit_trail
SET sample_id = 
    coalesce(cast(row_data ->> 'sample_id' AS integer),
        (SELECT sample_id FROM lada.measm WHERE id = measm_id));

CREATE OR REPLACE FUNCTION update_audit_sample_id()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
    BEGIN
        IF NEW.table_name = 'sample' THEN 
            NEW.sample_id = NEW.object_id;
        ELSE 
            NEW.sample_id =
                coalesce(cast(NEW.row_data ->> 'sample_id' AS integer),
                    (SELECT sample_id FROM lada.measm
                     WHERE id = cast(NEW.row_data ->> 'measm_id' AS integer)));
        END IF;
        RETURN NEW;
    END;
$BODY$;

CREATE TRIGGER tree_mod_sample
    BEFORE INSERT OR UPDATE 
    ON lada.audit_trail
    FOR EACH ROW
    EXECUTE FUNCTION lada.update_audit_sample_id();

CREATE INDEX audit_trail_sample_id_idx
    ON lada.audit_trail USING btree (sample_id ASC NULLS LAST);
CREATE INDEX audit_trail_measm_id_idx
    ON lada.audit_trail USING btree (measm_id ASC NULLS LAST);

ALTER TABLE master.audit_trail
  ADD COLUMN site_id INTEGER GENERATED always AS 
    (cast(row_data ->> 'id' AS integer)) STORED;

CREATE INDEX audit_trail_site_id_idx
    ON master.audit_trail USING btree (site_id ASC NULLS LAST);

DROP VIEW lada.audit_trail_sample_view CASCADE;
CREATE OR REPLACE VIEW lada.audit_trail_sample_view AS
SELECT
    lada_audit.id,
    lada_audit.table_name,
    lada_audit.action,
    lada_audit.object_id,
    lada_audit.tstamp,
    lada_audit.row_data,
    lada_audit.changed_fields,
    lada_audit.sample_id,
    lada_audit.measm_id,
    NULL::integer AS site_id
FROM lada.audit_trail lada_audit
UNION
SELECT master_audit.id,
    master_audit.table_name,
    master_audit.action,
    master_audit.object_id,
    master_audit.tstamp,
    master_audit.row_data,
    master_audit.changed_fields,
    NULL::integer AS measm_id,
    NULL::integer AS sample_id,
    master_audit.site_id
FROM master.audit_trail master_audit;

CREATE OR REPLACE VIEW audit_trail_measm_view AS
SELECT
    audit_trail.id,
    audit_trail.table_name,
    audit_trail.tstamp,
    audit_trail.action,
    audit_trail.object_id,
    audit_trail.row_data,
    audit_trail.changed_fields,
    audit_trail.measm_id
FROM audit_trail
WHERE audit_trail.table_name = 'measm' OR audit_trail.measm_id IS NOT NULL;

DROP VIEW land.audit_trail;
CREATE VIEW land.audit_trail AS SELECT
    id,
    CASE
        WHEN table_name = 'sample_specif_meas_val' THEN 'zusatz_wert'
        WHEN table_name = 'comm_measm' THEN 'kommentar_m'
        WHEN table_name = 'comm_sample' THEN 'kommentar_p'
        WHEN table_name = 'sample' THEN 'probe'
        WHEN table_name = 'geolocat' THEN 'ortszuordnung'
        WHEN table_name = 'meas_val' THEN 'messwert'
        WHEN table_name = 'measm' THEN 'messung'
        ELSE table_name
    END AS table_name,
    tstamp,
    action,
    object_id,
    CASE
        WHEN table_name = 'sample_specif_meas_val' THEN public.jsonb_rename_keys(row_data, ARRAY['sample_id'::text, 'probe_id'::text, 'sample_specif_id', 'pzs_id', 'meas_val', 'messwert_pzs', 'meas_err', 'messfehler', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified', 'smaller_than', 'kleiner_als', 'nwg_zu_messwert', 'detect_lim'])
        WHEN table_name = 'comm_measm' THEN public.jsonb_rename_keys(row_data, ARRAY['meas_facil_id', 'mst_id', 'date', 'datum', 'measm_id', 'messungs_id'])
        WHEN table_name = 'comm_sample' THEN public.jsonb_rename_keys(row_data, ARRAY['meas_facil_id', 'mst_id', 'date', 'datum', 'sample_id', 'probe_id'])
        WHEN table_name = 'sample' THEN public.jsonb_rename_keys(row_data, ARRAY['is_test', 'test', 'meas_facil_id', 'mst_id', 'appr_lab_id', 'labor_mst_id', 'main_sample_id', 'hauptproben_nr', 'regulation_id', 'datenbasis_id', 'opr_mode_id', 'ba_id', 'sample_meth_id', 'probenart_id', 'env_descrip_display', 'media_desk', 'env_descrip_name', 'media', 'env_medium_id', 'umw_id', 'sample_start_date', 'probeentnahme_beginn', 'sample_end_date', 'probeentnahme_ende', 'mid_sample_date', 'mittelungsdauer', 'last_mod', 'letzte_aenderung', 'dataset_creator_id', 'erzeuger_id', 'sampler_id', 'probe_nehmer_id', 'mpg_categ_id', 'mpl_id', 'mpg_id', 'mpr_id', 'sched_start_date', 'solldatum_beginn', 'sched_end_date', 'solldatum_ende', 'tree_mod', 'tree_modified', 'rei_ag_gr_id', 'rei_progpunkt_grp_id', 'nucl_facil_gr_id', 'kta_gruppe_id', 'orig_date', 'ursprungszeit', 'mid_coll_pd', 'mitte_sammelzeitraum'])
        WHEN table_name = 'geolocat' THEN public.jsonb_rename_keys(row_data, ARRAY['sample_id', 'probe_id', 'site_id', 'ort_id', 'type_regulation', 'ortszuordnung_typ', 'add_site_text', 'ortszusatztext', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified', 'poi_id', 'oz_id'])
        WHEN table_name = 'meas_val' THEN public.jsonb_rename_keys(row_data, ARRAY['measm_id', 'messungs_id', 'measd_id', 'messgroesse_id', 'less_than_lod', 'messwert_nwg', 'meas_val', 'messwert', 'error', 'messfehler', 'detect_lim', 'nwg_zu_messwert', 'meas_unit_id', 'meh_id', 'is_threshold', 'grenzwertueberschreitung', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified'])
        WHEN table_name = 'measm' THEN public.jsonb_rename_keys(row_data, ARRAY['sample_id', 'probe_id', 'min_sample_id', 'nebenproben_nr', 'meas_pd', 'messdauer', 'measm_start_date', 'messzeitpunkt', 'is_completed', 'fertig', 'last_mod', 'letzte_aenderung', 'is_scheduled', 'geplant', 'tree_mod', 'tree_modified'])
        ELSE row_data
    END AS row_data,
    CASE
        WHEN table_name = 'sample_specif_meas_val' THEN public.jsonb_rename_keys(changed_fields, ARRAY['sample_id', 'probe_id', 'sample_specif_id', 'pzs_id', 'meas_val', 'messwert_pzs', 'meas_err', 'messfehler', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified', 'smaller_than', 'kleiner_als', 'nwg_zu_messwert', 'detect_lim'])
        WHEN table_name = 'comm_measm' THEN public.jsonb_rename_keys(changed_fields, ARRAY['meas_facil_id', 'mst_id', 'date', 'datum', 'measm_id', 'messungs_id'])
        WHEN table_name = 'comm_sample' THEN public.jsonb_rename_keys(changed_fields, ARRAY['meas_facil_id', 'mst_id', 'date', 'datum', 'sample_id', 'probe_id'])
        WHEN table_name = 'sample' THEN public.jsonb_rename_keys(changed_fields, ARRAY['is_test', 'test', 'meas_facil_id', 'mst_id', 'appr_lab_id', 'labor_mst_id', 'main_sample_id', 'hauptproben_nr', 'regulation_id', 'datenbasis_id', 'opr_mode_id', 'ba_id', 'sample_meth_id', 'probenart_id', 'env_descrip_display', 'media_desk', 'env_descrip_name', 'media', 'env_medium_id', 'umw_id', 'sample_start_date', 'probeentnahme_beginn', 'sample_end_date', 'probeentnahme_ende', 'mid_sample_date', 'mittelungsdauer', 'last_mod', 'letzte_aenderung', 'dataset_creator_id', 'erzeuger_id', 'sampler_id', 'probe_nehmer_id', 'mpg_categ_id', 'mpl_id', 'mpg_id', 'mpr_id', 'sched_start_date', 'solldatum_beginn', 'sched_end_date', 'solldatum_ende', 'tree_mod', 'tree_modified', 'rei_ag_gr_id', 'rei_progpunkt_grp_id', 'nucl_facil_gr_id', 'kta_gruppe_id', 'orig_date', 'ursprungszeit', 'mid_coll_pd', 'mitte_sammelzeitraum'])
        WHEN table_name = 'geolocat' THEN public.jsonb_rename_keys(changed_fields, ARRAY['sample_id', 'probe_id', 'site_id', 'ort_id', 'type_regulation', 'ortszuordnung_typ', 'add_site_text', 'ortszusatztext', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified', 'poi_id', 'oz_id'])
        WHEN table_name = 'meas_val' THEN public.jsonb_rename_keys(changed_fields, ARRAY['measm_id', 'messungs_id', 'measd_id', 'messgroesse_id', 'less_than_lod', 'messwert_nwg', 'meas_val', 'messwert', 'error', 'messfehler', 'detect_lim', 'nwg_zu_messwert', 'meas_unit_id', 'meh_id', 'is_threshold', 'grenzwertueberschreitung', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified'])
        WHEN table_name = 'measm' THEN public.jsonb_rename_keys(changed_fields, ARRAY['sample_id', 'probe_id', 'min_sample_id', 'nebenproben_nr', 'meas_pd', 'messdauer', 'measm_start_date', 'messzeitpunkt', 'is_completed', 'fertig', 'last_mod', 'letzte_aenderung', 'is_scheduled', 'geplant', 'tree_mod', 'tree_modified'])
        ELSE changed_fields
    END AS changed_fields
FROM lada.audit_trail;

DROP VIEW land.audit_trail_messung;
CREATE VIEW land.audit_trail_messung AS SELECT
    id,
    CASE
        WHEN table_name = 'sample_specif_meas_val' THEN 'zusatz_wert'
        WHEN table_name = 'comm_measm' THEN 'kommentar_m'
        WHEN table_name = 'comm_sample' THEN 'kommentar_p'
        WHEN table_name = 'sample' THEN 'probe'
        WHEN table_name = 'geolocat' THEN 'ortszuordnung'
        WHEN table_name = 'meas_val' THEN 'messwert'
        WHEN table_name = 'measm' THEN 'messung'
        ELSE table_name
    END AS table_name,
    tstamp,
    action,
    object_id,
    CASE
        WHEN table_name = 'sample_specif_meas_val' THEN public.jsonb_rename_keys(row_data, ARRAY['sample_id'::text, 'probe_id'::text, 'sample_specif_id', 'pzs_id', 'meas_val', 'messwert_pzs', 'meas_err', 'messfehler', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified', 'smaller_than', 'kleiner_als', 'nwg_zu_messwert', 'detect_lim'])
        WHEN table_name = 'comm_measm' THEN public.jsonb_rename_keys(row_data, ARRAY['meas_facil_id', 'mst_id', 'date', 'datum', 'measm_id', 'messungs_id'])
        WHEN table_name = 'comm_sample' THEN public.jsonb_rename_keys(row_data, ARRAY['meas_facil_id', 'mst_id', 'date', 'datum', 'sample_id', 'probe_id'])
        WHEN table_name = 'sample' THEN public.jsonb_rename_keys(row_data, ARRAY['is_test', 'test', 'meas_facil_id', 'mst_id', 'appr_lab_id', 'labor_mst_id', 'main_sample_id', 'hauptproben_nr', 'regulation_id', 'datenbasis_id', 'opr_mode_id', 'ba_id', 'sample_meth_id', 'probenart_id', 'env_descrip_display', 'media_desk', 'env_descrip_name', 'media', 'env_medium_id', 'umw_id', 'sample_start_date', 'probeentnahme_beginn', 'sample_end_date', 'probeentnahme_ende', 'mid_sample_date', 'mittelungsdauer', 'last_mod', 'letzte_aenderung', 'dataset_creator_id', 'erzeuger_id', 'sampler_id', 'probe_nehmer_id', 'mpg_categ_id', 'mpl_id', 'mpg_id', 'mpr_id', 'sched_start_date', 'solldatum_beginn', 'sched_end_date', 'solldatum_ende', 'tree_mod', 'tree_modified', 'rei_ag_gr_id', 'rei_progpunkt_grp_id', 'nucl_facil_gr_id', 'kta_gruppe_id', 'orig_date', 'ursprungszeit', 'mid_coll_pd', 'mitte_sammelzeitraum'])
        WHEN table_name = 'geolocat' THEN public.jsonb_rename_keys(row_data, ARRAY['sample_id', 'probe_id', 'site_id', 'ort_id', 'type_regulation', 'ortszuordnung_typ', 'add_site_text', 'ortszusatztext', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified', 'poi_id', 'oz_id'])
        WHEN table_name = 'meas_val' THEN public.jsonb_rename_keys(row_data, ARRAY['measm_id', 'messungs_id', 'measd_id', 'messgroesse_id', 'less_than_lod', 'messwert_nwg', 'meas_val', 'messwert', 'error', 'messfehler', 'detect_lim', 'nwg_zu_messwert', 'meas_unit_id', 'meh_id', 'is_threshold', 'grenzwertueberschreitung', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified'])
        WHEN table_name = 'measm' THEN public.jsonb_rename_keys(row_data, ARRAY['sample_id', 'probe_id', 'min_sample_id', 'nebenproben_nr', 'meas_pd', 'messdauer', 'measm_start_date', 'messzeitpunkt', 'is_completed', 'fertig', 'last_mod', 'letzte_aenderung', 'is_scheduled', 'geplant', 'tree_mod', 'tree_modified'])
        ELSE row_data
    END AS row_data,
    CASE
        WHEN table_name = 'sample_specif_meas_val' THEN public.jsonb_rename_keys(changed_fields, ARRAY['sample_id', 'probe_id', 'sample_specif_id', 'pzs_id', 'meas_val', 'messwert_pzs', 'meas_err', 'messfehler', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified', 'smaller_than', 'kleiner_als', 'nwg_zu_messwert', 'detect_lim'])
        WHEN table_name = 'comm_measm' THEN public.jsonb_rename_keys(changed_fields, ARRAY['meas_facil_id', 'mst_id', 'date', 'datum', 'measm_id', 'messungs_id'])
        WHEN table_name = 'comm_sample' THEN public.jsonb_rename_keys(changed_fields, ARRAY['meas_facil_id', 'mst_id', 'date', 'datum', 'sample_id', 'probe_id'])
        WHEN table_name = 'sample' THEN public.jsonb_rename_keys(changed_fields, ARRAY['is_test', 'test', 'meas_facil_id', 'mst_id', 'appr_lab_id', 'labor_mst_id', 'main_sample_id', 'hauptproben_nr', 'regulation_id', 'datenbasis_id', 'opr_mode_id', 'ba_id', 'sample_meth_id', 'probenart_id', 'env_descrip_display', 'media_desk', 'env_descrip_name', 'media', 'env_medium_id', 'umw_id', 'sample_start_date', 'probeentnahme_beginn', 'sample_end_date', 'probeentnahme_ende', 'mid_sample_date', 'mittelungsdauer', 'last_mod', 'letzte_aenderung', 'dataset_creator_id', 'erzeuger_id', 'sampler_id', 'probe_nehmer_id', 'mpg_categ_id', 'mpl_id', 'mpg_id', 'mpr_id', 'sched_start_date', 'solldatum_beginn', 'sched_end_date', 'solldatum_ende', 'tree_mod', 'tree_modified', 'rei_ag_gr_id', 'rei_progpunkt_grp_id', 'nucl_facil_gr_id', 'kta_gruppe_id', 'orig_date', 'ursprungszeit', 'mid_coll_pd', 'mitte_sammelzeitraum'])
        WHEN table_name = 'geolocat' THEN public.jsonb_rename_keys(changed_fields, ARRAY['sample_id', 'probe_id', 'site_id', 'ort_id', 'type_regulation', 'ortszuordnung_typ', 'add_site_text', 'ortszusatztext', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified', 'poi_id', 'oz_id'])
        WHEN table_name = 'meas_val' THEN public.jsonb_rename_keys(changed_fields, ARRAY['measm_id', 'messungs_id', 'measd_id', 'messgroesse_id', 'less_than_lod', 'messwert_nwg', 'meas_val', 'messwert', 'error', 'messfehler', 'detect_lim', 'nwg_zu_messwert', 'meas_unit_id', 'meh_id', 'is_threshold', 'grenzwertueberschreitung', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified'])
        WHEN table_name = 'measm' THEN public.jsonb_rename_keys(changed_fields, ARRAY['sample_id', 'probe_id', 'min_sample_id', 'nebenproben_nr', 'meas_pd', 'messdauer', 'measm_start_date', 'messzeitpunkt', 'is_completed', 'fertig', 'last_mod', 'letzte_aenderung', 'is_scheduled', 'geplant', 'tree_mod', 'tree_modified'])
        ELSE changed_fields
    END AS changed_fields,
    measm_id AS messungs_id
FROM audit_trail
WHERE audit_trail.table_name = 'measm' OR audit_trail.measm_id IS NOT NULL;

CREATE VIEW land.audit_trail_probe AS SELECT
    id,
    CASE
        WHEN table_name = 'sample_specif_meas_val' THEN 'zusatz_wert'
        WHEN table_name = 'comm_measm' THEN 'kommentar_m'
        WHEN table_name = 'comm_sample' THEN 'kommentar_p'
        WHEN table_name = 'sample' THEN 'probe'
        WHEN table_name = 'geolocat' THEN 'ortszuordnung'
        WHEN table_name = 'meas_val' THEN 'messwert'
        WHEN table_name = 'measm' THEN 'messung'
		WHEN table_name = 'site' THEN 'ort'
        ELSE table_name
    END AS table_name,
    tstamp,
    action,
    object_id,
    CASE
        WHEN table_name = 'sample_specif_meas_val' THEN public.jsonb_rename_keys(row_data, ARRAY['sample_id'::text, 'probe_id'::text, 'sample_specif_id', 'pzs_id', 'meas_val', 'messwert_pzs', 'meas_err', 'messfehler', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified', 'smaller_than', 'kleiner_als', 'nwg_zu_messwert', 'detect_lim'])
        WHEN table_name = 'comm_measm' THEN public.jsonb_rename_keys(row_data, ARRAY['meas_facil_id', 'mst_id', 'date', 'datum', 'measm_id', 'messungs_id'])
        WHEN table_name = 'comm_sample' THEN public.jsonb_rename_keys(row_data, ARRAY['meas_facil_id', 'mst_id', 'date', 'datum', 'sample_id', 'probe_id'])
        WHEN table_name = 'sample' THEN public.jsonb_rename_keys(row_data, ARRAY['is_test', 'test', 'meas_facil_id', 'mst_id', 'appr_lab_id', 'labor_mst_id', 'main_sample_id', 'hauptproben_nr', 'regulation_id', 'datenbasis_id', 'opr_mode_id', 'ba_id', 'sample_meth_id', 'probenart_id', 'env_descrip_display', 'media_desk', 'env_descrip_name', 'media', 'env_medium_id', 'umw_id', 'sample_start_date', 'probeentnahme_beginn', 'sample_end_date', 'probeentnahme_ende', 'mid_sample_date', 'mittelungsdauer', 'last_mod', 'letzte_aenderung', 'dataset_creator_id', 'erzeuger_id', 'sampler_id', 'probe_nehmer_id', 'mpg_categ_id', 'mpl_id', 'mpg_id', 'mpr_id', 'sched_start_date', 'solldatum_beginn', 'sched_end_date', 'solldatum_ende', 'tree_mod', 'tree_modified', 'rei_ag_gr_id', 'rei_progpunkt_grp_id', 'nucl_facil_gr_id', 'kta_gruppe_id', 'orig_date', 'ursprungszeit', 'mid_coll_pd', 'mitte_sammelzeitraum'])
        WHEN table_name = 'geolocat' THEN public.jsonb_rename_keys(row_data, ARRAY['sample_id', 'probe_id', 'site_id', 'ort_id', 'type_regulation', 'ortszuordnung_typ', 'add_site_text', 'ortszusatztext', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified', 'poi_id', 'oz_id'])
        WHEN table_name = 'meas_val' THEN public.jsonb_rename_keys(row_data, ARRAY['measm_id', 'messungs_id', 'measd_id', 'messgroesse_id', 'less_than_lod', 'messwert_nwg', 'meas_val', 'messwert', 'error', 'messfehler', 'detect_lim', 'nwg_zu_messwert', 'meas_unit_id', 'meh_id', 'is_threshold', 'grenzwertueberschreitung', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified'])
        WHEN table_name = 'measm' THEN public.jsonb_rename_keys(row_data, ARRAY['sample_id', 'probe_id', 'min_sample_id', 'nebenproben_nr', 'meas_pd', 'messdauer', 'measm_start_date', 'messzeitpunkt', 'is_completed', 'fertig', 'last_mod', 'letzte_aenderung', 'is_scheduled', 'geplant', 'tree_mod', 'tree_modified'])
		WHEN table_name = 'site' THEN public.jsonb_rename_keys(row_data, ARRAY['network_id', 'netzbetreiber_id', 'ext_id', 'ort_id', 'long_text', 'langtext', 'state_id', 'staat_id', 'munic_id', 'gem_id', 'is_fuzzy', 'unscharf', 'nuts_id', 'nuts_code', 'spat_ref_sys_id', 'kda_id', 'coord_x_ext', 'koord_x_extern', 'coord_y_ext', 'koord_y_extern', 'alt', 'hoehe_land', 'last_mod', 'letzte_aenderung', 'site_class_id', 'ort_typ', 'short_text', 'kurztext', 'rei_report_text', 'berichtstext', 'rei_zone', 'zone', 'rei_sector', 'sektor', 'rei_competence', 'zustaendigkeit', 'rei_opr_mode', 'mp_art', 'is_rei_active', 'aktiv', 'rei_nucl_facil_gr_id', 'kta_gruppe_id', 'poi_id', 'oz_id', 'height_asl', 'hoehe_ueber_nn', 'rei_ag_gr_id', 'rei_progpunkt_grp_id', 'munic_div_id', 'gem_unt_id'])
        ELSE row_data
    END AS row_data,
    CASE
        WHEN table_name = 'sample_specif_meas_val' THEN public.jsonb_rename_keys(changed_fields, ARRAY['sample_id', 'probe_id', 'sample_specif_id', 'pzs_id', 'meas_val', 'messwert_pzs', 'meas_err', 'messfehler', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified', 'smaller_than', 'kleiner_als', 'nwg_zu_messwert', 'detect_lim'])
        WHEN table_name = 'comm_measm' THEN public.jsonb_rename_keys(changed_fields, ARRAY['meas_facil_id', 'mst_id', 'date', 'datum', 'measm_id', 'messungs_id'])
        WHEN table_name = 'comm_sample' THEN public.jsonb_rename_keys(changed_fields, ARRAY['meas_facil_id', 'mst_id', 'date', 'datum', 'sample_id', 'probe_id'])
        WHEN table_name = 'sample' THEN public.jsonb_rename_keys(changed_fields, ARRAY['is_test', 'test', 'meas_facil_id', 'mst_id', 'appr_lab_id', 'labor_mst_id', 'main_sample_id', 'hauptproben_nr', 'regulation_id', 'datenbasis_id', 'opr_mode_id', 'ba_id', 'sample_meth_id', 'probenart_id', 'env_descrip_display', 'media_desk', 'env_descrip_name', 'media', 'env_medium_id', 'umw_id', 'sample_start_date', 'probeentnahme_beginn', 'sample_end_date', 'probeentnahme_ende', 'mid_sample_date', 'mittelungsdauer', 'last_mod', 'letzte_aenderung', 'dataset_creator_id', 'erzeuger_id', 'sampler_id', 'probe_nehmer_id', 'mpg_categ_id', 'mpl_id', 'mpg_id', 'mpr_id', 'sched_start_date', 'solldatum_beginn', 'sched_end_date', 'solldatum_ende', 'tree_mod', 'tree_modified', 'rei_ag_gr_id', 'rei_progpunkt_grp_id', 'nucl_facil_gr_id', 'kta_gruppe_id', 'orig_date', 'ursprungszeit', 'mid_coll_pd', 'mitte_sammelzeitraum'])
        WHEN table_name = 'geolocat' THEN public.jsonb_rename_keys(changed_fields, ARRAY['sample_id', 'probe_id', 'site_id', 'ort_id', 'type_regulation', 'ortszuordnung_typ', 'add_site_text', 'ortszusatztext', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified', 'poi_id', 'oz_id'])
        WHEN table_name = 'meas_val' THEN public.jsonb_rename_keys(changed_fields, ARRAY['measm_id', 'messungs_id', 'measd_id', 'messgroesse_id', 'less_than_lod', 'messwert_nwg', 'meas_val', 'messwert', 'error', 'messfehler', 'detect_lim', 'nwg_zu_messwert', 'meas_unit_id', 'meh_id', 'is_threshold', 'grenzwertueberschreitung', 'last_mod', 'letzte_aenderung', 'tree_mod', 'tree_modified'])
        WHEN table_name = 'measm' THEN public.jsonb_rename_keys(changed_fields, ARRAY['sample_id', 'probe_id', 'min_sample_id', 'nebenproben_nr', 'meas_pd', 'messdauer', 'measm_start_date', 'messzeitpunkt', 'is_completed', 'fertig', 'last_mod', 'letzte_aenderung', 'is_scheduled', 'geplant', 'tree_mod', 'tree_modified'])
		WHEN table_name = 'site' THEN public.jsonb_rename_keys(changed_fields, ARRAY['network_id', 'netzbetreiber_id', 'ext_id', 'ort_id', 'long_text', 'langtext', 'state_id', 'staat_id', 'munic_id', 'gem_id', 'is_fuzzy', 'unscharf', 'nuts_id', 'nuts_code', 'spat_ref_sys_id', 'kda_id', 'coord_x_ext', 'koord_x_extern', 'coord_y_ext', 'koord_y_extern', 'alt', 'hoehe_land', 'last_mod', 'letzte_aenderung', 'site_class_id', 'ort_typ', 'short_text', 'kurztext', 'rei_report_text', 'berichtstext', 'rei_zone', 'zone', 'rei_sector', 'sektor', 'rei_competence', 'zustaendigkeit', 'rei_opr_mode', 'mp_art', 'is_rei_active', 'aktiv', 'rei_nucl_facil_gr_id', 'kta_gruppe_id', 'poi_id', 'oz_id', 'height_asl', 'hoehe_ueber_nn', 'rei_ag_gr_id', 'rei_progpunkt_grp_id', 'munic_div_id', 'gem_unt_id'])
        ELSE changed_fields
    END AS changed_fields,
	sample_id AS probe_id,
	measm_id AS messungs_id,
	site_id AS ort_id
FROM lada.audit_trail_sample_view;

DROP VIEW stamm.audit_trail;
CREATE VIEW stamm.audit_trail
 AS
 SELECT audit_trail.id,
	CASE
		WHEN table_name = 'site' THEN 'ort'
		ELSE table_name
	END AS table_name,
    audit_trail.tstamp,
    audit_trail.action,
    audit_trail.object_id,
	CASE
		WHEN table_name = 'site' THEN public.jsonb_rename_keys(row_data, ARRAY['network_id', 'netzbetreiber_id', 'ext_id', 'ort_id', 'long_text', 'langtext', 'state_id', 'staat_id', 'munic_id', 'gem_id', 'is_fuzzy', 'unscharf', 'nuts_id', 'nuts_code', 'spat_ref_sys_id', 'kda_id', 'coord_x_ext', 'koord_x_extern', 'coord_y_ext', 'koord_y_extern', 'alt', 'hoehe_land', 'last_mod', 'letzte_aenderung', 'site_class_id', 'ort_typ', 'short_text', 'kurztext', 'rei_report_text', 'berichtstext', 'rei_zone', 'zone', 'rei_sector', 'sektor', 'rei_competence', 'zustaendigkeit', 'rei_opr_mode', 'mp_art', 'is_rei_active', 'aktiv', 'rei_nucl_facil_gr_id', 'kta_gruppe_id', 'poi_id', 'oz_id', 'height_asl', 'hoehe_ueber_nn', 'rei_ag_gr_id', 'rei_progpunkt_grp_id', 'munic_div_id', 'gem_unt_id'])
        ELSE row_data
	END AS row_data,
	CASE
		WHEN table_name = 'site' THEN public.jsonb_rename_keys(changed_fields, ARRAY['network_id', 'netzbetreiber_id', 'ext_id', 'ort_id', 'long_text', 'langtext', 'state_id', 'staat_id', 'munic_id', 'gem_id', 'is_fuzzy', 'unscharf', 'nuts_id', 'nuts_code', 'spat_ref_sys_id', 'kda_id', 'coord_x_ext', 'koord_x_extern', 'coord_y_ext', 'koord_y_extern', 'alt', 'hoehe_land', 'last_mod', 'letzte_aenderung', 'site_class_id', 'ort_typ', 'short_text', 'kurztext', 'rei_report_text', 'berichtstext', 'rei_zone', 'zone', 'rei_sector', 'sektor', 'rei_competence', 'zustaendigkeit', 'rei_opr_mode', 'mp_art', 'is_rei_active', 'aktiv', 'rei_nucl_facil_gr_id', 'kta_gruppe_id', 'poi_id', 'oz_id', 'height_asl', 'hoehe_ueber_nn', 'rei_ag_gr_id', 'rei_progpunkt_grp_id', 'munic_div_id', 'gem_unt_id'])
		ELSE audit_trail.changed_fields
	END AS changed_fields
   FROM master.audit_trail;

SET search_path TO master;
CREATE OR REPLACE FUNCTION if_modified_func() RETURNS TRIGGER AS $body$
DECLARE
    audit_row audit_trail;
    include_values boolean;
    log_diffs boolean;
    h_old jsonb;
    h_new jsonb;
    excluded_cols text[] = ARRAY[]::text[];
BEGIN
    IF TG_WHEN <> 'AFTER' THEN
        RAISE EXCEPTION 'if_modified_func() may only run as an AFTER trigger';
    END IF;

    -- Do nothing on delete.
    IF (TG_OP = 'DELETE') THEN
        RETURN NULL;
    END IF;

    audit_row = ROW(
        nextval('audit_trail_id_seq'),        -- id
        TG_TABLE_NAME::varchar,               -- table_name
        current_timestamp AT TIME ZONE 'utc', -- tstamp
        substring(TG_OP,1,1),                 -- action
        NEW.id,                               -- object_id
        NULL, NULL                            -- row_data, changed_fields
        );

    IF TG_ARGV[1] IS NOT NULL THEN
        excluded_cols = TG_ARGV[1]::text[];
    END IF;

    IF (TG_OP = 'UPDATE' AND TG_LEVEL = 'ROW') THEN
        audit_row.row_data = row_to_json(OLD)::JSONB;
        audit_row.changed_fields = row_to_json(NEW)::JSONB - audit_row.row_data - excluded_cols;
        IF audit_row.changed_fields = '{}'::jsonb THEN
            -- All changed fields are ignored. Skip this update.
            RETURN NULL;
        END IF;
    ELSIF (TG_OP = 'INSERT' AND TG_LEVEL = 'ROW') THEN
        audit_row.row_data = row_to_json(NEW)::JSONB;
        audit_row.changed_fields = jsonb_strip_nulls(row_to_json(NEW)::JSONB - excluded_cols);
    ELSE
        RAISE EXCEPTION '[if_modified_func] - Trigger func added as trigger for unhandled case: %, %',TG_OP, TG_LEVEL;
        RETURN NULL;
    END IF;
    INSERT INTO audit_trail(id, table_name, tstamp, action, object_id, row_data, changed_fields)
    VALUES (
        audit_row.id, 
        audit_row.table_name,
        audit_row.tstamp,
        audit_row.action,
        audit_row.object_id,
        audit_row.row_data,
        audit_row.changed_fields);
    RETURN NULL;
END;
$body$
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = master, public;

DROP VIEW stamm.audit_trail;
CREATE VIEW stamm.audit_trail AS SELECT
	id,
	CASE
		WHEN table_name = 'site' THEN 'ort'
		ELSE table_name
	END AS table_name,
	tstamp,
	action,
	object_id,
	CASE
		WHEN table_name = 'site' THEN public.jsonb_rename_keys(row_data, ARRAY['network_id', 'netzbetreiber_id', 'ext_id', 'ort_id', 'long_text', 'langtext', 'state_id', 'staat_id', 'munic_id', 'gem_id', 'is_fuzzy', 'unscharf', 'nuts_id', 'nuts_code', 'spat_ref_sys_id', 'kda_id', 'coord_x_ext', 'koord_x_extern', 'coord_y_ext', 'koord_y_extern', 'alt', 'hoehe_land', 'last_mod', 'letzte_aenderung', 'site_class_id', 'ort_typ', 'short_text', 'kurztext', 'rei_report_text', 'berichtstext', 'rei_zone', 'zone', 'rei_sector', 'sektor', 'rei_competence', 'zustaendigkeit', 'rei_opr_mode', 'mp_art', 'is_rei_active', 'aktiv', 'rei_nucl_facil_gr_id', 'kta_gruppe_id', 'poi_id', 'oz_id', 'height_asl', 'hoehe_ueber_nn', 'rei_ag_gr_id', 'rei_progpunkt_grp_id', 'munic_div_id', 'gem_unt_id'])
		ELSE row_data
	END AS row_data,
	CASE
		WHEN table_name = 'site' THEN public.jsonb_rename_keys(changed_fields, ARRAY['network_id', 'netzbetreiber_id', 'ext_id', 'ort_id', 'long_text', 'langtext', 'state_id', 'staat_id', 'munic_id', 'gem_id', 'is_fuzzy', 'unscharf', 'nuts_id', 'nuts_code', 'spat_ref_sys_id', 'kda_id', 'coord_x_ext', 'koord_x_extern', 'coord_y_ext', 'koord_y_extern', 'alt', 'hoehe_land', 'last_mod', 'letzte_aenderung', 'site_class_id', 'ort_typ', 'short_text', 'kurztext', 'rei_report_text', 'berichtstext', 'rei_zone', 'zone', 'rei_sector', 'sektor', 'rei_competence', 'zustaendigkeit', 'rei_opr_mode', 'mp_art', 'is_rei_active', 'aktiv', 'rei_nucl_facil_gr_id', 'kta_gruppe_id', 'poi_id', 'oz_id', 'height_asl', 'hoehe_ueber_nn', 'rei_ag_gr_id', 'rei_progpunkt_grp_id', 'munic_div_id', 'gem_unt_id'])
		ELSE changed_fields
	END AS changed_fields
FROM master.audit_trail;

CREATE VIEW stamm.audit_trail_ort AS SELECT
	id,
	CASE
		WHEN table_name = 'site' THEN 'ort'
		ELSE table_name
	END AS table_name,
	tstamp,
	action,
	object_id,
	CASE
		WHEN table_name = 'site' THEN public.jsonb_rename_keys(row_data, ARRAY['network_id', 'netzbetreiber_id', 'ext_id', 'ort_id', 'long_text', 'langtext', 'state_id', 'staat_id', 'munic_id', 'gem_id', 'is_fuzzy', 'unscharf', 'nuts_id', 'nuts_code', 'spat_ref_sys_id', 'kda_id', 'coord_x_ext', 'koord_x_extern', 'coord_y_ext', 'koord_y_extern', 'alt', 'hoehe_land', 'last_mod', 'letzte_aenderung', 'site_class_id', 'ort_typ', 'short_text', 'kurztext', 'rei_report_text', 'berichtstext', 'rei_zone', 'zone', 'rei_sector', 'sektor', 'rei_competence', 'zustaendigkeit', 'rei_opr_mode', 'mp_art', 'is_rei_active', 'aktiv', 'rei_nucl_facil_gr_id', 'kta_gruppe_id', 'poi_id', 'oz_id', 'height_asl', 'hoehe_ueber_nn', 'rei_ag_gr_id', 'rei_progpunkt_grp_id', 'munic_div_id', 'gem_unt_id'])
		ELSE row_data
	END AS row_data,
	CASE
		WHEN table_name = 'site' THEN public.jsonb_rename_keys(changed_fields, ARRAY['network_id', 'netzbetreiber_id', 'ext_id', 'ort_id', 'long_text', 'langtext', 'state_id', 'staat_id', 'munic_id', 'gem_id', 'is_fuzzy', 'unscharf', 'nuts_id', 'nuts_code', 'spat_ref_sys_id', 'kda_id', 'coord_x_ext', 'koord_x_extern', 'coord_y_ext', 'koord_y_extern', 'alt', 'hoehe_land', 'last_mod', 'letzte_aenderung', 'site_class_id', 'ort_typ', 'short_text', 'kurztext', 'rei_report_text', 'berichtstext', 'rei_zone', 'zone', 'rei_sector', 'sektor', 'rei_competence', 'zustaendigkeit', 'rei_opr_mode', 'mp_art', 'is_rei_active', 'aktiv', 'rei_nucl_facil_gr_id', 'kta_gruppe_id', 'poi_id', 'oz_id', 'height_asl', 'hoehe_ueber_nn', 'rei_ag_gr_id', 'rei_progpunkt_grp_id', 'munic_div_id', 'gem_unt_id'])
		ELSE changed_fields
	END AS changed_fields,
	site_id AS ort_id
FROM master.audit_trail;
