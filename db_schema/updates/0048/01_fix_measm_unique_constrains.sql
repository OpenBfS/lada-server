SET role lada;

ALTER TABLE lada.measm DROP CONSTRAINT measm_id_ext_id_key;

ALTER TABLE lada.measm DROP CONSTRAINT measm_id_min_sample_id_key;

ALTER TABLE lada.measm
    ADD CONSTRAINT measm_sample_id_ext_id_key UNIQUE (sample_id, ext_id);

ALTER TABLE lada.measm
    ADD CONSTRAINT measm_sample_id_min_sample_id_key UNIQUE (sample_id, min_sample_id);
