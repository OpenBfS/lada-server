ALTER TABLE lada.sample
ALTER COLUMN ext_id
DROP DEFAULT;

CREATE SEQUENCE IF NOT EXISTS lada.sample_lfgb_ext_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE OR REPLACE FUNCTION lada.set_default_sample_ext_id()
RETURNS TRIGGER AS $$
BEGIN
    RAISE NOTICE 'regulation_id: %, ext_id: %', NEW.regulation_id, NEW.ext_id;
    IF (NEW.regulation_id = 8 AND NEW.ext_id IS NULL) THEN
        NEW.ext_id := 'LFGB' || lpad(((nextval('lada.sample_lfgb_ext_id_seq'::regclass))::character varying)::text, 12, '0'::text);
    ELSIF (NEW.regulation_id <> 8 AND NEW.ext_id IS NULL) THEN
        NEW.ext_id := ('ZDB'::text || lpad(((nextval('lada.sample_sample_id_seq'::regclass))::character varying)::text, 12, '0'::text)) || 'Y'::text;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_set_sample_ext_id_default
BEFORE INSERT ON lada.sample
FOR EACH ROW
EXECUTE FUNCTION lada.set_default_sample_ext_id();
