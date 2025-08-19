--delete LFGB default value trigger and restore column default; LFGB only allows self-defined ext_ids
ALTER TABLE lada.sample
ALTER COLUMN ext_id SET DEFAULT ('ZDB'::text || lpad(((nextval('lada.sample_sample_id_seq'::regclass))::character varying)::text, 12, '0'::text)) || 'Y'::text;

DROP SEQUENCE lada.sample_lfgb_ext_id_seq;
DROP TRIGGER trg_set_sample_ext_id_default ON lada.sample;
DROP FUNCTION lada.set_default_sample_ext_id();

