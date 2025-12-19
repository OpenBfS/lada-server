CREATE FUNCTION lada.set_status_prot_seq_no() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    BEGIN
        NEW.seq_no = (
            SELECT coalesce(max(seq_no), 0)
               FROM lada.status_prot
               WHERE measm_id = NEW.measm_id) + 1;
        RETURN NEW;
    END;
$$;

ALTER TABLE lada.status_prot
    ADD COLUMN seq_no smallint,
    ADD UNIQUE(measm_id, seq_no);
UPDATE lada.status_prot target SET seq_no = (
    SELECT count(*) + 1 FROM lada.status_prot source
        WHERE source.measm_id = target.measm_id AND source.id < target.id);
ALTER TABLE lada.status_prot ALTER seq_no SET NOT NULL;

CREATE TRIGGER seq_no BEFORE INSERT ON lada.status_prot
    FOR EACH ROW EXECUTE PROCEDURE lada.set_status_prot_seq_no();
