CREATE OR REPLACE VIEW land.messwert_view AS SELECT
	id,
	measm_id AS messungs_id,
	measd_id AS messgroesse_id,
	less_than_lod AS messwert_nwg,
	meas_val AS messwert,
	error AS messfehler,
	detect_lim AS nwg_zu_messwert,
	meas_unit_id AS meh_id,
	is_threshold AS grenzwertueberschreitung,
	status_mp_id AS status_kombi,
	last_mod AS letzte_aenderung
FROM public.lada_meas_val;

DROP VIEW lada.meas_val_view;
