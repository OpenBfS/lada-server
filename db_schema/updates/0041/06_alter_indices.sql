SET role lada;

ALTER INDEX IF EXISTS probe_id_ndx RENAME TO sample_id_ndx;
ALTER INDEX IF EXISTS messung_id_ndx RENAME TO measm_id_ndx;
ALTER INDEX IF EXISTS ort_id_ndx RENAME TO site_id_ndx;
ALTER INDEX IF EXISTS messung_probe_id_idx RENAME TO measm_sample_id_idx;
ALTER INDEX IF EXISTS ort_probe_id_idx RENAME TO site_sample_id_idx;
ALTER INDEX IF EXISTS zusatz_wert_probe_id_idx RENAME TO sample_specif_meas_val_sample_id_idx;
ALTER INDEX IF EXISTS kommentar_probe_id_idx RENAME TO comm_sample_id_idx;
ALTER INDEX IF EXISTS messwert_messungs_id_idx RENAME TO meas_val_measm_id_idx;
ALTER INDEX IF EXISTS status_messungs_id_idx RENAME TO status_prot_measm_id_idx;
ALTER INDEX IF EXISTS kommentar_messungs_id_idx RENAME TO comm_measm_id_idx;
ALTER INDEX IF EXISTS ort_netz_id_idx RENAME TO site_network_id_idx;
ALTER INDEX IF EXISTS ort_id_ndx RENAME TO site_id_ndx;
ALTER INDEX IF EXISTS idx_land_probe_mitte_sammelzeitraum_idx RENAME TO state_sample_mid_collect_period_id_ndx;


