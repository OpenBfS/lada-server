CREATE FUNCTION cleanup_meas_vals() RETURNS TRIGGER LANGUAGE plpgsql AS $$
    BEGIN
        IF (SELECT v.id FROM master.status_val v
            JOIN master.status_mp mp ON v.id = mp.status_val_id
            WHERE mp.id = NEW.status_mp_id) = 7 THEN
            DELETE FROM lada.meas_val
                WHERE measm_id = NEW.measm_id
                AND meas_val IS NULL AND less_than_lod IS NULL;
        END IF;
        RETURN NEW;
    END;
$$;
CREATE TRIGGER cleanup_meas_vals AFTER INSERT ON lada.status_prot
    FOR EACH ROW EXECUTE PROCEDURE cleanup_meas_vals();
