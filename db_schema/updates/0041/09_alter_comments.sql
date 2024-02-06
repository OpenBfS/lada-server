--START TRANSACTION;

SET search_path TO lada;

COMMENT ON COLUMN geolocat.type_regulation IS 'E = Entnahmeort, U = Ursprungsort, Z = Ortszusatz';
COMMENT ON COLUMN sample.id IS 'internal sample_id';
COMMENT ON COLUMN sample.is_test IS 'is test data?';
COMMENT ON COLUMN sample.meas_facil_id IS 'ID for measuring facility';
COMMENT ON COLUMN sample.appr_lab_id IS 'ID for approved laboratory';
COMMENT ON COLUMN sample.main_sample_id IS 'external sample id';
COMMENT ON COLUMN sample.opr_mode_id IS 'ID of operation mode (normal/Routine oder Störfall/intensiv)';
COMMENT ON COLUMN sample.sample_meth_id IS 'ID of sample method (Einzel-, Sammel-, Misch- ...Probe)';
COMMENT ON COLUMN sample.env_descrip_name IS 'dekodierte Medienbezeichnung (aus env_descrip_display abgeleitet)';
COMMENT ON COLUMN mpg.env_descrip_display IS 'Mediencodierung (Deskriptoren oder ADV-Codierung)';
COMMENT ON COLUMN sample.env_medium_id IS 'ID for environmental medium';


--COMMIT;