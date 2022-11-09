SET role lada;

--ALTER INDEX IF EXISTS probe_id_ndx RENAME TO sample_id_ndx;
--ALTER INDEX IF EXISTS messung_id_ndx RENAME TO measm_id_ndx;
--ALTER INDEX IF EXISTS master.ort_id_ndx RENAME TO site_id_ndx;
ALTER INDEX IF EXISTS lada.messung_probe_id_idx RENAME TO measm_sample_id_idx;
ALTER INDEX IF EXISTS lada.ort_probe_id_idx RENAME TO site_sample_id_idx;
ALTER INDEX IF EXISTS lada.zusatz_wert_probe_id_idx RENAME TO sample_specif_meas_val_sample_id_idx;
ALTER INDEX IF EXISTS lada.kommentar_probe_id_idx RENAME TO comm_sample_id_idx;
ALTER INDEX IF EXISTS lada.messwert_messungs_id_idx RENAME TO meas_val_measm_id_idx;
ALTER INDEX IF EXISTS lada.status_messungs_id_idx RENAME TO status_prot_measm_id_idx;
ALTER INDEX IF EXISTS lada.kommentar_messungs_id_idx RENAME TO comm_measm_id_idx;
ALTER INDEX IF EXISTS master.ort_netz_id_idx RENAME TO site_network_id_idx;
ALTER INDEX IF EXISTS lada.idx_land_probe_mitte_sammelzeitraum_idx RENAME TO state_sample_mid_collect_period_id_ndx;
ALTER INDEX IF EXISTS master.auto_tag_unique_idx RENAME TO is_auto_tag_unique_idx;
ALTER INDEX IF EXISTS master.fts_stauts_kooin10001 RENAME TO fts_status_kooin10001;

DROP INDEX IF EXISTS lada.probe_id_ndx;
CREATE INDEX measm_id_ndx
    ON lada.audit_trail USING btree
    (((row_data ->> 'measm_id'::text)::integer) ASC NULLS LAST)
    TABLESPACE pg_default;
DROP INDEX IF EXISTS lada.messung_id_ndx;
CREATE INDEX sample_id_ndx
    ON lada.audit_trail USING btree
    (((row_data ->> 'sample_id'::text)::integer) ASC NULLS LAST)
    TABLESPACE pg_default;

DROP INDEX IF EXISTS master.ort_id_ndx;
CREATE INDEX site_id_ndx
    ON master.audit_trail USING btree
    (((row_data ->> 'object_id'::text)::character varying) COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;